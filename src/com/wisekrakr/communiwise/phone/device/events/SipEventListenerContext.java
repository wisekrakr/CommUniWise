package com.wisekrakr.communiwise.phone.device.events;


import java.util.EventListener;

public interface SipEventListenerContext extends EventListener {
    void onSipMessage(SipEvent sipEvent);
}
