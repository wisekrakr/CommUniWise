package com.wisekrakr.communiwise.gui.layouts.fx.app.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerJFXPanel;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;

import java.net.URL;
import java.util.ResourceBundle;

public class PreferencesController extends ControllerJFXPanel {

    private final SoundAPI sound;
    private final AbstractGUI gui;

    public PreferencesController(SoundAPI sound, AbstractGUI gui){
        this.sound = sound;
        this.gui = gui;
    }


    private void close() {

    }

    @Override
    public void initComponents() {

    }
}
