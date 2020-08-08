package com.wisekrakr.communiwise.user;

public class SipUserProfile {

    private int port;
    private String server;
    private String sipUserName;

    public SipUserProfile( int port, String server, String sipUserName) {
        this.port = port;
        this.server = server;
        this.sipUserName = sipUserName;
    }

    public int getPort() {
        return port;
    }

    public String getServer() {
        return server;
    }

    public String getSipUserName() {
        return sipUserName;
    }

    public String getSipCallAddress(){
        return "sip:"+ sipUserName + "@" + server;
    }

}
