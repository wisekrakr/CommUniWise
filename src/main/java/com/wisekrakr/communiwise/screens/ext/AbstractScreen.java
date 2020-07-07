package com.wisekrakr.communiwise.screens.ext;

import javax.swing.*;

public class AbstractScreen extends JFrame implements FrameContext {
    @Override
    public void clearScreen() {
        this.setVisible(false);
        this.dispose();
    }
}
