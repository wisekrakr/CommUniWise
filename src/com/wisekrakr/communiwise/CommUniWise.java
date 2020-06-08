package com.wisekrakr.communiwise;


import com.wisekrakr.communiwise.SipManager;
import com.wisekrakr.communiwise.screen.PhoneScreen;
import com.wisekrakr.communiwise.user.SipProfile;


public class CommUniWise{

    private static PhoneScreen phoneScreen;
    private static SipManager sipManager;
    private static SipProfile sipProfile;

    public static void main(String[] args) {
        sipProfile = new SipProfile();
        sipManager = new SipManager(sipProfile);
        phoneScreen = new PhoneScreen(sipManager);


//        for(Mixer.Info info: getMixerInfo()){
//            System.out.println(info.getName() + " --- " + info.getDescription());
//        }



    }

}
