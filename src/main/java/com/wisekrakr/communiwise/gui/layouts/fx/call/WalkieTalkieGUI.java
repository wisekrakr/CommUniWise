package com.wisekrakr.communiwise.gui.layouts.fx.call;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;

import javax.swing.*;
import java.awt.*;
import java.net.InetSocketAddress;
import java.util.Map;

public class WalkieTalkieGUI extends AbstractGUI {

    private static final int DESIRED_HEIGHT = 100;
    private static final int DESIRED_WIDTH = 400;

    private final EventManager eventManager;
    private final Map<String, String> userInfo;
    private final SoundAPI sound;
    private final String proxyName;
    private final InetSocketAddress proxyAddress;

    public WalkieTalkieGUI(EventManager eventManager, SoundAPI sound, Map<String, String> userInfo, String proxyName, InetSocketAddress proxyAddress) throws HeadlessException  {
        this.eventManager = eventManager;
        this.userInfo = userInfo;
        this.sound = sound;
        this.proxyName = proxyName;
        this.proxyAddress = proxyAddress;

        prepareGUI();
    }


    @Override
    public void prepareGUI() {
        setUndecorated(false);
        setAlwaysOnTop(true);

        setBounds(getScreenSize().width + DESIRED_WIDTH, getScreenSize().height, DESIRED_WIDTH, DESIRED_HEIGHT);
        setLocationRelativeTo(null);

        WalkieTalkieGUIController controller = (WalkieTalkieGUIController) new WalkieTalkieGUIController(this, eventManager, sound, userInfo, proxyName, proxyAddress).initialize("/walkietalkie.fxml");
        controller.initComponents();

        add(controller,BorderLayout.CENTER);
    }


    @Override
    public void showWindow() {
        setVisible(true);
    }
}
