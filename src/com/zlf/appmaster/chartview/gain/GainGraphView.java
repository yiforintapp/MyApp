package com.zlf.appmaster.chartview.gain;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.dimension.LineChart;
import com.zlf.appmaster.utils.DipPixelUtil;
import com.zlf.appmaster.utils.TimeUtil;

import java.util.ArrayList;

public class GainGraphView extends RelativeLayout {

    private Context b;
    private LineChart c;
    private ArrayList d;
    private ArrayList e;
    private boolean f;

    public GainGraphView(Context context) {
        super(context);
        f = true;
        a(context, null);
    }

    public GainGraphView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        f = true;
        a(context, attributeset);
    }

    private void a(Context context, AttributeSet attributeset) {
        b = context;
        LayoutInflater layoutinflater = LayoutInflater.from(context);
        if (isInEditMode())
            return;
        View view = layoutinflater.inflate(R.layout.view_graph_dimension, this, true);
        c = (LineChart) view.findViewById(R.id.view_line);
        c.setShowCirclePoint(false);
        c.setShowHorizontalLine(false);
        c.setVerticalNum(5);
        c.setLineWidth(DipPixelUtil.dip2px(b, 2.6F));
        d = new ArrayList();
        e = new ArrayList();
        if (null != attributeset) {
            TypedArray typedarray = context.obtainStyledAttributes(attributeset, R.styleable.GainGraphView);
            if (null != typedarray) {
                f = typedarray.getBoolean(R.styleable.GainGraphView_show_percent, true);
                if (!f) {
                    f = false;
                    android.widget.RelativeLayout.LayoutParams layoutparams = new android.widget.RelativeLayout.LayoutParams(c.getLayoutParams());
                    layoutparams.setMargins(0, 0, 0, 0);
                    c.setLayoutParams(layoutparams);
                }
                typedarray.recycle();
            }
        }
    }

    private TextView a(Context context) {
        TextView textview = new TextView(context);
        textview.setTextSize(2, 10F);
        textview.setTextColor(0xff9d9d9d);
        return textview;
    }

    public void clearData() {
        c.a();
        for (int i = 0; i < d.size(); i++)
            removeView((View) d.get(i));

        d.clear();
        for (int j = 0; j < e.size(); j++)
            removeView((View) e.get(j));

        e.clear();
    }

    public void setLineData(ArrayList arraylist, int i, ArrayList arraylist1, int j, ArrayList arraylist2) {
        clearData();
        if (arraylist2 == null || arraylist2.size() == 0)
            return;
        int k = arraylist.size();
        if (k < 100)
            c.setLineWidth(DipPixelUtil.dip2px(b, 2.6F));
        else if (k < 200)
            c.setLineWidth(DipPixelUtil.dip2px(b, 1.6F));
        else
            c.setLineWidth(DipPixelUtil.dip2px(b, 1.0F));
        c.a(arraylist, i);
        if (arraylist1 != null)
            c.a(arraylist1, j);
        Paint paint = new Paint();
        paint.setTextSize(DipPixelUtil.sp2px(b, 10F));
        if (c.getBottom() != 0) {
            a(arraylist2, 0, 0, paint);
            a(arraylist2, c.getHorizontalNum() - 1, -1, paint);
            if (f) {
                for (int l = 0; l < c.getVerticalNum(); l++) {
                    TextView textview = a(b);
                    textview.setText(String.format("%.2f%%", new Object[]{
                            Float.valueOf(c.c(l) * 100F)
                    }));
                    e.add(textview);
                    android.widget.RelativeLayout.LayoutParams layoutparams = new android.widget.RelativeLayout.LayoutParams(-2, -2);
                    int i1 = c.getRight();
                    int j1 = (int) c.b(l);
                    layoutparams.setMargins(i1, j1, 0, 0);
                    addView(textview, layoutparams);
                }

            }
        }
        invalidate();
    }

    private void a(ArrayList arraylist, int i, int j, Paint paint) {
        TextView textview = a(b);
        d.add(textview);
        textview.setText(TimeUtil.getYearAndDay(((Long) arraylist.get(i)).longValue()));
        android.widget.RelativeLayout.LayoutParams layoutparams = new android.widget.RelativeLayout.LayoutParams(-2, -2);
        int k = c.getLeft() + (int) c.a(i);
        if (j == -1)
            k -= (int) paint.measureText(textview.getText().toString());
        int l = c.getBottom() + DipPixelUtil.dip2px(b, 2.0F);
        float f1 = paint.measureText(textview.getText().toString());
        int i1 = DipPixelUtil.dip2px(b, 12F);
        if (k < i1)
            k = i1;
        else if ((float) k + f1 > (float) getWidth())
            k = getWidth() - (int) f1;
        layoutparams.setMargins(k, l, 0, 0);
        addView(textview, layoutparams);
    }

}