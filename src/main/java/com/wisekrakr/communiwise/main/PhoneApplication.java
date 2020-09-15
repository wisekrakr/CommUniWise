package com.wisekrakr.communiwise.main;


import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.layouts.CommandConsole;
import com.wisekrakr.communiwise.operations.DeviceImplementations;
import com.wisekrakr.communiwise.phone.audio.AudioManager;
import com.wisekrakr.communiwise.phone.connections.RTPConnectionManager;
import com.wisekrakr.communiwise.phone.sip.SipManager;
import com.wisekrakr.communiwise.phone.sip.SipManagerListener;
import com.wisekrakr.communiwise.user.ContactManager;
import com.wisekrakr.communiwise.user.SipAccountManager;
import com.wisekrakr.communiwise.user.history.CallInstance;
import org.apache.commons.cli.*;

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
        System.out.println(" =======>< CommUniWise activated ><======= ");

        CommandConsole commandConsole = new CommandConsole();
        commandConsole.start();


        Options options = new Options();

        CommandLine cmd = null;
        HelpFormatter formatter = new HelpFormatter();

        try {
            cmd = commandLineParser(options, args);
        } catch (Throwable t) {
            throw new IllegalStateException("Missing option(s) in program arguments", t);
        }

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        dataLineNotThere(cmd, "i",  options,mixers, formatter);
        dataLineNotThere(cmd, "o",  options,mixers, formatter);

        PhoneApplication application = new PhoneApplication();

        try {

            TargetDataLine inputLine = null;
            SourceDataLine outputLine = null;

            for (int i = 0; i < mixers.length; i++) {
                if (cmd.getOptionValue("i").equals(mixers[i].getName())) {
                    inputLine = (TargetDataLine) AudioSystem.getMixer(mixers[i]).getLine(new DataLine.Info(TargetDataLine.class, FORMAT));
                }
                if (cmd.getOptionValue("o").equals(mixers[i].getName())) {
                    outputLine = (SourceDataLine) AudioSystem.getMixer(mixers[i]).getLine(new DataLine.Info(SourceDataLine.class, FORMAT));
                }
            }

            if (inputLine == null) {

                formatter.printHelp("Input line not found " , options);
                dataLineNotThere(cmd, "i",  options,mixers, formatter);

                System.exit(1);
            }
            if (outputLine == null) {

                formatter.printHelp("Output line not found " ,options);
                dataLineNotThere(cmd, "o",  options,mixers, formatter);

                System.exit(1);
            }

            inputLine.open(FORMAT);
            outputLine.open(FORMAT);

            optionNotThere(cmd, "ip", " Please fill in your current IP Address", options, formatter);

            String localAddress = cmd.getOptionValue("ip");

            if(!commandConsole.getDomain().isEmpty()){
                application.initialize(
                        inputLine,
                        outputLine,
                        localAddress,
                        commandConsole.getDomain()
                );

                commandConsole.stop();
            }
        } catch (Exception e) {
            System.out.println("Unable to initialize: " + e);
            e.printStackTrace();

            System.exit(1);

            return;
        }

        application.run();

    }

    /**
     * Looks if there are no options missing in the program arguments.
     * If there are options missing then the app stops.
     * @param cmd Command line with all the options.
     * @param option the specific option = ip
     * @param message string that is shown if the option in missing
     * @param options the options object that holds all the options
     * @param formatter the helpformatter that will print a message
     */
    private static void optionNotThere(CommandLine cmd, String option, String message, Options options, HelpFormatter formatter){
        if(!cmd.hasOption(option)){
            formatter.printHelp("The -" + option + " is missing from the arguments", message, options,"Thank you for using a Wisekrakr product");
            System.exit(1);
        }
    }

    /**
     * Looks for audio devices when no devices have been put in the program arguments.
     * If no devices have been put, then the app stops.
     * @param cmd Command line with all the options.
     * @param option the specific audio option to search for (either o (output) or i (input)
     * @param options the options object that holds all the options
     * @param mixers  AudioSystem.getMixerInfo()
     * @param formatter the helpformatter that will print a message
     */
    private static void dataLineNotThere(CommandLine cmd,String option,Options options,Mixer.Info[] mixers,HelpFormatter formatter){

        if(!cmd.hasOption(option)){
            if(option.equals("i")){
                formatter.printHelp("Input device must be assigned", options);
            }
            if(option.equals("o")){
                formatter.printHelp("Output device must be assigned", options);
            }

            System.out.println("Available devices: ");
            for (int i = 0; i < mixers.length; i++) {
                System.out.println(String.format("%-50s %50s %30s %30s", mixers[i].getName(), mixers[i].getDescription(), mixers[i].getVersion(), mixers[i].getVendor()));
            }
            System.exit(1);
        }
    }

    private static CommandLine commandLineParser(Options options, String[] strings) throws ParseException {
        options
                .addOption("ip", "ipAddress", true, "Your IP Address")
                .addOption("i", "inputLine", true, "Audio input device for the target data line")
                .addOption("o", "outputLine", true, "Audio output device for the source data line");

        CommandLineParser parser = new DefaultParser();

        return parser.parse(options, strings);
    }


    private void run() {
    }

    private void initialize(TargetDataLine inputLine, SourceDataLine outputLine, String localAddress, String proxyHost) throws Exception {

        SipManager sipManager = new SipManager(proxyHost, 5060, localAddress, 5080, "udp").
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
