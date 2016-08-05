package com.zlf.appmaster.chartview.chart;

import android.view.MotionEvent;
import android.view.View;

class k implements android.view.View.OnTouchListener {

    final KLineChart a;

    k(KLineChart klinechart) {
        super();
        a = klinechart;
    }

    public boolean onTouch(View view, MotionEvent motionevent) {
        if (KLineChart.i(a) != null) {
            int i = motionevent.getPointerCount();
            if (i == 1 && !KLineChart.j(a)) {
                if (KLineChart.k(a)) {
                    if (motionevent.getAction() == 0 || motionevent.getAction() == 2) {
                        KLineChart.l(a).setVisibility(0);
                        KLineChart.b(a, motionevent.getX());
                    } else if (motionevent.getAction() == 1) {
                        KLineChart.l(a).setVisibility(8);
                        KLineChart.b(a, false);
                        if (KLineChart.c(a) != null) {
                            KLineChart.m(a).setCurStockKLineInfo(-1);
                            KLineChart.c(a).onTouchUp();
                        }
                    }
                } else {
                    float f = motionevent.getX();
                    if (motionevent.getAction() == 0)
                        KLineChart.c(a, f);
                    else if (motionevent.getAction() == 2) {
                        if (KLineChart.i(a).size() < KLineChart.n(a)) {
                            KLineChart.d(a, 0);
                        } else {
                            int j = KLineChart.m(a).a(KLineChart.o(a)) - KLineChart.m(a).a(f);
                            if (j < 0) {
                                if (KLineChart.p(a) + j > 0)
                                    KLineChart.d(a, KLineChart.p(a) + j);
                                else
                                    KLineChart.d(a, 0);
                            } else if (j > 0) {
                                if (KLineChart.p(a) + j > KLineChart.i(a).size() - KLineChart.n(a))
                                    KLineChart.d(a, KLineChart.i(a).size() - KLineChart.n(a));
                                else
                                    KLineChart.d(a, KLineChart.p(a) + j);
                            } else {
                                return false;
                            }
                        }
                        KLineChart.c(a, true);
                        KLineChart.c(a, f);
                    }
                    return KLineChart.a(a, motionevent);
                }
            } else {
                KLineChart.l(a).setVisibility(8);
                if (KLineChart.c(a) != null) {
                    KLineChart.m(a).setCurStockKLineInfo(-1);
                    KLineChart.c(a).onTouchUp();
                }
                if (KLineChart.q(a) != null)
                    KLineChart.q(a).onTouchEvent(motionevent);
            }
            return true;
        } else {
            return true;
        }
    }
}