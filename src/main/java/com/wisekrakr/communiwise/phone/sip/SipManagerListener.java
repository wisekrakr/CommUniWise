package com.wisekrakr.communiwise.phone.sip;

public interface SipManagerListener {
    void onTextMessage(String message, String from);

    void onRemoteBye(String callId);

    void onRemoteCancel();

    void onRemoteDeclined();

    void callConfirmed(String rtpHost, int rtpPort, String codec, String callId);

    void onUnavailable();

    void onRinging(String callId, String username, String rtpAddress, int rtpPort);

    void onBusy();

    void onRemoteAccepted();

    void onRegistered();

    void onBye(String callId);

    void onTrying();

    void authenticationFailed();

    void onAccepted(String callId, String rtpHost, int rtpPort, String codec);

    void onDeclined();
}
