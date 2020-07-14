package com.wisekrakr.communiwise.phone.audiovisualconnection.rtp;



import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;



public class Capture implements Runnable {

    public static final int SAMPLE_SIZE = 16;
    public static final int BUFFER_SIZE = SAMPLE_SIZE * 20;

    private PipedOutputStream rawData;
    private boolean isStopped;
    private CountDownLatch latch;
    private TargetDataLine targetDataLine;

    public Capture(PipedOutputStream rawData, CountDownLatch latch, TargetDataLine targetDataLine) {
        this.rawData = rawData;
        this.latch = latch;
        this.targetDataLine = targetDataLine;
        isStopped = false;
    }

    public void run() {
        targetDataLine.start();

        byte[] buffer;

        while (!isStopped) {
            buffer = readData();
            try {
                if (buffer == null) {
                    break;
                }
                rawData.write(buffer);
                rawData.flush();
            } catch (IOException e) {
                System.out.println("input/output error "+ e);
                return;
            }
        }
        latch.countDown();
        if (latch.getCount() != 0) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                System.out.println("input/output error "+ e);

            }
        }
        targetDataLine.stop();
    }

    private synchronized byte[] readData() {

        if (targetDataLine == null) {
            return null;
        }
        int ready = targetDataLine.available();
        while (ready == 0) {
            try {
                Thread.sleep(2);
                ready = targetDataLine.available();
            } catch (InterruptedException e) {
                return null;
            }
        }
        if (ready <= 0) {
            return null;
        }
        byte[] buffer = new byte[ready];
        targetDataLine.read(buffer, 0, buffer.length);
        return buffer;

    }

    public synchronized void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

}
