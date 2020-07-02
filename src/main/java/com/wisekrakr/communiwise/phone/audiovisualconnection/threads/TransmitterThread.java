package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import javax.media.ResourceUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jitsi.impl.neomedia.codec.audio.g722.*;

public class TransmitterThread implements Runnable {
    private final TargetDataLine input;
    private final DatagramSocket socket;

    private final AtomicBoolean running = new AtomicBoolean(false);

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

//                System.out.println("mic check: "  + Arrays.toString(data.getData()));

                socket.send(transmitPacket);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}