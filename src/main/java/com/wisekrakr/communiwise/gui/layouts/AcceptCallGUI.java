package com.wisekrakr.communiwise.gui.layouts;

import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.gui.ext.AbstractScreen;
import com.wisekrakr.communiwise.gui.layouts.objects.Button;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AcceptCallGUI extends AbstractScreen {

    private final PhoneAPI phone;
    private final String callId;
    private final String rtpAddress;

    private JPanel panel;

    public AcceptCallGUI(PhoneAPI phone, String callId, String rtpAddress) throws HeadlessException {
        this.phone = phone;
        this.callId = callId;
        this.rtpAddress = rtpAddress;
    }

    public void showWindow() {
//        setLayout(new BorderLayout());
        setTitle(callId + " is calling!");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 288) / 2, (screenSize.height - 310) / 2, 400, 150);

        panel = new JPanel();
        panel.setBackground(new Color(176, 228, 234));
        panel.setForeground(Color.WHITE);

        add(panel);
        panel.setLayout(null);

        handleAccepting();
        handleDeclining();

        setVisible(true);
    }

    private void handleAccepting() {
        Button acceptBtn = new Button("Accept", 10, 80);
        panel.add(acceptBtn);

        acceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                phone.accept(rtpAddress);
            }
        });
    }

    private void handleDeclining() {
        Button declineBtn = new Button("Decline", 120, 80);
        panel.add(declineBtn);

        declineBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                phone.reject();
            }
        });
    }

}
