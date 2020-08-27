package com.wisekrakr.communiwise.gui.layouts.gui.menu;


import com.wisekrakr.communiwise.gui.EventManager;

import javax.swing.*;

import static com.wisekrakr.communiwise.gui.layouts.components.ImageIconCreator.addImageIcon;

/**
 * A simple menu bar. With three option menus with various items.
 * Not only
 */
public class PhoneGUIMenuBar extends JMenuBar{
    private final EventManager eventManager;

    public PhoneGUIMenuBar(EventManager eventManager) {
        this.eventManager = eventManager;

        init();
    }

    public void init() {

        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F');

        //Login

        JMenuItem menuItemLogin = new JMenuItem("Login", addImageIcon("/images/login.png",true));
        menuItemLogin.setMnemonic('L');
        menuItemLogin.addActionListener(e -> eventManager.onRegistering());
        menuFile.add(menuItemLogin);

        //Logout

        JMenuItem menuItemExit = new JMenuItem("Exit", addImageIcon("/images/exit.png",true));
        menuItemExit.setMnemonic('x');
        menuItemExit.addActionListener(e -> eventManager.close());
        menuFile.add(menuItemExit);

        add(menuFile);

        JMenu menuEdit = new JMenu("Edit");
        menuEdit.setMnemonic('E');

        //Acount

        JMenuItem menuItemAccount = new JMenuItem("Account", addImageIcon("/images/account.png",true));
        menuItemAccount.setMnemonic('A');
        menuItemAccount.addActionListener(e -> eventManager.menuAccountOpen());
        menuEdit.add(menuItemAccount);

        //Contacts

        JMenuItem menuItemContacts = new JMenuItem("Contacts", addImageIcon("/images/contact-list.png",true));
        menuItemContacts.setMnemonic('C');
        menuItemContacts.addActionListener(e -> eventManager.menuContactListOpen());
        menuEdit.add(menuItemContacts);

        //Preferences

        JMenuItem menuItemPrefs = new JMenuItem("Preferences", addImageIcon("/images/preferences.png",true));
        menuItemPrefs.setMnemonic('P');
        menuItemPrefs.addActionListener(e -> eventManager.menuPreferencesOpen());
        menuEdit.add(menuItemPrefs);
        add(menuEdit);


        JMenu menuAbout = new JMenu("About");
        menuAbout.setMnemonic('A');

        //About

        JMenuItem menuItemAbout = new JMenuItem("wisekrakr inc.", addImageIcon("/images/logo1.png",true));
        menuItemAbout.setMnemonic('P');
        menuItemAbout.addActionListener(e -> eventManager.menuAboutOpen());
        menuAbout.add(menuItemAbout);
        add(menuAbout);

        setVisible(true);
    }


}

