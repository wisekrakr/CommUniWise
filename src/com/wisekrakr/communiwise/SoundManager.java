package com.wisekrakr.communiwise;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    // path of the wav file
    File wavFile = new File("H:/CODERING/SIP_dev/assets/RecordAudio" + Math.random() + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    // the line from which audio data is captured
    TargetDataLine inputLine;

    boolean isRecording;

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

    /**
     * Captures the sound and record into a WAV file
     */
    public void start() {
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

            System.out.println("Start capturing...");

            AudioInputStream ais = new AudioInputStream(inputLine);

            System.out.println("Start recording...");

            isRecording = true;
            // start recording
            AudioSystem.write(ais, fileType, wavFile);

        } catch (LineUnavailableException | IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Closes the target data line to finish capturing and recording
     */
    public void stop() {
        inputLine.stop();
        inputLine.close();

        isRecording = false;

        System.out.println("Finished");
    }

    public boolean isRecording() {
        return isRecording;
    }
}
