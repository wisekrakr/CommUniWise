package com.wisekrakr.communiwise.user.history;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

public class CallLogBook {

    private final String fileName;
    private final HashMap<String, CallInstance> entriesMap = new HashMap<>();

    /**
     * Constructs a new call log with the provided file name.
     * @param fileName The file name of the file to store this call log in disk.
     */
    public CallLogBook(String fileName) {
        this.fileName = fileName;
    }

    /**
     *
     * @return the values, CallInstances's of this call log.
     */
    public Collection<CallInstance> getEntries() {
        return entriesMap.values();
    }

    /**
     *
     * @return the keys, contacts of this call log.
     */
    public Collection<String> getKeys() {
        return entriesMap.keySet();
    }

    /**
     *
     * @return the name of the file at which this call log will be stored in disk.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     *
     * @return The number of call instances stored in this Call Log
     */
    public int getSize() {
        return entriesMap.size();
    }


    /**
     * Attempts to add a new entry to the map of call log entriesMap
     */
    public void addCallInstance(CallInstance callInstance ) {
        entriesMap.put(callInstance.getId(), callInstance);
    }

    /**
     * Attempts to remove a call instance from this Call Log ID
     * @param id The id of the call instance
     */
    public void deleteCallInstance(String id) {
        if(id != null) {
            entriesMap.remove(id);
        }
    }

    /**
     * Attempts to remove all call instances from this Call Log book
     */
    public void clearCallLogBook() {
        if(!entriesMap.isEmpty()){
            entriesMap.clear();
        }
    }

    /**
     * Adds the collection of CallInstances loaded from disk into this CallLogBook.
     * @param callLogEntries The collection of call instances to load into this call log book.
     */
    public void addFromFile(Collection<CallInstance> callLogEntries) {
        if(callLogEntries != null) {
            for (CallInstance entry : callLogEntries) {
                if (entry != null) {
                    //bypass validation and add the contact to the map by name.
                    entriesMap.put(entry.getId(), entry);
                }
            }
        }
    }

    /**
     * Tries to save this CallLogBook's call instances to disk
     * @return Returns true if the save was successful and false otherwise.
     */
    public boolean save() {
        return CallLogFileManager.save(this);
    }

    /**
     * Tries to load a CallLogBook from disk
     * @param fileName the name of the file to load the CallInstances from
     * @return The loaded CallLog or null if none.
     */
    public static CallLogBook load(String fileName) throws IOException {
        return CallLogFileManager.load((fileName));
    }
}
