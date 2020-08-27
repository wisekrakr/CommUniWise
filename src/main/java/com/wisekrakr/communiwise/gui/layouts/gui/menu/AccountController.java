package com.wisekrakr.communiwise.gui.layouts.gui.menu;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.gui.ControllerContext;
import com.wisekrakr.communiwise.user.SipAccountManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class AccountController implements ControllerContext, Initializable {
    private static EventManager eventManager;
    private static AbstractGUI gui;

    @FXML
    private Label username,domain;

    private void showInfo(){
        String dom = eventManager.getAccount().getUserInfo().get(SipAccountManager.UserInfoPart.DOMAIN.getInfoPart());
        String name = eventManager.getAccount().getUserInfo().get(SipAccountManager.UserInfoPart.USERNAME.getInfoPart());

        username.setText(name);
        domain.setText(dom);
    }

    @FXML
    @Override
    public void close() {
        gui.hideWindow();
    }

    @Override
    public ControllerContext initImplementations(EventManager eventManager, AbstractGUI gui) {
        AccountController.eventManager = eventManager;
        AccountController.gui = gui;
        return this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showInfo();
    }
}
