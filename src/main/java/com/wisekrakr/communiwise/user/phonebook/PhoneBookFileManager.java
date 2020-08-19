package com.wisekrakr.communiwise.user.phonebook;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;

/**
 * This class is used to save and load the entries of a PhoneBook to and from file on disk
 * by means of serialization and deserialization
 */
public class PhoneBookFileManager {
    /**
     * Seriliazes Collection of the entries of a PhoneBook and saves in disk.
     * @param phoneBook The phonebook to be saved
     * @return Returns true if save was successful and false otherwise.
     */
    protected static boolean save(PhoneBook phoneBook) {
        if(phoneBook != null) {
            String fileName = phoneBook.getFileName();

            //make sure the file is a txt file
            String fileNameAndExt = getFileNameWithExtension(fileName);

            FileOutputStream fileOutputStream = null;
            ObjectOutputStream objectOutputStream = null;
            try {
                //create a file output stream to write the objects to
                fileOutputStream = new FileOutputStream(fileNameAndExt);
                //create an object output stream to write out the objects to the file
                objectOutputStream = new ObjectOutputStream(fileOutputStream);

                /*convert the collection of phone book entries into a LinkedList
                because LinkedLists implement Serializable*/
                LinkedList<PhoneBookEntry> serializableList = new LinkedList<>(phoneBook.getEntries());
                //write the serializable list to the object output stream
                objectOutputStream.writeObject(serializableList);
                //flush the object output stream
                objectOutputStream.flush();

                //set each entry's isNew value to false because they are saved now.
                for(PhoneBookEntry entry: serializableList) {
                    entry.setIsNew(false);
                }

                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //before proceeding, close output streams if they were opened
                closeCloseable(fileOutputStream);
                closeCloseable(objectOutputStream);
            }
        }

        //fail
        return false;
    }

    /**
     * Find the file holding the serialized PhoneBookEntry Collection,
     * deserialize it and return a containing PhoneBook of its elements.
     * @param fileName The name of the file from which to load the phone book's entries from.
     * @return Returns a new PhoneBook if loading succeeded and null otherwise.
     */
    protected static PhoneBook load(String fileName) throws IOException{
        if(fileName != null && !fileName.trim().isEmpty()) {
            //make sure the file is a txt file
            String fileNameWithExt = getFileNameWithExtension(fileName);

            FileInputStream fileInputStream = null;
            ObjectInputStream objectinputstream = null;
            try {

                //create the file input stream with the fileNameWithExt to read the objects from
                fileInputStream = new FileInputStream(fileNameWithExt);
                //create an object input stream on the file input stream to read in the objects from the file
                objectinputstream = new ObjectInputStream(fileInputStream);

                //read the deserialized object from the object input stream and cast it to a collection of PhoneBookEntry
                Collection<PhoneBookEntry> deserializedPhoneBookEntries = (Collection<PhoneBookEntry>) objectinputstream.readObject();

                //create a new PhoneBook to load the deserialized entries into
                PhoneBook phoneBook = new PhoneBook(fileName);
                //add the collection of phone book entries to the phone book
                phoneBook.addFromFile(deserializedPhoneBookEntries);

                return phoneBook;
            } catch (Throwable e) {
                //fail
                System.out.println("Failed to load file " + e.getMessage());
            }  finally {
                //before proceeding, close input streams if they were opened
                closeCloseable(fileInputStream);
                closeCloseable(objectinputstream);
            }
        }

        //fail
        return null;
    }

    /**
     * Makes sure the fileName has an extension of .txt and returns it with the extension.
     * @param fileName
     * @return The file name with its extension.
     */
    private static String getFileNameWithExtension(String fileName) {
        if(fileName != null && !fileName.trim().isEmpty()) {
            fileName = fileName.replaceAll("[^A-Za-z0-9]", "");
            return fileName.contains(".txt") ? fileName : fileName + ".txt";
        } else return fileName;
    }

    /**
     * Closes a valid Closeable object.
     * @param closeable The Closable to be closed.
     */
    private static void closeCloseable(Closeable closeable) {
        if(closeable != null) {
            //try to close it
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
