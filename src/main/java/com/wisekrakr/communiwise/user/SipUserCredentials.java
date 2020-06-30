package com.wisekrakr.communiwise.user;

import gov.nist.javax.sip.clientauthutils.UserCredentials;

public class SipUserCredentials implements UserCredentials {
    private String userName;
    private String sipDomain;
    private String password;

    public SipUserCredentials(String userName, String sipDomain, String password) {
        this.userName = userName;
        this.sipDomain = sipDomain;
        this.password = password;
    }


    public String getPassword() {
        return password;
    }


    public String getSipDomain() {
        return sipDomain;
    }


    public String getUserName() {

        return userName;
    }
}
