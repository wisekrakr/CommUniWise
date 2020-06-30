package com.wisekrakr.communiwise.phone.device.layout;

import com.wisekrakr.communiwise.phone.device.events.SipEvent;

import java.util.EventObject;

public class ScreenEvent extends EventObject {

    public ScreenEvent.ScreenEventType type;

    public enum ScreenEventType {
        REGISTERED, UNREGISTERED, AUDIO_CALLING, VIDEO_CALLING, MESSAGING, EXITING, POWER_OFF,INCOMING;

    }

    public ScreenEvent(Object source, ScreenEvent.ScreenEventType type) {
        super(source);

        this.type = type;
    }

}
