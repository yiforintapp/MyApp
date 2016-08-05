package com.zlf.appmaster.chartview.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.StockVolume;
import com.zlf.appmaster.utils.DipPixelUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class VolumeChart extends com.zlf.appmaster.chartview.chart.a
{
  private ArrayList<StockVolume> a;
  private List<? extends StockVolume> b;
  private float c;
  private long d;
  private Paint e;
  private Paint f;
  private Paint g;
  private int h;
  private int i;
  private int j;
  private int k;
  private int l;
  private int m;
  private int n;
  private int o;
  private boolean p;
  private Context q;
  private String r;
  private String s;
  private float t;
  private float u;

  public VolumeChart(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    a(paramContext);
  }

  public VolumeChart(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    a(paramContext);
  }

  public VolumeChart(Context paramContext)
  {
    super(paramContext);
    a(paramContext);
  }

  private void a(Context paramContext)
  {
    this.q = paramContext;
    this.e = new Paint();
    this.e.setAntiAlias(true);
    this.e.setStyle(Style.FILL);
    this.h = paramContext.getResources().getColor(R.color.stock_rise);
    this.i = paramContext.getResources().getColor(R.color.stock_slumped);
    this.f = new Paint();
    this.f.setAntiAlias(true);
    this.f.setColor(-7829368);
    this.f.setTextSize(DipPixelUtil.sp2px(paramContext, 12.0F));
    this.g = new Paint();
    this.g.setStyle(Style.FILL);
    this.g.setColor(-592138);
    this.a = new ArrayList();
  }

  public void setShowLeftValue(boolean paramBoolean)
  {
    this.p = paramBoolean;
  }

  public void a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.l = paramInt1;
    this.m = paramInt2;
    this.o = paramInt4;
    this.n = paramInt3;
  }

  public float getMaxTextAreaLeft()
  {
    if (this.t > this.u)
      return this.t;
    return this.u;
  }

  public void a(int paramInt1, int paramInt2)
  {
    this.j = paramInt2;
    this.k = paramInt1;
  }

  public void setSourceData(Object paramObject)
  {
    ArrayList localArrayList = (ArrayList)paramObject;
    this.a.clear();
    try
    {
      Iterator localIterator = localArrayList.iterator();
      while (localIterator.hasNext())
      {
        StockVolume localStockVolume = (StockVolume)localIterator.next();
        this.a.add(localStockVolume.clone());
      }
    }
    catch (Exception localCloneNotSupportedException)
    {
      localCloneNotSupportedException.printStackTrace();
    }
    a();
  }

  public void a()
  {
    if (this.a.isEmpty())
      return;
    int i1 = this.a.size() - this.k;
    if (i1 > this.j)
      i1 = this.j;
    this.b = this.a.subList(this.k, this.k + i1);
    this.c = ((float)((StockVolume)this.b.get(0)).getTradeCount());
    for (int i2 = 1; (i2 < this.j) && (i2 <= this.b.size() - 1); i2++)
      if ((float)((StockVolume)this.b.get(i2)).getTradeCount() > this.c)
        this.c = ((float)((StockVolume)this.b.get(i2)).getTradeCount());
    if (this.c > 1.0E+010F)
    {
      this.d = 10000000000L;
      this.r = String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(this.c / (float)this.d) });
      this.s = "亿手";
    }
    else if (this.c > 1000000.0F)
    {
      this.d = 1000000L;
      this.r = String.format(Locale.getDefault(), "%.1f", new Object[] { Float.valueOf(this.c / (float)this.d) });
      this.s = "万手";
    }
    else
    {
      this.d = 100L;
      this.r = String.format(Locale.getDefault(), "%.0f", new Object[] { Float.valueOf(this.c / (float)this.d) });
      this.s = "手";
    }
    if (this.p)
    {
      this.t = (this.f.measureText(this.r) + DipPixelUtil.dip2px(this.q, 5.0F));
      this.u = (this.f.measureText(this.s) + DipPixelUtil.dip2px(this.q, 5.0F));
    }
    else
    {
      this.t = 0.0F;
      this.u = 0.0F;
    }
    invalidate();
  }

  public float a(int paramInt)
  {
    float f1 = (float)((StockVolume)this.b.get(paramInt)).getTradeCount();
    return a(f1);
  }

  private float a(float paramFloat)
  {
    float f1 = paramFloat * (getHeight() - this.n - this.o) / this.c;
    float f2 = getHeight() - this.o - f1;
    return f2;
  }

  private void a(Canvas paramCanvas, float paramFloat1, float paramFloat2, boolean paramBoolean, float paramFloat3)
  {
    if (paramBoolean)
      this.e.setColor(this.h);
    else
      this.e.setColor(this.i);
    float f1 = a(paramFloat2);
    paramCanvas.drawRect(paramFloat1, f1, paramFloat1 + paramFloat3, getHeight() - this.o, this.e);
  }

  protected void onDraw(Canvas paramCanvas)
  {
    if ((this.b == null) || (this.b.size() == 0))
      return;
    paramCanvas.drawRect(this.l, this.n, getWidth() - this.m, getHeight() - this.o, this.g);
    float f1 = this.l;
    float f2 = (getWidth() - f1 - this.m) / this.j;
    float f3 = f2 - 1.0F;
    if (f3 < 1.0F)
      f3 = 1.0F;
    if (this.p)
    {
      FontMetricsInt localFontMetricsInt = this.f.getFontMetricsInt();
      float f4 = f1 - this.t;
      float f5 = f1 - this.u;
      paramCanvas.drawText(this.r, f4, this.n - localFontMetricsInt.top, this.f);
      paramCanvas.drawText(this.s, f5, getHeight() - this.o - localFontMetricsInt.bottom, this.f);
    }
    for (int i1 = 0; i1 < this.b.size(); i1++)
    {
      a(paramCanvas, f1, (float)((StockVolume)this.b.get(i1)).getTradeCount(), ((StockVolume)this.b.get(i1)).isUp(), f3);
      f1 += f2;
    }
  }

  public long getUnitValue()
  {
    return this.d;
  }
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.chart.VolumeChart
 * JD-Core Version:    0.6.2
 */