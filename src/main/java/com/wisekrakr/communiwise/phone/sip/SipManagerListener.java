package com.wisekrakr.communiwise.phone.sip;

import javax.sip.address.Address;

public interface SipManagerListener {
    void onTextMessage(String message, String from);

    void onBye();

    void onRemoteCancel();

    void onRemoteDeclined();

    void callConfirmed(String rtpHost, int rtpPort, String codec, String callId);

    void onUnavailable();

    void onRinging(String callId, Address address);

    void onBusy();

    void onRemoteAccepted();

    void onRegistered();

    void onHangup(String callId);

    void onTrying();

    void authenticationFailed();
}
