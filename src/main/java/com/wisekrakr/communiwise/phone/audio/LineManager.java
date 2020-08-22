package com.wisekrakr.communiwise.phone.audio;

import javax.sound.sampled.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

public class LineManager {

    private final Mixer.Info[] mixers = AudioSystem.getMixerInfo();
    private Mixer mixer;

    public static final AudioFormat FORMAT = new AudioFormat(16000, 16, 1, true, true);

    private final HashMap<String, DataLine>workingLines = new HashMap<>();

    //todo find right mixer
    //todo create new lines for every call or remote sound play
    //todo put them all in a map and when done, find the right line and close it.

    public void initMixer(){

    }

    public Mixer getMixer() {
        return mixer;
    }

    public void setMixer(Mixer mixer) {
        this.mixer = mixer;
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

    public TargetDataLine createTargetDataLine(String inputDevice) {
        TargetDataLine targetDataLine = null;

        try {
            for (int i = 0; i < mixers.length; i++) {
                if(inputDevice != null){

                    if (inputDevice.equals(mixers[i].getName())) {
                        targetDataLine = (TargetDataLine) AudioSystem.getMixer(mixers[i]).getLine(new DataLine.Info(TargetDataLine.class, FORMAT));
                    }
                }else {
                    if(mixers[i].getName().toLowerCase().contains("mic")){
                        targetDataLine = (TargetDataLine) AudioSystem.getMixer(mixers[i]).getLine(new DataLine.Info(TargetDataLine.class, FORMAT));
                    }else{
                        targetDataLine = (TargetDataLine) AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, FORMAT));

                    }
                }
            }

            workingLines.put(inputDevice != null ? inputDevice : String.valueOf(targetDataLine.hashCode()), targetDataLine);
        }catch (LineUnavailableException e){
            throw new IllegalStateException(" TargetDataLine not available",e);
        }



        return targetDataLine;
    }

    public SourceDataLine createSourceDataLine(String outputDevice){
        SourceDataLine sourceDataLine = null;
        try {
            for (int i = 0; i < mixers.length; i++) {
                if(outputDevice != null){

                    if (outputDevice.equals(mixers[i].getName())) {
                        sourceDataLine = (SourceDataLine) AudioSystem.getMixer(mixers[i]).getLine(new DataLine.Info(SourceDataLine.class, FORMAT));
                    }
                }else {
                    if(mixers[i].getName().toLowerCase().contains("speaker")){
                        sourceDataLine = (SourceDataLine) AudioSystem.getMixer(mixers[i]).getLine(new DataLine.Info(SourceDataLine.class, FORMAT));
                    } else {
                        sourceDataLine = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, FORMAT));

                    }
                }
            }
            workingLines.put(outputDevice != null ? outputDevice : String.valueOf(sourceDataLine.hashCode()), sourceDataLine);

        }catch (LineUnavailableException e){
            throw new IllegalStateException(" SourceDataLine not available",e);
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
