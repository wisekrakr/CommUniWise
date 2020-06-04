package com.wisekrakr.communiwise.audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class AudioClip extends AudioContext{

    private Clip clip;

    public AudioClip(Mixer mixer) {
        super(mixer);
    }

    public void createClipURL(String path){
        try {
            URL soundURL = new URL(path);

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);

            AudioFormat format = audioInputStream.getFormat();

            DataLine.Info dataInfo = new DataLine.Info(Clip.class, format);

            clip = (Clip)getMixer().getLine(dataInfo);
            clip.open(audioInputStream);
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    public Clip getClip() {
        return clip;
    }
}
