package com.wisekrakr.communiwise.phone;




import com.wisekrakr.communiwise.phone.events.SipEvent;
import com.wisekrakr.communiwise.phone.events.SipEventListenerContext;
import com.wisekrakr.communiwise.phone.impl.Message;
import com.wisekrakr.communiwise.phone.audio.impl.ClientAudio;
import com.wisekrakr.communiwise.phone.audio.impl.ServerAudio;
import com.wisekrakr.communiwise.phone.managers.SipManagerContext;
import com.wisekrakr.communiwise.phone.managers.SipManagerState;
import com.wisekrakr.communiwise.user.SipAccountManager;
import com.wisekrakr.communiwise.utils.Headers;
import com.wisekrakr.communiwise.utils.NotInitializedException;
import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.message.SIPMessage;
import com.wisekrakr.communiwise.phone.impl.Invite;
import com.wisekrakr.communiwise.phone.impl.Register;
import com.wisekrakr.communiwise.user.SipProfile;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.sdp.MediaDescription;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

public class SipManager implements SipListener, SipManagerContext {

    private Dialog dialog;
    private ArrayList<SipEventListenerContext> sipEventListenerList = new ArrayList<SipEventListenerContext>();
    private int rtpPort;

    public enum CallDirection{ NONE, INCOMING, OUTGOING};

    private SipFactory sipFactory;
    private ListeningPoint udpListeningPoint;
    private SipStack sipStack;
    private SipProvider sipProvider;
    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private MessageFactory messageFactory;
    private boolean initialized;
    private final SipProfile sipProfile;
    private SipManagerState sipManagerState;
    private ClientTransaction currentClientTransaction = null;
    private ServerTransaction currentServerTransaction;

    private CallDirection direction = CallDirection.NONE;

    private ClientAudio clientAudio;
    private ServerAudio serverAudio;

    public SipManager(SipProfile sipProfile) {
        this.sipProfile = sipProfile;

        clientAudio = new ClientAudio();
        serverAudio = new ServerAudio();

        initialize();
    }
    private void initialize(){
//        sipProfile.setLocalIp(IpAddress.get(true));
        sipManagerState = SipManagerState.REGISTERING;

        sipFactory = SipFactory.getInstance();
        sipFactory.resetFactory();
        sipFactory.setPathName("gov.nist");

        Properties properties = new Properties();

//        properties.setProperty("javax.sip.OUTBOUND_PROXY", sipProfile.getRemoteEndpoint() + "/"
//                + sipProfile.getTransport());
        properties.setProperty("javax.sip.STACK_NAME", "CommUniWise");
        properties
                .setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "1048576");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "CommUniWise_DEBUG.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "CommUniWise_LOG.txt");
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
            sipManagerState = SipManagerState.READY;
        } catch (Exception e) {
            e.printStackTrace();
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

    public CallDirection getDirection() {
        return direction;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public SipManagerState getSipManagerState() {
        return sipManagerState;
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent.getServerTransaction();
        SIPMessage sp = (SIPMessage) request;

        System.out.println("\n\nRequest " + request.getMethod()
                + " received at " + sipStack.getStackName()
                + " with server transaction id " + serverTransactionId);

        SipURI uri = (SipURI) ((FromHeader)request.getHeader(FromHeader.NAME)).getAddress().getURI();
        System.out.println("Request Header: " + uri);

        switch (request.getMethod()) {
            case "MESSAGE":
                sendOk(requestEvent);

                try {
                    String message = sp.getMessageContent();
                    dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.MESSAGE,
                            message, sp.getFrom().getAddress().toString()));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case Request.BYE:
                sipManagerState = SipManagerState.IDLE;
                processBye(requestEvent, serverTransactionId);

                dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.BYE, "", sp
                        .getFrom().getAddress().toString()));

                direction = CallDirection.NONE;
                break;
            case Request.ACK:
                processAck(request, serverTransactionId);
                break;
            case Request.REGISTER:
                sendOk(requestEvent);
                break;
            case "INVITE":
                direction = CallDirection.INCOMING;
                processInvite(requestEvent, serverTransactionId);
                break;
            case "CANCEL":
                sipManagerState = SipManagerState.IDLE;
                processCancel(requestEvent, serverTransactionId);

                dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.REMOTE_CANCEL, "", sp
                        .getFrom().getAddress().toString()));

                direction = CallDirection.NONE;
                break;
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
        ClientTransaction tid = responseEvent.getClientTransaction();

        System.out.println("Response received : Status Code = "
                + response.getStatusCode() + " " + cseq);
        if (tid == null) {
            System.out.println("Stray response -- dropping ");
            return;
        }

        System.out.println("transaction state is " + tid.getState());
        System.out.println("Dialog = " + tid.getDialog());
