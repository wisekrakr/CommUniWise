package com.wisekrakr.communiwise.phone.audio.impl;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;

public abstract class AudioAbstract implements AudioContext{


    public static AudioFormat getAudioFormat() {
        float sampleRate = 8000;
        int sampleSizeInBits = 16;
        int channels = 1;

        return new AudioFormat(sampleRate, sampleSizeInBits,
                channels, true, false);
    }

    public AudioAbstract() {
    }


}
