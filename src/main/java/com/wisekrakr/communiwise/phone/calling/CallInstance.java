package com.wisekrakr.communiwise.phone.calling;


import java.net.InetSocketAddress;

public class CallInstance {
    private long timestampRecStart = 0;
    private String id;
    private String proxy;
    private InetSocketAddress proxyAddress;

    public CallInstance(String id, String proxy, InetSocketAddress proxyAddress) {
        this.id = id;
        this.proxy = proxy;
        this.proxyAddress = proxyAddress;
    }

    public String getId() {
        return id;
    }

    public String getProxy() {
        return proxy;
    }

    public InetSocketAddress getProxyAddress() {
        return proxyAddress;
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
