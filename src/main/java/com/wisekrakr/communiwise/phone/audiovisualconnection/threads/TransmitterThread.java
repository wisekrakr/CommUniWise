package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.PcmuEncoder;

import javax.media.protocol.DataSource;
import javax.media.rtp.RTPManager;
import javax.sound.sampled.TargetDataLine;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransmitterThread implements Runnable {
    private final TargetDataLine input;
    private final DatagramSocket socket;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Socket s;
    private DataSource dataOutput;
    private RTPManager[] rtpMgrs;
    private PcmuEncoder encoder;
    private FileOutputStream rtpSenderInput;

    public TransmitterThread(TargetDataLine input, DatagramSocket socket) {
        this.input = input;
        this.socket = socket;
    }

    public void start() {
        Thread thread = new Thread(this);

        running.set(true);

        thread.start();
    }

    public void stop() {
        running.set(false);
    }

    @Override
    public void run() {

        System.out.println("input socket info: " + socket.getLocalPort() + " " + socket.getPort() + " " + socket.getLocalSocketAddress() + " " + socket.getRemoteSocketAddress());

        byte[] buffer = new byte[512];

        DatagramPacket transmitPacket = new DatagramPacket(buffer, buffer.length, 0, socket.getRemoteSocketAddress());

        while (running.get()) {

            try {
                input.read(buffer, 0, buffer.length);

                PipedOutputStream rawDataOutput = new PipedOutputStream();
                PipedInputStream rawDataInput;
                try {
                    rawDataInput = new PipedInputStream(rawDataOutput, buffer.length);
                } catch (IOException e) {
                    return;
                }
                PipedOutputStream encodedDataOutput = new PipedOutputStream();
                PipedInputStream encodedDataInput;
                try {
                    encodedDataInput = new PipedInputStream(encodedDataOutput,
                            buffer.length);
                } catch (IOException e) {
                    rawDataInput.close();
                    return;
                }

                encoder = new PcmuEncoder(rawDataInput, encodedDataOutput, new CountDownLatch(3));

                int numBytesRead = 0;
                try {
                    while (numBytesRead < buffer.length) {
                        // expect that the buffer is full
                        int tempBytesRead = encodedDataInput.read(buffer, numBytesRead,
                                buffer.length - numBytesRead);
                        numBytesRead += tempBytesRead;
                    }
                } catch (IOException e) {
                    return;
                }
                byte[] trimmedBuffer;
                if (numBytesRead < buffer.length) {
                    trimmedBuffer = new byte[numBytesRead];
                    System.arraycopy(buffer, 0, trimmedBuffer, 0, numBytesRead);
                } else {
                    trimmedBuffer = buffer;
                }
                if (true) {
                    try {
                        rtpSenderInput.write(trimmedBuffer);
                    } catch (IOException e) {
                        break;
                    }
                }


//                System.out.println("mic check: "  + Arrays.toString(data.getData()));

//                socket.send(transmitPacket);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

