package com.wisekrakr.communiwise.phone.audio.processing.g722;

/*
G.722[1] is a ITU-T 16 kHz (with 14 bits per sample) wideband speech codec standard operating at 48, 56 and 64 kbps with an encoding frame length of 10 ms.
Technology of the codec is based on sub-band ADPCM (SB-ADPCM). G.722 sample audio data at a rate of 16 kHz (using 14 bits) with an encoding frame length of
10 ms, double that of traditional telephony interfaces, which results in superior audio quality and clarity.
 */

import com.wisekrakr.communiwise.phone.audio.processing.utils.CodecUtil;

public class G722Decoder {

    private final int bitsPerSample = 8;
    private final boolean ituTestMode = false;
    private final boolean packed = false;
    private final boolean eightK = false;
    private final int[] xInts = new int[24];
    private final Band[] mainBand;
    private int inBuffer;
    private int inBits;

    public G722Decoder() {
        mainBand = new Band[2];
        mainBand[0] = new Band();
        mainBand[1] = new Band();

        mainBand[0]._det = 32;
        mainBand[1]._det = 8;
    }

    private static final int[] wl = {-60, -30, 58, 172, 334, 538, 1198, 3042};
    private static final int[] rl42 = {0, 7, 6, 5, 4, 3, 2, 1, 7, 6, 5, 4, 3, 2, 1, 0};
    private static final int[] ilb = {
            2048, 2093, 2139, 2186, 2233, 2282, 2332,
            2383, 2435, 2489, 2543, 2599, 2656, 2714,
            2774, 2834, 2896, 2960, 3025, 3091, 3158,
            3228, 3298, 3371, 3444, 3520, 3597, 3676,
            3756, 3838, 3922, 4008
    };
    private static final int[] qm4 = {
            0, -20456, -12896, -8968,
            -6288, -4240, -2584, -1200,
            20456, 12896, 8968, 6288,
            4240, 2584, 1200, 0
    };
    private static final int[] qm2 = {-7408, -1616, 7408, 1616};
    private static final int[] qmfCoeffs = {3, -11, 12, 32, -210, 951, 3876, -805, 362, -156, 53, -11};
    private static final int[] wh = {0, -214, 798};
    private static final int[] rh2 = {2, 1, 2, 1};
    private static final int[] qm5 = {
            -280, -280, -23352, -17560,
            -14120, -11664, -9752, -8184,
            -6864, -5712, -4696, -3784,
            -2960, -2208, -1520, -880,
            23352, 17560, 14120, 11664,
            9752, 8184, 6864, 5712,
            4696, 3784, 2960, 2208,
            1520, 880, 280, -280
    };
    private static final int[] qm6 = {
            -136, -136, -136, -136,
            -24808, -21904, -19008, -16704,
            -14984, -13512, -12280, -11192,
            -10232, -9360, -8576, -7856,
            -7192, -6576, -6000, -5456,
            -4944, -4464, -4008, -3576,
            -3168, -2776, -2400, -2032,
            -1688, -1360, -1040, -728,
            24808, 21904, 19008, 16704,
            14984, 13512, 12280, 11192,
            10232, 9360, 8576, 7856,
            7192, 6576, 6000, 5456,
            4944, 4464, 4008, 3576,
            3168, 2776, 2400, 2032,
            1688, 1360, 1040, 728,
            432, 136, -432, -136
    };




