package com.wisekrakr.communiwise.phone.events;


public interface SipConnectionListener {
    void onSipUAConnecting(SipEvent event);
    void onSipUAConnected(SipEvent event);
    void onSipUADisconnected(SipEvent event);
    void onSipUACancelled(SipEvent event);
    void onSipUADeclined(SipEvent event);
}
