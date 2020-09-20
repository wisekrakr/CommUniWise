package com.wisekrakr.communiwise.user;

import com.wisekrakr.communiwise.user.history.CallInstance;
import com.wisekrakr.communiwise.user.history.CallLogBook;
import com.wisekrakr.communiwise.user.phonebook.PhoneBook;
import com.wisekrakr.communiwise.user.phonebook.PhoneBookEntry;

public class ContactManager {
    private PhoneBook phoneBook;
    private CallLogBook callLogBook;

    public PhoneBook getPhoneBook() {
        return phoneBook;
    }
    public CallLogBook getCallLogBook() {
        return callLogBook;
    }

    /**
     * Attempts to load a previously saved phone book from the file with name matching phoneBookFileName
     */
    public void loadPhoneBook(String filename) {

        //try to load the user's phone book with the file name
        try {
            phoneBook = PhoneBook.load(filename);

            if(phoneBook == null){
                phoneBook = new PhoneBook(filename);
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Could not load phonebook", e);
        }

    }

    /**
     * Attempts to load a previously saved call log from the file with name matching callLogFileName
     */
    public void loadCallLogBook(String filename) {

        //try to load the user's phone book with the file name
        try {
            callLogBook = CallLogBook.load(filename);

            if(callLogBook == null){
                callLogBook = new CallLogBook(filename);
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Could not load call log book", e);
        }
    }

    /**
     * A method for adding PhoneBookEntries
     * @param username name of the contact
     * @param domain domain of the contact
     * @param extension extension number of the contact
     * @return a new phonebook entry
     */
    public PhoneBookEntry addContact(String username, String domain, int extension){
        try {
            return phoneBook.addContact(username, domain, extension);

        }catch (Throwable e){
            throw new IllegalStateException("Could not add contact",e);
        }
    }

    /**
     * A method for adding CallInstances to the Call Log Book
     * @param callInstance the call that has been
     */
    public void addCallInstance(CallInstance callInstance){
        try {
             callLogBook.addCallInstance(callInstance);

        }catch (Throwable e){
            throw new IllegalStateException("Could not add call log instance",e);
        }
    }

    /**
     * A method to delete an entry out of the user's phonebook. The entry is searched on an unique username.
     * @param username the contact's username
     * @return true if there is no exception
     */
    public boolean deleteContact(String username){
        try {
            phoneBook.deleteContact(username);
            return true;

        }catch (Throwable e){
            throw new IllegalStateException("Could not delete contact",e);
        }
    }

    /**
     * A method to delete an entry out of the user's phonebook. The entry is searched on an unique username.
     * @param id the call instance's id
     * @return true if there is no exception
     */
    public boolean deleteCallInstance(String id){
        try {
            callLogBook.deleteCallInstance(id);
            return true;

        }catch (Throwable e){
            throw new IllegalStateException("Could not delete contact",e);
        }
    }

    /**
     * A method to clear all entries in the Call Log Book
     * @return true if there is no exception
     */
    public boolean clearCallLogBook(){
        try {
            callLogBook.clearCallLogBook();
            callLogBook.save();
            return true;

        }catch (Throwable e){
            throw new IllegalStateException("Could not clear call log",e);
        }
    }

    /**
     * Saves all entries in the phonebook.
     * @return true if there is no exception
     */
    public boolean savePhoneBook(){
        try {
            phoneBook.save();
            return true;

        }catch (Throwable e){
            throw new IllegalStateException("Could not save phonebook",e);
        }
    }

    /**
     * Saves all entries in the call log.
     * @return true if there is no exception
     */
    public boolean saveCallLogBook(){
        try {
            callLogBook.save();
            return true;

        }catch (Throwable e){
            throw new IllegalStateException("Could not save call log book",e);
        }
    }
}
