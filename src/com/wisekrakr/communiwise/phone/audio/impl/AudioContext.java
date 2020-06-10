package com.wisekrakr.communiwise.phone.audio.impl;

import java.net.DatagramSocket;

public interface AudioContext {
    void init(int remoteRtpPort, String server, DatagramSocket socket);
    void stop();
}
