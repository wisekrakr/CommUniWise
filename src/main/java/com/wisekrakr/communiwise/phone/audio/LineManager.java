package com.wisekrakr.communiwise.phone.audio;

import javax.sound.sampled.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

public class LineManager {

    private Mixer.Info[] mixers = AudioSystem.getMixerInfo();
    public static final AudioFormat FORMAT = new AudioFormat(16000, 16, 1, true, true);

    //todo find right mixer
    //todo create new lines for every call or remote sound play
    //todo put them all in a map and when done, find the right line and close it.

    public void initMixer(){



    }



//    MouseListener handler = new MouseAdapter() {
//
//        @Override
//        public void mouseClicked(MouseEvent e) {
//            if (e.getSource() == play) {
//                resume();
//            } else if (e.getSource() == pause) {
//                pause();
//            }
//        }
//
//    };

    public TargetDataLine createTargetDataLine(String inputDevice) throws LineUnavailableException {
        TargetDataLine targetDataLine = null;

        for (int i = 0; i < mixers.length; i++) {
            if (inputDevice.equals(mixers[i].getName())) {
                targetDataLine = (TargetDataLine) AudioSystem.getMixer(mixers[i]).getLine(new DataLine.Info(TargetDataLine.class, FORMAT));
            }
        }
        return targetDataLine;
    }

    public SourceDataLine createSourceDataLine(String outputDevice) throws LineUnavailableException {
        SourceDataLine sourceDataLine = null;
        for (int i = 0; i < mixers.length; i++) {
            if (outputDevice.equals(mixers[i].getName())) {
                sourceDataLine = (SourceDataLine) AudioSystem.getMixer(mixers[i]).getLine(new DataLine.Info(SourceDataLine.class, FORMAT));
            }
        }

        return sourceDataLine;
    }


    public void pause(TargetDataLine targetDataLine) {

        if (targetDataLine != null && targetDataLine.isRunning()) {
            BooleanControl control = (BooleanControl) targetDataLine.getControl(BooleanControl.Type.MUTE);
            control.setValue(true);
        }

    }

    public void resume(TargetDataLine targetDataLine) {

        if (targetDataLine != null && !targetDataLine.isRunning()) {
            BooleanControl control = (BooleanControl) targetDataLine.getControl(BooleanControl.Type.MUTE);
            control.setValue(false);
        }

    }
}
