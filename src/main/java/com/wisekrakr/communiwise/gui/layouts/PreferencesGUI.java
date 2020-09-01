package com.wisekrakr.communiwise.gui.layouts;

import com.wisekrakr.communiwise.operations.apis.SoundAPI;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import static com.wisekrakr.communiwise.gui.layouts.utils.TabbedPaneUtils.createImageIcon;
import static com.wisekrakr.communiwise.gui.layouts.utils.TabbedPaneUtils.makeTextPanel;

public class PreferencesGUI extends AbstractGUI {

    private static final long serialVersionUID = 1L;

    private SoundAPI sound;

    private JPanel buttonPanel;

    private JComponent generalPanel;
    private JComponent audioPanel;
    private JComponent videoPanel;
    private JComponent panel4;

    public PreferencesGUI(SoundAPI sound) {
        this.sound = sound;

        prepareGUI();
    }

    @Override
    public void prepareGUI() {
        setUndecorated(true);
        setPreferredSize(new Dimension(400,400));

        buttonPanel = new JPanel(new GridLayout(0, 2));
        add(new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);


        JTabbedPane tabbedPane = new JTabbedPane();

        generalPanel = makeTextPanel("General");
        tabbedPane.addTab("General", createImageIcon(getClass().getResource("/images/preferences.png")), generalPanel, "General Options");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        audioPanel = makeTextPanel("Audio");
        tabbedPane.addTab("Audio", createImageIcon(getClass().getResource("/images/speaker.png")), showAudioInputOptions(), "Choose audio input and output devices");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        videoPanel = makeTextPanel("Video");
        tabbedPane.addTab("Video", createImageIcon(getClass().getResource("/images/video-call.png")), videoPanel, "Choose video input and output devices");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

//        panel4 = makeTextPanel(   "Panel #4 (has a preferred size of 410 x 50).");
//        panel4.setPreferredSize(new Dimension(410, 50));
//        tabbedPane.addTab("Tab 4", icon, panel4,  "Does nothing at all");
//        tabbedPane.setMnemonicAt(4, KeyEvent.VK_4);

        //Add the tabbed pane to this panel.
        add(tabbedPane);

        //The following line enables to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    @Override
    public void showWindow() {
        pack();
        setVisible(true);
    }

    private JPanel showAudioInputOptions(){

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
                buttonPanel.add(targetPanel);
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
                buttonPanel.add(sourcePanel);
            }

        }
        return buttonPanel;

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
