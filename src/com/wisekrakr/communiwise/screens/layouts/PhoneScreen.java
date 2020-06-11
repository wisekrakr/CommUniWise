package com.wisekrakr.communiwise.screens.layouts;


import com.wisekrakr.communiwise.phone.Device;
import com.wisekrakr.communiwise.phone.audio.impl.AudioClip;
import com.wisekrakr.communiwise.screens.ext.AbstractScreen;
import com.wisekrakr.communiwise.screens.ext.FrameContext;
import com.wisekrakr.communiwise.screens.layouts.objects.Button;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PhoneScreen extends AbstractScreen {

    private JLabel status;

    private JTextField sipAddress;
    private Device device;


    public PhoneScreen(Device device)  {
        this.device = device;

        initScreen();
    }

    @Override
    public void initScreen() {

        getContentPane().setLayout(null);
        setTitle("CommUniWise Phone");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImage(new ImageIcon("https://raw.githubusercontent.com/wisekrakr/portfolio_res/master/images/favicon/favicon-32x32.png").getImage());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-288)/2, (screenSize.height-310)/2, 800, 800);
        setBackground(new Color(176,228,234));

        JLabel statusText = new JLabel("status: ");
        statusText.setBounds(650, 20, 100, 30);
        getContentPane().add(statusText);
        status = new JLabel();
        getContentPane().add(status);
        status.setBounds(700, 20, 100, 30);

        setVisible(true);

        System.out.println("PhoneScreen lights up!");

        handleCallingAndAccepting();
        handlePlayingSoundClip();
    }

    private void handleCallingAndAccepting(){
        JLabel destination = new JLabel("destination");
        sipAddress = new JTextField();
        Button callBtn = new Button("call",10, 400);
        Button acceptBtn = new Button("accept",120, 400 );
        Button stopBtn = new Button("hang up",230, 400);

        getContentPane().add(destination);
        destination.setBounds(10, 300, 100, 30);
        getContentPane().add(sipAddress);
        sipAddress.setBounds(10, 360, 100, 30);

        getContentPane().add(callBtn);

        callBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                device.call("sip:"+sipAddress.getText().trim());
                device.getSipManager().getSipProfile().setSipAddress(sipAddress.getText());
            }
        });

        getContentPane().add(acceptBtn);
        getContentPane().add(stopBtn);

        acceptBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                device.accept();
                System.out.println("Clicked accept");

                acceptBtn.setEnabled(false);
                stopBtn.setEnabled(true);
            }
        });
        stopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                device.hangup();
                System.out.println("Clicked hanging up");
                stopBtn.setEnabled(false);
                acceptBtn.setEnabled(true);


            }
        });
    }

    /**
     * @see Device#onSipMessage
     */
    public void showStatus(){
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

    /**
     * Press a button and play a sound.
     * Todo: add choosing own sounds
     */
    private void handlePlayingSoundClip(){
        JButton playBtn = new JButton("play beep");
        playBtn.setBounds(120, 300, 100, 30);
        getContentPane().add(playBtn);

        AudioClip audioClip = new AudioClip();
        audioClip.createClipURL("https://file-examples.com/wp-content/uploads/2017/11/file_example_WAV_1MG.wav");

        playBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!audioClip.getClip().isActive()){
                    audioClip.getClip().start();
                }else{
                    audioClip.getClip().stop();
                }

            }
        });
    }


}
