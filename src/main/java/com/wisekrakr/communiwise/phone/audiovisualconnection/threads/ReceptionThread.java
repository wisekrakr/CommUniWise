package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.Decoder;
import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.PcmuDecoder;
import com.wisekrakr.communiwise.phone.rtp.RTPPacket;
import com.wisekrakr.communiwise.phone.rtp.RTPParser;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceptionThread implements Runnable {
    private final SourceDataLine output;
    private final DatagramSocket socket;
    private AudioFormat audioFormat;
    private RTPParser rtpParser;
    private Object sourceDataLineMutex;
    private FileOutputStream speakerInput;

    public ReceptionThread(SourceDataLine output, DatagramSocket socket, AudioFormat audioFormat) {
        this.output = output;
        this.socket = socket;
        this.audioFormat = audioFormat;

        rtpParser = new RTPParser(null);
        sourceDataLineMutex = new Object();
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

            try {
                byte[] data = receivedPacket.getData();
                int offset = receivedPacket.getOffset();
                int length = receivedPacket.getLength();
                byte[] trimmedData = new byte[length];

                System.arraycopy(data, offset, trimmedData, 0, length);

                RTPPacket rtpPacket = rtpParser.decode(trimmedData);

                receivedRtpPacket(rtpPacket);
            } catch (Exception e) {
                System.out.println("Error while receiving rtp packet: " + e.getMessage());

            }

//              todo:  [ bytes written ] % [frame size in bytes ] == 0 ==>The number of bytes to write must represent an integral number of sample frames
//            output.write(receivedPacket.getData(), 0, receivedPacket.getLength());

            System.out.println("Speaker is receiving data: " + receivedPacket.getLength());


        }

        output.stop();

        // TODO: deal with a stopping reception thread
//        output.close();
//        output.drain();

//        System.out.println("Output Thread finished");
    }

    private void receivedRtpPacket(RTPPacket rtpPacket) {
        Decoder decoder = new PcmuDecoder();
        byte[] rawBuf = decoder.process(rtpPacket.getData());
        output.write(rawBuf, 0, rawBuf.length);


    }

}

