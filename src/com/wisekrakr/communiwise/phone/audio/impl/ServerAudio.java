package com.wisekrakr.communiwise.phone.audio.impl;

import com.wisekrakr.communiwise.phone.audio.listener.OutgoingAudioListener;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;

public class ServerAudio extends AudioAbstract {

    private OutgoingAudioListener osl;


    public ServerAudio(){
    }

    @Override
    public void init(int remoteRtpPort, String server) {

        try{
            System.out.println("Creating Server Socket...");
            InetAddress serverAddress = InetAddress.getByName(server);
            DatagramSocket serverSocket = new DatagramSocket(remoteRtpPort, serverAddress);

            osl = new OutgoingAudioListener(serverSocket, getAudioFormat());

            osl.runListener();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop(){
        try{
            System.out.println("Stopping Server Socket...");

            osl.endListener();
        }catch(Exception e){
            e.printStackTrace();
        }
    }


}
