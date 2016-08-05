package com.zlf.appmaster.chartview.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.StockKDJ;
import com.zlf.appmaster.utils.DipPixelUtil;

import java.util.Iterator;
import java.util.List;

public class KDJChart extends com.zlf.appmaster.chartview.chart.a
{
  private static final String a = KDJChart.class.getSimpleName();
  private List<StockKDJ> b;
  private List<StockKDJ> c;
  private float d;
  private float e;
  private Paint f;
  private Paint g;
  private Paint h;
  private int i;
  private int j;
  private int k;
  private int l;
  private int m;
  private int n;
  private int o;
  private int p;
  private Context q;
  private float r;
  private boolean s = true;
  private Paint t;
  private Paint u;
  private Paint v;
  private String w;
  private String x;
  private float y;
  private float z;

  public KDJChart(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    a(paramContext);
  }

  public void setSourceData(Object paramObject)
  {
    this.b = StockKDJ.getDataByStockKLine((List)paramObject);
    a();
  }

  public void a()
  {
    if (this.b.isEmpty())
      return;
    int i1 = this.b.size() - this.l;
    if (i1 > this.k)
      i1 = this.k;
    this.c = this.b.subList(this.l, this.l + i1);
    this.d = 1.4E-45F;
    this.e = 3.4028235E+38F;
    Iterator localIterator = this.c.iterator();
    while (localIterator.hasNext())
    {
      StockKDJ localStockKDJ = (StockKDJ)localIterator.next();
      float f1 = localStockKDJ.getJ();
      if (f1 > this.d)
        this.d = f1;
      if (f1 < this.e)
        this.e = f1;
    }
    this.w = String.format("%.3f", new Object[] { Float.valueOf(this.d) });
    this.x = String.format("%.3f", new Object[] { Float.valueOf(this.e) });
    this.y = (this.g.measureText(this.w) + DipPixelUtil.dip2px(this.q, 5.0F));
    this.z = (this.g.measureText(this.x) + DipPixelUtil.dip2px(this.q, 5.0F));
    this.s = true;
    invalidate();
  }

  public void a(int paramInt1, int paramInt2)
  {
    this.k = paramInt2;
    this.l = paramInt1;
  }

  public float getMaxTextAreaLeft()
  {
    return 0.0F;
  }

  public KDJChart(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    a(paramContext);
  }

  public KDJChart(Context paramContext)
  {
    super(paramContext);
    a(paramContext);
    b();
  }

  private void a(Context paramContext)
  {
    this.q = paramContext;
    this.f = new Paint();
    this.f.setAntiAlias(true);
    this.f.setStyle(Style.FILL);
    this.i = paramContext.getResources().getColor(R.color.stock_rise);
    this.j = paramContext.getResources().getColor(R.color.stock_slumped);
    this.g = new Paint();
    this.g.setAntiAlias(true);
    this.g.setColor(-7829368);
    this.g.setTextSize(DipPixelUtil.sp2px(paramContext, 12.0F));
    this.h = new Paint();
    this.h.setStyle(Style.FILL);
    this.h.setColor(-592138);
    this.t = new Paint();
    this.t.setColor(-11941946);
    this.t.setAntiAlias(true);
    this.t.setStyle(Style.FILL);
    this.t.setStrokeWidth(DipPixelUtil.dip2px(paramContext, 1.0F));
    this.u = new Paint();
    this.u.setColor(-1650592);
    this.u.setAntiAlias(true);
    this.u.setStyle(Style.FILL);
    this.u.setStrokeWidth(DipPixelUtil.dip2px(paramContext, 1.0F));
    this.v = new Paint();
    this.v.setColor(-10062135);
    this.v.setAntiAlias(true);
    this.v.setStyle(Style.FILL);
    this.v.setStrokeWidth(DipPixelUtil.dip2px(paramContext, 1.0F));
  }

  protected void onDraw(Canvas paramCanvas)
  {
    paramCanvas.drawRect(this.m, this.o, getWidth() - this.n, getHeight() - this.p, this.h);
    float f1 = this.m;
    float f2 = (getWidth() - f1 - this.n) / this.k;
    float f3 = f2 - 1.0F;
    if (f3 < 1.0F)
      f3 = 1.0F;
    if (null != this.c)
    {
      FontMetricsInt localFontMetricsInt = this.g.getFontMetricsInt();
      float f4 = f1 - this.y;
      float f5 = f1 - this.z;
      paramCanvas.drawText(this.w, f4, this.o - localFontMetricsInt.top, this.g);
      paramCanvas.drawText(this.x, f5, getHeight() - this.p - localFontMetricsInt.bottom, this.g);
      float f6 = f1 + f3 / 2.0F;
      float f7 = f6 + f2;
      float f8 = a(((StockKDJ)this.c.get(0)).getK());
      float f9 = 0.0F;
      float f10 = a(((StockKDJ)this.c.get(0)).getD());
      float f11 = 0.0F;
      float f12 = a(((StockKDJ)this.c.get(0)).getJ());
      float f13 = 0.0F;
      for (int i1 = 1; i1 < this.c.size(); i1++)
      {
        f9 = a(((StockKDJ)this.c.get(i1)).getK());
        paramCanvas.drawLine(f6, f8, f7, f9, this.t);
        f11 = a(((StockKDJ)this.c.get(i1)).getD());
        paramCanvas.drawLine(f6, f10, f7, f11, this.u);
        f13 = a(((StockKDJ)this.c.get(i1)).getJ());
        paramCanvas.drawLine(f6, f12, f7, f13, this.v);
        f8 = f9;
        f10 = f11;
        f12 = f13;
        f6 = f7;
        f7 += f2;
      }
    }
  }

  public void a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.m = paramInt1;
    this.n = paramInt2;
    this.p = paramInt4;
    this.o = paramInt3;
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

  public void b()
  {
    this.r = 0.0F;
  }

  public float a(float paramFloat)
  {
    return (this.d - paramFloat) * getPerValueHeight();
  }

  private float getPerValueHeight()
  {
    if (this.s)
    {
      this.r = ((getHeight() - this.o - this.p) / (this.d - this.e));
      this.s = false;
    }
    return this.r;
  }
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.chart.KDJChart
 * JD-Core Version:    0.6.2
 */