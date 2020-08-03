package com.wisekrakr.communiwise.phone.audio.processing.utils;

public class CodecUtil {

    public static short[] bytesToShorts(byte byteBuffer[], int l) {
        int len = l/2;
        short[] output = new short[len];
        int j = 0;

        for (int i = 0; i < len; i++) {
            output[i] = (short) (byteBuffer[j++] << 8);
            output[i] |= (byteBuffer[j++] & 0xff);
        }
        return output;
    }

    public static byte[] shortsToBytes(short shortBuffer[], int len) {
        byte[] output = new byte[len*2];
        int j = 0;

        for (int i = 0; i < len; i++) {
            output[j++] = (byte) (shortBuffer[i] >>> 8);
            output[j++] = (byte) (0xff & shortBuffer[i]);
        }
        return output;
    }
}
