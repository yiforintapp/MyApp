package com.zlf.appmaster.chartview.dimension;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.DimensionChartInfo;
import com.zlf.appmaster.chartview.bean.DimensionItemInfo;
import com.zlf.appmaster.utils.QLog;

import java.util.ArrayList;
import java.util.Iterator;

public class CompareDimensionView extends RelativeLayout {

    private final Context a;
    private DimensionChart b;
    private ArrayList c;
    private boolean d;
    private OnClickDimensionItemListener e;

    public CompareDimensionView(Context context) {
        super(context);
        d = false;
        a = context;
        a(a);
    }

    public CompareDimensionView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        d = false;
        a = context;
        a(a);
    }

    public CompareDimensionView(Context context, AttributeSet attributeset, int i) {
        super(context, attributeset, i);
        d = false;
        a = context;
        a(a);
    }

    private void a(Context context) {
        LayoutInflater layoutinflater = LayoutInflater.from(context);
        if (isInEditMode())
            return;
        View view = layoutinflater.inflate(R.layout.view_detail_dimension, this, true);
        b = (DimensionChart) view.findViewById(R.id.view_chart);
        b.setIsShowCircleRing(false);
        c = new ArrayList();
        for (int i = 0; i < 5; i++) {
            g g1 = new g(a);
            c.add(g1);
            addView(g1);
        }

    }

    public void addCompareInfo(ArrayList arraylist, int i) {
        if (arraylist == null || arraylist.size() == 0) {
            QLog.e("CompareDimensionView", "compareInfos == null");
            return;
        }
        d d1 = new d();
        d1.a = new ArrayList();
        DimensionItemInfo dimensioniteminfo;
        for (Iterator iterator = arraylist.iterator(); iterator.hasNext(); d1.a.add(Float.valueOf(dimensioniteminfo.getScore())))
            dimensioniteminfo = (DimensionItemInfo) iterator.next();

        d1.b = i;
        d1.c = i;
        d1.d = i;
        b.setCompareDimension(d1);
        for (int j = 0; j < c.size(); j++) {
            ((g) c.get(j)).setCompareColor(i);
            ((g) c.get(j)).a(((DimensionItemInfo) arraylist.get(j)).getShowNumString());
            ((g) c.get(j)).setOnClickListener(new b(this, j));
        }

    }

    public void setOnClickDimensionItemListener(OnClickDimensionItemListener onclickdimensionitemlistener) {
        e = onclickdimensionitemlistener;
    }

    private void a(g g1, int i, String s, String s1, int j) {
        android.widget.RelativeLayout.LayoutParams layoutparams = new android.widget.RelativeLayout.LayoutParams(-2, -2);
        PointF pointf = b.b(i + 1);
        pointf.x += b.getLeft();
        pointf.y += b.getTop();
        g1.a(s, s1);
        h h1 = h.d;
        switch (i) {
            case 0: // '\0'
                h1 = h.a;
                break;

            case 1: // '\001'
                h1 = h.b;
                break;

            case 2: // '\002'
                h1 = h.b;
                break;

            case 3: // '\003'
                h1 = h.d;
                break;

            case 4: // '\004'
                h1 = h.d;
                break;
        }
        PointF pointf1 = g1.a(h1, pointf);
        layoutparams.setMargins((int) pointf1.x, (int) pointf1.y, 0, 0);
        g1.setLayoutParams(layoutparams);
        g1.setBaseColor(j);
    }

    public void setTopPointTitle(DimensionChartInfo dimensionchartinfo, int i) {
        if (dimensionchartinfo == null || dimensionchartinfo.getSubItemList() == null)
            return;
        ArrayList arraylist = new ArrayList();
        for (int j = 0; j < dimensionchartinfo.getSubItemList().size(); j++)
            arraylist.add(Float.valueOf(((DimensionItemInfo) dimensionchartinfo.getSubItemList().get(j)).getScore()));

        b.a();
        d d1 = new d();
        d1.d = i;
        d1.b = i;
        d1.c = i;
        d1.e = true;
        d1.a = arraylist;
        b.setBaseDimensionInfo(d1);
        b.setOnDrawListener(new a(this, dimensionchartinfo, i));
        d = false;
        b.invalidate();
        invalidate();
    }

    static OnClickDimensionItemListener a(CompareDimensionView comparedimensionview) {
        return comparedimensionview.e;
    }

    static boolean b(CompareDimensionView comparedimensionview) {
        return comparedimensionview.d;
    }

    static boolean a(CompareDimensionView comparedimensionview, boolean flag) {
        return comparedimensionview.d = flag;
    }

    static ArrayList c(CompareDimensionView comparedimensionview) {
        return comparedimensionview.c;
    }

    static void a(CompareDimensionView comparedimensionview, g g1, int i, String s, String s1, int j) {
        comparedimensionview.a(g1, i, s, s1, j);
    }
}