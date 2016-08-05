package com.zlf.appmaster.chartview.chart;

import com.zlf.appmaster.chartview.view.LongClickImageView;

class e implements LongClickImageView.LongClickRepeatListener
{

    final KLineChart a;

    e(KLineChart klinechart)

    {  
        super();
        a = klinechart;

    }

    public void repeatAction()
    {
        KLineChart.a(a, KLineChart.a());
    }
}