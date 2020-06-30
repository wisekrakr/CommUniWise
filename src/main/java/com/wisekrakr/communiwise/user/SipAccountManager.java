package com.wisekrakr.communiwise.user;

import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.UserCredentials;

import javax.sip.ClientTransaction;

public class SipAccountManager implements AccountManager {
    String username;
    String password;
    String server;

    public SipAccountManager(String username, String server, String password) {
        this.username = username;
        this.password = password;
        this.server = server;

    }

    public UserCredentials getCredentials(ClientTransaction challengedTransaction, String realm) {
        return new SipUserCredentials(username, server, password);
    }
}
