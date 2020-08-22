package com.wisekrakr.communiwise.operations.apis;

import com.wisekrakr.communiwise.phone.audio.LineManager;

public interface SoundAPI {

    void startRecording();
    void playRemoteSound(String file);
    void stopRecording();
    void stopRemoteSound();
    void mute();
    void ringing(boolean isRinging);
    LineManager getLineManager();
}
