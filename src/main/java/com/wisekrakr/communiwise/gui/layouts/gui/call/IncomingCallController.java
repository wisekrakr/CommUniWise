package com.wisekrakr.communiwise.gui.layouts.gui.call;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.gui.ControllerContext;
import com.wisekrakr.communiwise.phone.calling.CallInstance;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class IncomingCallController implements ControllerContext {
    private static EventManager eventManager;
    private static AbstractGUI gui;
    private static CallInstance callInstance;

    @FXML
    private final Label username = new Label();

    @Override
    public ControllerContext initImplementations(EventManager eventManager, AbstractGUI gui) {
        IncomingCallController.eventManager = eventManager;
        IncomingCallController.gui = gui;
        return this;
    }

    public void setCallInstance(CallInstance callInstance){
        IncomingCallController.callInstance = callInstance;
    }

    public void showInfo(){
        username.setText(callInstance.getProxy());
    }

    @FXML
    @Override
    public void close() {
        gui.hideWindow();

        eventManager.getPhone().reject();
    }

    @FXML
    private void accept(){
        System.out.println(callInstance.getProxyAddress().getAddress().getHostAddress() + "   PICKED UP!!!");
        eventManager.getPhone().accept(callInstance.getProxyAddress().getAddress().getHostAddress());
    }

    @FXML
    private void reject(){
        eventManager.getPhone().reject();
    }




}
