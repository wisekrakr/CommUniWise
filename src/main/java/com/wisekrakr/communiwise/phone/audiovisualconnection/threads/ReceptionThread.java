package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.g722.Codec;
import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.g722.G722Codec;
import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.g722.G722CodecOld;
import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.utils.CodecUtil;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.RTPPacket;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.RTPParser;

import javax.sound.sampled.SourceDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ReceptionThread implements Runnable {
    private final SourceDataLine output;
    private final DatagramSocket socket;
    private String codec;
    private short[] rawBuf;


    public ReceptionThread(SourceDataLine output, DatagramSocket socket, String codec) {
        this.output = output;
        this.socket = socket;
        this.codec = codec;

        rawBuf = new short[160];
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

                receivedRtpPacket(rtpPacket, offset);
            } catch (Exception e) {
                System.out.println("Error while receiving rtp packet: " + e);
            }
        }

        output.stop();
    }

    private void receivedRtpPacket(RTPPacket rtpPacket, int offset) {


//        if(codec.contains("PCMU")) {
//            PcmuDecoder pcmuDecoder = new PcmuDecoder();
//            rawBuf = pcmuDecoder.process(rtpPacket.getData());
//
//        }else if(codec.contains("G722")){
//            G722Codec g722Codec = new G722Codec();
//            rawBuf = g722Codec.process(rtpPacket.getData());
//        }

        G722Codec g722Codec = new G722Codec();
        short[] rawBuf = g722Codec.decode(rtpPacket.getData());

        byte[] rawBytes = CodecUtil.shortsToBytes(rawBuf);

//        G722CodecOld g722Codec = new G722CodecOld();
//        rawBuf = g722Codec.decode(rtpPacket.getData(),offset);
//
//        byte[] rawBytes = CodecUtil.shortsToBytes(rawBuf);
//


        System.out.println("    raw    " + rawBytes.length);

        output.write(rawBytes, 0, rawBytes.length);
    }
}

