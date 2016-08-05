package com.zlf.appmaster.chartview.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.chartview.bean.DimensionChartInfo;
import com.zlf.appmaster.chartview.bean.DimensionItemInfo;

public class DimensionAdapter extends BaseAdapter {

    private Context a;
    private DimensionChartInfo b;
    private LayoutInflater c;
    private android.widget.AdapterView.OnItemClickListener d;

    public DimensionAdapter(Context context, DimensionChartInfo dimensionchartinfo) {
        a = context;
        c = LayoutInflater.from(context);
        b = dimensionchartinfo;
    }

    public void setListener(android.widget.AdapterView.OnItemClickListener onitemclicklistener) {
        d = onitemclicklistener;
    }

    public int getCount() {
        if (b != null && b.getSubItemList() != null)
            return b.getSubItemList().size();
        else
            return 0;
    }

    public Object getItem(int i) {
        if (b != null && b.getSubItemList() != null)
            return b.getSubItemList().get(i);
        else
            return null;
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public View getView(int i, View view, ViewGroup viewgroup) {
        if (b == null)
            return null;
        b b1;
        if (view == null) {
            view = c.inflate(R.layout.list_item_dimension, null);
            b1 = new b(this);
            b1.a = (TextView) view.findViewById(R.id.tv_dimension_name);
            b1.b = (Button) view.findViewById(R.id.btn_dimension_compare);
            b1.b.setOnClickListener(new a(this, i));
            view.setTag(b1);
        } else {
            b1 = (b) view.getTag();
        }
        b1.a.setText(((DimensionItemInfo) b.getSubItemList().get(i)).getName());
        return view;
    }

    static android.widget.AdapterView.OnItemClickListener a(DimensionAdapter dimensionadapter) {
        return dimensionadapter.d;
    }
}