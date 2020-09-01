package com.wisekrakr.communiwise.phone.calling;


import javax.sip.address.Address;
import java.net.InetSocketAddress;

public class CallInstance {
    private final String id;
    private final String displayName;
    private final InetSocketAddress proxyAddress;
    private final Address sipAddress;

    public CallInstance(String id, String displayName, InetSocketAddress proxyAddress, Address sipAddress) {
        this.id = id;
        this.displayName = displayName;
        this.proxyAddress = proxyAddress;
        this.sipAddress = sipAddress;
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
}
