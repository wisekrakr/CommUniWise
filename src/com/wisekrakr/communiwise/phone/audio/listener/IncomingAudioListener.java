package com.wisekrakr.communiwise.phone.audio.listener;





import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

import static com.wisekrakr.communiwise.phone.rtp.AudioConversion.decode;
import static com.wisekrakr.communiwise.phone.rtp.AudioConversion.encode;
import static javax.sound.sampled.AudioSystem.getMixerInfo;

public class IncomingAudioListener extends Thread {

    private final AudioFormat format;
    private final int remoteRtpPort;
    private final String server;

    private SourceDataLine speaker;
    private boolean listening = true;

    // path of the wav file
    File wavFile = new File("test/RecordAudio incoming"+ "-" + Math.random() * 1000 + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    public IncomingAudioListener(AudioFormat format, int remoteRtpPort, String server) {
        super();
        this.format = format;
        this.remoteRtpPort = remoteRtpPort;
        this.server = server;
    }

    public void runListener(){
        try{
            System.out.println("Connecting to server:"+server+" Port:"+remoteRtpPort);

            SocketAddress serverAddress = new InetSocketAddress(server, remoteRtpPort);
            DatagramSocket socket = new DatagramSocket();

            socket.connect(serverAddress);

            System.out.println("Connected client?: "+ socket.isConnected());
            System.out.println("Listening to incoming audio on: " + socket.getRemoteSocketAddress());

            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class,format);

//            for(Mixer.Info info: getMixerInfo()){
//                System.out.println(info.getName() + " --- " + info.getDescription());
//            }

            Mixer mixer = AudioSystem.getMixer(getMixerInfo()[1]);
            speaker = (SourceDataLine)mixer.getLine(speakerInfo);
//            speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);

            speaker.open(format);
            speaker.start();

            byte[] receivedData = new byte[4096];

            while(listening){

                DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
                ByteArrayInputStream bais = new ByteArrayInputStream(receivedPacket.getData());
                AudioInputStream ais = new AudioInputStream(bais,format,receivedPacket.getLength());

                AudioSystem.write(ais, fileType, wavFile);

                byte[] samples = new byte[(int)(ais.getFrameLength() * format.getFrameSize())];

                int bytesRead = 0;
                int decoderData;
                if((bytesRead = ais.read(receivedData)) != -1){
                    decoderData = decode(receivedData, samples, bytesRead, format);

                    bytesRead = encode(samples, receivedData, decoderData, format);

                    socket.receive(receivedPacket);

                    speaker.write(receivedData,0,bytesRead);

                    //                 bais.reset();
                }

                ais.close();
                bais.close();
            }

//            speaker.drain();
//            speaker.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }



    public void endListener(){

        try {
            if(speaker != null){
                speaker.stop();
                speaker.close();
                System.out.println("Stop listening to incoming audio.");
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        listening = false;
    }
}
