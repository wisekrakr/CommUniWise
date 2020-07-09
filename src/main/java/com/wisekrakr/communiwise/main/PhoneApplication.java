package com.wisekrakr.communiwise.main;


import com.wisekrakr.communiwise.phone.audiovisualconnection.RTPConnectionManager;
import com.wisekrakr.communiwise.phone.audiovisualconnection.impl.AudioClip;
import com.wisekrakr.communiwise.phone.device.DeviceContext;
import com.wisekrakr.communiwise.phone.device.layout.ScreenEvent;
import com.wisekrakr.communiwise.phone.managers.SipManagerListener;
import com.wisekrakr.communiwise.phone.managers.SipManager;
import com.wisekrakr.communiwise.screens.ext.AbstractScreen;
import com.wisekrakr.communiwise.screens.ext.ScreenState;
import com.wisekrakr.communiwise.screens.layouts.AudioCallScreen;
import com.wisekrakr.communiwise.screens.layouts.IncomingCallScreen;
import com.wisekrakr.communiwise.screens.layouts.LoginScreen;
import com.wisekrakr.communiwise.screens.layouts.PhoneScreen;

import javax.sound.sampled.*;
import java.io.Serializable;

public class PhoneApplication implements DeviceContext, Serializable {
    private Clip ringingClip;
    private String sipUserName;
    private String sipPassword;
    private String sipAddress;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Arguments: <local address>");
            System.exit(1);
        }

        String localAddress = args[0];

        PhoneApplication application = new PhoneApplication();
        try {
            application.initialize(
                    localAddress,
                    5080,
                    "udp",
                    "asterisk.interzone",
                    5060,
                    "damian2",
                    "45jf83f",
                    "sip:" + "damian2" + "@" + "asterisk.interzone"
            );


        } catch (Exception e) {
            System.out.println("Unable to initialize: " + e);
            e.printStackTrace();

            System.exit(1);

            return;
        }


        application.run();
    }

    private void run() {
    }


    private SipManager sipManager;
    private AbstractScreen currentScreen;

    private ScreenState screenState = ScreenState.LOGIN;

    private LoginScreen loginScreen;
    private PhoneScreen phoneScreen;
    private IncomingCallScreen incomingCallScreen;
    private AudioCallScreen audioCallScreen;

    private RTPConnectionManager RTPConnectionManager;

    private void initialize(String localAddress, int localPort, String transport, String proxyHost, int proxyPort, String sipUserName, String sipPassword, String sipAddress) throws Exception {
        this.sipUserName = sipUserName;
        this.sipPassword = sipPassword;
        this.sipAddress = sipAddress;


        sipManager = new SipManager(proxyHost, proxyPort, localAddress, localPort, transport).
                logging("server.log", "debug.log", 16).
                listener(new SipManagerListener() {

                    @Override
                    public void onTextMessage(String message, String from) {
                        System.out.println("Received message from " + from + " :" + message);
                    }

                    @Override
                    public void onBye() {

                    }

                    @Override
                    public void onRemoteCancel() {

                    }

                    @Override
                    public void onRemoteDeclined() {
                    }

                    @Override
                    public void onConnected(int rtpPort) {

                    }

                    @Override
                    public void onUnavailable() {

                    }

                    @Override
                    public void onRinging(String from) {

                    }

                    @Override
                    public void onBusy() {

                    }

                    @Override
                    public void onRemoteAccepted() {

                    }

                    @Override
                    public void onRegistered() {

                    }

                    @Override
                    public void onHangup() {

                    }

/*
                    public void onSipMessage(final SipEvent sipEventObject) {
                        System.out.println("Sip Event fired: " + sipEventObject.type);
//        phoneScreen.showStatus();
                        switch (sipEventObject.type) {
                            case MESSAGE:
                                if (this.sipDeviceListener != null) {
                                    this.sipDeviceListener.onSipUAMessageArrived(new SipEvent(this, SipEvent.SipEventType.MESSAGE, sipEventObject.content, sipEventObject.from));
                                }
                                break;
                            case BYE:
                                RTPConnectionManager.stopStreaming();

                                if (this.sipConnectionListener != null) {
                                    this.sipConnectionListener.onSipUADisconnected(null);
                                }
                                break;
                            case REMOTE_CANCEL:
                                RTPConnectionManager.stopStreaming();

                                if (this.sipConnectionListener != null) {
                                    this.sipConnectionListener.onSipUACancelled(null);
                                }
                                break;
                            case DECLINED:
                                RTPConnectionManager.stopStreaming();

                                if (this.sipConnectionListener != null) {
                                    this.sipConnectionListener.onSipUADeclined(null);
                                }
                                break;
                            case BUSY_HERE:
                            case SERVICE_UNAVAILABLE:
                                RTPConnectionManager.stopStreaming();

                                break;

                            case CALL_CONNECTED:
                                try {
                                    RTPConnectionManager.connect(new InetSocketAddress(sipEventObject.rtpPort.getServer(), sipEventObject.rtpPort));
                                } catch (LineUnavailableException | IOException e) {
                                    e.printStackTrace();
                                }

                                if (this.sipConnectionListener != null) {
                                    this.sipConnectionListener.onSipUAConnected(null);
                                }
                                break;


                            case REMOTE_RINGING:
                                if (this.sipConnectionListener != null) {
                                    this.sipConnectionListener.onSipUAConnecting(null);
                                }

                                break;
                            case LOCAL_RINGING:
                                ringingClip.loop(Clip.LOOP_CONTINUOUSLY);


                                if (this.sipDeviceListener != null) {
                                    this.sipDeviceListener.onSipUAConnectionArrived(null);
                                }
                                break;

                        }

                    }    */

                    public void onScreenEventMessage(ScreenEvent screenEvent) {
                        System.out.println("Screen Event fired: " + screenEvent.type);
                        switch (screenEvent.type) {
                            case REGISTERED:
                                // TODO
//                if (sipProfile.isAuthenticated()) {
                                loginScreen.clearScreen();

                                setScreenState(ScreenState.PHONE);
//                }
                                break;


                            case UNREGISTERED:
                                break;
                            case INCOMING:
                                setScreenState(ScreenState.INCOMING);
                                break;
                            case AUDIO_CALLING:
                                setScreenState(ScreenState.AUDIO_CALL);
                                break;
                            case VIDEO_CALLING:
                                break;
                            case MESSAGING:
                                break;
                            case EXITING:
                                currentScreen.clearScreen();
                                break;
                        }
                        screenHandler();
                    }

                });

        sipManager.addUser("asterisk", sipUserName, sipPassword, proxyHost, sipAddress);

        sipManager.initialize();

        RTPConnectionManager = new RTPConnectionManager();
        RTPConnectionManager.init();


        // Handles changing of screens
        screenHandler();

        AudioInputStream stream = AudioClip.loadClip("shake_bake.wav");
        AudioFormat format = stream.getFormat();
        DataLine.Info dataInfo = new DataLine.Info(Clip.class, format);
        ringingClip = (Clip) AudioSystem.getLine(dataInfo);
        ringingClip.open(stream);
    }

    /**
     * Handles changing of screens.
     * Creates the current screen, so that it can be destroyed when it is no longer used at a later stage.
     */
    public void screenHandler() {
        System.out.println("Setting up screen: " + screenState);

        //TODO: small frame for went we get an invite and we need to accept a call.
        switch (screenState) {
            case LOGIN:
                if (loginScreen == null) loginScreen = new LoginScreen(this);
                currentScreen = loginScreen;
                break;
            case INCOMING:
                if (incomingCallScreen == null) incomingCallScreen = new IncomingCallScreen(this);
                currentScreen = incomingCallScreen;
                break;
            case PHONE:
                if (phoneScreen == null) phoneScreen = new PhoneScreen(this);
                break;
            case AUDIO_CALL:
                if (audioCallScreen == null) audioCallScreen = new AudioCallScreen(this);
                currentScreen = audioCallScreen;
                break;
            case VIDEO_CALL:
                break;
            case MESSENGER:
                break;
            case BYE_BYE:
                break;
        }

    }


    @Override
    public void initiateCall(String sipAddress, int localRtpPort) {
        this.sipManager.initiateCall(sipAddress, localRtpPort);
    }

    @Override
    public void accept() {
//        this.sipManager.acceptingCall(Config.ANOTHER_RTP_PORT);
        ringingClip.stop();

    }

    @Override
    public void reject() {
        this.sipManager.reject();

        ringingClip.stop();
    }


    @Override
    public void hangup() {
        try {
            this.sipManager.hangup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //audio call screen clear

    }

    @Override
    public void sendMessage(String to, String message) {
        this.sipManager.sendTextMessage(to, message);
    }

    @Override
    public void register(String username, String password) {
        this.sipManager.register(username, password);
    }

    @Override
    public void mute(boolean muted) {
        //mute audio
    }

    @Override
    public SipManager getSipManager() {
        return sipManager;
    }

    public RTPConnectionManager getRTPConnectionManager() {
        return RTPConnectionManager;
    }

    @Override
    public ScreenState getScreenState() {
        return screenState;
    }

    public void setScreenState(ScreenState screenState) {
        this.screenState = screenState;
    }


}
