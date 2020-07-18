package com.wisekrakr.communiwise.main;


import com.wisekrakr.communiwise.phone.audiovisualconnection.AudioManager;
import com.wisekrakr.communiwise.phone.audiovisualconnection.RTPConnectionManager;
import com.wisekrakr.communiwise.phone.audiovisualconnection.SoundAPI;
import com.wisekrakr.communiwise.phone.device.PhoneAPI;
import com.wisekrakr.communiwise.phone.managers.SipManagerListener;
import com.wisekrakr.communiwise.phone.managers.SipManager;
import com.wisekrakr.communiwise.frames.layouts.AudioCallScreen;
import com.wisekrakr.communiwise.frames.layouts.IncomingCallScreen;
import com.wisekrakr.communiwise.frames.layouts.LoginScreen;
import com.wisekrakr.communiwise.frames.layouts.PhoneScreen;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;

public class PhoneApplication implements Serializable {

    private static final AudioFormat FORMAT = new AudioFormat(8000, 16, 1, true, false);
    private SipManager sipManager;

    private LoginScreen loginScreen;
    private PhoneScreen phoneScreen;
    private IncomingCallScreen incomingCallScreen;
    private AudioCallScreen audioCallScreen;

    private RTPConnectionManager rtpConnectionManager;
    private AudioManager audioManager;

    private static void printHelp(String message) {
        System.out.println(message);
        System.out.println("Arguments: <local address> <audio input> <audio output>");

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        System.out.println("Available: ");
        for (int i = 0; i < mixers.length; i++) {
            System.out.println(String.format("%-50s %50s %30s %30s", mixers[i].getName(), mixers[i].getDescription(), mixers[i].getVersion(), mixers[i].getVendor()));
        }
    }

    public static void main(String[] args) {
        if (args.length == 1 && "help".equalsIgnoreCase(args[0]) || args.length != 3) {
            printHelp((args.length == 1 && "help".equalsIgnoreCase(args[0])) ? "Help" : "Invalid arguments");

            System.exit(1);
        }


/*
        // TODO: audio setup should happen outside of this class
        DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class,FORMAT);

        Mixer mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[1]); // todo add mixer

        output = (SourceDataLine) AudioSystem.getLine(speakerInfo);

        DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class,FORMAT);

        Mixer mixer2 = AudioSystem.getMixer(AudioSystem.getMixerInfo()[8]); // todo add mixer
        input = (TargetDataLine) mixer2.getLine(micInfo);

 */


        PhoneApplication application = new PhoneApplication();
        try {
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();

            TargetDataLine inputLine = null;
            SourceDataLine outputLine = null;

            for (int i = 0; i < mixers.length; i++) {
                if (args[1].equals(mixers[i].getName())) {
                    inputLine = (TargetDataLine) AudioSystem.getMixer(mixers[i]).getLine(new DataLine.Info(TargetDataLine.class, FORMAT));
                }
                if (args[2].equals(mixers[i].getName())) {
                    outputLine = (SourceDataLine) AudioSystem.getMixer(mixers[i]).getLine(new DataLine.Info(SourceDataLine.class, FORMAT));
                }
            }

            if (inputLine == null) {
                printHelp("Input line not found " + args[1]);
                System.exit(1);
            }
            if (outputLine == null) {
                printHelp("Output line not found " + args[2]);
                System.exit(1);
            }

            inputLine.open(FORMAT);
            outputLine.open(FORMAT);

            String localAddress = args[0];


            application.initialize(
                    inputLine,
                    outputLine,
                    localAddress,
                    5080,
                    "udp",
                    "asterisk.interzone",
                    5060
            );


        } catch (Exception e) {
            System.out.println("Unable to initialize: " + e);
            e.printStackTrace();

            System.exit(1);

            return;
        }

        application.run();
    }

    private void run() {
    }

