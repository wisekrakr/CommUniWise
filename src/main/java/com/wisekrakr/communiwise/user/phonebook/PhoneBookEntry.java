package com.wisekrakr.communiwise.user.phonebook;

import java.io.Serializable;

import static java.lang.String.format;

public class PhoneBookEntry implements Serializable {
//    private static final long serialVersionUID = 6529685098267757690L;

    private final String username;
    private String domain;
    private int extension;
    //whether or not this contact is new and unsaved. Won't be serialized.
    private transient boolean isNew;

    /**
     * Constructs a new contact with the provided name, domain and number.
     * @param username The name of this contact.
     * @param domain The domain of this contact.
     * @param extension The extension number for this contact
     */
    public PhoneBookEntry(String username, String domain,int extension/*, boolean isNew*/) {
        this.username = username;
        this.domain = domain;
        this.extension = extension;
        this.isNew = true;
    }

    /**
     * Sets the domain of this contact
     * @param domain The new domain for this contact.
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     *
     * @return Returns the name of this contact
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @return Returns the domain of this contact
     */
    public String getDomain() {
        return domain;
    }

    /**
     *
     * @return Returns the extension of this contact
     */
    public int getExtension() {
        return extension;
    }

    /**
     * Toggles whether or not this contact is isNew.
     * @param isNew
     */
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     *
     * @return Returns a string representation of this contact.
     */
    @Override
    public String toString() {
        return format("< %s: %s %s %s >", username, domain, extension, isNew ? "| new": "");
    }

    /**
     * Sets the number of this contact
     * @param extension The new number for this contact.
     */
    public void setExtension(int extension) {
        this.extension = extension;
    }
}
