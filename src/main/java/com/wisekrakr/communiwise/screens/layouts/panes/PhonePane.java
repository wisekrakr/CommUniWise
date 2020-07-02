package com.wisekrakr.communiwise.screens.layouts.panes;

import com.wisekrakr.communiwise.config.Config;
import com.wisekrakr.communiwise.phone.Device;
import com.wisekrakr.communiwise.screens.layouts.panes.background.GradientPanel;
import com.wisekrakr.communiwise.screens.layouts.panes.main.MenuPane;
import com.wisekrakr.communiwise.screens.layouts.panes.main.ControlsPane;
import com.wisekrakr.communiwise.screens.layouts.panes.main.DestinationPane;

import javax.swing.*;
import java.awt.*;

public class PhonePane extends GradientPanel {

    private DestinationPane destinationPane;
    private ControlsPane controlsPane;
    private MenuPane menuPane;

    private final Device device;

    public PhonePane(Device device) {
        this.device = device;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.33;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 4, 4, 4);

        add((destinationPane = new DestinationPane(device)), gbc);
        gbc.gridy++;
        add((controlsPane = new ControlsPane(device)), gbc);
        gbc.gridy++;

        gbc.gridy = 0;
        gbc.gridx++;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1;
        gbc.weightx = 0;
        add((menuPane = new MenuPane(device)), gbc);
    }

//    @Override
//    public void paint(Graphics g) {
//        super.paint(g);
//
//        Graphics2D g2 = (Graphics2D) g.create();
//
//        int w = this.getWidth();
//        int h = this.getHeight();
//
//        g2.setComposite(AlphaComposite.getInstance(
//                AlphaComposite.SRC_OVER, .2f));
//        g2.setPaint(new GradientPaint(0, 0, Config.LIGHT_CYAN, 0, h, Config.DARK_CYAN));
//
//        g2.fillRect(0, 0, w, h);
//
//        g2.dispose();
//    }
}
