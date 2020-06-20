package com.wisekrakr.communiwise.phone.audio.impl;

import com.wisekrakr.communiwise.phone.audio.impl.ext.AudioAbstract;
import com.wisekrakr.communiwise.phone.audio.listener.IncomingAudioListener;

public class IncomingAudio extends AudioAbstract {

    private IncomingAudioListener incomingAudioListener;


    @Override
    public void init(int rtpPort, String server) {
        try {
            System.out.println("Creating Client Socket...");


            incomingAudioListener = new IncomingAudioListener(getAudioFormat(), rtpPort, server);
            incomingAudioListener.runListener();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void stop() {
        try {
            System.out.println("Stopping Client Socket...");

            if(incomingAudioListener != null){
                incomingAudioListener.endListener();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
