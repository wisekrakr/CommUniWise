package com.wisekrakr.communiwise.screens.layouts;

import com.wisekrakr.communiwise.phone.Device;
import com.wisekrakr.communiwise.screens.ext.AbstractScreen;
import com.wisekrakr.communiwise.screens.ext.FrameContext;
import com.wisekrakr.communiwise.screens.layouts.objects.Button;
import org.apache.commons.lang3.time.StopWatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Clock;


public class AudioCallScreen extends AbstractScreen {

    private final Device device;
    private StopWatch stopWatch;

    public AudioCallScreen(Device device) throws HeadlessException {
        this.device = device;

        stopWatch = new StopWatch();

        initScreen();
    }

    @Override
    public void initScreen() {
        setTitle("Call with: "+ device.getSipManager().getSipProfile().getSipAddress());
        getContentPane().setLayout(null);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-288)/2, (screenSize.height-310)/2, 500, 600);

        showStatus();

        JLabel image = new JLabel(new ImageIcon("person.png"));
        image.setBounds(10,10, 480, 480);
        getContentPane().add(image);

        stopWatch.start();

        hangUpComponent();
        callTime();

        setVisible(true);
    }

    private void hangUpComponent(){
        Button stopBtn = new Button("hang up",10, 520, new Color(172, 15, 15));
        getContentPane().add(stopBtn);

        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                device.hangup();
                stopWatch.stop();
                System.out.println("Clicked hanging up");
            }
        });
    }

    private void callTime(){
        JLabel time = new JLabel("Current Call Time: ");
        time.setBounds(300, 520, 150, 30);
        getContentPane().add(time);


        JLabel callTime = new JLabel(String.valueOf(stopWatch.getTime()));
        callTime.setBounds(450, 520, 50, 30);
        getContentPane().add(callTime);

    }

    private void showStatus(){
        JLabel status = new JLabel();
        status.setBounds(200, 520, 150, 30);
        add(status);
        switch (device.getSipManager().getProcessedResponse().getStatusCode()){
            case 603:
                status.setText("Decline");
                status.setForeground(Color.ORANGE);
                break;
            case 486:
                status.setText("Busy");
                status.setForeground(Color.ORANGE);

                break;
            case 408:
                status.setText("Request Timeout");
                status.setForeground(Color.RED);

                break;
            case 403:
                status.setText("Forbidden");
                status.setForeground(Color.RED);

                break;
            case 401:
                status.setText("Unauthorized");
                status.setForeground(Color.RED);

                break;
            case 400:
                status.setText("Bad Request");
                status.setForeground(Color.RED);

                break;
            case 200:
                status.setText("OK");
                status.setForeground(Color.GREEN);

                break;
            case 100:
                status.setText("Trying");
                status.setForeground(Color.ORANGE);

                break;
            case 180:
                status.setText("Ringing");
                status.setForeground(Color.BLUE);

                break;
            case 183:
                status.setText("Session Progress");
                status.setForeground(Color.YELLOW);

                break;
            default:
                throw new IllegalStateException("Unexpected value: " + device.getSipManager().getProcessedResponse().getStatusCode());
        }

    }
}
