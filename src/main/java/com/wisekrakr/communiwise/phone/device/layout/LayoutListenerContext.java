package com.wisekrakr.communiwise.phone.device.layout;


import java.util.EventListener;

public interface LayoutListenerContext extends EventListener {
    void onScreenEventMessage(ScreenEvent screenEvent);

}
