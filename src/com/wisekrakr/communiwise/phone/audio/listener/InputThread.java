package com.wisekrakr.communiwise.phone.audio.listener;

import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class InputThread extends Thread {
    private TargetDataLine inputLine = null;
    private DatagramSocket datagramSocket;
    byte[] buff = new byte[4096];
    private InetAddress serverIp;
    private int serverPort;

    private final AudioManager audioManager;

    public InputThread(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public void run() {
        int i = 0;
        while (audioManager.isServingInput()) {

            try {
                inputLine.read(buff, 0, buff.length);
                DatagramPacket data = new DatagramPacket(buff, buff.length, serverIp, serverPort);

//                System.out.println("send #" + i++);
                datagramSocket.send(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        inputLine.close();
        inputLine.drain();

        System.out.println("Input Thread finished");
    }

    public TargetDataLine getInputLine() {
        return inputLine;
    }

    public void setInputLine(TargetDataLine inputLine) {
        this.inputLine = inputLine;
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public byte[] getBuff() {
        return buff;
    }

    public void setBuff(byte[] buff) {
        this.buff = buff;
    }

    public InetAddress getServerIp() {
        return serverIp;
    }

    public void setServerIp(InetAddress serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}