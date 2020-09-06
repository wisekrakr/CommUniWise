package com.wisekrakr.communiwise.gui.layouts.fx.call;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.user.history.CallInstance;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IncomingCallGUI extends AbstractGUI {

    private final PhoneAPI phone;
    private final CallInstance callInstance;

    private static final int DESIRED_HEIGHT = 250;
    private static final int DESIRED_WIDTH = 200;

    public IncomingCallGUI(PhoneAPI phone, CallInstance callInstance) throws HeadlessException {
        this.phone = phone;
        this.callInstance = callInstance;
    }

    @Override
    public void prepareGUI() {
        setUndecorated(true);
        setBounds(getScreenSize().width, getScreenSize().height + DESIRED_HEIGHT, DESIRED_WIDTH, DESIRED_HEIGHT);

        IncomingCallController controller = (IncomingCallController) new IncomingCallController(phone, this, callInstance).initialize("/incoming-call.fxml");
        controller.initComponents();

        add(controller, BorderLayout.CENTER);
    }

    @Override
    public void showWindow() {
        setVisible(true);

        new Timer(1, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setLocation(getScreenSize().width - DESIRED_WIDTH, getY() - 1);
                if (getY() == getScreenSize().height - DESIRED_HEIGHT) {
                    ((Timer) e.getSource()).stop();

                }
            }
        }).start();


    }



}
