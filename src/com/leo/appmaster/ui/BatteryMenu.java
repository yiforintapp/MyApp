
package com.leo.appmaster.ui;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;

import java.util.List;

public class BatteryMenu extends LeoPopMenu {

    public BatteryMenu() {
        mIconOffest = 8;
    }

    public void setPopMenuItems(Context context, List<String> items, List<Integer> icons) {
        mContext = context;
        mItems = items;
        mIcons = icons;
        mAdapter = new MenuListAdapter();

        super.setPopMenuItems(context, items);
    }
    
    private class MenuListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private Holder mHolder;

        private MenuListAdapter() {
            inflater = LayoutInflater.from(AppMasterApplication.getInstance());
        }

        @Override
        public int getCount() {
            if (mItems != null) {
                return mItems.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            if (convertView != null) {
                mHolder = (Holder) convertView.getTag();
            } else {
                mHolder = new Holder();
                convertView = inflater.inflate(R.layout.popmenu_window_home_list_item, null);
                mHolder.mItemName = (TextView) convertView.findViewById(R.id.menu_text);
                convertView.setTag(mHolder);
            }
            
            if (mIsItemHTMLFormatted) {
                Spanned itemText = Html.fromHtml(mItems.get(position));
                mHolder.mItemName.setText(itemText);
            } else {
                mHolder.mItemName.setText(mItems.get(position));
            }
            return convertView;
        }
    }
    
    /**
     * update the icon of this item
     * @param position
     * @param drawable
     */
    public void updateItemIcon(int position,int drawable){
     View childView = mListView.getChildAt(position);
        if(null != childView){
            ImageView    itemIcon = (ImageView) childView.findViewById(R.id.menu_icon);
            itemIcon.setImageDrawable(mContext.getResources().getDrawable(drawable));
        }
    }
}
