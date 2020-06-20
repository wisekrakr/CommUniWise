package com.wisekrakr.communiwise.phone.managers;


import com.wisekrakr.communiwise.config.Config;
import com.wisekrakr.communiwise.phone.Device;
import com.wisekrakr.communiwise.phone.audio.impl.IncomingAudio;
import com.wisekrakr.communiwise.phone.audio.impl.OutgoingAudio;


public class SoundManager{

    private final IncomingAudio incomingAudio;
    private final OutgoingAudio outgoingAudio;
    private final Device device;

    public SoundManager(Device device) {
        this.device = device;

        incomingAudio = new IncomingAudio();
        outgoingAudio = new OutgoingAudio();
    }

    public void startAudioStream(int rtpPort, String ipAddress){

        try {
            Thread threadOne = new Thread(){
                @Override
                public void run() {
                    incomingAudio.init(rtpPort, ipAddress);
                }
            };
            threadOne.start();

            Thread threadTwo = new Thread(){
                @Override
                public void run() {
                    outgoingAudio.init(Config.ANOTHER_RTP_PORT, device.getSipManager().getSipProfile().getLocalIp());

                }
            };
            threadTwo.start();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void stopAudioStream(){
        incomingAudio.stop();
        outgoingAudio.stop();
    }
}
