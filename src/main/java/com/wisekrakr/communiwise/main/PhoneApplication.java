package com.wisekrakr.communiwise.main;


import com.wisekrakr.communiwise.config.Config;
import com.wisekrakr.communiwise.phone.audiovisualconnection.impl.AudioClip;
import com.wisekrakr.communiwise.phone.audiovisualconnection.RTPConnectionManager;
import com.wisekrakr.communiwise.phone.device.DeviceContext;
import com.wisekrakr.communiwise.phone.device.SipDeviceListener;
import com.wisekrakr.communiwise.phone.device.events.SipConnectionListener;
import com.wisekrakr.communiwise.phone.device.events.SipEvent;
import com.wisekrakr.communiwise.phone.device.layout.ScreenEvent;
import com.wisekrakr.communiwise.phone.managers.SipManager;
import com.wisekrakr.communiwise.screens.ext.AbstractScreen;
import com.wisekrakr.communiwise.screens.layouts.*;
import com.wisekrakr.communiwise.screens.ext.ScreenState;
import com.wisekrakr.communiwise.user.SipProfile;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.util.Enumeration;

public class PhoneApplication implements DeviceContext, Serializable {
    private Clip ringingClip;

    public static void main(String[] args) {
        PhoneApplication application = new PhoneApplication();


        try {
            application.initialize(
                    new SipProfile(
                            getHostIpAddress(),
                            Config.LOCAL_PORT,
                            "udp",
                            -1,
                            Config.SERVER,
                            Config.MASTER_PORT,
                            Config.USERNAME,
                            Config.PASSWORD,
                            "sip:" + Config.USERNAME + "@" + Config.SERVER
                    ));
        } catch (Exception e) {
            System.out.println("Unable to initialize: " + e);

            return;
        }


        application.run();
    }

    private void run() {
    }

    //todo remove this cheat
    private static String getHostIpAddress() {
        String address = "";

        Enumeration<NetworkInterface> e = null;
        try {
            e = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException socketException) {
            socketException.printStackTrace();
        }
        while(e.hasMoreElements())
        {
            NetworkInterface n =  e.nextElement();
            Enumeration<InetAddress> ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = ee.nextElement();

                if(i.getHostAddress().equals("192.168.84.87")){
                    address = i.getHostAddress();

                }


            }
        }

        return address;
    }

    private SipManager sipManager;
    private SipProfile sipProfile;
    private AbstractScreen currentScreen;

    private final SipDeviceListener sipDeviceListener = null;
    private final SipConnectionListener sipConnectionListener = new SipConnectionListener() {
        @Override
        public void onSipUAConnecting(SipEvent event) {

        }

        @Override
        public void onSipUAConnected(SipEvent event) {

        }

        @Override
        public void onSipUADisconnected(SipEvent event) {

        }

        @Override
        public void onSipUACancelled(SipEvent event) {

        }

        @Override
        public void onSipUADeclined(SipEvent event) {

        }
    };

    private ScreenState screenState = ScreenState.LOGIN;

    private LoginScreen loginScreen;
    private PhoneScreen phoneScreen;
    private IncomingCallScreen incomingCallScreen;
    private AudioCallScreen audioCallScreen;

    private RTPConnectionManager RTPConnectionManager;

    public void initialize(SipProfile sipProfile) throws Exception {
        this.sipProfile = sipProfile;

        sipManager = new SipManager(sipProfile);
        sipManager.initialize();

        RTPConnectionManager = new RTPConnectionManager();
        RTPConnectionManager.init();

        sipManager.addSipListener(this);
        sipManager.addScreenListener(this);

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
                    RTPConnectionManager.start(new InetSocketAddress(sipProfile.getServer(), sipEventObject.rtpPort));
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
    }

    @Override
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

    @Override
    public void initiateCall(String sipAddress, int localRtpPort) {
        this.sipManager.callRequest(sipAddress, localRtpPort);
    }

    @Override
    public void accept() {
//        this.sipManager.acceptingCall(Config.ANOTHER_RTP_PORT);
        ringingClip.stop();

    }

    @Override
    public void reject() {
        this.sipManager.rejectingCall();

        ringingClip.stop();
    }


    @Override
    public void hangup() {
        try {
            this.sipManager.hangingUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //audio call screen clear

    }

    @Override
    public void sendMessage(String to, String message) {
        try {
            this.sipManager.sendingMessage(to, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
