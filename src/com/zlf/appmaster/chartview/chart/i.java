package com.zlf.appmaster.chartview.chart;

import java.util.TimerTask;

class i extends TimerTask {

    final KLineChart a;

    i(KLineChart klinechart) {
        super();
        a = klinechart;
    }

    public void run() {
        if (KLineChart.r(a) != null)
            KLineChart.r(a).sendEmptyMessage(0);
    }
}