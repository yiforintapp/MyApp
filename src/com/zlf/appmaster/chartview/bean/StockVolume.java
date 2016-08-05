package com.zlf.appmaster.chartview.bean;

import java.io.Serializable;

public class StockVolume
  implements Serializable, Cloneable
{
  private long a;
  private boolean b;

  public long getTradeCount()
  {
    return this.a;
  }

  public void setTradeCount(long paramLong)
  {
    this.a = paramLong;
  }

  public boolean isUp()
  {
    return this.b;
  }

  public void setUp(boolean paramBoolean)
  {
    this.b = paramBoolean;
  }

  public StockVolume clone()
  {
    try {
      return (StockVolume) super.clone();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;   //观察，自己添加的保护
  }
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.bean.StockVolume
 * JD-Core Version:    0.6.2
 */

