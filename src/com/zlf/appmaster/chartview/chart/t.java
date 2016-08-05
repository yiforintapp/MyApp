package com.zlf.appmaster.chartview.chart;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

class t extends Handler {

    private WeakReference b;
    final MinuteLine a;

    t(MinuteLine minuteline, MinuteLine minuteline1) {
        super();
        a = minuteline;
        b = new WeakReference(minuteline1);
    }

    public void handleMessage(Message message) {
        if (b == null)
            return;
        MinuteLine minuteline = (MinuteLine) b.get();
        if (minuteline == null) {
            return;
        } else {
            MinuteLine.a(minuteline);
            return;
        }
    }
}