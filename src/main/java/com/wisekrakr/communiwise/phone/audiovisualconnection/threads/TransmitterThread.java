package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import javax.media.*;
import javax.media.control.FormatControl;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.rtcp.SourceDescription;
import javax.sound.sampled.TargetDataLine;
import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jitsi.impl.neomedia.codec.audio.ulaw.JavaDecoder;
import org.jitsi.impl.neomedia.codec.audio.ulaw.JavaEncoder;

public class TransmitterThread implements Runnable {
    private final TargetDataLine input;
    private final DatagramSocket socket;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Socket s;
    private DataSource dataOutput;
    private RTPManager[] rtpMgrs;

    public TransmitterThread(TargetDataLine input, DatagramSocket socket) {
        this.input = input;
        this.socket = socket;
    }

    public void start() {
        Thread thread = new Thread(this);

        running.set(true);

        thread.start();
    }

    public void stop() {
        running.set(false);
    }

    @Override
    public void run() {

        System.out.println("input socket info: " + socket.getLocalPort() + " " + socket.getPort() + " " + socket.getLocalSocketAddress() + " " + socket.getRemoteSocketAddress());

        byte[] buffer = new byte[512];

        DatagramPacket transmitPacket = new DatagramPacket(buffer, buffer.length, 0, socket.getRemoteSocketAddress());

        while (running.get()) {

            try {
                input.read(buffer, 0, buffer.length);

                createTransmitter();
//                AVTransmit transmit = new AVTransmit(String.valueOf(socket.getLocalPort()), socket.getInetAddress().getHostAddress(), String.valueOf(socket.getPort()));
//                transmit.go();
//                encodeAndDecode(transmitPacket.getData());

//                System.out.println("mic check: "  + Arrays.toString(data.getData()));

//                socket.send(transmitPacket);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {
        Buffer source = new Buffer();
        byte[] buffer = new byte[512];

        source.setData(buffer);
        source.setFormat(new AudioFormat("ULAW", 44100, 16,2));
        source.setLength(buffer.length);
        source.setOffset(0);

        Buffer encoded = new Buffer();
        Buffer decoded = new Buffer();

        JavaEncoder encoder = new JavaEncoder();
        encoder.process(source, encoded);

        JavaDecoder decoder = new JavaDecoder();
        decoder.process(encoded, decoded);

    }

    private void createTransmitter() throws IOException, InvalidSessionAddressException, UnsupportedFormatException, NoDataSourceException {

        dataOutput = Manager.createDataSource(new URL(socket.getLocalSocketAddress().toString().substring(1)));

        PushBufferDataSource pbds = (PushBufferDataSource)dataOutput;
        PushBufferStream pbss[] = pbds.getStreams();

        rtpMgrs = new RTPManager[pbss.length];
        SessionAddress localAddr, destAddr;
        InetAddress ipAddr;
        SendStream sendStream;
        int port;

        for (int i = 0; i < pbss.length; i++) {

            rtpMgrs[i] = RTPManager.newInstance();

            port = socket.getPort();
            ipAddr = InetAddress.getByName(socket.getLocalSocketAddress().toString());

            localAddr = new SessionAddress( InetAddress.getLocalHost(), port);
            destAddr = new SessionAddress( ipAddr, port);

            rtpMgrs[i].initialize( localAddr);
            rtpMgrs[i].addTarget( destAddr);

            sendStream = rtpMgrs[i].createSendStream(dataOutput, i);
            sendStream.start();
        }
    }

    public void encodeAndDecode(byte[]inputBytes) {
        Buffer source = new Buffer();

        source.setData(inputBytes);
        source.setFormat(new Format("ULAW"));
        source.setLength(inputBytes.length);
        source.setOffset(0);

        Buffer encoded = new Buffer();
        Buffer decoded = new Buffer();

        JavaEncoder encoder = new JavaEncoder();
        encoder.process(source, encoded);

        JavaDecoder decoder = new JavaDecoder();
        decoder.process(encoded, decoded);

//
//        System.out.println("    encoder: " + encoded.getLength());
//        System.out.println("    decoder: " + decoded.getLength());

    }
    public void test() {
//       First find a capture device that will capture linear audio
        // data at 8bit 8Khz
        AudioFormat format= new AudioFormat(AudioFormat.ULAW,
                44100,
                16,
                2);

        Vector devices= CaptureDeviceManager.getDeviceList( format);

        CaptureDeviceInfo di= null;

        if (devices.size() > 0) {
            di = (CaptureDeviceInfo) devices.elementAt( 0);
        }
        else {
            // exit if we could not find the relevant capturedevice.
            System.exit(-1);
        }

        // Create a processor for this capturedevice & exit if we
        // cannot create it
        Processor processor = null;
        try {
            processor = Manager.createProcessor(di.getLocator());
        } catch (IOException e) {
            System.exit(-1);
        } catch (NoProcessorException e) {
            System.exit(-1);
        }

        // configure the processor
        processor.configure();

        while (processor.getState() != Processor.Configured){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        processor.setContentDescriptor(
                new ContentDescriptor( ContentDescriptor.RAW));

        TrackControl track[] = processor.getTrackControls();

        boolean encodingOk = false;

        // Go through the tracks and try to program one of them to
        // output gsm data.

        for (int i = 0; i < track.length; i++) {
            if (!encodingOk && track[i] instanceof FormatControl) {
                if (((FormatControl)track[i]).
                        setFormat( new AudioFormat(AudioFormat.GSM_RTP,
                                8000,
                                8,
                                1)) == null) {

                    track[i].setEnabled(false);
                }
                else {
                    encodingOk = true;
                }
            } else {
                // we could not set this track to gsm, so disable it
                track[i].setEnabled(false);
            }
        }

        // At this point, we have determined where we can send out
        // gsm data or not.
        // realize the processor
        if (encodingOk) {
            processor.realize();
            while (processor.getState() != Processor.Realized){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // get the output datasource of the processor and exit
            // if we fail
            DataSource ds = null;

            try {
                ds = processor.getDataOutput();

            } catch (NotRealizedError e) {
                System.exit(-1);
            }

            // hand this datasource to manager for creating an RTP
            // datasink our RTP datasink will multicast the audio
            try {
                String url= "rtp://" + socket.getRemoteSocketAddress() + "/audio/16";

                MediaLocator m = new MediaLocator(url);

                DataSink d = Manager.createDataSink(ds, m);
                d.open();
                d.start();
                processor.start();
            } catch (Exception e) {
                System.exit(-1);
            }
        }



    }
}

