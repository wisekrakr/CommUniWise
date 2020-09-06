package com.wisekrakr.communiwise.gui.layouts.fx.app;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.components.AlertFrame;
import com.wisekrakr.communiwise.gui.layouts.components.Contact;
import com.wisekrakr.communiwise.gui.layouts.components.IconCreator;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerJFXPanel;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.user.history.CallInstance;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import javax.swing.*;

public class PhoneGUIController extends ControllerJFXPanel {

    private final PhoneAPI phone;
    private final AccountAPI account;
    private final AbstractGUI gui;
    private final EventManager eventManager;

    private Contact selectedContact;

    @FXML
    private TextField extensionField, domainField, portField;
    @FXML
    private MenuItem loginMenuItem, prefsMenuItem, quitMenuItem, editMenuItem, contactsMenuItem, aboutMenuItem;
    @FXML
    private Button messengerButton, audioCallButton, videoCallButton, refreshButton, clearButton;
    @FXML
    private Text status;
    @FXML
    private TableView<Contact> table;
    @FXML
    private TableColumn<Contact, String> colDate, colExtension, colDomain;


    public PhoneGUIController(EventManager eventManager, PhoneAPI phone, AccountAPI account, AbstractGUI gui) {
        this.eventManager = eventManager;
        this.phone = phone;
        this.account = account;
        this.gui = gui;
    }

    @FXML
    private void call(){
        if(account.isAuthenticated() && checkForInputs()){
            phone.initiateCall(extensionField.getText().trim(), domainField.getText().trim());
        }else{
            new AlertFrame().showAlert(gui, "You have to register first, go to: File -> Login ", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @FXML
    private void clear(){
        account.clearCallLogBook();

        showRecentCalls();
    }

    @FXML
    private void refresh(){
        showRecentCalls();
    }

    private void onSelectedTableItem(){
        table.setOnMouseClicked((MouseEvent event) -> {

            if (event.getClickCount() > 1) {
                if (table.getSelectionModel().getSelectedItem() != null) {
                    selectedContact = table.getSelectionModel().getSelectedItem();

                    extensionField.setText(selectedContact.getExtension());
                    domainField.setText(selectedContact.getDomain());
                    portField.setText("5060");

                }
            }
        });

    }

    @Override
    public void initComponents() {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                loginMenuItem.setOnAction(event -> {
                    eventManager.onRegistering();
                });
                quitMenuItem.setOnAction(event -> eventManager.close());
                editMenuItem.setOnAction(event -> eventManager.menuAccountOpen());
                contactsMenuItem.setOnAction(event -> eventManager.menuContactListOpen());
                aboutMenuItem.setOnAction(event -> eventManager.menuAboutOpen());
                prefsMenuItem.setOnAction(event -> eventManager.menuPreferencesOpen());

                messengerButton.setGraphic(IconCreator.addIconForButton("/images/chat.png", 20,20 ));
                audioCallButton.setGraphic(IconCreator.addIconForButton("/images/mic.png", 20,20  ));
                videoCallButton.setGraphic(IconCreator.addIconForButton("/images/video-call.png", 20,20  ));
                refreshButton.setGraphic(IconCreator.addIconForButton("/images/refresh.png", 15,15  ));
                clearButton.setGraphic(IconCreator.addIconForButton("/images/clear.png", 15,15  ));

                colDate.setCellValueFactory(new PropertyValueFactory<>("Name"));
                colExtension.setCellValueFactory(new PropertyValueFactory<>("Extension"));
                colDomain.setCellValueFactory(new PropertyValueFactory<>("Domain"));
            }
        });




//        if(account.isAuthenticated() && account.getContacts() != null){
//            for (PhoneBookEntry contact : account.getContacts()) {
//                contactList.add(new Contact(contact.getUsername(),contact.getDomain(), String.valueOf(contact.getExtension()), contact.getContactId()));
//            }
//        }

        showRecentCalls();
        onSelectedTableItem();

    }

    public void showRecentCalls(){
        ObservableList<Contact> contactList = FXCollections.observableArrayList();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(account.isAuthenticated() && account.getCallLogs() != null){
                    for (CallInstance callInstance : account.getCallLogs()) {

                        System.out.println(callInstance.getFromCallDate());

                        contactList.add(new Contact(callInstance.getFromCallDate(), callInstance.getProxyAddress().getAddress().getHostName(),callInstance.getDisplayName(), callInstance.getContactId()));
                    }
                }

                table.setItems(contactList);
            }
        });
    }

    private boolean checkForInputs(){
        if(extensionField.getText().equals("")){
            new AlertFrame().showAlert(gui, "Please fill in an extension to call", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if(domainField.getText().equals("")){
            new AlertFrame().showAlert(gui, "Please fill in a domain", JOptionPane.INFORMATION_MESSAGE);
            return false;

        }
        if(portField.getText().equals("")){
            new AlertFrame().showAlert(gui, "Please fill in a proxy port", JOptionPane.INFORMATION_MESSAGE);
            return false;

        }
        return true;
    }
}
