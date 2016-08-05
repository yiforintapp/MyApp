package com.zlf.appmaster.chartview.dimension;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;

import com.zlf.appmaster.utils.DipPixelUtil;

public class g extends View
{

  private h a;
  private String b;
  private String c;
  private String d;
  private int e;
  private int f;
  private int g;
  private StaticLayout h;
  private Paint i;
  private Paint j;
  private Paint k;
  private int l;
  private int m;
  private TextPaint n;
  private int o;
  private Context p;

  public g(Context context)
  {
    super(context);
    o = 0;
    p = context;
    k = new Paint();
    k.setAntiAlias(true);
    k.setTextSize(DipPixelUtil.sp2px(p, 14F));
    k.setColor(0xff333333);
    g = DipPixelUtil.dip2px(p, 110F);
    e = DipPixelUtil.dip2px(p, 6F);
    f = DipPixelUtil.dip2px(p, 9F);
    i = new Paint();
    i.setColor(0xffeddcc8);
    i.setAntiAlias(true);
    i.setStyle(android.graphics.Paint.Style.STROKE);
    i.setStrokeWidth(DipPixelUtil.dip2px(p, 1.0F));
    l = -23706;
    m = 0xff7dd1bc;
    j = new Paint();
  }

  public void setBaseColor(int i1)
  {
    l = i1;
  }

  public void setCompareColor(int i1)
  {
    m = i1;
  }

  public void a(String s)
  {
    d = s;
    invalidate();
  }

