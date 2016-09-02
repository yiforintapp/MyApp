package com.zlf.appmaster.chartview.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import com.zlf.appmaster.chartview.adapter.DimensionAdapter;
import com.zlf.appmaster.utils.DipPixelUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class r extends View
{
  private Context b;
  private float c;
  private ArrayList<Float> d;
  private ArrayList<Long> e;
  private float f;
  private float g;
  private float h;
  private float i;
  private float j;
  private float k;
  private float l;
  private Paint m;
  private Paint n;
  private Paint o;
  private Paint p;
  private Paint q;
  private float r;
  private int s;
  public float topMargin;
  public float leftMargin;
  public float bottomMargin;
  public float rightMargin;
  private boolean t = false;
  private boolean u = false;
  private int v = 0;
  private int w = 0;
  private int x = 6;
  private int y = 0;
  private int z = this.w + this.x + this.y;
  private float A;
  private float B;
  private boolean C = true;
  private boolean D = true;
  boolean a = true;
  private boolean E = false;
  private int F = 0;
  public static final PathEffect DEFAULT_DASH_EFFECT = new DashPathEffect(new float[] { 5.0F, 5.0F, 5.0F, 5.0F }, 1.0F);

  public r(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    a(paramContext);
  }

  public r(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    a(paramContext);
  }

  public r(Context paramContext)
  {
    super(paramContext);
    a(paramContext);
  }

  public void setPicMargins(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.topMargin = paramInt2;
    this.leftMargin = paramInt1;
    this.bottomMargin = paramInt4;
    this.rightMargin = paramInt3;
  }

  private void a(Context paramContext)
  {
    this.b = paramContext;
    this.m = new Paint();
    this.m.setStyle(Style.STROKE);
    this.m.setColor(-2114304);
    this.m.setStrokeWidth(1.0F);
    this.m.setAntiAlias(false);
    this.m.setPathEffect(DEFAULT_DASH_EFFECT);
    this.n = new Paint();
    this.n.setColor(-7829368);
    int i1 = DipPixelUtil.sp2px(paramContext, 12.0F);
    this.n.setTextSize(i1);
    this.n.setAntiAlias(true);
    this.o = new Paint();
    this.o.setStyle(Style.STROKE);
    this.o.setColor(-10000537);
    this.o.setStrokeWidth(1.0F);
    this.o.setAntiAlias(false);
    this.o.setPathEffect(DEFAULT_DASH_EFFECT);
    this.c = (DipPixelUtil.sp2px(paramContext, 12.0F) + 2);
    this.p = new Paint();
    this.p.setStyle(Style.FILL);
    this.p.setColor(-3289651);
    this.q = new Paint();
    this.q.setStyle(Style.FILL);
    this.q.setColor(-592138);
    FontMetricsInt localFontMetricsInt = this.n.getFontMetricsInt();
    this.A = (0 - (localFontMetricsInt.bottom + localFontMetricsInt.top));
    this.B = this.A;
  }

  public void addTime(long paramLong)
  {
    this.e.add(Long.valueOf(paramLong));
  }

  public void addDashLine(float paramFloat)
  {
    if (this.C)
      this.d.add(Float.valueOf(paramFloat));
  }

  public void setIsShowDivider(boolean paramBoolean)
  {
    this.D = paramBoolean;
  }

  public void setIsShowDashLine(boolean paramBoolean)
  {
    this.C = paramBoolean;
  }

  public void setIsShowMaxMin(boolean paramBoolean)
  {
    this.a = paramBoolean;
  }

  public void setIsShowBottomTime(boolean paramBoolean)
  {
    this.E = paramBoolean;
    if (this.E)
    {
      FontMetricsInt localFontMetricsInt = this.n.getFontMetricsInt();
      this.v = (0 - (localFontMetricsInt.bottom + localFontMetricsInt.top) + DipPixelUtil.dip2px(this.b, 5.0F));
    }
    else
    {
      this.v = 0;
    }
  }

  public void setMinMode(int paramInt)
  {
    this.F = paramInt;
  }

  public void setIsZeroMid(boolean paramBoolean)
  {
    this.t = paramBoolean;
  }

  public void initData(float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, int paramInt)
  {
    this.f = paramFloat3;
    this.g = paramFloat4;
    this.h = paramFloat2;
    this.i = paramFloat5;
    this.d = new ArrayList();
    this.e = new ArrayList();
    if (this.i > this.f)
    {
      addDashLine(this.i);
      addDashLine(this.f);
      addDashLine(this.h);
      addDashLine(this.g);
      this.f = this.i;
      if (this.t)
        this.f += this.i - this.g;
    }
    else if (this.i < this.g)
    {
      addDashLine(this.f);
      addDashLine(this.h);
      addDashLine(this.g);
      addDashLine(this.i);
      this.g = this.i;
      if (this.t)
        this.g -= this.f - this.i;
    }
    else
    {
      addDashLine(this.f);
      if (this.i > this.h)
      {
        addDashLine(this.i);
        addDashLine(this.h);
      }
      else
      {
        addDashLine(this.h);
        addDashLine(this.i);
      }
      addDashLine(this.g);
      if (this.t)
        if (this.f - this.i > this.i - this.g)
          this.g = (this.i - (this.f - this.i));
        else
          this.f = (this.i + (this.i - this.g));
    }
    this.s = paramInt;
    this.j = this.f;
    this.k = this.g;
    this.l = 0.0F;
  }

  public float getPicWidth()
  {
    return getWidth() - this.leftMargin - this.rightMargin;
  }

  public void setLayoutScale(int paramInt1, int paramInt2, int paramInt3)
  {
    this.w = paramInt1;
    this.x = paramInt2;
    this.y = paramInt3;
    this.z = (this.w + this.x + this.y);
  }

  private float getPerValueHeight()
  {
    if (this.l == 0.0F)
    {
      float f1 = getHeight() * this.x / this.z - (this.A + this.B) - this.v;
      this.l = (f1 / (this.f - this.g));
      this.r = (getHeight() * this.w / this.z);
    }
    return this.l;
  }

  public float getValueYSize(float paramFloat)
  {
    return (this.f - paramFloat) * getPerValueHeight() + this.r + this.A;
  }

  private void a(Canvas paramCanvas, float paramFloat, int paramInt)
  {
    a(paramCanvas, paramFloat, paramInt, true);
  }

  private void a(Canvas paramCanvas, float paramFloat, int paramInt, boolean paramBoolean)
  {
    float f1 = getValueYSize(paramFloat);
    if (paramBoolean)
    {
      float f2 = getPicWidth();
      Path localPath = new Path();
      localPath.moveTo(this.leftMargin, f1);
      localPath.lineTo(this.leftMargin + f2, f1);
      if (paramFloat == this.i)
        paramCanvas.drawPath(localPath, this.o);
      else
        paramCanvas.drawPath(localPath, this.m);
    }
    if (paramInt == 0)
      return;
    FontMetricsInt localFontMetricsInt = this.n.getFontMetricsInt();
    if (paramInt == 3)
    {
      f1 -= (localFontMetricsInt.top + localFontMetricsInt.bottom) / 2;
    }
    else
    {
      if (paramInt == 1)
        f1 -= localFontMetricsInt.bottom;
      else
        f1 -= localFontMetricsInt.top;
      if (f1 + localFontMetricsInt.top < 0.0F)
        f1 = 0 - localFontMetricsInt.top;
    }
    int i1 = DipPixelUtil.dip2px(this.b, 5.0F);
    String str1 = String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(paramFloat) });
    if (this.u)
    {
      float f3 = this.n.measureText(str1);
      i1 = (int)(this.leftMargin - f3 - i1);
      if (i1 < 0)
        i1 = 0;
      float f4 = (paramFloat - this.i) / this.i * 100.0F;
      String str2 = String.format("%.2f", new Object[] { Float.valueOf(f4) }) + "%";
      float f5 = getWidth() - this.rightMargin + DipPixelUtil.dip2px(this.b, 5.0F);
      paramCanvas.drawText(str2, f5, f1, this.n);
    }
    paramCanvas.drawText(str1, i1, f1, this.n);
  }

  private void a(Canvas paramCanvas, long paramLong)
  {
    Time localTime = new Time();
    localTime.set(paramLong);
    if ((localTime.hour >= 15) || (localTime.hour < 9) || ((localTime.hour == 9) && (localTime.minute < 30)))
      return;
    int i1;
    float f1;
    if ((localTime.hour > 13) || ((localTime.hour == 13) && (localTime.minute > 30)))
    {
      i1 = (localTime.hour - 13) * 60 + localTime.minute + 120;
      f1 = i1 / 240.0F;
    }
    else if ((localTime.hour < 11) || ((localTime.hour == 11) && (localTime.minute < 30)))
    {
      i1 = (localTime.hour - 9) * 60 + localTime.minute - 30;
      f1 = i1 / 240.0F;
    }
    else
    {
      f1 = 0.5F;
    }
    SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    String str = localSimpleDateFormat.format(new Date(paramLong));
    float f2 = this.leftMargin + getPicWidth() * f1 - this.n.measureText(str) / 2.0F;
    if (f2 < this.leftMargin)
      f2 = this.leftMargin;
    else if (f2 > this.leftMargin + getPicWidth() - this.n.measureText(str))
      f2 = this.leftMargin + getPicWidth() - this.n.measureText(str);
    FontMetricsInt localFontMetricsInt = this.n.getFontMetricsInt();
    float f3 = getHeight() - localFontMetricsInt.bottom;
    paramCanvas.drawText(str, f2, f3, this.n);
  }

  public void drawDashLine(Canvas paramCanvas)
  {
    if (this.d.size() == 0)
      return;
    ArrayList localArrayList = new ArrayList();
    for (int i1 = 0; i1 < this.d.size(); i1++)
      localArrayList.add(Float.valueOf(getValueYSize(((Float)this.d.get(i1)).floatValue())));
    if (this.s == 3)
      a(paramCanvas, ((Float)this.d.get(0)).floatValue(), 3, false);
    else
      a(paramCanvas, ((Float)this.d.get(0)).floatValue(), 3);
    if (this.s == 4)
      a(paramCanvas, ((Float)this.d.get(3)).floatValue(), 3, false);
    else
      a(paramCanvas, ((Float)this.d.get(3)).floatValue(), 3);
    FontMetricsInt localFontMetricsInt = this.n.getFontMetricsInt();
    float f1 = 0 - (localFontMetricsInt.top + localFontMetricsInt.bottom);
    if (((Float)localArrayList.get(1)).floatValue() - ((Float)localArrayList.get(0)).floatValue() < f1)
    {
      a(paramCanvas, ((Float)this.d.get(1)).floatValue(), 0);
      if ((((Float)localArrayList.get(2)).floatValue() - ((Float)localArrayList.get(0)).floatValue() > f1) && (((Float)localArrayList.get(3)).floatValue() - ((Float)localArrayList.get(2)).floatValue() > f1))
        a(paramCanvas, ((Float)this.d.get(2)).floatValue(), 3);
      else
        a(paramCanvas, ((Float)this.d.get(2)).floatValue(), 0);
    }
    else
    {
      a(paramCanvas, ((Float)this.d.get(1)).floatValue(), 3);
      if ((((Float)localArrayList.get(2)).floatValue() - ((Float)localArrayList.get(1)).floatValue() > f1) && (((Float)localArrayList.get(3)).floatValue() - ((Float)localArrayList.get(2)).floatValue() > f1))
        a(paramCanvas, ((Float)this.d.get(2)).floatValue(), 3);
      else
        a(paramCanvas, ((Float)this.d.get(2)).floatValue(), 0);
    }
  }

  public void drawBottomTime(Canvas paramCanvas)
  {
    if (!this.E)
      return;
    FontMetricsInt localFontMetricsInt = this.n.getFontMetricsInt();
    for (int i1 = 0; i1 < this.e.size(); i1++)
      a(paramCanvas, ((Long)this.e.get(i1)).longValue());
    if (this.E)
    {
      String str = /*"11:30|13:00"*/"4:30";
      float f1 = this.leftMargin + getPicWidth() * 0.5F - this.n.measureText(str) / 2.0F;
      float f2 = getHeight() - localFontMetricsInt.bottom;
      paramCanvas.drawText(str, f1, f2, this.n);
      str = /*"9:30"*/"6:00";
      f1 = this.leftMargin;
      paramCanvas.drawText(str, f1, f2, this.n);
      str = /*"15:00"*/"";
      f1 = this.leftMargin + getPicWidth() - this.n.measureText(str);
      paramCanvas.drawText(str, f1, f2, this.n);
    }
  }

  public void drawCornerValue(Canvas paramCanvas)
  {
    FontMetricsInt localFontMetricsInt = this.n.getFontMetricsInt();
    float f2 = getValueYSize(this.j);
    float f3 = f2 - (localFontMetricsInt.bottom + localFontMetricsInt.top) / 2;
    String str = String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(this.j) });
    paramCanvas.drawText(str, 0.0F, f3, this.n);
    float f1 = 0.0F + this.n.measureText(str);
    float f4 = (this.j - this.i) * 100.0F / this.i;
    str = String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(f4) }) + "%";
    float f5 = getWidth() - this.n.measureText(str);
    paramCanvas.drawText(str, f5, f3, this.n);
    paramCanvas.drawLine(f1 + 10.0F, f2, f5 - 10.0F, f2, this.p);
    str = String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(this.k) });
    f2 = getValueYSize(this.k);
    f3 = f2 - (localFontMetricsInt.bottom + localFontMetricsInt.top) / 2;
    paramCanvas.drawText(str, 0.0F, f3, this.n);
    f1 = 0.0F + this.n.measureText(str);
    f4 = (this.i - this.k) * 100.0F / this.i;
    str = "-" + String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(f4) }) + "%";
    f5 = getWidth() - this.n.measureText(str);
    paramCanvas.drawText(str, f5, f3, this.n);
    paramCanvas.drawLine(f1 + 10.0F, f2, f5 - 10.0F, f2, this.p);
  }

  public int getLineHeight()
  {
    return getHeight() * (this.w + this.x) / this.z - this.v;
  }

  protected void onDraw(Canvas paramCanvas)
  {
    paramCanvas.drawRect(this.leftMargin, this.topMargin, getWidth() - this.rightMargin, getLineHeight(), this.q);
    if (this.D)
    {
      for (int i1 = 0; i1 < 4; i1++)
      {
        int i2 = (i1 + 1) * getWidth() / 4;
        if (this.F == 2)
          paramCanvas.drawLine(i2, 0.0F, i2, getHeight() - this.v, this.p);
        else
          paramCanvas.drawLine(i2, 0.0F, i2, getHeight(), this.p);
      }
      String str = "0%";
      FontMetricsInt localFontMetricsInt = this.n.getFontMetricsInt();
      float f1 = this.n.measureText(str);
      float f2 = getWidth() - f1;
      float f3 = getHeight() / 2 - (localFontMetricsInt.bottom + localFontMetricsInt.top) / 2;
      paramCanvas.drawText(str, f2, f3, this.n);
      paramCanvas.drawLine(0.0F, getHeight() / 2, f2 - 2.0F, getHeight() / 2, this.p);
    }
  }

  public void setIsShowOutSide(boolean paramBoolean)
  {
    this.u = paramBoolean;
  }

  public float getMaxRightTextArea()
  {
    float f1 = 0.0F;
    Iterator localIterator = this.d.iterator();
    while (localIterator.hasNext())
    {
      float f2 = ((Float)localIterator.next()).floatValue();
      String str = String.format("%.2f", new Object[] { Float.valueOf(f2) }) + "%";
      float f3 = this.n.measureText(str);
      if (f1 < f3)
        f1 = f3;
    }
    return f1 + DipPixelUtil.dip2px(this.b, 15.0F);
  }

  public float getMaxLeftTextArea()
  {
    float f1 = 0.0F;
    Iterator localIterator = this.d.iterator();
    while (localIterator.hasNext())
    {
      float f2 = ((Float)localIterator.next()).floatValue();
      String str = String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(f2) });
      float f3 = this.n.measureText(str);
      if (f1 < f3)
        f1 = f3;
    }
    return f1 + DipPixelUtil.dip2px(this.b, 15.0F);
  }

  public void setMaxLeftTextArea(float paramFloat)
  {
    this.leftMargin = paramFloat;
  }

  public void setMaxRightTextArea(float paramFloat)
  {
    this.rightMargin = paramFloat;
  }
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.chart.r
 * JD-Core Version:    0.6.2
 */