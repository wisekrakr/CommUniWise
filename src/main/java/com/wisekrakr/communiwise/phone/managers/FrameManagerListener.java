package com.wisekrakr.communiwise.phone.managers;

public interface FrameManagerListener {

    void onOutgoingCall();
    void onIncomingCall();
    void close();
    void open();
    void onRegistering();
}
