package com.wisekrakr.communiwise.gui;

import com.wisekrakr.communiwise.gui.ext.AbstractScreen;
import com.wisekrakr.communiwise.gui.layouts.*;
import com.wisekrakr.communiwise.gui.layouts.background.AlertFrame;
import com.wisekrakr.communiwise.operations.DeviceImplementations;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EventManager implements FrameManagerListener {

    private PhoneGUI phoneGUI;
    private LoginGUI loginGUI;
    private ContactsGUI contactsGUI;

    private final Map<String, AbstractScreen> callGUIs = new HashMap<>();

    private final PhoneAPI phone;
    private final AccountAPI account;
    private final SoundAPI sound;

    public EventManager(DeviceImplementations impl) {

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
        for(AbstractScreen s: callGUIs.entrySet().stream().filter(cc -> callId.equals(cc.getKey())).map(Map.Entry::getValue).collect(Collectors.toList())){
            s.hideWindow();
        }

        if (phoneGUI != null){
            phoneGUI.showWindow();
        }
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
//        try {
//            account.getContactManager().loadPhoneBook(account.getUserInfo().get("username"));
//        }catch (Throwable e){
//            throw new IllegalArgumentException("Phonebook could not be loaded", e);
//        }

        try{
            if(loginGUI.isActive()){
                loginGUI.hideWindow();
            }
        }catch (Throwable e){
            System.out.println("Login GUI Could not be displayed " + e);
        }
    }

    @Override
    public void onAuthenticationFailed() {
        try{
            if(loginGUI.isActive()){
                loginGUI.showErrorStatus();
            }
        }catch (Throwable e){
            System.out.println("Login GUI Could not be displayed " + e);

        }
    }

    @Override
    public void onUnregistering() {

    }

    @Override
    public void menuContactListOpen() {
        SwingUtilities.invokeLater(() -> {
            try {
                if(account.getUserInfo().size() > 0){
                    contactsGUI = new ContactsGUI(account);
                    contactsGUI.showWindow();
                }else{
                    new AlertFrame().showAlert("You have to be logged in to see your contacts", JOptionPane.INFORMATION_MESSAGE);
                }


            }catch (Throwable e){
                System.out.println("Contact list GUI could not be displayed " + e);

            }
        });
    }

    @Override
    public void menuPreferencesOpen() {

    }
}
