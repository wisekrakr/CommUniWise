package com.wisekrakr.communiwise.phone.audiovisualconnection.impl;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioClip  {

    private Clip clip;
    private final Mixer mixer;

    public AudioClip() {
        mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);
    }

    public void createClipURL(String path){
        try {

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path));

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
