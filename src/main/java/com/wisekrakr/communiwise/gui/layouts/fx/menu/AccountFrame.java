package com.wisekrakr.communiwise.gui.layouts.fx.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import java.awt.*;

public class AccountFrame extends AbstractGUI {

    private JFXPanel jfxPanel;
    private final AccountAPI account;


    public AccountFrame(AccountAPI account) {
        this.account = account;

        new AccountController().initialize(account, this);

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

        initializeJFXPanel(jfxPanel, "/account.fxml");

    }
}
