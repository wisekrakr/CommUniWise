package com.wisekrakr.communiwise.gui.layouts.fx.call;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.components.ButtonSpecial;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;
import com.wisekrakr.communiwise.phone.calling.CallInstance;
import javafx.embed.swing.JFXPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AudioCallGUI extends AbstractGUI {
    private static final int DESIRED_HEIGHT = 440;
    private static final int DESIRED_WIDTH = 270;

    private final EventManager eventManager;
    private final PhoneAPI phone;
    private final SoundAPI sound;
    private final CallInstance callInstance;

    public AudioCallGUI(EventManager eventManager, PhoneAPI phone, SoundAPI sound, CallInstance callInstance) throws HeadlessException {
        this.eventManager = eventManager;
        this.phone = phone;
        this.sound = sound;
        this.callInstance = callInstance;
    }

    @Override
    public void prepareGUI() {
        setUndecorated(true);
        setBounds((getScreenSize().width - DESIRED_WIDTH) / 2, (getScreenSize().height - DESIRED_HEIGHT) / 2, DESIRED_WIDTH, DESIRED_HEIGHT);

        AudioCallController controller = (AudioCallController) new AudioCallController(eventManager, phone, sound, this, callInstance).initialize("/audio-call.fxml");

        controller.initComponents();

        add(controller, BorderLayout.CENTER);
    }

    @Override
    public void showWindow() {
        setVisible(true);
    }

}
