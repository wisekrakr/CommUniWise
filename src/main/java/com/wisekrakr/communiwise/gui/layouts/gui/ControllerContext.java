package com.wisekrakr.communiwise.gui.layouts.gui;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.ext.AbstractGUI;

public interface ControllerContext {

    ControllerContext initImplementations(EventManager eventManager, AbstractGUI gui);
    void close();

}
