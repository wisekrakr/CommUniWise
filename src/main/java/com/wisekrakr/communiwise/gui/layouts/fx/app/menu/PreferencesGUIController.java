package com.wisekrakr.communiwise.gui.layouts.fx.app.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.AbstractJFXPanel;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerContext;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

public class PreferencesGUIController extends AbstractJFXPanel implements ControllerContext {

    private final SoundAPI sound;
    private final AbstractGUI gui;

    @FXML
    private TabPane container;

    public PreferencesGUIController(SoundAPI sound, AbstractGUI gui){
        this.sound = sound;
        this.gui = gui;
    }


    @Override
    public void close() {
        gui.hideWindow();
    }

    @FXML
    @Override
    public void drag() {
        addDraggability(gui, container);
    }

    @Override
    public void initComponents() {

    }
}
