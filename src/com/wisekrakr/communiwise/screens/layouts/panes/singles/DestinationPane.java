package com.wisekrakr.communiwise.screens.layouts.panes.singles;

import com.wisekrakr.communiwise.config.Config;
import com.wisekrakr.communiwise.phone.Device;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DestinationPane extends JPanel {
    static JTextField sipTargetName;
    static JTextField sipTargetAddress;
    static JTextField sipTargetPort;

    final Device device;

    public DestinationPane(Device device) {
        this.device = device;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        add(new JLabel("Contact Name (or Number): "), gbc);
        gbc.gridy++;
        add(new JLabel("Contact Address (ip or server): "), gbc);
        gbc.gridy++;
        add(new JLabel("Contact Port (5060 or 5061): "), gbc);

        gbc.gridx++;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        add((sipTargetName = new JTextField(3)), gbc);
        gbc.gridy++;
        add((sipTargetAddress = new JTextField(Config.SERVER,3)), gbc);
        gbc.gridy++;
        add((sipTargetPort = new JTextField(Integer.toString(Config.MASTER_PORT),3)), gbc);

    }


    protected static String getSipTargetName() {
        return sipTargetName.getText().trim();
    }
    protected static String getSipTargetAddress() {
        return sipTargetAddress.getText().trim();
    }
    protected static String getSipTargetPort() {
        return sipTargetPort.getText().trim();
    }
}