package com.wisekrakr.communiwise.gui.layouts.gui;

import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.net.URL;


public class GUIContext {
    /**
     * This sets the scene on a JFXPanel. We need this to use fxml without creating a new application with its own stage.
     * We can create a beautiful layout for the app very easily this way and we no longer have to use JComponents.
     * @param jfxPanel the JFXPanel that was created within the class. It initializes the JavaFX runtime implicitly.
     * @param fxml the fxml layout for the component class
     * @param gui the gui that created a new JFXPanel
     */
    public static void initFX(JFXPanel jfxPanel, String fxml, AbstractGUI gui){
        try {
            FXMLLoader loader = new FXMLLoader();

            URL xmlUrl = GUIContext.class.getResource("/fxml"+fxml);
            loader.setLocation(xmlUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root, gui.getPreferredSize().getWidth(), gui.getPreferredSize().getHeight());
            jfxPanel.setScene(scene);
        } catch (Throwable t) {
            throw new IllegalStateException("Could not set scene for JFXPanel",t);
        }
    }
}
