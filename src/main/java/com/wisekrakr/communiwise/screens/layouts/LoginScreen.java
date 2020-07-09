package com.wisekrakr.communiwise.screens.layouts;

import com.wisekrakr.communiwise.config.Config;
import com.wisekrakr.communiwise.phone.device.PhoneAPI;
import com.wisekrakr.communiwise.screens.ext.AbstractScreen;
import com.wisekrakr.communiwise.screens.layouts.objects.Button;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginScreen extends AbstractScreen {
    private final PhoneAPI phoneAPI;
    private JPanel panel;
    private JTextField domainInput;
    private JTextField usernameInput;
    private JPasswordField passwordInput;
    private JTextField fromInput;
    private JTextField realmInput;

    public LoginScreen(PhoneAPI phoneAPI) throws HeadlessException {
        this.phoneAPI = phoneAPI;
    }

    private static final int DESIRED_HEIGHT = 250;
    private static final int DESIRED_WIDTH = 500;

    @Override
    public void showWindow() {
//        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Login to CommUniWise");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - DESIRED_WIDTH) / 2, (screenSize.height - DESIRED_HEIGHT) / 2, DESIRED_WIDTH, DESIRED_HEIGHT);

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
        JLabel fromAddress = new JLabel("address");
        JLabel domain = new JLabel("domain");
        JLabel realm = new JLabel("realm");
        JLabel username = new JLabel("username");
        JLabel password = new JLabel("password");
        fromInput = new JTextField("sip:damian2@asterisk.interzone <Damian 2>");
        domainInput = new JTextField("asterisk.interzone");
        realmInput = new JTextField("asterisk");
        usernameInput = new JTextField("damian2");
        passwordInput = new JPasswordField("45jf83f");

        fromAddress.setBounds(10, 10, 80, 25);
        panel.add(fromAddress);
        domain.setBounds(10, 40, 80, 25);
        panel.add(domain);
        realm.setBounds(10, 70, 80, 25);
        panel.add(realm);
        username.setBounds(10, 100, 80, 25);
        panel.add(username);
        password.setBounds(10, 130, 80, 25);
        panel.add(password);
        fromInput.setBounds(100, 10, 160, 25);
        panel.add(fromInput);
        domainInput.setBounds(100, 40, 160, 25);
        panel.add(domainInput);
        realmInput.setBounds(100, 70, 160, 25);
        panel.add(realmInput);
        usernameInput.setBounds(100, 100, 160, 25);
        panel.add(usernameInput);
        passwordInput.setBounds(100, 130, 160, 25);
        panel.add(passwordInput);
    }

    private void handleRegister() {
        com.wisekrakr.communiwise.screens.layouts.objects.Button loginBtn = new Button("Login", 10, 140);
        panel.add(loginBtn);

        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                phoneAPI.register(realmInput.getText(), domainInput.getText(), usernameInput.getText(), new String(passwordInput.getPassword()), fromInput.getText());
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
