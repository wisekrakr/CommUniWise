package com.wisekrakr.communiwise.phone.connections.threads;

import com.wisekrakr.communiwise.phone.audio.processing.g722.G722Decoder;
import com.wisekrakr.communiwise.phone.audio.processing.pcmu.PcmuDecoder;
import com.wisekrakr.communiwise.rtp.RTPPacket;
import com.wisekrakr.communiwise.rtp.RTPParser;

import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReceptionThread implements Runnable {
    private final SourceDataLine output;
    private final DatagramSocket socket;

    private G722Decoder g722Decoder = new G722Decoder();
    private PcmuDecoder pcmuDecoder = new PcmuDecoder();


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

            byte[] data = receivedPacket.getData();
            int offset = receivedPacket.getOffset();
            int length = receivedPacket.getLength();

            byte[] fragmentData = new byte[length];
            System.arraycopy(data, offset, fragmentData, 0, length);

            RTPPacket rtpPacket = RTPParser.decode(fragmentData);
            receivedRtpPacket(rtpPacket);

        }

        output.stop();
    }

    private void receivedRtpPacket(RTPPacket rtpPacket) {

        byte[] rawBuf = g722Decoder.decode(rtpPacket.getData(), rtpPacket.getData().length);
        output.write(rawBuf, 0, rawBuf.length);

//        if (codec.contains("PCMU")) {
//            byte[] rawBuf = pcmuDecoder.process(rtpPacket.getData());
//            output.write(rawBuf, 0, rawBuf.length);
//
//        } else if (codec.contains("G722")) {
//            byte[] rawBuf = g722Decoder.decode(rtpPacket.getData(), rtpPacket.getData().length);
//            output.write(rawBuf, 0, rawBuf.length);
//        }

    }
}

