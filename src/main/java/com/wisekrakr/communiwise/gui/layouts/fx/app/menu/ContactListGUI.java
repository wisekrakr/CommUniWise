package com.wisekrakr.communiwise.gui.layouts.fx.app.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;

import java.awt.*;

public class ContactListGUI extends AbstractGUI {
    private static final int DESIRED_HEIGHT = 577;
    private static final int DESIRED_WIDTH = 443;

    private final PhoneAPI phone;
    private final AccountAPI account;


    public ContactListGUI(PhoneAPI phone, AccountAPI account) {
        this.phone = phone;
        this.account = account;
    }


    @Override
    public void prepareGUI() {
        setUndecorated(true);
        setBounds((getScreenSize().width - DESIRED_WIDTH) / 2, (getScreenSize().height - DESIRED_HEIGHT) / 2, DESIRED_WIDTH, DESIRED_HEIGHT);

        ContactListGUIController controller = (ContactListGUIController) new ContactListGUIController(phone,account, this).initialize("/contact-list.fxml");
        controller.initComponents();
        add(controller, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    @Override
    public void showWindow() {
        setVisible(true);
    }

}
