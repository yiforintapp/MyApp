package com.zlf.appmaster.chartview.chart;

import android.view.View;

class d implements android.view.View.OnClickListener {

    final KLineChart a;

    d(KLineChart klinechart) {
        super();
        a = klinechart;
    }

    public void onClick(View view) {
        KLineChart.a(a, KLineChart.a());
    }
}