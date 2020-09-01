package com.wisekrakr.communiwise.gui.layouts.fx.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import java.awt.*;

public class AboutFrame extends AbstractGUI {

    private JFXPanel jfxPanel;

    public AboutFrame() {

        new AboutController().initialize(this);

        prepareGUI();
    }

    @Override
    public void prepareGUI() {

        setUndecorated(true);

        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(220, 207));
        setLocationRelativeTo(null);
    }

    @Override
    public void showWindow() {
        pack();
        setVisible(true);

        initializeJFXPanel(jfxPanel, "/about.fxml");
    }

}
