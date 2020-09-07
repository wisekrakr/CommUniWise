package com.wisekrakr.communiwise.main;


import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.operations.DeviceImplementations;
import com.wisekrakr.communiwise.phone.TimeKeeper;
import com.wisekrakr.communiwise.phone.audio.AudioManager;
import com.wisekrakr.communiwise.phone.connections.RTPConnectionManager;
import com.wisekrakr.communiwise.phone.sip.SipManager;
import com.wisekrakr.communiwise.phone.sip.SipManagerListener;
import com.wisekrakr.communiwise.user.ContactManager;
import com.wisekrakr.communiwise.user.SipAccountManager;
import com.wisekrakr.communiwise.user.history.CallInstance;

import javax.sip.address.Address;
import javax.sound.sampled.*;
import java.io.Serializable;
import java.net.InetSocketAddress;

public class PhoneApplication implements Serializable {

    private static final AudioFormat FORMAT = new AudioFormat(16000, 16, 1, true, true);

    private RTPConnectionManager rtpConnectionManager;
    private EventManager eventManager;

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

        SipManager sipManager = new SipManager(proxyHost, proxyPort, localAddress, localPort, transport).
                logging("server.log", "debug.log", 16).
                listener(new SipManagerListener() {


                    @Override
                    public void onTextMessage(String message, String from) {
                        System.out.println("Received message from " + from + " :" + message);
                    }

                    @Override
                    public void onRemoteBye(CallInstance callInstance) {

                        eventManager.onHangUp(callInstance);

                        rtpConnectionManager.stopStreamingAudio();
                    }

                    @Override
                    public void onRemoteCancel(CallInstance callInstance) {
                        eventManager.onHangUp(callInstance);
                    }

                    @Override
                    public void onRemoteDeclined() {

                    }

                    @Override
                    public void callConfirmed(CallInstance callInstance) {
                        try {
                            rtpConnectionManager.connectRTPAudio(callInstance.getProxyAddress());

                        } catch (Throwable e) {
                            throw new IllegalStateException("Unable to connect call", e);
                        }

                        eventManager.onOutgoingCall(callInstance);

                    }

                    @Override
                    public void onUnavailable() {

                    }

                    @Override
                    public void onRinging(CallInstance callInstance) {
                        eventManager.onIncomingCall(callInstance);
                    }

                    @Override
                    public void onAccepted(CallInstance callInstance, int remoteRtpPort) {

                        InetSocketAddress address = new InetSocketAddress(callInstance.getProxyAddress().getAddress(), remoteRtpPort);
                        try {
                            rtpConnectionManager.connectRTPAudio(address);
                        } catch (Throwable e) {
                            System.out.println("Unable to connect: " + e);

                            e.printStackTrace();
                        }

                        eventManager.onAcceptingCall(callInstance);

                    }

                    @Override
                    public void onDeclined(String callId) {
                        eventManager.onDecliningCall(callId);
                    }

                    @Override
                    public void onNotFound(Address proxyAddress) {
                        eventManager.onNotFound(proxyAddress);
                    }

                    @Override
                    public void onBusy() {

                    }

                    @Override
                    public void onRemoteAccepted() {
                    }

                    @Override
                    public void onRegistered() {
                        eventManager.onRegistered();
                    }

                    @Override
                    public void onBye(CallInstance callInstance) {
                        eventManager.onHangUp(callInstance);
                    }

                    @Override
                    public void onTrying() {
                    }

                    @Override
                    public void authenticationFailed() {
                        System.out.println("Authentication failed :-(");
                        eventManager.onAuthenticationFailed();
                    }
                });

        rtpConnectionManager = new RTPConnectionManager(inputLine, outputLine);
        rtpConnectionManager.init();

        SipAccountManager accountManager = new SipAccountManager();

        AudioManager audioManager = new AudioManager(rtpConnectionManager.getSocket(), inputLine, outputLine);

        ContactManager contactManager = new ContactManager();

        sipManager.initialize(accountManager,contactManager);

        DeviceImplementations impl = new DeviceImplementations(sipManager, rtpConnectionManager, accountManager,audioManager,contactManager);

        eventManager = new EventManager(impl);
        eventManager.open();

    }

}
