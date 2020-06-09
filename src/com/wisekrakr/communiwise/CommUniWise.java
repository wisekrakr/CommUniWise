package com.wisekrakr.communiwise;


import com.wisekrakr.communiwise.phone.Device;
import com.wisekrakr.communiwise.phone.SipManager;
import com.wisekrakr.communiwise.screen.PhoneScreen;
import com.wisekrakr.communiwise.user.SipProfile;


public class CommUniWise{

    private static PhoneScreen phoneScreen;
    private static Device device = Device.GetInstance();


    public static void main(String[] args) {

        phoneScreen = new PhoneScreen(device);





    }

}
