package com.wisekrakr.communiwise.phone;



import com.wisekrakr.communiwise.config.Config;
import com.wisekrakr.communiwise.phone.events.SipConnectionListener;
import com.wisekrakr.communiwise.phone.events.SipEvent;
import com.wisekrakr.communiwise.phone.listeners.SipDeviceListener;
import com.wisekrakr.communiwise.user.SipProfile;

import java.io.*;

public class Device implements DeviceContext, Serializable {

    private static Device device;
    SipManager sipManager;
    SipProfile sipProfile;
    SoundManager soundManager;
    boolean isInitialized;
    public SipDeviceListener sipDeviceListener = null;
    public SipConnectionListener sipConnectionListener = null;

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

        isInitialized = true;
    }

    @Override
    public void onSipMessage(final SipEvent sipEventObject) {
        System.out.println("Sip Event fired: " + sipEventObject.type);
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
    public void call(String to) {
        try {
            this.sipManager.calling(to, Config.LOCAL_RTP_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void accept() {
        sipManager.acceptingCall(Config.LOCAL_RTP_PORT);
    }

    @Override
    public void reject() {
        sipManager.rejectingCall();
    }


    @Override
    public void hangup() {
        try {
            this.sipManager.hangingUp();
        } catch (Exception e) {
            e.printStackTrace();
        }

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




}
