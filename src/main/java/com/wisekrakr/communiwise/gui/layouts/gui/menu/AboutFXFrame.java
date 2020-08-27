package com.wisekrakr.communiwise.gui.layouts.gui.menu;

import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.gui.GUIContext;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import java.awt.*;

public class AboutFXFrame extends AbstractGUI {

    private JFXPanel jfxPanel;

    public AboutFXFrame() {
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

        new AboutController().initImplementations(null, this);

        Platform.runLater(() -> GUIContext.initFX(jfxPanel, "/about.fxml", this));
    }

}
