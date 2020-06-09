package com.wisekrakr.communiwise.phone.audio.impl;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;

public abstract class AudioAbstract implements AudioContext{


    public static AudioFormat getAudioFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 2;

        return new AudioFormat(sampleRate, sampleSizeInBits,
                channels, true, true);
    }

    public AudioAbstract() {
    }


}
