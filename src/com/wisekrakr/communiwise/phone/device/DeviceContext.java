package com.wisekrakr.communiwise.phone.device;



import com.wisekrakr.communiwise.phone.events.SipEventListenerContext;
import com.wisekrakr.communiwise.phone.managers.SipManager;

public interface DeviceContext  extends SipEventListenerContext {
    void register();

    void call(String to);

    void accept();

    void reject();


    void hangup();

    void sendMessage(String to, String message);


    void mute(boolean muted);

    SipManager getSipManager();

}
