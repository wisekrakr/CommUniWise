package com.wisekrakr.communiwise.phone.audio.listener;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import static com.wisekrakr.communiwise.phone.rtp.AudioConversion.decode;
import static com.wisekrakr.communiwise.phone.rtp.AudioConversion.encode;

public class OutgoingAudioListener {

    private final AudioFormat format;
    private final String localIp;
    private final int localRtpPort;

    boolean talking;
    private TargetDataLine mic;

    // path of the wav file
    File wavFile = new File("test/RecordAudio outgoing"+ "-" + Math.random() * 1000 + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    public OutgoingAudioListener(AudioFormat format, String localIp, int localRtpPort) {
        this.format = format;
        this.localIp = localIp;
        this.localRtpPort = localRtpPort;
    }

    public void runListener(){
        try{

            System.out.println("Listening from mic.");
            InetAddress localAddress = InetAddress.getByName(localIp);

            DatagramSocket socket = new DatagramSocket();
            socket.connect(localAddress, localRtpPort);

            System.out.println("Connecting to Client:"+localAddress.getHostAddress()+" Port:"+localRtpPort);

            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class,format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(micInfo)) {
                System.out.println("Line not supported");
                System.exit(0);
            }

//            Mixer mixer = AudioSystem.getMixer(getMixerInfo()[1]);
            mic = (TargetDataLine) AudioSystem.getLine(micInfo);

//            for(Mixer.Info info: getMixerInfo()){
//                System.out.println(info.getName() + " --- " + info.getDescription() + Arrays.toString(mixer.getTargetLineInfo()));
//            }

            mic.open(format);
            mic.start();
            System.out.println("Mic open.");

            byte[] tmpBuff = new byte[4096];

            talking = true;

            while(talking) {
                AudioInputStream ais = new AudioInputStream(mic);

                int count = mic.read(tmpBuff,0,tmpBuff.length);

                DatagramPacket packet = new DatagramPacket(tmpBuff,count, localAddress, localRtpPort);

                ByteArrayOutputStream out = new ByteArrayOutputStream(packet.getLength());

                AudioSystem.write(ais, fileType, wavFile);

                if (count > 0){

                    socket.send(packet);

                    out.write(tmpBuff, 0, count);

                }
            }
//            mic.close();
//            mic.drain();


        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void endListener(){
        try {
            if(mic != null){
                mic.stop();
                mic.close();

                talking = false;

                System.out.println("Stopped listening from mic.");
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
