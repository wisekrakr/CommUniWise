package com.wisekrakr.communiwise.screens.layouts.panes.options;

import com.wisekrakr.communiwise.phone.Device;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class OptionsPane extends JPanel {
    private final Device device;

    public OptionsPane(Device device) {
//        super(new GridLayout(1, 1));
        this.device = device;

        JTabbedPane tabbedPane = new JTabbedPane();

        JComponent general = makeTextPanel("General");
        tabbedPane.addTab("General", null, general,
                "General Options");

        JComponent audio = audioTab();
        tabbedPane.addTab("Audio", null, audio,
                "Audio Options");

        JComponent video = makeTextPanel("Video");
        tabbedPane.addTab("Video", null, video,
                "Video options");

        add(tabbedPane);

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public void init() {
        JFrame frame = new JFrame("Please choose a mixer.");

        frame.add(new OptionsPane(this.device), BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    protected JComponent audioTab() {
        JPanel panel = new JPanel(new GridLayout(0, 2));

        JPanel outputPanel = new JPanel(new GridLayout(0, 1));
        JPanel inputPanel = new JPanel(new GridLayout(0, 1));

        outputPanel.setBorder(BorderFactory.createTitledBorder("Please choose your output mixer."));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Please choose your input mixer."));

        java.util.List<String> outputNames = device.getAVConnectionStream().getMixerNames();
        java.util.List<String> inputNames = device.getAVConnectionStream().getMixerNames();

        JScrollPane scrollPane1 = new JScrollPane(outputPanel);
        JScrollPane scrollPane2 = new JScrollPane(inputPanel);

        panel.add(scrollPane1);
        panel.add(scrollPane2);

        for (JRadioButton jrb : mixerList((ArrayList<String>) outputNames, outputPanel)) {
            jrb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        System.out.println("    Selected output Mixer: " + jrb.getText().trim());

                        device.getAVConnectionStream().selectAudioOutput(jrb.getText().trim());

                    } catch (LineUnavailableException lineUnavailableException) {
                        lineUnavailableException.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(outputPanel, "You chose " + jrb.getText());
                }
            });

        }

        for (JRadioButton jrb : mixerList((ArrayList<String>) inputNames, inputPanel)) {
            jrb.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        System.out.println("    Selected input Mixer: " + jrb.getText().trim());

                        device.getAVConnectionStream().selectAudioInput(jrb.getText().trim());

                    } catch (LineUnavailableException lineUnavailableException) {
                        lineUnavailableException.printStackTrace();
                    }
                    JOptionPane.showMessageDialog(outputPanel, "You chose " + jrb.getText());
                }
            });
        }



        return panel;
    }

    /**
     * Returns a list of mixer types as radio buttons to choose from
     * @return ArrayList of radio buttons
     */
    private ArrayList<JRadioButton> mixerList(ArrayList<String>list , JPanel panel){
        ArrayList<JRadioButton> buttonList = new ArrayList<>();
        ButtonGroup bg = new ButtonGroup();
        for (String name : list) {
            JRadioButton jrb = new JRadioButton(name);
            buttonList.add(jrb);
            bg.add(jrb);
            panel.add(jrb);
        }
        return buttonList;
    }

    private JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }
}
