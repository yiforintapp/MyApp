package com.zlf.appmaster.chartview.chart;

import java.util.TimerTask;

class s extends TimerTask {

    final MinuteLine a;

    s(MinuteLine minuteline) {
        super();
        a = minuteline;
    }

    public void run() {
        if (MinuteLine.b(a) != null)
            MinuteLine.b(a).sendEmptyMessage(0);
    }
}