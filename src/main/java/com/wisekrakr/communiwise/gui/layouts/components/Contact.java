package com.wisekrakr.communiwise.gui.layouts.components;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Contact {

    private SimpleStringProperty name;
    private SimpleStringProperty domain;
    private SimpleStringProperty extension;
    private SimpleDoubleProperty id;

    public Contact() {
    }


    public Contact(String name, String domain, String extension, double id) {
        this.name = new SimpleStringProperty(name);
        this.domain = new SimpleStringProperty(domain);
        this.extension = new SimpleStringProperty(extension);
        this.id = new SimpleDoubleProperty(id);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDomain() {
        return domain.get();
    }

    public SimpleStringProperty domainProperty() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain.set(domain);
    }

    public String getExtension() {
        return extension.get();
    }

    public SimpleStringProperty extensionProperty() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension.set(extension);
    }

    public double getId() {
        return id.get();
    }

    public SimpleDoubleProperty idProperty() {
        return id;
    }

    public void setId(double id) {
        this.id.set(id);
    }
}
