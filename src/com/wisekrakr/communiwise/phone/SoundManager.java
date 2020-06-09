package com.wisekrakr.communiwise.phone;


import com.wisekrakr.communiwise.phone.audio.impl.ClientAudio;
import com.wisekrakr.communiwise.phone.audio.impl.ServerAudio;

public class SoundManager{

    private ClientAudio clientAudio;
    private ServerAudio serverAudio;

    public SoundManager() {
        clientAudio = new ClientAudio();
        serverAudio = new ServerAudio();
    }

    public void startAudioStream(int remoteRtpPort, String remoteIp){
        clientAudio.init(remoteRtpPort, remoteIp);
        serverAudio.init(remoteRtpPort, remoteIp);
    }

    public void stopAudioStream(){
        clientAudio.stop();
        serverAudio.stop();
    }
}
