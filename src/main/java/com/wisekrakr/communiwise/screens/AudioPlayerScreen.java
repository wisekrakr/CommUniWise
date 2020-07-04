package com.wisekrakr.communiwise.screens;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import javax.media.*;

public class AudioPlayerScreen extends JFrame implements ActionListener, ControllerListener{

    private final JButton open;
    private final JButton play;
    private final JButton fastForward;
    private final JButton fastRewind;
    private final JButton pause;
    private final JButton stop;

    private final JSlider slider;

    private final JTextField tf;

    private final JPanel panelForButtons = new JPanel();
    //panel will contain buttons and slider
    private final JPanel panelForSlider = new JPanel();
    private File f;
    private javax.media.Player createdPlayer =null;

    private java.util.Timer timer;

    private Container container;

    private int flagForDisplayRate =0;
    private Time playedTimeForPause;

    private int playerStarted =0;
    private static float forwardRate =1;

    public AudioPlayerScreen()
    {
        super("CommUniWise Player");
        open =new JButton("○");
        play =new JButton("►");
        fastForward =new JButton("»");
        fastRewind =new JButton("«");
        pause =new JButton("ll");
        stop =new JButton("■");
        slider =new JSlider(SwingConstants.HORIZONTAL, 0,100,0);
        tf=new JTextField(3);
        playedTimeForPause =new Time(0.0);
    }

    public void player_gui()
    {
        panelForButtons.setLayout(new FlowLayout());
        panelForSlider.setLayout(new FlowLayout());
        panelForSlider.add(slider);

        panelForButtons.add(open);
        panelForButtons.add(play);
        panelForButtons.add(fastForward);
        panelForButtons.add(fastRewind);
        panelForButtons.add(pause);
        panelForButtons.add(stop);

        panelForButtons.add(tf);

        container =this.getContentPane();

        container.add(panelForButtons, BorderLayout.SOUTH);

        container.add(panelForSlider, BorderLayout.NORTH);
        open.addActionListener(this);
        play.addActionListener(this);
        stop.addActionListener(this);
        fastForward.addActionListener(this);
        pause.addActionListener(this);

    }

    public File open()
    {
        JFileChooser chooser= new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.showOpenDialog(this);
        return chooser.getSelectedFile();
    }

    public void displayrate()
    {
        long player_time= createdPlayer.getMediaNanoseconds()/1000000000;
        long minutesForTextfield = player_time / 60;
        long secondsForTextfield = player_time % 60;
        tf.setText(minutesForTextfield +":"+ secondsForTextfield);
        long totalTime = (long) (createdPlayer.getDuration().getSeconds());
        long playedTime = createdPlayer.getMediaNanoseconds() / 1000000000;
        int sliderValue = (int) ((playedTime * 100) / totalTime);
        slider.setValue(sliderValue);
    }

    public void Reminder()
    {
        timer=new java.util.Timer();
        timer.scheduleAtFixedRate(new RemindTask(),1000,1);
    }

    class RemindTask extends java.util.TimerTask
    {
        public void run()
        {
            displayrate();
            timer.cancel();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource()== open)
        {
            try
            {
                f=open();
            }
            catch(Exception fx)
            {
                fx.printStackTrace();
            }
        }
        if(e.getSource()== play)
        {
            if(playerStarted ==1)
            {
                createdPlayer.setMediaTime(playedTimeForPause);
            }else
            {
                try
                {
                    createdPlayer = Manager.createPlayer(f.toURI().toURL());
                    createdPlayer.addControllerListener(this);
                    displayrate();
                }
                catch(Exception ex)
                {
                    System.err.println("Got exception "+ex);
                }
            }
            createdPlayer.start();
            playerStarted =1;
            if(flagForDisplayRate ==0)
            {
                Reminder();
                Reminder();
                flagForDisplayRate =1;
            }
        }
        if(e.getSource()== stop)
        {
            createdPlayer.stop();
            createdPlayer.deallocate();
            playedTimeForPause =new Time(0.0);
            playerStarted =0;
        }
        if(e.getSource()== fastForward)
        {
            forwardRate +=.5f;
            createdPlayer.setRate(forwardRate);
        }
        if(e.getSource()== pause)
        {
            playedTimeForPause = createdPlayer.getMediaTime();
            createdPlayer.stop();
        }
    }
    public synchronized void controllerUpdate(ControllerEvent event)
    {
        if (event instanceof RealizeCompleteEvent)
        {
            Component comp;
            if ((comp = createdPlayer.getVisualComponent()) != null)
                container.add(comp, BorderLayout.CENTER, 1);
            //resize window as per its components
            pack();
        }
    }


}

