package com.wisekrakr.communiwise.phone.audio.impl.ext;

public interface AudioContext {
    void init(int rtpPort, String server);
    void stop();
}
