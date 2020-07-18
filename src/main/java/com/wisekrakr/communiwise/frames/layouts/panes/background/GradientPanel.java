package com.wisekrakr.communiwise.frames.layouts.panes.background;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GradientPanel extends JPanel {

    private static final int WIDE = 640;
    private static final int HIGH = 240;
    private static final float HUE_MIN = 0;
    private static final float HUE_MAX = 1;
    private final Timer timer;
    private float hue = HUE_MIN;
    private Color color1 = Color.white;
    private Color color2 = Color.black;
    private float delta = 0.01f;

    public GradientPanel() {
        ActionListener action = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                hue += delta;
                if (hue > HUE_MAX) {
                    hue = HUE_MIN;
                }
                color1 = Color.getHSBColor(hue, 1, 0.5f);
                color2 = Color.getHSBColor(hue + 16 * delta, 1, 0.5f);
                repaint();
            }
        };
        timer = new Timer(200, action);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint p = new GradientPaint(
                0, 0, color1, getWidth(), 0, color2);
        g2d.setPaint(p);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDE, HIGH);
    }
}



