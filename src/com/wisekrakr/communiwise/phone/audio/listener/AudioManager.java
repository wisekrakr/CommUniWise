package com.wisekrakr.communiwise.phone.audio.listener;

import com.wisekrakr.communiwise.config.Config;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.*;

import static com.wisekrakr.communiwise.phone.audio.impl.ext.AudioAbstract.getAudioFormat;


public class AudioManager {

    byte[] buff = new byte[512]; //4096

    // path of the wav file
    File wavFile = new File("test/RecordAudio input thread "+ "-" + Math.random() * 1000 + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    // the line from which audio data is captured
    TargetDataLine mic;
    SourceDataLine speaker;

    boolean servingInput;
    boolean servingOutput;

    private String remoteAddress;
    private int remotePort;
    private DatagramSocket socket;
    private AudioFormat format;


    public void start(String remoteAddress, int remotePort, DatagramSocket socket)  {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.socket = socket;

        format = getAudioFormat();


        startSpeaker();
        startMicrophone();

    }




    private void startMicrophone(){
        try {

            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(micInfo)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            mic = (TargetDataLine) AudioSystem.getLine(micInfo);
            mic.open(format);
            mic.start();   // start capturing

            System.out.println("Start capturing client... " + socket.getLocalAddress());

            AudioInputStream ais = new AudioInputStream(mic);
            InputThread inputThread = new InputThread(this);
            inputThread.setMic(mic);

            inputThread.setSocket(socket);
            inputThread.setLocalIp(socket.getLocalAddress());
            inputThread.setLocalRtpPort(socket.getLocalPort());
            inputThread.setBuff(buff);

//            socket.connect(socket.getLocalAddress(), socket.getLocalPort());

            inputThread.start();

            System.out.println("Start recording client... connected: " + socket.isConnected());

            servingInput = true;
//            // start recording
            AudioSystem.write(ais, fileType, wavFile);

        } catch (LineUnavailableException  ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startSpeaker() {
        try {

            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(speakerInfo)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speaker.open(format);
            speaker.start();   // start capturing

            System.out.println("Start capturing server...");

            OutputThread outputThread = new OutputThread(this);
            outputThread.setSpeaker(speaker);
            outputThread.setBuff(buff);

            //TODO: switch addresses with mic.

            InetSocketAddress serverAddress = new InetSocketAddress(remoteAddress, remotePort);
//            DatagramSocket socket = new DatagramSocket(remotePort);
            socket.connect(serverAddress);
            outputThread.setSocket(socket);

            System.out.println("Start recording server...");

            servingOutput = true;

            outputThread.start();

        } catch (LineUnavailableException  ex) {
            ex.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void stopStreaming(){
        stopClient();
        stopServer();

        socket.disconnect();
        socket.close();
    }

    private void stopClient() {
        mic.stop();
        mic.close();

        servingInput = false;

        System.out.println("Finished Client");
    }

    private void stopServer() {
        speaker.stop();
        speaker.close();

        servingOutput = false;

        System.out.println("Finished Server");
    }

    public boolean isServingInput() {
        return servingInput;
    }

    public boolean isServingOutput() {
        return servingOutput;
    }
}
