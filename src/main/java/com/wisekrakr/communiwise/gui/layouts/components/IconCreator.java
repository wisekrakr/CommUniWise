package com.wisekrakr.communiwise.gui.layouts.components;

import com.wisekrakr.communiwise.gui.layouts.fx.app.PhoneGUI;
import javafx.scene.image.ImageView;

public class IconCreator {
    /**
     * Creates an ImageView, either for a button or not.
     *
     * @param path the URL path to a resources file
     * @param width preferred width for the icon
     * @param height preferred height of the icon
     * @return a new ImageView
     */
    public static ImageView addIconForButton(String path, int width, int height){
        ImageView image;

        try {
            image = new ImageView(String.valueOf(PhoneGUI.class.getResource(path)));

            image.setFitWidth(width);
            image.setFitHeight(height);

            return image;


        }catch (Throwable t){
            throw new IllegalArgumentException("Could not find path to image " + path,t);
        }
    }

}
