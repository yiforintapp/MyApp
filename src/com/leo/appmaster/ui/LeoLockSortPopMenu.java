
package com.leo.appmaster.ui;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;

public class LeoLockSortPopMenu extends LeoPopMenu{

    private static int mCrrentIndex;
    
    public LeoLockSortPopMenu() {
        mIconOffest = 9;
    }
    
    public void setPopMenuItems(Context context, List<String> items, int currentIndex) {
        mContext = context;
        mCrrentIndex = currentIndex;
        mItems = items;
        mAdapter = new MenuSortListAdapter();
        setIconOffect();
        
        super.setPopMenuItems(context, items);
    }

    private void setIconOffect(){
        Display mDisplay = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        int W = mDisplay.getWidth();
        if(W == 240){
            mIconOffest = 40;
        }
    }
    
    private class MenuSortListAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        private MenuSortListAdapter() {
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
            /* Holder mHolder;
             * if (convertView != null) { mHolder = (Holder)
             * convertView.getTag(); } else { mHolder = new Holder();
             * convertView = inflater.inflate(R.layout.popmenu_window_list_item,
             * null); mHolder.mItemName = (TextView)
             * convertView.findViewById(R.id.menu_text); mHolder.mImageView =
             * (ImageView) convertView.findViewById(R.id.menu_icon);
             * convertView.setTag(mHolder); }
             * mHolder.mItemName.setText(mItems.get(position)); if (position ==
             * mCrrentIndex) {
             * mHolder.mItemName.setTextColor(mContext.getResources().getColor(
             * (R.color.sort_select_color)));
             * mHolder.mImageView.setVisibility(View.VISIBLE); }
             */
            convertView = inflater.inflate(R.layout.popmenu_window_list_item, null);
            TextView mItemName = (TextView) convertView.findViewById(R.id.menu_text);
            mItemName.setText(mItems.get(position));
            if (position == mCrrentIndex) {
                ImageView mImageView = (ImageView) convertView.findViewById(R.id.menu_icon);
                mItemName.setTextColor(mContext.getResources().getColor(
                        (R.color.sort_select_color)));
                if(mImageView!=null){
                mImageView.setVisibility(View.VISIBLE);
                }
            }
            return convertView;
        }
    }
    
    
}
