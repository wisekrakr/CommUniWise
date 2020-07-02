package com.wisekrakr.communiwise.config;

import java.awt.*;

public abstract class Config {

    /**
     * Development constants (removed after development phase)
     */
    public static String DISPLAY_NAME = "damian2";
    public static String SERVER = "asterisk.interzone";
    public static String USERNAME = "252";
    public static String PASSWORD = "45jf83f";
    public static String LOCAL_IP = "192.168.84.87"; //"127.0.0.1"; //
    public static String ANOTHER_IP = "192.168.1.107";
    public static Integer LOCAL_RTP_PORT = 33060;
    public static Integer ANOTHER_RTP_PORT = 55401;
    public static Integer MASTER_PORT = 5060;
    public static Integer LOCAL_PORT = 5080;
    public static String REMOTE_IP = "192.168.80.61";

    /**
     * Color constants for frame and panel layouts
     */

    public static Color LIGHT_CYAN = new Color(176,228,234);
    public static Color DARK_CYAN = new Color(18, 95, 101);
    public static Color SUNSET_ORANGE = new Color(227, 159, 41);
}
