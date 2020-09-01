package com.wisekrakr.communiwise.gui.layouts.fx.login;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import java.awt.*;

public class LoginGUI extends AbstractGUI {

    private static final int DESIRED_HEIGHT = 440;
    private static final int DESIRED_WIDTH = 676;

    private JFXPanel jfxPanel;

    public LoginGUI(PhoneAPI phone){

        new LoginController().initialize(phone, this);

        prepareGUI();
    }

    @Override
    public void prepareGUI() {

        setUndecorated(true);
        setBounds((getScreenSize().width - DESIRED_WIDTH) / 2, (getScreenSize().height - DESIRED_HEIGHT) / 2, DESIRED_WIDTH, DESIRED_HEIGHT);

        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null);

    }


    @Override
    public void showWindow() {
        setVisible(true);

        initializeJFXPanel(jfxPanel, "/login.fxml");
    }
}
