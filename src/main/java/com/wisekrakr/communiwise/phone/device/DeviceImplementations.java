package com.wisekrakr.communiwise.phone.device;

import com.wisekrakr.communiwise.phone.audio.AudioManager;
import com.wisekrakr.communiwise.phone.connections.RTPConnectionManager;
import com.wisekrakr.communiwise.phone.managers.ContactManager;
import com.wisekrakr.communiwise.phone.managers.SipManager;
import com.wisekrakr.communiwise.user.SipAccountManager;
import com.wisekrakr.communiwise.user.SipUserProfile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class DeviceImplementations {

    private final SipManager sipManager;
    private final RTPConnectionManager rtpConnectionManager;
    private final AudioManager audioManager;
    private final SipAccountManager accountManager;
    private final ContactManager contactManager;

    public DeviceImplementations(SipManager sipManager, RTPConnectionManager rtpConnectionManager,SipAccountManager accountManager, AudioManager audioManager) {
        this.sipManager = sipManager;
        this.rtpConnectionManager = rtpConnectionManager;
        this.accountManager = accountManager;
        this.audioManager = audioManager;

        contactManager = new ContactManager();
    }

    public SoundAPI getSoundApi(){
        return new SoundAPI() {
            private final AudioFormat FORMAT = new AudioFormat(16000, 16, 1, true, true);

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

    public PhoneAPI getPhoneApi(){
        return new PhoneAPI() {

            private String proxyAddress;

            @Override
            public void initiateCall(String sipAddress) {
                //todo codec
                sipManager.initiateCall(sipAddress, rtpConnectionManager.getSocket().getLocalPort());

                proxyAddress = sipAddress;
            }

            @Override
            public void accept() {
                sipManager.acceptCall(rtpConnectionManager.getSocket().getLocalPort());

            }

            @Override
            public void reject() {
                sipManager.reject();
            }


            @Override
            public void hangup() {
                try {
                    sipManager.hangup(proxyAddress);
                } catch (Throwable e) {
                    throw new IllegalStateException("Unable to hang up the device", e);
                }

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

    public AccountAPI getAccountApi(){

        return new AccountAPI() {

            @Override
            public Map<String, String> getUserInfo() {
                return accountManager.getUserInfo();
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
