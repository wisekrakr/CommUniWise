package com.wisekrakr.communiwise.screens.ext;

public enum ScreenState {

    /**
     * Login Screen, where a profile/user can register to a server/domain
     */
    LOGIN,
    /**
     * Main screen where the user can pick its next action or accept calls
     */
    PHONE,
    /**
     * When a voice call is accepted or trying to connect, this screen will pop up.
     */
    AUDIO_CALL,
    /**
     * When a video call is accepted or trying to connect, this screen will pop up.
     */
    VIDEO_CALL,
    /**
     * When the user want to start a chat session via text.
     */
    MESSENGER,
    /**
     * Bye screen
     */
    BYE_BYE,
    /**
     * Shows a small screen where the user can accept or decline an incoming call
     */
    INCOMING;
}
