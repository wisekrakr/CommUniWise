package com.wisekrakr.communiwise.phone.device;


import com.wisekrakr.communiwise.phone.events.SipEvent;

public interface SipDeviceListener {
    void onSipUAConnectionArrived(SipEvent event);
    void onSipUAMessageArrived(SipEvent event);
}
