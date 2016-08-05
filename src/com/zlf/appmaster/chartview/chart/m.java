package com.zlf.appmaster.chartview.chart;

import android.os.Handler;
import android.os.Message;


import java.lang.ref.WeakReference;

class m extends Handler {

    private WeakReference b;
    final KLineChart a;

    m(KLineChart klinechart, KLineChart klinechart1) {
        super();
        a = klinechart;
        b = new WeakReference(klinechart1);
    }

    public void handleMessage(Message message) {
        if (b == null)
            return;
        KLineChart klinechart = (KLineChart) b.get();
        if (klinechart == null)
            return;
        if (message.what == 0)
            KLineChart.a(klinechart);
        else if (message.what == 1)
            KLineChart.b(klinechart);
    }
}