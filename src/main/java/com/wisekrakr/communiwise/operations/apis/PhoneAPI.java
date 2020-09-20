package com.wisekrakr.communiwise.operations.apis;


public interface PhoneAPI {
    void register(String realm, String domain, String username, String password, String fromAddress);

    void initiateCall(String extension, String domain);

    void accept(String sipAddress);

    void reject();

    void hangup(String callId);

    int callStatus();

    void sendMessage(String recipient, String message);

    void sendVoiceMessage(String extension, String domain);
}
