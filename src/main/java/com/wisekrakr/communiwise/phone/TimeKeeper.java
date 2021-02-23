package com.wisekrakr.communiwise.phone;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeKeeper {

    private Timeline clock;
    private String callTime;

    public String getCallTime() {

        return callTime;
    }

    public void start(){
        clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {

            long time = System.currentTimeMillis();

            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date(time);

            callTime = dateFormat.format(date);

        }), new KeyFrame(Duration.seconds(1)));

        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        System.out.println(clock.currentTimeProperty());
    }

    public void stop() {
        clock.stop();
//        clock.pause();
    }

    public static void main(String[] args) {

        TimeKeeper timeKeeper = new TimeKeeper();
        timeKeeper.start();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame jFrame = new JFrame();
                JLabel label = new JLabel();

                label.setText(timeKeeper.getCallTime());
                jFrame.add(label, BorderLayout.CENTER);

                jFrame.setPreferredSize(new Dimension(300,300));
                jFrame.pack();
                jFrame.setVisible(true);
            }
        });

    }
}
