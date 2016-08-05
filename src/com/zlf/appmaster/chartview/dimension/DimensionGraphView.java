package com.zlf.appmaster.chartview.dimension;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.utils.DipPixelUtil;
import com.zlf.appmaster.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Iterator;

public class DimensionGraphView extends RelativeLayout {

    private LineChart a;
    private ArrayList b;
    private ArrayList c;
    private Context d;
    private android.widget.RelativeLayout.LayoutParams e;
    private ArrayList f;
    private float g;
    private int h;
    private Paint i;

    public DimensionGraphView(Context context) {
        super(context);
        g = 0.0F;
        a(context);
    }

    public DimensionGraphView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        g = 0.0F;
        a(context);
    }

    private void a(Context context) {
        d = context;
        if (isInEditMode()) {
            return;
        } else {
            h = DipPixelUtil.dip2px(d, 2.0F);
            i = new Paint();
            i.setTextSize(DipPixelUtil.sp2px(d, 10F));
            a = new LineChart(d);
            a.setShowCirclePoint(true);
            e = new android.widget.RelativeLayout.LayoutParams(-1, -1);
            addView(a, e);
            b = new ArrayList();
            c = new ArrayList();
            a.setOnDrawListener(new f(this));
            return;
        }
    }

    private TextView b(Context context) {
        TextView textview = new TextView(context);
        textview.setTextSize(2, 10F);
        textview.setTextColor(0xff9d9d9d);
        return textview;
    }

    private TextView a(int j, ArrayList arraylist) {
        TextView textview;
        if (arraylist.size() > j) {
            textview = (TextView) arraylist.get(j);
            removeView(textview);
        } else {
            textview = b(d);
            arraylist.add(textview);
        }
        return textview;
    }

    private void a() {
        if (f == null)
            return;
        for (int j = 0; j < a.getHorizontalNum(); j++) {
            TextView textview = a(j, b);
            textview.setText(TimeUtil.getQuarterTime(((Long) f.get(j)).longValue()));
            android.widget.RelativeLayout.LayoutParams layoutparams = new android.widget.RelativeLayout.LayoutParams(-2, -2);
            float f1 = i.measureText(textview.getText().toString());
            int l = (int) (((float) a.getLeft() + a.a(j)) - f1 / 2.0F);
            int j1 = a.getBottom() + h;
            layoutparams.setMargins(l, j1, 0, 0);
            addView(textview, layoutparams);
        }

        TextView textview1;
        for (Iterator iterator = c.iterator(); iterator.hasNext(); textview1.setText(""))
            textview1 = (TextView) iterator.next();

        for (int k = 0; k < a.getVerticalNum(); k++) {
            TextView textview2 = a(k, c);
            textview2.setText(String.format("%.2f", new Object[]{
                    Float.valueOf(a.c(k))
            }));
            android.widget.RelativeLayout.LayoutParams layoutparams1 = new android.widget.RelativeLayout.LayoutParams(-2, -2);
            float f2 = i.measureText(textview2.getText().toString());
            int i1 = (int) ((float) a.getLeft() - f2 - (float) h);
            int k1 = (int) a.b(k) - DipPixelUtil.sp2px(d, 5F);
            layoutparams1.setMargins(i1, k1, 0, 0);
            addView(textview2, layoutparams1);
        }

    }

    public void setBaseLineData(ArrayList arraylist, int j, ArrayList arraylist1) {
        a.a(arraylist, j);
        f = arraylist1;
        b();
        invalidate();
    }

    public void setCompareLineData(ArrayList arraylist, int j) {
        a.b(arraylist, j);
        b();
        invalidate();
    }

    private void b() {
        for (int j = 0; j < a.getVerticalNum(); j++) {
            float f1 = i.measureText(String.format("%.2f", new Object[]{
                    Float.valueOf(a.c(j))
            }));
            if (f1 > g)
                g = f1;
        }

        int k = DipPixelUtil.dip2px(d, 20F);
        e.setMargins((int) g + h, 0, k, k);
    }

    static void a(DimensionGraphView dimensiongraphview) {
        dimensiongraphview.a();
    }
}