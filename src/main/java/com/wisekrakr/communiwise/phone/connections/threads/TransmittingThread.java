package com.wisekrakr.communiwise.phone.connections.threads;

import com.wisekrakr.communiwise.phone.audio.processing.g722.G722Encoder;
import com.wisekrakr.communiwise.rtp.RTPPacket;
import com.wisekrakr.communiwise.rtp.RTPParser;

import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Random;

public class TransmittingThread {
    private static final int PIPE_SIZE = 4096;
    private final DatagramSocket socket;
    private final TargetDataLine targetDataLine;

    private Thread captureThread;
    private Thread encoderThread;
    private Thread rtpSenderThread;

    private final G722Encoder g722Encoder = new G722Encoder(2000);

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
                        System.out.println("   Couldn't read what I wanted");

                        break;
                    }
                }

                rawDataOutput.close();

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
                    byte[] rawBuffer = new byte[BUFFER_SIZE];
                    byte[] encodingBuffer = new byte[BUFFER_SIZE];

                    while (!Thread.currentThread().isInterrupted()) {
                        int read = rawDataInput.read(rawBuffer);

                        int encoded = g722Encoder.encode(encodingBuffer, rawBuffer, read);

                        encodedDataOutput.write(encodingBuffer, 0, encoded);
                    }
                    encodedDataOutput.close();
                    rawDataInput.close();


                    System.out.println("Encoding thread has stopped");
                } catch (Throwable e) {
                    System.out.println("Encoding thread has stopped unexpectedly " + e);
                }
            }
        }, "Encoding thread");
        encoderThread.setDaemon(true);

        rtpSenderThread = new Thread(new Runnable() {

            int timestamp = 0;

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

                int targetSize = Math.min(1000, BUFFER_SIZE);

                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        int numBytesRead = 0;

                        while (numBytesRead < targetSize) {
                            numBytesRead += encodedDataInput.read(buffer, numBytesRead, buffer.length - numBytesRead);
                        }

//                        System.out.println("About to send " + numBytesRead + " bytes ");

                        rtpPacket.setData(Arrays.copyOf(buffer, numBytesRead));
                        rtpPacket.setSequenceNumber(sequenceNumber++);
                        rtpPacket.setTimestamp(timestamp);


                        send(rtpPacket);
                    }

                    encodedDataInput.close();

                    System.out.println("Sending thread has stopped");
                } catch (Throwable e) {
                    System.out.println("Sending thread has stopped unexpectedly " + e.getMessage());
                }
            }

            private void send(RTPPacket rtpPacket) {
                byte[] buf = RTPParser.encode(rtpPacket);
                timestamp += timestamp + buf.length;
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
