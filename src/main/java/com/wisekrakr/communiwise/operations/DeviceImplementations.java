package com.wisekrakr.communiwise.operations;

import com.wisekrakr.communiwise.phone.audio.AudioManager;
import com.wisekrakr.communiwise.phone.audio.LineManager;
import com.wisekrakr.communiwise.phone.connections.RTPConnectionManager;
import com.wisekrakr.communiwise.operations.apis.AccountAPI;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;
import com.wisekrakr.communiwise.phone.sip.SipManager;
import com.wisekrakr.communiwise.user.ContactManager;
import com.wisekrakr.communiwise.user.SipAccountManager;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class DeviceImplementations {

    private final SipManager sipManager;
    private final RTPConnectionManager rtpConnectionManager;
    private final AudioManager audioManager;
    private final SipAccountManager accountManager;
    private final ContactManager contactManager;

    public DeviceImplementations(SipManager sipManager, RTPConnectionManager rtpConnectionManager,SipAccountManager accountManager,AudioManager audioManager) {
        this.sipManager = sipManager;
        this.rtpConnectionManager = rtpConnectionManager;
        this.accountManager = accountManager;
        this.audioManager = audioManager;

        contactManager = new ContactManager();
    }

    public SoundAPI getSoundApi(){
        LineManager lineManager = new LineManager();

//        audioManager = new AudioManager(rtpConnectionManager.getSocket(), lineManager.createTargetDataLine(null), lineManager.createSourceDataLine(null));


        return new SoundAPI() {
            public final AudioFormat FORMAT = new AudioFormat(16000, 16, 1, true, true);

            @Override
            public LineManager getLineManager() {
                return lineManager;
            }

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
            public void ringing(boolean isRinging) {
                audioManager.ringing(isRinging);
            }

            @Override
            public void mute() {
                rtpConnectionManager.mute();
            }
        };
    }

    public PhoneAPI getPhoneApi(){
        return new PhoneAPI() {

            private String proxyAddress;

            @Override
            public void initiateCall(String sipAddress) {

                sipManager.initiateCall(sipAddress, rtpConnectionManager.getSocket().getLocalPort());

                proxyAddress = sipAddress;
            }

            @Override
            public void accept(String sipAddress) {
                sipManager.acceptCall(rtpConnectionManager.getSocket().getLocalPort());

                proxyAddress = sipAddress;
            }

            @Override
            public void reject() {
                sipManager.reject();
            }


            @Override
            public void hangup(String callId) {
                try {
                    sipManager.hangup(proxyAddress, callId);
                } catch (Throwable e) {
                    throw new IllegalStateException("Unable to hang up the device", e);
                }

            }


            @Override
            public void register(String realm, String domain, String username, String password, String fromAddress) {
                sipManager.login(realm, username, password, domain, fromAddress);

                try {
                    getAccountApi().getContactManager().loadPhoneBook(getAccountApi().getUserInfo().get(SipAccountManager.UserInfoPart.USERNAME.getInfoPart()));
                }catch (Throwable e){
                    throw new IllegalArgumentException("Phonebook could not be loaded", e);
                }
            }

            @Override
            public int callStatus() {
                return sipManager.getStatus();
            }
        };
    }

    public AccountAPI getAccountApi(){

        return new AccountAPI() {

            public boolean isAuthenticated = false;

            @Override
            public boolean isAuthenticated() {
                return isAuthenticated;
            }

            @Override
            public void userIsOnline() {
                isAuthenticated = true;
            }

            @Override
            public Map<String, String> getUserInfo() {
                return accountManager.getUserInfo();
            }

            @Override
            public boolean phoneBookHandler(ContactManager.UserOption userOption, String username, String domain, int extension) {
                return contactManager.handleUserMenuSelection(userOption,username,domain, extension);
            }

            @Override
            public void updateContact(String username, String domain, int extension) {

            }

            @Override
            public ContactManager getContactManager() {
                return contactManager;
            }

        };
    }
}
