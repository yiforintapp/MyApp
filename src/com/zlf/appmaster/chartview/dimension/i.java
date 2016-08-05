package com.zlf.appmaster.chartview.dimension;

import android.widget.TextView;

import com.zlf.appmaster.chartview.bean.DimensionChartInfo;
import com.zlf.appmaster.chartview.bean.DimensionItemInfo;

class i
        implements e {

    final DimensionChartInfo a;
    final DimensionView b;

    i(DimensionView dimensionview, DimensionChartInfo dimensionchartinfo) {
        super();
        b = dimensionview;
        a = dimensionchartinfo;
    }

    public void a() {
        if (!DimensionView.a(b)) {
            DimensionView.a(b, true);
            for (int j = 0; j < a.getSubItemList().size(); j++)
                DimensionView.a(b, (TextView) DimensionView.b(b).get(j), j, ((DimensionItemInfo) a.getSubItemList().get(j)).getName());

        }
    }
}