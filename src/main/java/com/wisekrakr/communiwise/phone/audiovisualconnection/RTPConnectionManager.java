package com.wisekrakr.communiwise.phone.audiovisualconnection;

import com.wisekrakr.communiwise.phone.audiovisualconnection.threads.ReceptionThread;
import com.wisekrakr.communiwise.phone.audiovisualconnection.threads.TransmittingThread;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RTPConnectionManager {
    private final TargetDataLine inputLine;
    private final SourceDataLine outputLine;
    private DatagramSocket socket;

    private final List<String> mixerNames = new ArrayList<>();


    private Thread receptionThread;
    private Thread transmitterThread;
    private TransmittingThread transmittingThread;

    public RTPConnectionManager(TargetDataLine inputLine, SourceDataLine outputLine) {
        this.inputLine = inputLine;
        this.outputLine = outputLine;
    }


    public void init() throws SocketException {
        this.socket = new DatagramSocket();

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixer : mixers) {
            mixerNames.add(mixer.getName());
        }

    }

/*
    public static AudioFormat format() {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, (16 / 8) * 2, 44100, false);
    //        new AudioFormat(44100, 16,
    //                2, true, true);

    }
*/

    public void connect(InetSocketAddress remoteAddress) throws IOException, LineUnavailableException {
        socket.connect(remoteAddress);


//        if (!AudioSystem.isLineSupported(speakerInfo)) {
//            throw new IllegalStateException("Line not supported by speaker");
//        }

//        output = (SourceDataLine) outputMixer.getLine(speakerInfo);
//        output.open(format());
//        output.start();   // start capturing


        receptionThread = new Thread(new ReceptionThread(outputLine, socket));
        receptionThread.setName("Reception thread");
        receptionThread.setDaemon(true);
        receptionThread.start();


//        DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format());

//        if (!AudioSystem.isLineSupported(micInfo)) {
//            throw new IllegalStateException("Line not supported by mic");
//        }
//        input = (TargetDataLine) AudioSystem.getLine(micInfo);
//        input.open(format());
//        input.start();   // start capturing

//        System.out.println("Start capturing client... " + socket.getLocalAddress());

//        AudioInputStream ais = new AudioInputStream(input);
//        transmitterThread = new Thread(new TransmitterThread(input, socket));
//        transmitterThread.setName("Transmitter thread");
//        transmitterThread.setDaemon(true);
//        transmitterThread.start();

        transmittingThread = new TransmittingThread(socket, inputLine);
        transmittingThread.start();

        System.out.println("Start recording client... connected: " + socket.isConnected());

        // start recording
        // AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File("test/RecordAudio input thread "+ "-" + Math.random() * 1000 + ".wav"));
    }


    public void stopStreaming() {
        receptionThread.interrupt();
        try {
            receptionThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        stopInput();
        stopOutput();

        socket.disconnect();
        socket.close();

        transmittingThread.stop();
    }

    private void stopInput() {
        inputLine.stop();
        inputLine.close();

        System.out.println("Finished Input");
    }

    private void stopOutput() {
        outputLine.stop();
        outputLine.close();

        System.out.println("Finished Output");
    }


    public List<String> getMixerNames() {
        return mixerNames;
    }

    public DatagramSocket getSocket() {
        return socket;
    }
}
