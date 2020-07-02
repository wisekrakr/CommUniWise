package com.wisekrakr.communiwise.phone.audiovisualconnection;

import com.wisekrakr.communiwise.phone.audiovisualconnection.threads.ReceptionThread;
import com.wisekrakr.communiwise.phone.audiovisualconnection.threads.TransmitterThread;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

import static javax.sound.sampled.AudioSystem.getMixerInfo;


public class AVConnectionStream {

    // path of the wav file
    File wavFile = new File("test/RecordAudio input thread "+ "-" + Math.random() * 1000 + ".wav");

    // format of audio file
    AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

    // the line from which audio data is captured
    private TargetDataLine input;
    private SourceDataLine output;

    private String remoteAddress;
    private int remotePort;
    private DatagramSocket socket;

    private final List<String>mixerNames = new ArrayList<>();
    private Mixer outputMixer;
    private Mixer inputMixer;

    private ReceptionThread receptionThread;
    private TransmitterThread transmitterThread;


    public void init(){
        findMixers();
    }

    public void start(String remoteAddress, int remotePort, DatagramSocket socket) throws IOException, LineUnavailableException {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.socket = socket;

        startReceiving();
        startTransmitting();

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


    public void findMixers(){
        Mixer.Info[] mixers = getMixerInfo();

        for (Mixer.Info mixer : mixers) {
            mixerNames.add(mixer.getName());
        }
    }



    public void selectAudioOutput(String name) throws LineUnavailableException {
        Mixer.Info[] mixers = getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
            if (name.equals(mixerInfo.getName())) {

                outputMixer = AudioSystem.getMixer(mixerInfo);

                if (outputMixer.getSourceLineInfo().length > 0) {
                    System.out.println(String.format("%s : %s source lines: %d  target lines: %d", mixerInfo.getName(), mixerInfo.getDescription(), outputMixer.getSourceLineInfo().length, outputMixer.getTargetLineInfo().length));
                    for (Line.Info info : outputMixer.getSourceLineInfo()) {
                        if (info.getLineClass() == SourceDataLine.class) {


                            System.out.println("  Source : " + info.toString() + " " + info.getLineClass());
                            Line line = outputMixer.getLine(info);
                            System.out.println("  Line " + line);

                            ((SourceDataLine) line).open(format());

                            for (Control control : line.getControls()) {
                                System.out.println("    control: " + control);
                            }
                        }
                    }
                }
            }

        }
    }

    public void selectAudioInput(String name) throws LineUnavailableException {
        Mixer.Info[] mixers = getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
            if (name.equals(mixerInfo.getName())) {

                inputMixer = AudioSystem.getMixer(mixerInfo);

                if (inputMixer.getTargetLines().length > 0) {
                    System.out.println(String.format("%s : %s source lines: %d  target lines: %d", mixerInfo.getName(), mixerInfo.getDescription(), inputMixer.getSourceLineInfo().length, inputMixer.getTargetLineInfo().length));
                    for (Line.Info info : inputMixer.getTargetLineInfo()) {
                        if (info.getLineClass() == TargetDataLine.class) {

                            System.out.println("  Target : " + info.toString() + " " + info.getLineClass());
                            Line line = inputMixer.getLine(info);
                            System.out.println("  Line " + line);

                            ((TargetDataLine) line).open(format());

                            for (Control control : line.getControls()) {
                                System.out.println("    control: " + control);
                            }
                        }
                    }
                }
            }

        }
    }

    public static AudioFormat format(){
            return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2,
                (16 / 8) * 2, 44100, false);
//        new AudioFormat(44100, 16,
//                2, true, true);

    }


    private void startTransmitting() throws LineUnavailableException, IOException {

        DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, format());

        if (!AudioSystem.isLineSupported(micInfo)) {
            throw new IllegalStateException("Line not supported by mic");
        }
        input = (TargetDataLine) AudioSystem.getLine(micInfo);
        input.open(format());
        input.start();   // start capturing

        System.out.println("Start capturing client... " + socket.getLocalAddress());

        AudioInputStream ais = new AudioInputStream(input);
        transmitterThread = new TransmitterThread(input, socket);

        transmitterThread.start();

        System.out.println("Start recording client... connected: " + socket.isConnected());

            // start recording
//        AudioSystem.write(ais, fileType, wavFile);


    }

    private void startReceiving() throws SocketException, LineUnavailableException {

        DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format());

        if (!AudioSystem.isLineSupported(speakerInfo)) {
            throw new IllegalStateException("Line not supported by speaker");
        }
        output = (SourceDataLine) outputMixer.getLine(speakerInfo);
        output.open(format());
        output.start();   // start capturing

        System.out.println("Start capturing server...");

        InetSocketAddress serverAddress = new InetSocketAddress(remoteAddress, remotePort);
        socket.connect(serverAddress);

        receptionThread = new ReceptionThread(output, socket);

        System.out.println("Start recording server...");

        receptionThread.start();

    }

    public void stopStreaming(){
        stopInput();
        stopOutput();

        socket.disconnect();
        socket.close();

        receptionThread.stop();
        transmitterThread.stop();
    }

    private void stopInput() {
        input.stop();
        input.close();

        System.out.println("Finished Input");
    }

    private void stopOutput() {
        output.stop();
        output.close();

        System.out.println("Finished Output");
    }



    public List<String> getMixerNames() {
        return mixerNames;
    }
}
