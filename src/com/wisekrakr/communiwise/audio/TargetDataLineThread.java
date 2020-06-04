package com.wisekrakr.communiwise.audio;

import com.wisekrakr.communiwise.SoundManager;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;

public class TargetDataLineThread extends Thread{

    private SoundManager soundManager;

    private TargetDataLine inputLine;
    private ByteArrayOutputStream outputStream;

    public TargetDataLineThread(SoundManager soundManager, TargetDataLine inputLine, ByteArrayOutputStream outputStream) {
        this.soundManager = soundManager;
        this.inputLine = inputLine;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        int readBytes;

        inputLine.start();
        byte[] buffer = new byte[inputLine.getBufferSize() / 5];
        //serve
        while (soundManager.isServingAudio()){

            readBytes = inputLine.read(buffer, 0 , buffer.length);

            outputStream.write(buffer, 0 , readBytes);
        }
    }
}
