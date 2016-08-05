package com.zlf.appmaster.chartview.dimension;

import android.os.Handler;
import android.os.Message;

class c extends Handler {

    final DimensionChart a;

    public c(DimensionChart dimensionchart) {
        super();
        a = dimensionchart;
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        if (DimensionChart.a(a) < 10) {
            if (DimensionChart.b(a) != null) {
                for (int i = 0; i < DimensionChart.c(a).a.size(); i++)
                    DimensionChart.c(a).a.set(i, Float.valueOf(((Float) DimensionChart.c(a).a.get(i)).floatValue() + ((Float) DimensionChart.b(a).get(i)).floatValue()));

            }
            a.postInvalidate();
            DimensionChart.d(a).sendEmptyMessageDelayed(0, 50L);
            DimensionChart.e(a);
        }
    }
}