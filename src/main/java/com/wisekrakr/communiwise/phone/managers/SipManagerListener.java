package com.wisekrakr.communiwise.phone.managers;

public interface SipManagerListener {
    void onTextMessage(String message, String from);

    void onBye();

    void onRemoteCancel();

    void onRemoteDeclined();

    void callConfirmed(String rtpHost, int rtpPort);

    void onUnavailable();

    void onRinging(String from);

    void onBusy();

    void onRemoteAccepted();

    void onRegistered();

    void onHangup();

    void onTrying();

    void authenticationFailed();
}
