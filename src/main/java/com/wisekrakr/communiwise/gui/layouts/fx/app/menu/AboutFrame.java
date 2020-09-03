package com.wisekrakr.communiwise.gui.layouts.fx.app.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;

import java.awt.*;

public class AboutFrame extends AbstractGUI {

    public AboutFrame() {
    }

    @Override
    public void prepareGUI() {

        setUndecorated(true);

        setPreferredSize(new Dimension(220, 207));
        setLocationRelativeTo(null);

        add(new AboutController(this).initialize("/about.fxml"), BorderLayout.CENTER);
    }

    @Override
    public void showWindow() {
        pack();
        setVisible(true);
    }

}