//            System.out.println("Dialog State is " + tid.getDialog().getState());

        currentClientTransaction = tid;
        //TODO with registering this must be call upon

        if (response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED
                || response.getStatusCode() == Response.UNAUTHORIZED) {
            System.out.println("Go for Authentication");

            AuthenticationHelper authenticationHelper = ((SipStackExt) sipStack)
                    .getAuthenticationHelper(
                            new SipAccountManager(sipProfile.getSipUserName(),
                                    sipProfile.getServer(), sipProfile
                                    .getSipPassword()), headerFactory);
            try {
                currentClientTransaction = authenticationHelper
                        .handleChallenge(response, tid, sipProvider, 5);

                currentClientTransaction.sendRequest();
            } catch (NullPointerException | SipException e) {
                e.printStackTrace();
            }

        }

        if (response.getStatusCode() == Response.OK) {
            switch (cseq.getMethod()) {
                case Request.INVITE:
                    System.out.println("Dialog after 200 OK  " );

                    try {
                        Dialog dialog = currentClientTransaction.getDialog();

                        Request ackRequest = dialog.createAck(cseq
                                .getSeqNumber());

                        System.out.println("Sending ACK " + ackRequest);

                        dialog.sendAck(ackRequest);

                        byte[] rawContent = response.getRawContent();
                        String sdpContent = new String(rawContent, StandardCharsets.UTF_8);
                        SDPAnnounceParser parser = new SDPAnnounceParser(sdpContent);
                        SessionDescriptionImpl sessionDescription = parser.parse();
                        MediaDescription incomingMediaDescriptor = (MediaDescription) sessionDescription
                                .getMediaDescriptions(false).get(0);
                        rtpPort = incomingMediaDescriptor.getMedia()
                                .getMediaPort();

                        System.out.println("Process Response INVITE rtpPort: " + rtpPort);

                        dispatchSipEvent(new SipEvent(this,
                                SipEvent.SipEventType.CALL_CONNECTED, "", "", rtpPort));

                    } // TODO Auto-generated catch block
                    catch (SipException | InvalidArgumentException | SdpException | ParseException e) {
                        e.printStackTrace();
                    }

                    break;
                case Request.CANCEL:
                    if (dialog.getState() == DialogState.CONFIRMED) {

                        System.out.println("Sending BYE -- cancel went in too late !!");
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
                        } catch (SipException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }

                    break;
                case Request.BYE:
                    sipManagerState = SipManagerState.IDLE;
                    System.out.println("--- Got 200 OK in UAC outgoing BYE");

                    dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.BYE, "", ""));
                    break;
            }

        } else if (response.getStatusCode() == Response.DECLINE || response.getStatusCode() == Response.TEMPORARILY_UNAVAILABLE) {
            System.out.println("CALL DECLINED");
            dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.DECLINED, "", ""));
        } else if (response.getStatusCode() == Response.NOT_FOUND) {
            System.out.println("NOT FOUND");
        } else if (response.getStatusCode() == Response.ACCEPTED) {
            System.out.println("ACCEPTED");
        } else if (response.getStatusCode() == Response.BUSY_HERE) {
            System.out.println("BUSY");
            dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.BUSY_HERE, "", ""));
        } else if (response.getStatusCode() == Response.RINGING) {
            System.out.println("RINGING");
            dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.REMOTE_RINGING, "", ""));
        } else if (response.getStatusCode() == Response.SERVICE_UNAVAILABLE) {
            System.out.println("SERVICE_UNAVAILABLE");
            dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.SERVICE_UNAVAILABLE, "", ""));
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

    @Override
    public void sendingMessage(String to, String message) throws NotInitializedException {
        if (!isInitialized())
            throw new NotInitializedException("Sip Stack not initialized");

        Message inviteRequest = new Message();
        try {
            Request r = inviteRequest.MakeRequest(this, to, message);

            final ClientTransaction transaction = this.sipProvider
                    .getNewClientTransaction(r);
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

        } catch (TransactionUnavailableException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void registering() {
        if(!isInitialized())
            return;

        Register registerRequest = new Register(this);
        try {
            Request r = registerRequest.MakeRequest();
            currentClientTransaction = this.sipProvider
                    .getNewClientTransaction(r);
            Thread thread = new Thread() {
                public void run() {
                    try {
                        currentClientTransaction.sendRequest();

                        System.out.println("Registering state: " + currentClientTransaction.getState());


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
    public void calling(String to, int localRtpPort) throws NotInitializedException{
        if (!isInitialized())
            throw new NotInitializedException("Sip Stack not initialized");

        sipManagerState = SipManagerState.CALLING;

        Invite inviteRequest = new Invite(this);
        Request r = inviteRequest.MakeRequest(to, localRtpPort);
        System.out.println(r);


        try {
            currentClientTransaction = this.sipProvider
                    .getNewClientTransaction(r);
            Thread thread = new Thread() {
                public void run() {
                    try {
                        currentClientTransaction.sendRequest();

                        System.out.println("Calling: " + currentClientTransaction.getDialog());

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
    public void hangingUp() throws NotInitializedException{
        if (!isInitialized())
            throw new NotInitializedException("Sip Stack not initialized");

        if (direction == CallDirection.OUTGOING) {
            if (currentClientTransaction != null) {
                sendByeClient(currentClientTransaction);
//                sipManagerState = SipManagerState.IDLE;
            }
        }
        else if (direction == CallDirection.INCOMING) {
            if (currentServerTransaction != null) {
                sendByeClient(currentServerTransaction);
                //
            }
        }
    }

    @Override
    public void acceptingCall(final int port) {
        if (currentServerTransaction == null){
            System.out.println("Accepting Call: no Server Transaction");
            return;
        }

        Thread thread = new Thread() {
            public void run() {
                try {
                    SIPMessage sm = (SIPMessage) currentServerTransaction
                            .getRequest();
                    Response responseOK = messageFactory.createResponse(
                            Response.OK, currentServerTransaction.getRequest());
                    Address address = Headers.createContactAddress(addressFactory, sipProfile);
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

                    dispatchSipEvent(new SipEvent(this,
                            SipEvent.SipEventType.CALL_CONNECTED, "", sm.getFrom()
                            .getAddress().toString(), rtpPort));

                    System.out.println("Call connected on port: " + rtpPort);

                    sipManagerState = SipManagerState.ESTABLISHED;
                } catch (ParseException | InvalidArgumentException | SipException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();


        sipManagerState = SipManagerState.ESTABLISHED;
    }

    @Override
    public void rejectingCall() {
        sendDecline(currentServerTransaction.getRequest());
        sipManagerState = SipManagerState.IDLE;
    }

    private void sendDecline(Request request) {
        Thread thread = new Thread() {
            public void run() {
                try {
                    Response responseBye = messageFactory.createResponse(
                            Response.DECLINE,
                            request);
                    currentServerTransaction.sendResponse(responseBye);

                } catch (ParseException | InvalidArgumentException | SipException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        direction = CallDirection.NONE;
    }

    private void processInvite(RequestEvent requestEvent, ServerTransaction serverTransaction) {
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

            currentServerTransaction = st;

            System.out.println("INVITE: sending Trying");
            Response response = messageFactory.createResponse(Response.TRYING,
                    request);

            dialog = st.getDialog();

            st.sendResponse(response);
            System.out.println("INVITE:Trying Sent");

            byte[] rawContent = response.getRawContent();
            String sdpContent = new String(rawContent, StandardCharsets.UTF_8);
            SDPAnnounceParser parser = new SDPAnnounceParser(sdpContent);
            SessionDescriptionImpl sessionDescription = parser.parse();
            MediaDescription incomingMediaDescriptor = (MediaDescription) sessionDescription
                    .getMediaDescriptions(false).get(0);
            rtpPort = incomingMediaDescriptor.getMedia()
                    .getMediaPort();

            System.out.println("Remote RTP port from incoming SDP:" + rtpPort);

            dispatchSipEvent(new SipEvent(this, SipEvent.SipEventType.LOCAL_RINGING, "",
                    sm.getFrom().getAddress().toString()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendOk(RequestEvent requestEvt) {
        try {
            Response response = messageFactory.createResponse(200,
                    requestEvt.getRequest());
            ServerTransaction serverTransaction = requestEvt
                    .getServerTransaction();
            if (serverTransaction == null) {
                serverTransaction = sipProvider
                        .getNewServerTransaction(requestEvt.getRequest());
            }
            serverTransaction.sendResponse(response);
            System.out.println("Send OK: " + serverTransaction);

        } catch (ParseException | SipException | InvalidArgumentException e) {
            e.printStackTrace();
        }

    }

    private void sendByeClient(Transaction transaction) {
        final Dialog dialog = transaction.getDialog();
        if (dialog == null) {
            System.out.println("Send Bye Client: Dialog is null");
        }
        else {
            Request byeRequest = null;
            try {
                byeRequest = dialog.createRequest(Request.BYE);
            } catch (SipException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            ClientTransaction newTransaction = null;
            try {
                newTransaction = sipProvider.getNewClientTransaction(byeRequest);
            } catch (TransactionUnavailableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            final ClientTransaction ct = newTransaction;

            Thread thread = new Thread() {
                public void run() {
                    try {
                        dialog.sendRequest(ct);

                        System.out.println("Send Bye Client: Adios!");


                    } catch (SipException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };
            thread.start();

        }

        direction = CallDirection.NONE;
    }

    private void processBye(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
        try {
            System.out.println("BYE received");
            if (serverTransactionId == null) {
                System.out.println("Process Bye:  null TID.");
                return;
            }
            Dialog dialog = serverTransactionId.getDialog();
            System.out.println("Dialog State = " + dialog.getState());
            Response response = messageFactory.createResponse(200, requestEvent.getRequest());
            serverTransactionId.sendResponse(response);
            System.out.println("Sending OK");
            System.out.println("Dialog State = " + dialog.getState());

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    private void processCancel(RequestEvent requestEvent, ServerTransaction serverTransactionId) {
        try {
            System.out.println("CANCEL received");
            if (serverTransactionId == null) {
                System.out.println("Process Cancel:  null TID.");
                return;
            }
            Dialog dialog = serverTransactionId.getDialog();
            System.out.println("Dialog State = " + dialog.getState());
            Response response = messageFactory.createResponse(200, requestEvent.getRequest());
            serverTransactionId.sendResponse(response);
            System.out.println("Sending 200 Canceled Request");
            System.out.println("Dialog State = " + dialog.getState());

            if (currentServerTransaction != null) {
                // also send a 487 Request Terminated response to the original INVITE request
                Request originalInviteRequest = currentServerTransaction.getRequest();
                Response originalInviteResponse = messageFactory.createResponse(Response.REQUEST_TERMINATED, originalInviteRequest);
                currentServerTransaction.sendResponse(originalInviteResponse);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }


    private void processAck(Request request,
                           ServerTransaction serverTransactionId) {
        System.out.println("Process Ack: got an ACK! " + request);
        System.out.println("Ack Dialog State = " + currentClientTransaction.getDialog().getState());
        try {
            if (serverTransactionId == null) {
                System.out.println("null server transaction -- ignoring the ACK!");
                return;
            }
            Dialog dialog = serverTransactionId.getDialog();

            System.out.println("Dialog Created = " + dialog.getDialogId() + " Dialog State = " + dialog.getState());

            System.out.println("Waiting for INFO");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatchSipEvent(SipEvent sipEvent) {
        System.out.println("Dispatching event:" + sipEvent.type);
        ArrayList<SipEventListenerContext> tmpSipListenerList;

        synchronized (this) {
            if (sipEventListenerList.size() == 0)
                return;
            tmpSipListenerList = (ArrayList<SipEventListenerContext>) sipEventListenerList
                    .clone();
        }

        for (SipEventListenerContext listener : tmpSipListenerList) {
            listener.onSipMessage(sipEvent);
        }
    }

    public synchronized void addSipListener(SipEventListenerContext listener) {
        if (!sipEventListenerList.contains(listener)) {
            sipEventListenerList.add(listener);
        }
    }
}
