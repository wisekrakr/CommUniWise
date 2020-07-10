package com.wisekrakr.communiwise.main;


import com.wisekrakr.communiwise.phone.audiovisualconnection.RTPConnectionManager;
import com.wisekrakr.communiwise.phone.audiovisualconnection.impl.AudioClip;
import com.wisekrakr.communiwise.phone.device.PhoneAPI;
import com.wisekrakr.communiwise.phone.managers.SipManagerListener;
import com.wisekrakr.communiwise.phone.managers.SipManager;
import com.wisekrakr.communiwise.screens.ext.AbstractScreen;
import com.wisekrakr.communiwise.screens.ext.ScreenState;
import com.wisekrakr.communiwise.screens.layouts.AudioCallScreen;
import com.wisekrakr.communiwise.screens.layouts.IncomingCallScreen;
import com.wisekrakr.communiwise.screens.layouts.LoginScreen;
import com.wisekrakr.communiwise.screens.layouts.PhoneScreen;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;

public class PhoneApplication implements Serializable {
    private Clip ringingClip;

    private SipManager sipManager;

    private LoginScreen loginScreen;
    private PhoneScreen phoneScreen;
    private IncomingCallScreen incomingCallScreen;
    private AudioCallScreen audioCallScreen;

    private RTPConnectionManager rtpConnectionManager;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Arguments: <local address>");
            System.exit(1);
        }

        String localAddress = args[0];

        PhoneApplication application = new PhoneApplication();
        try {
            application.initialize(
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

    private void initialize(String localAddress, int localPort, String transport, String proxyHost, int proxyPort) throws Exception {

        sipManager = new SipManager(proxyHost, proxyPort, localAddress, localPort, transport).
                logging("server.log", "debug.log", 16).
                listener(new SipManagerListener() {

                    @Override
                    public void onTextMessage(String message, String from) {
                        System.out.println("Received message from " + from + " :" + message);
                    }

                    @Override
                    public void onBye() {

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
                        SwingUtilities.invokeLater(() -> {
                            incomingCallScreen = new IncomingCallScreen(((LoginState) active).phone);
                            incomingCallScreen.showWindow();
                        });
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

                    /*
                    public void onSipMessage(final SipEvent sipEventObject) {
                        System.out.println("Sip Event fired: " + sipEventObject.type);
//        phoneScreen.showStatus();
                        switch (sipEventObject.type) {
                            case MESSAGE:
                                if (this.sipDeviceListener != null) {
                                    this.sipDeviceListener.onSipUAMessageArrived(new SipEvent(this, SipEvent.SipEventType.MESSAGE, sipEventObject.content, sipEventObject.from));
                                }
                                break;
                            case BYE:
                                RTPConnectionManager.stopStreaming();

                                if (this.sipConnectionListener != null) {
                                    this.sipConnectionListener.onSipUADisconnected(null);
                                }
                                break;
                            case REMOTE_CANCEL:
                                RTPConnectionManager.stopStreaming();

                                if (this.sipConnectionListener != null) {
                                    this.sipConnectionListener.onSipUACancelled(null);
                                }
                                break;
                            case DECLINED:
                                RTPConnectionManager.stopStreaming();

                                if (this.sipConnectionListener != null) {
                                    this.sipConnectionListener.onSipUADeclined(null);
                                }
                                break;
                            case BUSY_HERE:
                            case SERVICE_UNAVAILABLE:
                                RTPConnectionManager.stopStreaming();

                                break;

                            case CALL_CONNECTED:
                                try {
                                    RTPConnectionManager.connect(new InetSocketAddress(sipEventObject.rtpPort.getServer(), sipEventObject.rtpPort));
                                } catch (LineUnavailableException | IOException e) {
                                    e.printStackTrace();
                                }

                                if (this.sipConnectionListener != null) {
                                    this.sipConnectionListener.onSipUAConnected(null);
                                }
                                break;


                            case REMOTE_RINGING:
                                if (this.sipConnectionListener != null) {
                                    this.sipConnectionListener.onSipUAConnecting(null);
                                }

                                break;
                            case LOCAL_RINGING:
                                ringingClip.loop(Clip.LOOP_CONTINUOUSLY);


                                if (this.sipDeviceListener != null) {
                                    this.sipDeviceListener.onSipUAConnectionArrived(null);
                                }
                                break;

                        }

                    }    */

                });

        // Handles changing of screens
        initGUI();


        rtpConnectionManager = new RTPConnectionManager();
        rtpConnectionManager.init();


        AudioInputStream stream = AudioClip.loadClip("shake_bake.wav");
        AudioFormat format = stream.getFormat();
        DataLine.Info dataInfo = new DataLine.Info(Clip.class, format);
        ringingClip = (Clip) AudioSystem.getLine(dataInfo);
        ringingClip.open(stream);

//        sipManager.addUser("asterisk", sipUserName, sipPassword, proxyHost, sipAddress);
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


//    public void onScreenEventMessage(ScreenEvent screenEvent) {
//        System.out.println("Screen Event fired: " + screenEvent.type);
//        switch (screenEvent.type) {
//            case REGISTERED:
//                // TODO
////                if (sipProfile.isAuthenticated()) {
//                loginScreen.hideWindow();
//
//                setScreenState(ScreenState.PHONE);
////                }
//                break;
//
//
//            case UNREGISTERED:
//                break;
//            case INCOMING:
//                setScreenState(ScreenState.INCOMING);
//                break;
//            case AUDIO_CALLING:
//                setScreenState(ScreenState.AUDIO_CALL);
//                break;
//            case VIDEO_CALLING:
//                break;
//            case MESSAGING:
//                break;
//            case EXITING:
//                currentScreen.hideWindow();
//                break;
//        }
//        initGUI();
//    }




    /**
     * Handles changing of screens.
     * Creates the current screen, so that it can be destroyed when it is no longer used at a later stage.
     */
    public void initGUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable e) {
                System.out.println("WARNING: unable to set look and feel, will continue");
            }

            enterState(new LoginState(new PhoneAPI() {
                @Override
                public void initiateCall(String sipAddress) {
                    sipManager.initiateCall(sipAddress, rtpConnectionManager.getSocket().getLocalPort());//todo get local rtp port here

                    audioCallScreen = new AudioCallScreen(this);
                    audioCallScreen.showWindow();
                }

                @Override
                public void accept() {
                    sipManager.acceptCall(rtpConnectionManager.getSocket().getLocalPort()); //todo get local rtp port here
                    ringingClip.stop();

                }

                @Override
                public void reject() {
                    sipManager.reject();

                    ringingClip.stop();
                }


                @Override
                public void hangup() {
                    try {
                        sipManager.hangup();
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


/*        System.out.println("Setting up screen: " + screenState);

        //TODO: small frame for went we get an invite and we need to accept a call.
        switch (screenState) {
            case LOGIN:
                if (loginScreen == null) loginScreen = new LoginScreen(this);
                currentScreen = loginScreen;
                break;
            case INCOMING:
                if (incomingCallScreen == null) incomingCallScreen = new IncomingCallScreen(this);
                currentScreen = incomingCallScreen;
                break;
            case PHONE:
                if (phoneScreen == null) phoneScreen = new PhoneScreen(this);
                break;
            case AUDIO_CALL:
                if (audioCallScreen == null) audioCallScreen = new AudioCallScreen(this);
                currentScreen = audioCallScreen;
                break;
            case VIDEO_CALL:
                break;
            case MESSENGER:
                break;
            case BYE_BYE:
                break;
        }
*/
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
