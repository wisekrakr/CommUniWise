package com.wisekrakr.communiwise.gui.layouts.fx.app.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerJFXPanel;
import javafx.fxml.FXML;

public class AboutController extends ControllerJFXPanel {
    private AbstractGUI gui;

    public AboutController(AbstractGUI gui) {
        this.gui = gui;
    }

    @FXML
    private void close(){
        gui.hideWindow();
    }

}
