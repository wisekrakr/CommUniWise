package com.wisekrakr.communiwise.gui.layouts.gui.call;

import com.wisekrakr.communiwise.operations.apis.SoundAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.components.ButtonSpecial;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AudioCallGUI extends AbstractGUI {
    private final PhoneAPI phone;
    private final SoundAPI sound;
    private String callId;

    public AudioCallGUI(PhoneAPI phone, SoundAPI sound, String callId) throws HeadlessException {
        this.phone = phone;
        this.sound = sound;
        this.callId = callId;

        prepareGUI();
    }

    @Override
    public void prepareGUI() {
        setTitle("Call with someone");
        getContentPane().setLayout(null);
        setBounds((getScreenSize().width - 288) / 2, (getScreenSize().height - 310) / 2, 500, 700);

//        showStatus();
        addFrameDragAbility();
    }

    @Override
    public void showWindow() {

        JLabel image = new JLabel(new ImageIcon("src/main/resources/images/person.png"));
        image.setBounds(10, 10, 480, 480);
        getContentPane().add(image);

        hangUpComponent();
        sendBeepSoundComponent();
        recordComponent();

        setVisible(true);
    }

    private void hangUpComponent() {
        ButtonSpecial hangUpButtonSpecial = new ButtonSpecial("hang up", 10, 520, new Color(172, 15, 15));
        getContentPane().add(hangUpButtonSpecial);

        hangUpButtonSpecial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                phone.hangup(callId);
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


    public void showStatus() {
        JLabel status = new JLabel();
        status.setBounds(20, 620, 150, 30);
        add(status);

        switch (phone.callStatus()){
            case 603:
                status.setText("Decline");
                status.setForeground(Color.ORANGE);
                break;
            case 486:
                status.setText("Busy");
                status.setForeground(Color.ORANGE);

                break;
            case 408:
                status.setText("Request Timeout");
                status.setForeground(Color.RED);

                break;
            case 403:
                status.setText("Forbidden");
                status.setForeground(Color.RED);

                break;
            case 401:
                status.setText("Unauthorized");
                status.setForeground(Color.RED);

                break;
            case 400:
                status.setText("Bad Request");
                status.setForeground(Color.RED);

                break;
            case 200:
                status.setText("OK");
                status.setForeground(Color.GREEN);

                break;
            case 100:
                status.setText("Trying");
                status.setForeground(Color.ORANGE);

                break;
            case 180:
                status.setText("Ringing");
                status.setForeground(Color.BLUE);

                break;
            case 183:
                status.setText("Session Progress");
                status.setForeground(Color.YELLOW);

                break;
            default:
                throw new IllegalStateException("Unexpected value: " + phone.callStatus());
        }



    }
}
