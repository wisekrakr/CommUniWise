package com.wisekrakr.communiwise.gui.layouts.fx.call;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerContext;
import com.wisekrakr.communiwise.gui.layouts.utils.Constants;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;
import com.wisekrakr.communiwise.phone.calling.CallInstance;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AudioCallController implements ControllerContext, Initializable {

    private static PhoneAPI phone;
    private static SoundAPI sound;
    private static AbstractGUI gui;
    private static CallInstance callInstance;

    private final Map<String, Button> buttons = new HashMap<>();

    @FXML
    private Button muteButton, recordButton, hangUpButton, playButton, inviteButton, contactListButton;
    @FXML
    private Label username, address;
    @FXML
    private ImageView muteImage, recordImage, playImage;
    @FXML
    private Text status;

    private boolean isMuted, isRecording,isPlaying;

    public AudioCallController initialize(PhoneAPI phone, SoundAPI sound, AbstractGUI gui, CallInstance callInstance){
        AudioCallController.phone = phone;
        AudioCallController.sound = sound;
        AudioCallController.gui = gui;
        AudioCallController.callInstance = callInstance;

        return this;
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

    @Override
    public void close() {
        phone.hangup(callInstance.getId());

        gui.hideWindow();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(callInstance != null){
            username.setText(callInstance.getSipAddress().toString());
            address.setText(callInstance.getProxyAddress().toString());

            showStatus();
        }else{
            throw new IllegalStateException("Could not initialize: There is no active call ");
        }

        buttons.put("hangUp", hangUpButton);
        buttons.put("mute", muteButton);
        buttons.put("record", recordButton);
        buttons.put("play", playButton);
        buttons.put("invite", inviteButton);
        buttons.put("contactList", contactListButton);

//        onHover();
    }

    private void showStatus() {
        System.out.println("STATUS BITCH   ========> " + phone.callStatus());
        switch (phone.callStatus()){
            case 603:
                status.setText("Decline");
                status.setFill(Color.ORANGE);
                break;
            case 486:
                status.setText("Busy");
                status.setFill(Color.ORANGE);
                break;
            case 408:
                status.setText("Request Timeout");
                status.setFill(Color.RED);
                break;
            case 403:
                status.setText("Forbidden");
                status.setFill(Color.RED);
                break;
            case 401:
                status.setText("Unauthorized");
                status.setFill(Color.RED);
                break;
            case 400:
                status.setText("Bad Request");
                status.setFill(Color.RED);
                break;
            case 200:
                status.setText("OK");
                status.setFill(Color.GREEN);
                break;
            case 100:
                status.setText("Trying");
                status.setFill(Color.ORANGE);
                break;
            case 180:
                status.setText("Ringing");
                status.setFill(Color.BLUE);
                break;
            case 183:
                status.setText("Session Progress");
                status.setFill(Color.YELLOW);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + phone.callStatus());
        }

    }
}
