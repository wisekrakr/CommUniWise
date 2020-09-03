package com.wisekrakr.communiwise.gui.layouts.utils;

import com.wisekrakr.communiwise.operations.apis.PhoneAPI;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class Status {
    public static void show(PhoneAPI phone, Text status) {
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
