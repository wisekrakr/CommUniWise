package com.wisekrakr.communiwise.gui.layouts.fx.app.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerJFXPanel;
import com.wisekrakr.communiwise.user.SipAccountManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Map;

public class AccountController extends ControllerJFXPanel {

    private final Map<String, String> userInfo;
    private final AbstractGUI gui;

    @FXML
    private Label username,domain;

    public AccountController(Map<String, String> userInfo,AbstractGUI gui) {
        this.userInfo = userInfo;
        this.gui = gui;
    }


    @FXML
    private void close() {
        gui.hideWindow();
    }

    @Override
    public void initComponents() {
        String dom = userInfo.get(SipAccountManager.UserInfoPart.DOMAIN.getInfoPart());
        String name = userInfo.get(SipAccountManager.UserInfoPart.USERNAME.getInfoPart());

        username.setText(name);
        domain.setText(dom);
    }


}
