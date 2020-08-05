package com.wisekrakr.communiwise.gui.layouts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatFrame extends JFrame implements ActionListener {

    private String screenName;

    // GUI stuff
    private JTextArea  enteredText = new JTextArea(10, 32);
    private JTextField typedText   = new JTextField(32);


    public ChatFrame(String screenName) {

        this.screenName = screenName;

        // create GUI stuff
        enteredText.setEditable(false);
        enteredText.setBackground(Color.LIGHT_GRAY);
        typedText.addActionListener(this);

        Container content = getContentPane();
        content.add(new JScrollPane(enteredText), BorderLayout.CENTER);
        content.add(typedText, BorderLayout.SOUTH);

        // display the window, with focus on typing box
        setTitle("Chat Client CommUniWise 1.0: [" + screenName + "]");
        pack();
        typedText.requestFocusInWindow();

    }

    // process TextField after user hits Enter
    public void actionPerformed(ActionEvent e) {
//        out.println("[" + screenName + "]: " + typedText.getText());
        typedText.setText("");
        typedText.requestFocusInWindow();
    }

//    // listen to socket and print everything that server broadcasts
//    public void listen() {
//        String s;
//        while ((s = in.readLine()) != null) {
//            enteredText.insert(s + "\n", enteredText.getText().length());
//            enteredText.setCaretPosition(enteredText.getText().length());
//        }
//        out.close();
//        in.close();
//        try                 { socket.close();      }
//        catch (Exception e) { e.printStackTrace(); }
//        System.err.println("Closed client socket");
//    }
//
//    public static void main(String[] args)  {
//        ChatClient client = new ChatClient(args[0], args[1]);
//        client.listen();
//    }
}