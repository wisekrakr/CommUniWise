package com.wisekrakr.communiwise.phone.audiovisualconnection;

public interface SoundAPI {

    void startRecording();
    void playRemoteSound(String file);
    void stopRecording();
    void stopRemoteSound();
    void mute(boolean muted);

}
