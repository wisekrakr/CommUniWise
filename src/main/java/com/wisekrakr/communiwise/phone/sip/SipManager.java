package com.wisekrakr.communiwise.phone.sip;


import com.wisekrakr.communiwise.user.history.CallInstance;
import com.wisekrakr.communiwise.phone.sip.ext.SipClient;
import com.wisekrakr.communiwise.phone.sip.ext.SipSessionState;
import com.wisekrakr.communiwise.user.ContactManager;
import com.wisekrakr.communiwise.user.SipAccountManager;
import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.fields.AttributeField;
import gov.nist.javax.sdp.parser.AttributeFieldParser;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.message.SIPMessage;
import org.apache.commons.lang3.StringUtils;

import javax.sdp.MediaDescription;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
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
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages a single SIP session
 */
public class SipManager implements SipClient {
    private static final String STACK_NAME = "WisekrakrSIP";
    //    private static final String STACK_DOMAIN_NAME = "com.wisekrakr";
    private static final int MAX_MESSAGE_SIZE = 1048576;

    private final String localSipAddress;
    private final int localSipPort;
    private final String sipTransport;
    private final String proxyHost;
    private final int proxyPort;
    private SipAccountManager accountManager;
    private ContactManager contactManager;

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

    public void initialize(SipAccountManager accountManager, ContactManager contactManager) throws Exception {
        this.accountManager = accountManager;
        this.contactManager = contactManager;

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
                                    sendResponse(transaction, Response.OK);

                                    String message = null;
                                    try {
                                        message = sp.getMessageContent();
                                    } catch (Throwable e) {
                                        throw new IllegalStateException("No message could be received",e);
                                    }

                                    listener.onReceiveMessage(message, sp.getFrom().getAddress().toString());

                                    break;

                                case Request.BYE:
                                    sipSessionState = SipSessionState.IDLE;

                                    if (transaction == null) {
                                        System.out.println("Process Bye:  null TID.");
                                    } else {
                                        Response response = messageFactory.createResponse(Response.OK, requestEvent.getRequest());
                                        transaction.sendResponse(response);

                                        System.out.println("BYE received");

                                        listener.onRemoteBye(getCurrentCallInstance(callId.getCallId()));

                                        removeFromCallInstances(callId.getCallId());
                                    }

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
                                        sendResponse(transaction, Response.DECLINE);// Already in a call, tells the other party the user is busy
                                    } else {
                                        sipSessionState = SipSessionState.INCOMING;

                                        transaction.sendResponse(messageFactory.createResponse(Response.TRYING, request));

                                        waitingCall = transaction;

                                        InetSocketAddress proxyAddress = new InetSocketAddress(sp.getRemoteAddress().getHostAddress(), sp.getRemotePort());


                                        CallInstance callInstance = new CallInstance(callId.getCallId(), sp.getFrom().getAddress().getDisplayName(),proxyAddress,
                                                addressFactory.createAddress(sp.getFromHeader().getAddress().getURI()));

                                        listener.onRinging(callInstance);

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

                                    listener.onRemoteCancel(getCurrentCallInstance(callId.getCallId()));

                                    removeFromCallInstances(callId.getCallId());

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
                                } catch (Throwable e) {
                                    listener.authenticationFailed();

                                    sipSessionState = SipSessionState.IDLE;

                                    throw new IllegalStateException("Not authenticated", e);
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

                                        MediaDescription incomingMediaDescriptor = (MediaDescription) sessionDescription.getMediaDescriptions(false).get(0);

                                        if (sessionDescription.getMediaDescriptions(true).size() != 1) {
                                            System.out.println("number of media descriptions != 1, will take the first anyway");
                                        }


                                        ToHeader toHeader = (ToHeader) processedResponse.getHeader(ToHeader.NAME);

                                        String name = toHeader.getAddress().toString();

                                        name = name.substring(name.indexOf(":") + 1);
                                        name = name.substring(0, name.indexOf("@"));

                                        toHeader.getAddress().setDisplayName(name);

                                        CallInstance call = new CallInstance(callId.getCallId(), name,
                                                new InetSocketAddress(sessionDescription.getConnection().getAddress(),incomingMediaDescriptor.getMedia().getMediaPort()),
                                                toHeader.getAddress());

                                        callInstances.put(callId.getCallId(), call);

                                        listener.callConfirmed(call);

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

                                        listener.onBye(getCurrentCallInstance(callId.getCallId()));

                                        removeFromCallInstances(callId.getCallId());
                                        break;


                                    default:
                                        throw new IllegalStateException("Unknown request type in response: " + responseEvent);
                                }

                            } else if (processedResponse.getStatusCode() == Response.DECLINE /*|| processedResponse.getStatusCode() == Response.TEMPORARILY_UNAVAILABLE*/) {
                                System.out.println("CALL DECLINED");
                                listener.onRemoteDeclined();

                            } else if (processedResponse.getStatusCode() == Response.NOT_FOUND) {
                                System.out.println("NOT FOUND");
                                listener.onNotFound(responseEvent.getDialog().getRemoteParty() );
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
                            throw new IllegalStateException("Error while processing response " + responseEvent, t);
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

    private CallInstance getCurrentCallInstance(String callId){
        CallInstance callInstance = null;

        for(CallInstance c: callInstances.values()){
            if(c.getId().equals(callId)){
                callInstance = c;
            }
        }

        return callInstance;
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
//            sipSessionState = SipSessionState.READY;
            sipSessionState = SipSessionState.IDLE;

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

    private void removeFromCallInstances(String callId){
        try {
            for (Map.Entry<String, CallInstance> c : callInstances.entrySet()) {
                if (c.getValue().getId().equals(callId)) {

                    callInstances.remove(callId);
                }
            }
        }catch (Throwable t){
            throw new IllegalArgumentException("Could not remove from CallInstances",t);
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

            String sdpContent =  new String(waitingCall.getRequest().getRawContent());
            SessionDescription requestSDP = SdpFactory.getInstance().createSessionDescription(sdpContent);

            String rtpPort = StringUtils.substring(requestSDP.toString(),requestSDP.toString().lastIndexOf("m=audio") + 8, requestSDP.toString().lastIndexOf("m=audio") + 13);

            for (Map.Entry<String, CallInstance> c : callInstances.entrySet()) {
                if (c.getValue().getSipAddress().getURI().equals(fromHeader.getAddress().getURI())) {
                    listener.onAccepted(c.getValue(), Integer.parseInt(rtpPort));
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

        listener.onDeclined(callId.getCallId());
        try {
            sendResponse(waitingCall, Response.DECLINE);

        } catch (Throwable e) {
            throw new IllegalStateException("Unable to send decline", e);
        }
        sipSessionState = SipSessionState.IDLE;
    }

    private void sendResponse(ServerTransaction serverTransaction, int status) throws ParseException, SipException, InvalidArgumentException {
        System.out.println("  Client response send  " + status);
        serverTransaction.sendResponse(messageFactory.createResponse(status, serverTransaction.getRequest()));
    }


    private Address createContactAddress() {
        try {
            return this.addressFactory.createAddress("sip:"
                    + accountManager.getUserInfo().get(SipAccountManager.UserInfoPart.USERNAME.getInfoPart()) + "@"
                    + localSipAddress + ":"+ localSipPort + ";transport=udp"
                    + ";registering_acc=" + accountManager.getUserInfo().get(SipAccountManager.UserInfoPart.DOMAIN.getInfoPart()));
        } catch (ParseException e) {
            return null;
        }
    }

    public Request createRegisterRequest() throws ParseException, InvalidArgumentException {
        return createRequest(addressFactory.createAddress("sip:" + proxyHost + ":" + proxyPort).getURI(), clientAddress, clientAddress, Request.REGISTER, null);
    }

    public Request makeInviteRequest(String to, int localRtpPort) throws ParseException, InvalidArgumentException {
        Request callRequest = createRequest(addressFactory.createURI(to), clientAddress, addressFactory.createAddress(to), Request.INVITE, null);

        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");

        SipURI contactURI = addressFactory.createSipURI(accountManager.getUserInfo().get(SipAccountManager.UserInfoPart.USERNAME.getInfoPart()), localSipAddress);
        contactURI.setPort(localSipPort);

        callRequest.addHeader(headerFactory.createContactHeader(addressFactory.createAddress(contactURI)));

        callRequest.setContent(("v=0\r\n" +
                "o=- 13760799956958020 13760799956958020" + " IN IP4 " + localSipAddress + "\r\n" +
                "s=mysession session\r\n" +
                "c=IN IP4 " + localSipAddress + "\r\n" +
                "t=0 0\r\n" +
                "m=audio " + localRtpPort + " RTP/AVP 9 \r\n" +
                "a=rtpmap:9 G722/8000\r\n" +
                "a=maxptime:150\r\n" +
                "a=sendrecv\r\n"
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
        return createRequest(addressFactory.createURI(to), clientAddress, addressFactory.createAddress(to), Request.MESSAGE, content);
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
            contentTypeHeader.setParameter("charset","UTF-8");
            request.setContent(content, contentTypeHeader);
        }
        authenticationHelper.setAuthenticationHeaders(request);

        System.out.println(request);

        return request;
    }
}
