package com.wisekrakr.communiwise.actions;


import com.wisekrakr.communiwise.SipManager;

import javax.sip.InvalidArgumentException;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import java.text.ParseException;
import java.util.ArrayList;

public class Register {

    public Request MakeRequest(SipManager sipManager) throws ParseException,
            InvalidArgumentException {

        AddressFactory addressFactory = sipManager.getAddressFactory();
        SipProvider sipProvider = sipManager.getSipProvider();
        MessageFactory messageFactory = sipManager.getMessageFactory();
        HeaderFactory headerFactory = sipManager.getHeaderFactory();

        // Create addresses and via header for the request
        Address fromAddress = addressFactory.createAddress("sip:"
                + sipManager.getSipProfile().getSipUserName() + "@"
                + sipManager.getSipProfile().getRemoteIp());
        fromAddress.setDisplayName(sipManager.getSipProfile().getSipUserName());
        Address toAddress = addressFactory.createAddress("sip:"
                + sipManager.getSipProfile().getSipUserName() + "@"
                + sipManager.getSipProfile().getRemoteIp());
        toAddress.setDisplayName(sipManager.getSipProfile().getSipUserName());

        Address contactAddress = sipManager.createContactAddress();
        ArrayList<ViaHeader> viaHeaders = sipManager.createViaHeader();
        URI requestURI = addressFactory.createAddress(
                "sip:" + sipManager.getSipProfile().getRemoteEndpoint())
                .getURI();
        // Build the request
        final Request request = messageFactory.createRequest(requestURI,
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
    }
}
