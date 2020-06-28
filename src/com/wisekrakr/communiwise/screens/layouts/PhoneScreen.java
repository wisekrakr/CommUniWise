package com.wisekrakr.communiwise.screens.layouts;


import com.wisekrakr.communiwise.config.Config;
import com.wisekrakr.communiwise.phone.Device;
import com.wisekrakr.communiwise.phone.audio.impl.AudioClip;
import com.wisekrakr.communiwise.screens.ext.AbstractScreen;
import com.wisekrakr.communiwise.screens.layouts.objects.Button;
import com.wisekrakr.communiwise.screens.layouts.panes.PhonePane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PhoneScreen extends AbstractScreen {

    private JLabel status;

    private Device device;


    public PhoneScreen(Device device)  {
        this.device = device;

        initScreen();
    }

    @Override
    public void initScreen() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        setTitle("CommUniWise Phone");
//        add(new PhonePane(device));
        setVisible(true);

        getContentPane().setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-288)/2, (screenSize.height-310)/2, 700, 300);
        setBounds((screenSize.width-288)/2, (screenSize.height-310)/2, 800, 800);

        setBackground(new Color(176,228,234));

        JLabel statusText = new JLabel("status: ");
        statusText.setBounds(650, 20, 100, 30);
        getContentPane().add(statusText);
        status = new JLabel();
        getContentPane().add(status);
        status.setBounds(700, 20, 100, 30);

        System.out.println("PhoneScreen lights up!");

        handleCallingAndAccepting();
        handlePlayingSoundClip();
        audioOptions();
    }

    private void handleCallingAndAccepting(){
        JLabel destinationLabel = new JLabel("destination: ");

        final JTextField sipTargetName;
        sipTargetName = new JTextField("253");
        final JTextField sipTargetAddress;
        sipTargetAddress = new JTextField(Config.SERVER);
        final JTextField sipTargetPort;
        sipTargetPort = new JTextField(Config.MASTER_PORT.toString());

        Button callBtn = new Button("call",10, 400);
        Button acceptBtn = new Button("accept",120, 400 );
        Button stopBtn = new Button("hang up",230, 400);

        getContentPane().add(destinationLabel);
        destinationLabel.setBounds(10, 360, 70, 30);
        getContentPane().add(sipTargetName);
        sipTargetName.setBounds(80, 360, 70, 30);
        getContentPane().add(sipTargetAddress);
        sipTargetAddress.setBounds(160, 360, 70, 30);
        getContentPane().add(sipTargetPort);
        sipTargetPort.setBounds(240, 360, 70, 30);

        getContentPane().add(callBtn);

        callBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newCall = sipTargetName.getText().trim() + "@" + sipTargetAddress.getText().trim();

                device.call("sip:"+ newCall);
                device.getSipManager().getSipProfile().setSipAddress(newCall);
                device.getSipManager().getSipProfile().setRemotePort(Integer.parseInt(sipTargetPort.getText()));
            }
        });

        getContentPane().add(acceptBtn);
        getContentPane().add(stopBtn);

        acceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                device.accept();
                System.out.println("Clicked accept");

                acceptBtn.setEnabled(false);
                stopBtn.setEnabled(true);
            }
        });
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                device.hangup();
                System.out.println("Clicked hanging up");
                stopBtn.setEnabled(false);
                acceptBtn.setEnabled(true);


            }
        });
    }

    /**
     * @see Device#onSipMessage
     */
    public void showStatus(){
        switch (device.getSipManager().getProcessedResponse().getStatusCode()){
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
                throw new IllegalStateException("Unexpected value: " + device.getSipManager().getProcessedResponse().getStatusCode());
        }
    }

    /**
     * Press a button and play a sound.
     * Todo: add choosing own sounds
     */
    private void handlePlayingSoundClip(){
        JButton playBtn = new JButton("play beep");
        playBtn.setBounds(120, 300, 100, 30);
        getContentPane().add(playBtn);

        AudioClip audioClip = new AudioClip();
        audioClip.createClipURL("audio/beep.wav");

        playBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!audioClip.getClip().isRunning()){
                    audioClip.getClip().start();
                }else{
                    audioClip.getClip().stop();
                }

            }
        });
    }


    private void audioOptions(){
//        JButton startEncodeBtn = new JButton("Start Encode");
//        getContentPane().add(startEncodeBtn);
//        startEncodeBtn.setBounds(400, 20, 50, 20);
//        startEncodeBtn.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                Global.ok = false;
//                if (device.getRtpApp() != null) {
//                    try {
//                        device.getRtpApp().releaseSocket();
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                        startEncodeBtn.setEnabled(true);
//                        return;
//                    }
//                    device.setRtpApp(null);
//
//                }
//                device.getAudioWrapper().startRecord();
//                device.getAudioWrapper().startListen(device.getRtpApp());
//                startEncodeBtn.setEnabled(true);
//                Global.ok = true;
//            }
//        });
//
//
//        JButton stopEncodeBtn = new JButton("Stop Encode");
//        getContentPane().add(stopEncodeBtn);
//        stopEncodeBtn.setBounds(460, 20, 50, 20);
//        stopEncodeBtn.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                stopEncodeBtn.setEnabled(false);
//                Global.ok = false;
//
//                device.getAudioWrapper().stopRecord();
//                device.getAudioWrapper().stopListen();
//                if (device.getRtpApp() != null) {
//                    try {
//                        device.getRtpApp() .releaseSocket();
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                        stopEncodeBtn.setEnabled(true);
//                        return;
//                    }
//                    device.setRtpApp(null);
//
//                }
//                stopEncodeBtn.setEnabled(true);
//            }
//        });
//        stopEncodeBtn.setEnabled(false);
    }


}
