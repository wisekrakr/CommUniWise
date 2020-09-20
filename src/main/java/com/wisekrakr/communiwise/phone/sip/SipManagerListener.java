package com.wisekrakr.communiwise.phone.sip;

import com.wisekrakr.communiwise.user.history.CallInstance;

import javax.sip.address.Address;

public interface SipManagerListener {

    void onReceiveMessage(String message, String from);

    void onRemoteBye(CallInstance callInstance);

    void onRemoteCancel(CallInstance callInstance);

    void onRemoteDeclined();

    void callConfirmed(CallInstance callInstance);

    void onUnavailable();

    void onRinging(CallInstance callInstance);

    void onBusy();

    void onRemoteAccepted();

    void onRegistered();

    void onBye(CallInstance callInstance);

    void onTrying();

    void authenticationFailed();

    void onAccepted(CallInstance callInstance, int remoteRtpPort);

    void onDeclined(String callId);

    void onNotFound(Address proxyAddress);

}
