package com.wisekrakr.communiwise.phone.audio.listener;





import com.wisekrakr.communiwise.config.Config;
import org.apache.commons.io.IOUtils;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class IncomingAudioListener {


    private final AudioFormat format;
    private final int remoteRtpPort;
    private final String server;

    private boolean talking = true;
    private SourceDataLine speaker;

    public IncomingAudioListener(AudioFormat format, int remoteRtpPort, String server) {
        this.format = format;
        this.remoteRtpPort = remoteRtpPort;
        this.server = server;
    }

    public void runListener(){


        try{
            System.out.println("Connecting to server:"+server+" Port:"+remoteRtpPort);

            InetAddress serverAddress = InetAddress.getByName(server);

            //TODO continue here
            DatagramSocket client = new DatagramSocket(remoteRtpPort);
            System.out.println("Connected client?: "+ client.isConnected());
            System.out.println("Listening for incoming audio on port." + client.getPort());
            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class,format);
            speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speaker.open(format);
            speaker.start();

            byte[] buffer = new byte[4096];

            while(talking){
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length, serverAddress, remoteRtpPort);

                client.receive(receivePacket);

                ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData());
                AudioInputStream ais = new AudioInputStream(bais,format,receivePacket.getLength());
                int bytesRead = 0;
                if((bytesRead = ais.read(buffer)) != -1){
//                    System.out.println("Writing to audio output.");
                    speaker.write(buffer,0,bytesRead);

                    //                 bais.reset();
                }
                ais.close();
                bais.close();

            }
//            speaker.drain();
//            speaker.close();
//            System.out.println("Stopped listening to incoming audio.");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void endListener(){
        try {
            if(speaker != null){
                speaker.drain();
                speaker.close();
                System.out.println("Stopped listening to incoming audio.");
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
