package com.wisekrakr.communiwise.gui.layouts.gui.login;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.gui.GUIContext;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import java.awt.*;

public class LoginFXGUI extends AbstractGUI {

    private static final int DESIRED_HEIGHT = 440;
    private static final int DESIRED_WIDTH = 676;

    private JFXPanel jfxPanel;
    private final EventManager eventManager;


    public LoginFXGUI(EventManager eventManager){
        this.eventManager = eventManager;

        prepareGUI();
    }

    @Override
    public void prepareGUI() {
        setLayout(new BorderLayout());
        setUndecorated(true);
        setBounds((getScreenSize().width - DESIRED_WIDTH) / 2, (getScreenSize().height - DESIRED_HEIGHT) / 2, DESIRED_WIDTH, DESIRED_HEIGHT);

        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(676, 442));
        setLocationRelativeTo(null);


    }


    @Override
    public void showWindow() {
        setVisible(true);

        new LoginController().initImplementations(eventManager, this);

        Platform.runLater(() -> GUIContext.initFX(jfxPanel, "/login.fxml", this));
    }
}
