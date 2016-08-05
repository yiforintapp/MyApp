package com.zlf.appmaster.chartview.dimension;

import android.view.View;


class b implements android.view.View.OnClickListener {

    int a;
    final CompareDimensionView b;

    public b(CompareDimensionView comparedimensionview, int i) {
        super();
        b = comparedimensionview;
        a = i;
    }

    public void onClick(View view) {
        if (CompareDimensionView.a(b) != null)
            CompareDimensionView.a(b).onClickItem(a, view);
    }
}