package com.wisekrakr.communiwise.gui.layouts.fx.app;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.components.AlertFrame;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerJFXPanel;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.user.SipAccountManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.text.Text;

import javax.swing.*;

public class PhoneGUIController extends ControllerJFXPanel {

    private final PhoneAPI phone;
    private final AccountAPI account;
    private final AbstractGUI gui;
    private final EventManager eventManager;

    @FXML
    private TextField extensionField, domainField, portField;
    @FXML
    private Label username, address, contacts;
    @FXML
    private MenuItem loginMenuItem, prefsMenuItem, quitMenuItem, editMenuItem, contactsMenuItem, aboutMenuItem;
    @FXML
    private ToggleButton showDetails;
    @FXML
    private Text status;

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
    private void showDetails(){
        if (account.isAuthenticated() && showDetails.isSelected()) {

            username.setText("Welcome : " + account.getUserInfo().get(SipAccountManager.UserInfoPart.USERNAME.getInfoPart()));
            address.setText("your domain: " + account.getUserInfo().get(SipAccountManager.UserInfoPart.DOMAIN.getInfoPart()));
            contacts.setText("number of contacts " + account.getContacts().size());

            showDetails.setText("Hide");
        }else if(!showDetails.isSelected()){
            username.setText("Account details hidden!");
            address.setText("");
            contacts.setText("");

            showDetails.setText("Show");

        }else{
            showDetails.setSelected(false);
            showDetails.setText("Show");

            new AlertFrame().showAlert(gui, "You have to register first, go to: File -> Login ", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    @Override
    public void initComponents() {

        loginMenuItem.setOnAction(event -> {
            eventManager.onRegistering();
        });
        quitMenuItem.setOnAction(event -> eventManager.close());
        editMenuItem.setOnAction(event -> eventManager.menuAccountOpen());
        contactsMenuItem.setOnAction(event -> eventManager.menuContactListOpen());
        aboutMenuItem.setOnAction(event -> eventManager.menuAboutOpen());
        prefsMenuItem.setOnAction(event -> eventManager.menuPreferencesOpen());

        username.setText("Login to see your account details!");

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
