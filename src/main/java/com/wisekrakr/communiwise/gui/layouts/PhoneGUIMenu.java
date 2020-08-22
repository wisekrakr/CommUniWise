package com.wisekrakr.communiwise.gui.layouts;


import com.wisekrakr.communiwise.gui.ext.AbstractGUI;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.user.SipAccountManager;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

public class PhoneGUIMenu {
    private final JFrame mainFrame;
    private final PhoneAPI phone;
    private final AccountAPI account;

    private AccountFrame accountFrame;
    private OptionsPane optionsPane;
    private AboutFrame aboutFrame;

    private final EventManager eventManager;

    public PhoneGUIMenu(JFrame mainFrame, EventManager eventManager, PhoneAPI phone, AccountAPI account) {
        this.mainFrame = mainFrame;
        this.eventManager = eventManager;
        this.phone = phone;
        this.account = account;
    }

    public void init() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            System.out.println("WARNING: unable to set look and feel, will continue");
        }

        accountFrame = new AccountFrame();
        optionsPane = new OptionsPane();
        aboutFrame = new AboutFrame();

        menu();
    }

    private void menu() {

        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F');

        JMenuItem menuItemLogin = new JMenuItem("Login");
        menuItemLogin.setMnemonic('L');
        menuItemLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eventManager.onRegistering();
            }
        });
        menuFile.add(menuItemLogin);

        JMenuItem menuItemExit = new JMenuItem("Exit");
        menuItemExit.setMnemonic('x');

        menuItemExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });
        menuFile.add(menuItemExit);
        menuBar.add(menuFile);

        JMenu menuEdit = new JMenu("Edit");
        menuEdit.setMnemonic('A');

        menuEdit.add(addMenuItemAndSetVisible("Account", accountFrame));

        JMenuItem menuItemContacts = new JMenuItem("Contacts");
        menuItemContacts.setMnemonic('C');
        menuItemContacts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eventManager.menuContactListOpen();
            }
        });
        menuEdit.add(menuItemContacts);

        JMenuItem menuItemPrefs = new JMenuItem("Preferences");
        menuItemPrefs.setMnemonic('P');
        menuItemPrefs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eventManager.menuPreferencesOpen();
            }
        });
        menuEdit.add(menuItemPrefs);
        menuBar.add(menuEdit);


        JMenu menuAbout = new JMenu("About");
        menuAbout.setMnemonic('B');
        menuAbout.add(addMenuItemAndSetVisible("wisekrakr inc", aboutFrame));
        menuBar.add(menuAbout);

        menuBar.setVisible(true);
        mainFrame.setJMenuBar(menuBar);
    }

    private JMenuItem addMenuItemAndSetVisible(String title, Component component){
        JMenuItem menuItemAbout = new JMenuItem(title);
        menuItemAbout.setMnemonic(title.toUpperCase().charAt(0));
        menuItemAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                component.setVisible(true);
            }
        });
        return menuItemAbout;
    }


    protected class AboutFrame extends AbstractGUI implements ActionListener, HyperlinkListener {

        private AboutFrame() {
            prepareGUI();

        }

        @Override
        public void prepareGUI() {
            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            setTitle("About");
            setUndecorated(true);

            String message = "CommUniWise: java SIP push-to-talk softphone<br>"
                    + "Copyright 2020 Wisekrakr<br>"
                    + "<a href=\"www.github.com/wisekrakr\">www.github.com/wisekrakr</a>";
            JTextPane textPane = new JTextPane();
            textPane.setContentType("text/html");
            textPane.setEditable(false);
            textPane.setText(message);
            textPane.addHyperlinkListener(this);
            textPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            add(textPane, BorderLayout.PAGE_START);

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            Font font = textArea.getFont();
            font = new Font(font.getName(), font.getStyle(), font.getSize() - 2);
            textArea.setFont(font);


            JPanel panel = new JPanel();
            JButton button = new JButton("Close");
            button.addActionListener(this);
            panel.add(button);
            add(panel, BorderLayout.PAGE_END);;
        }

        @Override
        public void showWindow() {
            pack();
            setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }

        @Override
        public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(hyperlinkEvent.getEventType())) {
                try {
                    URI uri = new URI("www.github.com/wisekrakr");
                    java.awt.Desktop.getDesktop().browse(uri);
                } catch (Throwable e) {
                    System.out.println("Hyperlinkupdate failed");
                }
            }
        }
    }


    protected class AccountFrame extends AbstractGUI implements ActionListener {

        private AccountFrame() {
           prepareGUI();
        }

        @Override
        public void prepareGUI() {
            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            setTitle("Account Details");
            setUndecorated(true);

            String domain = account.getUserInfo().get(SipAccountManager.UserInfoPart.DOMAIN.getInfoPart());
            String username = account.getUserInfo().get(SipAccountManager.UserInfoPart.USERNAME.getInfoPart());

            String message = "Account logged in: <br>"
                    + username + "<br>" + "<br>"
                    + "Registered on Domain <br>"
                    + domain;

            JTextPane textPane = new JTextPane();
            textPane.setContentType("text/html");
            textPane.setEditable(false);
            textPane.setText(message);
            textPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
            add(textPane, BorderLayout.PAGE_START);

            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            Font font = textArea.getFont();
            font = new Font(font.getName(), font.getStyle(), font.getSize() - 2);
            textArea.setFont(font);

            JPanel panel = new JPanel();
            JButton button = new JButton("Close");
            button.addActionListener(this);
            panel.add(button);
            add(panel, BorderLayout.PAGE_END);

        }

        @Override
        public void showWindow() {
            pack();
            setVisible(true);
        }



        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }



    }

    protected class OptionsPane extends AbstractGUI {
        private static final long serialVersionUID = 1L;

        Mixer mixer = null;

        private OptionsPane() {

            JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
            ButtonGroup group = new ButtonGroup();
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            for (Mixer.Info info : mixerInfos) {
                Mixer m = AudioSystem.getMixer(info);
                Line.Info[] lineInfos = m.getTargetLineInfo();
                if (lineInfos.length > 0 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
                    JRadioButton button = new JRadioButton();
                    button.setText(info.getName());
                    button.setActionCommand(info.toString());
                    button.addActionListener(setInput);
                    buttonPanel.add(button);
                    group.add(button);
                }
            }
            this.add(new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

            this.setMaximumSize(new Dimension(300, 150));
            this.setPreferredSize(new Dimension(300, 150));

        }

        private ActionListener setInput = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                for (Mixer.Info info : AudioSystem.getMixerInfo()) {
                    if (arg0.getActionCommand().equals(info.toString())) {
                        Mixer newValue = AudioSystem.getMixer(info);

                        firePropertyChange("mixer", mixer, newValue);

                        mixer = newValue;
                        break;
                    }
                }
            }
        };

    }
}
