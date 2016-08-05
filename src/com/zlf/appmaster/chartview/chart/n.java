package com.zlf.appmaster.chartview.chart;

import android.view.ScaleGestureDetector;

class n implements android.view.ScaleGestureDetector.OnScaleGestureListener
{

    private float b;
    private float c;
    final KLineChart a;

    private n(KLineChart klinechart)
    {
        super();
        a = klinechart;
    }

    public boolean onScale(ScaleGestureDetector scalegesturedetector)
    {
        KLineChart.a(a, true);
        c = scalegesturedetector.getCurrentSpan();
        float f = c / b;
        KLineChart.a(a, 1.0F / f);
        b = c;
        return false;
    }

    public boolean onScaleBegin(ScaleGestureDetector scalegesturedetector)
    {
        b = scalegesturedetector.getCurrentSpan();
        c = scalegesturedetector.getCurrentSpan();
        return true;
    }

    public void onScaleEnd(ScaleGestureDetector scalegesturedetector)
    {
        KLineChart.a(a, false);
    }

    n(KLineChart klinechart, d d)
    {
        this(klinechart);
    }
}