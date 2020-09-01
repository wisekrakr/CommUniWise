package com.wisekrakr.communiwise.gui.layouts;

import javafx.embed.swing.JFXPanel;

public interface FrameContext {
    void prepareGUI();

    void hideWindow();

    void showWindow();

    void addFrameDragAbility();

    void initializeJFXPanel(JFXPanel jfxPanel, String fxml);
}
