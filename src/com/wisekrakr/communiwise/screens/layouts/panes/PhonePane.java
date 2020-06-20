package com.wisekrakr.communiwise.screens.layouts.panes;

import com.wisekrakr.communiwise.phone.Device;
import com.wisekrakr.communiwise.screens.layouts.panes.singles.MenuPane;
import com.wisekrakr.communiwise.screens.layouts.panes.singles.ControlsPane;
import com.wisekrakr.communiwise.screens.layouts.panes.singles.DestinationPane;

import javax.swing.*;
import java.awt.*;

public class PhonePane extends JPanel {

    private DestinationPane destinationPane;
    private ControlsPane controlsPane;
//    private SystemDatabasePane systemDatabasePane;
    private MenuPane menuPane;

    final Device device;

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
//        add((systemDatabasePane = new SystemDatabasePane()), gbc);

        gbc.gridy = 0;
        gbc.gridx++;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1;
        gbc.weightx = 0;
        add((menuPane = new MenuPane()), gbc);
    }

}
