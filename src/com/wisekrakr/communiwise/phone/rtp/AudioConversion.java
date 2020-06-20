package com.wisekrakr.communiwise.phone.rtp;

import javax.sound.sampled.AudioFormat;

import static java.lang.Math.*;

public class AudioConversion {
    public AudioConversion() {}

    /**
     * Converts from a byte array to an audio sample float array.
     *
     * @param bytes   the byte array, filled by the AudioInputStream
     * @param samples an array to fill up with audio samples
     * @param blen    the return value of AudioInputStream.read
     * @param fmt     the source AudioFormat
     *
     * @return the number of valid audio samples converted
     *
     * @throws NullPointerException if bytes, samples or fmt is null
     * @throws ArrayIndexOutOfBoundsException
     *         if bytes.length is less than blen or
     *         if samples.length is less than blen / bytesPerSample(fmt.getSampleSizeInBits())
     */
    public static int decode(byte[]      bytes,
                             byte[] samples,
                             int         blen,
                             AudioFormat fmt) {
        int   bitsPerSample = fmt.getSampleSizeInBits();
        int  bytesPerSample = bytesPerSample(bitsPerSample);
        boolean isBigEndian = fmt.isBigEndian();
        AudioFormat.Encoding encoding = fmt.getEncoding();
        double    fullScale = fullScale(bitsPerSample);

        int i = 0;
        int s = 0;
        while (i < blen) {
            long temp = unpackBits(bytes, i, isBigEndian, bytesPerSample);
            byte sample = 0;

            if (encoding == AudioFormat.Encoding.PCM_SIGNED) {
                temp = extendSign(temp, bitsPerSample);
                sample = (byte) (temp / fullScale);

            } else if (encoding == AudioFormat.Encoding.PCM_UNSIGNED) {
                temp = unsignedToSigned(temp, bitsPerSample);
                sample = (byte) (temp / fullScale);

            } else if (encoding == AudioFormat.Encoding.PCM_FLOAT) {
                if (bitsPerSample == 32) {
                    sample = (byte) Float.intBitsToFloat((int) temp);
                } else if (bitsPerSample == 64) {
                    sample = (byte) Double.longBitsToDouble(temp);
                }
            } else if (encoding == AudioFormat.Encoding.ULAW) {
                sample = (byte) bitsToMuLaw(temp);

            } else if (encoding == AudioFormat.Encoding.ALAW) {
                sample = (byte) bitsToALaw(temp);
            }

            samples[s] = sample;

            i += bytesPerSample;
            s++;
        }

        return s;
    }

    /**
     * Converts from an audio sample float array to a byte array.
     *
     * @param samples an array of audio samples to encode
     * @param bytes   an array to fill up with bytes
     * @param slen    the return value of the decode method
     * @param fmt     the destination AudioFormat
     *
     * @return the number of valid bytes converted
     *
     * @throws NullPointerException if samples, bytes or fmt is null
     * @throws ArrayIndexOutOfBoundsException
     *         if samples.length is less than slen or
     *         if bytes.length is less than slen * bytesPerSample(fmt.getSampleSizeInBits())
     */
    public static int encode(byte[] samples,
                             byte[]      bytes,
                             int         slen,
                             AudioFormat fmt) {
        int   bitsPerSample = fmt.getSampleSizeInBits();
        int  bytesPerSample = bytesPerSample(bitsPerSample);
        boolean isBigEndian = fmt.isBigEndian();
        AudioFormat.Encoding encoding = fmt.getEncoding();
        double    fullScale = fullScale(bitsPerSample);

        int i = 0;
        int s = 0;
        while (s < slen) {
            byte sample = samples[s];
            long temp = 0L;

            if (encoding == AudioFormat.Encoding.PCM_SIGNED) {
                temp = (long) (sample * fullScale);

            } else if (encoding == AudioFormat.Encoding.PCM_UNSIGNED) {
                temp = (long) (sample * fullScale);
                temp = signedToUnsigned(temp, bitsPerSample);

            } else if (encoding == AudioFormat.Encoding.PCM_FLOAT) {
                if (bitsPerSample == 32) {
                    temp = Float.floatToRawIntBits(sample);
                } else if (bitsPerSample == 64) {
                    temp = Double.doubleToRawLongBits(sample);
                }
            } else if (encoding == AudioFormat.Encoding.ULAW) {
                temp = muLawToBits(sample);

            } else if (encoding == AudioFormat.Encoding.ALAW) {
                temp = aLawToBits(sample);
            }

            packBits(bytes, i, temp, isBigEndian, bytesPerSample);

            i += bytesPerSample;
            s++;
        }

        return i;
    }

