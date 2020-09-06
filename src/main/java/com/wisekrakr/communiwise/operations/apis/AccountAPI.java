package com.wisekrakr.communiwise.operations.apis;

import com.wisekrakr.communiwise.user.ContactManager;
import com.wisekrakr.communiwise.user.history.CallInstance;
import com.wisekrakr.communiwise.user.phonebook.PhoneBookEntry;

import java.util.Collection;
import java.util.Map;

public interface AccountAPI {

    boolean isAuthenticated();

    void userIsOnline();

    Map<String, String> getUserInfo();

    PhoneBookEntry addContact(String username, String domain, int extension);
    boolean deleteContact(String username);
    boolean savePhoneBook();
    Collection<PhoneBookEntry>getContacts();

    void addCallInstance(CallInstance callInstance);
    boolean deleteCallInstance(String id);
    boolean saveCallLogBook();
    boolean clearCallLogBook();
    Collection<CallInstance>getCallLogs();
}
