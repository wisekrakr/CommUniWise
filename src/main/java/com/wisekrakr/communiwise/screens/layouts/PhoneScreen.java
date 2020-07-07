package com.wisekrakr.communiwise.screens.layouts;


import com.wisekrakr.communiwise.config.Config;
import com.wisekrakr.communiwise.main.PhoneApplication;
import com.wisekrakr.communiwise.screens.ext.AbstractScreen;
import com.wisekrakr.communiwise.screens.layouts.objects.Button;
import com.wisekrakr.communiwise.screens.layouts.panes.PhonePane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PhoneScreen extends AbstractScreen {

    private final PhoneApplication application;

    public PhoneScreen(PhoneApplication application)  {
        this.application = application;

        initScreen();
    }

    public void initScreen() {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        setTitle("CommUniWise Phone");
        add(new PhonePane(application));
        setVisible(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-288)/2, (screenSize.height-310)/2, 700, 300);
//        setBounds((screenSize.width-288)/2, (screenSize.height-310)/2, 800, 800);

        System.out.println("PhoneScreen lights up!");
    }

    @Deprecated
    private void handleCallingAndAccepting(){
        JLabel destinationLabel = new JLabel("destination: ");

        final JTextField sipTargetName;
        sipTargetName = new JTextField("253");
        final JTextField sipTargetAddress;
        sipTargetAddress = new JTextField(Config.SERVER);
        final JTextField sipTargetPort;
        sipTargetPort = new JTextField(Config.MASTER_PORT.toString());

        Button callButton = new Button("call",10, 400);
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

        getContentPane().add(callButton);

        callButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                application.initiateCall("sip:"+ (sipTargetName.getText().trim() + "@" + sipTargetAddress.getText().trim()),
                        Integer.parseInt(sipTargetPort.getText()));
            }
        });

        getContentPane().add(acceptBtn);
        getContentPane().add(stopBtn);

        acceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                application.accept();
                System.out.println("Clicked accept");

                acceptBtn.setEnabled(false);
                stopBtn.setEnabled(true);
            }
        });
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                application.hangup();
                System.out.println("Clicked hanging up");
                stopBtn.setEnabled(false);
                acceptBtn.setEnabled(true);


            }
        });
    }



    /**
     * Press a button and play a sound.
     * Todo: add choosing own sounds
    @Deprecated
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


     */



}
