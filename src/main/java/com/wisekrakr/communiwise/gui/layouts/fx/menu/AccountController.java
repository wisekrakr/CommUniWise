package com.wisekrakr.communiwise.gui.layouts.fx.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerContext;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.user.SipAccountManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class AccountController implements ControllerContext, Initializable {
    private static AccountAPI account;
    private static AbstractGUI gui;

    @FXML
    private Label username,domain;

    private void showInfo(){
        String dom = account.getUserInfo().get(SipAccountManager.UserInfoPart.DOMAIN.getInfoPart());
        String name = account.getUserInfo().get(SipAccountManager.UserInfoPart.USERNAME.getInfoPart());

        username.setText(name);
        domain.setText(dom);
    }

    @FXML
    @Override
    public void close() {
        gui.hideWindow();
    }


    public void initialize(AccountAPI account, AbstractGUI gui) {
        AccountController.account = account;
        AccountController.gui = gui;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showInfo();
    }
}
