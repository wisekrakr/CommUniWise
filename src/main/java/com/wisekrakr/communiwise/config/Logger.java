package com.wisekrakr.communiwise.config;

public interface Logger {
    public void debug(String message);
    public void info(String message);
    public void error(String message);
    public void error(String message, Exception exception);
    public void traceNetwork(String message, String direction);
}
