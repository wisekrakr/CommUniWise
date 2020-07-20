package com.wisekrakr.communiwise.phone.device;


public interface PhoneAPI {
    void register(String realm, String domain, String username, String password, String fromAddress);

    void initiateCall(String sipAddress);

    void accept();

    void reject();

    void hangup();

    int callStatus();
}
