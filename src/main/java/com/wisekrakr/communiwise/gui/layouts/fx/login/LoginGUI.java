package com.wisekrakr.communiwise.gui.layouts.fx.login;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;

import java.awt.*;

public class LoginGUI extends AbstractGUI {

    private static final int DESIRED_HEIGHT = 440;
    private static final int DESIRED_WIDTH = 676;

    private final PhoneAPI phone;

    public LoginGUI(PhoneAPI phone){
        this.phone = phone;
    }

    @Override
    public void prepareGUI() {

        setUndecorated(true);
        setBounds((getScreenSize().width - DESIRED_WIDTH) / 2, (getScreenSize().height - DESIRED_HEIGHT) / 2, DESIRED_WIDTH, DESIRED_HEIGHT);

        LoginGUIController controller = (LoginGUIController) new LoginGUIController(phone, this).initialize("/login.fxml");
        controller.initComponents();

        add(controller, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    @Override
    public void showWindow() {
        setVisible(true);
    }
}
