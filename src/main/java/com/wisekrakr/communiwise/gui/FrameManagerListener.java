package com.wisekrakr.communiwise.gui;

import com.wisekrakr.communiwise.phone.calling.CallInstance;

import javax.sip.address.Address;
import java.awt.*;

public interface FrameManagerListener {

    void onOutgoingCall(CallInstance callInstance);
    void onIncomingCall(CallInstance callInstance);
    void close();
    void open();
    void onRegistering();
    void onUnregistering();
    void onHangUp(CallInstance callInstance);
    void onAcceptingCall(CallInstance callInstance);
    void onDecliningCall(String callId);
    void onAuthenticationFailed();
    void onRegistered();
    void menuContactListOpen();
    void menuPreferencesOpen();
    void menuAboutOpen();
    void menuAccountOpen();
    void onNotFound(Address proxyAddress);
    void onAlert(Component component, String text, int messageCode);
}
