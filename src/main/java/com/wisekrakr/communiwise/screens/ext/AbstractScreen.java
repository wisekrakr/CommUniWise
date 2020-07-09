package com.wisekrakr.communiwise.screens.ext;

import javax.swing.*;

public abstract class AbstractScreen extends JFrame implements FrameContext {
    @Override
    public void hideWindow() {
        this.setVisible(false);
        this.dispose();
    }

    @Override
    public void showWindow() {
    }
}
