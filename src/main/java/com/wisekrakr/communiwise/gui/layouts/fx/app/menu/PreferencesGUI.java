package com.wisekrakr.communiwise.gui.layouts.fx.app.menu;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PreferencesGUI extends AbstractGUI {

    private static final long serialVersionUID = 1L;

    private static final int DESIRED_HEIGHT = 400;
    private static final int DESIRED_WIDTH = 600;

    private final SoundAPI sound;

    public PreferencesGUI(SoundAPI sound) {
        this.sound = sound;
    }


    @Override
    public void prepareGUI() {
        setUndecorated(true);
        setPreferredSize(new Dimension(DESIRED_WIDTH,DESIRED_HEIGHT));

        PreferencesGUIController controller = (PreferencesGUIController) new PreferencesGUIController(sound, this).initialize("/preferences.fxml");
        controller.initComponents();
        add(controller, BorderLayout.CENTER);
    }

    @Override
    public void showWindow() {
        pack();
        setVisible(true);

    }

    private void showAudioInputOptions(){

        ButtonGroup group = new ButtonGroup();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer m = AudioSystem.getMixer(info);

            Line.Info[] targetLineInfo = m.getTargetLineInfo();
            if (targetLineInfo.length > 0 && targetLineInfo[0].getLineClass().equals(TargetDataLine.class)) {
                JPanel targetPanel = new JPanel();
                JRadioButton button = new JRadioButton();
                button.setText(info.getName());
                button.setActionCommand(info.toString());
                button.addActionListener(setInput);
                targetPanel.add(button);
                group.add(button);
            }

            Line.Info[] sourceLineInfo = m.getSourceLineInfo();
            if (sourceLineInfo.length > 0 && sourceLineInfo[0].getLineClass().equals(SourceDataLine.class)) {
                JPanel sourcePanel = new JPanel();
                JRadioButton button = new JRadioButton();
                button.setText(info.getName());
                button.setActionCommand(info.toString());
                button.addActionListener(setInput);
                sourcePanel.add(button);
                group.add(button);
            }

        }

    }

    private final ActionListener setInput = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            for (Mixer.Info info : AudioSystem.getMixerInfo()) {
                if (arg0.getActionCommand().equals(info.toString())) {
                    Mixer newValue = AudioSystem.getMixer(info);

                    firePropertyChange("mixer", sound.getLineManager().getMixer(), newValue);

                    sound.getLineManager().setMixer(newValue);
                    break;
                }
            }
        }
    };
}
