package com.wisekrakr.communiwise.main;


import com.wisekrakr.communiwise.gui.layouts.PhoneGUI;
import com.wisekrakr.communiwise.phone.audio.AudioManager;
import com.wisekrakr.communiwise.phone.connections.RTPConnectionManager;
import com.wisekrakr.communiwise.phone.device.DeviceImplementations;
import com.wisekrakr.communiwise.phone.managers.EventManager;
import com.wisekrakr.communiwise.phone.managers.SipManager;
import com.wisekrakr.communiwise.phone.managers.SipManagerListener;
import com.wisekrakr.communiwise.user.SipAccountManager;

import javax.sound.sampled.*;
import java.io.Serializable;
import java.net.InetSocketAddress;

public class PhoneApplication implements Serializable {

    private static final AudioFormat FORMAT = new AudioFormat(16000, 16, 1, true, true);

    private SipManager sipManager;
    private RTPConnectionManager rtpConnectionManager;
    private AudioManager audioManager;
    private SipAccountManager accountManager;
    private EventManager eventManager;

//    private DeviceImplementations impl;

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

            int playBuffer = 100 * Math.max(10, 12);

            inputLine.open(FORMAT, playBuffer);
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
                        rtpConnectionManager.stopStreamingAudio();

//                        audioCallGUI.hideWindow();
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
//                        if(sipManager.getSipSessionState() == SipSessionState.INCOMING){
//                            SwingUtilities.invokeLater(() -> {
//                                incomingCallGUI = new IncomingCallGUI(((LoginState) active).phone);
//                                incomingCallGUI.showWindow();
//                            });
//                        }

                    }

                    @Override
                    public void onBusy() {

                    }

                    @Override
                    public void onRemoteAccepted() {

                    }

                    @Override
                    public void onRegistered() {
                        //todo eventManager.registerSuccessful()
//                        SwingUtilities.invokeLater(() -> {
//                            if (active instanceof LoginState) {
//                                enterState(new LoggedInState(((LoginState) active).phone, impl.accountApiImpl(accountManager.getUserInfo())));
//                            }
//                        });


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
//                        SwingUtilities.invokeLater(() -> {
//                            if (!(active instanceof LoginState)) {
//                                // TODO: WRONG
//                                enterState(new LoginState(null));
//                            }
//                        });
                    }
                });

        // Handles changing of screens
//        initGUI();

        rtpConnectionManager = new RTPConnectionManager(inputLine, outputLine);
        rtpConnectionManager.init();

        accountManager = new SipAccountManager();

        audioManager = new AudioManager(rtpConnectionManager.getSocket(), inputLine, outputLine);

//        impl = new DeviceImplementations();

//        PhoneGUI phoneGUI = new PhoneGUI(
//                impl.phoneApiImpl(sipManager,rtpConnectionManager,audioManager,FORMAT),
//                impl.accountApiImpl(accountManager.getUserInfo()));
//        phoneGUI.showWindow();

        sipManager.initialize(accountManager);

        eventManager = new EventManager(sipManager, rtpConnectionManager, accountManager, audioManager);
        eventManager.open();
    }

//    public interface ApplicationState {
//        void enter();
//
//        void leave();
//    }
//
//    public class LoggedInState implements ApplicationState {
//        private PhoneGUI screen;
//        private PhoneAPI phone;
//        private AccountAPI account;
//
//
//        public LoggedInState(PhoneAPI phone, AccountAPI account) {
//            this.phone = phone;
//            this.account = account;
//        }
//
//        public PhoneAPI getPhone() {
//            return phone;
//        }
//
//        public AccountAPI getAccount() {
//            return account;
//        }
//
//        @Override
//        public void enter() {
//            screen = new PhoneGUI(phone, account);
//            screen.showWindow();
//        }
//
//        @Override
//        public void leave() {
//            screen.hideWindow();
//        }
//    }
//
//    public class LoginState implements ApplicationState {
//        private LoginGUI screen;
//        private PhoneAPI phone;
//
//        public LoginState(PhoneAPI phone) {
//            this.phone = phone;
//        }
//
//        @Override
//        public void enter() {
//            screen = new LoginGUI(phone);
//            screen.showWindow();
//        }
//
//        @Override
//        public void leave()  {
//            screen.hideWindow();
//        }
//    }
//
//    public void initGUI() {
//
//        SwingUtilities.invokeLater(() -> {
//            try {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            } catch (Throwable e) {
//                System.out.println("WARNING: unable to set look and feel, will continue");
//            }
//
//            enterState(new LoginState(impl.phoneApiImpl(sipManager, rtpConnectionManager,audioManager,FORMAT)));
//        });
//    }
//
//    private ApplicationState active;
//
//    private void enterState(ApplicationState loginState) {
//        if (active != null) {
//            active.leave();
//            active = null;
//        }
//
//        active = loginState;
//
//        if (active != null) {
//            active.enter();
//        }
//    }
}
