package com.wisekrakr.communiwise.operations.apis;

public interface SoundAPI {

    void startRecording();
    void playRemoteSound(String file);
    void stopRecording();
    void stopRemoteSound();
    void mute(boolean muted);

}
