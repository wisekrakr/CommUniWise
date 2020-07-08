package com.wisekrakr.communiwise.phone.managers.ext;

public interface SipClient {
    void sendTextMessage(String recipient, String message);

    void register(String username, String password);

    void initiateCall(String recipient, int localRtpPort);

    void hangup();
    void reject();

    void acceptCall(final int port);
}
