package com.wisekrakr.communiwise.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;

public abstract class AudioContext {
    private Mixer mixer;

    public AudioContext(Mixer mixer) {
        this.mixer = mixer;
    }

    public static AudioFormat getAudioFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 2;

        return new AudioFormat(sampleRate, sampleSizeInBits,
                channels, true, true);
    }

    public AudioContext() {
    }

    public Mixer getMixer() {
        return mixer;
    }

    public void setMixer(Mixer mixer) {
        this.mixer = mixer;
    }
}
