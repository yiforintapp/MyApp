package com.zlf.appmaster.chartview.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

import com.zlf.appmaster.utils.DipPixelUtil;
import com.zlf.appmaster.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class b extends View
{
  int a;
  int b;
  int c;
  int d;
  float e;
  float f;
  int g;
  int h;
  private ArrayList<Float> l;
  private ArrayList<com.zlf.appmaster.chartview.chart.c> m;
  private Paint n;
  private Paint o;
  private Paint p;
  private int q;
  private float r;
  private float s;
  private float t;
  private Context u;
  boolean i = true;
  int j = 0;
  float k;

  public b(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    a(paramContext);
  }

  public b(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    a(paramContext);
  }

  public b(Context paramContext)
  {
    super(paramContext);
    a(paramContext);
  }

  private void a(Context paramContext)
  {
    this.u = paramContext;
    this.l = new ArrayList();
    this.m = new ArrayList();
    this.n = new Paint();
    this.n.setAntiAlias(true);
    this.n.setColor(-7829368);
    this.n.setTextSize(DipPixelUtil.sp2px(paramContext, 12.0F));
    this.q = DipPixelUtil.dip2px(paramContext, 20.0F);
    this.d = this.q;
    this.o = new Paint();
    this.o.setStyle(Style.FILL);
    this.o.setColor(-3223858);
    this.p = new Paint();
    this.p.setStyle(Style.FILL);
    this.p.setColor(-592138);
    this.k = 1.0F;
    this.s = (this.r = DipPixelUtil.dip2px(this.u, 18.0F));
  }

  public void a()
  {
    this.t = 0.0F;
  }

  private float getPerValueHeight()
  {
    if (this.t == 0.0F)
      this.t = ((getHeight() - this.d - this.c - this.r - this.s) / (this.f - this.e));
    return this.t;
  }

  public float b(float paramFloat)
  {
    return (this.f - paramFloat) * getPerValueHeight() + this.c + this.r;
  }

  public float c(int paramInt)
  {
    return paramInt * getStickWidth() + this.a + (getStickWidth() - this.k) / 2.0F;
  }

  public float getStickWidth()
  {
    return (getWidth() - this.a - this.b) / this.g;
  }

  public void b()
  {
    this.l.clear();
  }

  public void c(float paramFloat)
  {
    this.l.add(Float.valueOf(paramFloat));
    Collections.sort(this.l);
    int i1 = (int)this.n.measureText(String.format("%.2f", new Object[] { Float.valueOf(paramFloat) }));
    if (this.j == 1)
      i1 += DipPixelUtil.dip2px(this.u, 30.0F);
    else
      i1 += DipPixelUtil.dip2px(this.u, 0.0F);
    if (i1 > this.a)
      this.a = i1;
  }

  public void c()
  {
    this.m.clear();
  }

  public void a(int paramInt, long paramLong)
  {
    this.m.add(new c(this, paramInt, paramLong));
  }

  public void setShowLeftValue(boolean paramBoolean)
  {
    this.i = paramBoolean;
  }

  protected void onDraw(Canvas paramCanvas)
  {
    paramCanvas.drawRect(this.a, this.c, getWidth() - this.b, getHeight() - this.d, this.p);
    super.onDraw(paramCanvas);
    FontMetricsInt localFontMetricsInt = this.n.getFontMetricsInt();
    int i1;
    float f2;
    float f3;
    float f4;
    String str2;
    float f6;
    if (this.i)
      for (i1 = 0; i1 < this.l.size(); i1++)
      {
        f2 = ((Float)this.l.get(i1)).floatValue();
        f3 = b(f2);
        f4 = f3 - (localFontMetricsInt.bottom + localFontMetricsInt.top) / 2;
        str2 = String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(f2) });
        f6 = this.a - this.n.measureText(str2) - DipPixelUtil.dip2px(this.u, 5.0F);
        paramCanvas.drawText(str2, f6, f4, this.n);
        paramCanvas.drawLine(this.a, f3, getWidth() - this.b, f3, this.o);
      }
    else
      for (i1 = 0; i1 < this.l.size(); i1++)
      {
        f2 = ((Float)this.l.get(i1)).floatValue();
        f3 = b(f2);
        f4 = f3 - (localFontMetricsInt.bottom + localFontMetricsInt.top) / 2;
        if ((i1 == 0) || (i1 == this.l.size() - 1) || (this.j == 2))
        {
          str2 = String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(f2) });
          f6 = this.a + DipPixelUtil.dip2px(this.u, 5.0F);
          paramCanvas.drawText(str2, f6, f4, this.n);
          f6 += this.n.measureText(str2) + DipPixelUtil.dip2px(this.u, 5.0F);
          paramCanvas.drawLine(f6, f3, getWidth() - this.b, f3, this.o);
        }
        else
        {
          paramCanvas.drawLine(this.a, f3, getWidth() - this.b, f3, this.o);
        }
      }
    float f1 = 0.0F;
    for (int i2 = 0; i2 < this.m.size(); i2++)
    {
      String str1 = TimeUtil.getYearAndMonth(((c)this.m.get(i2)).b);
      f4 = this.n.measureText(str1);
      float f5 = c(((c)this.m.get(i2)).a);
      f6 = f5 - f4 / 2.0F;
      if (((f1 < 1.0F) && (f6 > this.a)) || ((f6 - f1 > f4 * 3.0F / 2.0F) && (f6 + f4 < getWidth())))
      {
        paramCanvas.drawText(str1, f6, getHeight() - localFontMetricsInt.bottom, this.n);
        paramCanvas.drawLine(f5, this.c, f5, getHeight() - this.d, this.o);
        f1 = f6;
      }
    }
  }
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.chart.b
 * JD-Core Version:    0.6.2
 */