package com.wisekrakr.communiwise.user;

import com.wisekrakr.communiwise.user.phonebook.PhoneBook;

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
     * Handles the user's last menu selection.
     */
    public void handleUserMenuSelection(UserOption userOption, String username, String domain, int extension) {

        switch (userOption) {
            case VIEW_CONTACTS:
                phoneBook.display();
                break;
            case ADD_CONTACT:
                try {
                    phoneBook.addContact(username, domain, extension/*, false*/);
                }catch (Throwable e){
                    throw new IllegalStateException("Could not add contact",e);
                }

                break;
            case DELETE_CONTACT:

                try {
                    phoneBook.deleteContact(username);
                }catch (Throwable e){
                    throw new IllegalStateException("Could not delete contact",e);
                }

                break;
            case SAVE:
                try {
                    phoneBook.save();
                }catch (Throwable e){
                    throw new IllegalStateException("Could not save phonebook",e);
                }
                break;
            default:
                throw new IllegalStateException("No action performed in phonebook");
        }
    }

    /**
     * An enum representing the types of options
     * the user can choose from in the menu to perform actions.
     */
    public enum UserOption {
        VIEW_CONTACTS("1","View Contacts"),
        ADD_CONTACT("2", "Add Contact"),
        DELETE_CONTACT("3", "Delete Contact"),
        SAVE("4", "Save");

        private final String description;
        private String key;

        UserOption(String key, String s) {
            this.key = key;
            this.description = s;
        }

        public String getDescription() {
            return description;
        }

        public String getKey() {
            return key;
        }

        public static UserOption getByKey(String key){
            for(UserOption userOption : values()){
                if( userOption.getKey().equals(key)){
                    return userOption;
                }
            }
            return null;
        }
    }
}
