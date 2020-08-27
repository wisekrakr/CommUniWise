package com.wisekrakr.communiwise.gui;

import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.*;
import com.wisekrakr.communiwise.gui.layouts.components.AlertFrame;
import com.wisekrakr.communiwise.gui.layouts.gui.call.IncomingCallFXGUI;
import com.wisekrakr.communiwise.gui.layouts.gui.call.AudioCallGUI;
import com.wisekrakr.communiwise.gui.layouts.gui.login.LoginFXGUI;
import com.wisekrakr.communiwise.gui.layouts.gui.menu.AboutFXFrame;
import com.wisekrakr.communiwise.gui.layouts.gui.menu.AccountFXFrame;
import com.wisekrakr.communiwise.gui.layouts.gui.menu.PhoneGUIMenuBar;
import com.wisekrakr.communiwise.operations.DeviceImplementations;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;
import com.wisekrakr.communiwise.phone.calling.CallInstance;


import javax.sip.address.Address;
import javax.swing.*;
import java.awt.*;
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
//    private LoginGUI loginGUI;
    private ContactsGUI contactsGUI;
    private PreferencesGUI preferencesGUI;
    private LoginFXGUI loginFXGUI;

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

    public PhoneAPI getPhone() {
        return phone;
    }

    public AccountAPI getAccount() {
        return account;
    }

    public SoundAPI getSound() {
        return sound;
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
     * When the user get an incoming call. A new {@link IncomingCallFXGUI}gets created and put into a HashMap of  {@link AbstractGUI}
     * @param callInstance has an id, user and an InetSocketAddress
     */
    @Override
    public void onIncomingCall(CallInstance callInstance) {
        SwingUtilities.invokeLater(() -> {
            IncomingCallFXGUI incomingCallFXGUI = new IncomingCallFXGUI(this, callInstance);

            callGUIs.put(callInstance.getId(), incomingCallFXGUI);

            incomingCallFXGUI.showWindow();
        });

        sound.ringing(true);
    }

    /**
     * When the user hangs up or on remote cancel or bye.
     * From the GUI that are shown, we find the right one through its callId and we hide it.
     * @param callId the id of the call
     */
    @Override
    public void onHangUp(String callId) {
        for(AbstractGUI s: callGUIs.entrySet().stream().filter(cc -> callId.equals(cc.getKey())).map(Map.Entry::getValue).collect(Collectors.toList())){
            s.hideWindow();

            sound.ringing(false);
        }
    }

    /**
     * A method to find the right {@link IncomingCallFXGUI} by its callId key and hide it.
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
     * When the user accepts a call. The {@link IncomingCallFXGUI} gets hidden and a new {@link AudioCallGUI} gets created and put in a HashMap of {@link AbstractGUI}
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
     * When the user declines a call the {@link IncomingCallFXGUI} gets hidden.
     * @param callId the id of the call
     */
    @Override
    public void onDecliningCall(String callId) {
        hideAcceptCallGUI(callId);

        sound.ringing(false);

    }

    /**
     * When the user closes the main phone GUI {@link PhoneGUI}
     */
    @Override
    public void close() {
        phoneGUI.hideWindow();

        System.exit(1);
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
                loginFXGUI = new LoginFXGUI(this);
                loginFXGUI.showWindow();

            } catch (Exception e) {
                System.out.println("Login GUI Could not be displayed " + e);
            }

        });
    }

    /**
     * When the user clicked the register button, the {@link LoginGUI} gets hidden
     */
    @Override
    public void onRegistered() {

        account.userIsOnline();

        try {
            loginFXGUI.hideWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try{
//            if(loginGUI.isActive()){
//                loginGUI.hideWindow();
//            }
//        }catch (Throwable e){
//            System.out.println("Login GUI Could not be displayed " + e);
//        }
    }

    /**
     * When registering/logging in has failed, a error frame will pop up.
     */
    @Override
    public void onAuthenticationFailed() {
        try{
            if(loginFXGUI.isActive()){
                onAlert(loginFXGUI, "Wrong credentials received....Please try again.", JOptionPane.ERROR_MESSAGE);
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
     * When the user clicks on the menu item contact in the {@link PhoneGUIMenuBar}
     * Opens up a new {@link ContactsGUI}, only if the user is registered, else opens an {@link AlertFrame}
     */
    @Override
    public void menuContactListOpen() {
        SwingUtilities.invokeLater(() -> {
            try {
                if(account.isAuthenticated()){
                    contactsGUI = new ContactsGUI(this,account);
                    contactsGUI.showWindow();
                }else{
                    onAlert(phoneGUI, "You have to be logged in to see your contacts, go to File --> Login", JOptionPane.INFORMATION_MESSAGE);
                }


            }catch (Throwable e){
                System.out.println("Contact list GUI could not be displayed " + e);

            }
        });
    }

    /**
     * When the user clicks on the menu item preferences in the {@link PhoneGUIMenuBar}
     * Opens up a new {@link PreferencesGUI}.
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
     * When the user clicks on the menu item preferences in the {@link PhoneGUIMenuBar}
     * Opens up a new {@link AboutFXFrame}.
     */
    @Override
    public void menuAboutOpen() {
        SwingUtilities.invokeLater(() -> {
            try {
                AboutFXFrame aboutFXFrame = new AboutFXFrame();
                aboutFXFrame.showWindow();

            }catch (Throwable e){
                System.out.println("Preferences GUI could not be displayed " + e);

            }
        });
    }

    /**
     * When the user clicks on the menu item preferences in the {@link PhoneGUIMenuBar}
     * Opens up a new {@link AccountFXFrame}.
     */
    @Override
    public void menuAccountOpen() {
        SwingUtilities.invokeLater(() -> {
            try {
                if(account.isAuthenticated()){
                    AccountFXFrame accountFXFrame = new AccountFXFrame(this);
                    accountFXFrame.showWindow();
                }else{
                    new AlertFrame().showAlert(phoneGUI,"You have to be logged in to see your account information, go to File --> Login", JOptionPane.INFORMATION_MESSAGE);
                }

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
     * This will show an {@link AlertFrame} pop up on a certain jFrame
     * @param component where the AlertFrame will pop out off
     * @param text alert message
     * @param messageCode JOptionPane message code
     */
    @Override
    public void onAlert(Component component, String text, int messageCode) {
        new AlertFrame().showAlert(component,text, messageCode);
    }



}
