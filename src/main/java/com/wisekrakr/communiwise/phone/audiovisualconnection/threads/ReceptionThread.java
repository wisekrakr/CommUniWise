package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import com.wisekrakr.communiwise.phone.audiovisualconnection.util.Base64;
import org.jitsi.impl.neomedia.codec.audio.g722.JNIDecoderImpl;
import org.jitsi.impl.neomedia.codec.audio.g722.JNIEncoderImpl;

import javax.media.Buffer;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.wisekrakr.communiwise.phone.audiovisualconnection.AVConnectionStream.format;

public class ReceptionThread implements Runnable  {

    // path of the wav file
    File wavFile = new File("test/RecordAudio output thread"+ "-" + Math.random() * 1000 + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    private final SourceDataLine output;
    private final DatagramSocket socket;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public ReceptionThread(SourceDataLine output, DatagramSocket socket) {
        this.output = output;
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

        byte[] buffer = new byte[2500];

        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

        System.out.println("output socket info: " + socket.getLocalPort() + " " + socket.getPort() + " " + socket.getLocalSocketAddress() + " " + socket.getRemoteSocketAddress());

        while (running.get()) {
            try {

                socket.receive(receivedPacket);

                AudioInputStream ais = new AudioInputStream(
                         new ByteArrayInputStream(receivedPacket.getData(),0,receivedPacket.getLength()),
                        format(),receivedPacket.getLength());

//                JNIEncoderImpl encoder = new JNIEncoderImpl();
//                JNIDecoderImpl decoder = new JNIDecoderImpl();
//                Buffer b = new Buffer();



//              todo:  [ bytes written ] % [frame size in bytes ] == 0 ==>The number of bytes to write must represent an integral number of sample frames
                output.write(receivedPacket.getData(), 0, receivedPacket.getLength());

                System.out.println("Speaker is receiving data: " + receivedPacket.getLength());




//                    try {
//                        AudioSystem.write(ais, fileType, wavFile);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
//        output.close();
//        output.drain();

//        System.out.println("Output Thread finished");
    }


}
