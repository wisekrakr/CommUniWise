package com.wisekrakr.communiwise.gui.layouts.fx.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.components.AlertFrame;
import com.wisekrakr.communiwise.gui.layouts.components.Contact;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerContext;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.user.phonebook.PhoneBookEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;

public class ContactListController implements ControllerContext, Initializable {

    private static PhoneAPI phone;
    private static AccountAPI account;
    private static AbstractGUI gui;

    private ObservableList<Contact> contactList;
    private Contact selectedContact;

    @FXML
    private TableView<Contact> table;
    @FXML
    private TableColumn<Contact, String> colName, colExtension, colDomain;
    @FXML
    private Button editButton, callButton, removeButton;
    @FXML
    private TextField nameField, extensionField, domainField;


    public void initialize(PhoneAPI phone, AccountAPI account, AbstractGUI gui) {
        ContactListController.phone = phone;
        ContactListController.account = account;
        ContactListController.gui = gui;
    }

    @FXML
    private void addContact(){
        try{
            PhoneBookEntry entry = account.addContact(nameField.getText(), domainField.getText(), Integer.parseInt(extensionField.getText()));

            contactList.add(new Contact(entry.getUsername(), entry.getDomain(), String.valueOf(entry.getExtension()), entry.getContactId()));

            new AlertFrame().showAlert(gui, "Successfully added new contact: " + nameField.getText(), JOptionPane.INFORMATION_MESSAGE);
        }catch (Throwable t){
            new AlertFrame().showAlert(gui, "Failed adding contact: " + nameField.getText(),
                    JOptionPane.INFORMATION_MESSAGE);

            throw new IllegalStateException("Could not add phone book entry to contact list",t);
        }

    }

    @FXML
    private void callContact(){
        phone.initiateCall(extensionField.getText().trim(), domainField.getText().trim());
    }

    @FXML
    private void removeContact(){
        if (account.deleteContact(nameField.getText())){

            contactList.removeIf(c -> c.getName().equals(nameField.getText()));

            new AlertFrame().showAlert(gui, "Successfully removed contact: " + nameField.getText(), JOptionPane.INFORMATION_MESSAGE);
        }else{
            new AlertFrame().showAlert(gui, "Failed to remove contact: " + nameField.getText(), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onSelectedTableItem(){
        table.setOnMouseClicked((MouseEvent event) -> {

            if (event.getClickCount() > 0) {
                if (table.getSelectionModel().getSelectedItem() != null) {
                    selectedContact = table.getSelectionModel().getSelectedItem();

                    nameField.setText(selectedContact.getName());
                    domainField.setText(selectedContact.getDomain());
                    extensionField.setText(selectedContact.getExtension());

                    editButton.setDisable(false);
                    callButton.setDisable(false);
                    removeButton.setDisable(false);
                }
            }
        });

        if (table.getSelectionModel().getSelectedItem() == null) {
            editButton.setDisable(true);
            callButton.setDisable(true);
            removeButton.setDisable(true);
        }
    }

    @FXML
    private void save(){
        if(account.savePhoneBook()){
            new AlertFrame().showAlert(gui, "Successfully saved contact list", JOptionPane.INFORMATION_MESSAGE);
        }else{
            new AlertFrame().showAlert(gui, "Could not save contact list", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void close() {
        gui.hideWindow();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colName.setCellValueFactory(new PropertyValueFactory<>("Name"));
        colExtension.setCellValueFactory(new PropertyValueFactory<>("Extension"));
        colDomain.setCellValueFactory(new PropertyValueFactory<>("Domain"));

        contactList = FXCollections.observableArrayList();

        for (PhoneBookEntry contact : account.getContacts()) {
            System.out.println(contact.getUsername());

            contactList.add(new Contact(contact.getUsername(),contact.getDomain(), String.valueOf(contact.getExtension()), contact.getContactId()));

        }
        table.setItems(contactList);

        onSelectedTableItem();
    }
}
