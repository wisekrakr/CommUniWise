package com.wisekrakr.communiwise.gui.layouts.gui.call;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.layouts.gui.GUIContext;
import com.wisekrakr.communiwise.gui.layouts.gui.login.LoginController;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.phone.calling.CallInstance;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IncomingCallFXGUI extends AbstractGUI {

    private final EventManager eventManager;
    private final CallInstance callInstance;

    private JFXPanel jfxPanel;

    private static final int DESIRED_HEIGHT = 250;
    private static final int DESIRED_WIDTH = 200;

    public IncomingCallFXGUI(EventManager eventManager, CallInstance callInstance) throws HeadlessException {
        this.eventManager = eventManager;
        this.callInstance = callInstance;

        prepareGUI();
    }

    @Override
    public void prepareGUI() {
        setUndecorated(true);
        setBounds(getScreenSize().width, getScreenSize().height + DESIRED_HEIGHT, DESIRED_WIDTH, DESIRED_HEIGHT);

        jfxPanel = new JFXPanel();
        add(jfxPanel);
    }

    @Override
    public void showWindow() {
        setVisible(true);

        new Timer(1, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setLocation(0, getY() - 1);
                if (getY() == getScreenSize().height - DESIRED_HEIGHT) {
                    ((Timer) e.getSource()).stop();
                }
            }
        }).start();

        IncomingCallController controller = (IncomingCallController) new IncomingCallController().initImplementations(eventManager, this );
        controller.setCallInstance(callInstance);

        Platform.runLater(() -> GUIContext.initFX(jfxPanel, "/incoming-call.fxml", this));

    }

}
