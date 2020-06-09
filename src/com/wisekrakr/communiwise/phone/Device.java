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
        System.out.println("Sip Event fired");
        if (sipEventObject.type == SipEvent.SipEventType.MESSAGE) {
            if (this.sipDeviceListener != null) {
                this.sipDeviceListener.onSipUAMessageArrived(new SipEvent(this, SipEvent.SipEventType.MESSAGE, sipEventObject.content, sipEventObject.from));
            }
        } else if (sipEventObject.type == SipEvent.SipEventType.BYE) {
            soundManager.stopAudioStream();
            if (this.sipConnectionListener != null) {
                // notify our listener that we are connected
                this.sipConnectionListener.onSipUADisconnected(null);
            }
        } else if (sipEventObject.type == SipEvent.SipEventType.REMOTE_CANCEL) {
            soundManager.stopAudioStream();

            if (this.sipConnectionListener != null) {
                // notify our listener that we are connected
                this.sipConnectionListener.onSipUACancelled(null);
            }
        } else if (sipEventObject.type == SipEvent.SipEventType.DECLINED) {
            soundManager.stopAudioStream();

            if (this.sipConnectionListener != null) {
                // notify our listener that we are connected
                this.sipConnectionListener.onSipUADeclined(null);
            }
        }else if (sipEventObject.type == SipEvent.SipEventType.BUSY_HERE) {
            soundManager.stopAudioStream();

        } else if (sipEventObject.type == SipEvent.SipEventType.SERVICE_UNAVAILABLE) {
            soundManager.stopAudioStream();

        } else if (sipEventObject.type == SipEvent.SipEventType.CALL_CONNECTED) {
            soundManager.startAudioStream(sipEventObject.remoteRtpPort, this.sipProfile.getServer());

            if (this.sipConnectionListener != null) {
                // notify our listener that we are connected
                this.sipConnectionListener.onSipUAConnected(null);
            }
        } else if (sipEventObject.type == SipEvent.SipEventType.REMOTE_RINGING) {
            if (this.sipConnectionListener != null) {
                // notify our listener that we are connecting
                this.sipConnectionListener.onSipUAConnecting(null);
            }
        } else if (sipEventObject.type == SipEvent.SipEventType.LOCAL_RINGING) {
            if (this.sipDeviceListener != null) {
                this.sipDeviceListener.onSipUAConnectionArrived(null);
            }
        }
    }

    @Override
    public void call(String to) {
        try {
            this.sipManager.calling(to, Config.RTP_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void accept() {
//        sipManager.AcceptCall(soundManager.setupAudioStream());
    }

    @Override
    public void reject() {
        sipManager.rejectingCall();
    }


    @Override
    public void hangup() {
        if (this.sipManager.getDirection() == SipManager.CallDirection.OUTGOING ||
                this.sipManager.getDirection() == SipManager.CallDirection.INCOMING) {
            try {
                this.sipManager.hangingUp();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
