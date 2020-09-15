package com.wisekrakr.communiwise.gui.layouts.fx.app;


import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;

import java.awt.*;

public class PhoneGUI extends AbstractGUI {

    private final EventManager eventManager;
    private final PhoneAPI phone;
    private final AccountAPI account;

    private static final int DESIRED_HEIGHT = 310;
    private static final int DESIRED_WIDTH = 923;


    public PhoneGUI(EventManager eventManager, PhoneAPI phone, AccountAPI account) {
        this.eventManager = eventManager;
        this.phone = phone;
        this.account = account;
    }

    @Override
    public void prepareGUI() {
        setTitle("CommUniWise Sip Phone");
        setUndecorated(true);

        setBounds((getScreenSize().width - DESIRED_WIDTH), DESIRED_HEIGHT/3, DESIRED_WIDTH, DESIRED_HEIGHT);

        PhoneGUIController controller = (PhoneGUIController) new PhoneGUIController(eventManager, phone, account, this).initialize("/main.fxml");
        controller.initComponents();
        add(controller, BorderLayout.CENTER);
    }

    @Override
    public void showWindow() {
        setVisible(true);
    }
}
