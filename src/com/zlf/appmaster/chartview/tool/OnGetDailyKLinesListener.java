package com.zlf.appmaster.chartview.tool;

import com.zlf.appmaster.chartview.bean.StockKLine;
import java.util.ArrayList;

public abstract interface OnGetDailyKLinesListener
{
  public abstract void onDataFinish(ArrayList<StockKLine> paramArrayList);

  public abstract void onError();
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.tool.OnGetDailyKLinesListener
 * JD-Core Version:    0.6.2
 */