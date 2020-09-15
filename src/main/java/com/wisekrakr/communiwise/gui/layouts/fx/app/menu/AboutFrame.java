package com.wisekrakr.communiwise.gui.layouts.fx.app.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;

import java.awt.*;

public class AboutFrame extends AbstractGUI {

    private static final int DESIRED_HEIGHT = 335;
    private static final int DESIRED_WIDTH = 322;

    public AboutFrame() {
    }

    public static void main(String[] args) {
        AboutFrame aboutFrame = new AboutFrame();
        aboutFrame.prepareGUI();
        aboutFrame.showWindow();
    }

    @Override
    public void prepareGUI() {

        setUndecorated(true);

        setPreferredSize(new Dimension(DESIRED_WIDTH, DESIRED_HEIGHT));
        setLocationRelativeTo(null);

        add(new AboutGUIController(this).initialize("/about.fxml"), BorderLayout.CENTER);
    }

    @Override
    public void showWindow() {
        pack();
        setVisible(true);
    }

}
