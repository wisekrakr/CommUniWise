package com.wisekrakr.communiwise.screens.layouts.objects;

import com.wisekrakr.communiwise.screens.layouts.ext.RoundedBorder;

import javax.swing.*;
import java.awt.*;

public class Button extends JButton{

    private String text;

    public Button(String text, int posX, int posY) {
        this.text = text;

        setText(text);
        setBackground(Color.WHITE);
        setForeground(new Color(35, 144, 156));
        setBounds(posX, posY, 80, 25);

        setBorder(new RoundedBorder(7));

    }

    public Button(String text, int posX, int posY, Color color) {
        this.text = text;

        setText(text);
        setBackground(color);
        setForeground(color.brighter());
        setBounds(posX, posY, 80, 25);

        setBorder(new RoundedBorder(7));

    }

    public JButton getButton() {
        return this;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }
}
