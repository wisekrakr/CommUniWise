package com.wisekrakr.communiwise.screens.layouts.panes.main;

import com.wisekrakr.communiwise.phone.Device;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControlsPane extends JPanel {

    private JButton message, audioCall, videoCall, unregister;
    private JLabel controls;

    final private Device device;

    public ControlsPane(Device device) {
        this.device = device;
        setLayout(new GridBagLayout());
        setBorder(new CompoundBorder(new TitledBorder("Controls"), new EmptyBorder(12, 0, 0, 0)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 4);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Make a connection: "), gbc);
        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add((controls = new JLabel()), gbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        add(panel, gbc);

        gbc.gridwidth = 1;
        gbc.weightx = 0.25;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add((message = new JButton("Message")), gbc);
        gbc.gridx++;
        add((audioCall = new JButton("Audio Call")), gbc);
        gbc.gridx++;
        add((videoCall = new JButton("Video Call")), gbc);
        gbc.gridx++;
        add((unregister = new JButton("Unregister")), gbc);

        makeAudioCall();
    }

    void makeAudioCall(){
        audioCall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newCall = DestinationPane.getSipTargetName().trim() + "@" + DestinationPane.getSipTargetAddress().trim();
                device.call("sip:"+ newCall);

                device.getSipManager().getSipProfile().setSipAddress(newCall);
                device.getSipManager().getSipProfile().setRemotePort(Integer.parseInt(DestinationPane.getSipTargetPort()));
            }
        });
    }

}
