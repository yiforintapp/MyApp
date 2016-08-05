package com.zlf.appmaster.chartview.chart;

import android.view.MotionEvent;
import android.view.View;

class j
        implements android.view.View.OnTouchListener {

    final KLineChart a;

    j(KLineChart klinechart) {
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
                    else if (motionevent.getAction() == 2)
                        KLineChart.c(a, f);
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