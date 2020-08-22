package com.wisekrakr.communiwise.gui.ext;

import com.wisekrakr.communiwise.gui.layouts.utils.FrameDragListener;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractGUI extends JFrame implements FrameContext {
    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    public Dimension getScreenSize() {
        return screenSize;
    }

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

    @Override
    public void prepareGUI() {

    }

    @Override
    public void addFrameDragAbility(){
        FrameDragListener frameDragListener = new FrameDragListener(this);
        this.addMouseListener(frameDragListener);
        this.addMouseMotionListener(frameDragListener);
    }
}
