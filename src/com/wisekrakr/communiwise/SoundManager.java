package com.wisekrakr.communiwise;

import com.wisekrakr.communiwise.audio.SourceDataLineThread;
import com.wisekrakr.communiwise.audio.InputThread;
import com.wisekrakr.communiwise.audio.OutputThread;
import com.wisekrakr.communiwise.audio.TargetDataLineThread;
import com.wisekrakr.communiwise.config.Config;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
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
    boolean servingAudio;



    /**
     * Defines an audio format
     */
    AudioFormat getAudioFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 2;

        return new AudioFormat(sampleRate, sampleSizeInBits,
                channels, true, true);
    }

    public void startAudioThread(){
        AudioFormat format = getAudioFormat();

        try {
            DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
            outputLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            outputLine.open();

            DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
            inputLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            inputLine.open();

            servingAudio = true;

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            TargetDataLineThread targetDataLineThread = new TargetDataLineThread(this, inputLine, outputStream);
            SourceDataLineThread sourceDataLineThread = new SourceDataLineThread(this, outputLine, outputStream);

            System.out.println("Target Thread started");

            targetDataLineThread.start();
            Thread.sleep(5000);
            inputLine.stop();
            inputLine.close();

            System.out.println("Target Thread Stopped");

            System.out.println("Source Thread started");

            sourceDataLineThread.start();
            Thread.sleep(5000);
            outputLine.stop();
            outputLine.close();

            System.out.println("Source Thread stopped");


        } catch (LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startClient() {

        AudioFormat format = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        // checks if system supports the data line
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line not supported");
            System.exit(0);
        }

        try {
            inputLine = (TargetDataLine) AudioSystem.getLine(info);
            inputLine.open(); //open format?
            inputLine.start();   // start capturing

            System.out.println("Start capturing client...");
            servingInput = true;

//            AudioInputStream ais = new AudioInputStream(inputLine);
            InputThread inputThread = new InputThread(this, inputLine);
            InetAddress inetAddress = InetAddress.getByName(Config.LOCAL_IP);
            inputThread.setInputLine(inputLine);
            inputThread.setDatagramSocket(new DatagramSocket());
            inputThread.setServerIp(inetAddress);
            inputThread.setServerPort(Config.RTP_PORT);

            inputThread.start();

            System.out.println("Start recording client...");




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
            servingOutput = true;

            OutputThread outputThread = new OutputThread(this);
            outputThread.setOutputLine(outputLine);
            outputThread.setDatagramSocket(new DatagramSocket(33060));
            outputThread.start();

            System.out.println("Start recording server...");



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

    public boolean isServingAudio() {
        return servingAudio;
    }
}
