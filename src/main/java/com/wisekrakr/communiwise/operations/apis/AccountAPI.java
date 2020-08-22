package com.wisekrakr.communiwise.operations.apis;

import com.wisekrakr.communiwise.user.ContactManager;

import java.util.Map;

public interface AccountAPI {

    boolean isAuthenticated();

    void userIsOnline();

    Map<String, String> getUserInfo();
    void addContact(String username, String domain, int extension);
    void saveContactList();
    void removeContact(String username);
    void updateContact(String username, String domain, int extension);
    ContactManager getContactManager();
}
