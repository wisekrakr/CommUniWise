package com.wisekrakr.communiwise.screens.layouts;

import com.wisekrakr.communiwise.config.Config;
import com.wisekrakr.communiwise.main.PhoneApplication;
import com.wisekrakr.communiwise.screens.ext.AbstractScreen;
import com.wisekrakr.communiwise.screens.layouts.objects.Button;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginScreen extends AbstractScreen {
    private final PhoneApplication application;
    private JPanel panel;
    private JTextField usernameInput;
    private JPasswordField passwordInput;

    public LoginScreen(PhoneApplication application) throws HeadlessException {
        this.application = application;

        initScreen();
    }

    public void initScreen() {
//        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Login to CommUniWise");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 288) / 2, (screenSize.height - 310) / 2, 400, 150);

        panel = new JPanel();
        panel.setBackground(Config.LIGHT_CYAN);
        panel.setForeground(Color.WHITE);

        add(panel);
        panel.setLayout(null);

        authentication();
        handleRegister();

        setVisible(true);

    }

    private void authentication() {
        JLabel username = new JLabel("username");
        JLabel password = new JLabel("password");
        usernameInput = new JTextField();
        passwordInput = new JPasswordField(Config.PASSWORD);

        username.setBounds(10, 10, 80, 25);
        panel.add(username);
        password.setBounds(10, 40, 80, 25);
        panel.add(password);
        usernameInput.setBounds(100, 10, 160, 25);
        panel.add(usernameInput);
        passwordInput.setBounds(100, 40, 160, 25);
        panel.add(passwordInput);
    }

    private void handleRegister() {
        com.wisekrakr.communiwise.screens.layouts.objects.Button loginBtn = new Button("Login", 10, 80);
        panel.add(loginBtn);

        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                application.register(usernameInput.getText(), new String(passwordInput.getPassword()));
            }
        });
    }
/*
    @Override
    public void update(Graphics g) {
        super.update(g);

        if (!application.getSipManager().getSipProfile().isAuthenticated()) {
            JLabel message = new JLabel("Wrong Credentials: Unauthorized!");
            message.setBounds(95, 80, 80, 25);

            panel.add(message);
        }
    }

 */
}
