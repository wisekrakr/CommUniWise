package com.wisekrakr.communiwise.gui.layouts.fx.call;

import com.wisekrakr.communiwise.gui.layouts.utils.Constants;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.components.ButtonSpecial;
import com.wisekrakr.communiwise.phone.calling.CallInstance;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Label;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AudioCallGUI extends AbstractGUI {
    private static final int DESIRED_HEIGHT = 440;
    private static final int DESIRED_WIDTH = 320;

    private final PhoneAPI phone;
    private final SoundAPI sound;
    private final CallInstance callInstance;
    private JFXPanel jfxPanel;

    public AudioCallGUI(PhoneAPI phone, SoundAPI sound, CallInstance callInstance) throws HeadlessException {
        this.phone = phone;
        this.sound = sound;
        this.callInstance = callInstance;

        new AudioCallController().initialize(phone, sound, this, callInstance);

        prepareGUI();
    }

    @Override
    public void prepareGUI() {
        setUndecorated(true);
        setBounds((getScreenSize().width - DESIRED_WIDTH) / 2, (getScreenSize().height - DESIRED_HEIGHT) / 2, DESIRED_WIDTH, DESIRED_HEIGHT);


        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);
    }

    @Override
    public void showWindow() {

        setVisible(true);


        initializeJFXPanel(jfxPanel, "/audio-call.fxml");

    }


    private void hangUpComponent() {
        ButtonSpecial hangUpButtonSpecial = new ButtonSpecial("hang up", 10, 520, new Color(172, 15, 15));
        getContentPane().add(hangUpButtonSpecial);

        hangUpButtonSpecial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                phone.hangup(callInstance.getId());
            }
        });
    }

    private void recordComponent() {
        ButtonSpecial recordButtonSpecial = new ButtonSpecial("record", 300, 620, new Color(170, 159, 198));
        getContentPane().add(recordButtonSpecial);

        ButtonSpecial stopButtonSpecial = new ButtonSpecial("stop", 400, 620, new Color(163, 155, 180));
        getContentPane().add(stopButtonSpecial);
        stopButtonSpecial.setEnabled(false);

        recordButtonSpecial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sound.startRecording();

                recordButtonSpecial.setEnabled(false);
                stopButtonSpecial.setEnabled(true);
            }
        });

        if(stopButtonSpecial.isEnabled()){
            stopButtonSpecial.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sound.stopRecording();

                    recordButtonSpecial.setEnabled(true);
                    stopButtonSpecial.setEnabled(false);
                }
            });
        }
    }

    private void sendBeepSoundComponent(){
        ButtonSpecial beepButtonSpecial = new ButtonSpecial("play sound", 210, 520, new Color(15, 135, 172));
        getContentPane().add(beepButtonSpecial);

        JTextField soundFileName = new JTextField("sounds/shake_bake.wav",3);
        soundFileName.setBounds(100,520,100,20);
        getContentPane().add(soundFileName);

        beepButtonSpecial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sound.playRemoteSound("src/main/resources/sounds/" + soundFileName.getText());
            }
        });
    }



}
