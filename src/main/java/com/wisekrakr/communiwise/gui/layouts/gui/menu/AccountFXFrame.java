package com.wisekrakr.communiwise.gui.layouts.gui.menu;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.gui.GUIContext;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import java.awt.*;

public class AccountFXFrame extends AbstractGUI {

    private final EventManager eventManager;

    private JFXPanel jfxPanel;


    public AccountFXFrame(EventManager eventManager) {
        this.eventManager = eventManager;

        prepareGUI();
    }

    @Override
    public void prepareGUI() {
        setUndecorated(true);

        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(375, 207));

        setLocationRelativeTo(null);

    }

    @Override
    public void showWindow() {
        pack();
        setVisible(true);

        new AccountController().initImplementations(eventManager, this);

        Platform.runLater(() -> GUIContext.initFX(jfxPanel, "/account.fxml", this));

    }
}