    public byte[] decode(byte[] rtpPacketData, int length) {
        //why is 160 so important? The coder works on a frame of 160 speech samples.
        //320 here because a short(16) is twice the length of a byte (8).
        short[] decoded = new short[320];

        int dlowt;
        int rlow;
        int ihigh;
        int dhigh;
        int rhigh;
        int xout1;
        int xout2;
        int wd1;
        int wd2;
        int wd3;
        int code;
        int outlen;
        int i;
        int j;

        outlen = 0;
        rhigh = 0;
        for (j = 0; j < length; ) {
            if (packed) {
                /* Unpack the code bits */
                if (inBits < bitsPerSample) {
                    inBuffer |= (rtpPacketData[j++] << inBits);
                    inBits += 8;
                }
                code = inBuffer & ((1 << bitsPerSample) - 1);
                inBuffer >>= bitsPerSample;
                inBits -= bitsPerSample;
            } else {
                code = rtpPacketData[j++];
            }

            switch (bitsPerSample) {
                default:
                case 8:
                    wd1 = code & 0x3F;
                    ihigh = (code >> 6) & 0x03;
                    wd2 = qm6[wd1];
                    wd1 >>= 2;
                    break;
                case 7:
                    wd1 = code & 0x1F;
                    ihigh = (code >> 5) & 0x03;
                    wd2 = qm5[wd1];
                    wd1 >>= 1;
                    break;
                case 6:
                    wd1 = code & 0x0F;
                    ihigh = (code >> 4) & 0x03;
                    wd2 = qm4[wd1];
                    break;
            }
            /* Block 5L, LOW BAND INVQBL */
            wd2 = (mainBand[0]._det * wd2) >> 15;
            /* Block 5L, RECONS */
            rlow = mainBand[0]._s + wd2;
            /* Block 6L, LIMIT */
            if (rlow > 16383) {
                rlow = 16383;
            } else if (rlow < -16384) {
                rlow = -16384;
            }

            /* Block 2L, INVQAL */
            wd2 = qm4[wd1];
            dlowt = (mainBand[0]._det * wd2) >> 15;

            /* Block 3L, LOGSCL */
            wd2 = rl42[wd1];
            wd1 = (mainBand[0]._nb * 127) >> 7;
            wd1 += wl[wd2];
            if (wd1 < 0) {
                wd1 = 0;
            } else if (wd1 > 18432) {
                wd1 = 18432;
            }
            mainBand[0]._nb = wd1;

            /* Block 3L, SCALEL */
            wd1 = (mainBand[0]._nb >> 6) & 31;
            wd2 = 8 - (mainBand[0]._nb >> 11);
            wd3 = (wd2 < 0) ? (ilb[wd1] << -wd2) : (ilb[wd1] >> wd2);
            mainBand[0]._det = wd3 << 2;

            mainBand[0].block4(dlowt);

            if (!eightK) {
                /* Block 2H, INVQAH */
                wd2 = qm2[ihigh];
                dhigh = (mainBand[1]._det * wd2) >> 15;
                /* Block 5H, RECONS */
                rhigh = dhigh + mainBand[1]._s;
                /* Block 6H, LIMIT */
                if (rhigh > 16383) {
                    rhigh = 16383;
                } else if (rhigh < -16384) {
                    rhigh = -16384;
                }

                /* Block 2H, INVQAH */
                wd2 = rh2[ihigh];
                wd1 = (mainBand[1]._nb * 127) >> 7;
                wd1 += wh[wd2];
                if (wd1 < 0) {
                    wd1 = 0;
                } else if (wd1 > 22528) {
                    wd1 = 22528;
                }
                mainBand[1]._nb = wd1;

                /* Block 3H, SCALEH */
                wd1 = (mainBand[1]._nb >> 6) & 31;
                wd2 = 10 - (mainBand[1]._nb >> 11);
                wd3 = (wd2 < 0) ? (ilb[wd1] << -wd2) : (ilb[wd1] >> wd2);
                mainBand[1]._det = wd3 << 2;

                mainBand[1].block4(dhigh);
            }

            if (ituTestMode) {
                decoded[outlen++] = (short) (rlow << 1);
                decoded[outlen++] = (short) (rhigh << 1);
            } else {
                if (eightK) {
                    decoded[outlen++] = (short) rlow;
                } else {
                    /* Apply the receive QMF */
                    for (i = 0; i < 22; i++) {
                        xInts[i] = xInts[i + 2];
                    }
                    xInts[22] = rlow + rhigh;
                    xInts[23] = rlow - rhigh;

                    xout1 = 0;
                    xout2 = 0;
                    for (i = 0; i < 12; i++) {
                        xout2 += xInts[2 * i] * qmfCoeffs[i];
                        xout1 += xInts[2 * i + 1] * qmfCoeffs[11 - i];
                    }
                    decoded[outlen++] = (short) (xout1 >>> 12);
                    decoded[outlen++] = (short) (xout2 >>> 12);
                }
            }
        }

        return CodecUtil.shortsToBytes(decoded, outlen);
    }

}
