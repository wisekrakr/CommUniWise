package com.wisekrakr.communiwise.phone.audio.impl;

import com.wisekrakr.communiwise.phone.audio.listener.OutgoingAudioListener;

import java.net.DatagramSocket;


public class ServerAudio extends AudioAbstract {

    private OutgoingAudioListener osl;

//    public ServerAudio(){
//    }

    @Override
    public void init(int remoteRtpPort, String server, DatagramSocket socket) {

        try{
            System.out.println("Creating Server Socket...");

            osl = new OutgoingAudioListener( getAudioFormat(), server, remoteRtpPort, socket);

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
