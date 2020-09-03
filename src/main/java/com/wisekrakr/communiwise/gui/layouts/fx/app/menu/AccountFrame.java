package com.wisekrakr.communiwise.gui.layouts.fx.app.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;

import java.awt.*;

public class AccountFrame extends AbstractGUI {

    private static final int DESIRED_HEIGHT = 207;
    private static final int DESIRED_WIDTH = 375;

    private final AccountAPI account;

    public AccountFrame(AccountAPI account) {
        this.account = account;
    }

    @Override
    public void prepareGUI() {
        setUndecorated(true);

        setPreferredSize(new Dimension(DESIRED_WIDTH, DESIRED_HEIGHT));
        setLocationRelativeTo(null);

        AccountController controller = (AccountController) new AccountController(account.getUserInfo(), this).initialize("/account.fxml");
        controller.initComponents();

        add(controller,BorderLayout.CENTER);
    }

    @Override
    public void showWindow() {
        pack();
        setVisible(true);
    }
}
