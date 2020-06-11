package com.wisekrakr.communiwise.phone;



import com.wisekrakr.communiwise.config.Config;
import com.wisekrakr.communiwise.phone.device.DeviceContext;
import com.wisekrakr.communiwise.phone.device.SipDeviceListener;
import com.wisekrakr.communiwise.phone.device.events.SipConnectionListener;
import com.wisekrakr.communiwise.phone.device.events.SipEvent;
import com.wisekrakr.communiwise.phone.device.layout.ScreenEvent;
import com.wisekrakr.communiwise.phone.managers.SipManager;
import com.wisekrakr.communiwise.phone.managers.SoundManager;
import com.wisekrakr.communiwise.screens.ext.AbstractScreen;
import com.wisekrakr.communiwise.screens.layouts.AudioCallScreen;
import com.wisekrakr.communiwise.screens.layouts.IncomingCallScreen;
import com.wisekrakr.communiwise.screens.layouts.PhoneScreen;
import com.wisekrakr.communiwise.screens.ext.ScreenState;
import com.wisekrakr.communiwise.screens.layouts.LoginScreen;
import com.wisekrakr.communiwise.user.SipProfile;

import javax.swing.*;
import java.io.*;

public class Device implements DeviceContext, Serializable {

    private static Device device;
    SipManager sipManager;
    SipProfile sipProfile;
    SoundManager soundManager;
    boolean isInitialized;
    AbstractScreen currentScreen;
    public SipDeviceListener sipDeviceListener = null;
    public SipConnectionListener sipConnectionListener = null;
    private ScreenState screenState = ScreenState.LOGIN;

    private LoginScreen loginScreen;
    private PhoneScreen phoneScreen;
    private IncomingCallScreen incomingCallScreen;
    private AudioCallScreen audioCallScreen;

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
        soundManager = new SoundManager();

        sipManager.addSipListener(this);
        sipManager.addScreenListener(this);

        screenHandler();

        isInitialized = true;
    }

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
        phoneScreen.showStatus();
        switch (sipEventObject.type) {
            case MESSAGE:
                if (this.sipDeviceListener != null) {
                    this.sipDeviceListener.onSipUAMessageArrived(new SipEvent(this, SipEvent.SipEventType.MESSAGE, sipEventObject.content, sipEventObject.from));
                }
                break;
            case BYE:
                soundManager.stopAudioStream();
                if (this.sipConnectionListener != null) {
                    // notify our listener that we are connected
                    this.sipConnectionListener.onSipUADisconnected(null);
                }
                break;
            case REMOTE_CANCEL:
                soundManager.stopAudioStream();

                if (this.sipConnectionListener != null) {
                    // notify our listener that we are connected
                    this.sipConnectionListener.onSipUACancelled(null);
                }
                break;
            case DECLINED:
                soundManager.stopAudioStream();

                if (this.sipConnectionListener != null) {
                    // notify our listener that we are connected
                    this.sipConnectionListener.onSipUADeclined(null);
                }
                break;
            case BUSY_HERE:
            case SERVICE_UNAVAILABLE:
                soundManager.stopAudioStream();
                break;
            case CALL_CONNECTED:
                soundManager.startAudioStream(sipEventObject.remoteRtpPort, sipProfile.getServer()); //TODO: changed from REMOTE IP

                if (this.sipConnectionListener != null) {
                    // notify our listener that we are connected
                    this.sipConnectionListener.onSipUAConnected(null);
                }
                break;
            case REMOTE_RINGING:
                if (this.sipConnectionListener != null) {
                    // notify our listener that we are connecting
                    this.sipConnectionListener.onSipUAConnecting(null);
                }
                break;
            case LOCAL_RINGING:
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
                    loginScreen.dispose();

                    setScreenState(ScreenState.PHONE);

                    screenHandler();
                }
                break;
            case UNREGISTERED:
                break;
            case INCOMING:
                setScreenState(ScreenState.INCOMING);
                screenHandler();
                break;
            case AUDIO_CALLING:
                setScreenState(ScreenState.AUDIO_CALL);
                screenHandler();
                break;
            case VIDEO_CALLING:
                break;
            case MESSAGING:
                break;
            case EXITING:
                currentScreen.clearScreen();
                break;
        }
    }

    @Override
    public void call(String to) {
        try {
            this.sipManager.calling(to, Config.LOCAL_RTP_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void accept() {
        this.sipManager.acceptingCall(Config.LOCAL_RTP_PORT);
    }

    @Override
    public void reject() {
        this.sipManager.rejectingCall();
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
    public SipManager getSipManager() {
        return sipManager;
    }

    @Override
    public void mute(boolean muted){
        //mute audio
    }

    @Override
    public ScreenState getScreenState() {
        return screenState;
    }

    public void setScreenState(ScreenState screenState) {
        this.screenState = screenState;
    }


}
