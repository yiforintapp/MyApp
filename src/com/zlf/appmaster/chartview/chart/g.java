package com.zlf.appmaster.chartview.chart;

import com.zlf.appmaster.chartview.view.LongClickImageView;

class g implements LongClickImageView.LongClickRepeatListener
{

    final KLineChart a;

    g(KLineChart klinechart) {
        super();
        a = klinechart;
    }

    public void repeatAction()
    {
        KLineChart.a(a, KLineChart.b());
    }
}