package com.wisekrakr.communiwise.gui.layouts.fx.call;

import com.wisekrakr.communiwise.gui.layouts.AbstractGUI;
import com.wisekrakr.communiwise.gui.layouts.AbstractJFXPanel;
import com.wisekrakr.communiwise.gui.layouts.fx.ControllerContext;
import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import com.wisekrakr.communiwise.user.history.CallInstance;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import javax.swing.*;
import java.security.SecureRandom;
import java.util.stream.Collectors;

public class IncomingCallGUIController extends AbstractJFXPanel implements ControllerContext {
    private AbstractGUI gui;
    private CallInstance callInstance;
    private PhoneAPI phone;
    private Timer timer;

    @FXML
    private AnchorPane container;
    @FXML
    private Label username;

    public IncomingCallGUIController(PhoneAPI phone, AbstractGUI gui, CallInstance callInstance) {
        this.phone = phone;
        this.gui = gui;
        this.callInstance = callInstance;
    }

    @FXML
    @Override
    public void close() {
        phone.reject();

        gui.hideWindow();
    }

    @Override
    public void drag() {
    }

    @FXML
    private void accept(){
        phone.accept(callInstance.getProxyAddress().getAddress().getHostAddress());
    }

    @FXML
    private void reject(){
        phone.reject();
    }

    @Override
    public void initComponents() {
        if(callInstance != null){
            username.setText(callInstance.getDisplayName());
        }
    }

    @FXML
    private void showInfo(){
//        Platform.runLater(()->{
//            timer = new Timer(1, new ActionListener() {
//                public void actionPerformed(ActionEvent e) {
//                    String randomCode = createRandomCode(8, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
//
//                    username.setText(randomCode);
//
//                }
//            });
//            timer.start();
//        });

//        if(callInstance != null){
//            username.setText(callInstance.getDisplayName());
//        }
//        timer.stop();
    }

    private static String createRandomCode(int codeLength, String id) {
        return new SecureRandom()
                .ints(codeLength, 0, id.length())
                .mapToObj(id::charAt)
                .map(Object::toString)
                .collect(Collectors.joining());
    }
}
