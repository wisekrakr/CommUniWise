package com.wisekrakr.communiwise.phone.managers;


import com.wisekrakr.communiwise.phone.audio.impl.ClientAudio;
import com.wisekrakr.communiwise.phone.audio.impl.ServerAudio;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class SoundManager{

    private ClientAudio clientAudio;
    private ServerAudio serverAudio;
    private DatagramSocket socket;

    public SoundManager() {
        serverAudio = new ServerAudio();
        clientAudio = new ClientAudio();

    }

    public void startAudioStream(int remoteRtpPort, String remoteIp){


        try {
            socket = new DatagramSocket();

            InetAddress address = InetAddress.getByName(remoteIp);

            socket.connect(address, remoteRtpPort);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        serverAudio.init(remoteRtpPort, remoteIp, socket);
        clientAudio.init(remoteRtpPort, remoteIp, socket);
    }

    public void stopAudioStream(){
        clientAudio.stop();
        serverAudio.stop();
    }
}
