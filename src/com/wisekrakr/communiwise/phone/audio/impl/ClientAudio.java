package com.wisekrakr.communiwise.phone.audio.impl;

import com.wisekrakr.communiwise.phone.audio.listener.IncomingAudioListener;

import java.net.DatagramSocket;

public class ClientAudio extends AudioAbstract {

    private IncomingAudioListener isl;


    @Override
    public void init(int remoteRtpPort, String server, DatagramSocket socket) {
        try {


            isl = new IncomingAudioListener(getAudioFormat(), remoteRtpPort, server, socket);
            isl.runListener();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void stop() {
        try {
            if(isl != null){
                isl.endListener();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
