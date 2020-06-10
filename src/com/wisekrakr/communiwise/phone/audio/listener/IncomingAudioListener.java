package com.wisekrakr.communiwise.phone.audio.listener;





import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.net.*;
import java.util.Arrays;

public class IncomingAudioListener {


    private final AudioFormat format;
    private final int remoteRtpPort;
    private final String server;
    private final DatagramSocket socket;

    private boolean listening;
    private SourceDataLine speaker;

    public IncomingAudioListener(AudioFormat format, int remoteRtpPort, String server, DatagramSocket socket) {
        this.format = format;
        this.remoteRtpPort = remoteRtpPort;
        this.server = server;
        this.socket = socket;
    }

    public void runListener(){
        try{
//            System.out.println("Connecting to server:"+server+" Port:"+remoteRtpPort);

            InetAddress serverAddress = InetAddress.getByName(InetAddress.getByName(server).getHostAddress());

//            DatagramSocket client = new DatagramSocket(remoteRtpPort);
//            socket.connect(serverAddress, remoteRtpPort);
//            System.out.println("Connected client?: "+ client.isConnected());
            System.out.println("Listening to incoming audio on: " + socket.getRemoteSocketAddress());
            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class,format);
            speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speaker.open(format);
            speaker.start();

            byte[] buffer = new byte[4096];

            listening = true;
            while(listening){

                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

                socket.receive(receivedPacket);

                System.out.println("Received packets: " + receivedPacket.getLength());

//                ByteArrayInputStream bais = new ByteArrayInputStream(receivedPacket.getData());
//                AudioInputStream ais = new AudioInputStream(bais,format,receivedPacket.getLength());
//                int bytesRead = 0;
//
//                if((bytesRead = ais.read(buffer)) != -1){
//                    System.out.println("Writing to audio output " + bytesRead);
//                    speaker.write(buffer,0,bytesRead);
//
////                                     bais.reset();
//                }
//                ais.close();
//                bais.close();

            }
            speaker.drain();
            speaker.close();
            System.out.println("Stopped listening to incoming audio.");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void endListener(){
        try {
            if(speaker != null){
                speaker.drain();
                speaker.close();
                socket.close();
                listening = false;
                System.out.println("Stopped listening to incoming audio.");
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
