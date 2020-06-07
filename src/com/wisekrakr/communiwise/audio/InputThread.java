package com.wisekrakr.communiwise.audio;

import com.wisekrakr.communiwise.SoundManager;

import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class InputThread extends Thread {

    private InetAddress serverIp;
    private int serverPort;

    private final SoundManager soundManager;
    private final TargetDataLine inputLine;
    private final DatagramSocket datagramSocket;

    public InputThread(SoundManager soundManager, TargetDataLine inputLine, DatagramSocket datagramSocket) {
        this.soundManager = soundManager;
        this.inputLine = inputLine;
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {

        while (soundManager.isServingInput()){
            try {
                byte[] buff = new byte[inputLine.getBufferSize()/5];
                inputLine.read(buff, 0, buff.length);

                DatagramPacket data = new DatagramPacket(buff,buff.length, serverIp, serverPort);

//                System.out.println("send #" + readBytes);
                datagramSocket.send(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        inputLine.close();
        inputLine.drain();

        System.out.println("Input Thread finished");
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
