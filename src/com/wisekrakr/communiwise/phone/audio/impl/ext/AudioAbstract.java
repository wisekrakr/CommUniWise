package com.wisekrakr.communiwise.phone.audio.impl.ext;

import javax.sound.sampled.AudioFormat;

public abstract class AudioAbstract implements AudioContext{

    public static AudioFormat getAudioFormat() {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 2;

//        return new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels,
//                (sampleSizeInBits / 8) * channels, sampleRate, false);
        return new AudioFormat(sampleRate, sampleSizeInBits,
                channels, true, true);
    }

    public AudioAbstract() {
    }


}
