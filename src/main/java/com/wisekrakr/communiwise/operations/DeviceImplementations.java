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
import com.wisekrakr.communiwise.user.history.CallInstance;
import com.wisekrakr.communiwise.user.phonebook.PhoneBookEntry;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class DeviceImplementations {

    private final SipManager sipManager;
    private final RTPConnectionManager rtpConnectionManager;
    private final AudioManager audioManager;
    private final SipAccountManager accountManager;
    private final ContactManager contactManager;

    public DeviceImplementations(SipManager sipManager, RTPConnectionManager rtpConnectionManager, SipAccountManager accountManager, AudioManager audioManager, ContactManager contactManager) {
        this.sipManager = sipManager;
        this.rtpConnectionManager = rtpConnectionManager;
        this.accountManager = accountManager;
        this.audioManager = audioManager;
        this.contactManager = contactManager;
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
            public void playRemoteSound(String resource) {
                try {

                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource(resource));
                    AudioInputStream lowResAudioStream = AudioSystem.getAudioInputStream(FORMAT, audioStream);

                    audioManager.startSendingAudio(lowResAudioStream);
                } catch (Throwable t) {
                    throw new IllegalStateException("Could not play remote sound",t);
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

            @Override
            public void unmute() {
                rtpConnectionManager.unmute();
            }
        };
    }

    private String sipAddressMaker(String extension, String domain){
        return "sip:" + (extension + "@" + domain);
    }

    public PhoneAPI getPhoneApi(){
        return new PhoneAPI() {

            private String proxyAddress;

            @Override
            public void initiateCall(String extension, String domain) {

                sipManager.initiateCall(sipAddressMaker(extension, domain), rtpConnectionManager.getSocket().getLocalPort());

                proxyAddress = extension;
            }

            @Override
            public void sendVoiceMessage(String extension, String domain) {
                sipManager.sendVoiceMessage(sipAddressMaker(extension, domain), rtpConnectionManager.getSocket().getLocalPort());

                proxyAddress = extension;
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
            public void sendMessage(String recipient, String message) {
                sipManager.sendTextMessage(recipient, message);
            }

            @Override
            public void register(String realm, String domain, String username, String password, String fromAddress) {
                sipManager.login(realm, username, password, domain, fromAddress);

                try {
                    contactManager.loadPhoneBook(username);
                }catch (Throwable e){
                    throw new IllegalArgumentException("Phonebook could not be loaded", e);
                }

                try {
                    contactManager.loadCallLogBook("history"+username);
                }catch (Throwable e){
                    throw new IllegalArgumentException("Call Log Book could not be loaded", e);
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
            public PhoneBookEntry addContact(String username, String domain, int extension) {
                return contactManager.addContact(username, domain, extension);
            }

            @Override
            public boolean deleteContact(String username) {
                return contactManager.deleteContact(username);
            }

            @Override
            public boolean savePhoneBook() {
                return contactManager.savePhoneBook();
            }

            @Override
            public Collection<PhoneBookEntry> getContacts() {
                return contactManager.getPhoneBook().getEntries();
            }

            @Override
            public void addCallInstance(CallInstance callInstance) {
                contactManager.addCallInstance(callInstance);
            }

            @Override
            public boolean deleteCallInstance(String id) {
                return contactManager.deleteCallInstance(id);
            }

            @Override
            public boolean saveCallLogBook() {
                return contactManager.saveCallLogBook();
            }

            @Override
            public boolean clearCallLogBook() {
                return contactManager.clearCallLogBook();
            }

            @Override
            public Collection<CallInstance> getCallLogs() {
                return contactManager.getCallLogBook().getEntries();
            }
        };
    }
}
