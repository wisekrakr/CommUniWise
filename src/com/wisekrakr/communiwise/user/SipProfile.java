package com.wisekrakr.communiwise.user;

import com.wisekrakr.communiwise.config.Config;

public class SipProfile {
    private String localIp= Config.LOCAL_IP;
    private int localPort = 52216;
    private String transport = "udp";

    private String server = Config.SERVER;
    private int remotePort = 5060;
    private String sipUserName = Config.USERNAME;
    private String sipPassword = Config.PASSWORD;

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        System.out.println("Setting localIp:" + localIp);
        this.localIp = localIp;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        System.out.println("Setting localPort:" + localPort);
        this.localPort = localPort;
    }

    public String getLocalEndpoint() {
        return localIp + ":" + localPort;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        System.out.println("Setting remoteIp:" + server);
        this.server = server;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        System.out.println("Setting remotePort:" + remotePort);
        this.remotePort = remotePort;
    }

    public String getRemoteEndpoint() {
        return server + ":" + remotePort;
    }

    public String getSipUserName() {
        return sipUserName;
    }

    public void setSipUserName(String sipUserName) {
        System.out.println("Setting sipUserName:" + sipUserName);
        this.sipUserName = sipUserName;
    }

    public String getSipPassword() {
        return sipPassword;
    }

    public void setSipPassword(String sipPassword) {
        System.out.println("Setting sipPassword:" + sipPassword);
        this.sipPassword = sipPassword;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        System.out.println("Setting transport:" + transport);
        this.transport = transport;
    }
}
