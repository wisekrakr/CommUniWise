package com.wisekrakr.communiwise.phone.sip;

import com.wisekrakr.communiwise.phone.calling.CallInstance;

import javax.sip.address.Address;

public interface SipManagerListener {

    void onTextMessage(String message, String from);

    void onRemoteBye(String callId);

    void onRemoteCancel(String callId);

    void onRemoteDeclined();

    void callConfirmed(CallInstance callInstance);

    void onUnavailable();

    void onRinging(CallInstance callInstance);

    void onBusy();

    void onRemoteAccepted();

    void onRegistered();

    void onBye(String callId);

    void onTrying();

    void authenticationFailed();

    void onAccepted(CallInstance callInstance, int remoteRtpPort);

    void onDeclined(String callId);

    void onNotFound(Address proxyAddress);

}
