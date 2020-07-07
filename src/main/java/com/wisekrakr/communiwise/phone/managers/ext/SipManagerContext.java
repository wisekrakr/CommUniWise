package com.wisekrakr.communiwise.phone.managers.ext;

import com.wisekrakr.communiwise.utils.NotInitializedException;

public interface SipManagerContext {

    void sendingMessage(String to, String message);
    void register(String username, String password);

    void callRequest(String to, int localRtpPort);
    void hangingUp();
    void rejectingCall();
    void acceptingCall(final int port);
}
