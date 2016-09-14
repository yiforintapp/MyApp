package com.zlf.appmaster.hometab;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.stock.StockIndex;
import com.zlf.appmaster.ui.stock.StockTextView;
import com.zlf.appmaster.utils.Utilities;

import java.util.List;

/**
 * Created by forint on 16-5-13.
 */
public class HorizontalAdapter extends BaseAdapter {

    private List<StockIndex> mList;
    private Context mContext;
    private SparseArray<View> mCachedViews;
    private LayoutInflater layoutInflater;
    private int mWidth;

    public HorizontalAdapter(Context context, List<StockIndex> themes) {
        mContext = context;
        this.mList = themes;
        this.layoutInflater = LayoutInflater.from(context);
        mWidth = Utilities.getScreenSize(context)[0];

        mCachedViews = new SparseArray<View>();
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;
    }

    @Override
    public Object getItem(int arg0) {

        return mList != null ? mList.get(arg0) : null;
    }

    @Override
    public long getItemId(int arg0) {

        return arg0;
    }

    class ViewHolder {
        TextView name;
        StockTextView price;
        StockTextView percentPrompt;
        LinearLayout mParent;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        convertView = mCachedViews.get(position);
        boolean needPut = convertView == null;
        convertView = showNormalTypeItem(position, convertView);
        if (needPut) {
            mCachedViews.put(position, convertView);
        }

        return convertView;
    }

    private View showNormalTypeItem(int position, View convertView) {
        ViewHolder viewHolder;

        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            viewHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.horizontal_item, null);
            convertView.setTag(viewHolder);
        }

        return convertView;
    }

    private void setItemWidth(LinearLayout layout) {
        int itemWidth = mWidth / 3;
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        if (params != null) {
            params.width = itemWidth;
            layout.setLayoutParams(params);
        }
    }
}
