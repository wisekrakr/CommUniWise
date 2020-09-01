package com.wisekrakr.communiwise.gui.layouts.components;

import javax.swing.*;
import java.awt.*;

public class ButtonSpecial extends JButton{

    private String text;

    public ButtonSpecial(String text, int posX, int posY) {
        this.text = text;

        setText(text);
        setBackground(Color.WHITE);
        setForeground(new Color(35, 144, 156));
        setBounds(posX, posY, 80, 25);

        setBorder(new RoundedBorder(7));

    }

    public ButtonSpecial(String text, int posX, int posY, Color color) {
        this.text = text;

        setText(text);
        setBackground(color);
        setForeground(color.brighter());
        setBounds(posX, posY, 80, 40);

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


    public static JButton loginButton(){
        ImageIcon icon=new ImageIcon("/images/login.png");


        JButton button=new JButton();
        button.setBorderPainted(false);
        button.setBorder(null);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setContentAreaFilled(false);
        button.setIcon(icon);
        button.setOpaque(false);
        button.setDisabledIcon(icon);
        return button;
    }
}
