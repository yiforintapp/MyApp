package com.zlf.appmaster.chartview.chart;

import android.view.View;

class p implements android.view.View.OnClickListener {

    final KLineChart a;

    p(KLineChart klinechart) {
        super();
        a = klinechart;

    }

    public void onClick(View view) {
        if (KLineChart.g(a) == 0)
            KLineChart.c(a, 1);
        else if (KLineChart.g(a) == 1)
            KLineChart.c(a, 2);
        else if (KLineChart.g(a) == 2)
            KLineChart.c(a, 0);
        KLineChart.h(a);
        a.setCurExtraText();
    }
}