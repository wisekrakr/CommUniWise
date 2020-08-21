package com.wisekrakr.communiwise.gui;

public interface FrameManagerListener {

    void onOutgoingCall(String callId);
    void onIncomingCall(String callId, String username, String rtpAddress, int rtpPort);
    void close();
    void open();
    void onRegistering();
    void onUnregistering();
    void onHangUp(String callId);
    void onAcceptingCall(String callId);
    void onAuthenticationFailed();
    void onRegistered();
    void menuContactListOpen();
    void menuPreferencesOpen();

}
