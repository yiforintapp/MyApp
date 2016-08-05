package com.zlf.appmaster.chartview.chart;

import android.view.View;


class h implements android.view.View.OnClickListener
{

  final KLineChart a;

  h(KLineChart klinechart)
  {
    super();
    a = klinechart;
  }

  public void onClick(View view)
  {
    KLineChart.c(a).onChange();
  }
}