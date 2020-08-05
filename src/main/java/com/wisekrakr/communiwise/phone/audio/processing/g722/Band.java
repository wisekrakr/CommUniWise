package com.wisekrakr.communiwise.phone.audio.processing.g722;

import static com.wisekrakr.communiwise.phone.audio.processing.g722.G722Codec.saturate;

public class Band {

    int _s;
    int _sp;
    int _sz;
    int _r[] = new int[3];
    int _a[] = new int[3];
    int _ap[] = new int[3];
    int _p[] = new int[3];
    int _d[] = new int[7];
    int _b[] = new int[7];
    int _bp[] = new int[7];
    int _sg[] = new int[7];
    int _nb;
    int _det;

    void block4(int d) {
        int wd1;
        int wd2;
        int wd3;
        int i;

        /* Block 4, RECONS */
        _d[0] = d;
        _r[0] = saturate(_s + d);

        /* Block 4, PARREC */
        _p[0] = saturate(_sz + d);

        /* Block 4, UPPOL2 */
        for (i = 0; i < 3; i++) {
            _sg[i] = _p[i] >> 15;
        }
        wd1 = saturate(_a[1] << 2);

        wd2 = (_sg[0] == _sg[1]) ? -wd1 : wd1;
        if (wd2 > 32767) {
            wd2 = 32767;
        }
        wd3 = (_sg[0] == _sg[2]) ? 128 : -128;
        wd3 += (wd2 >> 7);
        wd3 += (_a[2] * 32512) >> 15;
        if (wd3 > 12288) {
            wd3 = 12288;
        } else if (wd3 < -12288) {
            wd3 = -12288;
        }
        _ap[2] = wd3;

        /* Block 4, UPPOL1 */
        _sg[0] = _p[0] >> 15;
        _sg[1] = _p[1] >> 15;
        wd1 = (_sg[0] == _sg[1]) ? 192 : -192;
        wd2 = (_a[1] * 32640) >> 15;

        _ap[1] = saturate(wd1 + wd2);
        wd3 = saturate(15360 - _ap[2]);
        if (_ap[1] > wd3) {
            _ap[1] = wd3;
        } else if (_ap[1] < -wd3) {
            _ap[1] = -wd3;
        }

        /* Block 4, UPZERO */
        wd1 = (d == 0) ? 0 : 128;
        _sg[0] = d >> 15;
        for (i = 1; i < 7; i++) {
            _sg[i] = _d[i] >> 15;
            wd2 = (_sg[i] == _sg[0]) ? wd1 : -wd1;
            wd3 = (_b[i] * 32640) >> 15;
            _bp[i] = saturate(wd2 + wd3);
        }

        /* Block 4, DELAYA */
        for (i = 6; i > 0; i--) {
            _d[i] = _d[i - 1];
            _b[i] = _bp[i];
        }

        for (i = 2; i > 0; i--) {
            _r[i] = _r[i - 1];
            _p[i] = _p[i - 1];
            _a[i] = _ap[i];
        }

        /* Block 4, FILTEP */
        wd1 = saturate(_r[1] + _r[1]);
        wd1 = (_a[1] * wd1) >> 15;
        wd2 = saturate(_r[2] + _r[2]);
        wd2 = (_a[2] * wd2) >> 15;
        _sp = saturate(wd1 + wd2);

        /* Block 4, FILTEZ */
        _sz = 0;
        for (i = 6; i > 0; i--) {
            wd1 = saturate(_d[i] + _d[i]);
            _sz += (_b[i] * wd1) >> 15;
        }
        _sz = saturate(_sz);

        /* Block 4, PREDIC */
        _s = saturate(_sp + _sz);
    }

}
