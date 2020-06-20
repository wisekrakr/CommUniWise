package com.wisekrakr.communiwise.phone.audio.listener;

import com.wisekrakr.communiwise.config.Config;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static com.wisekrakr.communiwise.phone.audio.impl.ext.AudioAbstract.getAudioFormat;

public class AudioManager {
    // record duration, in milliseconds
    static final long RECORD_TIME = 60000;  // 1 minute

    // path of the wav file
    File wavFile = new File("test/RecordAudio input thread "+ "-" + Math.random() * 1000 + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    // the line from which audio data is captured
    TargetDataLine mic;
    SourceDataLine speaker;

    boolean servingInput;
    boolean servingOutput;


    public void startClient(){
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            mic = (TargetDataLine) AudioSystem.getLine(info);
            mic.open(format);
            mic.start();   // start capturing

            System.out.println("Start capturing client...");

            AudioInputStream ais = new AudioInputStream(mic);
            InputThread inputThread = new InputThread(this);
            InetAddress inetAddress = InetAddress.getByName(Config.LOCAL_IP);
            inputThread.setInputLine(mic);
            inputThread.setDatagramSocket(new DatagramSocket());
            inputThread.setServerIp(inetAddress);
            inputThread.setServerPort(Config.ANOTHER_RTP_PORT);

            inputThread.start();

            System.out.println("Start recording client...");

            servingInput = true;
//            // start recording
            AudioSystem.write(ais, fileType, wavFile);

        } catch (LineUnavailableException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public void startServer(String ipAddress, int rtpPort) {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(format);
            speaker.start();   // start capturing

            System.out.println("Start capturing server...");

            OutputThread outputThread = new OutputThread(this);
            outputThread.setOutputLine(speaker);
            SocketAddress serverAddress = new InetSocketAddress(ipAddress, rtpPort);
            DatagramSocket socket = new DatagramSocket();

            socket.connect(serverAddress);
            outputThread.setDatagramSocket(socket);
            outputThread.start();

            System.out.println("Start recording server...");

            servingOutput = true;



        } catch (LineUnavailableException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public void stopStreaming(){
        stopClient();
        stopServer();
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
