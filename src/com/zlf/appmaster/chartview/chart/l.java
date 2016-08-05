package com.zlf.appmaster.chartview.chart;

import android.view.View;
import android.view.View.OnClickListener;

import com.zlf.appmaster.R;

class l
        implements android.view.View.OnClickListener {

    private boolean b;
    final KLineChart a;

    private l(KLineChart klinechart) {
        super();
        a = klinechart;
        b = false;
    }

    private void a(boolean flag) {
        if (flag) {
            KLineChart.s(a).setImageResource(R.drawable.btn_arrow_2_left);
            KLineChart.t(a).setVisibility(0);
            KLineChart.u(a).setVisibility(0);
        } else {
            KLineChart.s(a).setImageResource(R.drawable.btn_arrow_2_right);
            KLineChart.t(a).setVisibility(8);
            KLineChart.u(a).setVisibility(8);
        }
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.kline_move_handle)
            if (!b) {
                a(true);
                b = true;
            } else {
                a(false);
                b = false;
            }
    }

    l(KLineChart klinechart, d d) {
        this(klinechart);
    }
}