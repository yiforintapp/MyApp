package com.zlf.appmaster.chartview.dimension;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import com.zlf.appmaster.utils.DipPixelUtil;
import com.zlf.appmaster.utils.QLog;

import java.util.ArrayList;
import java.util.Iterator;

public class DimensionChart extends View {

    private int a;
    private int b;
    private int c;
    private Context d;
    private Paint e;
    private Paint f;
    private Paint g;
    private PointF h;
    private float i;
    private e j;
    private ArrayList k;
    private d l;
    private d m;
    private boolean n;
    private c o;
    private int p;
    private ArrayList q;

    public DimensionChart(Context context) {
        super(context);
        a = 5;
        b = 270;
        c = 360 / a;
        n = false;
        a(context);
    }

    public DimensionChart(Context context, AttributeSet attributeset, int i1) {
        super(context, attributeset, i1);
        a = 5;
        b = 270;
        c = 360 / a;
        n = false;
        a(context);
    }

    public DimensionChart(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        a = 5;
        b = 270;
        c = 360 / a;
        n = false;
        a(context);
    }

    private void a(Context context) {
        d = context;
        float f1 = DipPixelUtil.dip2px(d, 1.0F);
        k = new ArrayList();
        h = new PointF();
        e = new Paint();
        e.setColor(0xfffcf9f4);
        e.setAntiAlias(true);
        e.setStyle(android.graphics.Paint.Style.FILL);
        e.setStrokeWidth(0.0F);
        f = new Paint();
        f.setColor(0xffc8b6a4);
        f.setAntiAlias(true);
        f.setStrokeWidth(f1);
        g = new Paint();
        g.setColor(0xf19a60);
        g.setAlpha(60);
        g.setAntiAlias(true);
        g.setStrokeWidth(f1);
        o = new c(this);
    }

    public void a() {
        if (k != null)
            k.clear();
    }

    public void a(d d1) {
        k.add(d1);
        invalidate();
    }

    public void setCompareDimension(d d1) {
        if (m == null) {
            m = d1.a();
            m.a = new ArrayList();
            for (int i1 = 0; i1 < d1.a.size(); i1++)
                m.a.add(Float.valueOf(0.0F));

        }
        p = 0;
        q = new ArrayList();
        for (int j1 = 0; j1 < d1.a.size(); j1++) {
            float f1 = (((Float) d1.a.get(j1)).floatValue() - ((Float) m.a.get(j1)).floatValue()) / 10F;
            q.add(Float.valueOf(f1));
        }

        o.sendEmptyMessage(0);
    }

    public void setBaseDimensionInfo(d d1) {
        l = d1;
        invalidate();
    }

    public int a(int i1) {
        return (i1 * c + b) % 360;
    }

    public PointF b(int i1) {
        PointF pointf = a(h.x, h.y, i, i1);
        return pointf;
    }

    private PointF a(float f1, float f2, float f3, int i1) {
        PointF pointf = new PointF();
        float f4 = f3;
        if (f3 > i)
            f4 = i;
        else if (f3 < i * 0.2F)
            f4 = i * 0.2F;
        double d1 = ((double) (b + (i1 - 1) * c) * 3.1415926535897931D) / 180D;
        pointf.x = f1 + (float) Math.cos(d1) * f4;
        pointf.y = f2 + (float) Math.sin(d1) * f4;
        return pointf;
    }

    public void setIsShowCircleRing(boolean flag) {
        n = flag;
    }

    private void a(Canvas canvas) {
        ArrayList arraylist = new ArrayList();
        for (int i1 = 0; i1 < a; i1++)
            arraylist.add(Float.valueOf(i));

        a(canvas, arraylist, h.x, h.y, false, e, 0, 0xffeddcc8, 0xfffcf9f4);
        a(canvas, h.x, h.y, i, 0xffc8b6a4);
        if (n) {
            for (int j1 = 1; j1 < a; j1++)
                b(canvas, h.x, h.y, (i * (float) j1) / 5F, 0xffeddcc8);

        }
    }

