package com.wisekrakr.communiwise.phone.managers;

import com.wisekrakr.communiwise.utils.NotInitializedException;

public interface SipManagerContext {

    void sendingMessage(String to, String message) throws NotInitializedException;
    void registering();
    void calling(String to, int localRtpPort) throws NotInitializedException;
    void hangingUp() throws NotInitializedException;
    void rejectingCall();
    void acceptingCall(final int port);
}
