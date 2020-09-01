package com.wisekrakr.communiwise.gui.layouts.fx.call;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerContext;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.phone.calling.CallInstance;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class IncomingCallController implements ControllerContext, Initializable {
    private static AbstractGUI gui;
    private static CallInstance callInstance;
    private static PhoneAPI phone;

    @FXML
    private Label username;


    public void initialize(PhoneAPI phone, AbstractGUI gui, CallInstance callInstance) {
        IncomingCallController.phone = phone;
        IncomingCallController.gui = gui;
        IncomingCallController.callInstance = callInstance;

    }

    private void showInfo(){
        username.setText(callInstance.getDisplayName());

//        username.setVisible(true);
    }

    @FXML
    @Override
    public void close() {
        phone.reject();

        gui.hideWindow();
    }

    @FXML
    private void accept(){
        phone.accept(callInstance.getProxyAddress().getAddress().getHostAddress());
    }

    @FXML
    private void reject(){
        phone.reject();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showInfo();
    }
}