    private void initialize(TargetDataLine inputLine, SourceDataLine outputLine, String localAddress, int localPort, String transport, String proxyHost, int proxyPort) throws Exception {

        sipManager = new SipManager(proxyHost, proxyPort, localAddress, localPort, transport).
                logging("server.log", "debug.log", 16).
                listener(new SipManagerListener() {

                    @Override
                    public void onTextMessage(String message, String from) {
                        System.out.println("Received message from " + from + " :" + message);
                    }

                    @Override
                    public void onBye() {
                        rtpConnectionManager.stopStreaming();
                    }

                    @Override
                    public void onRemoteCancel() {

                    }

                    @Override
                    public void onRemoteDeclined() {
                    }

                    @Override
                    public void callConfirmed(String rtpHost, int rtpPort) {
                        try {
                            rtpConnectionManager.connect(new InetSocketAddress(rtpHost, rtpPort));
                        } catch (Exception e) {
                            System.out.println("Unable to connect: " + e);

                            e.printStackTrace();
                        }

//                        incomingCallScreen.hideWindow();
                    }

                    @Override
                    public void onUnavailable() {

                    }

                    @Override
                    public void onRinging(String from) {
//                        SwingUtilities.invokeLater(() -> {
//                            incomingCallScreen = new IncomingCallScreen(((LoginState) active).phone);
//                            incomingCallScreen.showWindow();
//                        });
                    }

                    @Override
                    public void onBusy() {

                    }

                    @Override
                    public void onRemoteAccepted() {

                    }

                    @Override
                    public void onRegistered() {
                        SwingUtilities.invokeLater(() -> {
                            if (active instanceof LoginState) {
                                enterState(new LoggedInState(((LoginState) active).phone));
                            }
                        });
                    }

                    @Override
                    public void onHangup() {
                        rtpConnectionManager.stopStreaming();
                    }

                    @Override
                    public void onTrying() {

                    }

                    @Override
                    public void authenticationFailed() {
                        System.out.println("Authentication failed :-(");
                        SwingUtilities.invokeLater(() -> {
                            if (!(active instanceof LoginState)) {
                                // TODO: WRONG
                                enterState(new LoginState(null));
                            }
                        });
                    }
                });

        // Handles changing of screens
        initGUI(inputLine);

        rtpConnectionManager = new RTPConnectionManager(inputLine, outputLine);
        rtpConnectionManager.init();

        audioManager = new AudioManager();

        sipManager.initialize();

    }

    public interface ApplicationState {
        void enter();

        void leave();
    }

    public class LoggedInState implements ApplicationState {
        private PhoneScreen screen;
        private PhoneAPI phone;

        public LoggedInState(PhoneAPI phone) {
            this.phone = phone;
        }

        public PhoneAPI getPhone() {
            return phone;
        }

        @Override
        public void enter() {
            screen = new PhoneScreen(phone);
            screen.showWindow();
        }

        @Override
        public void leave() {
            screen.hideWindow();
        }
    }

    public class LoginState implements ApplicationState {
        private LoginScreen screen;
        private PhoneAPI phone;

        public LoginState(PhoneAPI phone) {
            this.phone = phone;
        }

        @Override
        public void enter() {
            screen = new LoginScreen(phone);

            screen.showWindow();
        }

        @Override
        public void leave() {
            screen.hideWindow();
        }
    }

    private SoundAPI getSoundApi(){
        return new SoundAPI() {

            @Override
            public void startRecording() {
                audioManager.startRecordingWavFile();
            }

            @Override
            public void playRemoteSound(String file) {
                File audioFile = new File(file);
                try {

                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                    AudioInputStream lowResAudioStream = AudioSystem.getAudioInputStream(FORMAT, audioStream);

                    rtpConnectionManager.send(lowResAudioStream);
                } catch (IOException | UnsupportedAudioFileException e) {
                    System.out.println(" error while sending audio file " + e);
                }
            }

            @Override
            public void stopRecording() {
                audioManager.stopRecording();
            }

            @Override
            public void stopRemoteSound() {
                rtpConnectionManager.stopSend();
            }

            @Override
            public void mute(boolean muted) {
//                BooleanControl bc = (BooleanControl) inputLine.getControl(BooleanControl.Type.MUTE);
//                if (bc != null) {
//                    bc.setValue(true);
//                }
            }
        };
    }

    public void initGUI(TargetDataLine inputLine) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable e) {
                System.out.println("WARNING: unable to set look and feel, will continue");
            }

            enterState(new LoginState(new PhoneAPI() {
                String proxyAddress;

                @Override
                public void initiateCall(String sipAddress) {
                    sipManager.initiateCall(sipAddress, rtpConnectionManager.getSocket().getLocalPort());//todo get local rtp port here

                    audioCallScreen = new AudioCallScreen(this, getSoundApi());
                    audioCallScreen.showWindow();

                    proxyAddress = sipAddress;
                }

                @Override
                public void accept() {
                    sipManager.acceptCall(rtpConnectionManager.getSocket().getLocalPort()); //todo get local rtp port here
                }

                @Override
                public void reject() {
                    sipManager.reject();
                }


                @Override
                public void hangup() {
                    try {
                        sipManager.hangup(proxyAddress);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //audio call screen clear
                    audioCallScreen.hideWindow();
                }

                @Override
                public void register(String realm, String domain, String username, String password, String fromAddress) {
                    sipManager.login(realm, username, password, domain, fromAddress);
                }
            }));
        });
    }

    private ApplicationState active;

    private void enterState(ApplicationState loginState) {
        if (active != null) {
            active.leave();
            active = null;
        }

        active = loginState;

        if (active != null) {
            active.enter();
        }
    }
}
