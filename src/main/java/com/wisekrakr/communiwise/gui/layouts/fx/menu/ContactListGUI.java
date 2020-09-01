package com.wisekrakr.communiwise.gui.layouts.fx.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import javafx.embed.swing.JFXPanel;

import java.awt.*;

public class ContactListGUI extends AbstractGUI {
    private static final int DESIRED_HEIGHT = 577;
    private static final int DESIRED_WIDTH = 443;

    private JFXPanel jfxPanel;

    public ContactListGUI(PhoneAPI phone, AccountAPI account) {

        new ContactListController().initialize(phone,account, this);

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

        initializeJFXPanel(jfxPanel, "/contact-list.fxml");
    }

}
