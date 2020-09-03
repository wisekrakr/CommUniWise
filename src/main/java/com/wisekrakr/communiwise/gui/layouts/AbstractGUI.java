package com.wisekrakr.communiwise.gui.layouts;

import com.wisekrakr.communiwise.gui.layouts.fx.ControllerContext;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractGUI extends JFrame implements FrameContext {
    private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public Dimension getScreenSize() {
        return screenSize;
    }

    @Override
    public void hideWindow() {
        this.setVisible(false);
    }

    /**
     * This sets the scene on a JFXPanel. We need this to use fxml without creating a new application with its own stage.
     * We can create a beautiful layout for the app very easily this way and we no longer have to use JComponents.
     * @param jfxPanel the JFXPanel that was created within the class. It initializes the JavaFX runtime implicitly.
     * @param fxml the fxml layout for the component class
     * @return the controller for the specific GUI
     */
    public ControllerContext getController(JFXPanel jfxPanel, String fxml){
        ControllerContext controller;

        try {
            FXMLLoader loader = new FXMLLoader(AbstractGUI.class.getResource("/fxml"+fxml));

            Parent root = loader.load();

            controller = loader.getController();

            jfxPanel.setScene(new Scene(root, getPreferredSize().getWidth(), getPreferredSize().getHeight()));
        } catch (Throwable t) {
            throw new IllegalStateException("Could not set scene for JFXPanel: " + fxml ,t);
        }

        return controller;
    }


}
