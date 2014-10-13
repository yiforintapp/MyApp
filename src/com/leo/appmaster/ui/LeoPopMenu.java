
package com.leo.appmaster.ui;

import java.util.ArrayList;
import java.util.List;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class LeoPopMenu {
    
    private PopupWindow mLeoPopMenu;

    /**
     * @param aContext
     * @param anchorView 显示tab列表
     */
    public void showPopMenu(Activity activity, View anchorView) {
        if (mLeoPopMenu != null) {
            if (mLeoPopMenu.isShowing()) {
                return;
            }
            mLeoPopMenu = null;
        }

        View convertView = buildTabListLayout();

        mLeoPopMenu = new PopupWindow(convertView, LayoutParams.WRAP_CONTENT,  LayoutParams.WRAP_CONTENT, true);
        mLeoPopMenu.setFocusable(true);
        mLeoPopMenu.setOutsideTouchable(true);
        mLeoPopMenu.setBackgroundDrawable(AppMasterApplication.getInstance().getResources().getDrawable(R.drawable.popup_menu_bg));
        mLeoPopMenu.update();
        mLeoPopMenu.setAnimationStyle(R.style.PopupListAnim);
        mLeoPopMenu.showAsDropDown(anchorView, 0, 0);
    }

    public void dismissSnapshotList() {
        if (mLeoPopMenu != null) {
            mLeoPopMenu.dismiss();
            mLeoPopMenu = null;
        }
    }

    private View buildTabListLayout() {
        LayoutInflater inflater = LayoutInflater.from(AppMasterApplication.getInstance());
        LinearLayout convertView = (LinearLayout) inflater
                .inflate(R.layout.popmenu_window_list_layout, null);
        ListView listView = (ListView) convertView.findViewById(R.id.menu_list);

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    
                } else if (position == 1) {
                    
                }
                dismissSnapshotList();
            }
        });

        MenuListAdapter adapter = new MenuListAdapter(getPopMenuItems());
        listView.setAdapter(adapter);
        return convertView;
    }
   
    private List<String> getPopMenuItems() {
        List<String> listItems = new ArrayList<String>();
        Resources resources = AppMasterApplication.getInstance().getResources();
        listItems.add(resources.getString(R.string.app_setting_update));
        listItems.add(resources.getString(R.string.app_setting_about));

        return listItems;
    }
    
    static class Holder {
        public TextView mItemName;
    }

    private class MenuListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<String> mListItems;
        private Holder mHolder;

        private MenuListAdapter(List<String> itemList) {
            mListItems = itemList;
            inflater = LayoutInflater.from(AppMasterApplication.getInstance());
        }

        @Override
        public int getCount() {
            if (mListItems != null) {
                return mListItems.size();
            } else {
                return 0;
            }

        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView != null) {
                mHolder = (Holder) convertView.getTag();
            } else {
                mHolder = new Holder();
                convertView = inflater.inflate(R.layout.popmenu_window_list_item, null);
                mHolder.mItemName = (TextView) convertView.findViewById(R.id.menu_text);
                convertView.setTag(mHolder);
            }
            mHolder.mItemName.setText(mListItems.get(position));

            return convertView;
        }
    }
}
