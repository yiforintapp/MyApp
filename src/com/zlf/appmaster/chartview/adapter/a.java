package com.zlf.appmaster.chartview.adapter;

import android.view.View;

class a
        implements android.view.View.OnClickListener {

    final int a;
    final DimensionAdapter b;

    a(DimensionAdapter dimensionadapter, int i) {
        super();
        b = dimensionadapter;
        a = i;
    }

    public void onClick(View view) {
        if (DimensionAdapter.a(b) != null)
            DimensionAdapter.a(b).onItemClick(null, null, a, a);
    }
}