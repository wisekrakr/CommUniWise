package com.wisekrakr.communiwise.gui;

import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.*;
import com.wisekrakr.communiwise.gui.layouts.background.AlertFrame;
import com.wisekrakr.communiwise.operations.DeviceImplementations;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;

import javax.sip.address.Address;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class will hold all GUI's and opens and closes them when the user call for it. It also handles showing error screens/frames when it is needed.
 *
 * The EventManager is used in in the SipManagerListener for the most part, to keep everything in one place.
 * However, it also is used in some GUI's for easy access from one GUI to another.
 *
 * We also keep track of multiple Call GUI's, if there are multiple calls, so that when we want to close a call screen/frame, we close the right one, bases of the call-id
 *
 * We also initialize the phone, account and sound api here, so that we can pass them to the screen/frame that will need it and we wont have to keep initializing them all over the app.
 *
 */

public class EventManager implements FrameManagerListener {

    private PhoneGUI phoneGUI;
    private LoginGUI loginGUI;
    private ContactsGUI contactsGUI;
    private PreferencesGUI preferencesGUI;

    //todo instead of String(callId) use a CallInstance
    private final Map<String, AbstractGUI> callGUIs = new HashMap<>();

    private final PhoneAPI phone;
    private final AccountAPI account;
    private final SoundAPI sound;

    /**
     *
     * @param impl {@link DeviceImplementations} for a phone device. The basic functions of a phone will be handled here.
     */
    public EventManager(DeviceImplementations impl) {

        phone = impl.getPhoneApi();
        account = impl.getAccountApi();
        sound = impl.getSoundApi();
    }

    /**
     * When the user makes a call a AudioCallGUI will show.
     * @param callId the id of the call
     */
    @Override
    public void onOutgoingCall(String callId) {
        SwingUtilities.invokeLater(() -> {
            AudioCallGUI audioCallGUI = new AudioCallGUI(phone, sound, callId);

            callGUIs.put(callId, audioCallGUI);

            audioCallGUI.showWindow();
        });
    }

    /**
     * When the user get an incoming call. A new {@link AcceptCallGUI}gets created and put into a HashMap of  {@link AbstractGUI}
     * @param callId the id of the call
     * @param displayName the username of the caller
     * @param rtpAddress the rtp address of the caller (name and domain)
     * @param rtpPort the rtp port of the caller
     */
    @Override
    public void onIncomingCall(String callId, String displayName, String rtpAddress, int rtpPort) {
        SwingUtilities.invokeLater(() -> {
            AcceptCallGUI acceptCallGUI = new AcceptCallGUI(phone, callId, displayName, rtpAddress);

            callGUIs.put(callId, acceptCallGUI);

            acceptCallGUI.showWindow();
        });

        sound.ringing(true);
    }

    /**
     * When the user hangs up
     * @param callId the id of the call
     */
    @Override
    public void onHangUp(String callId) {
        for(AbstractGUI s: callGUIs.entrySet().stream().filter(cc -> callId.equals(cc.getKey())).map(Map.Entry::getValue).collect(Collectors.toList())){
            s.hideWindow();
        }
    }

    /**
     * A method to find the right {@link AcceptCallGUI} by its callId key and hide it.
     * @param callId the id of the call.
     */
    private void hideAcceptCallGUI(String callId){

        for (Map.Entry<String, AbstractGUI> c : callGUIs.entrySet()) {
            if (c.getKey().equals(callId)) {
//                c.getValue().setTitle("A call with " + );

                c.getValue().hideWindow();
            }
        }
    }

    /**
     * When the user accepts a call. The {@link AcceptCallGUI} gets hidden and a new {@link AudioCallGUI} gets created and put in a HashMap of {@link AbstractGUI}
     * We also stop the phone ringing sound.
     * @param callId the id of the call
     */
    @Override
    public void onAcceptingCall(String callId) {
        hideAcceptCallGUI(callId);

        SwingUtilities.invokeLater(() -> {
            AudioCallGUI audioCallGUI = new AudioCallGUI(phone, sound, callId);

            callGUIs.put(callId, audioCallGUI);

            audioCallGUI.showWindow();
        });

        sound.ringing(false);
    }

    /**
     * When the user declines a call the {@link AcceptCallGUI} gets hidden.
     * @param callId the id of the call
     */
    @Override
    public void onDecliningCall(String callId) {
        hideAcceptCallGUI(callId);
    }

    /**
     * When the user closes the main phone GUI {@link PhoneGUI}
     */
    @Override
    public void close() {
        phoneGUI.hideWindow();
    }

    /**
     * When the user start the app. A new {@link PhoneGUI} gets created.
     */
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

    /**
     * When the user wants to register/login a new {@link LoginGUI} gets created
     */
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

    /**
     * When the user clicked the register button, the {@link LoginGUI} gets hidden
     */
    @Override
    public void onRegistered() {
//        try {
//            account.getContactManager().loadPhoneBook(account.getUserInfo().get("username"));
//        }catch (Throwable e){
//            throw new IllegalArgumentException("Phonebook could not be loaded", e);
//        }
        account.userIsOnline();

        try{
            if(loginGUI.isActive()){
                loginGUI.hideWindow();
            }
        }catch (Throwable e){
            System.out.println("Login GUI Could not be displayed " + e);
        }
    }

    /**
     * When registering/logging in has failed, a error frame will pop up.
     */
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

    /**
     * When the user has unregistered
     */
    @Override
    public void onUnregistering() {

    }

    /**
     * When the user clicks on the menu item contact in the {@link PhoneGUIMenu}
     * We open up a new {@link ContactsGUI}, only if the user is registered, else we show an {@link AlertFrame}
     */
    @Override
    public void menuContactListOpen() {
        SwingUtilities.invokeLater(() -> {
            try {
                if(account.getUserInfo().size() > 0){
                    contactsGUI = new ContactsGUI(account);
                    contactsGUI.showWindow();
                }else{
                    new AlertFrame().showAlert(phoneGUI,"You have to be logged in to see your contacts", JOptionPane.INFORMATION_MESSAGE);
                }


            }catch (Throwable e){
                System.out.println("Contact list GUI could not be displayed " + e);

            }
        });
    }

    /**
     * When the user clicks on the menu item preferences in the {@link PhoneGUIMenu}
     * We open up a new {@link PreferencesGUI}.
     */
    @Override
    public void menuPreferencesOpen() {
        SwingUtilities.invokeLater(() -> {
            try {
                preferencesGUI = new PreferencesGUI(sound);
                preferencesGUI.showWindow();

            }catch (Throwable e){
                System.out.println("Preferences GUI could not be displayed " + e);

            }
        });
    }

    /**
     * When the user tries to call somebody and gets a NOT FOUND response, an alert will pop up.
     * @param proxyAddress the address that was attempted to call
     */
    @Override
    public void onNotFound(Address proxyAddress) {
        new AlertFrame().showAlert(phoneGUI,"Could not find: " + proxyAddress, JOptionPane.WARNING_MESSAGE);

    }

    /**
     * When an error occurs, an alert will pop up
     * @param text error message
     */
    @Override
    public void onError(String text) {
        new AlertFrame().showAlert(phoneGUI,text, JOptionPane.ERROR_MESSAGE);

    }

}
