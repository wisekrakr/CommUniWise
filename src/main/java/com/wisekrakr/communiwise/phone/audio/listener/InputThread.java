package com.wisekrakr.communiwise.phone.audio.listener;

import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class InputThread extends Thread {
    private TargetDataLine mic = null;
    private DatagramSocket socket;
    byte[] sendData;
    private InetAddress localIp;
    private int localRtpPort;

    private final AudioManager audioManager;

    public InputThread(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public void run() {
        System.out.println("input socket info: " + socket.getLocalPort() + " " + socket.getPort() + " " + socket.getLocalSocketAddress() + " " + socket.getRemoteSocketAddress());

        while (audioManager.isServingInput()) {

            try {
                mic.read(sendData, 0, sendData.length);
                DatagramPacket data = new DatagramPacket(sendData, sendData.length, 0, socket.getRemoteSocketAddress());

//                System.out.println("mic check: "  + Arrays.toString(data.getData()));

                socket.send(data);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mic.close();
        mic.drain();

        System.out.println("Input Thread finished");
    }

    public void setMic(TargetDataLine mic) {
        this.mic = mic;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public void setSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    public void setBuff(byte[] buff) {
        this.sendData = buff;
    }

    public void setLocalIp(InetAddress localIp) {
        this.localIp = localIp;
    }


    public void setLocalRtpPort(int localRtpPort) {
        this.localRtpPort = localRtpPort;
    }
}