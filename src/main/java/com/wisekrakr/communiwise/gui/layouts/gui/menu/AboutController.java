package com.wisekrakr.communiwise.gui.layouts.gui.menu;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.gui.ControllerContext;
import javafx.fxml.FXML;

public class AboutController implements ControllerContext {
    private static AbstractGUI gui;

    @Override
    public ControllerContext initImplementations(EventManager eventManager, AbstractGUI gui) {
        AboutController.gui = gui;
        return this;
    }

    @FXML
    @Override
    public void close(){
        gui.hideWindow();
    }
}
