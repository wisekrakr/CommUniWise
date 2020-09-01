package com.wisekrakr.communiwise.gui.layouts.components;

import com.wisekrakr.communiwise.gui.layouts.PhoneGUI;
import javafx.scene.image.ImageView;

import javax.swing.*;
import java.awt.*;

public class ImageCreator {
    /**
     * Creates an ImageIcon, either for a button or not.
     *
     * @param path the URL path to a resources file
     * @param buttonIcon if the image icon is for a button
     *
     * @return a new ImageIcon either with width 0f 20 and height of 20 or its original size.
     */
    public static ImageIcon addImageIcon(String path, boolean buttonIcon){
        Image resizedImage;
        ImageIcon image;

        try {
            image = new ImageIcon(PhoneGUI.class.getResource(path));

            if(!buttonIcon){
                return image;
            }else {
                resizedImage = image.getImage().getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH);

                return new ImageIcon(resizedImage);
            }


        }catch (Throwable t){
            throw new IllegalArgumentException("Could not find path to image " + path,t);
        }
    }

}
