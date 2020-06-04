package com.wisekrakr.communiwise.audio;

import com.wisekrakr.communiwise.SoundManager;

import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class InputThread extends Thread {

    private DatagramSocket datagramSocket;

    private InetAddress serverIp;
    private int serverPort;

    private final SoundManager soundManager;
    private TargetDataLine inputLine;

    public InputThread(SoundManager soundManager, TargetDataLine inputLine) {
        this.soundManager = soundManager;
        this.inputLine = inputLine;
    }

    @Override
    public void run() {
        int readBytes;
        while (soundManager.isServingInput()){

            try {
                byte[] buff = new byte[inputLine.getBufferSize()/5];
                readBytes = inputLine.read(buff, 0, buff.length);

                DatagramPacket data = new DatagramPacket(buff,buff.length, serverIp, serverPort);

                System.out.println("send #" + readBytes);
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
