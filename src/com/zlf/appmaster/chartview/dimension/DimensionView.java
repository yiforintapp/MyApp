package com.zlf.appmaster.chartview.dimension;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.PointF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.DimensionChartInfo;
import com.zlf.appmaster.chartview.bean.DimensionItemInfo;
import com.zlf.appmaster.utils.DipPixelUtil;
import com.zlf.appmaster.utils.QLog;

import java.util.ArrayList;

public class DimensionView extends RelativeLayout {

    private Context a;
    private DimensionChart b;
    private ArrayList c;
    private boolean d;
    private Paint e;

    public DimensionView(Context context) {
        super(context);
        d = false;
        a = context;
        a(a);
    }

    public DimensionView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        d = false;
        a = context;
        a(a);
    }

    public DimensionView(Context context, AttributeSet attributeset, int j) {
        super(context, attributeset, j);
        d = false;
        a = context;
        a(a);
    }

    private void a(Context context) {
        e = new Paint();
        e.setTextSize(DipPixelUtil.dip2px(a, 13F));
        LayoutInflater layoutinflater = LayoutInflater.from(context);
        if (isInEditMode())
            return;
        View view = layoutinflater.inflate(R.layout.view_dimension, this, true);
        b = (DimensionChart) view.findViewById(R.id.view_chart);
        c = new ArrayList();
        for (int j = 0; j < 5; j++) {
            TextView textview = new TextView(a);
            textview.setTextSize(2, 13F);
            c.add(textview);
            addView(textview);
        }

    }

    private void a(TextView textview, int j, String s) {
        android.widget.RelativeLayout.LayoutParams layoutparams = new android.widget.RelativeLayout.LayoutParams(-2, -2);
        if (TextUtils.isEmpty(s)) {
            QLog.e("DimensionView", (new StringBuilder()).append("text").append(j).append(":=null").toString());
            return;
        }
        textview.setText(s);
        int k = b.a(j);
        PointF pointf = b.b(j + 1);
        pointf.x += b.getLeft();
        pointf.y += b.getTop();
        int l = (int) e.measureText(s);
        android.graphics.Paint.FontMetricsInt fontmetricsint = e.getFontMetricsInt();
        int i1 = 0 - (fontmetricsint.bottom + fontmetricsint.top);
        int j1;
        int k1;
        if (k > 45 && k <= 135) {
            if (l > DipPixelUtil.dip2px(a, 78F)) {
                String s1 = (new StringBuilder()).append(s.substring(0, 6)).append("...").toString();
                l = (int) e.measureText(s1);
                textview.setText(s1);
            }
            textview.setSingleLine();
            j1 = (int) pointf.x - l / 2;
            k1 = (int) pointf.y;
        } else if (k > 135 && k <= 225) {
            int l1 = DipPixelUtil.dip2px(a, 92F);
            textview.setSingleLine(false);
            textview.setWidth(l1);
            if (l > l1)
                j1 = (int) pointf.x - l1;
            else
                j1 = (int) pointf.x - l;
            j1 -= DipPixelUtil.dip2px(a, 5F);
            k1 = (int) pointf.y - i1 / 2;
        } else if (k > 225 && k <= 315) {
            textview.setSingleLine();
            j1 = (int) pointf.x - l / 2;
            k1 = (int) pointf.y - 2 * i1;
        } else {
            textview.setSingleLine(false);
            textview.setWidth(DipPixelUtil.dip2px(a, 92F));
            j1 = (int) pointf.x + DipPixelUtil.dip2px(a, 5F);
            k1 = (int) pointf.y - i1 / 2;
        }
        layoutparams.setMargins(j1, k1, 0, 0);
        textview.setLayoutParams(layoutparams);
    }

    public void setTopPointTitle(DimensionChartInfo dimensionchartinfo) {
        if (dimensionchartinfo == null || dimensionchartinfo.getSubItemList() == null)
            return;
        ArrayList arraylist = new ArrayList();
        ArrayList arraylist1 = new ArrayList();
        for (int j = 0; j < dimensionchartinfo.getSubItemList().size(); j++) {
            arraylist.add(Float.valueOf(((DimensionItemInfo) dimensionchartinfo.getSubItemList().get(j)).getScore()));
            arraylist1.add(Float.valueOf(((DimensionItemInfo) dimensionchartinfo.getSubItemList().get(j)).getAverage()));
        }

        b.a();
        d d1 = new d();
        d1.d = 0xd3d2d0;
        d1.b = 0;
        d1.c = 0xb8b8b8;
        d1.e = false;
        d1.a = arraylist1;
        b.a(d1);
        d d2 = new d();
        d2.d = dimensionchartinfo.getBaseColor();
        d2.b = dimensionchartinfo.getBaseColor();
        d2.c = dimensionchartinfo.getBaseColor();
        d2.e = true;
        d2.a = arraylist;
        b.a(d2);
        b.setOnDrawListener(new i(this, dimensionchartinfo));
        d = false;
        b.invalidate();
        invalidate();
    }

    static boolean a(DimensionView dimensionview) {
        return dimensionview.d;
    }

    static boolean a(DimensionView dimensionview, boolean flag) {
        return dimensionview.d = flag;
    }

    static ArrayList b(DimensionView dimensionview) {
        return dimensionview.c;
    }

    static void a(DimensionView dimensionview, TextView textview, int j, String s) {
        dimensionview.a(textview, j, s);
    }
}