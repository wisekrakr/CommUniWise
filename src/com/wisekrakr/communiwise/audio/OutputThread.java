package com.wisekrakr.communiwise.audio;

import com.wisekrakr.communiwise.SoundManager;

import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class OutputThread extends Thread{

    private SourceDataLine outputLine;
    private DatagramSocket datagramSocket;
    byte buff[] = new byte[512];

    private SoundManager soundManager;

    public OutputThread(SoundManager soundManager, SourceDataLine outputLine, DatagramSocket datagramSocket) {
        this.soundManager = soundManager;
        this.outputLine = outputLine;
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        int i = 0;
        DatagramPacket incoming = new DatagramPacket(buff,buff.length);

        while (soundManager.isServingOutput()){
            try {
                datagramSocket.receive(incoming);

                buff = incoming.getData();

                outputLine.write(buff, 0 , buff.length);

//                System.out.println("received #" + i++);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        outputLine.close();
        outputLine.drain();

        System.out.println("Output Thread finished");
    }


}
