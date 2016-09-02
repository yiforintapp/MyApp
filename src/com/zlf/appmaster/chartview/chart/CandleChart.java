package com.zlf.appmaster.chartview.chart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;

import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.StockKLine;
import com.zlf.appmaster.utils.DipPixelUtil;
import com.zlf.appmaster.utils.QLog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CandleChart extends b
{
  private ArrayList<StockKLine> l;
  private Paint m;
  private Paint n;
  private Paint o;
  private Paint p;
  private Paint q;
  private Paint r;
  private Paint s;
  private int t = 1;
  private Context u;
  private StockKLine v = null;

  public CandleChart(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    a(paramContext);
  }

  public CandleChart(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    a(paramContext);
  }

  public CandleChart(Context paramContext)
  {
    super(paramContext);
    a(paramContext);
  }

  private void a(Context paramContext)
  {
    this.u = paramContext;
    this.m = new Paint();
    this.m.setStyle(Style.FILL);
    this.m.setColor(getResources().getColor(R.color.stock_rise));
    this.n = new Paint();
    this.n.setStyle(Style.FILL);
    this.n.setColor(getResources().getColor(R.color.stock_slumped));
    this.o = this.m;
    this.p = new Paint();
    this.p.setColor(-7638764);
    this.p.setAntiAlias(true);
    this.p.setStyle(Style.FILL);
    this.p.setStrokeWidth(DipPixelUtil.dip2px(paramContext, 1.0F));
    this.q = new Paint();
    this.q.setColor(-11960594);
    this.q.setAntiAlias(true);
    this.q.setStyle(Style.FILL);
    this.q.setStrokeWidth(DipPixelUtil.dip2px(paramContext, 1.0F));
    this.r = new Paint();
    this.r.setColor(-5358121);
    this.r.setAntiAlias(true);
    this.r.setStyle(Style.FILL);
    this.r.setStrokeWidth(DipPixelUtil.dip2px(paramContext, 1.0F));
    this.s = new Paint();
    this.s.setAntiAlias(true);
    this.s.setColor(-7829368);
    this.s.setTextSize(DipPixelUtil.sp2px(paramContext, 12.0F));
  }

  public void setKLineType(int paramInt)
  {
    this.t = paramInt;
  }

  public void a(ArrayList<StockKLine> paramArrayList, int paramInt)
  {
    this.l = paramArrayList;
    this.g = paramInt;
    if (this.l.size() > paramInt)
      this.h = paramInt;
    else
      this.h = this.l.size();
    this.e = ((StockKLine)this.l.get(0)).getLow();
    this.f = ((StockKLine)this.l.get(0)).getHigh();
    Calendar localCalendar = Calendar.getInstance();
    localCalendar.setTime(new Date(((StockKLine)paramArrayList.get(0)).getDataTime()));
    int i = localCalendar.get(1);
    int j = localCalendar.get(2) + i * 12;
    c();
    for (int k = 0; k < this.h; k++)
    {
      StockKLine localStockKLine = (StockKLine)this.l.get(k);
      if (this.e > localStockKLine.getLow())
        this.e = localStockKLine.getLow();
      if ((localStockKLine.getMa5() > 0.0F) && (this.e > localStockKLine.getMa5()))
        this.e = localStockKLine.getMa5();
      if ((localStockKLine.getMa10() > 0.0F) && (this.e > localStockKLine.getMa10()))
        this.e = localStockKLine.getMa10();
      if ((localStockKLine.getMa30() > 0.0F) && (this.e > localStockKLine.getMa30()))
        this.e = localStockKLine.getMa30();
      if (this.f < localStockKLine.getHigh())
        this.f = localStockKLine.getHigh();
      if ((localStockKLine.getMa5() > 0.0F) && (this.f < localStockKLine.getMa5()))
        this.f = localStockKLine.getMa5();
      if ((localStockKLine.getMa10() > 0.0F) && (this.f < localStockKLine.getMa10()))
        this.f = localStockKLine.getMa10();
      if ((localStockKLine.getMa30() > 0.0F) && (this.f < localStockKLine.getMa30()))
        this.f = localStockKLine.getMa30();
      int i1;
      if ((this.t == 1) || (this.t == 2))
      {
        localCalendar.setTime(new Date(((StockKLine)paramArrayList.get(k)).getDataTime()));
        i1 = localCalendar.get(2);
        if (i1 != j)
        {
          a(k, ((StockKLine)this.l.get(k)).getDataTime());
          j = i1;
        }
      }
      else if (this.t == 3)
      {
        localCalendar.setTime(new Date(((StockKLine)paramArrayList.get(k)).getDataTime()));
        i1 = localCalendar.get(1);
        if (i1 != i)
        {
          a(k, ((StockKLine)this.l.get(k)).getDataTime());
          i = i1;
        }
      }
    }
    b();
    c(this.e);
    c(this.f);
    c(this.e + (this.f - this.e) / 3.0F);
    c(this.e + 2.0F * (this.f - this.e) / 3.0F);
    a();
  }

  public void a()
  {
    super.a();
    setCurStockKLineInfo(-1);
  }

  public int a(float paramFloat)
  {
    int i = (int)((paramFloat - this.a) / getStickWidth());
    if (i > this.l.size() - 1)
      i = this.l.size() - 1;
    else if (i < 0)
      i = 0;
    return i;
  }

  public void setCurStockKLineInfo(int paramInt)
  {
    if (paramInt < 0)
      this.v = ((StockKLine)this.l.get(this.l.size() - 1));
    else
      this.v = b(paramInt);
    invalidate();
  }

  private float a(Canvas paramCanvas, float paramFloat1, float paramFloat2, Paint paramPaint, String paramString)
  {
    float f = DipPixelUtil.dip2px(this.u, 3.0F) * 2;
    paramCanvas.drawCircle(paramFloat1, paramFloat2, f / 2.0F, paramPaint);
    String str = String.format(paramString, new Object[0]);
    FontMetricsInt localFontMetricsInt = this.s.getFontMetricsInt();
    paramFloat2 -= (localFontMetricsInt.top + localFontMetricsInt.bottom) / 2;
    paramCanvas.drawText(str, paramFloat1 + f, paramFloat2, this.s);
    f += this.s.measureText(str);
    f += DipPixelUtil.dip2px(this.u, 16.0F);
    return f;
  }

  private void a(Canvas paramCanvas, float paramFloat1, float paramFloat2, StockKLine paramStockKLine)
  {
    if ((paramStockKLine == null) || (this.j != 1))
      return;
    String str;
    if (paramStockKLine.getMa5() > 0.0F)
      str = String.format("MA5:%.2f", new Object[] { Float.valueOf(paramStockKLine.getMa5()) });
    else
      str = "MA5:--";
    float f = a(paramCanvas, paramFloat1, paramFloat2, this.p, str);
    if (paramStockKLine.getMa10() > 0.0F)
      str = String.format("MA10:%.2f", new Object[] { Float.valueOf(paramStockKLine.getMa10()) });
    else
      str = "MA10:--";
    f += a(paramCanvas, paramFloat1 + f, paramFloat2, this.q, str);
    if (paramStockKLine.getMa30() > 0.0F)
      str = String.format("MA30:%.2f", new Object[] { Float.valueOf(paramStockKLine.getMa30()) });
    else
      str = "MA30:--";
    a(paramCanvas, paramFloat1 + f, paramFloat2, this.r, str);
  }

  @SuppressLint({"DrawAllocation"})
  protected void onDraw(Canvas paramCanvas)
  {
    if (this.g == 0)
      return;
    super.onDraw(paramCanvas);
    float f1 = this.a;
    float f2 = getStickWidth();
    float f3 = f2 - this.k;
    PointF localPointF1 = null;
    PointF localPointF2 = null;
    PointF localPointF3 = null;
    for (int i = 0; i < this.h; i++)
    {
      StockKLine localStockKLine = (StockKLine)this.l.get(i);
      float f4 = b(localStockKLine.getOpen());
      float f5 = b(localStockKLine.getHigh());
      float f6 = b(localStockKLine.getLow());
      float f7 = b(localStockKLine.getClose());
      if (f4 - f7 > 1.0F)
      {
        if (f3 >= 2.0F)
          paramCanvas.drawRect(f1, f7, f1 + f3, f4, this.m);
        paramCanvas.drawLine(f1 + f3 / 2.0F, f5, f1 + f3 / 2.0F, f6, this.m);
      }
      else if (f7 - f4 > 1.0F)
      {
        if (f3 >= 2.0F)
          paramCanvas.drawRect(f1, f4, f1 + f3, f7, this.n);
        paramCanvas.drawLine(f1 + f3 / 2.0F, f5, f1 + f3 / 2.0F, f6, this.n);
      }
      else
      {
        if (localStockKLine.isUp())
          this.o = this.m;
        else
          this.o = this.n;
        if (f3 >= 2.0F)
          paramCanvas.drawLine(f1, f7, f1 + f3, f4, this.o);
        paramCanvas.drawLine(f1 + f3 / 2.0F, f5, f1 + f3 / 2.0F, f6, this.o);
      }
      float f8;
      if (localStockKLine.getMa5() > 0.0F)
      {
        f8 = b(localStockKLine.getMa5());
        if (null != localPointF1)
          paramCanvas.drawLine(localPointF1.x, localPointF1.y, f1 + f3 / 2.0F, f8, this.p);
        localPointF1 = new PointF(f1 + f3 / 2.0F, f8);
      }
      if (localStockKLine.getMa10() > 0.0F)
      {
        f8 = b(localStockKLine.getMa10());
        if (null != localPointF2)
          paramCanvas.drawLine(localPointF2.x, localPointF2.y, f1 + f3 / 2.0F, f8, this.q);
        localPointF2 = new PointF(f1 + f3 / 2.0F, f8);
      }
      if (localStockKLine.getMa30() > 0.0F)
      {
        f8 = b(localStockKLine.getMa30());
        if (null != localPointF3)
          paramCanvas.drawLine(localPointF3.x, localPointF3.y, f1 + f3 / 2.0F, f8, this.r);
        localPointF3 = new PointF(f1 + f3 / 2.0F, f8);
      }
      f1 += f2;
    }
    a(paramCanvas, this.a + DipPixelUtil.dip2px(this.u, 10.0F), DipPixelUtil.dip2px(this.u, 8.0F), this.v);
  }

  public PointF a(int paramInt)
  {
    PointF localPointF = new PointF();
    localPointF.x = (this.a + getStickWidth() * paramInt + (getStickWidth() - this.k) / 2.0F);
    localPointF.y = b(((StockKLine)this.l.get(paramInt)).getClose());
    return localPointF;
  }

  public StockKLine b(int paramInt)
  {
    if ((paramInt < this.l.size()) && (paramInt >= 0))
      return (StockKLine)this.l.get(paramInt);
    return null;
  }

  public StockKLine getLastItem()
  {
    if (this.l != null)
      return (StockKLine)this.l.get(this.l.size() - 1);
    return null;
  }
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.chart.CandleChart
 * JD-Core Version:    0.6.2
 */