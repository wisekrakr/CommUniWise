package com.wisekrakr.communiwise.gui.layouts;


import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.ext.AbstractScreen;
import com.wisekrakr.communiwise.gui.layouts.background.AlertFrame;
import com.wisekrakr.communiwise.gui.layouts.utils.FrameDragListener;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PhoneGUI extends AbstractScreen {

    private final EventManager eventManager;
    private final PhoneAPI phone;
    private final AccountAPI account;

    private static final int DESIRED_HEIGHT = 300;
    private static final int DESIRED_WIDTH = 700;

    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public PhoneGUI(EventManager eventManager, PhoneAPI phone, AccountAPI account) {
        this.eventManager = eventManager;
        this.phone = phone;
        this.account = account;
    }

    public void showWindow() {
        setTitle("CommUniWise Phone");
        setUndecorated(true);

        add(new PhonePane());

        setMinimumSize(new Dimension(DESIRED_WIDTH, DESIRED_HEIGHT));
        setBounds((screenSize.width - DESIRED_WIDTH) / 2, (screenSize.height - DESIRED_HEIGHT) / 2, DESIRED_WIDTH, DESIRED_HEIGHT);

        Border raised = BorderFactory.createRaisedBevelBorder();
        Border lowered = BorderFactory.createLoweredBevelBorder();
        Border compound = BorderFactory.createCompoundBorder(raised, lowered);

        getRootPane().setBorder(compound);

        FrameDragListener frameDragListener = new FrameDragListener(this);
        this.addMouseListener(frameDragListener);
        this.addMouseMotionListener(frameDragListener);

        PhoneGUIMenu phoneGUIMenu = new PhoneGUIMenu(this, eventManager, phone, account);
        phoneGUIMenu.init();

//        pack();
        setVisible(true);
        setResizable(true);

    }


    public class PhonePane extends JPanel {
        private final DestinationPane destinationPane;
        private final ControlsPane controlsPane;

        public PhonePane() {
            BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
            setLayout(boxLayout);

            setMinimumSize(new Dimension(DESIRED_WIDTH, DESIRED_HEIGHT));

            add((destinationPane = new DestinationPane()), BorderLayout.CENTER);
            add((controlsPane = new ControlsPane()), BorderLayout.CENTER);

        }

        public class ControlsPane extends JPanel {

            private JButton messageButton, audioCallButton, videoCallButton, unregisterButton;

            public ControlsPane() {

                setLayout(new GridLayout());
                setBorder(new CompoundBorder(new TitledBorder("Controls"), new EmptyBorder(12, 0, 0, 0)));

                JPanel panel = new JPanel(new GridLayout());
                panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Controls"));

                initComponents();
                makeAudioCall();
                startChatMessaging();
            }

            void initComponents() {
                add((messageButton = new JButton("Message")), BorderLayout.CENTER);
                add((audioCallButton = new JButton("Audio Call")), BorderLayout.CENTER);
                add((videoCallButton = new JButton("Video Call")), BorderLayout.CENTER);
                add((unregisterButton = new JButton("Unregister")), BorderLayout.CENTER);
            }

            void makeAudioCall() {
                audioCallButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        phone.initiateCall("sip:" + (destinationPane.getSipTargetName().trim() + "@" + destinationPane.getSipTargetAddress().trim()));

                        destinationPane.checkForInputs();
                    }
                });
            }

            void startChatMessaging() {
                messageButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        ChatFrame chatFrame = new ChatFrame("bla");
                        chatFrame.setVisible(true);
                    }
                });

            }
        }

        public class DestinationPane extends JPanel {
            private final JTextField sipTargetName;
            private final JTextField sipTargetAddress;
            private final JTextField sipTargetPort;

            public DestinationPane() {

                GridLayout gridLayout = new GridLayout(4, 2);
                setLayout(gridLayout);
                setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Destination"));

                add(new JLabel("Contact Name (or Number): "), BorderLayout.WEST);
                add((sipTargetName = new JTextField(3)), BorderLayout.CENTER);

                add(new JLabel("Contact Address (ip or server): "), BorderLayout.WEST);
                add((sipTargetAddress = new JTextField("asterisk.interzone", 3)), BorderLayout.CENTER);

                add(new JLabel("Contact Port (5060 or 5061): "), BorderLayout.WEST);
                add((sipTargetPort = new JTextField("5060", 3)), BorderLayout.CENTER);

            }

            public void checkForInputs(){
                if(sipTargetName.getText().equals("")){
                    new AlertFrame().showAlert("Please fill in an extension to call", JOptionPane.INFORMATION_MESSAGE);
                }
                if(sipTargetAddress.getText().equals("")){
                    new AlertFrame().showAlert("Please fill in a domain", JOptionPane.INFORMATION_MESSAGE);
                }
                if(sipTargetPort.getText().equals("")){
                    new AlertFrame().showAlert("Please fill in a proxy port", JOptionPane.INFORMATION_MESSAGE);
                }
            }

            protected String getSipTargetName() {
                return sipTargetName.getText().trim();
            }

            protected String getSipTargetAddress() {
                return sipTargetAddress.getText().trim();
            }

            protected String getSipTargetPort() {
                return sipTargetPort.getText().trim();
            }
        }


/*
            private void audioOptions() {
                JFrame chooseMixerFrame = new JFrame("Sound Mixer");

                java.util.List<String> names = application.getRTPConnectionManager().getMixerNames();

                JPanel mainPanel = new JPanel(new GridLayout(2, 0));
                JPanel teamPanel = new JPanel(new GridLayout(0, 1));

                java.util.List<JRadioButton> list = new ArrayList<>();
                ButtonGroup bg = new ButtonGroup();
                for (String name : names) {
                    JRadioButton jrb = new JRadioButton(name);
                    list.add(jrb);
                    bg.add(jrb);
                    teamPanel.add(jrb);
                }
                teamPanel.setBorder(BorderFactory.createTitledBorder("Please choose a mixer."));
                mainPanel.add(new JScrollPane(teamPanel));
                JPanel okPanel = new JPanel(new GridBagLayout());

                JButton okButton = new JButton(new AbstractAction("OK") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        for (JRadioButton jrb : list) {
                            if (jrb.isSelected()) {
                                try {
                                    System.out.println("    Selected Mixer: " + jrb.getText().trim());
                                    application.getRTPConnectionManager().selectAudioOutput(jrb.getText().trim());
                                } catch (LineUnavailableException lineUnavailableException) {
                                    lineUnavailableException.printStackTrace();
                                }
                                JOptionPane.showMessageDialog(chooseMixerFrame, "You chose " + jrb.getText());
                            }
                        }
                    }
                });
                okButton.setFont(okButton.getFont().deriveFont(36f));
                okPanel.add(okButton);
                mainPanel.add(okPanel);
                chooseMixerFrame.add(mainPanel);
                chooseMixerFrame.pack();
                chooseMixerFrame.setSize(400, list.get(0).getHeight() * 16);
                chooseMixerFrame.setLocationRelativeTo(null);
                chooseMixerFrame.setVisible(true);
            }*/

    }




}
