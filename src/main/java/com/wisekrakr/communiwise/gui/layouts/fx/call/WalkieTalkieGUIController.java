package com.wisekrakr.communiwise.gui.layouts.fx.call;

import com.wisekrakr.communiwise.gui.EventManager;
import com.wisekrakr.communiwise.gui.layouts.AbstractJFXPanel;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerContext;
import com.wisekrakr.communiwise.operations.apis.SoundAPI;
import com.wisekrakr.communiwise.user.SipAccountManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import javax.swing.*;
import java.net.InetSocketAddress;
import java.util.Map;

import static com.wisekrakr.communiwise.gui.layouts.utils.SipAddressMaker.make;

public class WalkieTalkieGUIController extends AbstractJFXPanel implements ControllerContext {


    private final WalkieTalkieGUI walkieTalkieGUI;
    private final EventManager eventManager;
    private final SoundAPI sound;
    private final Map<String, String> userInfo;
    private final String proxyName;
    private final InetSocketAddress proxyAddress;

    private boolean isMuted = true;
    private int counter;

    @FXML
    private Label username,domain, proxy;
    @FXML
    private Button closeButton;
    @FXML
    private AnchorPane textPane, container;
    @FXML
    private Button talkButton;

    public WalkieTalkieGUIController(WalkieTalkieGUI walkieTalkieGUI, EventManager eventManager, SoundAPI sound, Map<String, String> userInfo, String proxyName, InetSocketAddress proxyAddress) {
        this.walkieTalkieGUI = walkieTalkieGUI;
        this.eventManager = eventManager;
        this.sound = sound;
        this.userInfo = userInfo;
        this.proxyName = proxyName;
        this.proxyAddress = proxyAddress;

    }

    @FXML
    public void close() {
//        eventManager.onHangUp();
    }

    @Override
    public void drag() {
        addDraggability(walkieTalkieGUI, container);
    }

    @FXML
    private void pushed(){
        talkButton.setText("Talking!");
        talkButton.setTextFill(Color.GREEN);

        sound.unmute();
    }

    @FXML
    private void released(){
        talkButton.setText("Push to Talk");
        talkButton.setTextFill(Color.RED);

        sound.mute();
    }

    @Override
    public void initComponents() {
        username.setText(userInfo.get(SipAccountManager.UserInfoPart.USERNAME.getInfoPart()));
        domain.setText(userInfo.get(SipAccountManager.UserInfoPart.DOMAIN.getInfoPart()));
        proxy.setText(make(proxyName, proxyAddress.getHostName()));

        closeButton.setGraphic(addIconForButton());

        textPane.setMouseTransparent(true);

        walkieTalkieGUI.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(walkieTalkieGUI,
                        "Are you sure you want to end the call?", "End Call with " + proxy.getText() +  "?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    System.out.println("Closing App");
                    close();
                }
            }
        });
    }

    private static ImageView addIconForButton(){
        ImageView image;

        String path = "/images/exit.png";

        try {
            image = new ImageView(String.valueOf(WalkieTalkieGUI.class.getResource(path)));

            image.setFitWidth(15);
            image.setFitHeight(15);

            return image;


        }catch (Throwable t){
            throw new IllegalArgumentException("Could not find path to image " + path,t);
        }
    }
}
