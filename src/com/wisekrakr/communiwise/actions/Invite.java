package com.wisekrakr.communiwise.actions;


import com.wisekrakr.communiwise.SipManager;
import com.wisekrakr.communiwise.config.Config;

import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.Request;
import java.util.ArrayList;

public class Invite {
    public Request MakeRequest(SipManager sipManager, String to, int port){


        try {
            //Create From Header
            SipURI from = sipManager.getAddressFactory().createSipURI(sipManager.getSipProfile().getSipUserName(), Config.SERVER);
            Address fromNameAddress = sipManager.getAddressFactory().createAddress(from);
            // fromNameAddress.setDisplayName(sipManager.getSipProfile().getSipUserName());
            FromHeader fromHeader = sipManager.getHeaderFactory().createFromHeader(fromNameAddress,
                    "12345");

            //Create To Header
            URI toAddress = sipManager.getAddressFactory().createURI(to);
            Address toNameAddress = sipManager.getAddressFactory().createAddress(toAddress);
            // toNameAddress.setDisplayName(username);
            ToHeader toHeader = sipManager.getHeaderFactory().createToHeader(toNameAddress, null);

            //Create Request URI
            URI requestURI = sipManager.getAddressFactory().createURI(to);

            //Create Via Header
            ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

            //Create CSeq Header
            CSeqHeader cSeqHeader = sipManager.getHeaderFactory().createCSeqHeader(1l,
                    Request.INVITE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = sipManager.getHeaderFactory()
                    .createMaxForwardsHeader(70);

            // Create a new CallId header
            CallIdHeader callIdHeader = sipManager.getSipProvider().getNewCallId();

            // Create the request.
            Request callRequest = sipManager.getMessageFactory().createRequest(requestURI,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            SupportedHeader supportedHeader = sipManager.getHeaderFactory()
                    .createSupportedHeader("replaces, outbound");
            callRequest.addHeader(supportedHeader);


            SipURI routeUri = sipManager.getAddressFactory().createSipURI(sipManager.getSipProfile().getSipUserName(), sipManager.getSipProfile().getServer());
            routeUri.setTransportParam(sipManager.getSipProfile().getTransport());
            routeUri.setLrParam();
            routeUri.setPort(sipManager.getSipProfile().getRemotePort());

            Address routeAddress = sipManager.getAddressFactory().createAddress(routeUri);
            RouteHeader route = sipManager.getHeaderFactory().createRouteHeader(routeAddress);
            callRequest.addHeader(route);

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = sipManager.getHeaderFactory()
                    .createContentTypeHeader("application", "sdp");

            // Create the contact name address.
            SipURI contactURI = sipManager.getAddressFactory().createSipURI(sipManager.getSipProfile().getSipUserName(), sipManager.getSipProfile().getLocalIp());
            contactURI.setPort(sipManager.getSipProvider().getListeningPoint(sipManager.getSipProfile().getTransport())
                    .getPort());

            Address contactAddress = sipManager.getAddressFactory().createAddress(contactURI);

            // Add the contact address.
            //contactAddress.setDisplayName(fromName);

            ContactHeader contactHeader = sipManager.getHeaderFactory().createContactHeader(contactAddress);
            callRequest.addHeader(contactHeader);

            String sdpData= "v=0\r\n" +
                    "o=- 13760799956958020 13760799956958020" + " IN IP4 " + sipManager.getSipProfile().getLocalIp() +"\r\n" +
                    "s=mysession session\r\n" +
                    "s=-\r\n" +
                    //"p=+46 8 52018010\r\n" +
                    "c=IN IP4 " + sipManager.getSipProfile().getLocalIp()+"\r\n" +
                    "t=0 0\r\n" +
                    "m=audio " + port + " RTP/AVP 0\r\n" +
                    "m=audio " + port + " RTP/AVP 0 4 18\r\n" +
                    "a=rtpmap:0 PCMU/8000\r\n" +
                    "a=rtpmap:4 G723/8000\r\n" +
                    "a=rtpmap:18 G729A/8000\r\n" +
                    "a=ptime:20\r\n";
            byte[] contents = sdpData.getBytes();

            callRequest.setContent(contents, contentTypeHeader);

            Header callInfoHeader = sipManager.getHeaderFactory().createHeader("sipphone.Call-Info",
                    "<http://www.antd.nist.gov>");
            callRequest.addHeader(callInfoHeader);

            return callRequest;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();

        }
        return null;
    }


}
