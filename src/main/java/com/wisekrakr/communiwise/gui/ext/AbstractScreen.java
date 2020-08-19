package com.wisekrakr.communiwise.gui.ext;

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

    @Override
    public void showErrorStatus() {

    }
}
