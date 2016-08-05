package com.zlf.appmaster.chartview.chart;

import android.view.View;

import com.zlf.appmaster.R;

class q implements android.view.View.OnClickListener {

    final KLineChart a;

    q(KLineChart klinechart) {
        super();
        a = klinechart;

    }

    public void onClick(View view) {
        long l = view.getId();
        if (l == (long) R.id.tv_repair_forward) {
            KLineChart.b(a, 1);
            if (KLineChart.d(a) != null)
                KLineChart.b(a);
            else
                KLineChart.e(a).setVisibility(0);
        } else if (l == (long) R.id.tv_repair_backward) {
            KLineChart.b(a, 2);
            if (KLineChart.f(a) != null)
                KLineChart.b(a);
            else
                KLineChart.e(a).setVisibility(0);
        } else if (l == (long) R.id.tv_repair_no) {
            KLineChart.e(a).setVisibility(8);
            KLineChart.b(a, 0);
            KLineChart.b(a);
        }
    }
}