package com.wisekrakr.communiwise.gui.layouts;

import com.wisekrakr.communiwise.gui.layouts.utils.FrameDragListener;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public abstract class AbstractGUI extends JFrame implements FrameContext {
    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public Dimension getScreenSize() {
        return screenSize;
    }

    @Override
    public void hideWindow() {
        this.setVisible(false);
    }

    @Override
    public void showWindow() {
    }


    @Override
    public void prepareGUI() {

    }

    @Override
    public void addFrameDragAbility(){
        FrameDragListener frameDragListener = new FrameDragListener(this);
        this.addMouseListener(frameDragListener);
        this.addMouseMotionListener(frameDragListener);
    }

    @Override
    public void initializeJFXPanel(JFXPanel jfxPanel, String fxml) {
        Platform.runLater(() -> initFX(jfxPanel, fxml));

    }

    /**
     * This sets the scene on a JFXPanel. We need this to use fxml without creating a new application with its own stage.
     * We can create a beautiful layout for the app very easily this way and we no longer have to use JComponents.
     * @param jfxPanel the JFXPanel that was created within the class. It initializes the JavaFX runtime implicitly.
     * @param fxml the fxml layout for the component class
     */
    private void initFX(JFXPanel jfxPanel, String fxml){
        try {
            FXMLLoader loader = new FXMLLoader();

            URL xmlUrl = AbstractGUI.class.getResource("/fxml"+fxml);
            loader.setLocation(xmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root, getPreferredSize().getWidth(), getPreferredSize().getHeight());
            jfxPanel.setScene(scene);
        } catch (Throwable t) {
            throw new IllegalStateException("Could not set scene for JFXPanel",t);
        }
    }
}
