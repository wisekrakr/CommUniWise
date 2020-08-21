package com.wisekrakr.communiwise.phone.sip.ext;

public interface SipClient {
    void sendTextMessage(String recipient, String message);

    void login(String fromAddress, String domain, String username, String password, String address);

    void initiateCall(String recipient, int localRtpPort);

    void hangup(String recipient, String callId);
    void reject();

    void acceptCall(final int port);

    int getStatus();
}
