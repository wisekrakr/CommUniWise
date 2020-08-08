package com.wisekrakr.communiwise.phone.device;

import com.wisekrakr.communiwise.gui.layouts.AudioCallGUI;
import com.wisekrakr.communiwise.gui.layouts.IncomingCallGUI;
import com.wisekrakr.communiwise.phone.audio.AudioManager;
import com.wisekrakr.communiwise.phone.connections.RTPConnectionManager;
import com.wisekrakr.communiwise.phone.managers.ContactManager;
import com.wisekrakr.communiwise.phone.managers.SipManager;
import com.wisekrakr.communiwise.user.SipUserProfile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class DeviceImplementations {
    private static final AudioFormat FORMAT = new AudioFormat(16000, 16, 1, true, true);


    public SoundAPI soundApiImpl(AudioManager audioManager){
        return new SoundAPI() {

            @Override
            public void startRecording() {

                audioManager.startRecordingWavFile();
            }

            @Override
            public void playRemoteSound(String file) {
                File audioFile = new File(file);
                try {

                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                    AudioInputStream lowResAudioStream = AudioSystem.getAudioInputStream(FORMAT, audioStream);

                    audioManager.startSendingAudio(lowResAudioStream);
                } catch (IOException | UnsupportedAudioFileException e) {
                    System.out.println(" error while sending audio file " + e);
                }
            }

            @Override
            public void stopRecording() {
                audioManager.stopRecording();
            }

            @Override
            public void stopRemoteSound() {
                audioManager.stopSendingAudio();
            }

            @Override
            public void mute(boolean muted) {
//                BooleanControl bc = (BooleanControl) inputLine.getControl(BooleanControl.Type.MUTE);
//                if (bc != null) {
//                    bc.setValue(true);
//                }
            }
        };
    }

    public PhoneAPI phoneApiImpl(SipManager sipManager, RTPConnectionManager rtpConnectionManager, AudioManager audioManager){
        return new PhoneAPI() {

            String proxyAddress;
            IncomingCallGUI incomingCallGUI;
            AudioCallGUI audioCallGUI;


            @Override
            public void initiateCall(String sipAddress) {
                //todo codec
                sipManager.initiateCall(sipAddress, rtpConnectionManager.getSocket().getLocalPort());

                audioCallGUI = new AudioCallGUI(this, soundApiImpl(audioManager));
                audioCallGUI.showWindow();

                proxyAddress = sipAddress;
            }

            @Override
            public void accept() {
                sipManager.acceptCall(rtpConnectionManager.getSocket().getLocalPort());

                incomingCallGUI = new IncomingCallGUI(this);
            }

            @Override
            public void reject() {
                sipManager.reject();
            }


            @Override
            public void hangup() {
                try {
                    sipManager.hangup(proxyAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                audioCallGUI.hideWindow();
            }

            @Override
            public void register(String realm, String domain, String username, String password, String fromAddress) {
                sipManager.login(realm, username, password, domain, fromAddress);
            }

            @Override
            public int callStatus() {
                return sipManager.getStatus();
            }
        };
    }

    public AccountAPI accountApiImpl(Map<String, String> info){
        ContactManager contactManager = new ContactManager();

        return new AccountAPI() {

            @Override
            public Map<String, String> getUserInfo() {
                return info;
            }

            @Override
            public void saveContact(SipUserProfile sipUserProfile) {
                contactManager.addToContactList(sipUserProfile.getSipCallAddress(), sipUserProfile);
            }

            @Override
            public void removeContact(SipUserProfile sipUserProfile) {
                contactManager.getContacts().remove(sipUserProfile.getSipCallAddress());
            }

            @Override
            public void updateContact(String username, String address, int port) {

            }

            @Override
            public ContactManager getContactManager() {
                return contactManager;
            }
        };
    }
}
