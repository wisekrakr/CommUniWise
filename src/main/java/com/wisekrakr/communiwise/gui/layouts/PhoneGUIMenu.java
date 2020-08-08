package com.wisekrakr.communiwise.gui.layouts;


import com.wisekrakr.communiwise.phone.device.AccountAPI;
import com.wisekrakr.communiwise.phone.device.PhoneAPI;
import com.wisekrakr.communiwise.phone.managers.EventManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;

public class PhoneGUIMenu {
    private final JFrame mainFrame;
    private final PhoneAPI phone;
    private final AccountAPI account;

    private AccountFrame accountFrame;
    private OptionsPane optionsPane;
    private ContactPane contactPane;
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
        contactPane = new ContactPane();
        aboutFrame = new AboutFrame();

        menu();
    }



    public void menu() {

        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F');

        JMenuItem menuItemLogin = new JMenuItem("Login");
        menuItemLogin.setMnemonic('L');
        menuItemLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eventManager.onRegistering();
//                SwingUtilities.invokeLater(() -> {
//                    new LoginGUI(phone).initialize();
//                });
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

        JMenu menuAccount = new JMenu("Account");
        menuAccount.setMnemonic('A');

        menuAccount.add(addMenuItemAndSetVisible("Edit", accountFrame));

        JMenuItem menuItemContacts = new JMenuItem("Contacts");
        menuItemContacts.setMnemonic('C');
        menuItemContacts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contactPane.setVisible(true);
                contactPane.addContact.run();
            }
        });
        menuAccount.add(menuItemContacts);
        menuBar.add(menuAccount);

        JMenu menuPrefs = new JMenu("Preferences");
        menuPrefs.setMnemonic('P');

        menuPrefs.add(addMenuItemAndSetVisible("Audio", optionsPane));
        menuPrefs.add(addMenuItemAndSetVisible("Video", optionsPane));
        menuBar.add(menuPrefs);

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


    protected class ContactPane extends JFrame {

        private final JPanel mainPanel;
        private final JPanel contactPanel;

        GridBagConstraints gbc = new GridBagConstraints();

        private int labelCount = 0;
        private int gridx = 0;
        private int gridy = 0;

        public ContactPane() {

            GridLayout gridLayout = new GridLayout();
            setLayout(gridLayout);
            setMaximumSize(new Dimension(200,300));

            mainPanel = new JPanel(new GridLayout(0,1));
            mainPanel.setBorder(new TitledBorder("Contact List"));
            mainPanel.setMaximumSize(new Dimension(200,300));

            contactPanel = new JPanel(new GridLayout(0,1));

            add(mainPanel, BorderLayout.CENTER);

        }

        public Runnable addContact = new Runnable() {

            @Override
            public void run() {

                gbc.weightx = 1;
                gbc.weighty = 1;
                gbc.gridx = gridx;
                gbc.gridy = gridy;


                for(String contact: account.getContactManager().getContacts().keySet()){
//                    System.out.println("    MY CONTACTS    " + contact);

                    contactPanel.add(new JLabel(++labelCount + " " + contact + " " + contact.substring(4,7)), gbc);
                    contactPanel.revalidate();
                }

                gridy++;
                if (gridy >= 0) {
                    gridy = 0;
                    gridx++;
                }

                mainPanel.add(new JScrollPane(contactPanel), BorderLayout.CENTER);

                pack();
                setLocationRelativeTo(null);

                try {
                    setLocationByPlatform(true);
                    setMinimumSize(getSize());
                } catch (Throwable e) {
                    System.out.println("Could not add to contact list");
                }

            }
        };

    }


    protected class AboutFrame extends JFrame implements ActionListener, HyperlinkListener {

        public AboutFrame() {
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
            add(panel, BorderLayout.PAGE_END);

            pack();
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


    protected class AccountFrame extends JFrame implements ActionListener {

        public AccountFrame() {
            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            setTitle("Account Details");
            setUndecorated(true);

            String domain = account.getUserInfo().get("domain");
            String userName = account.getUserInfo().get("userName");

            String message = "Account logged in: <br>"
                    + userName + "<br>" + "<br>"
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

            pack();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }



    }

    protected class OptionsPane extends JFrame implements ActionListener {

        public OptionsPane() {

            //        super(new GridLayout(1, 1));

            JTabbedPane tabbedPane = new JTabbedPane();

            JComponent general = makeTextPanel("General");
            tabbedPane.addTab("General", null, general, "General Options");

            JComponent audio = audioTab();
            tabbedPane.addTab("Audio", null, audio, "Audio Options");

            JComponent video = makeTextPanel("Video");
            tabbedPane.addTab("Video", null, video, "Video options");

            add(tabbedPane);

            tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

            pack();
        }

        protected JComponent audioTab() {
            JPanel panel = new JPanel(new GridLayout(0, 2));

            JPanel outputPanel = new JPanel(new GridLayout(0, 1));
            JPanel inputPanel = new JPanel(new GridLayout(0, 1));

            outputPanel.setBorder(BorderFactory.createTitledBorder("Please choose your output mixer."));
            inputPanel.setBorder(BorderFactory.createTitledBorder("Please choose your input mixer."));

//        List<String> outputNames = application.getRTPConnectionManager().getMixerNames();
//        List<String> inputNames = application.getRTPConnectionManager().getMixerNames();

            JScrollPane scrollPane1 = new JScrollPane(outputPanel);
            JScrollPane scrollPane2 = new JScrollPane(inputPanel);

            panel.add(scrollPane1);
            panel.add(scrollPane2);

//        for (JRadioButton jrb : mixerList((ArrayList<String>) outputNames, outputPanel)) {
//            jrb.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    try {
//                        System.out.println("    Selected output Mixer: " + jrb.getText().trim());
//
//                        application.getRTPConnectionManager().selectAudioOutput(jrb.getText().trim());
//
//                    } catch (LineUnavailableException lineUnavailableException) {
//                        lineUnavailableException.printStackTrace();
//                    }
//                    JOptionPane.showMessageDialog(outputPanel, "You chose " + jrb.getText());
//                }
//            });
//
//        }
//
//        for (JRadioButton jrb : mixerList((ArrayList<String>) inputNames, inputPanel)) {
//            jrb.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    try {
//                        System.out.println("    Selected input Mixer: " + jrb.getText().trim());
//
//                        application.getRTPConnectionManager().selectAudioInput(jrb.getText().trim());
//
//                    } catch (LineUnavailableException lineUnavailableException) {
//                        lineUnavailableException.printStackTrace();
//                    }
//                    JOptionPane.showMessageDialog(outputPanel, "You chose " + jrb.getText());
//                }
//            });
//        }


            return panel;
        }

        /**
         * Returns a list of mixer types as radio buttons to choose from
         *
         * @return ArrayList of radio buttons
         */
        private ArrayList<JRadioButton> mixerList(ArrayList<String> list, JPanel panel) {
            ArrayList<JRadioButton> buttonList = new ArrayList<>();
            ButtonGroup bg = new ButtonGroup();
            for (String name : list) {
                JRadioButton jrb = new JRadioButton(name);
                buttonList.add(jrb);
                bg.add(jrb);
                panel.add(jrb);
            }
            return buttonList;
        }

        private JComponent makeTextPanel(String text) {
            JPanel panel = new JPanel(false);
            JLabel filler = new JLabel(text);
            filler.setHorizontalAlignment(JLabel.CENTER);
            panel.setLayout(new GridLayout(1, 1));
            panel.add(filler);
            return panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }
    }
}
