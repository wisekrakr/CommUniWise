package com.wisekrakr.communiwise.phone.sip;


import com.wisekrakr.communiwise.phone.calling.CallInstance;
import com.wisekrakr.communiwise.phone.sip.ext.SipClient;
import com.wisekrakr.communiwise.phone.sip.ext.SipSessionState;
import com.wisekrakr.communiwise.user.SipAccountManager;
import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.fields.AttributeField;
import gov.nist.javax.sdp.parser.AttributeFieldParser;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.message.SIPMessage;
import jdk.nashorn.internal.ir.annotations.Ignore;

import javax.sdp.MediaDescription;
import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.Message;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages a single SIP session
 */
public class SipManager implements SipClient {
    private static final String STACK_NAME = "WiseKrakrSIP";
    //    private static final String STACK_DOMAIN_NAME = "com.wisekrakr";
    private static final int MAX_MESSAGE_SIZE = 1048576;

    private final String localSipAddress;
    private final int localSipPort;
    private final String sipTransport;
    private final String proxyHost;
    private final int proxyPort;
    private SipAccountManager accountManager;

    private int traceLevel = 0;
    private String serverLogFile;
    private String debugLogFile;
    private SipManagerListener listener;
    private Address clientAddress;
    private AuthenticationHelper authenticationHelper;
    private CallIdHeader callId;

    private SipStack sipStack;

    private SipProvider sipProvider;

    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private MessageFactory messageFactory;

    private SipSessionState sipSessionState;

    private ServerTransaction waitingCall;
    private ClientTransaction currentCall;

    private final HashMap<String, CallInstance>callInstances = new HashMap<>();

    private int status;

