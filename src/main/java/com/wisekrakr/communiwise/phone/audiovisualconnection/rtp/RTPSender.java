package com.wisekrakr.communiwise.phone.audiovisualconnection.rtp;/*
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


import java.io.IOException;
import java.io.PipedInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;
import java.util.concurrent.CountDownLatch;


public class RTPSender implements Runnable {

    private final PipedInputStream encodedData;
    private boolean isStopped;

    private final CountDownLatch latch;
    private final DatagramSocket socket;

    private final RTPParser rtpParser;

    public RTPSender(PipedInputStream encodedData, CountDownLatch latch, DatagramSocket socket) {
        this.encodedData = encodedData;
        this.latch = latch;
        this.socket = socket;

        isStopped = false;
        rtpParser = new RTPParser();
    }

    @Override
    public void run() {

        RTPPacket rtpPacket = new RTPPacket();
        rtpPacket.setVersion(2);
        rtpPacket.setPadding(false);
        rtpPacket.setExtension(false);
        rtpPacket.setCsrcCount(0);
        rtpPacket.setMarker(false);
        rtpPacket.setPayloadType(0); //PCMU == 0   PCMA == 8    telephone-event == 101
        Random random = new Random();
        int sequenceNumber = random.nextInt();
        rtpPacket.setSequenceNumber(sequenceNumber);
        rtpPacket.setSsrc(random.nextInt());
        byte[] buffer = new byte[2500]; // todo wrong size?
        int timestamp = 0;
        int numBytesRead;
        int tempBytesRead;
        long sleepTime = 0;
        long offset = 0;
        long lastSentTime = System.nanoTime();
        // indicate if its the first time that we send a packet (dont wait)
        boolean firstTime = true;


        while (!isStopped) {
            numBytesRead = 0;
            try {
                while (!isStopped && numBytesRead < buffer.length) {
                    // expect that the buffer is full? todo  the train stopped here
                    tempBytesRead = encodedData.read(buffer, numBytesRead, buffer.length - numBytesRead);

                    numBytesRead += tempBytesRead;
                }
            } catch (IOException e) {
                System.out.println("input/output error " + e);
                return;
            }
            byte[] trimmedBuffer;
            if (numBytesRead < buffer.length) {
                trimmedBuffer = new byte[numBytesRead];
                System.arraycopy(buffer, 0, trimmedBuffer, 0, numBytesRead);
            } else {
                trimmedBuffer = buffer;
            }

            rtpPacket.setData(trimmedBuffer); // todo now:  rtpPacket.getData().length == buf.length



            rtpPacket.setSequenceNumber(sequenceNumber++);
            if (rtpPacket.isIncrementTimeStamp()) {
                timestamp += buffer.length;
            }
            rtpPacket.setTimestamp(timestamp);
            if (firstTime) {
                send(rtpPacket);
                lastSentTime = System.nanoTime();
                firstTime = false;
                continue;
            }
            sleepTime = 19500000 - (System.nanoTime() - lastSentTime) + offset;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(Math.round(sleepTime / 1000000f));
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted " + e);
                    return;
                }
                send(rtpPacket);
                lastSentTime = System.nanoTime();
                offset = 0;
            } else {
                send(rtpPacket);
                lastSentTime = System.nanoTime();
                if (sleepTime < -20000000) {
                    offset = sleepTime + 20000000;
                }
            }
        }

        latch.countDown();
        if (latch.getCount() != 0) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                System.out.println("interrupt exception " + e);
            }
        }
    }

    private void send(RTPPacket rtpPacket) {
        if (socket == null) {
            return;
        }
        byte[] buf = rtpParser.encode(rtpPacket);
        final DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, socket.getInetAddress(), socket.getPort());

        if (!socket.isClosed()) {
            try {
                socket.send(datagramPacket);

                System.out.println("Mic is sending data: "  + datagramPacket.getLength()); //todo now:  datagramPacket.length == buf.length + 12 from encoded data

            } catch (IOException | SecurityException e) {
                System.out.println(" error while sending datagram packet "+ e);
            }
        }
    }

    public void setStopped(boolean stopped) {
        isStopped = stopped;
    }
}
