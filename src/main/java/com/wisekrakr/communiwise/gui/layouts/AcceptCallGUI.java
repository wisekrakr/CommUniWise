package com.wisekrakr.communiwise.gui.layouts;

import com.wisekrakr.communiwise.gui.layouts.utils.Constants;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.gui.ext.AbstractGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AcceptCallGUI extends AbstractGUI {

    private final PhoneAPI phone;
    private final String callId;
    private final String displayName;
    private final String rtpAddress;

    private JPanel controlPanel;
    private JLabel headerLabel;

    private static final int DESIRED_HEIGHT = 200;
    private static final int DESIRED_WIDTH = 250;

    public AcceptCallGUI(PhoneAPI phone, String callId, String displayName, String rtpAddress) throws HeadlessException {
        this.phone = phone;
        this.callId = callId;
        this.displayName = displayName;
        this.rtpAddress = rtpAddress;

        prepareGUI();
    }

    @Override
    public void prepareGUI() {
        setUndecorated(true);
        setBounds(getScreenSize().width, getScreenSize().height + DESIRED_HEIGHT, DESIRED_WIDTH, DESIRED_HEIGHT);
        setLayout(new GridLayout(2, 1));

        addFrameDragAbility();

        headerLabel = new JLabel("<html><p>" + displayName + " is calling!</p></html>", JLabel.CENTER);

        controlPanel = new JPanel();
//        controlPanel.setLayout(new BorderLayout());
        controlPanel.setBackground(Constants.LIGHT_CYAN);


        add(headerLabel);
        add(controlPanel);
    }

    @Override
    public void showWindow() {

        JButton acceptBtn = new JButton("Accept");
        controlPanel.add(acceptBtn);

        acceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                phone.accept(rtpAddress);
            }
        });

        JButton declineBtn = new JButton("Decline");
        controlPanel.add(declineBtn);

        declineBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                phone.reject();
            }
        });

        new Timer(1, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setLocation(0, getY() - 1);
                if (getY() == getScreenSize().height - DESIRED_HEIGHT) {
                    ((Timer) e.getSource()).stop();
                }
            }
        }).start();

        setVisible(true);
    }


}