  public void a(String s, String s1)
  {
    if (s == null)
      s = "";
    b = s;
    c = s1;
    n = new TextPaint();
    n.setAntiAlias(true);
    n.setTextSize(DipPixelUtil.sp2px(p, 12.5F));
    n.setColor(0xff333333);
    int i1 = (int)n.measureText(s);
    if (i1 > g)
      i1 = g;
    h = new StaticLayout(b, n, i1, android.text.Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
  }

  public PointF a(h h1, PointF pointf)
  {
    if (h == null)
      return null;
    PointF pointf1 = new PointF();
    pointf1.x = pointf.x;
    pointf1.y = pointf.y;
    a = h1;
    if (h1 == com.zlf.appmaster.chartview.dimension.h.b)
      pointf1.y -= h.getHeight() - f;
    else
    if (h1 == com.zlf.appmaster.chartview.dimension.h.a)
      pointf1.y -= h.getHeight() + f;
    else
    if (h1 == com.zlf.appmaster.chartview.dimension.h.c)
    {
      pointf1.x -= g + e;
      pointf1.y -= h.getHeight() + f;
    } else
    if (h1 == com.zlf.appmaster.chartview.dimension.h.d)
    {
      pointf1.x -= g + e;
      pointf1.y -= h.getHeight() - f;
    }
    pointf1.y -= DipPixelUtil.dip2px(p, 5F);
    return pointf1;
  }

  private void a(Canvas canvas, String s, String s1, int i1, int j1)
  {
    if (TextUtils.isEmpty(s))
      return;
    Rect rect = new Rect();
    rect.left = i1;
    rect.top = j1;
    rect.right = rect.left + DipPixelUtil.dip2px(p, 5F);
    rect.bottom = rect.top + DipPixelUtil.dip2px(p, 5F);
    j.setColor(l);
    canvas.drawRect(rect, j);
    j1 += DipPixelUtil.dip2px(p, 6F);
    i1 = rect.right + DipPixelUtil.dip2px(p, 3F);
    canvas.drawText(s, i1, j1, k);
    i1 = (int)((float)i1 + (k.measureText(s) + (float)DipPixelUtil.dip2px(p, 13F)));
    rect.left = i1;
    rect.right = rect.left + DipPixelUtil.dip2px(p, 5F);
    j.setColor(m);
    canvas.drawRect(rect, j);
    i1 = rect.right + DipPixelUtil.dip2px(p, 3F);
    if (!TextUtils.isEmpty(s1))
    {
      canvas.drawText(s1, i1, j1, k);
      i1 += (int)k.measureText(s1);
    }
    int k1 = j1 + (int)k.getTextSize();
    int l1 = getLayoutParams().width;
    int i2 = getLayoutParams().height;
    if (i1 < o)
      i1 = o;
    if (i2 != k1 || l1 != i1)
    {
      getLayoutParams().width = i1;
      getLayoutParams().height = k1;
      requestLayout();
    }
  }

  private void a(Canvas canvas, Point point, Point point1, Point point2)
  {
    Path path = new Path();
    path.moveTo(point.x, point.y);
    path.lineTo(point1.x, point1.y);
    path.lineTo(point2.x, point2.y);
    canvas.drawPath(path, i);
    o = point.x <= point2.x ? point2.x : point.x;
  }

  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);
    if (h == null)
      return;
    int i1 = h.getHeight() + DipPixelUtil.dip2px(p, 5F);
    if (a == com.zlf.appmaster.chartview.dimension.h.b)
    {
      Point point = new Point();
      point.x = 0;
      point.y = i1 - f;
      Point point4 = new Point(point);
      point4.x += e;
      point4.y += f;
      Point point8 = new Point(point4);
      point8.x += g;
      a(canvas, point, point4, point8);
      Point point12 = new Point(point4);
      point12.y += DipPixelUtil.dip2px(p, 7F);
      point12.x += DipPixelUtil.dip2px(p, 7F);
      a(canvas, c, d, point12.x, point12.y);
      canvas.translate(e + DipPixelUtil.dip2px(p, 5F), 0.0F);
      h.draw(canvas);
    } else
    if (a == com.zlf.appmaster.chartview.dimension.h.a)
    {
      Point point1 = new Point();
      point1.x = 0;
      point1.y = i1 + f;
      Point point5 = new Point(point1);
      point5.x += e;
      point5.y -= f;
      Point point9 = new Point(point5);
      point9.x += g;
      a(canvas, point1, point5, point9);
      Point point13 = new Point(point5);
      point13.y += DipPixelUtil.dip2px(p, 7F);
      point13.x += DipPixelUtil.dip2px(p, 7F);
      a(canvas, c, d, point13.x, point13.y);
      canvas.translate(e + DipPixelUtil.dip2px(p, 5F), 0.0F);
      h.draw(canvas);
    } else
    if (a == com.zlf.appmaster.chartview.dimension.h.c)
    {
      Point point2 = new Point();
      point2.x = g + e;
      point2.y = i1 + f;
      Point point6 = new Point(point2);
      point6.x -= e;
      point6.y -= f;
      Point point10 = new Point(point6);
      point10.x -= g;
      a(canvas, point2, point6, point10);
      Point point14 = new Point(point6);
      point14.y += DipPixelUtil.dip2px(p, 7F);
      a(canvas, c, d, 0, point14.y);
      if (h.getWidth() < g - DipPixelUtil.dip2px(p, 5F))
        canvas.translate(g - h.getWidth() - DipPixelUtil.dip2px(p, 5F), 0.0F);
      h.draw(canvas);
    } else
    if (a == com.zlf.appmaster.chartview.dimension.h.d)
    {
      Point point3 = new Point();
      point3.x = g + e;
      point3.y = i1 - f;
      Point point7 = new Point(point3);
      point7.x -= e;
      point7.y += f;
      Point point11 = new Point(point7);
      point11.x -= g;
      a(canvas, point3, point7, point11);
      Point point15 = new Point(point7);
      point15.y += DipPixelUtil.dip2px(p, 7F);
      a(canvas, c, d, 0, point15.y);
      if (h.getWidth() < g - DipPixelUtil.dip2px(p, 5F))
        canvas.translate(g - h.getWidth() - DipPixelUtil.dip2px(p, 5F), 0.0F);
      h.draw(canvas);
    }
  }
}