package com.wisekrakr.communiwise.phone.audio.listener;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static com.wisekrakr.communiwise.phone.audio.impl.ext.AudioAbstract.getAudioFormat;

public class OutputThread extends Thread {

    // path of the wav file
    File wavFile = new File("test/RecordAudio output thread"+ "-" + Math.random() * 1000 + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    private SourceDataLine outputLine = null;
    private DatagramSocket datagramSocket;
    byte[] buff = new byte[4096];

    private final AudioManager audioManager;

    public OutputThread(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public void run() {
        int i = 0;
        DatagramPacket incoming = new DatagramPacket(buff, buff.length);

        while (audioManager.isServingOutput()) {
            ByteArrayInputStream bais = new ByteArrayInputStream(incoming.getData());
            AudioInputStream ais = new AudioInputStream(bais,getAudioFormat(),incoming.getLength());

            try {
                datagramSocket.receive(incoming);

                buff = incoming.getData();

                outputLine.write(buff, 0, buff.length);

                AudioSystem.write(ais, fileType, wavFile);
//                System.out.println("received #" + i++);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        outputLine.close();
        outputLine.drain();

        System.out.println("Output Thread finished");
    }

    public SourceDataLine getOutputLine() {
        return outputLine;
    }

    public void setOutputLine(SourceDataLine outputLine) {
        this.outputLine = outputLine;
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }
}
