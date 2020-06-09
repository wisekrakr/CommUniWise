package com.wisekrakr.communiwise.phone.audio.impl;

import com.wisekrakr.communiwise.phone.audio.listener.IncomingAudioListener;

public class ClientAudio extends AudioAbstract {

    private IncomingAudioListener isl;

    public ClientAudio(){

    }

    @Override
    public void init(int remoteRtpPort, String server) {
        try {
            isl = new IncomingAudioListener(getAudioFormat(), remoteRtpPort, server);
            isl.runListener();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void stop() {
        try {
            isl.endListener();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
