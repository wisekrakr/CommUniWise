package com.wisekrakr.communiwise.audio;

import com.wisekrakr.communiwise.SoundManager;

import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;

public class SourceDataLineThread extends Thread{

    private SoundManager soundManager;

    private SourceDataLine outputLine;
    private ByteArrayOutputStream outputStream;

    public SourceDataLineThread(SoundManager soundManager, SourceDataLine outputLine, ByteArrayOutputStream outputStream) {
        this.soundManager = soundManager;
        this.outputLine = outputLine;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        outputLine.start();
        //serve

        while (soundManager.isServingAudio()){

            outputLine.write(outputStream.toByteArray(), 0 , outputStream.size());

        }
    }
}
