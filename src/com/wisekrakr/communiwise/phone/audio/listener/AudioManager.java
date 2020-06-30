package com.wisekrakr.communiwise.phone.audio.listener;

import javax.sound.sampled.*;
import java.io.File;
import java.net.*;



public class AudioManager {

    byte[] buff = new byte[512]; //4096

    // path of the wav file
    File wavFile = new File("test/RecordAudio input thread "+ "-" + Math.random() * 1000 + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    // the line from which audio data is captured
    TargetDataLine input;
    SourceDataLine output;

    boolean servingInput;
    boolean servingOutput;

    private String remoteAddress;
    private int remotePort;
    private DatagramSocket socket;


    public void start(String remoteAddress, int remotePort, DatagramSocket socket) throws SocketException, LineUnavailableException {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.socket = socket;


        startSpeaker();
        startMicrophone();

    }

//    public static void main(String[] args) {
//        try {
//            Line line = selectAudioOutput("Main Compu Speakers (Realtek High Definition Audio)");
//
//            System.out.println(line.getLineInfo());
//
//            ((SourceDataLine) line).open(FORMAT);
//
//        } catch (LineUnavailableException e) {
//            System.out.println("Error " + e);
//        }
//    }

    public static Line selectAudioOutput(String name) throws LineUnavailableException {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixer : mixers) {
            if (name.equals(mixer.getName())) {
                Mixer more = AudioSystem.getMixer(mixer);

                if (more.getSourceLineInfo().length > 0) {
                    System.out.println(String.format("%s : %s source lines: %d  target lines: %d", mixer.getName(), mixer.getDescription(), more.getSourceLineInfo().length, more.getTargetLineInfo().length));
                    for (Line.Info info : more.getSourceLineInfo()) {
                        if (info.getLineClass() == SourceDataLine.class) {


                            System.out.println("  Source : " + info.toString() + " " + info.getLineClass());
                            Line line = more.getLine(info);
                            System.out.println("  Line " + line);

                            line.open();

                            for (Control control : line.getControls()) {
                                System.out.println("    control: " + control);
                            }

                        }
//                        more.open();

//                        return more.getLine(info);
                    }
                }
            }

        }

        throw new IllegalStateException("No output line found for " + name);
    }

    public static AudioFormat FORMAT =
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2,
                (16 / 8) * 2, 44100, false);
//        new AudioFormat(44100, 16,
//                2, true, true);


    private void startMicrophone() throws LineUnavailableException {


        DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, FORMAT);

        if (!AudioSystem.isLineSupported(micInfo)) {
            throw new IllegalStateException("Line not supported by mic");
        }
        input = (TargetDataLine) AudioSystem.getLine(micInfo);
        input.open(FORMAT);
        input.start();   // start capturing

        System.out.println("Start capturing client... " + socket.getLocalAddress());

        AudioInputStream ais = new AudioInputStream(input);
        InputThread inputThread = new InputThread(this);
        inputThread.setMic(input);

        inputThread.setSocket(socket);
        inputThread.setLocalIp(socket.getLocalAddress());
        inputThread.setLocalRtpPort(socket.getLocalPort());
        inputThread.setBuff(buff);

//            socket.connect(socket.getLocalAddress(), socket.getLocalPort());

        inputThread.start();

        System.out.println("Start recording client... connected: " + socket.isConnected());

        servingInput = true;
//            // start recording
//            AudioSystem.write(ais, fileType, wavFile);


    }

    private void startSpeaker() throws SocketException, LineUnavailableException {


        DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, FORMAT);

        if (!AudioSystem.isLineSupported(speakerInfo)) {
            throw new IllegalStateException("Line not supported by speaker");
        }
        output = (SourceDataLine) AudioSystem.getLine(speakerInfo);
        output.open(FORMAT);
        output.start();   // start capturing

        System.out.println("Start capturing server...");

        InetSocketAddress serverAddress = new InetSocketAddress(remoteAddress, remotePort);
        socket.connect(serverAddress);

        ReceptionThread receptionThread = new ReceptionThread(output, socket);

        System.out.println("Start recording server...");

        servingOutput = true;

        receptionThread.start();

    }

    public void stopStreaming(){
        stopClient();
        stopServer();

        socket.disconnect();
        socket.close();
    }

    private void stopClient() {
        input.stop();
        input.close();

        servingInput = false;

        System.out.println("Finished Client");
    }

    private void stopServer() {
        output.stop();
        output.close();

        servingOutput = false;

        System.out.println("Finished Server");
    }

    public boolean isServingInput() {
        return servingInput;
    }

    public boolean isServingOutput() {
        return servingOutput;
    }
}
