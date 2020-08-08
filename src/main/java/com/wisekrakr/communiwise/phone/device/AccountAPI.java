package com.wisekrakr.communiwise.phone.device;

import com.wisekrakr.communiwise.phone.managers.ContactManager;
import com.wisekrakr.communiwise.user.SipAccountManager;
import com.wisekrakr.communiwise.user.SipUserProfile;

import java.util.Map;

public interface AccountAPI {

    Map<String, String> getUserInfo();
    void saveContact(SipUserProfile sipUserProfile);
    void removeContact(SipUserProfile sipUserProfile);
    void updateContact(String username, String address, int port);
    ContactManager getContactManager();
}
