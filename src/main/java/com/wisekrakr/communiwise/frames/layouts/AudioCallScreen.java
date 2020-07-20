package com.wisekrakr.communiwise.frames.layouts;

import com.wisekrakr.communiwise.phone.audiovisualconnection.SoundAPI;
import com.wisekrakr.communiwise.phone.device.PhoneAPI;
import com.wisekrakr.communiwise.frames.ext.AbstractScreen;
import com.wisekrakr.communiwise.frames.layouts.objects.Button;
import org.apache.commons.lang3.time.StopWatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AudioCallScreen extends AbstractScreen {
    private final PhoneAPI phone;
    private final SoundAPI audioSound;

    public AudioCallScreen(PhoneAPI phone, SoundAPI audioSound) throws HeadlessException {
        this.phone = phone;
        this.audioSound = audioSound;


        showWindow();
    }

    public void showWindow() {
        setTitle("Call with someone");
        getContentPane().setLayout(null);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 288) / 2, (screenSize.height - 310) / 2, 500, 700);

//        showStatus();

        JLabel image = new JLabel(new ImageIcon("src/main/resources/person.png"));
        image.setBounds(10, 10, 480, 480);
        getContentPane().add(image);

        hangUpComponent();
        sendBeepSoundComponent();
        recordComponent();

        setVisible(true);
    }

    private void hangUpComponent() {
        Button hangUpButton = new Button("hang up", 10, 520, new Color(172, 15, 15));
        getContentPane().add(hangUpButton);

        hangUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                phone.hangup();
            }
        });
    }

    private void recordComponent() {
        Button recordButton = new Button("record", 300, 620, new Color(170, 159, 198));
        getContentPane().add(recordButton);

        Button stopButton = new Button("stop", 400, 620, new Color(163, 155, 180));
        getContentPane().add(stopButton);
        stopButton.setEnabled(false);

        recordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                audioSound.startRecording();

                recordButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });

        if(stopButton.isEnabled()){
            stopButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    audioSound.stopRecording();

                    recordButton.setEnabled(true);
                    stopButton.setEnabled(false);
                }
            });
        }
    }

    private void sendBeepSoundComponent(){
        Button beepButton = new Button("play sound", 210, 520, new Color(15, 135, 172));
        getContentPane().add(beepButton);

        JTextField soundFileName = new JTextField("shake_bake.wav",3);
        soundFileName.setBounds(100,520,100,20);
        getContentPane().add(soundFileName);

        beepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                audioSound.playRemoteSound("src/main/resources/" + soundFileName.getText());
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
