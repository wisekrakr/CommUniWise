package com.wisekrakr.communiwise.operations.apis;


public interface PhoneAPI {
    void register(String realm, String domain, String username, String password, String fromAddress);

    void initiateCall(String sipAddress);

    void accept();

    void reject();

    void hangup();

    int callStatus();

}