    public SipManager(String proxyHost, int proxyPort, String localSipAddress, int localSipPort, String sipTransport) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.localSipAddress = localSipAddress;
        this.localSipPort = localSipPort;
        this.sipTransport = sipTransport;

    }

    public SipManager listener(SipManagerListener listener) {
        if (this.listener != null) {
            throw new IllegalStateException("Already have a listener");
        }

        this.listener = listener;

        return this;
    }

    public SipManager logging(String serverLogFile, String debugLogFile, int traceLevel) {
        this.serverLogFile = serverLogFile;
        this.debugLogFile = debugLogFile;
        this.traceLevel = traceLevel;

        return this;
    }

    public void initialize(SipAccountManager accountManager) throws Exception {
        this.accountManager = accountManager;

        sipSessionState = SipSessionState.IDLE;

        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.resetFactory();
//        sipFactory.setPathName(STACK_DOMAIN_NAME);

        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();

        Properties properties = new Properties();

        properties.setProperty("javax.sip.OUTBOUND_PROXY", proxyHost + ":" + proxyPort + "/" + sipTransport);          // TODO: here? it's a router parameter
        properties.setProperty("javax.sip.STACK_NAME", STACK_NAME);
        properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", Integer.toString(MAX_MESSAGE_SIZE));

        if (debugLogFile != null) {
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", debugLogFile);
        }
        if (serverLogFile != null) {
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG", serverLogFile);
        }

        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", Integer.toString(traceLevel));

        // Drop the client connection after we are done with the transaction.
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");

        sipStack = sipFactory.createSipStack(properties);

        authenticationHelper = ((SipStackExt) sipStack).getAuthenticationHelper(accountManager, headerFactory);

        ListeningPoint udp = sipStack.createListeningPoint(localSipAddress, localSipPort, sipTransport);

        sipProvider = sipStack.createSipProvider(udp);

        callId = sipProvider.getNewCallId();

        sipProvider.addSipListener(
                new SipListener() {
                    @Override
                    public void processRequest(RequestEvent requestEvent) {
//                        if (requestEvent.getServerTransaction() == null) {
//                            System.out.println("Sending request\n" + requestEvent.getRequest());
//                            try {
//                                sipProvider.sendRequest(requestEvent.getRequest());
//
//                            } catch (Exception e) {
//                                System.out.println("Whatever " + e.getMessage());
//                                e.printStackTrace();
//
//                            }
//                            return;
//                        }

                        System.out.println("Processing server request\n" + requestEvent.getRequest());
                        System.out.println("SipSessionState :" + sipSessionState);


                        try {
                            Request request = requestEvent.getRequest();
                            ServerTransaction transaction = requestEvent.getServerTransaction();

                            if(transaction == null){
                                transaction = sipProvider.getNewServerTransaction(request);
                            }

                            SIPMessage sp = (SIPMessage) request;

                            System.out.println("\n\nRequest " + request.getMethod() + " received at " + sipStack.getStackName() + " with server transaction id " + transaction);

                            SipURI uri = (SipURI) ((FromHeader) request.getHeader(FromHeader.NAME)).getAddress().getURI();
                            System.out.println("Request Header: " + uri);

                            switch (request.getMethod()) {
                                case Request.MESSAGE:
                                    sendOk(requestEvent);

                                    String message = null;
                                    try {
                                        message = sp.getMessageContent();
                                    } catch (UnsupportedEncodingException e) {
                                        System.out.println("ERROR: unsupported encoding receiving message");
                                        break;
                                    }

                                    listener.onTextMessage(message, sp.getFrom().getAddress().toString());

                                    break;

                                case Request.BYE:
                                    sipSessionState = SipSessionState.IDLE;

                                    System.out.println("BYE received");

                                    if (transaction == null) {
                                        System.out.println("Process Bye:  null TID.");
                                    } else {
                                        Dialog dialog = transaction.getDialog();
                                        Response response = messageFactory.createResponse(Response.OK, requestEvent.getRequest());
                                        transaction.sendResponse(response);
                                        System.out.println("Sending OK");
                                        System.out.println("Dialog State = " + dialog.getState());
                                    }

                                    listener.onRemoteBye(callId.getCallId());
                                    break;

                                case Request.ACK:
                                    System.out.println("Process Ack: got an ACK! " + request);

                                    if (transaction == null) {
                                        System.out.println("null server transaction -- ignoring the ACK!");
                                    } else {
                                        Dialog dialog = transaction.getDialog();
                                        System.out.println("Dialog Created = " + dialog.getDialogId() + " Dialog State = " + dialog.getState());
                                        System.out.println("Waiting for INFO");
                                    }

                                    break;
                                case Request.INVITE:
                                    if (sipSessionState != SipSessionState.IDLE
                                            && sipSessionState != SipSessionState.READY
                                            && sipSessionState != SipSessionState.INCOMING
                                    ) {
                                        sendDecline(requestEvent.getServerTransaction());// Already in a call
                                    } else {
                                        sipSessionState = SipSessionState.INCOMING;

                                        transaction.sendResponse(messageFactory.createResponse(Response.TRYING, request));

                                        waitingCall = transaction;

                                        listener.onRinging(callId.getCallId(),sp.getFrom().getAddress().getDisplayName() , sp.getRemoteAddress().getHostAddress(), sp.getRemotePort());

                                        InetSocketAddress proxyAddress = new InetSocketAddress(sp.getRemoteAddress().getHostAddress(), sp.getRemotePort());
                                        CallInstance callInstance = new CallInstance(callId.getCallId(), sp.getFrom().getAddress().getDisplayName(),proxyAddress);

                                        callInstances.put(callId.getCallId(), callInstance);
                                    }

                                    break;

                                case Request.CANCEL:
                                    sipSessionState = SipSessionState.IDLE;

                                    System.out.println("CANCEL received");
                                    if (transaction == null) {
                                        System.out.println("Process Cancel:  null TID.");
                                    } else {
                                        Dialog dialog1 = transaction.getDialog();
                                        System.out.println("Dialog State = " + dialog1.getState());

                                        transaction.sendResponse(messageFactory.createResponse(Response.OK, requestEvent.getRequest()));

                                        System.out.println("Sending 200 Canceled Request");
                                        System.out.println("Dialog State = " + dialog1.getState());

                                    }

                                    listener.onRemoteCancel();

                                    break;

                                default:
                                    throw new IllegalStateException("Unexpected request method: " + request.getMethod());
                            }

                        } catch (Throwable t) {
                            System.out.println("Error while processing request event " + requestEvent);
                            t.printStackTrace();
                        }
                    }

                    @Override
                    public void processResponse(ResponseEvent responseEvent) {
                        try {
                            System.out.println("Processing response\n" + responseEvent.getResponse());

                            Response processedResponse = responseEvent.getResponse();
                            CSeqHeader cseq = (CSeqHeader) processedResponse.getHeader(CSeqHeader.NAME);
                            System.out.println("Response received : Status Code = " + processedResponse.getStatusCode() + " " + cseq);
                            ClientTransaction clientTransaction = responseEvent.getClientTransaction();

                            currentCall = clientTransaction;

                            status = processedResponse.getStatusCode();

                            if (status == Response.PROXY_AUTHENTICATION_REQUIRED || status == Response.UNAUTHORIZED) {
                                System.out.println("Go for Authentication");

                                try {
                                    authenticationHelper.handleChallenge(processedResponse, clientTransaction, sipProvider, 5).sendRequest();
                                } catch (Exception e) {
                                    listener.authenticationFailed();

                                    sipSessionState = SipSessionState.IDLE;
                                }
                            } else if (processedResponse.getStatusCode() == Response.OK) {
                                switch (cseq.getMethod()) {
                                    case Request.REGISTER:
                                        System.out.println("REGISTERED");

                                        sipSessionState = SipSessionState.IDLE;

                                        listener.onRegistered();
                                        break;

                                    case Request.INVITE:
                                        System.out.println("Dialog after 200 OK  ");
                                        sipSessionState = SipSessionState.OUTGOING;

                                        clientTransaction.getDialog().sendAck(clientTransaction.getDialog().createAck(cseq.getSeqNumber()));

                                        SessionDescriptionImpl sessionDescription = getSessionDescription(processedResponse);

                                        if (sessionDescription.getMediaDescriptions(true).size() != 1) {
                                            System.out.println("number of media descriptions != 1, will take the first anyway");
                                        }

                                        MediaDescription incomingMediaDescriptor = (MediaDescription) sessionDescription.getMediaDescriptions(false).get(0);

                                        String codec = parseAttribute((AttributeField) incomingMediaDescriptor.getAttributes(false).get(0));
                                        listener.callConfirmed(
                                                sessionDescription.getConnection().getAddress(),
                                                incomingMediaDescriptor.getMedia().getMediaPort(),
                                                codec,
                                                callId.getCallId()
                                        );
                                        break;

                                    case Request.CANCEL:
                                        if (clientTransaction.getDialog().getState() == DialogState.CONFIRMED) {
                                            System.out.println("Sending BYE -- cancel went in too late !!");
                                            Request byeRequest = clientTransaction.getDialog().createRequest(Request.BYE);

                                            ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
                                            clientTransaction.getDialog().sendRequest(ct);
                                        }

                                        break;

                                    case Request.BYE:
                                        sipSessionState = SipSessionState.IDLE;
                                        System.out.println("--- Got 200 OK in UAC outgoing BYE from host");

                                        listener.onBye(callId.getCallId());
                                        break;


                                    default:
                                        throw new IllegalStateException("Unknown request type in response: " + responseEvent);
                                }

                            } else if (processedResponse.getStatusCode() == Response.DECLINE || processedResponse.getStatusCode() == Response.TEMPORARILY_UNAVAILABLE) {
                                System.out.println("CALL DECLINED");
                                listener.onRemoteDeclined();

                            } else if (processedResponse.getStatusCode() == Response.NOT_FOUND) {
                                System.out.println("NOT FOUND");
                            } else if (processedResponse.getStatusCode() == Response.ACCEPTED) {
                                System.out.println("ACCEPTED");
                                listener.onRemoteAccepted();

                            } else if (processedResponse.getStatusCode() == Response.BUSY_HERE) {
                                System.out.println("BUSY");
                                listener.onBusy();
                            } else if (processedResponse.getStatusCode() == Response.RINGING) {
                                System.out.println("RINGING");
//                                listener.onRinging(callId, sp.getFrom().getAddress().toString());
                            } else if (processedResponse.getStatusCode() == Response.SERVICE_UNAVAILABLE) {
                                System.out.println("SERVICE_UNAVAILABLE");
                                listener.onUnavailable();
                            } else if (processedResponse.getStatusCode() == Response.TRYING) {
                                System.out.println("Trying...");
                                listener.onTrying();
                            }
                            else if (processedResponse.getStatusCode() == Response.FORBIDDEN) {
                                System.out.println("FORBIDDEN!");
                                sipSessionState = SipSessionState.IDLE;
//                                listener.authenticationFailed();
                            }
                            else {
                                throw new IllegalStateException("Unknown status code " + processedResponse.getStatusCode());
                            }

                        } catch (Throwable t) {
                            System.out.println("Error while processing response " + responseEvent);
                            t.printStackTrace();
                        }
                    }


                    @Override
                    public void processTimeout(TimeoutEvent timeoutEvent) {
                        Transaction transaction;
                        if (timeoutEvent.isServerTransaction()) {
                            transaction = timeoutEvent.getServerTransaction();
                        } else {
                            transaction = timeoutEvent.getClientTransaction();
                            System.out.println(timeoutEvent.getTimeout().getValue());
                        }
                        System.out.println("state = " + transaction.getState());
                        System.out.println("dialog = " + transaction.getDialog());
                        System.out.println("Transaction Time out");
                    }

                    @Override
                    public void processIOException(IOExceptionEvent ioExceptionEvent) {
                        System.out.println("IOException happened for "
                                + ioExceptionEvent.getHost() + " port = "
                                + ioExceptionEvent.getPort());
                    }

                    @Override
                    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
                        if (transactionTerminatedEvent.isServerTransaction())
                            System.out.println("Server Transaction terminated event received "
                                    + transactionTerminatedEvent.getServerTransaction());
                        else {
                            System.out.println("Client Transaction terminated "
                                    + transactionTerminatedEvent.getClientTransaction());
                        }
                    }

                    @Override
                    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
                        System.out.println("processDialogTerminated: " + dialogTerminatedEvent);
                    }
                }
        );

        sipSessionState = SipSessionState.IDLE;

    }

    private String parseAttribute(AttributeField attribute) {
        try {
            AttributeFieldParser attributeFieldParser = new AttributeFieldParser(attribute.toString());
            AttributeField attributeField = attributeFieldParser.attributeField();

            if(!attributeField.encode().isEmpty()){
                System.out.println("encoded: " + attributeField.encode());

                return attributeField.encode();
            }
        }catch (ParseException e){
            System.out.println(" Unable to parse the attributes " + e);
        }
        return null;
    }

    private SessionDescriptionImpl getSessionDescription(Message message){
        SessionDescriptionImpl sessionDescription = null;
        try {
            byte[] rawContent = message.getRawContent();
            String sdpContent = new String(rawContent, StandardCharsets.UTF_8);
            SDPAnnounceParser parser = new SDPAnnounceParser(sdpContent);
            sessionDescription = parser.parse();


            if (sessionDescription.getMediaDescriptions(true).size() != 1) {
                System.out.println("number of media descriptions != 1, will take the first anyway");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return sessionDescription;
    }

    /************                               ACTIONS                             ************/

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void sendTextMessage(String recipient, String message) {
        try {
            this.sipProvider.getNewClientTransaction(makeMessageRequest(recipient, message)).sendRequest();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to send message", e);
        }
    }

    @Override
    public void login(String realm, String username, String password, String domain, String fromAddress) {
        assureState(SipSessionState.IDLE);

        if(fromAddress.isEmpty()){
            fromAddress = "sip:nobody@null.null";
        }

        try {
            clientAddress = addressFactory.createAddress(fromAddress);
        } catch (ParseException e) {
            listener.authenticationFailed();

            throw new IllegalArgumentException("Invalid from address " + fromAddress, e);

        }

        accountManager.clear();
        accountManager.addCredentials(realm, username, password, domain);

        sipSessionState = SipSessionState.REGISTERING;
        try {
            this.sipProvider.getNewClientTransaction(createRegisterRequest()).sendRequest();
        } catch (Exception e) {
            sipSessionState = SipSessionState.IDLE;

            throw new IllegalStateException("Unable to register", e);
        }


    }

    private void assureState(SipSessionState expectedState) {
        if (sipSessionState != expectedState) {
            throw new IllegalStateException("Invalid state, expected " + expectedState + " actual " + sipSessionState);
        }
    }

    @Override
    public void initiateCall(String recipient, int localRtpPort) {
        try {
            this.sipProvider.getNewClientTransaction(makeInviteRequest(recipient, localRtpPort)).sendRequest();
        } catch (Throwable e) {
            sipSessionState = SipSessionState.READY;

            throw new IllegalStateException("Call failed " , e);
        }
    }

    @Override
    public void hangup(String recipient, String callId) {

        try{
            if (sipSessionState == SipSessionState.INCOMING) {
//                sipProvider.getNewClientTransaction(makeByeRequest(recipient)).sendRequest();
                makeByeRequest(waitingCall);

            }else if(sipSessionState == SipSessionState.OUTGOING){
                makeByeRequest(currentCall);

            }
        }catch (Throwable e) {
            throw new IllegalStateException("Unable to hang up",e);
        }
    }

    private void makeByeRequest(Transaction transaction){
        final Dialog dialog = transaction.getDialog();

        if(dialog == null){
            throw new IllegalStateException("Dialog can't be null");
        }else{
            try {
                ClientTransaction newTransaction = sipProvider.getNewClientTransaction(dialog.createRequest(Request.BYE));

                dialog.sendRequest(newTransaction);
            }catch (Throwable e){
                throw new IllegalStateException("Could not send bye request", e);
            }
        }
    }

    @Override
    public void acceptCall(final int port) {
        if (waitingCall == null) {
            throw new IllegalStateException("No call waiting to be accepted");
        }

        try {
            Response responseOK = messageFactory.createResponse(Response.OK, waitingCall.getRequest());
            responseOK.addHeader(headerFactory.createContactHeader(clientAddress));

            ToHeader toHeader = (ToHeader) responseOK.getHeader(ToHeader.NAME);
            FromHeader fromHeader = (FromHeader) responseOK.getHeader(FromHeader.NAME);

            toHeader.setTag(currentCallTag());
            responseOK.addHeader(toHeader);

            // TODO: this line should be generated (e.g. it announces codecs now
            // TODO: cf https://tools.ietf.org/html/rfc3555  https://andrewjprokop.wordpress.com/2013/09/30/understanding-session-description-protocol-sdp/

            String sdpData =
                      "v=0\r\n"
                    + "o=yomama 1234 1234 IN IP4 " + localSipAddress + "\r\n"
                    + "s=Communwise 0.9.0 beta\r\n"
                    + "c=IN IP4 " + localSipAddress + "\r\n"
                    + "t=0 0\r\n"
                    + "m=audio " + port + " RTP/AVP 9\r\n"
//                    + "a=rtpmap:8 PCMA/8000\r\n";
                    + "a=rtpmap:9 G722/8000\r\n";
//                    + "a=rtpmap:18 G729A/8000\r\n"
 //                   + "a=ptime:20\r\n";


            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
            responseOK.setContent(sdpData.getBytes(), contentTypeHeader);

            waitingCall.sendResponse(responseOK);

            for (Map.Entry<String, CallInstance> c : callInstances.entrySet()) {
                if (c.getValue().getProxy().contains(fromHeader.getAddress().getDisplayName())) {
                    listener.onAccepted(c.getKey(), c.getValue().getProxyAddress().getAddress().getHostAddress(),c.getValue().getProxyAddress().getPort(),null);
                }
            }

//            sipSessionState = SipSessionState.ESTABLISHED;
        } catch (Throwable e) {
            throw new IllegalStateException("Unable to accept call", e);
        }
    }

    @Override
    public void reject() {
        if (waitingCall == null) {
            throw new IllegalStateException("No call waiting to be accepted");
        }

        try {
            sendDecline(waitingCall);
        } catch (Throwable e) {
            throw new IllegalStateException("Unable to send decline", e);
        }
        sipSessionState = SipSessionState.IDLE;
    }

    private void sendDecline(ServerTransaction request) throws ParseException, SipException, InvalidArgumentException {
        request.sendResponse(messageFactory.createResponse(Response.DECLINE, request.getRequest()));
    }

    private void sendOk(RequestEvent requestEvt) throws ParseException, SipException, InvalidArgumentException {
        requestEvt.getServerTransaction().sendResponse(messageFactory.createResponse(Response.OK, requestEvt.getRequest()));
    }


    private Address createContactAddress() {
        try {
            return this.addressFactory.createAddress("sip:"
                    + accountManager.getUserInfo().get("username") + "@"
                    + localSipAddress + ":"+ localSipPort + ";transport=udp"
                    + ";registering_acc=" + accountManager.getUserInfo().get("domain"));
        } catch (ParseException e) {
            return null;
        }
    }

    public Request createRegisterRequest() throws ParseException, InvalidArgumentException {
        return createRequest(addressFactory.createAddress("sip:" + proxyHost + ":" + proxyPort).getURI(), clientAddress, clientAddress, Request.REGISTER, null);
    }

    @Deprecated
    @Ignore
    private Request makeByeRequest(String to) throws ParseException, InvalidArgumentException{
        return createRequest(addressFactory.createURI(to), clientAddress, addressFactory.createAddress(to), Request.BYE, null);
    }

    public Request makeInviteRequest(String to, int localRtpPort) throws ParseException, InvalidArgumentException {
        Request callRequest = createRequest(addressFactory.createURI(to), clientAddress, addressFactory.createAddress(to), Request.INVITE, null);

        // Create ContentTypeHeader
        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");

        // Create the contact name address.
        SipURI contactURI = addressFactory.createSipURI(accountManager.getUserInfo().get("username"), localSipAddress); //todo right user name/extension
        contactURI.setPort(localSipPort);

        callRequest.addHeader(headerFactory.createContactHeader(addressFactory.createAddress(contactURI)));

        //TODO: PCMA/8000 was PCMU/8000
//            String sdpData= "v=0\r\n" +
//                    "o=- 13760799956958020 13760799956958020" + " IN IP4 " + sipProfile.getLocalIp() +"\r\n" +
//                    "s=mysession session\r\n" +
//                    "s=-\r\n" +
//                    //"p=+46 8 52018010\r\n" +
//                    "c=IN IP4 " + sipProfile.getLocalIp()+"\r\n" +
//                    "t=0 0\r\n" +
//                    "m=audio " + port + " RTP/AVP 0\r\n" +
//                    "m=audio " + port + " RTP/AVP 0 4 18\r\n" +
//                    "a=rtpmap:0 PCMU/8000\r\n" + //was PCMA
//                    "a=rtpmap:4 G723/8000\r\n" +
//                    "a=rtpmap:18 G729A/8000\r\n" +
////                    "a=rtpmap:18 G7222/16000\r\n" +
//                    "a=ptime:20\r\n";

        callRequest.setContent(("v=0\r\n" +
                "o=- 13760799956958020 13760799956958020" + " IN IP4 " + localSipAddress + "\r\n" +
                "s=mysession session\r\n" +
                "c=IN IP4 " + localSipAddress + "\r\n" +
                "t=0 0\r\n" +
                "m=audio " + localRtpPort + " RTP/AVP 9\r\n" +
//                "m=audio " + localRtpPort + " RTP/AVP 0 4 18 101\r\n" +
//                "a=rtpmap:0 PCMU/8000\r\n" +
                "a=rtpmap:9 G722/8000\r\n" +
//                "a=rtpmap:18 G729A/8000\r\n" +
//                "a=rtpmap:101 telephone-event/8000\r\n" +
                "a=maxptime:150\r\n" +
                "a=sendrecv\r\n"
//                "a=ptime:20\r\n"
        ).getBytes(), contentTypeHeader);

        callRequest.addHeader(headerFactory.createHeader("sipphone.Call-Info", "<http://www.antd.nist.gov>"));

        System.out.println("Our INVITE Request: \r\n" + callRequest);

        return callRequest;

    }

    private final AtomicLong sequenceNumberGenerator = new AtomicLong();

    private CSeqHeader nextRequestSequenceNumber(String method) throws ParseException, InvalidArgumentException {
        return headerFactory.createCSeqHeader(sequenceNumberGenerator.incrementAndGet(), method);
    }

    public Request makeMessageRequest(String to, String content) throws ParseException, InvalidArgumentException {
        URI toAddress = addressFactory.createURI(to);
        Address toNameAddress = addressFactory.createAddress(toAddress);

        return createRequest(toAddress, clientAddress, toNameAddress, Request.MESSAGE, content);
    }

    private String currentCallTag(){
        return callId.getCallId().substring(1,12);
    }

    public Request createRequest(URI requestURI, Address from, Address to, String method, Object content) throws ParseException, InvalidArgumentException {

        FromHeader fromHeader = headerFactory.createFromHeader(from, "metag");
        ToHeader toHeader = headerFactory.createToHeader(to, null);

        MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

        Request request = messageFactory.createRequest(
                requestURI,
                method,
                callId,
                nextRequestSequenceNumber(method),
                fromHeader,
                toHeader,
                Collections.emptyList(),
                maxForwards);

        SupportedHeader supportedHeader = headerFactory.createSupportedHeader("replaces, outbound");
        request.addHeader(supportedHeader);

        SipURI routeUri = addressFactory.createSipURI(null, proxyHost);
        routeUri.setTransportParam(sipTransport);
        routeUri.setLrParam();
        routeUri.setPort(proxyPort);

        request.addHeader(headerFactory.createContactHeader(createContactAddress()));

        if (content != null) {
            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
            request.setContent(content, contentTypeHeader);
        }
        authenticationHelper.setAuthenticationHeaders(request);

        System.out.println(request);

        return request;
    }
}
