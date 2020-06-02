package com.wisekrakr.communiwise.audio;

import javax.sound.sampled.TargetDataLine;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RecorderThread extends Thread {
    private TargetDataLine inputLine = null;
    private DatagramSocket datagramSocket;
    byte buff[] = new byte[512];
    private InetAddress serverIp;
}
