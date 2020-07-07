package com.wisekrakr.communiwise.phone.audiovisualconnection.impl;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class AudioClip {
    public static AudioInputStream loadClip(String path) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(AudioClip.class.getResourceAsStream(path));

        return audioInputStream;
    }

/*
        try {


        AudioFormat format = audioInputStream.getFormat();

        DataLine.Info dataInfo = new DataLine.Info(Clip.class, format);

        clip = (Clip)mixer.getLine(dataInfo);
        clip.open(audioInputStream);
    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
        e.printStackTrace();
    }
    public Clip getClip() {
        return clip;
    }

 */
}