    private void a(Canvas canvas, float f1, float f2, Paint paint, d d1) {
        ArrayList arraylist = new ArrayList();
        for (int i1 = 0; i1 < d1.a.size(); i1++)
            arraylist.add(Float.valueOf((((Float) d1.a.get(i1)).floatValue() * i * 0.8F) / 100F + i * 0.2F));

        a(canvas, arraylist, f1, f2, d1.e, paint, d1.b, d1.c, d1.d);
    }

    private void a(Canvas canvas, ArrayList arraylist, float f1, float f2, boolean flag, Paint paint, int i1,
                   int j1, int k1) {
        if (arraylist == null || arraylist.size() < a) {
            QLog.e("DimensionChart", "dimensList == null || dimensList.size() < mDimensionNum");
            return;
        }
        f.setColor(j1);
        f.setAlpha(255);
        Path path = new Path();
        PointF pointf = a(f1, f2, ((Float) arraylist.get(0)).floatValue(), 1);
        path.moveTo(pointf.x, pointf.y);
        PointF pointf1 = pointf;
        for (int l1 = 2; l1 <= a; l1++) {
            PointF pointf2 = a(f1, f2, ((Float) arraylist.get(l1 - 1)).floatValue(), l1);
            path.lineTo(pointf2.x, pointf2.y);
            canvas.drawLine(pointf1.x, pointf1.y, pointf2.x, pointf2.y, f);
            pointf1 = pointf2;
        }

        canvas.drawLine(pointf1.x, pointf1.y, pointf.x, pointf.y, f);
        path.lineTo(pointf.x, pointf.y);
        paint.setColor(k1);
        paint.setAlpha(60);
        canvas.drawPath(path, paint);
        if (flag) {
            paint.setColor(i1);
            paint.setAlpha(255);
            for (int i2 = 0; i2 < a; i2++) {
                PointF pointf3 = a(f1, f2, ((Float) arraylist.get(i2)).floatValue(), i2 + 1);
                canvas.drawCircle(pointf3.x, pointf3.y, DipPixelUtil.dip2px(d, 3F), paint);
            }

        }
    }

    private void a(Canvas canvas, float f1, float f2, float f3, int i1) {
        PointF pointf = a(f1, f2, f3, 1);
        f.setColor(i1);
        f.setAlpha(50);
        for (int j1 = 2; j1 <= a; j1++) {
            PointF pointf1 = a(f1, f2, f3, j1);
            canvas.drawLine(f1, f2, pointf1.x, pointf1.y, f);
        }

        canvas.drawLine(f1, f2, pointf.x, pointf.y, f);
    }

    private void b(Canvas canvas, float f1, float f2, float f3, int i1) {
        PointF pointf = a(f1, f2, f3, 1);
        PointF pointf1 = pointf;
        f.setColor(i1);
        f.setAlpha(255);
        for (int j1 = 2; j1 <= a; j1++) {
            PointF pointf2 = a(f1, f2, f3, j1);
            canvas.drawLine(pointf1.x, pointf1.y, pointf2.x, pointf2.y, f);
            pointf1 = pointf2;
        }

        canvas.drawLine(pointf1.x, pointf1.y, pointf.x, pointf.y, f);
    }

    public void setOnDrawListener(e e1) {
        j = e1;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        i = getWidth() / 2;
        if (i <= 0.0F)
            return;
        if (i > (float) (getHeight() / 2))
            i = getHeight() / 2;
        i -= DipPixelUtil.dip2px(d, 5F);
        h.x = getWidth() / 2;
        h.y = getHeight() / 2;
        if (j != null)
            j.a();
        a(canvas);
        if (l != null)
            a(canvas, h.x, h.y, g, l);
        if (m != null)
            a(canvas, h.x, h.y, g, m);
        if (k != null) {
            d d1;
            for (Iterator iterator = k.iterator(); iterator.hasNext(); a(canvas, h.x, h.y, g, d1))
                d1 = (d) iterator.next();

        }
    }

    static int a(DimensionChart dimensionchart) {
        return dimensionchart.p;
    }

    static ArrayList b(DimensionChart dimensionchart) {
        return dimensionchart.q;
    }

    static d c(DimensionChart dimensionchart) {
        return dimensionchart.m;
    }

    static c d(DimensionChart dimensionchart) {
        return dimensionchart.o;
    }

    static int e(DimensionChart dimensionchart) {
        return dimensionchart.p++;
    }
}