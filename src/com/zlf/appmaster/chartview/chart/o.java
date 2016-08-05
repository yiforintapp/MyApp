package com.zlf.appmaster.chartview.chart;

import android.view.View;

import com.zlf.appmaster.R;

class o implements android.view.View.OnClickListener {

    final KLineChart a;

    o(KLineChart klinechart) {
        super();
        a = klinechart;
    }

    public void onClick(View view) {
        long l = view.getId();
        if (l == (long) R.id.tv_select_volume)
            KLineChart.c(a, 0);
        else if (l == (long) R.id.tv_select_kdj)
            KLineChart.c(a, 1);
        else if (l == (long) R.id.tv_select_macd)
            KLineChart.c(a, 2);
        KLineChart.h(a);
    }
}