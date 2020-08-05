package com.wisekrakr.communiwise.gui.layouts;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

public class AboutFrame extends JFrame implements ActionListener,
        HyperlinkListener {


    public AboutFrame() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");

        String message = "CommUniWise: java SIP push-to-talk softphone<br>"
            + "Copyright 2020 Wisekrakr<br>"
            + "<a href=\"www.github.com/wisekrakr\">www.github.com/wisekrakr</a>";
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        textPane.setText(message);
        textPane.addHyperlinkListener(this);
        textPane.setOpaque(false);
        textPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        add(textPane, BorderLayout.PAGE_START);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        Font font = textArea.getFont();
        font = new Font(font.getName(), font.getStyle(), font.getSize() - 2);
        textArea.setFont(font);


        JPanel panel = new JPanel();
        JButton button = new JButton("Close");
        button.addActionListener(this);
        panel.add(button);
        add(panel, BorderLayout.PAGE_END);

        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dispose();
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
        if (EventType.ACTIVATED.equals(hyperlinkEvent.getEventType())) {
            try {
                URI uri = new URI("www.github.com/wisekrakr");
                java.awt.Desktop.getDesktop().browse(uri);
            } catch (Throwable e) {
                System.out.println("Hyperlinkupdate failed");
            }
        }
    }

}
