
package com.zlf.appmaster.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.model.WinTopItem;
import com.zlf.appmaster.utils.Utilities;

import java.util.List;

public class WinTopAdapter extends BaseAdapter {
    Context mContext;
    List<WinTopItem> mList;
    LayoutInflater mInflater;
    private int mWidth;

    public WinTopAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mWidth = Utilities.getScreenSize(context)[0];
    }

    public void setList(List<WinTopItem> list) {
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.win_top_item, null);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.tv_win_name);
            viewHolder.price = (TextView) convertView.findViewById(R.id.tv_win_price);
            viewHolder.width = (RelativeLayout) convertView.findViewById(R.id.set_width);
            viewHolder.tv_crown = (TextView) convertView.findViewById(R.id.tv_crown);
            viewHolder.crown = (RelativeLayout) convertView.findViewById(R.id.crown);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        setItemWidth(viewHolder.width);
        setCrown(viewHolder.crown, viewHolder.tv_crown, position);
        WinTopItem item = (WinTopItem) getItem(position);
        viewHolder.name.setText(item.getWinName());
        viewHolder.price.setText(item.getWinPrice() + "");
        return convertView;
    }

    private void setCrown(RelativeLayout crown, TextView tv_crown, int position) {
        if (position == 0 || position == 1 || position == 2) {
            crown.setBackgroundResource(R.drawable.rank_icon_top);
        } else {
            crown.setBackgroundResource(R.drawable.rank_icon_other);
        }
        tv_crown.setText(position + 1 + "");
    }

    class ViewHolder {
        TextView name, price, tv_crown;
        RelativeLayout width, crown;
    }

    private void setItemWidth(RelativeLayout layout) {
        int itemWidth = mWidth / 3;
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        if (params != null) {
            params.width = itemWidth;
            layout.setLayoutParams(params);
        }
    }

}
