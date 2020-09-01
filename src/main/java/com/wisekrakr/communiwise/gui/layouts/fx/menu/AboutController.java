package com.wisekrakr.communiwise.gui.layouts.fx.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerContext;
import javafx.fxml.FXML;

public class AboutController implements ControllerContext {
    private static AbstractGUI gui;

    public void initialize(AbstractGUI gui) {
        AboutController.gui = gui;

    }

    @FXML
    private void drag(){
        gui.addFrameDragAbility();
    }

    @FXML
    @Override
    public void close(){
        gui.hideWindow();
    }

}
