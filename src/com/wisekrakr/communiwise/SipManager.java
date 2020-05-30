package com.wisekrakr.communiwise;



import gov.nist.javax.sip.message.SIPMessage;
import com.wisekrakr.communiwise.actions.Invite;
import com.wisekrakr.communiwise.actions.Register;
import com.wisekrakr.communiwise.user.SipProfile;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

public class SipManager implements SipListener, SipManagerContext{

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
    }

    private boolean initialize(){

        sipFactory = SipFactory.getInstance();
        sipFactory.resetFactory();
        sipFactory.setPathName("android.gov.nist");

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
            return false;
        }
        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            udpListeningPoint = sipStack.createListeningPoint("127.0.0.1",
                    6060, "udp");
            sipProvider = sipStack.createSipProvider(udpListeningPoint);
            sipProvider.addSipListener(this);
            initialized = true;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public ArrayList<ViaHeader> createViaHeader() {
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader myViaHeader;
        try {
            myViaHeader = this.headerFactory.createViaHeader(
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
            return this.addressFactory.createAddress("sip:"
                    + getSipProfile().getSipUserName() + "@"
                    + getSipProfile().getLocalEndpoint() + ";transport=udp"
                    + ";registering_acc=" + getSipProfile().getRemoteIp());
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

    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {

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
        Register registerRequest = new Register();
        try {
            Request r = registerRequest.MakeRequest(this);
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
        this.sipManagerState = SipManagerState.CALLING;
        Invite inviteRequest = new Invite();
        Request r = inviteRequest.MakeRequest(this, to, localRtpPort);
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
                } catch (ParseException | InvalidArgumentException e) {
                    e.printStackTrace();
                } catch (SipException e) {
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

                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (SipException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvalidArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        direction = CallDirection.NONE;
    }
}
