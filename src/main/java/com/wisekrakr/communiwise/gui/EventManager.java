package com.wisekrakr.communiwise.gui;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.*;
import com.wisekrakr.communiwise.gui.layouts.components.AlertFrame;
import com.wisekrakr.communiwise.gui.layouts.fx.call.IncomingCallGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.call.AudioCallGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.login.LoginGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.menu.AboutFrame;
import com.wisekrakr.communiwise.gui.layouts.fx.menu.AccountFrame;
import com.wisekrakr.communiwise.gui.layouts.fx.menu.ContactListGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.menu.PhoneGUIMenuBar;
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
    private ContactListGUI contactListGUI;
    private PreferencesGUI preferencesGUI;
    private LoginGUI loginGUI;

    private final Map<CallInstance, AbstractGUI> callGUIs = new HashMap<>();

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
     * @param callInstance the id of the call
     */
    @Override
    public void onOutgoingCall(CallInstance callInstance) {
        SwingUtilities.invokeLater(() -> {
            AudioCallGUI audioCallGUI = new AudioCallGUI(phone, sound, callInstance);

            callGUIs.put(callInstance, audioCallGUI);

            audioCallGUI.showWindow();
        });
    }

    /**
     * When the user get an incoming call. A new {@link IncomingCallGUI}gets created and put into a HashMap of  {@link AbstractGUI}
     * @param callInstance has an id, user and an InetSocketAddress
     */
    @Override
    public void onIncomingCall(CallInstance callInstance) {
        SwingUtilities.invokeLater(() -> {
            IncomingCallGUI incomingCallGUI = new IncomingCallGUI(phone, callInstance);

            callGUIs.put(callInstance, incomingCallGUI);

            incomingCallGUI.showWindow();
        });

        sound.ringing(true);
    }

    /**
     * When the user hangs up or on remote cancel or bye.
     * From the GUI that are shown, we find the right one through its callId and we hide it.
     * @param callInstance
     */
    @Override
    public void onHangUp(CallInstance callInstance) {
        for(AbstractGUI s: callGUIs.entrySet().stream().filter(cc -> callInstance.getId().equals(cc.getKey().getId())).map(Map.Entry::getValue).collect(Collectors.toList())){
            s.hideWindow();

            sound.ringing(false);
        }
    }

    /**
     * A method to find the right {@link IncomingCallGUI} by its callId key and hide it.
     * @param callId the id of the call.
     */
    private void hideAcceptCallGUI(String callId){

        for (Map.Entry<CallInstance, AbstractGUI> c : callGUIs.entrySet()) {
            if (c.getKey().getId().equals(callId)) {

                c.getValue().hideWindow();
            }
        }
    }

    /**
     * When the user accepts a call. The {@link IncomingCallGUI} gets hidden and a new {@link AudioCallGUI} gets created and put in a HashMap of {@link AbstractGUI}
     * We also stop the phone ringing sound.
     * @param callInstance
     */
    @Override
    public void onAcceptingCall(CallInstance callInstance) {
        hideAcceptCallGUI(callInstance.getId());

        SwingUtilities.invokeLater(() -> {
            AudioCallGUI audioCallGUI = new AudioCallGUI(phone, sound, callInstance);

            callGUIs.put(callInstance, audioCallGUI);

            audioCallGUI.showWindow();
        });

        sound.ringing(false);
    }



    /**
     * When the user declines a call the {@link IncomingCallGUI} gets hidden.
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
     * When the user wants to register/login a new {@link com.wisekrakr.communiwise.gui.layouts.LoginGUI} gets created
     */
    @Override
    public void onRegistering() {

        SwingUtilities.invokeLater(() -> {
            try {
                loginGUI = new LoginGUI(phone);

                loginGUI.showWindow();

            } catch (Exception e) {
                System.out.println("Login GUI Could not be displayed " + e);
            }

        });
    }

    /**
     * When the user clicked the register button, the {@link com.wisekrakr.communiwise.gui.layouts.LoginGUI} gets hidden
     */
    @Override
    public void onRegistered() {

        account.userIsOnline();

        try {
            loginGUI.hideWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * When registering/logging in has failed, a error frame will pop up.
     */
    @Override
    public void onAuthenticationFailed() {
        try{
            if(loginGUI.isActive()){
                onAlert(loginGUI, "Wrong credentials received....Please try again.", JOptionPane.ERROR_MESSAGE);
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
     * Opens up a new {@link ContactListGUI}, only if the user is registered, else opens an {@link AlertFrame}
     */
    @Override
    public void menuContactListOpen() {
        SwingUtilities.invokeLater(() -> {
            try {
                if(account.isAuthenticated()){
                    contactListGUI = new ContactListGUI(phone, account);
                    contactListGUI.showWindow();
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
     * Opens up a new {@link AboutFrame}.
     */
    @Override
    public void menuAboutOpen() {
        SwingUtilities.invokeLater(() -> {
            try {
                AboutFrame aboutFrame = new AboutFrame();
                aboutFrame.showWindow();

            }catch (Throwable e){
                System.out.println("Preferences GUI could not be displayed " + e);

            }
        });
    }

    /**
     * When the user clicks on the menu item preferences in the {@link PhoneGUIMenuBar}
     * Opens up a new {@link AccountFrame}.
     */
    @Override
    public void menuAccountOpen() {
        SwingUtilities.invokeLater(() -> {
            try {
                if(account.isAuthenticated()){
                    AccountFrame accountFrame = new AccountFrame(account);
                    accountFrame.showWindow();
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