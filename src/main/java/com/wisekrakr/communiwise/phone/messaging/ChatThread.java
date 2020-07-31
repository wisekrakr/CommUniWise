package com.wisekrakr.communiwise.phone.messaging;

import java.io.*;
import java.net.Socket;

public class ChatThread {
    private Socket socket;

    public ChatThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String clientMessage;

            do {
                clientMessage = reader.readLine();


            } while (!clientMessage.equals("bye"));

            socket.close();

        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
