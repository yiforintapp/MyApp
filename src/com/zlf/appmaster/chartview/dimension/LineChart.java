package com.zlf.appmaster.chartview.dimension;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import com.zlf.appmaster.utils.DipPixelUtil;

import java.util.ArrayList;
import java.util.Iterator;

public class LineChart extends View {

    private Context b;
    private int c;
    private int d;
    private Paint e;
    private Paint f;
    private Paint g;
    private float h;
    private ArrayList i;
    private float j;
    private float k;
    private boolean l;
    private boolean m;
    private boolean n;
    private boolean o;
    private k p;
    public static final PathEffect a = new DashPathEffect(new float[]{
            5F, 5F, 5F, 5F
    }, 1.0F);

    public LineChart(Context context) {
        super(context);
        l = true;
        m = true;
        n = true;
        o = true;
        a(context);
    }

    public LineChart(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        l = true;
        m = true;
        n = true;
        o = true;
        a(context);
    }

    public LineChart(Context context, AttributeSet attributeset, int i1) {
        super(context, attributeset, i1);
        l = true;
        m = true;
        n = true;
        o = true;
        a(context);
    }

    private void a(Context context) {
        b = context;
        e = new Paint();
        e.setColor(0xffededed);
        e.setAntiAlias(true);
        f = new Paint();
        f.setAntiAlias(true);
        f.setStrokeWidth(DipPixelUtil.dip2px(b, 1.0F));
        g = new Paint();
        g.setStyle(android.graphics.Paint.Style.STROKE);
        g.setAntiAlias(true);
        g.setPathEffect(a);
        h = DipPixelUtil.dip2px(b, 5F);
        c = 10;
        d = 9;
        i = new ArrayList();
    }

    public void setHorizontalNum(int i1) {
        c = i1;
    }

    public void setVerticalNum(int i1) {
        d = i1;
    }

    public int getHorizontalNum() {
        return c;
    }

    public int getVerticalNum() {
        return d;
    }

    private float getDrawLeft() {
        return h;
    }

    private float getDrawRight() {
        return (float) getWidth() - h;
    }

    private float getDrawTop() {
        return h;
    }

    private float getDrawBottom() {
        return (float) getHeight() - h;
    }

    private float getDrawHeight() {
        return (float) getHeight() - h * 2.0F;
    }

    private float getDrawWidth() {
        return (float) getWidth() - h * 2.0F;
    }

    public float a(int i1) {
        return (float) i1 * (getDrawWidth() / (float) (c - 1)) + getDrawLeft();
    }

    public float b(int i1) {
        return (float) (d - 1 - i1) * (getDrawHeight() / (float) (d - 1)) + getDrawTop();
    }

    public float c(int i1) {
        return ((float) i1 * (j - k)) / (float) (d - 1) + k;
    }

    private float a(float f1) {
        return ((j - f1) * getDrawHeight()) / (j - k) + getDrawTop();
    }

    private void a(Canvas canvas) {
        if (n) {
            for (int i1 = 0; i1 < c; i1++) {
                float f1 = a(i1);
                canvas.drawLine(f1, getDrawTop(), f1, getDrawBottom(), e);
            }

        }
        if (m) {
            for (int j1 = 0; j1 < d; j1++) {
                float f2 = b(j1);
                canvas.drawLine(getDrawLeft(), f2, getDrawRight(), f2, e);
            }

        }
    }

