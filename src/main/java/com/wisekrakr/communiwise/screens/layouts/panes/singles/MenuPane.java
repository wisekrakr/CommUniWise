package com.wisekrakr.communiwise.screens.layouts.panes.singles;

import javax.swing.*;
import java.awt.*;

public class MenuPane extends JPanel {

    private JButton okay, cancel, help, advanced, options;

    public MenuPane() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(4, 4, 4, 4);

        add((okay = new JButton("Ok")), gbc);
        gbc.gridy++;
        add((cancel = new JButton("Cancel")), gbc);
        gbc.gridy++;
        add((help = new JButton("Help")), gbc);
        gbc.gridy++;
        add((advanced = new JButton("Advanced")), gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.SOUTH;
        add((options = new JButton("Options >>")), gbc);
    }

}
