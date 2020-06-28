package com.wisekrakr.communiwise.phone.audio.listener;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import static com.wisekrakr.communiwise.phone.audio.impl.ext.AudioAbstract.getAudioFormat;

public class OutputThread extends Thread {

    // path of the wav file
    File wavFile = new File("test/RecordAudio output thread"+ "-" + Math.random() * 1000 + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    private SourceDataLine speaker = null;
    private DatagramSocket socket;
    byte[] receivedData;

    private final AudioManager audioManager;

    public OutputThread(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public void run() {
        DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
        ByteArrayInputStream bais = new ByteArrayInputStream(receivedPacket.getData());
        AudioInputStream ais = new AudioInputStream(bais,getAudioFormat(),receivedPacket.getLength());

        System.out.println("output socket info: " + socket.getLocalPort() + " " + socket.getPort() + " " + socket.getLocalSocketAddress() + " " + socket.getRemoteSocketAddress());

        while (audioManager.isServingOutput()) {
            try {
                int bytesRead = 0;
                if((bytesRead = ais.read(receivedData)) != -1){
                    socket.receive(receivedPacket);

                    receivedData = receivedPacket.getData();

                    speaker.write(receivedData, 0, bytesRead);

                    System.out.println("Speaker is receiving data: " + Arrays.toString(receivedPacket.getData()));


                    try {
                        AudioSystem.write(ais, fileType, wavFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        speaker.close();
        speaker.drain();

        System.out.println("Output Thread finished");
    }

    public void setSpeaker(SourceDataLine speaker) {
        this.speaker = speaker;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public void setBuff(byte[] receivedData) {
        this.receivedData = receivedData;
    }

}
