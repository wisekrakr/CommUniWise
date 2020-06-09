package com.wisekrakr.communiwise.phone.audio.listener;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class OutgoingAudioListener {

    private final DatagramSocket serverSocket;
    private final AudioFormat format;

    boolean outVoice = true;
    private TargetDataLine mic;

    public OutgoingAudioListener(DatagramSocket serverSocket, AudioFormat format) {
        this.serverSocket = serverSocket;
        this.format = format;
    }

    public void runListener(){
        try{

            System.out.println("Listening from mic.");

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class,format);
            mic = (TargetDataLine) AudioSystem.getLine(micInfo);

            mic.open(format);
            System.out.println("Mic open.");

            byte[] tmpBuff = new byte[mic.getBufferSize()/5];

            mic.start();
            while(outVoice) {

                int count = mic.read(tmpBuff,0,tmpBuff.length);
                if (count > 0){
//                    System.out.println("Writing buffer to server.");
                    out.write(tmpBuff, 0, count);
                }
            }
//            mic.drain();
//            mic.close();
//
//            System.out.println("Stopped listening from mic.");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void endListener(){
        try {
            if(mic != null){
                mic.drain();
                mic.close();

                System.out.println("Stopped listening from mic.");
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
