package com.wisekrakr.communiwise.phone.audiovisualconnection;

import com.wisekrakr.communiwise.phone.audiovisualconnection.threads.ReceptionThread;
import com.wisekrakr.communiwise.phone.audiovisualconnection.threads.TransmitterThread;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static javax.sound.sampled.AudioSystem.getMixerInfo;

public class RTPConnectionManager {
    private TargetDataLine input;
    private SourceDataLine output;

    private InetSocketAddress serverAddress;

    private DatagramSocket socket;

    private final List<String> mixerNames = new ArrayList<>();

    private Mixer outputMixer;
    private Mixer inputMixer;

    private Thread receptionThread;
    private TransmitterThread transmitterThread;

    public void init() throws SocketException {
        this.socket = new DatagramSocket();

        Mixer.Info[] mixers = getMixerInfo();

        for (Mixer.Info mixer : mixers) {
            mixerNames.add(mixer.getName());
        }
    }

    public void start(InetSocketAddress serverAddress) throws IOException, LineUnavailableException {
        this.serverAddress = serverAddress;


        startReceiving();
        startTransmitting();
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

//                if (inputMixer.getTargetLines().length > 0) {
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
//                    }
                }
            }

        }
    }

    /**
     * Creates a audio format ULAW (Or PCM_SIGNED) 44100, 16, 2, 4, 44100, little endian
     *
     * @return new AudioFormat
     */
    public static AudioFormat format() {
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
        // AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File("test/RecordAudio input thread "+ "-" + Math.random() * 1000 + ".wav"));
    }

    private void startReceiving() throws SocketException, LineUnavailableException {
        // TODO: audio setup should happen outside of this class
        DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, format());

        if (!AudioSystem.isLineSupported(speakerInfo)) {
            throw new IllegalStateException("Line not supported by speaker");
        }

        output = (SourceDataLine) outputMixer.getLine(speakerInfo);
        output.open(format());
        output.start();   // start capturing

        socket.connect(serverAddress);

        receptionThread = new Thread(new ReceptionThread(output, socket));
        receptionThread.setName("Reception thread");
        receptionThread.setDaemon(true);
        receptionThread.start();
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

    public DatagramSocket getSocket() {
        return socket;
    }
}
