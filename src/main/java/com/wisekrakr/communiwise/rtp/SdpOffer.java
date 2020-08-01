package com.wisekrakr.communiwise.rtp;

import javax.sdp.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

@Deprecated
public class SdpOffer implements Serializable {

    private final SdpFactory sdpFactory;

    public SdpOffer() {
        sdpFactory = SdpFactory.getInstance();
    }

    public byte[] createSdp(String localIp, int localPort) {
        try {

            Version version = sdpFactory.createVersion(0);
            long ss = SdpFactory.getNtpTime(new Date());
            Origin origin = sdpFactory.createOrigin("-", ss, ss, "IN", "IP4", localIp);
            SessionName sessionName = sdpFactory.createSessionName("-");
            Connection con = sdpFactory.createConnection("IN", "IP4", localIp);
            Time time = sdpFactory.createTime();
            Vector<Time> timeVector = new Vector<Time>();
            timeVector.add(time);
            Vector<java.io.Serializable> mediaDescriptionVector = new Vector<>();
            int[] audioformat = new int[3];
            //audioformat[0] = senderInfo.getMediaPayloadType();
            audioformat[0] = 0;
            audioformat[1] = 4;
            audioformat[2] = 18;
            MediaDescription audioMediaDescription = sdpFactory.createMediaDescription("audio", localPort, 1, "RTP/AVP", audioformat);
            mediaDescriptionVector.add(audioMediaDescription);
            String[] encoding = new String[3];
            encoding[0] = "PCMA";
            encoding[1] = "G723";
            encoding[2] = "G729A";
            // Attribute audioAttributeDescription = sdpFactory.createAttribute("rtpmap", "PCMA/8000");
            // mediaDescriptionVector.add(audioAttributeDescription);
            for (int i = 0; i < audioformat.length; i++) {
                Attribute rtpmap = sdpFactory.createAttribute(
                        SdpConstants.RTPMAP,
                        audioformat[i] + " " + encoding[i] + "/"
                                + 8000);
                mediaDescriptionVector.add(rtpmap);            }
//            Attribute fmtp = sdpFactory.createAttribute("fmtp", audioformat[1] + " 0-16");//0-16 1-10 abcdef
//            mediaDescriptionVector.add(fmtp);
//            Attribute direction = sdpFactory.createAttribute("outbound", null);
//            mediaDescriptionVector.add(direction);
            Attribute ptime = sdpFactory.createAttribute("ptime", "20");
            mediaDescriptionVector.add(ptime);
            // SDP
            SessionDescription sdpMessage = sdpFactory.createSessionDescription();
            sdpMessage.setVersion(version);
            sdpMessage.setOrigin(origin);
            sdpMessage.setSessionName(sessionName);
            sdpMessage.setConnection(con);
            sdpMessage.setTimeDescriptions(timeVector);
            sdpMessage.setMediaDescriptions(mediaDescriptionVector);

            return sdpMessage.toString().getBytes();
        } catch (IllegalArgumentException | SdpException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }
}
