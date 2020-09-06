package com.wisekrakr.communiwise.user.phonebook;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

public class PhoneBook {

    //the name of the file to store this phone book in disk. Unchangeable
    private final String fileName;
    //Stores entries for this phone book. The entries of this map may be referred to as contacts
    private final HashMap<String,PhoneBookEntry> entriesMap = new HashMap<>();


    /**
     * Constructs a new phone book with the provided file name.
     * @param fileName The file name of the file to store this phone book in disk.
     */
    public PhoneBook(String fileName) {
        this.fileName = fileName;
    }

    /**
     *
     * @return the values, PhoneBookEntry's of this phone book.
     */
    public Collection<PhoneBookEntry> getEntries() {
        return entriesMap.values();
    }

    /**
     *
     * @return the keys, contacts of this phone book.
     */
    public Collection<String> getKeys() {
        return entriesMap.keySet();
    }

    /**
     *
     * @return the name of the file at which this phone book will be stored in disk.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     *
     * @return The number of contacts stored in this PhoneBook
     */
    public int getSize() {
        return entriesMap.size();
    }

    /**
     * Attempts to add a new entry to the map of phone book entriesMap by name
     * after validating the name and number.
     * @param username The name of the contact
     * @param domain The domain of the contact
     * @param extension The phone number of the contact
     * @return the result of the action.
     * ADDED or UPDATED means the contact was successfully added
     */
    public PhoneBookEntry addContact(String username, String domain, int extension ) {
        PhoneBookEntry entry;

        if(isValidName(username) && isValidName(domain)){

            //make the name lowercase for consistency
            username = username.toLowerCase();

            entry = entriesMap.get(username);
            //check if the contact already exists in the map by name
            if(entry != null){
                entry.setDomain(domain);
                entry.setExtension(extension);

                //mark this contact as unsaved because its been changed
                entry.setIsNew(false);
            } else {
                //if not, create the new contact and add it to the map by name
                entry = new PhoneBookEntry(username, domain, extension, Math.random() * 100000000);

                entriesMap.put(username, entry);
            }

        } else {
            //fail
            throw new IllegalStateException(PhoneBook.class.getName() + ": Could not add to phonebook");
        }

        return entry;

    }

    /**
     * Adds the collection of PhoneBookEntry's loaded from disk into this PhoneBook.
     * @param phoneBookEntries The collection of contacts to load into this phone book.
     */
    public void addFromFile(Collection<PhoneBookEntry> phoneBookEntries) {
        if(phoneBookEntries != null) {
            for (PhoneBookEntry entry : phoneBookEntries) {
                if (entry != null) {
                    entriesMap.put(entry.getUsername(), entry);
                }
            }
        }
    }

    /**
     * Attempts to remove a contact from this PhoneBook by name
     * @param name The name of the contact
     */
    public void deleteContact(String name) {
        if(name != null) {
            entriesMap.remove(name.toLowerCase());
        }
    }


    /**
     * Displays all of the contacts in this phone book
     */
    public void display() {
        if(!entriesMap.isEmpty()) {
            StringBuilder contacts = new StringBuilder();
            for (PhoneBookEntry contact : entriesMap.values()) {
                contacts.append(contact + " | ");
            }
        }
    }

    /**
     * Returns whether or not the provided name is a valid name.
     * @param name The name to check
     * @return true if the provided name is a valid name and false otherwise.
     */
    public boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }


    /**
     * Tries to save this PhoneBook's contacts to disk
     * @return Returns true if the save was successful and false otherwise.
     */
    public boolean save() {
        return PhoneBookFileManager.save(this);
    }

    /**
     * Tries to load a PhoneBook from disk
     * @param fileName the name of the file to load the PhoneBookEntry's from
     * @return The loaded PhoneBook or null if none.
     */
    public static PhoneBook load(String fileName) throws IOException {
        return PhoneBookFileManager.load((fileName));
    }

}
