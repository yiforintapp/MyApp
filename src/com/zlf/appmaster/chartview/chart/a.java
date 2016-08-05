package com.zlf.appmaster.chartview.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public abstract class a extends View
{
  public a(Context paramContext)
  {
    super(paramContext);
  }

  public a(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
  }

  public a(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
  }

  public abstract void setSourceData(Object paramObject);

  public abstract void a();

  public abstract void a(int paramInt1, int paramInt2);

  public abstract float getMaxTextAreaLeft();

  public abstract void a(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

  public abstract float a(int paramInt);

  public abstract long getUnitValue();

  public abstract void setShowLeftValue(boolean paramBoolean);
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.chart.a
 * JD-Core Version:    0.6.2
 */