package com.wisekrakr.communiwise.user;

import gov.nist.javax.sip.clientauthutils.AccountManager;
import gov.nist.javax.sip.clientauthutils.UserCredentials;

import javax.sip.ClientTransaction;
import java.util.HashMap;
import java.util.Map;

public class SipAccountManager implements AccountManager {
    private final Map<String, UserCredentials> credentials = new HashMap<>();
    private final Map<String, String> userInfo = new HashMap<>();

    public void clear() {
        credentials.clear();
    }

    public void addCredentials(String realm, String userName, String password, String domain) {
        credentials.put(realm, new SipUserCredentials(userName, domain, password));

        userInfo.put("userName", userName);
        userInfo.put("domain", domain);
    }

    public UserCredentials getCredentials(ClientTransaction challengedTransaction, String realm) {
        return credentials.get(realm);
    }

    public Map<String, String> getUserInfo() {
        return userInfo;
    }
}
