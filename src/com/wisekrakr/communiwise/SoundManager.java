package com.wisekrakr.communiwise;

import com.wisekrakr.communiwise.audio.InputThread;
import com.wisekrakr.communiwise.audio.OutputThread;
import com.wisekrakr.communiwise.config.Config;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SoundManager {
    // path of the wav file
    File wavFile = new File("H:/CODERING/SIP_dev/assets/RecordAudio" + Math.random() + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    // the line from which audio data is captured
    TargetDataLine inputLine;
    SourceDataLine outputLine;

    boolean servingInput;
    boolean servingOutput;



    /**
     * Defines an audio format
     */
    AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;

        return new AudioFormat(sampleRate, sampleSizeInBits,
                channels, true, true);
    }

    public void startClient() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            inputLine = (TargetDataLine) AudioSystem.getLine(info);
            inputLine.open(format);
            inputLine.start();   // start capturing

            System.out.println("Start capturing client...");

//            AudioInputStream ais = new AudioInputStream(inputLine);
            InputThread inputThread = new InputThread(this);
            InetAddress inetAddress = InetAddress.getByName(Config.LOCAL_IP);
            inputThread.setInputLine(inputLine);
            inputThread.setDatagramSocket(new DatagramSocket());
            inputThread.setServerIp(inetAddress);
            inputThread.setServerPort(Config.RTP_PORT);

            inputThread.start();

            System.out.println("Start recording client...");

            servingInput = true;
//            // start recording
//            AudioSystem.write(ais, fileType, wavFile);

        } catch (LineUnavailableException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public void startServer() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            outputLine = (SourceDataLine) AudioSystem.getLine(info);
            outputLine.open(format);
            outputLine.start();   // start capturing

            System.out.println("Start capturing server...");

//            AudioInputStream ais = new AudioInputStream(inputLine);
            OutputThread outputThread = new OutputThread(this);
            outputThread.setOutputLine(outputLine);
            outputThread.setDatagramSocket(new DatagramSocket(5060));
            outputThread.start();

            System.out.println("Start recording server...");

            servingInput = true;

        } catch (LineUnavailableException | IOException ex) {
            ex.printStackTrace();
        }
    }


    public void stopClient() {
        inputLine.stop();
        inputLine.close();

        servingInput = false;

        System.out.println("Finished Client");
    }

    public void stopServer() {
        outputLine.stop();
        outputLine.close();

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
