package com.wisekrakr.communiwise.phone.audio.impl;

import com.wisekrakr.communiwise.phone.audio.impl.ext.AudioAbstract;
import com.wisekrakr.communiwise.phone.audio.listener.OutgoingAudioListener;


public class OutgoingAudio extends AudioAbstract {

    private OutgoingAudioListener outgoingAudioListener;

//    public ServerAudio(){
//    }

    @Override
    public void init(int rtpPort, String ipAddress) {

        try{
            System.out.println("Creating Server Socket...");

            outgoingAudioListener = new OutgoingAudioListener(getAudioFormat(), ipAddress, rtpPort);

            outgoingAudioListener.runListener();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop(){
        try{
            System.out.println("Stopping Server Socket...");

            if(outgoingAudioListener != null){
                outgoingAudioListener.endListener();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


}
