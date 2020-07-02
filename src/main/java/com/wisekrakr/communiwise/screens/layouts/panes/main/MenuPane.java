package com.wisekrakr.communiwise.screens.layouts.panes.main;

import com.wisekrakr.communiwise.phone.Device;
import com.wisekrakr.communiwise.screens.layouts.panes.options.OptionsPane;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class MenuPane extends JPanel {

    private JButton okay, cancel, help, advanced, options;

    private final Device device;

    public MenuPane(Device device) {
        this.device = device;

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

        clickOptions();
    }

    private void clickOptions(){
        options.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionsPane optionsPane = new OptionsPane(device);
                optionsPane.init();
            }
        });
    }

    private void audioOptions(){
        JFrame chooseMixerFrame = new JFrame("Sound Mixer");

        java.util.List<String> names = device.getAVConnectionStream().getMixerNames();

        JPanel mainPanel = new JPanel(new GridLayout(2, 0));
        JPanel teamPanel = new JPanel(new GridLayout(0, 1));

        List<JRadioButton> list = new ArrayList<>();
        ButtonGroup bg = new ButtonGroup();
        for (String name : names) {
            JRadioButton jrb = new JRadioButton(name);
            list.add(jrb);
            bg.add(jrb);
            teamPanel.add(jrb);
        }
        teamPanel.setBorder(BorderFactory.createTitledBorder("Please choose a mixer."));
        mainPanel.add(new JScrollPane(teamPanel));
        JPanel okPanel = new JPanel(new GridBagLayout());

        JButton okButton = new JButton(new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (JRadioButton jrb : list) {
                    if (jrb.isSelected()) {
                        try {
                            System.out.println("    Selected Mixer: " + jrb.getText().trim());
                            device.getAVConnectionStream().selectAudioOutput(jrb.getText().trim());
                        } catch (LineUnavailableException lineUnavailableException) {
                            lineUnavailableException.printStackTrace();
                        }
                        JOptionPane.showMessageDialog(chooseMixerFrame, "You chose " + jrb.getText());
                    }
                }
            }
        });
        okButton.setFont(okButton.getFont().deriveFont(36f));
        okPanel.add(okButton);
        mainPanel.add(okPanel);
        chooseMixerFrame.add(mainPanel);
        chooseMixerFrame.pack();
        chooseMixerFrame.setSize(400, list.get(0).getHeight() * 16);
        chooseMixerFrame.setLocationRelativeTo(null);
        chooseMixerFrame.setVisible(true);
    }

}
