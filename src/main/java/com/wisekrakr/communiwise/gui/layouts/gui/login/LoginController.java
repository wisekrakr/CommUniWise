package com.wisekrakr.communiwise.gui.layouts.gui.login;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.gui.ControllerContext;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;


public class LoginController implements ControllerContext {
    private static EventManager eventManager;
    private static AbstractGUI gui;

    @FXML
    private TextField username, address, domain, realm;
    @FXML
    private PasswordField password;

    @FXML
    private void textFieldHandler() {

        username.setOnKeyPressed(event -> address.setText("sip:"+ username.getText() + "@" + domain.getText()));

        domain.setOnKeyPressed(event -> {
            String realmOnly = null;

            address.setText("sip:"+ username.getText() + "@" + domain.getText());

            int dot = domain.getText().indexOf(".");

            if(dot != -1){
                realmOnly = domain.getText().substring(0, dot);
            }

            realm.setText(realmOnly);
        });
        password.setText("45jf83f");

        
    }

    @FXML
    private void login() {
        eventManager.getPhone().register(realm.getText(), domain.getText(), username.getText(), password.getText(), address.getText());

    }

    @FXML
    @Override
    public void close(){
        gui.hideWindow();
    }

    @Override
    public ControllerContext initImplementations(EventManager eventManager, AbstractGUI gui) {
        LoginController.eventManager = eventManager;
        LoginController.gui = gui;
        return this;
    }


}
