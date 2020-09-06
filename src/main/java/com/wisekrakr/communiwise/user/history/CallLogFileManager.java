package com.wisekrakr.communiwise.user.history;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;

public class CallLogFileManager {
    /**
     * Seriliazes Collection of the entries of a CallLogBook and saves in disk.
     * @param callLogBook the call log book to be saved
     * @return Returns true if save was successful and false otherwise.
     */
    protected static boolean save(CallLogBook callLogBook) {
        if(callLogBook != null) {
            String fileName = callLogBook.getFileName();

            String fileNameAndExt = getFileNameWithExtension(fileName);

            FileOutputStream fileOutputStream = null;
            ObjectOutputStream objectOutputStream = null;
            try {

                fileOutputStream = new FileOutputStream(fileNameAndExt);

                objectOutputStream = new ObjectOutputStream(fileOutputStream);

                LinkedList<CallInstance> serializableList = new LinkedList<>(callLogBook.getEntries());

                objectOutputStream.writeObject(serializableList);

                objectOutputStream.flush();

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
     * Find the file holding the serialized CallInstancesEntry Collection,
     * deserialize it and return a containing CallLog of its elements.
     * @param fileName The name of the file from which to load the call log book's entries from.
     * @return Returns a new CallLogBook if loading succeeded and null otherwise.
     */
    protected static CallLogBook load(String fileName) throws IOException{
        if(fileName != null && !fileName.trim().isEmpty()) {
            String fileNameWithExt = getFileNameWithExtension(fileName);

            FileInputStream fileInputStream = null;
            ObjectInputStream objectinputstream = null;

            try {
//todo find the reason why I cannot create a new book when there is none found
                fileInputStream = new FileInputStream(fileNameWithExt);
                objectinputstream = new ObjectInputStream(fileInputStream);

                Collection<CallInstance> deserializedCallInstancesEntries = (Collection<CallInstance>) objectinputstream.readObject();

                CallLogBook callLogBook = new CallLogBook(fileName);
                callLogBook.addFromFile(deserializedCallInstancesEntries);

                return callLogBook;
            } catch (Throwable e) {
                System.out.println("Failed to load file " + e.getMessage());

//                throw new IllegalStateException("Failed to load file ",e);

            }  finally {

                //before proceeding, close input streams if they were opened
                closeCloseable(fileInputStream);
                closeCloseable(objectinputstream);
            }
        }

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
