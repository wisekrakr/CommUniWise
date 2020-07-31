package com.wisekrakr.communiwise.phone.messaging;

import java.io.IOException;
import java.net.*;

public class ChatClient {
    private DatagramSocket socket;
    private String hostname;
    private int port;
    private String message;
    private String name;

    public ChatClient(DatagramSocket socket, String hostname, int port) {
        this.socket = socket;
        this.hostname = hostname;
        this.port = port;
    }

    public void sendMessage(String name, String message){
        this.name = name;
        this.message = message;

        while(true) {

            message = hostname + ": " + message;
            byte[] buffer = message.getBytes();
            DatagramPacket datagram = new DatagramPacket(buffer,buffer.length,socket.getInetAddress(),port);

            try {
                socket.send(datagram);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
