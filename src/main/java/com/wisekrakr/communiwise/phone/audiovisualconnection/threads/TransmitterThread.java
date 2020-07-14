package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.PcmuEncoder;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.RTPPacket;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.RTPParser;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.RTPSender;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

@Deprecated
public class TransmitterThread implements Runnable {
    private final TargetDataLine input;
    private final DatagramSocket socket;
    private AudioFormat audioFormat;
    private final RTPParser rtpParser;


    private PipedInputStream rawDataInput;
    private PipedInputStream encodedDataInput;
    private RTPSender rtpSender;
    private PcmuEncoder encoder;


    public TransmitterThread(TargetDataLine input, DatagramSocket socket, AudioFormat audioFormat) {
        this.input = input;
        this.socket = socket;
        this.audioFormat = audioFormat;

        rtpParser = new RTPParser();
    }


    @Override
    public void run() {

        System.out.println("input socket info: " + socket.getLocalPort() + " " + socket.getPort() + " " + socket.getLocalSocketAddress() + " " + socket.getRemoteSocketAddress());

        input.start();

        try {
            captureRTPSender();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        input.stop();
    }

    public static final int PIPE_SIZE = 4096;

    private void captureRTPSender() throws IOException {

        CountDownLatch latch = new CountDownLatch(3);
        PipedOutputStream rawDataOutput = new PipedOutputStream();

        try {
            rawDataInput = new PipedInputStream(rawDataOutput, PIPE_SIZE);
        } catch (IOException e) {
            System.out.println("input/output error " + e.getMessage());
        }

        PipedOutputStream encodedDataOutput = new PipedOutputStream();

        try {
            encodedDataInput = new PipedInputStream(encodedDataOutput, PIPE_SIZE);
        } catch (IOException e) {
            System.out.println("input/output error " + e.getMessage());
            rawDataInput.close();

        }
        encoder = new PcmuEncoder(rawDataInput, encodedDataOutput,false, socket.getLocalAddress().getHostAddress(), latch);
        byte[] rawBuf = encoder.process(new byte[rawDataInput.available()]);

        RTPPacket rtpPacket = new RTPPacket();
        rtpPacket.setVersion(2);
        rtpPacket.setPadding(false);
        rtpPacket.setExtension(false);
        rtpPacket.setCsrcCount(0);
        rtpPacket.setMarker(false);
        rtpPacket.setPayloadType(0); //PCMU == 0   PCMA == 8    telephone-event == 101
        Random random = new Random();
        int sequenceNumber = random.nextInt();
        rtpPacket.setSequenceNumber(sequenceNumber);
        rtpPacket.setSsrc(random.nextInt());
        rtpPacket.setData(rawBuf);

        send(rtpPacket);

//        Thread encoderThread = new Thread(encoder, RTPSender.class.getSimpleName());
//        encoderThread.start();
//
//        rtpSender = new RTPSender(encodedDataInput,  socket.getLocalAddress().getHostAddress(), latch,this);
//
//        Thread rtpSenderThread = new Thread(rtpSender, RTPSender.class.getSimpleName());
//        rtpSenderThread.start();
    }

    public void send(RTPPacket rtpPacket) {
        if (socket == null) {
            return;
        }
        byte[] buf = rtpParser.encode(rtpPacket);
        final DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, socket.getInetAddress(), socket.getPort());

        System.out.println("..... Will send ......");

        if (!socket.isClosed()) {
            try {
                socket.send(datagramPacket);

                System.out.println("Mic is sending data: "  + datagramPacket.getLength());

            } catch (IOException | SecurityException e) {
                System.out.println(" error while sending datagram packet "+ e);
            }
        }
    }

}

