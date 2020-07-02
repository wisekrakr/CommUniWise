package com.wisekrakr.communiwise.phone;



import com.wisekrakr.communiwise.phone.audiovisualconnection.impl.AudioClip;
import com.wisekrakr.communiwise.phone.audiovisualconnection.AVConnectionStream;
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

import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import java.io.*;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Device implements DeviceContext, Serializable {

    private static Device device;
    private SipManager sipManager;
    private SipProfile sipProfile;
    private boolean isInitialized;
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

    private AudioClip audioClip;
//    private AudioWrapper audioWrapper;
//    private RtpApp rtpApp;
    private AVConnectionStream AVConnectionStream;
    private DatagramSocket datagramSocket;


    private Device(){

    }
    public static Device GetInstance(){
        if(device == null){
            device = new Device();
            device.Initialize(new SipProfile());

        }
        return device;
    }

    public void Initialize(SipProfile sipProfile){
        this.sipProfile = sipProfile;
        sipManager = new SipManager(sipProfile);

        AVConnectionStream = new AVConnectionStream();
        AVConnectionStream.init();

        sipManager.addSipListener(this);
        sipManager.addScreenListener(this);

        // Handles changing of screens
        screenHandler();

//        // Handles rtp connection
//        try {
//            rtpApp = new RtpApp(sipProfile.getServer(), Config.LOCAL_RTP_PORT);
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//
//        // Handles all audio functions
//        audioWrapper = AudioWrapper.getInstance();

        // Holds our Local Ringing sound
        audioClip = new AudioClip();
        audioClip.createClipURL("audio/shake_bake.wav");

        isInitialized = true;
    }

    /**
     * Handles changing of screens.
     * Creates the current screen, so that it can be destroyed when it is no longer used at a later stage.
     */
    public void screenHandler(){
        System.out.println("Setting up screen: " + screenState);

        //TODO: small frame for went we get an invite and we need to accept a call.
        switch (screenState){
            case LOGIN:
                if(loginScreen == null)loginScreen = new LoginScreen(this);
                currentScreen = loginScreen;
                break;
            case INCOMING:
                if(incomingCallScreen == null)incomingCallScreen = new IncomingCallScreen(this);
                currentScreen = incomingCallScreen;
                break;
            case PHONE:
                if(phoneScreen == null)phoneScreen = new PhoneScreen(this);
                break;
            case AUDIO_CALL:
                if(audioCallScreen == null)audioCallScreen = new AudioCallScreen(this);
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
                AVConnectionStream.stopStreaming();

                if (this.sipConnectionListener != null) {
                    this.sipConnectionListener.onSipUADisconnected(null);
                }
                break;
            case REMOTE_CANCEL:
                AVConnectionStream.stopStreaming();

                if (this.sipConnectionListener != null) {
                    this.sipConnectionListener.onSipUACancelled(null);
                }
                break;
            case DECLINED:
                AVConnectionStream.stopStreaming();

                if (this.sipConnectionListener != null) {
                    this.sipConnectionListener.onSipUADeclined(null);
                }
                break;
            case BUSY_HERE:
            case SERVICE_UNAVAILABLE:
                AVConnectionStream.stopStreaming();

                break;
            case CALL_CONNECTED:
                try {
                    AVConnectionStream.start( sipProfile.getServer(),sipEventObject.rtpPort, datagramSocket);
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
                audioClip.getClip().loop(Clip.LOOP_CONTINUOUSLY);


                if (this.sipDeviceListener != null) {
                    this.sipDeviceListener.onSipUAConnectionArrived(null);
                }
                break;

        }
    }

    @Override
    public void onScreenEventMessage(ScreenEvent screenEvent) {
        System.out.println("Screen Event fired: " + screenEvent.type);
        switch (screenEvent.type){
            case REGISTERED:
                if(sipProfile.isAuthenticated()){
                    loginScreen.clearScreen();

                    setScreenState(ScreenState.PHONE);
                }
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
    public void call(String to) {
        try {
            datagramSocket = new DatagramSocket();
            sipProfile.setLocalRtpPort(datagramSocket.getLocalPort());

        } catch (SocketException e) {
            e.printStackTrace();
        }
        try {
            this.sipManager.calling(to, sipProfile.getLocalRtpPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void accept() {
//        this.sipManager.acceptingCall(Config.ANOTHER_RTP_PORT);
        audioClip.getClip().stop();

    }

    @Override
    public void reject() {
        this.sipManager.rejectingCall();
        audioClip.getClip().stop();

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
    public void register() {
        this.sipManager.registering();
    }

    @Override
    public void mute(boolean muted){
        //mute audio
    }

    @Override
    public SipManager getSipManager() {
        return sipManager;
    }

    public AVConnectionStream getAVConnectionStream() {
        return AVConnectionStream;
    }

    @Override
    public ScreenState getScreenState() {
        return screenState;
    }

//    public AudioWrapper getAudioWrapper() {
//        return audioWrapper;
//    }
//
//    public RtpApp getRtpApp() {
//        return rtpApp;
//    }
//
//    public void setRtpApp(RtpApp rtpApp) {
//        this.rtpApp = rtpApp;
//    }

    public void setScreenState(ScreenState screenState) {
        this.screenState = screenState;
    }


}
