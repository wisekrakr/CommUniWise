package com.wisekrakr.communiwise.phone.managers.ext;

public interface SipClient {
    void sendTextMessage(String recipient, String message);

    void login(String realm, String username, String password, String domain, String fromAddress);

    void initiateCall(String recipient, int localRtpPort);

    void hangup(String recipient);
    void reject();

    void acceptCall(final int port);
}
