package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.PcmuEncoder;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.RTPPacket;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.RTPParser;

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

    private Thread captureThread;
    private Thread encoderThread;
    private Thread rtpSenderThread;

    public TransmittingThread(DatagramSocket socket, TargetDataLine targetDataLine) {
        this.socket = socket;
        this.targetDataLine = targetDataLine;
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
        encoderThread = new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] rawBuffer = new byte[10000];
                    byte[] encodingBuffer = new byte[10000];

                    while (!Thread.currentThread().isInterrupted()) {
                        int read = rawDataInput.read(rawBuffer);

                        int encoded = PcmuEncoder.process(rawBuffer, encodingBuffer, 0, read);
                        encodedDataOutput.write(encodingBuffer, 0, encoded);
                    }

                    System.out.println("Encoding thread has stopped");
                } catch (Throwable e) {
                    System.out.println("Encoding thread has stopped unexpectedly " + e.getMessage());
                }
            }
        }, "Encoding thread");
        encoderThread.setDaemon(true);
        rtpSenderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO: research meaning of all fields
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

                byte[] buffer = new byte[1024];

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

                        System.out.println("Mic is sending data: " + datagramPacket.getLength()); //todo now:  datagramPacket.length == buf.length + 12 from encoded data

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


    /*


     */

}
