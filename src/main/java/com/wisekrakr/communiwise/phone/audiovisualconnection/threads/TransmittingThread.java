package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.g722.G722Codec;
import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.g722.G722CodecOld;
import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.pcmu.PcmuEncoder;
import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.utils.CodecUtil;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.RTPPacket;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.RTPParser;

import javax.sound.sampled.*;
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
    private String codec;

    private Thread captureThread;
    private Thread encoderThread;
    private Thread rtpSenderThread;
    private Thread audioFileThread;
    private Thread encodeFileThread;
    private Thread rtpFileSenderThread;
    protected double inEnergy;


    public TransmittingThread(DatagramSocket socket, TargetDataLine targetDataLine, String codec) {
        this.socket = socket;
        this.targetDataLine = targetDataLine;
        this.codec = codec;
    }

    // data from mic
    public short[] effectIn(short[] in) {
        double energy = 0;
        for (int i = 0; i < in.length; i++) {
            energy = energy + (Math.abs(in[i]));
        }
        inEnergy = energy / in.length;

        return in;
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
                    int encoded = 0;

                    while (!Thread.currentThread().isInterrupted()) {
                        int read = rawDataInput.read(rawBuffer);
//                        if(codec.contains("PCMU")) {
//                            encoded = PcmuEncoder.process(rawBuffer, encodingBuffer, 0, read);
//
//                        }else if(codec.contains("G722")){
//                            G722Codec g722Codec = new G722Codec();
//                            encoded = g722Codec.encode(rawBuffer);
//                        }






                        if(read != -1){
//                            G722Codec g722Codec = new G722Codec();
//
//                            short[] sframe = CodecUtil.bytesToShorts(new byte[100]);
//                            short[] seframe = effectIn(sframe);
//                            byte[] ret = new byte[161];
//                            byte[] tbuff = g722Codec.encode(seframe);
//
//                            System.out.println(" Encoded: " + tbuff.length);
////                            encoded = g722Codec.encode(ret, CodecUtil.bytesToShorts(rawBuffer));
//                            encodedDataOutput.write(tbuff, 0, tbuff.length);
//                            G722CodecOld g722CodecOld = new G722CodecOld();
//                            encodingBuffer = g722CodecOld.encode(CodecUtil.bytesToShorts(rawBuffer));
//                            encodedDataOutput.write(encodingBuffer, 0, encodingBuffer.length);
                        }
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

                rtpPacket.setCsrcList(new long[rtpPacket.getCsrcCount()]);

                byte[] buffer = new byte[BUFFER_SIZE]; //was 1024

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

    public void startSending(AudioInputStream audioStream) throws IOException {

        PipedOutputStream rawDataOutput = new PipedOutputStream();
        PipedInputStream rawDataInput = new PipedInputStream(rawDataOutput, PIPE_SIZE);

        PipedOutputStream encodedDataOutput = new PipedOutputStream();
        PipedInputStream encodedDataInput = new PipedInputStream(encodedDataOutput, PIPE_SIZE);

        audioFileThread = new Thread(()->{
            try {
                byte[] buffer = new byte[BUFFER_SIZE];

                int actuallyRead;

                while ((actuallyRead = audioStream.read(buffer)) != -1) {
                    rawDataOutput.write(buffer, 0, actuallyRead);

                    if (actuallyRead < buffer.length) {
                        break;
                    }
                }
            }catch (Throwable e){
                System.out.println("Audio file thread has stopped unexpectedly " + e.getMessage());
            }
        });
        audioFileThread.setDaemon(true);

        encodeFileThread = new Thread(new Runnable() {
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
        encodeFileThread.setDaemon(true);

        rtpFileSenderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO: research meaning of all fields
                RTPPacket rtpPacket = new RTPPacket();
                //This field identifies the version of RTP. The version defined by this specification is two (2).
                rtpPacket.setVersion(2);
                //If the padding bit is set, the packet contains one or more additional padding octets at the end which are not part of the payload.
                rtpPacket.setPadding(false);
                //If the extension bit is set, the fixed header is followed by exactly one header extension.
                rtpPacket.setExtension(false);
                //The CSRC count contains the number of CSRC identifiers that follow the fixed header.
                rtpPacket.setCsrcCount(0);
                //The interpretation of the marker is defined by a profile. It is intended to allow significant events such as frame boundaries to be marked in the packet stream.
                rtpPacket.setMarker(false);
                //This field identifies the format of the RTP payload and determines its interpretation by the application
                rtpPacket.setPayloadType(0); //PCMU == 0   PCMA == 8    telephone-event == 101

                Random random = new Random();
                int sequenceNumber = random.nextInt();

//              //The sequence number increments by one for each RTP data packet sent, and may be used by the receiver to detect packet loss and to restore packet sequence.
                rtpPacket.setSequenceNumber(sequenceNumber);
                //The SSRC field identifies the synchronization source.
                rtpPacket.setSsrc(random.nextInt());

                //     The CSRC list identifies the contributing sources for the payload contained in this packet. The number of identifiers is given by the CC field.
                //     If there are more than 15 contributing sources, only 15 may be identified. CSRC identifiers are inserted by mixers, using the SSRC identifiers
                //     of contributing sources.
                rtpPacket.setCsrcList(new long[rtpPacket.getCsrcCount()]); //todo is this needed?

                byte[] buffer = new byte[BUFFER_SIZE * 2];

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

                        //The timestamp reflects the sampling instant of the first octet in the RTP data packet.
                        // The sampling instant must be derived from a clock that increments monotonically and linearly in time to allow synchronization and jitter calculations
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

                        System.out.println("Wav data send: " + datagramPacket.getLength());

                    } catch (IOException | SecurityException e) {
                        System.out.println(" error while sending datagram packet " + e);
                    }
                }
            }
        }, "Send thread");
        rtpFileSenderThread.setDaemon(true);

        audioFileThread.start();
        encodeFileThread.start();
        rtpFileSenderThread.start();
    }

    public void interruptSending() {
        audioFileThread.interrupt();
        encodeFileThread.interrupt();
        rtpFileSenderThread.interrupt();
    }

    public void stop() {
        captureThread.interrupt();
        encoderThread.interrupt();
        rtpSenderThread.interrupt();
    }

    public static final int SAMPLE_SIZE = 16;
    public static final int BUFFER_SIZE = SAMPLE_SIZE * 20;




}
