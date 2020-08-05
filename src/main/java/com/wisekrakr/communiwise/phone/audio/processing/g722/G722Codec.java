package com.wisekrakr.communiwise.phone.audio.processing.g722;

public class G722Codec {
    public int getFrameSize() {
        return 160;
    }

    public int getFrameInterval() {
        return 20;
    }

    final static short saturate(int amp) {
        short amp16;

        /* Hopefully this is optimised for the common case - not clipping */
        amp16 = (short) amp;
        if (amp == amp16) {
            return amp16;
        }
        if (amp > Short.MAX_VALUE) {
            return Short.MAX_VALUE;
        }
        return Short.MIN_VALUE;
    }

    public float getSampleRate() {
        return 16000.0F;
    }

    public String getName() {
        return "G.722";
    }
}
