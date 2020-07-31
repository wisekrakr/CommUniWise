package com.wisekrakr.communiwise.phone.audiovisualconnection;


import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioManager {

    private Thread recorder;
    private TargetDataLine targetDataLine;

    static AudioFormat WAV_FORMAT = new AudioFormat(16000, 8,2, true, true);
    static AudioFormat MP3_FORMAT = new AudioFormat(44100, 16,2, true, true);
    private File wavFile;

    public AudioManager(TargetDataLine targetDataLine) {
        this.targetDataLine = targetDataLine;
    }

    public void startRecordingWavFile(){
        wavFile = new File("src/main/resources/" + Math.random() * 1000 + ".wav");


        if(!Thread.currentThread().isInterrupted()){
            recorder = new Thread(()->{
                try {
//                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, WAV_FORMAT);
//                    targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
//                    targetDataLine.open(WAV_FORMAT);
//                    targetDataLine.start();

                    AudioInputStream ais = new AudioInputStream(targetDataLine);

                    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavFile);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        recorder.setDaemon(true);

        recorder.start();
    }

    public void convertWAVtoMP3File(String path){
        File wavFile = new File(path);
        File mp3File = new File("src/main/resources/" + path + ".mp3");

        byte[]buffer = new byte[1024];

        try {
            System.out.println("Converting wav file to mp3 .... " + path);

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(wavFile);
            AudioInputStream mp3AudioStream = AudioSystem.getAudioInputStream(MP3_FORMAT, audioStream);

            while (audioStream.read(buffer) != -1) {
                AudioFileFormat.Type[] audioTypes = AudioSystem.getAudioFileTypes();

                for (AudioFileFormat.Type audioType : audioTypes) {

                    if (audioType.toString().equals("MP3")){
                        AudioSystem.write(mp3AudioStream, audioType, mp3File);
                    }
                }

            }
        }catch (Exception e){
            System.out.println(" Unable to write mp3 file " + e);
        }

    }

    public void stopRecording(){
        System.out.println("Recording stopped for file " + wavFile.getName());

        recorder.interrupt();

//        targetDataLine.stop();
//        targetDataLine.close();

//        convertWAVtoMP3File(wavFile.getAbsolutePath());
    }
}
