package com.wisekrakr.communiwise.screens.layouts;

import com.wisekrakr.communiwise.config.Config;
import com.wisekrakr.communiwise.phone.Device;
import com.wisekrakr.communiwise.screens.ext.AbstractScreen;
import com.wisekrakr.communiwise.screens.layouts.objects.Button;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IncomingCallScreen extends AbstractScreen {

    private final Device device;

    private JPanel panel;
    private JTextField usernameInput;
    private JPasswordField passwordInput;
    private boolean loginClick = false;


    public IncomingCallScreen(Device device) throws HeadlessException {
        this.device = device;

        initScreen();
    }

    @Override
    public void initScreen() {
//        setLayout(new BorderLayout());
        setTitle("Login to CommUniWise");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-288)/2, (screenSize.height-310)/2, 400, 150);

        panel = new JPanel();
        panel.setBackground(new Color(176,228,234));
        panel.setForeground(Color.WHITE);

        add(panel);
        panel.setLayout(null);

        handleAccepting();
        handleDeclining();

        setVisible(true);
    }

    private void handleAccepting(){
        Button acceptBtn = new Button("Accept", 10, 80);
        panel.add(acceptBtn);

        acceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                device.accept();

            }
        });
    }

    private void handleDeclining(){
        Button declineBtn = new Button("Decline", 120, 80);
        panel.add(declineBtn);

        declineBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                device.reject();

            }
        });
    }

}
