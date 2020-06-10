package com.wisekrakr.communiwise.screen;


import com.wisekrakr.communiwise.phone.Device;
import com.wisekrakr.communiwise.phone.SipManager;
import com.wisekrakr.communiwise.phone.audio.impl.AudioClip;
import com.wisekrakr.communiwise.config.Config;
import com.wisekrakr.communiwise.utils.NotInitializedException;

import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PhoneScreen extends JFrame {

    private JLabel status;
    private JTextField usernameInput;
    private JPasswordField passwordInput;

    private JTextField sipAddress;
    private Device device;


    public PhoneScreen(Device device)  {
        this.device = device;


        initScreen();

    }


    private void initScreen() {
        status = new JLabel();

        getContentPane().setLayout(null);
        setTitle("CommUniWise");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("https://raw.githubusercontent.com/wisekrakr/portfolio_res/master/images/favicon/favicon-32x32.png").getImage());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-288)/2, (screenSize.height-310)/2, 800, 800);

        getContentPane().add(status);
        status.setBounds(700, 10, 100, 30);

        authentication();
        handleRegister();
        handleCallingAndAccepting();
        handlePlayingSoundClip();

        setVisible(true);

        System.out.println("PhoneScreen lights up!");

    }

    private void authentication(){
        JLabel username = new JLabel("username");
        JLabel password = new JLabel("password");
        usernameInput = new JTextField();
        passwordInput = new JPasswordField(Config.PASSWORD);

        getContentPane().add(username);
        username.setBounds(10, 20, 100, 30);
        getContentPane().add(password);
        password.setBounds(10, 60, 100, 30);
        getContentPane().add(usernameInput);
        usernameInput.setBounds(120, 20, 100, 30);
        getContentPane().add(passwordInput);
        passwordInput.setBounds(120, 60, 100, 30);


    }

    private void handleRegister(){
        JButton connectBtn = new JButton("connect");
        getContentPane().add(connectBtn, FlowLayout.LEFT);
        connectBtn.setBounds(10, 100, 100, 30);

        connectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                device.getSipManager().getSipProfile().setSipUserName(usernameInput.getText().trim());
                device.getSipManager().getSipProfile().setSipPassword(Config.PASSWORD);

                device.register();
            }
        });
    }

    private void handleCallingAndAccepting(){
        JLabel destination = new JLabel("destination");
        sipAddress = new JTextField();
        JButton callBtn = new JButton("call");
        JButton acceptBtn = new JButton("accept");
        JButton stopBtn = new JButton("hang up");

        getContentPane().add(destination);
        destination.setBounds(10, 300, 100, 30);
        getContentPane().add(sipAddress);
        sipAddress.setBounds(10, 360, 100, 30);

        callBtn.setBounds(10, 400, 100, 30);
        getContentPane().add(callBtn);

        callBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                device.call("sip:"+sipAddress.getText().trim());
            }
        });

        acceptBtn.setBounds(120, 400, 100, 30);
        getContentPane().add(acceptBtn);

        stopBtn.setBounds(230, 400, 100, 30);
        getContentPane().add(stopBtn);



        acceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                device.accept();
                System.out.println("Clicked accept");

//                acceptBtn.setEnabled(false);
//                stopBtn.setEnabled(true);
            }
        });
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                device.hangup();
                System.out.println("Clicked hanging up");
//                stopBtn.setEnabled(false);
//                acceptBtn.setEnabled(true);


            }
        });
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
        audioClip.createClipURL("https://file-examples.com/wp-content/uploads/2017/11/file_example_WAV_1MG.wav");

        playBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!audioClip.getClip().isActive()){
                    audioClip.getClip().start();
                }else{
                    audioClip.getClip().stop();
                }

            }
        });
    }


}
