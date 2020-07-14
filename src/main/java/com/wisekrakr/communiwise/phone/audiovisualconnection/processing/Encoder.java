/*
    This file is part of Peers, a java SIP softphone.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    Copyright 2008, 2009, 2010, 2011 Yohann Martineau 
*/

package com.wisekrakr.communiwise.phone.audiovisualconnection.processing;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;


public abstract class Encoder implements Runnable {
    
    private PipedInputStream rawData;
    private PipedOutputStream encodedData;
    private boolean isStopped;
    private FileOutputStream encoderOutput;
    private FileOutputStream encoderInput;
    private boolean mediaDebug;
    private String peersHome;
    private CountDownLatch latch;

    public Encoder(PipedInputStream rawData, PipedOutputStream encodedData,
                   boolean mediaDebug,  String peersHome,
                   CountDownLatch latch) {
        this.rawData = rawData;
        this.encodedData = encodedData;
        this.mediaDebug = mediaDebug;
        this.peersHome = peersHome;
        this.latch = latch;
        isStopped = false;
    }

    public Encoder() {
    }

    public void run() {
        byte[] buffer;
        if (mediaDebug) {
            SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String date = simpleDateFormat.format(new Date());
            String dir = peersHome + File.separator
                    + "media" + File.separator;
            String fileName = dir + date + "_g711_encoder.output";
            try {
                encoderOutput = new FileOutputStream(fileName);
                fileName = dir + date + "_g711_encoder.input";
                encoderInput = new FileOutputStream(fileName);
            } catch (FileNotFoundException e) {
                return;
            }
        }
        int ready;
        while (!isStopped) {
            try {
                ready = rawData.available();
                while (ready == 0 && !isStopped) {
                    try {
                        Thread.sleep(2);
                        ready = rawData.available();
                    } catch (InterruptedException e) {
                        System.out.println(" Encoder error: " + e.getMessage());
                    }
                }
                if (isStopped) {
                    break;
                }
                buffer = new byte[ready];
                rawData.read(buffer);
                if (mediaDebug) {
                    try {
                        encoderInput.write(buffer);
                    } catch (IOException e) {
                        System.out.println(" Encoder error: " + e.getMessage());

                    }
                }
            } catch (IOException e) {
                return;
            }

            byte[] ulawData = process(buffer);
            if (mediaDebug) {
                try {
                    encoderOutput.write(ulawData);
                } catch (IOException e) {
                    break;
                }
            }
            try {
                encodedData.write(ulawData);
                encodedData.flush();
            } catch (IOException e) {
                return;
            }
        }
        if (mediaDebug) {
            try {
                encoderOutput.close();
                encoderInput.close();
            } catch (IOException e) {
                return;
            }
        }
        latch.countDown();
        if (latch.getCount() != 0) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                System.out.println(" Encoder latch error: " + e.getMessage());

            }
        }
    }

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    public abstract byte[] process(byte[] media);

}
