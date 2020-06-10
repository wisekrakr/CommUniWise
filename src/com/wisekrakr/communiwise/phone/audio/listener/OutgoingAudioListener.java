package com.wisekrakr.communiwise.phone.audio.listener;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class OutgoingAudioListener {

    private final AudioFormat format;
    private final String serverIp;
    private final int serverPort;
    private final DatagramSocket socket;

    boolean talking;
    private TargetDataLine mic;

    public OutgoingAudioListener(AudioFormat format, String serverIp, int serverPort, DatagramSocket socket) {
        this.format = format;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.socket = socket;
    }

    public void runListener(){
        try{

            System.out.println("Listening from mic.");
            InetAddress serverAddress = InetAddress.getByName(serverIp);
//            DatagramSocket serverSocket = new DatagramSocket();
//            socket.connect(serverAddress, serverPort);

//            ByteArrayOutputStream out = new ByteArrayOutputStream();


            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class,format);
            mic = (TargetDataLine) AudioSystem.getLine(micInfo);

            mic.open(format);
            System.out.println("Mic open.");

            byte[] tmpBuff = new byte[mic.getBufferSize()/5];

            mic.start();

            talking = true;

            while(talking) {

                int count = mic.read(tmpBuff,0,tmpBuff.length);

                DatagramPacket packet = new DatagramPacket(tmpBuff,count, serverAddress, serverPort );

                if (count > 0){
//                    System.out.println("Writing buffer to server " + Arrays.toString(packet.getData()));
//                    out.write(tmpBuff, 0, count);

                    socket.send(packet);
                }

            }
            mic.drain();
            mic.close();

            System.out.println("Stopped listening from mic.");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void endListener(){
        try {
            if(mic != null){
                mic.drain();
                mic.close();
                socket.close();

                talking = false;

                System.out.println("Stopped listening from mic.");
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
