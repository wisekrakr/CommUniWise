package com.wisekrakr.communiwise.phone.connections.threads;

import com.wisekrakr.communiwise.phone.audio.processing.g722.G722Encoder;
import com.wisekrakr.communiwise.rtp.RTPPacket;
import com.wisekrakr.communiwise.rtp.RTPParser;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Random;

public class RemoteAudioPlayThread {
    private static final int PIPE_SIZE = 4096;

    private final DatagramSocket socket;

    private Thread audioFileThread;
    private Thread encodeFileThread;
    private Thread rtpFileSenderThread;

    private final G722Encoder g722Encoder = new G722Encoder(1000);

    public RemoteAudioPlayThread(DatagramSocket socket) {

        this.socket = socket;
    }

    public DatagramSocket getSocket() {
        return socket;
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
                audioStream.close();
                rawDataOutput.close();
            }catch (Throwable e){
                System.out.println("Audio file thread has stopped unexpectedly " + e.getMessage());
            }

        });
        audioFileThread.setDaemon(true);

        encodeFileThread = new Thread(new Runnable() {
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

                    System.out.println("Encoding WAV File thread has stopped");
                } catch (Throwable e) {
                    System.out.println("Encoding WAV File thread has stopped unexpectedly " + e.getMessage());
                }
            }
        }, "Encoding thread");
        encodeFileThread.setDaemon(true);

        rtpFileSenderThread = new Thread(new Runnable() {

            int timestamp = 0;

            @Override
            public void run() {
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
                rtpPacket.setPayloadType(9); //PCMU == 0   PCMA == 8    telephone-event == 101   G722 = 9

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

                byte[] buffer = new byte[BUFFER_SIZE];

                int targetSize = Math.min(1000, BUFFER_SIZE);

                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        int numBytesRead = 0;

                        while (numBytesRead < targetSize) {
                            numBytesRead += encodedDataInput.read(buffer, numBytesRead, buffer.length - numBytesRead);
                        }

                        rtpPacket.setData(Arrays.copyOf(buffer, numBytesRead));
                        rtpPacket.setSequenceNumber(sequenceNumber++);
                        rtpPacket.setTimestamp(timestamp);


                        send(rtpPacket);

                        //The timestamp reflects the sampling instant of the first octet in the RTP data packet.
                        // The sampling instant must be derived from a clock that increments monotonically and linearly in time to allow synchronization and jitter calculations

                    }
                    encodedDataInput.close();

                    System.out.println("Sending WAV File thread has stopped");
                } catch (Throwable e) {
                    System.out.println("Sending WAV File thread has stopped unexpectedly " + e.getMessage());
                }
            }

            private void send(RTPPacket rtpPacket) {
                byte[] buf = RTPParser.encode(rtpPacket);
                timestamp += timestamp + buf.length;
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

    public void stopSending() {
        audioFileThread.interrupt();
        encodeFileThread.interrupt();
        rtpFileSenderThread.interrupt();
    }

    public static final int SAMPLE_SIZE = 16;
    public static final int BUFFER_SIZE = SAMPLE_SIZE * 20;
}
