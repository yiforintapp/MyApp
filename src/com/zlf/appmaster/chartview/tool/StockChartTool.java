package com.zlf.appmaster.chartview.tool;

public abstract class StockChartTool
{
  public abstract void getDailyKLines(OnGetDailyKLinesListener paramOnGetDailyKLinesListener);

  public abstract void getMinuteData(OnGetMinuteDataListener paramOnGetMinuteDataListener);

  public abstract void getHandicapData(OnGetHandicapListener paramOnGetHandicapListener);
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.tool.StockChartTool
 * JD-Core Version:    0.6.2
 */