package com.wisekrakr.communiwise.gui;

import javax.sip.address.Address;

public interface FrameManagerListener {

    void onOutgoingCall(String callId);
    void onIncomingCall(String callId, String displayName, String rtpAddress, int rtpPort);
    void close();
    void open();
    void onRegistering();
    void onUnregistering();
    void onHangUp(String callId);
    void onAcceptingCall(String callId);
    void onDecliningCall(String callId);
    void onAuthenticationFailed();
    void onRegistered();
    void menuContactListOpen();
    void menuPreferencesOpen();
    void onNotFound(Address proxyAddress);
    void onError(String text);
}
