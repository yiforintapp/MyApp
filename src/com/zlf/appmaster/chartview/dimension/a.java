package com.zlf.appmaster.chartview.dimension;


import com.zlf.appmaster.chartview.bean.DimensionChartInfo;
import com.zlf.appmaster.chartview.bean.DimensionItemInfo;

class a implements e {

    final DimensionChartInfo a;
    final int b;
    final CompareDimensionView c;

    a(CompareDimensionView comparedimensionview, DimensionChartInfo dimensionchartinfo, int i) {
        super();
        c = comparedimensionview;
        a = dimensionchartinfo;
        b = i;
    }

    public void a() {
        if (!CompareDimensionView.b(c)) {
            CompareDimensionView.a(c, true);
            for (int i = 0; i < a.getSubItemList().size(); i++)
                CompareDimensionView.a(c, (g) CompareDimensionView.c(c).get(i), i, ((DimensionItemInfo) a.getSubItemList().get(i)).getName(), ((DimensionItemInfo) a.getSubItemList().get(i)).getShowNumString(), b);

        }
    }
}