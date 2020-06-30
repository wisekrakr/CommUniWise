package com.wisekrakr.communiwise.phone.audio.listener;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static com.wisekrakr.communiwise.phone.audio.listener.AudioManager.FORMAT;

public class ReceptionThread extends Thread {

    // path of the wav file
    File wavFile = new File("test/RecordAudio output thread"+ "-" + Math.random() * 1000 + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    private SourceDataLine output;
    private DatagramSocket socket;

    public ReceptionThread(SourceDataLine output, DatagramSocket socket) {
        this.output = output;
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[2500];

        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

        System.out.println("output socket info: " + socket.getLocalPort() + " " + socket.getPort() + " " + socket.getLocalSocketAddress() + " " + socket.getRemoteSocketAddress());

        while (true) {
            try {
                int bytesRead = 0;
                //if((bytesRead = ais.read(receivedData)) != -1){
                    socket.receive(receivedPacket);

                     AudioInputStream ais = new AudioInputStream(
                             new ByteArrayInputStream(receivedPacket.getData(),0,receivedPacket.getLength()),
                             FORMAT,receivedPacket.getLength());

                    //receivedData = receivedPacket.getData();

                    output.write(receivedPacket.getData(), 0, receivedPacket.getLength());

                    System.out.println("Speaker is receiving data: " + receivedPacket.getLength());


//                    try {
//                        AudioSystem.write(ais, fileType, wavFile);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                //}
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
//        output.close();
//        output.drain();

//        System.out.println("Output Thread finished");
    }


}
