package com.wisekrakr.communiwise.phone.audiovisualconnection.threads;

import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.service.neomedia.format.MediaFormatFactory;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;


public class AVTransmit
{

    private final int localPortBase;

    /**
     * The <tt>MediaStream</tt> instances initialized by this instance indexed
     * by their respective <tt>MediaType</tt> ordinal.
     */
    private MediaStream[] mediaStreams;


    private final InetAddress remoteAddr;

    private final int remotePortBase;

    /**
     * Initializes a new <tt>AVTransmit2</tt> instance which is to transmit
     * audio and video to a specific host and a specific port.
     *
     * @param localPortBase the port which is the source of the transmission
     * i.e. from which the media is to be transmitted
     * @param remoteHost the name of the host which is the target of the
     * transmission i.e. to which the media is to be transmitted
     * @param remotePortBase the port which is the target of the transmission
     * i.e. to which the media is to be transmitted
     * @throws Exception if any error arises during the parsing of the specified
     * <tt>localPortBase</tt>, <tt>remoteHost</tt> and <tt>remotePortBase</tt>
     */
    public AVTransmit(
            String localPortBase,
            String remoteHost, String remotePortBase)
            throws Exception
    {
        this.localPortBase
                = (localPortBase == null)
                ? -1
                : Integer.valueOf(localPortBase).intValue();
        this.remoteAddr = InetAddress.getByName(remoteHost);
        this.remotePortBase = Integer.valueOf(remotePortBase).intValue();
    }

    /**
     * Starts the transmission. Returns null if transmission started ok.
     * Otherwise it returns a string with the reason why the setup failed.
     */
    private void start()
            throws Exception
    {
        /*
         * Prepare for the start of the transmission i.e. initialize the
         * MediaStream instances.
         */
        MediaType[] mediaTypes = MediaType.values();
        MediaService mediaService = LibJitsi.getMediaService();
        int localPort = localPortBase;
        int remotePort = remotePortBase;

        mediaStreams = new MediaStream[mediaTypes.length];
        for (MediaType mediaType : mediaTypes)
        {
            System.out.println("    mediatypes:  " + mediaType.ordinal()+ "  " + mediaType);
            /*
             * The default MediaDevice (for a specific MediaType) is configured
             * (by the user of the application via some sort of UI) into the
             * ConfigurationService. If there is no ConfigurationService
             * instance known to LibJitsi, the first available MediaDevice of
             * the specified MediaType will be chosen by MediaService.
             */
            MediaDevice device
                    = mediaService.getDefaultDevice(mediaType, MediaUseCase.CALL);
            MediaStream mediaStream = mediaService.createMediaStream(device);

            System.out.println("   mediastream:  " + mediaStream.getDirection().toString());
            // direction
            /*
             * The AVTransmit2 example sends only and the AVReceive2 receives
             * only. In a call, the MediaStream's direction will most commonly
             * be set to SENDRECV.
             */
            mediaStream.setDirection(MediaDirection.SENDONLY);

            // format
            String encoding;
            double clockRate;
            /*
             * The AVTransmit2 and AVReceive2 examples use the H.264 video
             * codec. Its RTP transmission has no static RTP payload type number
             * assigned.
             */
            byte dynamicRTPPayloadType;


            System.out.println("  device media: " + device.getMediaType());
            switch (device.getMediaType())
            {
                case AUDIO:
                    encoding = "PCMU";
                    clockRate = 8000;
                    /* PCMU has a static RTP payload type number assigned. */
                    dynamicRTPPayloadType = -1;
                    break;
                case VIDEO:
                    encoding = "H264";
                    clockRate = MediaFormatFactory.CLOCK_RATE_NOT_SPECIFIED;
                    /*
                     * The dymanic RTP payload type numbers are usually negotiated
                     * in the signaling functionality.
                     */
                    dynamicRTPPayloadType = 99;
                    break;
                default:
                    encoding = null;
                    clockRate = MediaFormatFactory.CLOCK_RATE_NOT_SPECIFIED;
                    dynamicRTPPayloadType = -1;
            }

            if (encoding != null)
            {
                MediaFormat format
                        = mediaService.getFormatFactory().createMediaFormat(
                        encoding,
                        clockRate);

                System.out.println("    format: " + format);
                /*
                 * The MediaFormat instances which do not have a static RTP
                 * payload type number association must be explicitly assigned
                 * a dynamic RTP payload type number.
                 */
                if (dynamicRTPPayloadType != -1)
                {
                    mediaStream.addDynamicRTPPayloadType(
                            dynamicRTPPayloadType,
                            format);
                }

                mediaStream.setFormat(format);
            }

            // connector
            StreamConnector connector;

            if (localPortBase == -1)
            {
                connector = new DefaultStreamConnector();
            }
            else
            {
                int localRTPPort = localPort++;
                int localRTCPPort = localPort++;

                connector
                        = new DefaultStreamConnector(
                        new DatagramSocket(localRTPPort),
                        new DatagramSocket(localRTCPPort));
            }
            mediaStream.setConnector(connector);

            // target
            /*
             * The AVTransmit2 and AVReceive2 examples follow the common
             * practice that the RTCP port is right after the RTP port.
             */
            int remoteRTPPort = remotePort++;
            int remoteRTCPPort = remotePort++;

            mediaStream.setTarget(
                    new MediaStreamTarget(
                            new InetSocketAddress(remoteAddr, remoteRTPPort),
                            new InetSocketAddress(remoteAddr, remoteRTCPPort)));

            System.out.println("    mediastream target: " + mediaStream.getTarget());
            // name
            /*
             * The name is completely optional and it is not being used by the
             * MediaStream implementation at this time, it is just remembered so
             * that it can be retrieved via MediaStream#getName(). It may be
             * integrated with the signaling functionality if necessary.
             */
            mediaStream.setName(mediaType.toString());

            mediaStreams[mediaType.ordinal()] = mediaStream;
        }

        /*
         * Do start the transmission i.e. start the initialized MediaStream
         * instances.
         */
        for (MediaStream mediaStream : mediaStreams)
            if (mediaStream != null)
                mediaStream.start();

    }

    /**
     * Stops the transmission if already started
     */
    private void stop()
    {
        if (mediaStreams != null)
        {
            for (int i = 0; i < mediaStreams.length; i++)
            {
                MediaStream mediaStream = mediaStreams[i];

                if (mediaStream != null)
                {
                    try
                    {
                        mediaStream.stop();
                    }
                    finally
                    {
                        mediaStream.close();
                        mediaStreams[i] = null;
                    }
                }
            }

            mediaStreams = null;
        }
    }



    public void go()
            throws Exception
    {


//        LibJitsi.start();
        try
        {
            // Create a audio transmit object with the specified params.
            start();

            // result will be non-null if there was an error. The return
            // value is a String describing the possible error. Print it.

            System.err.println("Start transmission for 60 seconds...");

            // Transmit for 60 seconds and then close the processor
            // This is a safeguard when using a capture data source
            // so that the capture device will be properly released
            // before quitting.
            // The right thing to do would be to have a GUI with a
            // "Stop" button that would call stop on AVTransmit2
            try
            {
                Thread.sleep(60000);
            } catch (InterruptedException ie)
            {
            }

            // Stop the transmission
            stop();

            System.err.println("...transmission ended.");

        }
        finally
        {
//            LibJitsi.stop();
        }

    }



}
