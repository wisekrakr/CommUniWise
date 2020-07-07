package com.wisekrakr.communiwise.phone.device;



import com.wisekrakr.communiwise.phone.device.events.SipEventListenerContext;
import com.wisekrakr.communiwise.phone.device.layout.LayoutListenerContext;
import com.wisekrakr.communiwise.phone.managers.SipManager;
import com.wisekrakr.communiwise.screens.ext.ScreenState;

public interface DeviceContext extends SipEventListenerContext, LayoutListenerContext {
    void register(String username, String password);

    void initiateCall(String sipAddressa, int serverPort);

    void accept();

    void reject();

    void hangup();

    void sendMessage(String to, String message);

    void mute(boolean muted);

    SipManager getSipManager();

    ScreenState getScreenState();

}