    private void a(Canvas canvas, ArrayList arraylist, int i1) {
        Path path = null;
        Path path1 = null;
        f.setColor(i1);
        g.setColor(i1);
        for (int j1 = 0; j1 < c && j1 < arraylist.size(); j1++) {
            if (arraylist.get(j1) == null) {
                if (path1 == null && path != null) {
                    path1 = new Path();
                    float f1 = a(j1 - 1);
                    float f3 = a(((Float) arraylist.get(j1 - 1)).floatValue());
                    path1.moveTo(f1, f3);
                }
                if (path != null) {
                    f.setStyle(android.graphics.Paint.Style.STROKE);
                    canvas.drawPath(path, f);
                    path = null;
                }
                continue;
            }
            float f2 = a(j1);
            float f4 = a(((Float) arraylist.get(j1)).floatValue());
            if (path == null) {
                path = new Path();
                path.moveTo(f2, f4);
            } else {
                path.lineTo(f2, f4);
            }
            if (path1 != null) {
                path1.lineTo(f2, f4);
                canvas.drawPath(path1, g);
                path1 = null;
            }
            if (l) {
                f.setStyle(android.graphics.Paint.Style.FILL);
                canvas.drawCircle(f2, f4, DipPixelUtil.dip2px(b, 4F), f);
            }
        }

        f.setStyle(android.graphics.Paint.Style.STROKE);
        if (path != null)
            canvas.drawPath(path, f);
    }

    public void setOnDrawListener(k k1) {
        p = k1;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (p != null)
            p.a();
        if (o)
            a(canvas);
        j j1;
        for (Iterator iterator = i.iterator(); iterator.hasNext(); a(canvas, j1.a, j1.b))
            j1 = (j) iterator.next();

    }

    private void b() {
        if (i == null || i.size() == 0 || ((j) i.get(0)).a == null || ((j) i.get(0)).a.size() == 0)
            return;
        float f1 = ((Float) ((j) i.get(0)).a.get(0)).floatValue();
        float f2 = ((Float) ((j) i.get(0)).a.get(0)).floatValue();
        c = ((j) i.get(0)).a.size();
        Iterator iterator = i.iterator();
        do {
            if (!iterator.hasNext())
                break;
            j j1 = (j) iterator.next();
            for (int i1 = 0; i1 < j1.a.size(); i1++) {
                if (j1.a.get(i1) == null)
                    continue;
                if (f1 < ((Float) j1.a.get(i1)).floatValue()) {
                    f1 = ((Float) j1.a.get(i1)).floatValue();
                    continue;
                }
                if (f2 > ((Float) j1.a.get(i1)).floatValue())
                    f2 = ((Float) j1.a.get(i1)).floatValue();
            }

            if (c < j1.a.size())
                c = j1.a.size();
        } while (true);
        a(f1, f2);
        invalidate();
    }

    private void a(float f1, float f2) {
        int i1 = 10000;
        float f3 = f1 - f2;
        float f4 = 0.01F;
        if (f3 > 0.03F) {
            f4 = 0.025F * (float) ((int) ((f3 * (float) i1) / 750F) + 1);
            if (f4 > 0.05F)
                f4 = 0.05F * (float) ((int) ((f3 * (float) i1) / 1500F) + 1);
        }
        j = f4 * (float) ((int) ((f1 * (float) i1) / (f4 * (float) i1)) + 1);
        k = f4 * (float) ((int) ((f2 * (float) i1) / (f4 * (float) i1)) - 1);
        d = (int) (((j - k) * (float) i1) / (f4 * (float) i1)) + 1;
    }

    public void a() {
        i.clear();
    }

    public void a(ArrayList arraylist, int i1) {
        i.add(new j(this, arraylist, i1));
        b();
    }

    public void b(ArrayList arraylist, int i1) {
        if (i.size() == 1)
            i.add(new j(this, arraylist, i1));
        else if (i.size() == 2)
            i.set(1, new j(this, arraylist, i1));
        b();
    }

    public void setShowCirclePoint(boolean flag) {
        l = flag;
    }

    public void setShowVerticalLine(boolean flag) {
        m = flag;
    }

    public void setShowHorizontalLine(boolean flag) {
        n = flag;
    }

    public void setShowGridLine(boolean flag) {
        o = flag;
    }

    public void setLineWidth(float f1) {
        f.setStrokeWidth(f1);
        g.setStrokeWidth(f1);
    }

}