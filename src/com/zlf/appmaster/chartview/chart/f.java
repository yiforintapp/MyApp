package com.zlf.appmaster.chartview.chart;

import android.view.View;

class f implements android.view.View.OnClickListener
{

    final KLineChart a;

    f(KLineChart klinechart)
    {
        super();
        a = klinechart;

    }

    public void onClick(View view)
    {
        KLineChart.a(a, KLineChart.b());
    }
}