package com.zlf.appmaster.chartview.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.StockMACD;
import com.zlf.appmaster.utils.DipPixelUtil;

import java.util.Iterator;
import java.util.List;

public class MACDChart extends com.zlf.appmaster.chartview.chart.a
{
  private static final String b = MACDChart.class.getSimpleName();
  private List<StockMACD> c;
  private List<StockMACD> d;
  private float e;
  private float f;
  private Paint g;
  private Paint h;
  private Paint i;
  private int j;
  private int k;
  private int l;
  private int m;
  private int n;
  private int o;
  private int p;
  private int q;
  private Context r;
  private Paint s;
  private Paint t;
  private Paint u;
  private Paint v;
  private String w;
  private String x;
  private float y;
  private float z;
  private float A = 0.0F;
  private boolean B = true;
  float a = 5.0F;

  public MACDChart(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    a(paramContext);
  }

  public void setSourceData(Object paramObject)
  {
    this.c = StockMACD.getDataByStockKLine((List)paramObject);
    a();
  }

  public void a()
  {
    int i1 = this.c.size() - this.m;
    if (i1 > this.l)
      i1 = this.l;
    this.d = this.c.subList(this.m, this.m + i1);
    this.e = 1.4E-45F;
    this.f = 3.4028235E+38F;
    Iterator localIterator = this.d.iterator();
    while (localIterator.hasNext())
    {
      StockMACD localStockMACD = (StockMACD)localIterator.next();
      float f3 = localStockMACD.getDIFF();
      if (f3 > this.e)
        this.e = f3;
      if (f3 < this.f)
        this.f = f3;
    }
    float f1 = Math.abs(this.e);
    float f2 = Math.abs(this.f);
    if (f1 > f2)
      this.f = (-f1);
    else
      this.e = f2;
    this.w = String.format("%.3f", new Object[] { Float.valueOf(this.e) });
    this.x = String.format("%.3f", new Object[] { Float.valueOf(this.f) });
    this.y = (this.h.measureText(this.w) + DipPixelUtil.dip2px(this.r, 5.0F));
    this.z = (this.h.measureText(this.x) + DipPixelUtil.dip2px(this.r, 5.0F));
    this.B = true;
    invalidate();
  }

  public void a(int paramInt1, int paramInt2)
  {
    this.l = paramInt2;
    this.m = paramInt1;
  }

  public float getMaxTextAreaLeft()
  {
    if (this.y > this.z)
      return this.y;
    return this.z;
  }

  public MACDChart(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    a(paramContext);
  }

  public MACDChart(Context paramContext)
  {
    super(paramContext);
    a(paramContext);
  }

  private void a(Context paramContext)
  {
    this.r = paramContext;
    this.g = new Paint();
    this.g.setAntiAlias(true);
    this.g.setStyle(Style.FILL);
    this.j = paramContext.getResources().getColor(R.color.stock_rise);
    this.k = paramContext.getResources().getColor(R.color.stock_slumped);
    this.h = new Paint();
    this.h.setAntiAlias(true);
    this.h.setColor(-7829368);
    this.h.setTextSize(DipPixelUtil.sp2px(paramContext, 12.0F));
    this.i = new Paint();
    this.i.setStyle(Style.FILL);
    this.i.setColor(-592138);
    this.s = new Paint();
    this.s.setColor(-11941946);
    this.s.setAntiAlias(true);
    this.s.setStyle(Style.FILL);
    this.s.setStrokeWidth(DipPixelUtil.dip2px(paramContext, 1.0F));
    this.t = new Paint();
    this.t.setColor(-1650592);
    this.t.setAntiAlias(true);
    this.t.setStyle(Style.FILL);
    this.t.setStrokeWidth(DipPixelUtil.dip2px(paramContext, 1.0F));
    this.u = new Paint();
    this.u.setStyle(Style.FILL);
    this.u.setColor(getResources().getColor(R.color.stock_rise));
    this.v = new Paint();
    this.v.setStyle(Style.FILL);
    this.v.setColor(getResources().getColor(R.color.stock_slumped));
  }

  protected void onDraw(Canvas paramCanvas)
  {
    paramCanvas.drawRect(this.n, this.p, getWidth() - this.o, getHeight() - this.q, this.i);
    float f1 = this.n;
    float f2 = (getWidth() - f1 - this.o) / this.l;
    float f3 = f2 - 1.0F;
    if (f3 < 1.0F)
      f3 = 1.0F;
    if (null != this.d)
    {
      FontMetricsInt localFontMetricsInt = this.h.getFontMetricsInt();
      float f4 = f1 - this.y;
      float f5 = f1 - this.z;
      paramCanvas.drawText(this.w, f4, this.p - localFontMetricsInt.top, this.h);
      paramCanvas.drawText(this.x, f5, getHeight() - this.q - localFontMetricsInt.bottom, this.h);
      float f6 = f1;
      float f7 = a(((StockMACD)this.d.get(0)).getDIFF());
      float f8 = 0.0F;
      float f9 = a(((StockMACD)this.d.get(0)).getDEA());
      float f10 = 0.0F;
      float f11 = a(0.0F);
      int i1 = this.d.size();
      for (int i2 = 0; i2 < i1; i2++)
      {
        float f13 = ((StockMACD)this.d.get(i2)).getMACD();
        if (f13 > 0.0F)
          paramCanvas.drawRect(f6, a(f13), f6 + f3, f11, this.u);
        else
          paramCanvas.drawRect(f6, f11, f6 + f3, a(f13), this.v);
        f6 += f2;
      }
      f6 = f1 + f3 / 2.0F;
      float f12 = f6 + f2;
      for (int i3 = 0; i3 < i1 - 1; i3++)
      {
        StockMACD localStockMACD = (StockMACD)this.d.get(i3 + 1);
        f8 = a(localStockMACD.getDIFF());
        paramCanvas.drawLine(f6, f7, f12, f8, this.s);
        f10 = a(localStockMACD.getDEA());
        paramCanvas.drawLine(f6, f9, f12, f10, this.t);
        f7 = f8;
        f9 = f10;
        f6 = f12;
        f12 += f2;
      }
    }
  }

  public void a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.n = paramInt1;
    this.o = paramInt2;
    this.q = paramInt4;
    this.p = paramInt3;
  }

  public float a(int paramInt)
  {
    return 0.0F;
  }

  public long getUnitValue()
  {
    return 0L;
  }

  public void setShowLeftValue(boolean paramBoolean)
  {
  }

  public float a(float paramFloat)
  {
    return (this.e - paramFloat) * getPerValueHeight();
  }

  private float getPerValueHeight()
  {
    if (this.B)
    {
      this.A = ((getHeight() - this.p - this.q) / (this.e - this.f));
      this.B = false;
    }
    return this.A;
  }

  public float getStickWidth()
  {
    return getWidth() / this.l;
  }
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.chart.MACDChart
 * JD-Core Version:    0.6.2
 */