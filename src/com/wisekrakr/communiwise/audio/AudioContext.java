package com.wisekrakr.communiwise.audio;

import javax.sound.sampled.Mixer;

public abstract class AudioContext {
    private Mixer mixer;

    public AudioContext(Mixer mixer) {
        this.mixer = mixer;
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
