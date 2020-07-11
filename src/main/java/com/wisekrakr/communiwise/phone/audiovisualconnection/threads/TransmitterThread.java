package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.Encoder;
import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.PcmuEncoder;
import com.wisekrakr.communiwise.phone.rtp.RTPPacket;
import com.wisekrakr.communiwise.phone.rtp.RTPParser;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class TransmitterThread implements Runnable {
    private final TargetDataLine input;
    private final DatagramSocket socket;
    private AudioFormat audioFormat;
    private final RTPParser rtpParser;


    private PipedInputStream rawDataInput;
    private PipedInputStream encodedDataInput;


    public TransmitterThread(TargetDataLine input, DatagramSocket socket, AudioFormat audioFormat) {
        this.input = input;
        this.socket = socket;
        this.audioFormat = audioFormat;

        rtpParser = new RTPParser(null);
    }


    @Override
    public void run() {

        System.out.println("input socket info: " + socket.getLocalPort() + " " + socket.getPort() + " " + socket.getLocalSocketAddress() + " " + socket.getRemoteSocketAddress());

        byte[] buffer = rtpParser.encode(createdRTPPacket());
        DatagramPacket transmitPacket = new DatagramPacket(buffer,buffer.length, socket.getInetAddress(), socket.getPort());

        input.start();
        while (true) {
            try {
                input.read(buffer, 0, buffer.length);

            } catch (Exception e) {
                System.out.println("Error while reading input: " + e.getMessage());
                break;
            }


//            System.out.println("Mic is receiving data: "  + transmitPacket.getLength());

            try {
                socket.send(transmitPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Mic is sending data: "  + transmitPacket.getLength());

        }
        input.stop();
    }

    private RTPPacket createdRTPPacket() {
        RTPPacket rtpPacket = new RTPPacket();
        rtpPacket.setVersion(2);
        rtpPacket.setPadding(false);
        rtpPacket.setExtension(false);
        rtpPacket.setCsrcCount(0);
        rtpPacket.setMarker(false);
        rtpPacket.setPayloadType(0);
        Random random = new Random();
        int sequenceNumber = random.nextInt();
        rtpPacket.setSequenceNumber(sequenceNumber);
        rtpPacket.setSsrc(random.nextInt());
        byte[] buffer = new byte[512];
        int timestamp = 0;
        int numBytesRead;
        int tempBytesRead;
        long sleepTime = 0;
        long offset = 0;
        long lastSentTime = System.nanoTime();
        // indicate if its the first time that we send a packet (dont wait)
        boolean firstTime = true;


        numBytesRead = 0;
        try {

            CountDownLatch latch = new CountDownLatch(3);
            PipedOutputStream rawDataOutput = new PipedOutputStream();

            try {
                rawDataInput = new PipedInputStream(rawDataOutput, buffer.length);
            } catch (IOException e) {
                System.out.println("input/output error " + e.getMessage());

            }

            PipedOutputStream encodedDataOutput = new PipedOutputStream();

            try {
                encodedDataInput = new PipedInputStream(encodedDataOutput, buffer.length);
            } catch (IOException e) {
                System.out.println("input/output error " + e.getMessage());
                rawDataInput.close();

            }
            Encoder encoder = new PcmuEncoder(rawDataInput, encodedDataOutput,  latch);
            Thread encoderThread = new Thread(encoder,
                    Encoder.class.getSimpleName());
            encoderThread.start();

            //todo train stops here
            tempBytesRead = encodedDataInput.read(buffer, numBytesRead,
                    buffer.length - numBytesRead);
            numBytesRead += tempBytesRead;

        } catch (IOException e) {
            System.out.println("input/output error" + e.getMessage());
        }
        byte[] trimmedBuffer;
        if (numBytesRead < buffer.length) {
            trimmedBuffer = new byte[numBytesRead];
            System.arraycopy(buffer, 0, trimmedBuffer, 0, numBytesRead);
        } else {
            trimmedBuffer = buffer;
        }
        rtpPacket.setData(trimmedBuffer);

        return rtpPacket;
    }
}

