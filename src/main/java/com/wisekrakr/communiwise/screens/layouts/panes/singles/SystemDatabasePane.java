package com.wisekrakr.communiwise.screens.layouts.panes.singles;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SystemDatabasePane extends JPanel {

    private JRadioButton none, database;
    private JLabel databaseLabel;
    private JButton systemDatabase;

    public SystemDatabasePane() {
        setLayout(new GridBagLayout());
        setBorder(new CompoundBorder(new TitledBorder("System Database"), new EmptyBorder(8, 0, 0, 0)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 4);
        gbc.anchor = GridBagConstraints.WEST;

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add((none = new JRadioButton("None")), gbc);
        gbc.gridy++;
        panel.add((none = new JRadioButton("Database: ")), gbc);

        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add((databaseLabel = new JLabel("")), gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        add(panel, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add((systemDatabase = new JButton("System Database...")), gbc);
        systemDatabase.setEnabled(false);
    }

}
