package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.Encoder;
import com.wisekrakr.communiwise.phone.audiovisualconnection.processing.PcmuEncoder;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.Capture;
import com.wisekrakr.communiwise.phone.audiovisualconnection.rtp.RTPSender;

import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.DatagramSocket;
import java.util.concurrent.CountDownLatch;

public class TransmittingThread {

    private static final int PIPE_SIZE = 4096; // todo what size should it be? same as buffer size for the datagram packet?

    private Capture capture;
    private Encoder encoder;
    private RTPSender rtpSender;

    public TransmittingThread(DatagramSocket socket, TargetDataLine targetDataLine) throws IOException {

        CountDownLatch latch = new CountDownLatch(3);
        PipedOutputStream rawDataOutput = new PipedOutputStream();
        PipedInputStream rawDataInput;
        try {
            rawDataInput = new PipedInputStream(rawDataOutput, PIPE_SIZE);
        } catch (IOException e) {
            System.out.println("input/output error " + e.getMessage());
            return ;
        }

        PipedOutputStream encodedDataOutput = new PipedOutputStream();
        PipedInputStream encodedDataInput;
        try {
            encodedDataInput = new PipedInputStream(encodedDataOutput, PIPE_SIZE);
        } catch (IOException e) {
            System.out.println("input/output error " + e.getMessage());
            rawDataInput.close();
            return ;
        }
        capture = new Capture(rawDataOutput, latch, targetDataLine);

        encoder = new PcmuEncoder(rawDataInput, encodedDataOutput,false, socket.getLocalAddress().getHostAddress(), latch);

        rtpSender = new RTPSender(encodedDataInput, latch, socket);

    }

    public void start(){
        capture.setStopped(false);
        encoder.setStopped(false);
        rtpSender.setStopped(false);

        Thread captureThread = new Thread(capture,
                Capture.class.getSimpleName());
        Thread encoderThread = new Thread(encoder,
                Encoder.class.getSimpleName());
        Thread rtpSenderThread = new Thread(rtpSender,
                RTPSender.class.getSimpleName());

        captureThread.start();
        encoderThread.start();
        rtpSenderThread.start();

    }

    public void stop() {
        if (capture != null) {
            capture.setStopped(true);
        }
        if (encoder != null) {
            encoder.setStopped(true);
        }
        if (rtpSender != null) {
            rtpSender.setStopped(true);
        }
    }
}
