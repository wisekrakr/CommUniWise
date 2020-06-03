package com.wisekrakr.communiwise.actions;


import com.wisekrakr.communiwise.SipManager;

import javax.sip.InvalidArgumentException;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.*;
import javax.sip.message.Request;
import java.text.ParseException;
import java.util.ArrayList;

public class Message {
    public Request MakeRequest(SipManager sipManager, String to, String message) throws ParseException, InvalidArgumentException {
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
        // requestURI.setTransportParam("udp");

        ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();

        CallIdHeader callIdHeader = sipManager.getSipProvider().getNewCallId();

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
