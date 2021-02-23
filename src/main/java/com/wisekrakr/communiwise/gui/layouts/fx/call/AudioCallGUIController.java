package com.wisekrakr.communiwise.gui.layouts.fx.call;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.AbstractJFXPanel;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerContext;
import com.wisekrakr.communiwise.gui.layouts.utils.LevelMeter;
import com.wisekrakr.communiwise.gui.layouts.utils.Status;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;
import com.wisekrakr.communiwise.user.history.CallInstance;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import javax.sound.sampled.TargetDataLine;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AudioCallGUIController extends AbstractJFXPanel implements ControllerContext {

    private final EventManager eventManager;
    private final PhoneAPI phone;
    private final SoundAPI sound;
    private final AbstractGUI gui;
    private final CallInstance callInstance;

    private final Map<String, Button> buttons = new HashMap<>();


    @FXML
    private AnchorPane container;
    @FXML
    private Button muteButton, recordButton, hangUpButton, playButton, inviteButton, contactListButton;
    @FXML
    private Label username, address,time,date;
    @FXML
    private ImageView muteImage, recordImage, playImage;
    @FXML
    private Text status;
    @FXML
    private ProgressBar bar;

    private boolean isMuted, isRecording,isPlaying;

    public AudioCallGUIController(EventManager eventManager, PhoneAPI phone, SoundAPI sound, AbstractGUI gui, CallInstance callInstance){
        this.eventManager = eventManager;
        this.phone = phone;
        this.sound = sound;
        this.gui = gui;
        this.callInstance = callInstance;

    }

    @FXML
    private void mute(){
        clickButtonForAction(isMuted, muteImage, "/images/mute.png","/images/unmute.png");
        isMuted = !isMuted;

        if(isMuted){
            sound.mute();
        }else {
            sound.unmute();
        }
    }

    @FXML
    private void record(){
        clickButtonForAction(isRecording, recordImage, "/images/record.png","/images/not-record.png");

        isRecording = !isRecording;

        if(isRecording){
            sound.startRecording();
        }else {
            sound.stopRecording();
        }
    }

    @FXML
    private void play(){
        clickButtonForAction(isPlaying, playImage, "/images/play.png","/images/play.png");

        isPlaying = !isPlaying;

        if(isPlaying){
            sound.playRemoteSound("/sounds/shake_bake.wav");
        }else{
            sound.stopRemoteSound();
        }
    }

    @FXML
    private void invite(){

    }


    @FXML
    private void openContactList(){
        eventManager.menuContactListOpen();
    }

    /**
     * Simple method to change images when clicked
     * @param isDoing boolean for the button being clicked
     * @param imageView the image of the button being changed
     * @param resourceA first image resource path
     * @param resourceB second image resource path
     */
    private void clickButtonForAction(boolean isDoing, ImageView imageView, String resourceA, String resourceB){
        Image image;
        try {
            if(!isDoing){
                image = new Image(getClass().getResourceAsStream(resourceA));

            }else{
                image = new Image(getClass().getResourceAsStream(resourceB));

            }
            imageView.setImage(image);
        }catch (Throwable t){
            throw new IllegalStateException("Could not set new image", t);
        }
    }

    @FXML
    @Override
    public void drag() {
        addDraggability(gui, container);
    }

    @FXML
    @Override
    public void close() {
        phone.hangup(callInstance.getId());

        callInstance.setCallDuration(time.getText());

        gui.hideWindow();

    }


    @Override
    public void initComponents() {
        if(callInstance != null){
            username.setText(callInstance.getSipAddress().toString());
            address.setText(callInstance.getProxyAddress().toString());

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    date.setText(callInstance.getCallDate());

                }
            });

            Status.show(phone, status);
        }else{
            throw new IllegalStateException("Could not initialize: There is no active call ");
        }

        buttons.put("hangUp", hangUpButton);
        buttons.put("mute", muteButton);
        buttons.put("record", recordButton);
        buttons.put("play", playButton);
        buttons.put("invite", inviteButton);
        buttons.put("contactList", contactListButton);






    }


}
