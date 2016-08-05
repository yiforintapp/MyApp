package com.zlf.appmaster.chartview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import com.zlf.appmaster.utils.DipPixelUtil;

public class CrossLineView extends View
{
  private Paint a;
  private Paint b;
  private Paint c;
  private Paint d;
  private float e;
  private float f;
  private float g;
  private float h;
  private float i;
  private float j;
  private float k;
  private float l;

  public CrossLineView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    a(paramContext);
  }

  public CrossLineView(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    a(paramContext);
  }

  public CrossLineView(Context paramContext)
  {
    super(paramContext);
    a(paramContext);
  }

  public void setPicMargins(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.i = paramInt2;
    this.j = paramInt1;
    this.k = paramInt4;
    this.l = paramInt3;
  }

  private void a(Context paramContext)
  {
    this.a = new Paint();
    this.a.setColor(-16777216);
    this.a.setStrokeWidth(DipPixelUtil.dip2px(paramContext, 1.0F));
    this.b = new Paint();
    this.b.setColor(3381759);
    this.b.setAntiAlias(true);
    this.b.setAlpha(255);
    this.b.setStyle(Style.FILL);
    this.b.setStrokeWidth(DipPixelUtil.dip2px(paramContext, 1.0F));
    this.c = new Paint();
    this.c.setColor(3381759);
    this.c.setAlpha(38);
    this.c.setAntiAlias(true);
    this.c.setStyle(Style.FILL);
    this.e = DipPixelUtil.dip2px(paramContext, 4.5F);
    this.f = DipPixelUtil.dip2px(paramContext, 2.5F);
    this.d = new Paint();
    this.d.setColor(-16777216);
    this.d.setTextSize(DipPixelUtil.sp2px(paramContext, 12.0F));
    this.d.setAntiAlias(true);
  }

  public void setLineWidth(float paramFloat)
  {
    this.a.setStrokeWidth(paramFloat);
  }

  public void setCrossPoint(float paramFloat1, float paramFloat2)
  {
    this.g = paramFloat1;
    this.h = paramFloat2;
  }

  protected void onDraw(Canvas paramCanvas)
  {
    paramCanvas.drawLine(this.j, this.h, getWidth() - this.l, this.h, this.a);
    paramCanvas.drawLine(this.g, this.i, this.g, getHeight() - this.k, this.a);
    paramCanvas.drawCircle(this.g, this.h, this.e, this.c);
    paramCanvas.drawCircle(this.g, this.h, this.f, this.b);
  }
}

/* Location:           C:\Users\Administrator.USER-20160712BM\Desktop\chart_classes\classes.jar
 * Qualified Name:     com.iqiniu.qiniu.view.CrossLineView
 * JD-Core Version:    0.6.2
 */