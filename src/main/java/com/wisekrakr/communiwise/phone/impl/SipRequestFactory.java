package com.wisekrakr.communiwise.phone.impl;


import com.wisekrakr.communiwise.phone.managers.SipManager;
import com.wisekrakr.communiwise.user.SipProfile;
import com.wisekrakr.communiwise.utils.Headers;

import javax.sip.InvalidArgumentException;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import java.text.ParseException;
import java.util.ArrayList;

public class SipRequestFactory {
    public static Request createRegisterRequest(SipManager sipManager) {

        AddressFactory addressFactory = sipManager.getAddressFactory();
        SipProvider sipProvider = sipManager.getUdpSipProvider();
        MessageFactory messageFactory = sipManager.getMessageFactory();
        HeaderFactory headerFactory = sipManager.getHeaderFactory();

        try {
            // Create addresses and via header for the request
            Address fromAddress = addressFactory.createAddress("sip:"
                    + sipManager.getSipProfile().getSipUserName() + "@"
                    + sipManager.getSipProfile().getServer());
            fromAddress.setDisplayName(sipManager.getSipProfile().getSipUserName());
            Address toAddress = addressFactory.createAddress("sip:"
                    + sipManager.getSipProfile().getSipUserName() + "@"
                    + sipManager.getSipProfile().getServer());
            toAddress.setDisplayName(sipManager.getSipProfile().getSipUserName());

            Address contactAddress = Headers.createContactAddress(addressFactory, sipManager.getSipProfile());
            ArrayList<ViaHeader> viaHeaders = Headers.createViaHeader(headerFactory, sipManager.getSipProfile(), sipProvider);
            URI requestURI = addressFactory.createAddress(
                    "sip:" + sipManager.getSipProfile().getRemoteEndpoint()) //was getRemoteEndPoint
                    .getURI();
            // Build the request
            Request request = messageFactory.createRequest(requestURI,
                    Request.REGISTER, sipProvider.getNewCallId(),
                    headerFactory.createCSeqHeader(1L, Request.REGISTER),
                    headerFactory.createFromHeader(fromAddress, "c3ff411e"),
                    headerFactory.createToHeader(toAddress, null), viaHeaders,
                    headerFactory.createMaxForwardsHeader(70));


            // Add the contact header
            request.addHeader(headerFactory.createContactHeader(contactAddress));
            ExpiresHeader eh = headerFactory.createExpiresHeader(300);
            request.addHeader(eh);

            // Print the request
            System.out.println(request.toString());

            return request;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Request makeInviteRequest(SipManager sipManager, String to, int port){

        AddressFactory addressFactory = sipManager.getAddressFactory();
        SipProvider sipProvider = sipManager.getUdpSipProvider();
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

//            sipProfile.setTransport("tcp");

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

            //TODO: PCMA/8000 was PCMU/8000
//            String sdpData= "v=0\r\n" +
//                    "o=- 13760799956958020 13760799956958020" + " IN IP4 " + sipProfile.getLocalIp() +"\r\n" +
//                    "s=mysession session\r\n" +
//                    "s=-\r\n" +
//                    //"p=+46 8 52018010\r\n" +
//                    "c=IN IP4 " + sipProfile.getLocalIp()+"\r\n" +
//                    "t=0 0\r\n" +
//                    "m=audio " + port + " RTP/AVP 0\r\n" +
//                    "m=audio " + port + " RTP/AVP 0 4 18\r\n" +
//                    "a=rtpmap:0 PCMU/8000\r\n" + //was PCMA
//                    "a=rtpmap:4 G723/8000\r\n" +
//                    "a=rtpmap:18 G729A/8000\r\n" +
////                    "a=rtpmap:18 G7222/16000\r\n" +
//                    "a=ptime:20\r\n";
            String sdpData= "v=0\r\n" +
                    "o=- 13760799956958020 13760799956958020" + " IN IP4 " + sipProfile.getLocalIp() +"\r\n" +
                    "s=mysession session\r\n" +
                    "c=IN IP4 " + sipProfile.getLocalIp()+"\r\n" +
                    "t=0 0\r\n" +
                    "m=audio " + port + " RTP/AVP 0\r\n" +
                    "m=audio " + port + " RTP/AVP 0 4 18 101\r\n" +
                    "a=rtpmap:0 PCMU/8000\r\n" +
                    "a=rtpmap:4 G723/8000\r\n" +
                    "a=rtpmap:18 G729A/8000\r\n" +
                    "a=rtpmap:101 telephone-event/8000\r\n" +
                    "a=maxptime:150\r\n" +
                    "a=sendrecv\r\n" +
                    "a=ptime:20\r\n";
//


            byte[] contents = sdpData.getBytes();

//            SdpOffer sdpOffer = new SdpOffer();
//            byte[] contents = sdpOffer.createSdp(sipProfile.getLocalIp(), port);

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

    public Request makeMessageRequest(SipManager sipManager, String to, String message) throws ParseException, InvalidArgumentException {
        SipURI from = sipManager.getAddressFactory().createSipURI(sipManager.getSipProfile().getSipUserName(), sipManager.getSipProfile().getLocalEndpoint());
        Address fromNameAddress = sipManager.getAddressFactory().createAddress(from);
        // fromNameAddress.setDisplayName(sipUsername);
        FromHeader fromHeader = sipManager.getHeaderFactory().createFromHeader(fromNameAddress,
                "Tzt0ZEP92");

        URI toAddress = sipManager.getAddressFactory().createURI(to);
        Address toNameAddress = sipManager.getAddressFactory().createAddress(toAddress);
        // toNameAddress.setDisplayName(username);
        ToHeader toHeader = sipManager.getHeaderFactory().createToHeader(toNameAddress, null);

        URI requestURI = sipManager.getAddressFactory().createURI(to);
        // requestURI.setTransportParam("tcp");

        ArrayList<ViaHeader> viaHeaders = Headers.createViaHeader(sipManager.getHeaderFactory(), sipManager.getSipProfile(), sipManager.getUdpSipProvider());

        CallIdHeader callIdHeader = sipManager.getUdpSipProvider().getNewCallId();

        CSeqHeader cSeqHeader = sipManager.getHeaderFactory().createCSeqHeader(50l,
                Request.MESSAGE);

        MaxForwardsHeader maxForwards = sipManager.getHeaderFactory()
                .createMaxForwardsHeader(70);

        Request request = sipManager.getMessageFactory().createRequest(requestURI,
                Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);
        SupportedHeader supportedHeader = sipManager.getHeaderFactory()
                .createSupportedHeader("replaces, outbound");
        request.addHeader(supportedHeader);

        SipURI routeUri = sipManager.getAddressFactory().createSipURI(null, sipManager.getSipProfile().getServer());
        routeUri.setTransportParam(sipManager.getSipProfile().getTransport());
        routeUri.setLrParam();
        routeUri.setPort(sipManager.getSipProfile().getRemotePort());

        Address routeAddress = sipManager.getAddressFactory().createAddress(routeUri);
        RouteHeader route = sipManager.getHeaderFactory().createRouteHeader(routeAddress);
        request.addHeader(route);
        ContentTypeHeader contentTypeHeader = sipManager.getHeaderFactory()
                .createContentTypeHeader("text", "plain");
        request.setContent(message, contentTypeHeader);

        System.out.println(request);

        return request;
    }
}
