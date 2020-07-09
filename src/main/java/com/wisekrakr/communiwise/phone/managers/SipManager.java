package com.wisekrakr.communiwise.phone.managers;


import com.wisekrakr.communiwise.phone.device.layout.LayoutListenerContext;
import com.wisekrakr.communiwise.phone.device.layout.ScreenEvent;
import com.wisekrakr.communiwise.phone.managers.ext.SipClient;
import com.wisekrakr.communiwise.phone.managers.ext.SipSessionState;
import com.wisekrakr.communiwise.user.SipAccountManager;
import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.clientauthutils.UserCredentials;
import gov.nist.javax.sip.message.SIPMessage;

import javax.sdp.MediaDescription;
import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.UnsupportedEncodingException;
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

    private final String localRtpHost;
    private final String localSipAddress;
    private final int localSipPort;
    private final String sipTransport;
    private final String proxyHost;
    private final int proxyPort;
    private boolean authenticated = false;

    private Dialog dialog;

    private final SipAccountManager accountManager;

    private int traceLevel = 0;
    private String serverLogFile;
    private String debugLogFile;
    private SipManagerListener listener;
    private Address fromAddress;
    private String fromTag;

    public static ViaHeader createViaHeader(HeaderFactory headerFactory, String host, int port, String transport) throws ParseException, InvalidArgumentException {
        ViaHeader result = headerFactory.createViaHeader(
                host,
                port,
                transport,
                null);

        result.setRPort();

        return result;
    }

    private SipStack sipStack;

    private SipProvider udpSipProvider;

    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private MessageFactory messageFactory;

    private SipSessionState sipSessionState;

    public SipManager(String proxyHost, int proxyPort, String localSipAddress, int localSipPort, String sipTransport) {
        //sipProfile.getRemoteEndpoint() + "/" + sipProfile.getTransport()

        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.localSipAddress = localSipAddress;
        this.localSipPort = localSipPort;
        this.sipTransport = sipTransport;
        this.localRtpHost = localSipAddress;

        this.fromTag = "This is a nice tag";

        accountManager = new SipAccountManager();

        //sipProfile.getLocalIp(), sipProfile.getLocalPort(), sipProfile.getTransport()
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

    public void addUser(String realm, String sipUserName, String sipPassword, String sipDomain, String sipAddress) {
        accountManager.addCredentials(realm, sipUserName, sipPassword, sipDomain);
    }

    public void initialize() throws Exception {
        sipSessionState = SipSessionState.REGISTERING;


        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.resetFactory();
//        sipFactory.setPathName(STACK_DOMAIN_NAME);

        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();

        fromAddress = addressFactory.createAddress(addressFactory.createSipURI("damian2", proxyHost));


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

        AuthenticationHelper authenticationHelper = ((SipStackExt) sipStack)
                .getAuthenticationHelper(accountManager, headerFactory);


        ListeningPoint udp = sipStack.createListeningPoint(localSipAddress, localSipPort, sipTransport);
        udpSipProvider = sipStack.createSipProvider(udp);


        udpSipProvider.addSipListener(
                new SipListener() {
                    @Override
                    public void processRequest(RequestEvent requestEvent) {
                        try {
                            Request request = requestEvent.getRequest();
                            ServerTransaction transaction = requestEvent.getServerTransaction();
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
                                    processBye(requestEvent, transaction);

                                    listener.onBye();
                                    break;

                                case Request.ACK:
                                    processAck(request, transaction);
                                    break;

                                case Request.REGISTER:
                                    sendOk(requestEvent);
                                    break;

                                case Request.INVITE:
                                    if (sipSessionState != SipSessionState.IDLE
                                            && sipSessionState != SipSessionState.READY
                                            && sipSessionState != SipSessionState.INCOMING
                                    ) {
                                        sendDecline(requestEvent.getServerTransaction());// Already in a call
                                    } else {
                                        sipSessionState = SipSessionState.INCOMING;

                                        dialog = transaction.getDialog();
                                        transaction.sendResponse(messageFactory.createResponse(Response.TRYING, request));
/*
                                    SDPAnnounceParser parser = new SDPAnnounceParser(new String(sp.getRawContent(), StandardCharsets.UTF_8));
                                    SessionDescriptionImpl sessionDescription = parser.parse();

                                    MediaDescription incomingMediaDescriptor = (MediaDescription) sessionDescription
                                                .getMediaDescriptions(false).get(0);


 */

                                        waitingCall = transaction;

                                        listener.onRinging(sp.getFrom().getAddress().toString());
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

                                        transaction.sendResponse(messageFactory.createResponse(200, requestEvent.getRequest()));

                                        System.out.println("Sending 200 Canceled Request");
                                        System.out.println("Dialog State = " + dialog1.getState());
/*
                                    if (currentServerTransaction != null) {
                                        // also send a 487 Request Terminated response to the original INVITE request
                                        Request originalInviteRequest = currentServerTransaction.getRequest();
                                        Response originalInviteResponse = messageFactory.createResponse(Response.REQUEST_TERMINATED, originalInviteRequest);
                                        currentServerTransaction.sendResponse(originalInviteResponse);
                                    }

 */
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

                            Response processedResponse = responseEvent.getResponse();
                            CSeqHeader cseq = (CSeqHeader) processedResponse.getHeader(CSeqHeader.NAME);
                            System.out.println("Response received : Status Code = " + processedResponse.getStatusCode() + " " + cseq);
                            ClientTransaction clientTransaction = responseEvent.getClientTransaction();
                            Dialog dialog = clientTransaction.getDialog();

                            System.out.println("transaction state is " + clientTransaction.getState());
                            System.out.println("Dialog = " + clientTransaction.getDialog());


                            if (processedResponse.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED
                                    || processedResponse.getStatusCode() == Response.UNAUTHORIZED) {
                                System.out.println("Go for Authentication");

                                authenticationHelper.handleChallenge(processedResponse, clientTransaction, udpSipProvider, 5).sendRequest();
                            } else if (processedResponse.getStatusCode() == Response.OK) {
                                switch (cseq.getMethod()) {
                                    case Request.REGISTER:
                                        System.out.println("REGISTERED");

                                        listener.onRegistered();
                                        authenticated = true;
                                        break;

                                    case Request.INVITE:
                                        System.out.println("Dialog after 200 OK  ");

                                        dialog.sendAck(dialog.createAck(cseq.getSeqNumber()));

                                        byte[] rawContent = processedResponse.getRawContent();
                                        String sdpContent = new String(rawContent, StandardCharsets.UTF_8);
                                        SDPAnnounceParser parser = new SDPAnnounceParser(sdpContent);
                                        SessionDescriptionImpl sessionDescription = parser.parse();

                                        // TODO: why always pick the first?
                                        MediaDescription incomingMediaDescriptor = (MediaDescription) sessionDescription.getMediaDescriptions(false).get(0);
                                        int rtpPort = incomingMediaDescriptor.getMedia().getMediaPort();

                                        System.out.println("Process Response INVITE rtpPort: " + rtpPort);

                                        // TODO: what is the host?
                                        listener.onConnected(rtpPort);

                                        break;

                                    case Request.CANCEL:
                                        if (dialog.getState() == DialogState.CONFIRMED) {
                                            System.out.println("Sending BYE -- cancel went in too late !!");
                                            Request byeRequest = dialog.createRequest(Request.BYE);

                                            ClientTransaction ct = udpSipProvider.getNewClientTransaction(byeRequest);
                                            dialog.sendRequest(ct);
                                        }

//                                        dispatchScreenEvent(new ScreenEvent(this, ScreenEvent.ScreenEventType.EXITING));

                                        break;

                                    case Request.BYE:
                                        sipSessionState = SipSessionState.IDLE;
                                        System.out.println("--- Got 200 OK in UAC outgoing BYE");


                                        listener.onBye();
                                        break;
                                }

                            } else if (processedResponse.getStatusCode() == Response.DECLINE || processedResponse.getStatusCode() == Response.TEMPORARILY_UNAVAILABLE) {
                                System.out.println("CALL DECLINED");
                                listener.onRemoteDeclined();
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
                                listener.onRinging("yo mama");
                            } else if (processedResponse.getStatusCode() == Response.SERVICE_UNAVAILABLE) {
                                System.out.println("SERVICE_UNAVAILABLE");
                                listener.onUnavailable();
                            } else {
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

        sipSessionState = SipSessionState.READY;
    }

    private void processBye(RequestEvent requestEvent, ServerTransaction serverTransactionId) {

        System.out.println("BYE received");
        if (serverTransactionId == null) {
            System.out.println("Process Bye:  null TID.");
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                Dialog dialog = serverTransactionId.getDialog();
                System.out.println("Dialog State = " + dialog.getState());
                Response response = null;
                try {
                    response = messageFactory.createResponse(200, requestEvent.getRequest());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                try {
                    serverTransactionId.sendResponse(response);
                } catch (SipException | InvalidArgumentException e) {
                    e.printStackTrace();
                }
                System.out.println("Sending OK");
                System.out.println("Dialog State = " + dialog.getState());

            }
        };
        thread.start();

    }

    private void processAck(Request request,
                            ServerTransaction serverTransactionId) {
        System.out.println("Process Ack: got an ACK! " + request);
//        System.out.println("Ack Dialog State = " + currentClientTransaction.getDialog().getState());

        if (serverTransactionId == null) {
            System.out.println("null server transaction -- ignoring the ACK!");
            return;
        }
        Dialog dialog = serverTransactionId.getDialog();

        System.out.println("Dialog Created = " + dialog.getDialogId() + " Dialog State = " + dialog.getState());

        System.out.println("Waiting for INFO");

    }

    /************                               ACTIONS                             ************/

    @Override
    public void sendTextMessage(String recipient, String message) {
        try {
            this.udpSipProvider.getNewClientTransaction(makeMessageRequest(recipient, message)).sendRequest();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to send message", e);
        }
    }

    @Override
    public void register(String username, String password) {
        sipSessionState = SipSessionState.REGISTERING;
        try {
            this.udpSipProvider.getNewClientTransaction(createRegisterRequest()).sendRequest();
        } catch (Exception e) {
            sipSessionState = SipSessionState.IDLE;
            // TODO: user feedback
            // TODO: proper state handling
        }
    }

    @Override
    public void initiateCall(String recipient, int localRtpPort) {
        sipSessionState = SipSessionState.CALLING;
        try {
            this.udpSipProvider.getNewClientTransaction(makeInviteRequest(recipient, localRtpPort)).sendRequest();
        } catch (SipException e) {
            sipSessionState = SipSessionState.READY;

            System.out.println("Call failed " + e);
        }
    }

    @Override
    public void hangup() {
        // TODO: if sessionState ...
        // TODO: sendByeClient(currentClientTransaction);
        sipSessionState = SipSessionState.IDLE; //todo: trying to reset the system to make or get new calls after a call.

        listener.onHangup();
    }

    private ServerTransaction waitingCall;

    @Override
    public void acceptCall(final int port) {
        if (waitingCall == null) {
            throw new IllegalStateException("No call waiting to be accepted");
        }

        try {

            SIPMessage sm = (SIPMessage) waitingCall.getRequest();
            Response responseOK = messageFactory.createResponse(Response.OK, waitingCall.getRequest());
            ContactHeader contactHeader = headerFactory.createContactHeader(fromAddress);
            responseOK.addHeader(contactHeader);

            ToHeader toHeader = (ToHeader) responseOK.getHeader(ToHeader.NAME);
//        toHeader.setTag("4321"); // Application is supposed to set.
            responseOK.addHeader(toHeader);

            // TODO: this line should be generated (e.g. it announces codecs now
            // TODO: cf https://tools.ietf.org/html/rfc3555  https://andrewjprokop.wordpress.com/2013/09/30/understanding-session-description-protocol-sdp/
            //
            String sdpData = "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020 IN IP4 " + localSipAddress + "\r\n"
                    + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n"
                    + "c=IN IP4 " + localSipAddress + "\r\n"
                    + "t=0 0\r\n"
                    + "m=audio " + String.valueOf(port) + " RTP/AVP 0 4 18\r\n"
                    + "a=rtpmap:0 PCMU/8000\r\n"
                    + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n"
                    + "a=ptime:20\r\n";

            //                    SdpOffer sdpOffer = new SdpOffer();
//                    byte[] contents = sdpOffer.createSdp(sipProfile.getLocalIp(), port);

            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
            responseOK.setContent(sdpData.getBytes(), contentTypeHeader);

            waitingCall.sendResponse(responseOK);

//                    dispatchScreenEvent(new ScreenEvent(this, ScreenEvent.ScreenEventType.EXITING));

            listener.onRemoteAccepted();
//        System.out.println("Call connected on port: " + rtpPort);

            sipSessionState = SipSessionState.ESTABLISHED;
        }
        catch (Exception e) {
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
        requestEvt.getServerTransaction().sendResponse(messageFactory.createResponse(200, requestEvt.getRequest()));
    }
/*
    private void sendByeClient(Transaction transaction) {
        final Dialog dialog = transaction.getDialog();
        if (dialog == null) {
            System.out.println("Send Bye Client: Dialog is null");
        } else {
            Request byeRequest = null;
            try {
                byeRequest = dialog.createRequest(Request.BYE);
            } catch (SipException e) {
                e.printStackTrace();
            }

            ClientTransaction newTransaction = null;
            try {
                newTransaction = udpSipProvider.getNewClientTransaction(byeRequest);
            } catch (TransactionUnavailableException e) {
                e.printStackTrace();
            }
            final ClientTransaction ct = newTransaction;

            Thread thread = new Thread() {
                public void run() {
                    try {
                        dialog.sendRequest(ct);

                        System.out.println("Send Bye Client: Adios!");
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }

        direction = CallDirection.NONE;
    }*/


    public Request createRegisterRequest() throws ParseException, InvalidArgumentException {
        /*
        // Create addresses and via header for the request
        Address fromAddress = addressFactory.createAddress("sip:" + getSipProfile().getSipUserName() + "@" + getSipProfile().getServer());
        fromAddress.setDisplayName(getSipProfile().getSipUserName());

        Address toAddress = addressFactory.createAddress("sip:" + getSipProfile().getSipUserName() + "@" + getSipProfile().getServer());
        toAddress.setDisplayName(getSipProfile().getSipUserName());

        Address contactAddress = createContactAddress(addressFactory, getSipProfile());


*/
        return createRequest(addressFactory.createAddress("sip:" + proxyHost + ":" + proxyPort).getURI(),
                fromAddress, fromAddress, Request.REGISTER, null);

    }

    public Request makeInviteRequest(String to, int port) {
        try {
            Request callRequest = createRequest(addressFactory.createURI(to), fromAddress, addressFactory.createAddress(to), Request.INVITE, null);

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI("asdasdasd", localSipAddress);
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
                    "o=- 13760799956958020 13760799956958020" + " IN IP4 " + localRtpHost + "\r\n" +
                    "s=mysession session\r\n" +
                    "c=IN IP4 " + localRtpHost + "\r\n" +
                    "t=0 0\r\n" +
                    "m=audio " + port + " RTP/AVP 0\r\n" +
                    "m=audio " + port + " RTP/AVP 0 4 18 101\r\n" +
                    "a=rtpmap:0 PCMU/8000\r\n" +
                    "a=rtpmap:4 G723/8000\r\n" +
                    "a=rtpmap:18 G729A/8000\r\n" +
                    "a=rtpmap:101 telephone-event/8000\r\n" +
                    "a=maxptime:150\r\n" +
                    "a=sendrecv\r\n" +
                    "a=ptime:20\r\n").getBytes(), contentTypeHeader);

            callRequest.addHeader(headerFactory.createHeader("sipphone.Call-Info", "<http://www.antd.nist.gov>"));

            return callRequest;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();

        }
        return null;
    }

    private final AtomicLong sequenceNumberGenerator = new AtomicLong();

    private CSeqHeader nextRequestSequenceNumber(String method) throws ParseException, InvalidArgumentException {
        return headerFactory.createCSeqHeader(sequenceNumberGenerator.incrementAndGet(), method);
    }

    public Request makeMessageRequest(String to, String content) throws ParseException, InvalidArgumentException {
        URI toAddress = addressFactory.createURI(to);
        Address toNameAddress = addressFactory.createAddress(toAddress);

        return createRequest(toAddress, fromAddress, toNameAddress, Request.MESSAGE, content);
    }

    public Request createRequest(URI requestURI, Address from, Address to, String method, Object content) throws ParseException, InvalidArgumentException {
        UserCredentials credentials = accountManager.getCredentials(null, "asterisk");

        FromHeader fromHeader = headerFactory.createFromHeader(from, "metag");
        ToHeader toHeader = headerFactory.createToHeader(to, null);

        MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

        Request request = messageFactory.createRequest(
                requestURI,
                method,
                udpSipProvider.getNewCallId(),
                nextRequestSequenceNumber(method),
                fromHeader,
                toHeader,
                Arrays.asList(createViaHeader(headerFactory, localSipAddress, udpSipProvider.getListeningPoint(sipTransport).getPort(), sipTransport)),
                maxForwards);

        SupportedHeader supportedHeader = headerFactory.createSupportedHeader("replaces, outbound");
        request.addHeader(supportedHeader);

        SipURI routeUri = addressFactory.createSipURI(null, proxyHost);
        routeUri.setTransportParam(sipTransport);
        routeUri.setLrParam();
        routeUri.setPort(proxyPort);

        Address routeAddress = addressFactory.createAddress("sip:"
                + "damian2" + "@"
                +  localSipAddress + ":" + localSipPort + ";transport=" + sipTransport
                + ";registering_acc=" + proxyHost);
        RouteHeader route = headerFactory.createRouteHeader(routeAddress);
        request.addHeader(route);

        if (content != null) {
            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
            request.setContent(content, contentTypeHeader);
        }

        System.out.println(request);

        return request;
    }


}
