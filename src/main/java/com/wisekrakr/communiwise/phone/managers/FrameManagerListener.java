package com.wisekrakr.communiwise.phone.managers;

public interface FrameManagerListener {

    void onOutgoingCall(String callId);
    void onIncomingCall(String callId);
    void close();
    void open();
    void onRegistering();
    void onUnregistering();
    void onHangUp(String callId);
    void onAcceptingCall(String callId);

    void onRegistered();

}
