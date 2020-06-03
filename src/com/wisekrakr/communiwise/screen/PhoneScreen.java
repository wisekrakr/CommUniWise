package com.wisekrakr.communiwise.screen;


import com.wisekrakr.communiwise.SipManager;
import com.wisekrakr.communiwise.SoundManager;
import com.wisekrakr.communiwise.config.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PhoneScreen extends JFrame {
    private SipManager sipManager;

    private JLabel status;
    private JTextField usernameInput;
    private JPasswordField passwordInput;

    private JTextField sipAddress;

    private SoundManager soundManager;


    public PhoneScreen(SipManager sipManager)  {
        this.sipManager = sipManager;

        initScreen();

        soundManager = new SoundManager();
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
                sipManager.getSipProfile().setSipUserName(usernameInput.getText().trim());
                sipManager.getSipProfile().setSipPassword(Config.PASSWORD);

                sipManager.registering();
            }
        });
    }

    private void handleCallingAndAccepting(){
        JLabel destination = new JLabel("destination");
        sipAddress = new JTextField();
        JButton callBtn = new JButton("call");
        JButton acceptBtn = new JButton("record");
        JButton stopBtn = new JButton("stop");

        getContentPane().add(destination);
        destination.setBounds(10, 300, 100, 30);
        getContentPane().add(sipAddress);
        sipAddress.setBounds(10, 360, 100, 30);

        callBtn.setBounds(10, 400, 100, 30);
        getContentPane().add(callBtn);

        callBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sipManager.calling("sip:"+sipAddress.getText().trim(), Config.LOCAL_RTP_PORT);
            }
        });

        acceptBtn.setBounds(120, 400, 100, 30);
        getContentPane().add(acceptBtn);

        stopBtn.setBounds(230, 400, 100, 30);
        getContentPane().add(stopBtn);

        acceptBtn.setEnabled(true);
        stopBtn.setEnabled(false);


        acceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                sipManager.acceptingCall(6070);
                if(!soundManager.isServingInput()){
                    soundManager.startClient();
                    acceptBtn.setEnabled(false);
                    stopBtn.setEnabled(true);

                }
            }
        });
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                sipManager.acceptingCall(6070);
                if(soundManager.isServingInput()){
                    soundManager.stopClient();
                    stopBtn.setEnabled(false);
                    acceptBtn.setEnabled(true);

                }
            }
        });
    }



}
