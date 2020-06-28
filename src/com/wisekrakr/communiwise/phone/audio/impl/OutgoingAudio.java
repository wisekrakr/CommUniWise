package com.wisekrakr.communiwise.phone.audio.impl;

import com.wisekrakr.communiwise.phone.audio.impl.ext.AudioAbstract;
import com.wisekrakr.communiwise.phone.audio.listener.OutgoingAudioListener;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;


public class OutgoingAudio extends AudioAbstract {

    private OutgoingAudioListener outgoingAudioListener;

//    public ServerAudio(){
//    }

    @Override
    public void init(int rtpPort, String ipAddress) {

        System.out.println("Creating Server Socket...");

        outgoingAudioListener = new OutgoingAudioListener(getAudioFormat(), ipAddress, rtpPort);

        outgoingAudioListener.runListener();

    }

    @Override
    public void stop(){
        System.out.println("Stopping Server Socket...");

        if(outgoingAudioListener != null){
            outgoingAudioListener.endListener();
        }
    }
}
