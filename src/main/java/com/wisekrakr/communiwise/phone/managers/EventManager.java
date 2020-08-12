package com.wisekrakr.communiwise.phone.managers;

import com.wisekrakr.communiwise.gui.ext.AbstractScreen;
import com.wisekrakr.communiwise.gui.layouts.AudioCallGUI;
import com.wisekrakr.communiwise.gui.layouts.AcceptCallGUI;
import com.wisekrakr.communiwise.gui.layouts.LoginGUI;
import com.wisekrakr.communiwise.gui.layouts.PhoneGUI;
import com.wisekrakr.communiwise.phone.device.AccountAPI;
import com.wisekrakr.communiwise.phone.device.DeviceImplementations;
import com.wisekrakr.communiwise.phone.device.PhoneAPI;
import com.wisekrakr.communiwise.phone.device.SoundAPI;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class EventManager implements FrameManagerListener{

    //todo handle in the PhoneApp

    private PhoneGUI phoneGUI;
    private LoginGUI loginGUI;

    private Map<String, AbstractScreen> callGUIs = new HashMap<>();

    private DeviceImplementations impl;

    private PhoneAPI phone;
    private AccountAPI account;
    private SoundAPI sound;

    public EventManager(DeviceImplementations impl) {
        this.impl = impl;

        phone = impl.getPhoneApi();
        account = impl.getAccountApi();
        sound = impl.getSoundApi();
    }


    @Override
    public void onOutgoingCall(String callId) {
        SwingUtilities.invokeLater(() -> {
            AudioCallGUI audioCallGUI = new AudioCallGUI(phone, sound, callId);

            callGUIs.put(callId, audioCallGUI);

            audioCallGUI.showWindow();
        });
    }

    @Override
    public void onIncomingCall(String callId) {
        SwingUtilities.invokeLater(() -> {
            AcceptCallGUI acceptCallGUI = new AcceptCallGUI(phone, callId);

            callGUIs.put(callId, acceptCallGUI);

            acceptCallGUI.showWindow();
        });
    }

    @Override
    public void onHangUp(String callId) {
        //todo this has to be fixed
        AudioCallGUI callGUI = (AudioCallGUI) callGUIs.entrySet().stream().filter(cc -> callId.equals(cc.getKey()));


        callGUI.hideWindow();
    }

    @Override
    public void onAcceptingCall(String callId) {
        SwingUtilities.invokeLater(() -> {
            AudioCallGUI audioCallGUI = new AudioCallGUI(phone, sound, callId);

            callGUIs.put(callId, audioCallGUI);

            audioCallGUI.showWindow();
        });

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

            phoneGUI = new PhoneGUI(this, phone, account);
            phoneGUI.showWindow();
        });
    }

    @Override
    public void onRegistering() {
        SwingUtilities.invokeLater(() -> {
            try {
                loginGUI = new LoginGUI(phone);
                loginGUI.showWindow();
            }catch (Throwable e){
                System.out.println("Login GUI could not be displayed " + e);

            }
        });
    }

    @Override
    public void onRegistered() {
        try{
            if(loginGUI.isActive()){
                loginGUI.hideWindow();
            }
        }catch (Throwable e){
            System.out.println("Login GUI Could not be displayed " + e);

        }
    }

    @Override
    public void onUnregistering() {

    }




}
