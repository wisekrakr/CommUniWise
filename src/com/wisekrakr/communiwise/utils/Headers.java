package com.wisekrakr.communiwise.utils;


import com.wisekrakr.communiwise.user.SipProfile;

import javax.sip.InvalidArgumentException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ViaHeader;
import java.text.ParseException;
import java.util.ArrayList;

public class Headers {

    public static ArrayList<ViaHeader> createViaHeader(HeaderFactory headerFactory, SipProfile sipProfile) {
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader myViaHeader;
        try {
            myViaHeader = headerFactory.createViaHeader(
                    sipProfile.getLocalIp(), sipProfile.getLocalPort(),
                    sipProfile.getTransport(), null);
            myViaHeader.setRPort();
            viaHeaders.add(myViaHeader);
        } catch (ParseException | InvalidArgumentException e) {
            e.printStackTrace();
        }
        return viaHeaders;
    }

    public static Address createContactAddress(AddressFactory addressFactory, SipProfile sipProfile) {
        try {
            return addressFactory.createAddress("sip:"
                    + sipProfile.getSipUserName() + "@"
                    + sipProfile.getLocalEndpoint() + ";transport=udp"
                    + ";registering_acc=" + sipProfile.getServer());
        } catch (ParseException e) {
            return null;
        }
    }
}
