
package com.leo.appmaster.ui;

import java.util.List;
import android.content.Context;
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
        mIconOffest = 12;
    }
    
    public void setPopMenuItems(Context context, List<String> items, int currentIndex) {
        mContext = context;
        mCrrentIndex = currentIndex;
        mItems = items;
        mAdapter = new MenuSortListAdapter();
        
        super.setPopMenuItems(context, items);
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
            convertView = inflater.inflate(R.layout.popmenu_window_list_item, null);
            TextView mItemName = (TextView) convertView.findViewById(R.id.menu_text);
            mItemName.setText(mItems.get(position));
            if (position == mCrrentIndex) {
                ImageView mImageView = (ImageView) convertView.findViewById(R.id.menu_icon);
                mItemName.setTextColor(mContext.getResources().getColor(
                        (R.color.sort_select_color)));
                mImageView.setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }
}
