package com.wisekrakr.communiwise.user;

import com.wisekrakr.communiwise.user.phonebook.PhoneBook;
import com.wisekrakr.communiwise.user.phonebook.PhoneBookEntry;

public class ContactManager {
    private PhoneBook phoneBook;

    public PhoneBook getPhoneBook() {
        return phoneBook;
    }

    /**
     * Attempts to load a previously saved phone book from the file with name matching phoneBookFileName
     */
    public void loadPhoneBook(String filename) {

        //try to load the user's phone book with the file name
        try {
            phoneBook = PhoneBook.load(filename);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Could not load phonebook", e);
        }

        if(phoneBook != null) {
            phoneBook.display();
        } else {
            //no phone book loaded. create new one
            phoneBook = new PhoneBook(filename);
        }
    }

    /**
     * A method for adding PhoneBookEntries, this differs from the method below so that we can also return that entry
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


}
