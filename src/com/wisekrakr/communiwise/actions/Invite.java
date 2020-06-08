package com.wisekrakr.communiwise.actions;


import com.wisekrakr.communiwise.SipManager;
import com.wisekrakr.communiwise.user.SipProfile;
import com.wisekrakr.communiwise.utils.Headers;

import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import java.util.ArrayList;

public class Invite {
    private SipManager sipManager;

    public Invite(SipManager sipManager) {
        this.sipManager = sipManager;
    }

    public Request MakeRequest(String to, int port){

        AddressFactory addressFactory = sipManager.getAddressFactory();
        SipProvider sipProvider = sipManager.getSipProvider();
        MessageFactory messageFactory = sipManager.getMessageFactory();
        HeaderFactory headerFactory = sipManager.getHeaderFactory();

        SipProfile sipProfile = sipManager.getSipProfile();

        try {
            //Create From Header
            SipURI from = addressFactory.createSipURI(sipProfile.getSipUserName(), sipProfile.getServer());
            Address fromNameAddress = addressFactory.createAddress(from);
            // fromNameAddress.setDisplayName(sipManager.getSipProfile().getSipUserName());
            FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,"12345");

            //Create Request URI
            URI requestURI = addressFactory.createURI(to);

            //Create To Header
//            URI toAddress = sipManager.getAddressFactory().createURI(to);
            Address toNameAddress = addressFactory.createAddress(requestURI);
            // toNameAddress.setDisplayName(username);
            ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);



            //Create Via Header
            ArrayList<ViaHeader> viaHeaders = Headers.createViaHeader(headerFactory,
                    sipProfile, sipProvider);

            //Create CSeq Header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1l, Request.INVITE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create the request.
            Request callRequest = messageFactory.createRequest(requestURI,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            SupportedHeader supportedHeader = headerFactory
                    .createSupportedHeader("replaces, outbound");
            callRequest.addHeader(supportedHeader);


            SipURI routeUri = addressFactory.createSipURI(sipProfile.getSipUserName(), sipProfile.getServer());
            routeUri.setTransportParam(sipProfile.getTransport());
            routeUri.setLrParam();
            routeUri.setPort(sipProfile.getRemotePort());

            Address routeAddress = addressFactory.createAddress(routeUri);
            RouteHeader route = headerFactory.createRouteHeader(routeAddress);
            callRequest.addHeader(route);

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(sipProfile.getSipUserName(), sipProfile.getLocalIp());
            contactURI.setPort(sipProvider.getListeningPoint(sipProfile.getTransport())
                    .getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            //contactAddress.setDisplayName(fromName);

            ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
            callRequest.addHeader(contactHeader);

            String sdpData= "v=0\r\n" +
                    "o=- 13760799956958020 13760799956958020" + " IN IP4 " + sipProfile.getLocalIp() +"\r\n" +
                    "s=mysession session\r\n" +
                    "s=-\r\n" +
                    //"p=+46 8 52018010\r\n" +
                    "c=IN IP4 " + sipProfile.getLocalIp()+"\r\n" +
                    "t=0 0\r\n" +
                    "m=audio " + port + " RTP/AVP 0\r\n" +
                    "m=audio " + port + " RTP/AVP 0 4 18\r\n" +
                    "a=rtpmap:0 PCMU/8000\r\n" +
                    "a=rtpmap:4 G723/8000\r\n" +
                    "a=rtpmap:18 G729A/8000\r\n" +
                    "a=ptime:20\r\n";
            byte[] contents = sdpData.getBytes();

            callRequest.setContent(contents, contentTypeHeader);

            Header callInfoHeader = headerFactory.createHeader("sipphone.Call-Info",
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
