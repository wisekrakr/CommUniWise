package com.wisekrakr.communiwise.phone.calling;

import com.wisekrakr.communiwise.user.SipProfile;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class CallInstance {
    private long timestampRecStart = 0;
    private final Map<String, SocketAddress>callInstances = new HashMap<>();

    public CallInstance(String proxy, SocketAddress proxyAddress) {
        callInstances.put(proxy, proxyAddress);
    }

    public Map<String, SocketAddress> getCallInstances() {
        return callInstances;
    }

    public long getTime() {
        long time = 0;
        time = System.currentTimeMillis();
        return time;
    }
    public int getOutboundTimestamp() {
        return (int) (this.getTime() - timestampRecStart);
    }

    public void resetTimestampRecStart() {
        timestampRecStart = getTime();
    }
}
