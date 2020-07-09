package com.wisekrakr.communiwise.phone.device;


public interface PhoneAPI {
    void register(String realm, String domain, String username, String password, String fromAddress);

    void initiateCall(String sipAddressa, int serverPort);

    void accept();

    void reject();

    void hangup();
}
