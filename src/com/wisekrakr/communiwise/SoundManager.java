package com.wisekrakr.communiwise;

import com.wisekrakr.communiwise.audio.*;
import com.wisekrakr.communiwise.config.Config;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SoundManager{
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




    public void startAudioThread(){
        AudioFormat format = AudioContext.getAudioFormat();

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
//            Thread.sleep(5000);
//            inputLine.stop();
//            inputLine.close();
//
//            System.out.println("Target Thread Stopped");

            System.out.println("Source Thread started");

            sourceDataLineThread.start();
//            Thread.sleep(5000);
//            outputLine.stop();
//            outputLine.close();
//
//            System.out.println("Source Thread stopped");


        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void stopAudioThread() {
        inputLine.stop();
        inputLine.close();

        outputLine.stop();
        outputLine.close();
    }


    public void startClient() {

        AudioFormat format = AudioContext.getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        // checks if system supports the data line
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line not supported");
            System.exit(0);
        }

        try {
            inputLine = (TargetDataLine) AudioSystem.getLine(info);
            inputLine.open(format);
            inputLine.start();   // start capturing

            System.out.println("Start capturing client...");
            servingInput = true;

//            AudioInputStream ais = new AudioInputStream(inputLine);
            InputThread inputThread = new InputThread(this, inputLine,new DatagramSocket());
            InetAddress inetAddress = InetAddress.getByName(Config.LOCAL_IP);
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
            AudioFormat format = AudioContext.getAudioFormat();
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

            OutputThread outputThread = new OutputThread(this, outputLine,new DatagramSocket(Config.RTP_PORT));

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
