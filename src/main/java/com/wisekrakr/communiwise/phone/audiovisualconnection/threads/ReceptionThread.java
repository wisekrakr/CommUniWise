package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceptionThread implements Runnable {
    private final SourceDataLine output;
    private final DatagramSocket socket;

    public ReceptionThread(SourceDataLine output, DatagramSocket socket) {
        this.output = output;
        this.socket = socket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[2500];
        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

        System.out.println("output socket info: " + socket.getLocalPort() + " " + socket.getPort() + " " + socket.getLocalSocketAddress() + " " + socket.getRemoteSocketAddress());

        output.start();
        while (true) {
            try {
                socket.receive(receivedPacket);
            } catch (IOException e) {
                System.out.println("Error while receiving: " + e.getMessage());
                break;
            }
/*
            AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(receivedPacket.getData(), 0, receivedPacket.getLength()),
                    format(), receivedPacket.getLength());


    // path of the wav file
    File wavFile = new File("test/RecordAudio output thread" + "-" + Math.random() * 1000 + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;


*/
            System.out.println("Received: " + receivedPacket.getLength());
//              todo:  [ bytes written ] % [frame size in bytes ] == 0 ==>The number of bytes to write must represent an integral number of sample frames
            output.write(receivedPacket.getData(), 0, receivedPacket.getLength());

//            System.out.println("Speaker is receiving data: " + receivedPacket.getLength());


//                    try {
//                        AudioSystem.write(ais, fileType, wavFile);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

        }

        output.stop();

        // TODO: deal with a stopping reception thread
//        output.close();
//        output.drain();

//        System.out.println("Output Thread finished");
    }


}
