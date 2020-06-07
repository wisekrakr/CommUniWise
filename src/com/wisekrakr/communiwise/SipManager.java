package com.wisekrakr.communiwise;




import com.wisekrakr.communiwise.actions.Message;
import com.wisekrakr.communiwise.user.SipAccountManager;
import com.wisekrakr.communiwise.utils.Headers;
import com.wisekrakr.communiwise.utils.IpAddress;
import gov.nist.javax.sdp.SessionDescriptionImpl;
import gov.nist.javax.sdp.parser.SDPAnnounceParser;
import gov.nist.javax.sip.SipStackExt;
import gov.nist.javax.sip.clientauthutils.AuthenticationHelper;
import gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import gov.nist.javax.sip.message.SIPMessage;
import com.wisekrakr.communiwise.actions.Invite;
import com.wisekrakr.communiwise.actions.Register;
import com.wisekrakr.communiwise.user.SipProfile;

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
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

public class SipManager implements SipListener, SipManagerContext{

    private Dialog dialog;

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

    public SipManager(SipProfile sipProfile) {
        this.sipProfile = sipProfile;

        initialize();
    }
    private void initialize(){
//        sipProfile.setLocalIp(IpAddress.get(true));

        sipFactory = SipFactory.getInstance();
        sipFactory.resetFactory();
        sipFactory.setPathName("gov.nist");

        Properties properties = new Properties();
        properties.setProperty("javax.sip.OUTBOUND_PROXY", sipProfile.getLocalIp() + ":" + sipProfile.getLocalPort() + "/"
                + "udp");
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

        System.out.println("Process Request: " + request.getMethod());
        System.out.println("Process Request ST: " + serverTransactionId);

        switch (request.getMethod()) {
            case "MESSAGE":
                sendOk(requestEvent);

                try {
                    String message = sp.getMessageContent();

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case Request.BYE:
                sipManagerState = SipManagerState.IDLE;
                processBye(requestEvent, serverTransactionId);

                direction = CallDirection.NONE;
                break;
            case Request.ACK:
                processAck(request, serverTransactionId);
//                sendOk(requestEvent);
                break;
            case "INVITE":
                direction = CallDirection.INCOMING;
                processInvite(requestEvent, serverTransactionId);
                break;
            case "CANCEL":
                sipManagerState = SipManagerState.IDLE;
                processCancel(requestEvent, serverTransactionId);

                direction = CallDirection.NONE;
                break;
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        System.out.println("Process Response: " + response.getStatusCode());

        Dialog resDialog;
        ClientTransaction tid = responseEvent.getClientTransaction();
        if (tid != null) {
            resDialog = tid.getDialog();
        } else {
            resDialog = responseEvent.getDialog();
        }
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

//        if (response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED
//                || response.getStatusCode() == Response.UNAUTHORIZED) {
//            AuthenticationHelper authenticationHelper = ((SipStackExt) sipStack)
//                    .getAuthenticationHelper(
//                            new SipAccountManager(sipProfile.getSipUserName(),
//                                    sipProfile.getServer(), sipProfile
//                                    .getSipPassword()), headerFactory);
//            try {
//                ClientTransaction inviteTid = authenticationHelper
//                        .handleChallenge(response, tid, sipProvider, 5);
//                currentClientTransaction = inviteTid;
//                inviteTid.sendRequest();
//            } catch (NullPointerException | SipException e) {
//                e.printStackTrace();
//            }
//
//        }

        if (response.getStatusCode() == Response.OK) {
            switch (cseq.getMethod()) {
                case Request.INVITE:
                    System.out.println("Dialog after 200 OK  " + resDialog);
                    System.out.println("CT after 200 OK  " + tid);

                    try {
                        Request ackRequest = resDialog.createAck(cseq
                                .getSeqNumber());

                        System.out.println("Sending ACK");

                        resDialog.sendAck(ackRequest);

                    } // TODO Auto-generated catch block
                    catch (SipException | InvalidArgumentException e) {
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
                    break;
            }

        } else if (response.getStatusCode() == Response.DECLINE || response.getStatusCode() == Response.TEMPORARILY_UNAVAILABLE) {
            System.out.println("CALL DECLINED");
        } else if (response.getStatusCode() == Response.NOT_FOUND) {
            System.out.println("NOT FOUND");
        } else if (response.getStatusCode() == Response.ACCEPTED) {
            System.out.println("ACCEPTED");
        } else if (response.getStatusCode() == Response.BUSY_HERE) {
            System.out.println("BUSY");
        } else if (response.getStatusCode() == Response.RINGING) {
            System.out.println("RINGING");
        } else if (response.getStatusCode() == Response.SERVICE_UNAVAILABLE) {
            System.out.println("SERVICE_UNAVAILABLE");

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
            System.out.println("Transaction terminated event received "
                    + transactionTerminatedEvent.getServerTransaction());
        else {
            System.out.println("Transaction terminated "
                    + transactionTerminatedEvent.getClientTransaction());
        }
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("processDialogTerminated: " + dialogTerminatedEvent);
    }

    @Override
    public void sendingMessage(String to, String message) {
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

        } catch (ParseException | TransactionUnavailableException | InvalidArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void calling(String to, int localRtpPort) {
        sipManagerState = SipManagerState.CALLING;
        Invite inviteRequest = new Invite(this);
        Request r = inviteRequest.MakeRequest(to, localRtpPort);
        System.out.println(r);
        try {
            final ClientTransaction transaction = this.sipProvider
                    .getNewClientTransaction(r);
            currentClientTransaction = transaction;
            Thread thread = new Thread() {
                public void run() {
                    try {
                        transaction.sendRequest();

                        System.out.println("Calling: " + transaction.getDialog());

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
        if (direction == CallDirection.OUTGOING) {
            if (currentClientTransaction != null) {
                sendByeClient(currentClientTransaction);
                //sipManagerState = SipManagerState.IDLE;
            }
        }
        else if (direction == CallDirection.INCOMING) {
            if (currentServerTransaction != null) {
                sendByeClient(currentServerTransaction);
                //
            }
        }
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

        try {
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                st = sipProvider.getNewServerTransaction(request);
            }

            currentServerTransaction = st;

            System.out.println("INVITE: sending Trying");
            Response response = messageFactory.createResponse(Response.TRYING,
                    request);
            st.sendResponse(response);
            System.out.println("INVITE:Trying Sent");

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


    public void processAck(Request request,
                           ServerTransaction serverTransactionId) {
        System.out.println("Process Ack: got an ACK! " + request);
        System.out.println("Ack Dialog State = " + dialog.getState());
//        try {
//            if (serverTransactionId == null) {
//                System.out.println("null server transaction -- ignoring the ACK!");
//                return;
//            }
//            Dialog dialog = serverTransactionId.getDialog();
//
//            System.out.println("Dialog Created = " + dialog.getDialogId() + " Dialog State = " + dialog.getState());
//
//            System.out.println("Waiting for INFO");
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }
}
