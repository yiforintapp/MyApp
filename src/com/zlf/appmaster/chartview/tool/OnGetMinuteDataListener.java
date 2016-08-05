package com.zlf.appmaster.chartview.tool;

import com.zlf.appmaster.chartview.bean.StockMinutes;
import java.util.ArrayList;

public abstract interface OnGetMinuteDataListener
{
  public abstract void onDataFinish(ArrayList<StockMinutes> paramArrayList);

  public abstract void onError();
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.tool.OnGetMinuteDataListener
 * JD-Core Version:    0.6.2
 */