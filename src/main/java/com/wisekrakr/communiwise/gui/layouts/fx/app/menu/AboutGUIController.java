package com.wisekrakr.communiwise.gui.layouts.fx.app.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.AbstractJFXPanel;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class AboutGUIController extends AbstractJFXPanel implements ControllerContext {
    private final AbstractGUI gui;

    @FXML private AnchorPane container;

    public AboutGUIController(AbstractGUI gui) {
        this.gui = gui;
    }

    @Override
    public void initComponents() {

    }

    @FXML
    @Override
    public void drag() {
        addDraggability(gui, container);
    }

    @FXML
    @Override
    public void close() {
        gui.hideWindow();
    }
}
