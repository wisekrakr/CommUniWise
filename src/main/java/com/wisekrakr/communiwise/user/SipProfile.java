package com.wisekrakr.communiwise.user;

public class SipProfile {
    private String localIp;
    private int localPort;
    private String transport;
    private int localRtpPort;

    private String server;
    private int remotePort;
    private String sipUserName;
    private String sipPassword;
    private String sipAddress;

    public SipProfile(String localIp, int localPort, String transport, int localRtpPort, String server, int remotePort, String sipUserName, String sipPassword, String sipAddress) {
        this.localIp = localIp;
        this.localPort = localPort;
        this.transport = transport;
        this.localRtpPort = localRtpPort;
        this.server = server;
        this.remotePort = remotePort;
        this.sipUserName = sipUserName;
        this.sipPassword = sipPassword;
        this.sipAddress = sipAddress;
    }

    public String getLocalIp() {
        return localIp;
    }

    public int getLocalRtpPort() {
        return localRtpPort;
    }

    public int getLocalPort() {
        return localPort;
    }

    public String getLocalEndpoint() {
        return localIp + ":" + localPort;
    }

    public String getServer() {
        return server;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getRemoteEndpoint() {
        return server + ":" + remotePort;
    }

    public String getSipAddress() {
        return sipAddress;
    }

    public String getSipUserName() {
        return sipUserName;
    }

    public String getSipPassword() {
        return sipPassword;
    }

    public String getTransport() {
        return transport;
    }

}
