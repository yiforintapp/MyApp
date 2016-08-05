package com.zlf.appmaster.chartview.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.StockKLine;
import com.zlf.appmaster.chartview.bean.StockMinutes;
import com.zlf.appmaster.utils.DipPixelUtil;
import com.zlf.appmaster.utils.TimeUtil;

import java.util.Locale;

public class ChartTextLayout extends RelativeLayout
{
  private TextView a;
  private TextView b;
  private TextView c;
  private TextView d;
  private int e;
  private int f;
  private int g;
  private int h;
  private float i;
  private float j;
  private float k;
  private float l;
  private Context m;
  private com.zlf.appmaster.chartview.view.CrossLineView n;
  private LayoutParams o;
  private LayoutParams p;
  private LayoutParams q;
  private LayoutParams r;
  private String s;
  private String t;
  private String u;
  private String v;
  private float w;
  private int x = 0;

  public ChartTextLayout(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    a(paramContext);
  }

  public ChartTextLayout(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    a(paramContext);
  }

  public ChartTextLayout(Context paramContext)
  {
    super(paramContext);
    a(paramContext);
  }

  public void setPicMargins(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    removeAllViews();
    this.e = paramInt2;
    this.f = paramInt1;
    this.g = paramInt4;
    this.h = paramInt3;
    if (this.x == 2)
      this.f = ((int)(this.a.getWidth() + this.w));
    this.n = new com.zlf.appmaster.chartview.view.CrossLineView(this.m);
    LayoutParams localLayoutParams = new LayoutParams(-2, -2);
    localLayoutParams.setMargins(this.f, this.e, this.h, this.g);
    this.n.setCrossPoint(-100.0F, -100.0F);
    addView(this.n, localLayoutParams);
    addView(this.a, this.o);
    addView(this.b, this.p);
    addView(this.d, this.q);
    addView(this.c, this.r);
  }

  private void a(Context paramContext)
  {
    this.m = paramContext;
    this.a = new TextView(paramContext);
    this.a.setTextSize(2, 12.0F);
    this.a.setTextColor(this.m.getResources().getColor(R.color.item_white));
    this.a.setBackgroundResource(R.drawable.chart_text_bg);
    this.b = new TextView(paramContext);
    this.b.setTextSize(2, 12.0F);
    this.b.setTextColor(this.m.getResources().getColor(R.color.item_white));
    this.b.setBackgroundResource(R.drawable.chart_text_bg);
    this.d = new TextView(paramContext);
    this.d.setTextSize(2, 12.0F);
    this.d.setTextColor(this.m.getResources().getColor(R.color.item_white));
    this.d.setBackgroundResource(R.drawable.chart_text_bg);
    this.c = new TextView(paramContext);
    this.c.setTextSize(2, 12.0F);
    this.c.setSingleLine(true);
    this.d.setTextColor(this.m.getResources().getColor(R.color.item_white));
    this.c.setBackgroundResource(R.drawable.chart_text_bg);
    this.o = new LayoutParams(-2, -2);
    this.p = new LayoutParams(-2, -2);
    this.q = new LayoutParams(-2, -2);
    this.r = new LayoutParams(-2, -2);
    addView(this.a, this.o);
    addView(this.b, this.p);
    addView(this.d, this.q);
    addView(this.c, this.r);
    this.w = DipPixelUtil.dip2px(this.m, 2.0F);
  }

  public void setCrossPoint(float paramFloat1, float paramFloat2)
  {
    this.i = paramFloat1;
    this.j = paramFloat2;
    if (this.n != null)
    {
      this.n.setCrossPoint(paramFloat1 - this.f, paramFloat2 - this.e);
      this.n.postInvalidate();
    }
  }

  public void setStockInfo(StockKLine paramStockKLine, long paramLong)
  {
    this.s = String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(paramStockKLine.getClose()) });
    this.t = TimeUtil.getYearAndDay(paramStockKLine.getDataTime());
    float f1 = (float)paramStockKLine.getTradeCount();
    if (paramLong == 0L)
      this.v = "";
    else
      this.v = String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(f1 / (float)paramLong) });
    if ((this.x == 2) && (!TextUtils.isEmpty(this.v)))
      if (paramLong == 10000000000L)
      {
        this.v += "亿手";
      }
      else if (paramLong == 1000000L)
      {
        paramLong = 1000000L;
        this.v += "万手";
      }
      else
      {
        paramLong = 100L;
        this.v += "手";
      }
    this.u = null;
    a();
  }

  public void setVolumeY(float paramFloat1, float paramFloat2)
  {
    this.k = paramFloat2;
    this.l = paramFloat1;
  }

  public void setStockInfo(StockMinutes paramStockMinutes, long paramLong)
  {
    this.s = String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(paramStockMinutes.getNowPrice()) });
    this.t = TimeUtil.getHourAndMin(paramStockMinutes.getDataTime());
    float f1 = (paramStockMinutes.getNowPrice() - paramStockMinutes.getYestodayPrice()) * 100.0F / paramStockMinutes.getYestodayPrice();
    this.u = (String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(f1) }) + "%");
    if (this.x != 2)
    {
      float f2 = (float)paramStockMinutes.getTradeCount();
      this.v = String.format(Locale.getDefault(), "%.2f", new Object[] { Float.valueOf(f2 / (float)paramLong) });
    }
    a();
  }

  public int getChartTextMode()
  {
    return this.x;
  }

  public void setChartTextMode(int paramInt)
  {
    this.x = paramInt;
  }

  private void a()
  {
    this.a.setText(this.s);
    int i1 = this.a.getHeight();
    float f1 = this.j - this.a.getHeight() / 2;
    float f2 = 0.0F;
    if (this.x == 2)
      f2 = 0.0F;
    else
      f2 = this.f - this.a.getWidth() - this.w;
    this.o.setMargins((int)f2, (int)f1, 0, 0);
    if (!TextUtils.isEmpty(this.u))
    {
      this.b.setText(this.u);
      f2 = getWidth() - this.h + this.w;
      this.p.setMargins((int)f2, (int)f1, 0, 0);
    }
    else
    {
      this.b.setVisibility(8);
    }
    this.d.setText(this.t);
    f2 = this.i - this.d.getWidth() / 2;
    if (f2 < 0.0F)
      f2 = 0.0F;
    else if (f2 > getWidth() - this.d.getWidth() - this.h)
      f2 = getWidth() - this.d.getWidth() - this.h;
    f1 = this.l - this.d.getHeight() - this.w;
    this.q.setMargins((int)f2, (int)f1, 0, 0);
    if (TextUtils.isEmpty(this.v))
    {
      this.c.setVisibility(8);
    }
    else
    {
      this.c.setVisibility(0);
      this.c.setText(this.v);
      this.c.setTextColor(this.m.getResources().getColor(R.color.item_white));
      if (this.x == 2)
      {
        f2 = getWidth() - this.c.getWidth() - this.w;
        f1 = this.l;
      }
      else
      {
        f2 = this.f - this.c.getWidth() - this.w;
        f1 = this.k + this.l - this.c.getHeight() / 2;
        int i2 = this.c.getHeight();
        if (f1 + i2 > getHeight())
          f1 = getHeight() - i2;
      }
      this.r.setMargins((int)f2, (int)f1, 0, 0);
    }
    if (i1 == 0)
    {
      this.o.setMargins(-1000, 0, 0, 0);
      this.p.setMargins(-1000, 0, 0, 0);
      this.q.setMargins(-1000, 0, 0, 0);
      this.r.setMargins(-1000, 0, 0, 0);
    }
  }
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.view.ChartTextLayout
 * JD-Core Version:    0.6.2
 */