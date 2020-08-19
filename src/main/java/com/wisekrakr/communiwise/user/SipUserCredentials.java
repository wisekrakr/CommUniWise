package com.wisekrakr.communiwise.user;

import gov.nist.javax.sip.clientauthutils.UserCredentials;

public class SipUserCredentials implements UserCredentials {
    private String username;
    private String sipDomain;
    private String password;

    public SipUserCredentials(String username, String sipDomain, String password) {
        this.username = username;
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

        return username;
    }
}
