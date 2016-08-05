package com.zlf.appmaster.chartview.bean;


import java.util.ArrayList;
import java.util.List;

public class StockMACD
{
  private float a;
  private float b;
  private float c;

  public static List<StockMACD> getDataByStockKLine(List<StockKLine> paramList)
  {
    ArrayList localArrayList = new ArrayList();
    int i = paramList.size();
    if (i > 0)
    {
      float f1 = ((StockKLine)paramList.get(0)).getClose();
      float f2 = ((StockKLine)paramList.get(0)).getClose();
      float f3 = 0.0F;
      float f4 = 0.0F;
      float f5 = 0.0F;
      localArrayList.add(new StockMACD());
      for (int j = 1; j < i; j++)
      {
        float f6 = ((StockKLine)paramList.get(j)).getClose();
        StockMACD localStockMACD = new StockMACD();
        f3 = a(0.07407408F, f1, f6);
        f4 = a(0.1538462F, f2, f6);
        localStockMACD.a = (f4 - f3);
        localStockMACD.b = a(0.2F, f5, localStockMACD.a);
        localStockMACD.c = ((localStockMACD.a - localStockMACD.b) * 2.0F);
        f1 = f3;
        f2 = f4;
        f5 = localStockMACD.b;
        localArrayList.add(localStockMACD);
      }
    }
    return localArrayList;
  }

  private static float a(float paramFloat1, float paramFloat2, float paramFloat3)
  {
    return paramFloat1 * paramFloat3 + (1.0F - paramFloat1) * paramFloat2;
  }

  public float getDIFF()
  {
    return this.a;
  }

  public void setDIFF(float paramFloat)
  {
    this.a = paramFloat;
  }

  public float getDEA()
  {
    return this.b;
  }

  public void setDEA(float paramFloat)
  {
    this.b = paramFloat;
  }

  public float getMACD()
  {
    return this.c;
  }

  public void setMACD(float paramFloat)
  {
    this.c = paramFloat;
  }
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.bean.StockMACD
 * JD-Core Version:    0.6.2
 */