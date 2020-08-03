package com.wisekrakr.communiwise.main;


import com.wisekrakr.communiwise.frames.layouts.AudioCallScreen;
import com.wisekrakr.communiwise.frames.layouts.IncomingCallScreen;
import com.wisekrakr.communiwise.frames.layouts.LoginScreen;
import com.wisekrakr.communiwise.frames.layouts.PhoneScreen;
import com.wisekrakr.communiwise.phone.audio.SoundAPI;
import com.wisekrakr.communiwise.phone.connections.RTPConnectionManager;
import com.wisekrakr.communiwise.phone.device.PhoneAPI;
import com.wisekrakr.communiwise.phone.managers.SipManager;
import com.wisekrakr.communiwise.phone.managers.SipManagerListener;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;

public class PhoneApplication implements Serializable {

    private static final AudioFormat FORMAT_SOURCE = new AudioFormat(16000, 16, 1, true, true); //G722 has 16000 samplerate
//    private static final AudioFormat FORMAT_TARGET = new AudioFormat(8000, 8, 1, true, false);
    private SipManager sipManager;

    private LoginScreen loginScreen;
    private PhoneScreen phoneScreen;
    private IncomingCallScreen incomingCallScreen;
    private AudioCallScreen audioCallScreen;

    private RTPConnectionManager rtpConnectionManager;
//    private AudioManager audioManager;

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

        PhoneApplication application = new PhoneApplication();
        try {
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();

            TargetDataLine inputLine = null;
            SourceDataLine outputLine = null;

            for (int i = 0; i < mixers.length; i++) {
                if (args[1].equals(mixers[i].getName())) {
                    inputLine = (TargetDataLine) AudioSystem.getMixer(mixers[i]).getLine(new DataLine.Info(TargetDataLine.class, FORMAT_SOURCE));
                }
                if (args[2].equals(mixers[i].getName())) {
                    outputLine = (SourceDataLine) AudioSystem.getMixer(mixers[i]).getLine(new DataLine.Info(SourceDataLine.class, FORMAT_SOURCE));
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

            int playBuffer = 100 * Math.max(10, 12);

            inputLine.open(FORMAT_SOURCE, playBuffer);
            outputLine.open(FORMAT_SOURCE);

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
                        rtpConnectionManager.stopStreamingAudio();
                    }

                    @Override
                    public void onRemoteCancel() {

                    }

                    @Override
                    public void onRemoteDeclined() {
                    }

                    @Override
                    public void callConfirmed(String rtpHost, int rtpPort, String codec) {
                        //todo codec?

                        try {
                            rtpConnectionManager.connectRTPAudio(new InetSocketAddress(rtpHost, rtpPort), codec);
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
                        rtpConnectionManager.stopStreamingAudio();
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
        initGUI();

        rtpConnectionManager = new RTPConnectionManager(inputLine, outputLine);
        rtpConnectionManager.init();

//        audioManager = new AudioManager(rtpConnectionManager.getSocket(), inputLine);

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

//                audioManager.startRecordingWavFile();
            }

            @Override
            public void playRemoteSound(String file) {
                File audioFile = new File(file);
                try {

                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                    AudioInputStream lowResAudioStream = AudioSystem.getAudioInputStream(FORMAT_SOURCE, audioStream);

//                    audioManager.startSendingAudio(lowResAudioStream);
                } catch (IOException | UnsupportedAudioFileException e) {
                    System.out.println(" error while sending audio file " + e);
                }
            }

            @Override
            public void stopRecording() {
//                audioManager.stopRecording();
            }

            @Override
            public void stopRemoteSound() {
//                audioManager.stopSendingAudio();
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

    public void initGUI() {
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
                    //todo codec
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
                    audioCallScreen.hideWindow();
                }

                @Override
                public void register(String realm, String domain, String username, String password, String fromAddress) {
                    sipManager.login(realm, username, password, domain, fromAddress);
                }

                @Override
                public int callStatus() {
                    return sipManager.getStatus();
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
