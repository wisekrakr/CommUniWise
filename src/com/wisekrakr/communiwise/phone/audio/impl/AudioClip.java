package com.wisekrakr.communiwise.phone.audio.impl;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class AudioClip  {

    private Clip clip;
    private Mixer mixer;

    public AudioClip() {
        mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);
    }

    public void createClipURL(String path){
        try {
            URL soundURL = new URL(path);

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);

            AudioFormat format = audioInputStream.getFormat();

            DataLine.Info dataInfo = new DataLine.Info(Clip.class, format);

            clip = (Clip)mixer.getLine(dataInfo);
            clip.open(audioInputStream);
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    public Clip getClip() {
        return clip;
    }
}
