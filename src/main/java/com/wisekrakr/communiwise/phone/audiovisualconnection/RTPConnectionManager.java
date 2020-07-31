package com.wisekrakr.communiwise.phone.audiovisualconnection;

import com.wisekrakr.communiwise.phone.audiovisualconnection.threads.ReceptionThread;
import com.wisekrakr.communiwise.phone.audiovisualconnection.threads.TransmittingThread;
import com.wisekrakr.communiwise.phone.messaging.ReadThread;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;


public class RTPConnectionManager {
    private final TargetDataLine inputLine;
    private final SourceDataLine outputLine;
    private DatagramSocket socket;

    private Thread receptionThread;
    private TransmittingThread transmittingThread;
    private Thread chatThread;

    public RTPConnectionManager(TargetDataLine inputLine, SourceDataLine outputLine) {
        this.inputLine = inputLine;
        this.outputLine = outputLine;
    }

    public void init() throws SocketException {
        this.socket = new DatagramSocket();
    }

    public void connectForTextMessaging(int port){

        chatThread = new Thread(new ReadThread(socket,socket.getInetAddress(),port));
        chatThread.setName("Chat thread");
        chatThread.setDaemon(true);
        chatThread.start();

        System.out.println(" RTP Connection Chat Client: " + socket.isConnected());

    }

    public void connectForAudioStream(InetSocketAddress remoteAddress, String codec) throws IOException{
        socket.connect(remoteAddress);

        receptionThread = new Thread(new ReceptionThread(outputLine, socket, codec));
        receptionThread.setName("Reception thread");
        receptionThread.setDaemon(true);
        receptionThread.start();

        transmittingThread = new TransmittingThread(socket, inputLine, codec);
        transmittingThread.start();

        System.out.println(" RTP Connection Audio Client: " + socket.isConnected());
    }

    public void send(AudioInputStream audioStream)throws IOException{
        transmittingThread.startSending(audioStream);
    }
    public void stopSend(){
        transmittingThread.interruptSending();
    }

    public void stopStreaming() {
        receptionThread.interrupt();
        try {
            receptionThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        stopInput();
        stopOutput();

        socket.disconnect();
        socket.close();

        transmittingThread.stop();
    }

    public void stopChat(){
        chatThread.interrupt();

        socket.disconnect();
        socket.close();
    }

    private void stopInput() {
        inputLine.stop();
        inputLine.close();

        System.out.println("Finished Input");
    }

    private void stopOutput() {
        outputLine.stop();
        outputLine.close();

        System.out.println("Finished Output");
    }

    public DatagramSocket getSocket() {
        return socket;
    }
}
