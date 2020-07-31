package com.wisekrakr.communiwise.phone.messaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class ReadThread implements Runnable{
    private DatagramSocket socket;
    private InetAddress group;
    private int port;
    private static final int MAX_LEN = 1000;
    public ReadThread(DatagramSocket socket,InetAddress group,int port){
        this.socket = socket;
        this.group = group;
        this.port = port;
    }

    @Override
    public void run(){
        while(true){
            byte[] buffer = new byte[ReadThread.MAX_LEN];
            DatagramPacket datagram = new DatagramPacket(buffer,buffer.length,group,port);
            String message;

            try{
                socket.receive(datagram);
                message = new String(buffer,0,datagram.getLength(),"UTF-8");

                System.out.println(message);
            }catch(IOException e){
                System.out.println("Socket closed!");
            }
        }
    }
}
