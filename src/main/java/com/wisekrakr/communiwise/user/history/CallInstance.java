package com.wisekrakr.communiwise.user.history;


import org.ocpsoft.prettytime.PrettyTime;

import javax.sip.address.Address;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CallInstance implements Serializable {
    private static final long serialVersionUID = 31L;

    private final String id;
    private final String displayName;
    private final InetSocketAddress proxyAddress;
    private final Address sipAddress;
    private final Date date;
    private final double contactId;
    private String callTime;

    public CallInstance(String id, String displayName, InetSocketAddress proxyAddress, Address sipAddress) {
        this.id = id;
        this.displayName = displayName;
        this.proxyAddress = proxyAddress;
        this.sipAddress = sipAddress;

        date = new Date();

        contactId = Math.random() * 100000000;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public InetSocketAddress getProxyAddress() {
        return proxyAddress;
    }

    public Address getSipAddress() {
        return sipAddress;
    }

    public void setCallDuration(String callTime) {
        this.callTime = callTime;
    }

    public String getCallDuration() {
        return callTime;
    }

    public String getCallDate(){
        SimpleDateFormat DateFor = new SimpleDateFormat("E, MMM dd yyyy HH:mm");

        return DateFor.format(date.getTime());
    }

    public String getFromCallDate(){

        PrettyTime prettyTime = new PrettyTime();

        return prettyTime.format(date);
    }

    public double getContactId() {
        return contactId;
    }
}
