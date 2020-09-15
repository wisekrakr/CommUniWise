package com.wisekrakr.communiwise.gui.layouts;

import javafx.scene.Node;

public interface AbstractGUIContext {

    AbstractJFXPanel initialize(String fxmlPath);
    void addDraggability(AbstractGUI parentGUI, Node pane);

}
