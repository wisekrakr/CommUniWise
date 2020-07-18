package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.Decoder;
import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.PcmuDecoder;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.RTPPacket;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.RTPParser;

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

            try {
                byte[] data = receivedPacket.getData();
                int offset = receivedPacket.getOffset();
                int length = receivedPacket.getLength();
                byte[] trimmedData = new byte[length];

                System.arraycopy(data, offset, trimmedData, 0, length);

                RTPPacket rtpPacket = RTPParser.decode(trimmedData);

                receivedRtpPacket(rtpPacket);
            } catch (Exception e) {
                System.out.println("Error while receiving rtp packet: " + e.getMessage());
            }
        }

        output.stop();
    }

    private void receivedRtpPacket(RTPPacket rtpPacket) {
        Decoder decoder = new PcmuDecoder();
        byte[] rawBuf = decoder.process(rtpPacket.getData());
        output.write(rawBuf, 0, rawBuf.length);
    }
}

