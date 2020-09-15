package com.wisekrakr.communiwise.gui.layouts.fx.app.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.AbstractJFXPanel;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerContext;
import com.wisekrakr.communiwise.user.SipAccountManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.util.Map;

public class AccountGUIController extends AbstractJFXPanel implements ControllerContext {

    private final Map<String, String> userInfo;
    private final AbstractGUI gui;

    @FXML
    private AnchorPane container;
    @FXML
    private Label username,domain;

    public AccountGUIController(Map<String, String> userInfo, AbstractGUI gui) {
        this.userInfo = userInfo;
        this.gui = gui;
    }


    @FXML
    @Override
    public void close() {
        gui.hideWindow();
    }

    @FXML
    @Override
    public void drag() {
        addDraggability(gui, container);
    }

    @Override
    public void initComponents() {
        String dom = userInfo.get(SipAccountManager.UserInfoPart.DOMAIN.getInfoPart());
        String name = userInfo.get(SipAccountManager.UserInfoPart.USERNAME.getInfoPart());

        username.setText(name);
        domain.setText(dom);
    }


}