    /**
     * Computes the block-aligned bytes per sample of the audio format,
     * using Math.ceil(bitsPerSample / 8.0).
     * <p>
     * Round towards the ceiling because formats that allow bit depths
     * in non-integral multiples of 8 typically pad up to the nearest
     * integral multiple of 8. So for example, a 31-bit AIFF file will
     * actually store 32-bit blocks.
     *
     * @param  bitsPerSample the return value of AudioFormat.getSampleSizeInBits
     * @return The block-aligned bytes per sample of the audio format.
     */
    public static int bytesPerSample(int bitsPerSample) {
        return (int) ceil(bitsPerSample / 8.0); // optimization: ((bitsPerSample + 7) >>> 3)
    }

    /**
     * Computes the largest magnitude representable by the audio format,
     * using Math.pow(2.0, bitsPerSample - 1). Note that for two's complement
     * audio, the largest positive value is one less than the return value of
     * this method.
     * <p>
     * The result is returned as a double because in the case that
     * bitsPerSample is 64, a long would overflow.
     *
     * @param bitsPerSample the return value of AudioFormat.getBitsPerSample
     * @return the largest magnitude representable by the audio format
     */
    public static double fullScale(int bitsPerSample) {
        return pow(2.0, bitsPerSample - 1); // optimization: (1L << (bitsPerSample - 1))
    }

    private static long unpackBits(byte[]  bytes,
                                   int     i,
                                   boolean isBigEndian,
                                   int     bytesPerSample) {
        switch (bytesPerSample) {
            case  1: return unpack8Bit(bytes, i);
            case  2: return unpack16Bit(bytes, i, isBigEndian);
            case  3: return unpack24Bit(bytes, i, isBigEndian);
            default: return unpackAnyBit(bytes, i, isBigEndian, bytesPerSample);
        }
    }

    private static long unpack8Bit(byte[] bytes, int i) {
        return bytes[i] & 0xffL;
    }

    private static long unpack16Bit(byte[]  bytes,
                                    int     i,
                                    boolean isBigEndian) {
        if (isBigEndian) {
            return (
                    ((bytes[i    ] & 0xffL) << 8)
                            |  (bytes[i + 1] & 0xffL)
            );
        } else {
            return (
                    (bytes[i    ] & 0xffL)
                            | ((bytes[i + 1] & 0xffL) << 8)
            );
        }
    }

    private static long unpack24Bit(byte[]  bytes,
                                    int     i,
                                    boolean isBigEndian) {
        if (isBigEndian) {
            return (
                    ((bytes[i    ] & 0xffL) << 16)
                            | ((bytes[i + 1] & 0xffL) <<  8)
                            |  (bytes[i + 2] & 0xffL)
            );
        } else {
            return (
                    (bytes[i    ] & 0xffL)
                            | ((bytes[i + 1] & 0xffL) <<  8)
                            | ((bytes[i + 2] & 0xffL) << 16)
            );
        }
    }

    private static long unpackAnyBit(byte[]  bytes,
                                     int     i,
                                     boolean isBigEndian,
                                     int     bytesPerSample) {
        long temp = 0;

        if (isBigEndian) {
            for (int b = 0; b < bytesPerSample; b++) {
                temp |= (bytes[i + b] & 0xffL) << (
                        8 * (bytesPerSample - b - 1)
                );
            }
        } else {
            for (int b = 0; b < bytesPerSample; b++) {
                temp |= (bytes[i + b] & 0xffL) << (8 * b);
            }
        }

        return temp;
    }

    private static void packBits(byte[]  bytes,
                                 int     i,
                                 long    temp,
                                 boolean isBigEndian,
                                 int     bytesPerSample) {
        switch (bytesPerSample) {
            case  1: pack8Bit(bytes, i, temp);
                break;
            case  2: pack16Bit(bytes, i, temp, isBigEndian);
                break;
            case  3: pack24Bit(bytes, i, temp, isBigEndian);
                break;
            default: packAnyBit(bytes, i, temp, isBigEndian, bytesPerSample);
                break;
        }
    }

