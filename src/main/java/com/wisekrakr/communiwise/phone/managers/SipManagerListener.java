package com.wisekrakr.communiwise.phone.managers;

public interface SipManagerListener {
    void onTextMessage(String message, String from);

    void onBye();

    void onRemoteCancel();

    void onRemoteDeclined();

    void onConnected(int rtpPort);

    void onUnavailable();

    void onRinging(String from);

    void onBusy();

    void onRemoteAccepted();

    void onRegistered();

    void onHangup();
}
