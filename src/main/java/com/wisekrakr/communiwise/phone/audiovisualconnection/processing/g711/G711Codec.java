package com.wisekrakr.communiwise.phone.audiovisualconnection.processing.g711;

public class G711Codec {
    /** decompress table constants */
    private static short aLawDecompressTable[] = new short[]
            { -5504, -5248, -6016, -5760, -4480, -4224, -4992, -4736, -7552, -7296, -8064, -7808, -6528, -6272, -7040, -6784, -2752, -2624, -3008, -2880, -2240, -2112, -2496, -2368, -3776, -3648, -4032, -3904, -3264, -3136, -3520, -3392, -22016, -20992, -24064, -23040, -17920, -16896, -19968, -18944, -30208, -29184, -32256, -31232, -26112, -25088, -28160, -27136, -11008, -10496, -12032, -11520, -8960, -8448, -9984, -9472, -15104, -14592, -16128, -15616, -13056, -12544, -14080, -13568, -344, -328, -376,
                    -360, -280, -264, -312, -296, -472, -456, -504, -488, -408, -392, -440, -424, -88, -72, -120, -104, -24, -8, -56, -40, -216, -200, -248, -232, -152, -136, -184, -168, -1376, -1312, -1504, -1440, -1120, -1056, -1248, -1184, -1888, -1824, -2016, -1952, -1632, -1568, -1760, -1696, -688, -656, -752, -720, -560, -528, -624, -592, -944, -912, -1008, -976, -816, -784, -880, -848, 5504, 5248, 6016, 5760, 4480, 4224, 4992, 4736, 7552, 7296, 8064, 7808, 6528, 6272, 7040, 6784, 2752, 2624,
                    3008, 2880, 2240, 2112, 2496, 2368, 3776, 3648, 4032, 3904, 3264, 3136, 3520, 3392, 22016, 20992, 24064, 23040, 17920, 16896, 19968, 18944, 30208, 29184, 32256, 31232, 26112, 25088, 28160, 27136, 11008, 10496, 12032, 11520, 8960, 8448, 9984, 9472, 15104, 14592, 16128, 15616, 13056, 12544, 14080, 13568, 344, 328, 376, 360, 280, 264, 312, 296, 472, 456, 504, 488, 408, 392, 440, 424, 88, 72, 120, 104, 24, 8, 56, 40, 216, 200, 248, 232, 152, 136, 184, 168, 1376, 1312, 1504, 1440, 1120,
                    1056, 1248, 1184, 1888, 1824, 2016, 1952, 1632, 1568, 1760, 1696, 688, 656, 752, 720, 560, 528, 624, 592, 944, 912, 1008, 976, 816, 784, 880, 848 };

    private final static int cClip = 32635;
    private static byte aLawCompressTable[] = new byte[]
            { 1, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 };

    public int encode( byte[] src, int offset, int len, byte[] res ) {
        int j = offset;
        int count = len / 2;
        short sample = 0;

        for ( int i = 0; i < count; i++ ) {
            sample = (short) ( ( ( src[j++] & 0xff ) | ( src[j++] ) << 8 ) );
            res[i] = linearToALawSample( sample );
        }
        return count;
    }

    private byte linearToALawSample( short sample ){
        int sign;
        int exponent;
        int mantissa;
        int s;

        sign = ( ( ~sample ) >> 8 ) & 0x80;
        if ( !( sign == 0x80 ) ){
            sample = (short) -sample;
        }
        if ( sample > cClip ){
            sample = cClip;
        }
        if ( sample >= 256 ){
            exponent = (int) aLawCompressTable[( sample >> 8 ) & 0x7F];
            mantissa = ( sample >> ( exponent + 3 ) ) & 0x0F;
            s = ( exponent << 4 ) | mantissa;
        }else{
            s = sample >> 4;
        }
        s ^= ( sign ^ 0x55 );
        return (byte) s;
    }

//    public void decode( byte[] src, int offset, int len, byte[] res ){
//        int j = 0;
//        for ( int i = 0; i < len; i++ ){
//            short s = aLawDecompressTable[src[i + offset] & 0xff];
//            res[j++] = (byte) s;
//            res[j++] = (byte) ( s >> 8 );
//        }
//
//    }


    public byte[] decode(byte[] media) {
        byte[] res = new byte[media.length * 2];
        int j = 0;
        for (int i = 0; i < media.length; i++) {
            short s = aLawDecompressTable[media[i] & 0xff];
            res[j++] = (byte) s;
            res[j++] = (byte) (s >> 8);
        }
        return res;

    }
}
