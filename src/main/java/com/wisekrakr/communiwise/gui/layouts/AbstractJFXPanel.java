package com.wisekrakr.communiwise.gui.layouts;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class AbstractJFXPanel extends JFXPanel implements AbstractGUIContext {

    private Parent root;

    private void createScene() {
        Scene scene = new Scene(root);
        setScene(scene);
    }

    @Override
    public void addDraggability(AbstractGUI parentGUI, Node pane) {

        pane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                parentGUI.setBounds((int)(event.getScreenX()), (int)(event.getScreenY()), parentGUI.getWidth(), parentGUI.getHeight());
            }
        });
    }

    /**
     * This sets the scene on a JFXPanel. We need this to use fxml without creating a new application with its own stage.
     * We can create a beautiful layout for the app very easily this way and we no longer have to use JComponents.
     * @param fxmlPath path to the fxml file in resources
     * @return the ControllerJFXPanel for the specific GUI
     */
    @Override
    public AbstractJFXPanel initialize(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml" + fxmlPath));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        root = loader.getRoot();
        Platform.runLater(this::createScene);
        return this;
    }



}
