package com.wisekrakr.communiwise.phone.messaging;

import java.io.Console;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class WriteThread extends Thread{
    private Socket socket;
    private ChatClient client;

    public WriteThread(Socket socket, ChatClient client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream output = socket.getOutputStream();
        } catch (IOException ex) {
            System.out.println("Error getting output stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {

        Console console = System.console();

        String userName = console.readLine("\nEnter your name: ");

        String text;

        do {
            text = console.readLine("[" + userName + "]: ");

        } while (!text.equals("bye"));

        try {
            socket.close();
        } catch (IOException ex) {

            System.out.println("Error writing to server: " + ex.getMessage());
        }
    }
}
