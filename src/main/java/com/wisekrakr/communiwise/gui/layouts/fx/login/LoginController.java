package com.wisekrakr.communiwise.gui.layouts.fx.login;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerJFXPanel;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


public class LoginController extends ControllerJFXPanel {
    private final PhoneAPI phone;
    private final AbstractGUI gui;

    @FXML
    private TextField username, address, domain, realm;
    @FXML
    private PasswordField password;

    public LoginController(PhoneAPI phone, AbstractGUI gui) {
        this.phone = phone;
        this.gui = gui;

    }

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
        phone.register(realm.getText(), domain.getText(), username.getText(), password.getText(), address.getText());

    }

    @FXML
    public void close(){
        gui.hideWindow();
    }

    @Override
    public void initComponents() {
        username.setText("damian2");
        domain.setText("asterisk.interzone");
        password.setText("45jf83f");
        realm.setText("asterisk");
        address.setText("sip:"+ username.getText() + "@" + domain.getText());
    }
}
