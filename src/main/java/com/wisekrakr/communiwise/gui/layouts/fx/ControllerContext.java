package com.wisekrakr.communiwise.gui.layouts.fx;


/**
 * Methods for a controller.
 */
public interface ControllerContext {
    /**
     * All JavaFX components that needs to initialize immediately at runtime need to be initialized in this method
     */
    void initComponents();

    /**
     * A FXML method that needs to be set in the fxml file and in the controller class
     */
    void close();

    /**
     * A FXML method that needs to be set in the fxml file and in the controller class.
     * Set "drag" at the node that will get dragged. Put it on Mouse Dragged.
     */
    void drag();
}