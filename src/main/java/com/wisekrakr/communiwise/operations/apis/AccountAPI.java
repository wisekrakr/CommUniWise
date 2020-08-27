package com.wisekrakr.communiwise.operations.apis;

import com.wisekrakr.communiwise.user.ContactManager;

import java.util.Map;

public interface AccountAPI {

    boolean isAuthenticated();

    void userIsOnline();

    Map<String, String> getUserInfo();
    boolean phoneBookHandler(ContactManager.UserOption userOption, String username, String domain, int extension);

    void updateContact(String username, String domain, int extension);
    ContactManager getContactManager();
}
