package com.wisekrakr.communiwise;

public interface SipManagerContext {
    void sendingMessage(String to, String message);
    void sendingDTMF(String digit);
    void registering();
    void calling(String to, int localRtpPort);
    void hangingUp();
}
