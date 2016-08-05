package com.zlf.appmaster.chartview.chart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.zlf.appmaster.chartview.bean.StockMinutes;
import com.zlf.appmaster.utils.DipPixelUtil;
import com.zlf.appmaster.utils.QLog;
import java.util.ArrayList;

public class MinLineChart extends com.zlf.appmaster.chartview.chart.r
{
  public static final int MAX_SHOW_POINT = 242;
  private ArrayList<StockMinutes> b;
  private Paint c;
  private Paint d;
  private int e;
  private Path f;
  private Paint g;
  private float h;
  private float i;
  private float j;
  private float k;
  private ArrayList<PointF> l;
  private float m;
  private float n;

  public MinLineChart(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    a(paramContext);
  }

  public MinLineChart(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    a(paramContext);
  }

  public MinLineChart(Context paramContext)
  {
    super(paramContext);
    a(paramContext);
  }

  private void a(Context paramContext)
  {
    this.c = new Paint();
    this.c.setColor(3381759);
    this.c.setAntiAlias(true);
    this.c.setAlpha(255);
    this.c.setStyle(Style.FILL);
    this.c.setStrokeWidth(DipPixelUtil.dip2px(paramContext, 1.0F));
    this.d = new Paint();
    this.d.setStyle(Style.FILL);
    this.d.setColor(-3098484);
    this.d.setAntiAlias(true);
    this.d.setStrokeWidth(DipPixelUtil.dip2px(paramContext, 1.0F));
    this.g = new Paint();
    this.g.setColor(3381759);
    this.g.setAlpha(38);
    this.g.setAntiAlias(true);
    this.g.setStyle(Style.FILL);
    this.m = DipPixelUtil.dip2px(paramContext, 4.5F);
    this.n = DipPixelUtil.dip2px(paramContext, 2.5F);
    setIsZeroMid(true);
    setIsShowBottomTime(false);
    setIsShowDashLine(false);
  }

  public void setLineData(ArrayList<StockMinutes> paramArrayList, float paramFloat)
  {
    if ((paramArrayList == null) || (paramArrayList.size() == 0))
      return;
    this.b = paramArrayList;
    if (this.b.size() > 242)
      this.e = 242;
    else
      this.e = this.b.size();
    this.j = ((StockMinutes)this.b.get(0)).getNowPrice();
    this.k = ((StockMinutes)this.b.get(this.b.size() - 1)).getNowPrice();
    this.h = this.j;
    this.i = this.j;
    int i1 = 0;
    for (i1 = 0; i1 < this.b.size(); i1++)
    {
      float f1 = ((StockMinutes)this.b.get(i1)).getNowPrice();
      if (Float.compare(f1, this.i) >= 0)
        this.i = f1;
      else if (Float.compare(f1, this.h) <= 0)
        this.h = f1;
    }
    initData(this.j, this.k, this.i, this.h, paramFloat, ((StockMinutes)this.b.get(this.b.size() - 1)).getStockStatus());
    this.l = new ArrayList();
    this.l.add(new PointF());
    invalidate();
  }

  public float getLineLenth()
  {
    return getPicWidth() / 241.0F;
  }

  public int getItemNoFromX(float paramFloat)
  {
    int i1 = (int)((paramFloat - this.leftMargin) / getLineLenth());
    if (i1 > this.b.size() - 1)
      i1 = this.b.size() - 1;
    else if (i1 < 0)
      i1 = 0;
    return i1;
  }

  public PointF getPointFromNo(int paramInt)
  {
    PointF localPointF = new PointF();
    localPointF.x = (this.leftMargin + paramInt * getLineLenth());
    localPointF.y = getValueYSize(((StockMinutes)this.b.get(paramInt)).getNowPrice());
    return localPointF;
  }

  public PointF getPointFromX(float paramFloat)
  {
    PointF localPointF = new PointF();
    int i1 = getItemNoFromX(paramFloat);
    localPointF.x = (this.leftMargin + i1 * getLineLenth());
    localPointF.y = getValueYSize(((StockMinutes)this.b.get(i1)).getNowPrice());
    return localPointF;
  }

  private void a(Canvas paramCanvas, float paramFloat1, float paramFloat2)
  {
    paramCanvas.drawCircle(paramFloat1, paramFloat2, this.m, this.g);
    paramCanvas.drawCircle(paramFloat1, paramFloat2, this.n, this.c);
  }

  @SuppressLint({"DrawAllocation"})
  protected void onDraw(Canvas paramCanvas)
  {
    if ((this.b == null) || (this.b.size() == 0))
      return;
    super.onDraw(paramCanvas);
    float f2 = this.leftMargin;
    PointF localPointF1 = null;
    PointF localPointF2 = null;
    float f1 = getLineLenth();
    this.f = new Path();
    this.f.moveTo(this.leftMargin, getLineHeight());
    QLog.i("MinLineChart", "getLineHeight:" + getLineHeight() + ",getHeight:" + getHeight());
    for (int i1 = 0; i1 < this.e; i1++)
    {
      float f5 = ((StockMinutes)this.b.get(i1)).getNowPrice();
      float f3 = getValueYSize(f5);
      if (i1 > 0)
        paramCanvas.drawLine(localPointF1.x, localPointF1.y, f2, f3, this.c);
      localPointF1 = new PointF(f2, f3);
      this.f.lineTo(f2, f3);
      float f6 = ((StockMinutes)this.b.get(i1)).getMaValue();
      float f4 = getValueYSize(f6);
      if (i1 > 0)
        paramCanvas.drawLine(localPointF2.x, localPointF2.y, f2, f4, this.d);
      localPointF2 = new PointF(f2, f4);
      if (i1 != this.e - 1)
        f2 += f1;
    }
    this.f.lineTo(f2, getLineHeight());
    this.f.lineTo(this.leftMargin, getLineHeight());
    paramCanvas.drawPath(this.f, this.g);
    super.drawDashLine(paramCanvas);
    super.drawBottomTime(paramCanvas);
    a(paramCanvas, f2, getValueYSize(this.k));
    if (this.a)
      drawCornerValue(paramCanvas);
  }
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.chart.MinLineChart
 * JD-Core Version:    0.6.2
 */