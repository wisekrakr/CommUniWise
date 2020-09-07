package com.wisekrakr.communiwise.phone;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

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
        //            clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    public void stop() {
        clock.stop();
//        clock.pause();
    }
}
