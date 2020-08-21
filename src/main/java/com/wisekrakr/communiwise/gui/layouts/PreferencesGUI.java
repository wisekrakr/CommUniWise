package com.wisekrakr.communiwise.gui.layouts;

import com.wisekrakr.communiwise.gui.ext.AbstractScreen;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PreferencesGUI extends AbstractScreen {

    private static final long serialVersionUID = 1L;

    private Mixer mixer;

    public PreferencesGUI() {
    }

    @Override
    public void showWindow() {


    }

    private void showAudioInputOptions(){
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
        ButtonGroup group = new ButtonGroup();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer m = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = m.getTargetLineInfo();
            if (lineInfos.length > 0 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
                JRadioButton button = new JRadioButton();
                button.setText(info.getName());
                button.setActionCommand(info.toString());
                button.addActionListener(setInput);
                buttonPanel.add(button);
                group.add(button);
            }
        }
        add(new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
    }

    private ActionListener setInput = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            for (Mixer.Info info : AudioSystem.getMixerInfo()) {
                if (arg0.getActionCommand().equals(info.toString())) {
                    Mixer newValue = AudioSystem.getMixer(info);

                    firePropertyChange("mixer", mixer, newValue);

                    mixer = newValue;
                    break;
                }
            }
        }
    };
}
