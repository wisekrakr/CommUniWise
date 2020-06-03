package com.wisekrakr.communiwise.user;

import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.UserCredentials;

import javax.sip.ClientTransaction;

public class SipAccountManager implements AccountManager {
    String Username;
    String Password;
    String RemoteIp;
    public SipAccountManager(String username,String RemoteIp, String password) {
        this.Username = username;
        this.Password = password;
        this.RemoteIp = RemoteIp;

    }

    public UserCredentials getCredentials(ClientTransaction challengedTransaction, String realm) {
        return new SipUserCredentials(Username,RemoteIp,Password);
    }
}
