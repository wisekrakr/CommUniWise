package com.wisekrakr.communiwise.gui.layouts.fx.call;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerJFXPanel;
import com.wisekrakr.communiwise.gui.layouts.utils.Constants;
import com.wisekrakr.communiwise.gui.layouts.utils.Status;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;
import com.wisekrakr.communiwise.user.history.CallInstance;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

public class AudioCallController extends ControllerJFXPanel {

    private final EventManager eventManager;
    private final PhoneAPI phone;
    private final SoundAPI sound;
    private final AbstractGUI gui;
    private final CallInstance callInstance;

    private final Map<String, Button> buttons = new HashMap<>();
//    private TimeKeeper timeKeeper;

    @FXML
    private Button muteButton, recordButton, hangUpButton, playButton, inviteButton, contactListButton;
    @FXML
    private Label username, address,time,date;
    @FXML
    private ImageView muteImage, recordImage, playImage;
    @FXML
    private Text status;

    private boolean isMuted, isRecording,isPlaying;

    public AudioCallController(EventManager eventManager, PhoneAPI phone, SoundAPI sound, AbstractGUI gui, CallInstance callInstance){
        this.eventManager = eventManager;
        this.phone = phone;
        this.sound = sound;
        this.gui = gui;
        this.callInstance = callInstance;

    }

    private void onHover(){
        for(Button button: buttons.values()){

            button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    button.setBorder(new Border(
                            new BorderStroke(Color.rgb(Constants.SUNSET_ORANGE.getRed(), Constants.SUNSET_ORANGE.getGreen(), Constants.SUNSET_ORANGE.getBlue()),
                                    BorderStrokeStyle.SOLID,
                                    CornerRadii.EMPTY,
                                    BorderWidths.FULL)));
                }
            });
        }

    }

    @FXML
    private void mute(){
        clickButtonForAction(isMuted, muteImage, "/images/mute.png","/images/unmute.png");
        isMuted = !isMuted;
    }

    @FXML
    private void record(){
        clickButtonForAction(isRecording, recordImage, "/images/record.png","/images/not-record.png");

        isRecording = !isRecording;
    }

    @FXML
    private void play(){
        clickButtonForAction(isPlaying, playImage, "/images/play.png","/images/pause.png");

        isPlaying = !isPlaying;

    }

    @FXML
    private void invite(){

    }

    @FXML
    private void openContactList(){
        eventManager.menuContactListOpen();
    }

    private void clickButtonForAction(boolean isDoing, ImageView imageView, String resourceA, String resourceB){
        Image image;
        try {
            if(!isDoing){
                image = new Image(getClass().getResourceAsStream(resourceA));

                if(imageView.getId().equals(recordImage.getId())){
                    sound.startRecording();
                }else if(imageView.getId().equals(playImage.getId())){
                    sound.playRemoteSound("/sounds/shake_bake.wav");
                }
            }else{
                image = new Image(getClass().getResourceAsStream(resourceB));

                if(imageView.getId().equals(recordImage.getId())){
                    sound.stopRecording();
                }else if(imageView.getId().equals(playImage.getId())){
                    sound.stopRemoteSound();
                }
            }
            imageView.setImage(image);
        }catch (Throwable t){
            throw new IllegalStateException("Could not set new image", t);
        }
    }

    @FXML
    private void keyPressed(KeyEvent event) {
        System.out.println("pressed");
        switch (event.getCode()) {
            case SPACE:
                mute();
                break;
            case Z:
                close();
                break;
            default:
                System.out.println("This key has not been bound to an action");
                break;
        }
    }

    @FXML
    public void close() {
        phone.hangup(callInstance.getId());

        callInstance.setCallDuration(time.getText());

        gui.hideWindow();

//        timeKeeper.stop();

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

//        timeKeeper = new TimeKeeper();
//        timeKeeper.start();

//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                time.setText(timeKeeper.getCallTime());
//            }
//        });

    }

}
