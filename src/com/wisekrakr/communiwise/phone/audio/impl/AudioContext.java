package com.wisekrakr.communiwise.phone.audio.impl;

public interface AudioContext {
    void init(int remoteRtpPort, String server);
    void stop();
}
