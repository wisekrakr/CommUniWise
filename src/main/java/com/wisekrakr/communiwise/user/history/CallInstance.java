package com.wisekrakr.communiwise.user.history;


import javax.sip.address.Address;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CallInstance implements Serializable {
    private static final long serialVersionUID = 31L;

    private final String id;
    private final String displayName;
    private final InetSocketAddress proxyAddress;
    private final Address sipAddress;
    private LocalDateTime fromDate;
    private final LocalDateTime date;
    private final double contactId;
    private String callTime;

    public CallInstance(String id, String displayName, InetSocketAddress proxyAddress, Address sipAddress) {
        this.id = id;
        this.displayName = displayName;
        this.proxyAddress = proxyAddress;
        this.sipAddress = sipAddress;

        date = LocalDateTime.now();

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
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm");
        return dateFormat.format(date);
    }

    public String getFromCallDate(){
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd HH:mm");
        fromDate = LocalDateTime.from(date);

        return fromDate + " ago";
    }

    public double getContactId() {
        return contactId;
    }
}