    private static void pack8Bit(byte[] bytes, int i, long temp) {
        bytes[i] = (byte) (temp & 0xffL);
    }

    private static void pack16Bit(byte[]  bytes,
                                  int     i,
                                  long    temp,
                                  boolean isBigEndian) {
        if (isBigEndian) {
            bytes[i    ] = (byte) ((temp >>> 8) & 0xffL);
            bytes[i + 1] = (byte) ( temp        & 0xffL);
        } else {
            bytes[i    ] = (byte) ( temp        & 0xffL);
            bytes[i + 1] = (byte) ((temp >>> 8) & 0xffL);
        }
    }

    private static void pack24Bit(byte[]  bytes,
                                  int     i,
                                  long    temp,
                                  boolean isBigEndian) {
        if (isBigEndian) {
            bytes[i    ] = (byte) ((temp >>> 16) & 0xffL);
            bytes[i + 1] = (byte) ((temp >>>  8) & 0xffL);
            bytes[i + 2] = (byte) ( temp         & 0xffL);
        } else {
            bytes[i    ] = (byte) ( temp         & 0xffL);
            bytes[i + 1] = (byte) ((temp >>>  8) & 0xffL);
            bytes[i + 2] = (byte) ((temp >>> 16) & 0xffL);
        }
    }

    private static void packAnyBit(byte[]  bytes,
                                   int     i,
                                   long    temp,
                                   boolean isBigEndian,
                                   int     bytesPerSample) {
        if (isBigEndian) {
            for (int b = 0; b < bytesPerSample; b++) {
                bytes[i + b] = (byte) (
                        (temp >>> (8 * (bytesPerSample - b - 1))) & 0xffL
                );
            }
        } else {
            for (int b = 0; b < bytesPerSample; b++) {
                bytes[i + b] = (byte) ((temp >>> (8 * b)) & 0xffL);
            }
        }
    }

    private static long extendSign(long temp, int bitsPerSample) {
        int bitsToExtend = Long.SIZE - bitsPerSample;
        return (temp << bitsToExtend) >> bitsToExtend;
    }

    private static long unsignedToSigned(long temp, int bitsPerSample) {
        return temp - (long) fullScale(bitsPerSample);
    }

    private static long signedToUnsigned(long temp, int bitsPerSample) {
        return temp + (long) fullScale(bitsPerSample);
    }

    // mu-law constant
    private static final double MU = 255.0;
    // A-law constant
    private static final double A = 87.7;
    // natural logarithm of A
    private static final double LN_A = log(A);

    private static float bitsToMuLaw(long temp) {
        temp ^= 0xffL;
        if ((temp & 0x80L) != 0) {
            temp = -(temp ^ 0x80L);
        }

        float sample = (float) (temp / fullScale(8));

        return (float) (
                signum(sample)
                        *
                        (1.0 / MU)
                        *
                        (pow(1.0 + MU, abs(sample)) - 1.0)
        );
    }

    private static long muLawToBits(float sample) {
        double sign = signum(sample);
        sample = abs(sample);

        sample = (float) (
                sign * (log(1.0 + (MU * sample)) / log(1.0 + MU))
        );

        long temp = (long) (sample * fullScale(8));

        if (temp < 0) {
            temp = -temp ^ 0x80L;
        }

        return temp ^ 0xffL;
    }

    private static float bitsToALaw(long temp) {
        temp ^= 0x55L;
        if ((temp & 0x80L) != 0) {
            temp = -(temp ^ 0x80L);
        }

        float sample = (float) (temp / fullScale(8));

        float sign = signum(sample);
        sample = abs(sample);

        if (sample < (1.0 / (1.0 + LN_A))) {
            sample = (float) (sample * ((1.0 + LN_A) / A));
        } else {
            sample = (float) (exp((sample * (1.0 + LN_A)) - 1.0) / A);
        }

        return sign * sample;
    }

    private static long aLawToBits(float sample) {
        double sign = signum(sample);
        sample = abs(sample);

        if (sample < (1.0 / A)) {
            sample = (float) ((A * sample) / (1.0 + LN_A));
        } else {
            sample = (float) ((1.0 + log(A * sample)) / (1.0 + LN_A));
        }

        sample *= sign;

        long temp = (long) (sample * fullScale(8));

        if (temp < 0) {
            temp = -temp ^ 0x80L;
        }

        return temp ^ 0x55L;
    }
}
