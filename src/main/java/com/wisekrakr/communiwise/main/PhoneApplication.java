package com.wisekrakr.communiwise.main;


import com.wisekrakr.communiwise.phone.audio.AudioManager;
import com.wisekrakr.communiwise.phone.audio.LineManager;
import com.wisekrakr.communiwise.phone.calling.CallInstance;
import com.wisekrakr.communiwise.phone.connections.RTPConnectionManager;
import com.wisekrakr.communiwise.operations.DeviceImplementations;
import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.phone.sip.SipManager;
import com.wisekrakr.communiwise.phone.sip.SipManagerListener;
import com.wisekrakr.communiwise.user.SipAccountManager;

import javax.sip.address.Address;
import javax.sound.sampled.*;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class PhoneApplication implements Serializable {

    private static final AudioFormat FORMAT = new AudioFormat(16000, 16, 1, true, true);

    private RTPConnectionManager rtpConnectionManager;
    private EventManager eventManager;

    private final Map<String, CallInstance> callInstances = new HashMap<>();


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
                    public void callConfirmed(String rtpHost, int rtpPort, String codec, String callId) {
                        //todo codec?
                        CallInstance callInstance = null;
                        InetSocketAddress proxyAddress = new InetSocketAddress(rtpHost, rtpPort);

                        try {
                            rtpConnectionManager.connectRTPAudio(proxyAddress, codec);
                        } catch (Throwable e) {
                            System.out.println("Unable to connect: " + e);

                            e.printStackTrace();
                        }


                        try {
                            callInstance = new CallInstance(callId, "bla", proxyAddress);
                            callInstances.put(callId, callInstance);
                        } catch (Throwable e) {
                            System.out.println("Unable to create call instance: " + e);

                            e.printStackTrace();
                        }

                        if (callInstance != null) {
                            eventManager.onOutgoingCall(callId);
                        }

                    }

                    @Override
                    public void onUnavailable() {

                    }

                    @Override
                    public void onRinging(String callId, Address address) {
                        CallInstance callInstance = null;
                        try {
                            callInstance = new CallInstance(callId, "bla", (InetSocketAddress) address);
                            callInstances.put(callId, callInstance);
                        } catch (Throwable e) {
                            System.out.println("Unable to create call instance: " + e);

                            e.printStackTrace();
                        }
                        eventManager.onIncomingCall(callId);

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
                    public void onHangup(String callId) {
                        eventManager.onHangUp(callId);
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

        LineManager lineManager = new LineManager();

        rtpConnectionManager = new RTPConnectionManager(inputLine, outputLine);
        rtpConnectionManager.init();

        SipAccountManager accountManager = new SipAccountManager();

        AudioManager audioManager = new AudioManager(rtpConnectionManager.getSocket(), inputLine, outputLine);

        sipManager.initialize(accountManager);

        DeviceImplementations impl = new DeviceImplementations(sipManager, rtpConnectionManager, accountManager, audioManager);

        eventManager = new EventManager(impl);
        eventManager.open();

    }

}
