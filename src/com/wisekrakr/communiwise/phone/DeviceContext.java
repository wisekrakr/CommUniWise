package com.wisekrakr.communiwise.phone;



import com.wisekrakr.communiwise.phone.events.SipEventListenerContext;

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
