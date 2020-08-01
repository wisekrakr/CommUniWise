package com.wisekrakr.communiwise.phone.connections.threads;

import com.wisekrakr.communiwise.phone.audio.MicrophoneAnalyzer;
import com.wisekrakr.communiwise.phone.audio.processing.g722.G722Encoder;
import com.wisekrakr.communiwise.phone.audio.util.AudioUtil;
import com.wisekrakr.communiwise.rtp.RTPPacket;
import com.wisekrakr.communiwise.rtp.RTPParser;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Random;

public class TransmittingThread {
    private static final int PIPE_SIZE = 4096; // todo what size should it be? same as buffer size for the datagram packet?
    private final DatagramSocket socket;
    private final TargetDataLine targetDataLine;
    private final String codec;

    private Thread captureThread;
    private Thread encoderThread;
    private Thread rtpSenderThread;

    private final G722Encoder g722Encoder = new G722Encoder();

    public TransmittingThread(DatagramSocket socket, TargetDataLine targetDataLine, String codec) {
        this.socket = socket;
        this.targetDataLine = targetDataLine;
        this.codec = codec;
    }

    public void start() throws IOException {

        PipedOutputStream rawDataOutput = new PipedOutputStream();
        PipedInputStream rawDataInput = new PipedInputStream(rawDataOutput, PIPE_SIZE);

        PipedOutputStream encodedDataOutput = new PipedOutputStream();
        PipedInputStream encodedDataInput = new PipedInputStream(encodedDataOutput, PIPE_SIZE);

        captureThread = new Thread(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            try {
                targetDataLine.start();

                while (!Thread.currentThread().isInterrupted()) {
                    int actuallyRead = targetDataLine.read(buffer, 0, buffer.length);

                    rawDataOutput.write(buffer, 0, actuallyRead);

                    if (actuallyRead < buffer.length) {
                        break;
                    }
                }

                targetDataLine.stop();

                System.out.println("Capture thread has stopped");
            } catch (Throwable e) {
                System.out.println("Capture thread has stopped unexpectedly " + e.getMessage());
            }
        }, "Capture thread");
        captureThread.setDaemon(true);

        //todo targetDataLine level is -1. Should be between 0 - 1. Low volume.
        encoderThread = new Thread(new Runnable() {

            public void run() {
                try {
                    byte[] rawBuffer = new byte[BUFFER_SIZE];

                    while (!Thread.currentThread().isInterrupted()) {
                        int read = rawDataInput.read(rawBuffer);

//                        System.out.println(String.format("%-50s %50s %30s %30s", targetDataLine.getBufferSize(), targetDataLine.getFramePosition(),
//                                targetDataLine.getLevel(), targetDataLine.available()));

                        byte[] encodingBuffer = g722Encoder.encode(rawBuffer, BUFFER_SIZE);

                        encodedDataOutput.write(encodingBuffer, 0, encodingBuffer.length);

                    }


                    System.out.println("Encoding thread has stopped");
                } catch (Throwable e) {
                    System.out.println("Encoding thread has stopped unexpectedly " + e);
                }
            }
        }, "Encoding thread");
        encoderThread.setDaemon(true);

        rtpSenderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                RTPPacket rtpPacket = new RTPPacket();
                rtpPacket.setVersion(2);
                rtpPacket.setPadding(false);
                rtpPacket.setExtension(false);
                rtpPacket.setCsrcCount(0);
                rtpPacket.setMarker(false);
                rtpPacket.setPayloadType(9); //PCMU == 0   PCMA == 8    telephone-event == 101

                Random random = new Random();
                int sequenceNumber = random.nextInt();

                rtpPacket.setSequenceNumber(sequenceNumber);
                rtpPacket.setSsrc(random.nextInt());

                rtpPacket.setCsrcList(new long[rtpPacket.getCsrcCount()]);

                byte[] buffer = new byte[BUFFER_SIZE];

                int targetSize = 1;

                int timestamp = 0;
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        int numBytesRead = 0;

                        while (numBytesRead < targetSize) {
                            numBytesRead += encodedDataInput.read(buffer, numBytesRead, buffer.length - numBytesRead);
                        }

                        rtpPacket.setData(Arrays.copyOf(buffer, numBytesRead));
                        rtpPacket.setSequenceNumber(sequenceNumber++);

                        timestamp += numBytesRead;

                        rtpPacket.setTimestamp(timestamp);

                        send(rtpPacket);
                    }

                    System.out.println("Sending thread has stopped");
                } catch (Throwable e) {
                    System.out.println("Sending thread has stopped unexpectedly " + e.getMessage());
                }
            }

            private void send(RTPPacket rtpPacket) {
                byte[] buf = RTPParser.encode(rtpPacket);
                final DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, socket.getInetAddress(), socket.getPort());

                if (!socket.isClosed()) {
                    try {
                        socket.send(datagramPacket);
                    } catch (IOException | SecurityException e) {
                        System.out.println(" error while sending datagram packet " + e);
                    }
                }
            }
        }, "Send thread");
        rtpSenderThread.setDaemon(true);

        captureThread.start();
        encoderThread.start();
        rtpSenderThread.start();
    }

    public void stop() {
        captureThread.interrupt();
        encoderThread.interrupt();
        rtpSenderThread.interrupt();
    }

    public static final int SAMPLE_SIZE = 16;
    public static final int BUFFER_SIZE = SAMPLE_SIZE * 20;

}