package com.wisekrakr.communiwise.phone.impl;


import com.wisekrakr.communiwise.phone.managers.SipManager;
import com.wisekrakr.communiwise.utils.Headers;

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

    private SipManager sipManager;
    public Register(SipManager sipManager) {
        this.sipManager = sipManager;
    }

    public Request MakeRequest() throws ParseException,
            InvalidArgumentException {

        AddressFactory addressFactory = sipManager.getAddressFactory();
        SipProvider sipProvider = sipManager.getSipProvider();
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

            Address contactAddress = Headers.createContactAddress(addressFactory,sipManager.getSipProfile());
            ArrayList<ViaHeader> viaHeaders = Headers.createViaHeader(headerFactory,sipManager.getSipProfile(), sipProvider);
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
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;



    }
}
