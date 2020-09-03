package com.wisekrakr.communiwise.gui.layouts.fx;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class ControllerJFXPanel extends JFXPanel implements ControllerContext {

    private Parent root;

//    @Override
//    public void initializeController(String fxmlPath) {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml" + fxmlPath));
//        loader.setController(this);
//        try {
//            loader.load();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        root = loader.getRoot();
//        Platform.runLater(this::createScene);
//    }

    private void createScene() {
        Scene scene = new Scene(root);
        setScene(scene);
    }


    @Override
    public ControllerJFXPanel initialize(String fxmlPath) {
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

    @Override
    public void initComponents() {

    }
}
