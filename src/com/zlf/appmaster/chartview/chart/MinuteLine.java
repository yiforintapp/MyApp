package com.zlf.appmaster.chartview.chart;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.StockMinutes;
import com.zlf.appmaster.chartview.view.ChartTextLayout;
import com.zlf.appmaster.chartview.view.OnTouchChartListener;
import com.zlf.appmaster.utils.DipPixelUtil;

import java.util.ArrayList;
import java.util.Timer;

public class MinuteLine extends FrameLayout
        implements android.view.GestureDetector.OnGestureListener
{

  private MinLineChart b;
  private VolumeChart c;
  private ChartTextLayout d;
  protected int a;
  private Context e;
  private OnTouchChartListener f;
  private ArrayList g;
  private boolean h;
  private GestureDetector i;
  private Timer j;
  private final int k = 300;
  private Point l;
  private final int m = 10;
  private t n;

  public MinuteLine(Context context, AttributeSet attributeset, int i1)
  {
    super(context, attributeset, i1);
    a = 0;
    h = false;
    a(context, attributeset);
    a(context);
  }

  public MinuteLine(Context context, AttributeSet attributeset)
  {
    super(context, attributeset);
    a = 0;
    h = false;
    a(context, attributeset);
    a(context);
  }

  public MinuteLine(Context context)
  {
    super(context);
    a = 0;
    h = false;
    a(context, null);
    a(context);
  }

  private void a(Context context, AttributeSet attributeset)
  {
    TypedArray typedarray = context.obtainStyledAttributes(attributeset, R.styleable.MinuteLine);
    a = typedarray.getInt(R.styleable.MinuteLine_minute_mode, 0);
  }

  private void a(Context context)
  {
    e = context;
    int i1;
    if (a != 2)
      i1 = DipPixelUtil.dip2px(context, 10F);
    else
      i1 = 0;
    LinearLayout linearlayout = new LinearLayout(context);
    linearlayout.setOrientation(1);
    b = new MinLineChart(context);
    android.widget.LinearLayout.LayoutParams layoutparams = new android.widget.LinearLayout.LayoutParams(-1, 0, 3F);
    linearlayout.addView(b, layoutparams);
    c = new VolumeChart(context);
    android.widget.LinearLayout.LayoutParams layoutparams1 = new android.widget.LinearLayout.LayoutParams(-1, 0, 1.0F);
    layoutparams1.setMargins(0, i1, 0, 0);
    linearlayout.addView(c, layoutparams1);
    android.widget.FrameLayout.LayoutParams layoutparams2 = new android.widget.FrameLayout.LayoutParams(-1, -1);
    addView(linearlayout, layoutparams2);
    d = new ChartTextLayout(context);
    d.setVisibility(8);
    addView(d, layoutparams2);
    c.setVisibility(View.GONE);
    d.setVisibility(View.GONE);
    n = new t(this, this);
    setPicMode();
  }

  public void setInBigPicMode()
  {
    b.setIsShowBottomTime(true);
    b.setIsShowDashLine(true);
    b.setIsShowDivider(false);
    b.setIsShowMaxMin(false);
    b.setIsShowOutSide(true);
    c.setShowLeftValue(true);
    a = 1;
    i = new GestureDetector(e, this);
  }

  public void setPicMode()
  {
    if (a == 2)
    {
      b.setMinMode(2);
      b.setIsShowBottomTime(true);
      i = new GestureDetector(e, this);
    } else
    if (a == 1)
      setInBigPicMode();
  }

  private void a()
  {
    int i1 = DipPixelUtil.dip2px(e, 12F);
    float f1 = b.getMaxLeftTextArea();
    float f2 = c.getMaxTextAreaLeft();
    float f3 = f1 <= f2 ? f2 : f1;
    f3 *= 1.2F;
    b.setMaxLeftTextArea(f3);
    float f4 = b.getMaxRightTextArea() * 1.2F;
    b.setMaxRightTextArea(f4);
    c.a((int)b.leftMargin, (int)b.rightMargin, 0, i1);
    d.setPicMargins((int)b.leftMargin, (int)b.topMargin, (int)b.rightMargin, i1);
  }

  public ArrayList getData()
  {
    return g;
  }

  public void setMinuteData(ArrayList arraylist)
  {
    g = arraylist;
    float f1 = ((StockMinutes)arraylist.get(0)).getYestodayPrice();
    b.setLineData(arraylist, f1);
    c.a(0, 242);
    c.setSourceData(arraylist);
    if (a == 1)
      a();
    else
    if (a == 2)
    {
      c.a(0, 0, 0, 0);
      d.setChartTextMode(a);
      d.setPicMargins(0, 0, 0, 0);
    }
  }

  public void clearData(float f1)
  {
    long l1 = 0x209d9c0L;
    long l2 = 0x277b6c0L;
    ArrayList arraylist = new ArrayList();
    for (int i1 = 0; i1 < 242; i1++)
    {
      arraylist.add(new StockMinutes(f1, f1, 0L, 0L, 0, l1));
      if (l1 == l2)
        l1 = 0x2ca1c80L;
      else
        l1 += 60000L;
    }

    setMinuteData(arraylist);
  }

  public void setOnTouchChartListener(OnTouchChartListener ontouchchartlistener)
  {
    f = ontouchchartlistener;
  }

  private void a(float f1)
  {
    int i1 = b.getItemNoFromX(f1);
    PointF pointf = b.getPointFromNo(i1);
    d.setCrossPoint(pointf.x, pointf.y);
    d.setVolumeY(c.getTop(), c.a(i1));
    d.setStockInfo((StockMinutes)g.get(i1), c.getUnitValue());
    d.postInvalidate();
    if (f != null)
      f.onTouchDown(i1);
  }

  public boolean onTouchEvent(MotionEvent motionevent)
  {
    if (a == 1 || a == 2)
    {
      if (h)
      {
        if (motionevent.getAction() == 0 || motionevent.getAction() == 2)
          a(motionevent.getX());
        else
        if (motionevent.getAction() == 1)
        {
          d.setVisibility(8);
          h = false;
          if (f != null)
            f.onTouchUp();
        }
      } else
      {
        return a(motionevent);
      }
      return true;
    } else
    {
      return super.onTouchEvent(motionevent);
    }
  }

  public boolean onDown(MotionEvent motionevent)
  {
    return false;
  }

  public void onShowPress(MotionEvent motionevent)
  {
  }

  public boolean onSingleTapUp(MotionEvent motionevent)
  {
    return false;
  }

  public boolean onScroll(MotionEvent motionevent, MotionEvent motionevent1, float f1, float f2)
  {
    return false;
  }

  public void onLongPress(MotionEvent motionevent)
  {
    a(motionevent.getX());
    h = true;
    d.setVisibility(0);
  }

  public boolean onFling(MotionEvent motionevent, MotionEvent motionevent1, float f1, float f2)
  {
    return false;
  }

  private void b()
  {
    if (l == null)
    {
      return;
    } else
    {
      a(l.x);
      d.setVisibility(0);
      h = true;
      l = null;
      return;
    }
  }

  private boolean a(MotionEvent motionevent)
  {
    switch (motionevent.getAction())
    {
      case 0: // '\0'
        l = new Point((int)motionevent.getX(), (int)motionevent.getY());
        j = new Timer();
        j.schedule(new s(this), 300L);
        return true;

      case 2: // '\002'
        Point point = new Point((int)motionevent.getX(), (int)motionevent.getY());
        int i1 = Math.abs(point.x - l.x);
        int j1 = Math.abs(point.y - l.y);
        int k1 = (int)Math.sqrt(i1 * i1 + j1 * j1);
        boolean flag = k1 >= 10;
        if (flag)
        {
          j.cancel();
          return false;
        } else
        {
          return true;
        }
    }
    j.cancel();
    return false;
  }

  static void a(MinuteLine minuteline)
  {
    minuteline.b();
  }

  static t b(MinuteLine minuteline)
  {
    return minuteline.n;
  }
}