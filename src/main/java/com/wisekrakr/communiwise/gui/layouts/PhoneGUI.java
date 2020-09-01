package com.wisekrakr.communiwise.gui.layouts;


import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.layouts.fx.menu.PhoneGUIMenuBar;
import com.wisekrakr.communiwise.gui.layouts.utils.Constants;
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

import static com.wisekrakr.communiwise.gui.layouts.components.ImageCreator.addImageIcon;

public class PhoneGUI extends AbstractGUI {

    private final EventManager eventManager;
    private final PhoneAPI phone;
    private final AccountAPI account;

    private static final int DESIRED_HEIGHT = 300;
    private static final int DESIRED_WIDTH = 700;

    public PhoneGUI(EventManager eventManager, PhoneAPI phone, AccountAPI account) {
        this.eventManager = eventManager;
        this.phone = phone;
        this.account = account;

        prepareGUI();
    }


    @Override
    public void prepareGUI() {
        setTitle("CommUniWise Sip Phone");
        setUndecorated(true);
        setVisible(true);
        setResizable(true);

        add(new PhonePane());

        showLogoPanel();

        setMinimumSize(new Dimension(DESIRED_WIDTH, DESIRED_HEIGHT));
        setBounds((getScreenSize().width - DESIRED_WIDTH) / 2, (getScreenSize().height - DESIRED_HEIGHT) / 2, DESIRED_WIDTH, DESIRED_HEIGHT);

        Border raised = BorderFactory.createRaisedBevelBorder();
        Border lowered = BorderFactory.createLoweredBevelBorder();
        Border compound = BorderFactory.createCompoundBorder(raised, lowered);

        getRootPane().setBorder(compound);
    }

    @Override
    public void showWindow() {

        addFrameDragAbility();

        PhoneGUIMenuBar phoneGUIMenuBar = new PhoneGUIMenuBar(eventManager);
        setJMenuBar(phoneGUIMenuBar);

        setVisible(true);

    }

    private void showLogoPanel(){

        JLabel picLabel = new JLabel(addImageIcon("/images/logo1.png", false));
        JPanel logoPanel = new JPanel();
        logoPanel.add(picLabel);
        logoPanel.setBackground(Constants.LIGHT_CYAN);

        add(logoPanel, BorderLayout.WEST);
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
                setBorder(new CompoundBorder(new TitledBorder("Controls"), new EmptyBorder(12, 12, 12, 12)));

                initComponents();
                audioCallComponent();
                chatMessagingComponent();
            }

            void initComponents() {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                panel.add((messageButton = new JButton("Messenger",addImageIcon("/images/chat.png", true))), BorderLayout.CENTER);
                panel.add((audioCallButton = new JButton("Audio Call",addImageIcon("/images/mic.png", true))), BorderLayout.CENTER);
                panel.add((videoCallButton = new JButton("Video Call",addImageIcon("/images/webcam.png",true ))), BorderLayout.CENTER);
//                panel.add((unregisterButton = new JButton("Unregister")), BorderLayout.CENTER);

                add(panel);
            }

            void audioCallComponent() {
                audioCallButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(account.isAuthenticated()){
                            phone.initiateCall(destinationPane.getSipTargetName().trim() , destinationPane.getSipTargetAddress().trim());
                            destinationPane.checkForInputs();
                        }else {
                            eventManager.onAlert(PhoneGUI.this, "You have to register first, go to: File -> Login ", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
            }

            void chatMessagingComponent() {
                messageButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(account.isAuthenticated()){
                            ChatFrame chatFrame = new ChatFrame("bla");
                            chatFrame.setVisible(true);
                        }else {
                            eventManager.onAlert(PhoneGUI.this, "You have to register first, go to: File -> Login ", JOptionPane.INFORMATION_MESSAGE);
                        }
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
                    eventManager.onAlert(PhoneGUI.this, "Please fill in an extension to call", JOptionPane.INFORMATION_MESSAGE);
                }
                if(sipTargetAddress.getText().equals("")){
                    eventManager.onAlert(PhoneGUI.this, "Please fill in a domain", JOptionPane.INFORMATION_MESSAGE);
                }
                if(sipTargetPort.getText().equals("")){
                    eventManager.onAlert(PhoneGUI.this, "Please fill in a proxy port", JOptionPane.INFORMATION_MESSAGE);
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
    }


}
