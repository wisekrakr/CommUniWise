package com.wisekrakr.communiwise.phone.managers;

import com.wisekrakr.communiwise.gui.layouts.AudioCallGUI;
import com.wisekrakr.communiwise.gui.layouts.LoginGUI;
import com.wisekrakr.communiwise.gui.layouts.PhoneGUI;
import com.wisekrakr.communiwise.phone.audio.AudioManager;
import com.wisekrakr.communiwise.phone.connections.RTPConnectionManager;
import com.wisekrakr.communiwise.phone.device.DeviceImplementations;
import com.wisekrakr.communiwise.user.SipAccountManager;

import javax.swing.*;
import java.util.Map;

public class EventManager implements SipManagerListener, FrameManagerListener{

    //todo handle in the PhoneApp

    private PhoneGUI phoneGUI;
    private LoginGUI loginGUI;
    private Map<String, AudioCallGUI> callGUIs;

    private final DeviceImplementations impl;
    private final SipManager sipManager;
    private final RTPConnectionManager rtpConnectionManager;
    private final SipAccountManager accountManager;
    private final AudioManager audioManager;

    public EventManager(SipManager sipManager, RTPConnectionManager rtpConnectionManager, SipAccountManager accountManager, AudioManager audioManager) {
        this.sipManager = sipManager;
        this.rtpConnectionManager = rtpConnectionManager;
        this.accountManager = accountManager;
        this.audioManager = audioManager;

        impl = new DeviceImplementations();
    }


    @Override
    public void onOutgoingCall() {

    }

    @Override
    public void onIncomingCall() {

    }

    @Override
    public void close() {
        phoneGUI.hideWindow();
    }

    @Override
    public void open() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable e) {
                System.out.println("WARNING: unable to set look and feel, will continue");
            }

            phoneGUI = new PhoneGUI(this,
                    impl.phoneApiImpl(sipManager,rtpConnectionManager,audioManager),
                    impl.accountApiImpl(accountManager.getUserInfo()));
            phoneGUI.showWindow();
        });
    }

    @Override
    public void onRegistering() {
        SwingUtilities.invokeLater(() -> {
                if (loginGUI != null) {
                    loginGUI.showWindow();
                }
                loginGUI = new LoginGUI(impl.phoneApiImpl(sipManager,rtpConnectionManager,audioManager)).initialize();
        });
    }

    @Override
    public void onTextMessage(String message, String from) {

    }

    @Override
    public void onBye() {

    }

    @Override
    public void onRemoteCancel() {

    }

    @Override
    public void onRemoteDeclined() {

    }

    @Override
    public void callConfirmed(String rtpHost, int rtpPort, String codec) {

    }

    @Override
    public void onUnavailable() {

    }

    @Override
    public void onRinging(String from) {

    }

    @Override
    public void onBusy() {

    }

    @Override
    public void onRemoteAccepted() {

    }

    @Override
    public void onRegistered() {

    }

    @Override
    public void onHangup() {

    }

    @Override
    public void onTrying() {

    }

    @Override
    public void authenticationFailed() {

    }
}
