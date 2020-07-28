package com.wisekrakr.communiwise.frames.layouts;


import com.wisekrakr.communiwise.frames.AudioPlayerFrame;
import com.wisekrakr.communiwise.phone.device.PhoneAPI;
import com.wisekrakr.communiwise.frames.ext.AbstractScreen;
import com.wisekrakr.communiwise.frames.layouts.panes.background.GradientPanel;
import com.wisekrakr.communiwise.phone.messaging.ChatClient;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class PhoneScreen extends AbstractScreen {
    private final PhoneAPI phone;

    public PhoneScreen(PhoneAPI phone) {
        this.phone = phone;
    }

    public void showWindow() {
        setTitle("CommUniWise Phone");

        add(new PhonePane());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 288) / 2, (screenSize.height - 310) / 2, 700, 300);

        setVisible(true);
    }

    public class PhonePane extends GradientPanel {
        private DestinationPane destinationPane;
        private ControlsPane controlsPane;
        private MenuPane menuPane;

        public PhonePane() {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 0.33;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(4, 4, 4, 4);

            add((destinationPane = new DestinationPane()), gbc);
            gbc.gridy++;
            add((controlsPane = new ControlsPane()), gbc);
            gbc.gridy++;

            gbc.gridy = 0;
            gbc.gridx++;
            gbc.gridheight = GridBagConstraints.REMAINDER;
            gbc.fill = GridBagConstraints.VERTICAL;
            gbc.weighty = 1;
            gbc.weightx = 0;
            add((menuPane = new MenuPane()), gbc);
        }

        public class ControlsPane extends JPanel {

            private JButton messageButton, audioCallButton, videoCallButton, unregisterButton;
            private JLabel controls;

            public ControlsPane() {
                setLayout(new GridBagLayout());
                setBorder(new CompoundBorder(new TitledBorder("Controls"), new EmptyBorder(12, 0, 0, 0)));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(0, 0, 0, 4);

                JPanel panel = new JPanel(new GridBagLayout());
                panel.add(new JLabel("Make a connection: "), gbc);
                gbc.gridx++;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(0, 0, 0, 0);
                panel.add((controls = new JLabel()), gbc);

                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.weightx = 1;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(4, 4, 4, 4);
                add(panel, gbc);

                gbc.gridwidth = 1;
                gbc.weightx = 0.25;
                gbc.gridy++;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                add((messageButton = new JButton("Message")), gbc);
                gbc.gridx++;
                add((audioCallButton = new JButton("Audio Call")), gbc);
                gbc.gridx++;
                add((videoCallButton = new JButton("Video Call")), gbc);
                gbc.gridx++;
                add((unregisterButton = new JButton("Unregister")), gbc);

                makeAudioCall();
                startChatMessaging();
            }

            void makeAudioCall() {
                audioCallButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        phone.initiateCall("sip:" + (destinationPane.getSipTargetName().trim() + "@" + destinationPane.getSipTargetAddress().trim()));
                    }
                });
            }

            void startChatMessaging(){
                messageButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ChatClient client = new ChatClient(destinationPane.getSipTargetAddress().trim(), Integer.parseInt(destinationPane.getSipTargetPort()));
                        client.execute();
                    }
                });

            }
        }

        public class DestinationPane extends JPanel {
            private JTextField sipTargetName;
            private JTextField sipTargetAddress;
            private JTextField sipTargetPort;

            public DestinationPane() {

                setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();

                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.WEST;

                add(new JLabel("Contact Name (or Number): "), gbc);
                gbc.gridy++;
                add(new JLabel("Contact Address (ip or server): "), gbc);
                gbc.gridy++;
                add(new JLabel("Contact Port (5060 or 5061): "), gbc);

                gbc.gridx++;
                gbc.gridy = 0;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;

                add((sipTargetName = new JTextField(3)), gbc);
                gbc.gridy++;
                add((sipTargetAddress = new JTextField("asterisk.interzone", 3)), gbc);
                gbc.gridy++;
                add((sipTargetPort = new JTextField("5060", 3)), gbc);
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

        public class MenuPane extends JPanel {

            private JButton okay, cancel, help, player, options;

            public MenuPane() {

                setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();

                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1;
                gbc.insets = new Insets(4, 4, 4, 4);

                add((okay = new JButton("Ok")), gbc);
                gbc.gridy++;
                add((cancel = new JButton("Cancel")), gbc);
                gbc.gridy++;
                add((help = new JButton("Help")), gbc);
                gbc.gridy++;
                add((player = new JButton("Player")), gbc);
                gbc.gridy++;
                gbc.weighty = 1;
                gbc.anchor = GridBagConstraints.SOUTH;
                add((options = new JButton("Options >>")), gbc);

                clickOptions();
                clickPlayer();

            }

            private void clickOptions() {
                options.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        OptionsPane optionsPane = new OptionsPane();
                        optionsPane.init();
                    }
                });
            }

            private void clickPlayer() {
                player.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                new AudioPlayerFrame().setVisible(true);
                            }
                        });
                    }
                });
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

        public class OptionsPane extends JPanel {
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
            }

            public void init() {
                JFrame frame = new JFrame("Please choose a mixer.");

                frame.add(new OptionsPane(), BorderLayout.CENTER);

                frame.pack();
                frame.setVisible(true);
            }

            protected JComponent audioTab() {
                JPanel panel = new JPanel(new GridLayout(0, 2));

                JPanel outputPanel = new JPanel(new GridLayout(0, 1));
                JPanel inputPanel = new JPanel(new GridLayout(0, 1));

                outputPanel.setBorder(BorderFactory.createTitledBorder("Please choose your output mixer."));
                inputPanel.setBorder(BorderFactory.createTitledBorder("Please choose your input mixer."));
/*
                java.util.List<String> outputNames = application.getRTPConnectionManager().getMixerNames();
                List<String> inputNames = application.getRTPConnectionManager().getMixerNames();

                JScrollPane scrollPane1 = new JScrollPane(outputPanel);
                JScrollPane scrollPane2 = new JScrollPane(inputPanel);

                panel.add(scrollPane1);
                panel.add(scrollPane2);

                for (JRadioButton jrb : mixerList((ArrayList<String>) outputNames, outputPanel)) {
                    jrb.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                System.out.println("    Selected output Mixer: " + jrb.getText().trim());

                                application.getRTPConnectionManager().selectAudioOutput(jrb.getText().trim());

                            } catch (LineUnavailableException lineUnavailableException) {
                                lineUnavailableException.printStackTrace();
                            }
                            JOptionPane.showMessageDialog(outputPanel, "You chose " + jrb.getText());
                        }
                    });

                }

                for (JRadioButton jrb : mixerList((ArrayList<String>) inputNames, inputPanel)) {
                    jrb.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                System.out.println("    Selected input Mixer: " + jrb.getText().trim());

                                application.getRTPConnectionManager().selectAudioInput(jrb.getText().trim());

                            } catch (LineUnavailableException lineUnavailableException) {
                                lineUnavailableException.printStackTrace();
                            }
                            JOptionPane.showMessageDialog(outputPanel, "You chose " + jrb.getText());
                        }
                    });
                }*/


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
        }
    }
}
