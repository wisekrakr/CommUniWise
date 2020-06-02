package com.wisekrakr.communiwise;



import com.wisekrakr.copypasta.implementation.SipAccountManager;
import com.wisekrakr.copypasta.implementation.SipEvent;
import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import gov.nist.javax.sip.message.SIPMessage;
import com.wisekrakr.communiwise.actions.Invite;
import com.wisekrakr.communiwise.actions.Register;
import com.wisekrakr.communiwise.user.SipProfile;
import jdk.nashorn.internal.ir.SetSplitState;

import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class SipManager implements SipListener, SipManagerContext{

    private Dialog dialog;
    private DigestServerAuthenticationHelper dsam;
    private Response okResponse;
    protected ServerTransaction inviteTid;
    private Request inviteRequest;
    private int remoteRtpPort;

    public enum CallDirection{ NONE, INCOMING, OUTGOING};

    private SipFactory sipFactory;
    private ListeningPoint udpListeningPoint;
    private SipStack sipStack;
    private SipProvider sipProvider;
    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private MessageFactory messageFactory;
    private boolean initialized;
    private SipProfile sipProfile;
    private SipManagerState sipManagerState;
    private ClientTransaction currentClientTransaction = null;
    private ServerTransaction currentServerTransaction;

    private CallDirection direction = CallDirection.NONE;

    public SipManager(SipProfile sipProfile) {
        this.sipProfile = sipProfile;

        initialize();
    }
    private void initialize(){

        sipFactory = SipFactory.getInstance();
        sipFactory.resetFactory();
        sipFactory.setPathName("gov.nist");

        Properties properties = new Properties();
        properties.setProperty("javax.sip.OUTBOUND_PROXY", "127.0.0.1:5070" + "/"
                + "udp");
        properties.setProperty("javax.sip.STACK_NAME", "Test Call");
        properties
                .setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "1048576");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "testcall.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "testcall.txt");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "16");
        // Drop the client connection after we are done with the transaction.
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
                "false");

        try {
            if (udpListeningPoint != null) {
                // Binding again
                sipStack.deleteListeningPoint(udpListeningPoint);
                sipProvider.removeSipListener(this);
            }
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("createSipStack " + sipStack);
        } catch (PeerUnavailableException | ObjectInUseException e) {
            e.printStackTrace();
            return;
        }
        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            udpListeningPoint = sipStack.createListeningPoint(sipProfile.getLocalIp(), sipProfile.getLocalPort(),
                    sipProfile.getTransport());
            sipProvider = sipStack.createSipProvider(udpListeningPoint);
            sipProvider.addSipListener(this);
            initialized = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ViaHeader> createViaHeader() {
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader myViaHeader;
        try {
            myViaHeader = headerFactory.createViaHeader(
                    sipProfile.getLocalIp(), sipProfile.getLocalPort(),
                    sipProfile.getTransport(), null);
            myViaHeader.setRPort();
            viaHeaders.add(myViaHeader);
        } catch (ParseException | InvalidArgumentException e) {
            e.printStackTrace();
        }
        return viaHeaders;
    }

    public Address createContactAddress() {
        try {
            return addressFactory.createAddress("sip:"
                    + getSipProfile().getSipUserName() + "@"
                    + getSipProfile().getLocalEndpoint() + ";transport=udp"
                    + ";registering_acc=" + getSipProfile().getServer());
        } catch (ParseException e) {
            return null;
        }
    }

    public SipProfile getSipProfile() {
        return sipProfile;
    }

    public SipFactory getSipFactory() {
        return sipFactory;
    }

    public ListeningPoint getUdpListeningPoint() {
        return udpListeningPoint;
    }

    public SipStack getSipStack() {
        return sipStack;
    }

    public SipProvider getSipProvider() {
        return sipProvider;
    }

    public HeaderFactory getHeaderFactory() {
        return headerFactory;
    }

    public AddressFactory getAddressFactory() {
        return addressFactory;
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent.getServerTransaction();
        SIPMessage sp = (SIPMessage) request;
        System.out.println(request.getMethod());
        if (request.getMethod().equals("MESSAGE")) {
            sendOk(requestEvent);

            try {
                String message = sp.getMessageContent();
//                dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.MESSAGE,
//                        message, sp.getFrom().getAddress().toString()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (request.getMethod().equals(Request.BYE)) {
            sipManagerState = SipManagerState.IDLE;
            processBye(requestEvent, serverTransactionId);
//            dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.BYE, "", sp
//                    .getFrom().getAddress().toString()));
            direction = CallDirection.NONE;
        }
        if (request.getMethod().equals("INVITE")) {
            direction = CallDirection.INCOMING;
            processInvite(requestEvent, serverTransactionId);
        }
        if (request.getMethod().equals("CANCEL")) {
            sipManagerState = SipManagerState.IDLE;
            processCancel(requestEvent, serverTransactionId);
//            dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.REMOTE_CANCEL, "", sp
//                    .getFrom().getAddress().toString()));
            direction = SipManager.CallDirection.NONE;
        }
    }



    @Override
    public void processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        System.out.println(response.getStatusCode());

        Dialog responseDialog = null;
        ClientTransaction tid = responseEvent.getClientTransaction();
        if (tid != null) {
            responseDialog = tid.getDialog();
        } else {
            responseDialog = responseEvent.getDialog();
        }
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
        if (response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED
                || response.getStatusCode() == Response.UNAUTHORIZED) {
            AuthenticationHelper authenticationHelper = ((SipStackExt) sipStack)
                    .getAuthenticationHelper(
                            new SipAccountManager(sipProfile.getSipUserName(),
                                    sipProfile.getServer(), sipProfile
                                    .getSipPassword()), headerFactory);
            try {
                ClientTransaction inviteTid = authenticationHelper
                        .handleChallenge(response, tid, sipProvider, 5);
                currentClientTransaction = inviteTid;
                inviteTid.sendRequest();
            } catch (NullPointerException | SipException e) {
                e.printStackTrace();
            }

        } else if (response.getStatusCode() == Response.OK) {
            if (cseq.getMethod().equals(Request.INVITE)) {
                System.out.println("Dialog after 200 OK  " + dialog);
                try {
                    Request ackRequest = responseDialog.createAck(cseq
                            .getSeqNumber());
                    System.out.println("Sending ACK");
                    responseDialog.sendAck(ackRequest);
                    byte[] rawContent = response.getRawContent();
                    String sdpContent = new String(rawContent, "UTF-8");
                    SDPAnnounceParser parser = new SDPAnnounceParser(sdpContent);
                    SessionDescriptionImpl sessionDescription = parser.parse();
                    MediaDescription incomingMediaDescriptor = (MediaDescription) sessionDescription
                            .getMediaDescriptions(false).get(0);
                    int rtpPort = incomingMediaDescriptor.getMedia()
                            .getMediaPort();
//                    dispatchSipEvent(new SipEvent(this,
//                            SipEvent.SipEventType.CALL_CONNECTED, "", "", rtpPort));
                } // TODO Auto-generated catch block
                catch (SdpException | UnsupportedEncodingException | ParseException | SipException | InvalidArgumentException e) {
                    e.printStackTrace();
                }

            } else if (cseq.getMethod().equals(Request.CANCEL)) {
                if (dialog.getState() == DialogState.CONFIRMED) {
                    // oops cancel went in too late. Need to hang up the
                    // dialog.
                    System.out
                            .println("Sending BYE -- cancel went in too late !!");
                    Request byeRequest = null;
                    try {
                        byeRequest = dialog.createRequest(Request.BYE);
                    } catch (SipException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    ClientTransaction ct = null;
                    try {
                        ct = sipProvider.getNewClientTransaction(byeRequest);
                    } catch (TransactionUnavailableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    try {
                        dialog.sendRequest(ct);
                    } catch (TransactionDoesNotExistException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SipException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            } else if (cseq.getMethod().equals(Request.BYE)) {
                sipManagerState = SipManagerState.IDLE;
                System.out.println("--- Got 200 OK in UAC outgoing BYE");
//                dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.BYE, "", ""));
            }

        } else if (response.getStatusCode() == Response.DECLINE || response.getStatusCode() == Response.TEMPORARILY_UNAVAILABLE) {
            System.out.println("CALL DECLINED");
//            dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.DECLINED, "", ""));
        } else if (response.getStatusCode() == Response.NOT_FOUND) {
            System.out.println("NOT FOUND");
        } else if (response.getStatusCode() == Response.ACCEPTED) {
            System.out.println("ACCEPTED");
        } else if (response.getStatusCode() == Response.BUSY_HERE) {
            System.out.println("BUSY");
//            dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.BUSY_HERE, "", ""));
        } else if (response.getStatusCode() == Response.RINGING) {
            System.out.println("RINGING");
//            dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.REMOTE_RINGING, "", ""));
        } else if (response.getStatusCode() == Response.SERVICE_UNAVAILABLE) {
            System.out.println("BUSY");
//            dispatchSipEvent(new SipEvent(this,
//                    SipEvent.SipEventType.SERVICE_UNAVAILABLE, "", ""));
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {

    }

    @Override
    public void processIOException(IOExceptionEvent ioExceptionEvent) {

    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

    }

    @Override
    public void sendingMessage(String to, String message) {

    }

    @Override
    public void sendingDTMF(String digit) {

    }

    @Override
    public void registering() {
        sipManagerState = SipManagerState.REGISTERING;

        Register registerRequest = new Register(this);
        try {
            Request r = registerRequest.MakeRequest();
            final ClientTransaction transaction = this.sipProvider
                    .getNewClientTransaction(r);
            // Send the request statefully, through the client transaction.
            Thread thread = new Thread() {
                public void run() {
                    try {
                        transaction.sendRequest();
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();

        } catch (ParseException | TransactionUnavailableException | InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void calling(String to, int localRtpPort) {
        sipManagerState = SipManagerState.CALLING;
        Invite inviteRequest = new Invite();
        Request r = inviteRequest.MakeRequest(this, to, localRtpPort);
        System.out.println(r);
        try {
            final ClientTransaction transaction = this.sipProvider
                    .getNewClientTransaction(r);
            currentClientTransaction = transaction;
            Thread thread = new Thread() {
                public void run() {
                    try {
                        transaction.sendRequest();
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        } catch (TransactionUnavailableException e) {
            e.printStackTrace();
        }
        direction = CallDirection.OUTGOING;
    }

    @Override
    public void hangingUp() {

    }

    public void acceptingCall(final int port) {
        if (currentServerTransaction == null)
            return;
        Thread thread = new Thread() {
            public void run() {
                try {
                    SIPMessage sm = (SIPMessage) currentServerTransaction
                            .getRequest();
                    Response responseOK = messageFactory.createResponse(
                            Response.OK, currentServerTransaction.getRequest());
                    Address address = createContactAddress();
                    ContactHeader contactHeader = headerFactory
                            .createContactHeader(address);
                    responseOK.addHeader(contactHeader);
                    ToHeader toHeader = (ToHeader) responseOK
                            .getHeader(ToHeader.NAME);
                    toHeader.setTag("4321"); // Application is supposed to set.
                    responseOK.addHeader(contactHeader);


                    String sdpData = "v=0\r\n"
                            + "o=4855 13760799956958020 13760799956958020"
                            + " IN IP4 " + sipProfile.getLocalIp() + "\r\n"
                            + "s=mysession session\r\n"
                            + "p=+46 8 52018010\r\n" + "c=IN IP4 "
                            + sipProfile.getLocalIp() + "\r\n" + "t=0 0\r\n"
                            + "m=audio " + String.valueOf(port)
                            + " RTP/AVP 0 4 18\r\n"
                            + "a=rtpmap:0 PCMU/8000\r\n"
                            + "a=rtpmap:4 G723/8000\r\n"
                            + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
                    byte[] contents = sdpData.getBytes();

                    ContentTypeHeader contentTypeHeader = headerFactory
                            .createContentTypeHeader("application", "sdp");
                    responseOK.setContent(contents, contentTypeHeader);

                    currentServerTransaction.sendResponse(responseOK);

                    sipManagerState = SipManagerState.ESTABLISHED;
                } catch (ParseException | InvalidArgumentException | SipException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        sipManagerState = SipManagerState.ESTABLISHED;
    }

    public void rejectingCall() {
        sendDecline(currentServerTransaction.getRequest());
        sipManagerState = SipManagerState.IDLE;
    }



    private void sendDecline(Request request) {
        Thread thread = new Thread() {
            public void run() {

                Response responseBye;
                try {
                    responseBye = messageFactory.createResponse(
                            Response.DECLINE,
                            currentServerTransaction.getRequest());
                    currentServerTransaction.sendResponse(responseBye);

                } catch (ParseException | InvalidArgumentException | SipException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        direction = CallDirection.NONE;
    }



    /**
     * Process the invite request.
     */
    public void processInvite(RequestEvent requestEvent, ServerTransaction serverTransaction) {
        if (sipManagerState != SipManagerState.IDLE
                && sipManagerState != SipManagerState.READY
                && sipManagerState != SipManagerState.INCOMING
        ) {
             sendDecline(requestEvent.getRequest());// Already in a call
            return;
        }
        sipManagerState = SipManagerState.INCOMING;
        Request request = requestEvent.getRequest();
        SIPMessage sm = (SIPMessage) request;

        try {
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);

            }
            if (st == null)
                return;
            currentServerTransaction = st;

            //System.out.println("INVITE: with Authorization, sending Trying");
            System.out.println("INVITE: sending Trying");
            Response response = messageFactory.createResponse(Response.TRYING,
                    request);
            st.sendResponse(response);
            System.out.println("INVITE:Trying Sent");

            // Verify AUTHORIZATION !!!!!!!!!!!!!!!!

			dsam = new DigestServerAuthenticationHelper();
			if (!dsam.doAuthenticatePlainTextPassword(request,
					sipProfile.getSipPassword())) {
				Response challengeResponse = messageFactory.createResponse(
						Response.PROXY_AUTHENTICATION_REQUIRED, request);
				dsam.generateChallenge(headerFactory, challengeResponse,
						"nist.gov");
				st.sendResponse(challengeResponse);
				System.out.println("INVITE:Authorization challenge sent");
				return;
			}
			System.out
					.println("INVITE:Incoming Authorization challenge Accepted");

            byte[] rawContent = sm.getRawContent();
            String sdpContent = new String(rawContent, "UTF-8");
            SDPAnnounceParser parser = new SDPAnnounceParser(sdpContent);
            SessionDescriptionImpl sessiondescription = parser.parse();
            MediaDescription incomingMediaDescriptor = (MediaDescription) sessiondescription
                    .getMediaDescriptions(false).get(0);
            remoteRtpPort = incomingMediaDescriptor.getMedia().getMediaPort();
            System.out.println("Remote RTP port from incoming SDP:"
                    + remoteRtpPort);
//            dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.LOCAL_RINGING, "",
//                    sm.getFrom().getAddress().toString()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendOk(RequestEvent requestEvt) {
        Response response;
        try {
            response = messageFactory.createResponse(200,
                    requestEvt.getRequest());
            ServerTransaction serverTransaction = requestEvt
                    .getServerTransaction();
            if (serverTransaction == null) {
                serverTransaction = sipProvider
                        .getNewServerTransaction(requestEvt.getRequest());
            }
            serverTransaction.sendResponse(response);

        } catch (ParseException | SipException | InvalidArgumentException e) {
            e.printStackTrace();
        }

    }

    private void sendInviteOK() {
        try {
            if (inviteTid.getState() != TransactionState.COMPLETED) {
                System.out.println("CommUniWise: Dialog state before 200: "
                        + inviteTid.getDialog().getState());
                inviteTid.sendResponse(okResponse);
                System.out.println("CommUniWise: Dialog state after 200: "
                        + inviteTid.getDialog().getState());
            }
        } catch (SipException | InvalidArgumentException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Process the bye request.
     */
    public void processBye(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            System.out.println("CommUniWise:  got a bye sending OK.");
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            System.out.println("Dialog State is " + serverTransactionId.getDialog().getState());

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processCancel(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request request = requestEvent.getRequest();
        try {
            System.out.println("CommUniWise:  got a cancel.");
            if (serverTransactionId == null) {
                System.out.println("CommUniWise:  null tid.");
                return;
            }
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);
            if (dialog.getState() != DialogState.CONFIRMED) {
                response = messageFactory.createResponse(Response.REQUEST_TERMINATED,
                        inviteRequest);
                inviteTid.sendResponse(response);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }
}
