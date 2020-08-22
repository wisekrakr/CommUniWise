package com.wisekrakr.communiwise.gui.layouts.utils;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class TabbedPaneUtils {
    public static JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }

    /** Returns an ImageIcon, or null if the path was invalid.
     * @param path the resource path
     */
    public static ImageIcon createImageIcon(URL path) {
        if (path != null) {
            return new ImageIcon(path);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
