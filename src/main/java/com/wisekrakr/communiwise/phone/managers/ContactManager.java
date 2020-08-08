package com.wisekrakr.communiwise.phone.managers;

import com.wisekrakr.communiwise.user.SipUserProfile;

import java.util.HashMap;
import java.util.Map;

public class ContactManager {

    private final Map<String, SipUserProfile> contacts =  new HashMap<>();

    public void addToContactList(String address, SipUserProfile sipUserProfile){
        try{
            if(!contacts.containsKey(address)){
                contacts.put(address, sipUserProfile);
            }
        }catch (Throwable e){
            throw new IllegalStateException("Contact already in contact list",e);
        }
    }

    public SipUserProfile getContact(String name) {
        SipUserProfile contact = null;
        try {
            if(contacts.get(name) != null){
                contact = contacts.get(name);
            }
        }catch (Throwable e){
            throw new IllegalStateException("Contact not found in contact list",e);
        }

        return contact;
    }

    public Map<String, SipUserProfile> getContacts() {
        return contacts;
    }
}
