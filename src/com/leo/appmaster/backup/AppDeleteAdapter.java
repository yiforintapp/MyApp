
package com.leo.appmaster.backup;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.leo.appmaster.R;
import com.leo.appmaster.model.AppItemInfo;

public class AppDeleteAdapter extends BaseAdapter{

    private ArrayList<AppItemInfo> mDeleteList;
    private AppBackupRestoreManager mDeleteManager;

    public AppDeleteAdapter(AppBackupRestoreManager manager, Context context) {
        mDeleteManager = manager;
        mDeleteList = new ArrayList<AppItemInfo>();
    }

    public void updateData() {
        mDeleteList.clear();
        AppItemInfo[] apps = mDeleteManager.getDeleteList().toArray(new AppItemInfo[0]);
        for (AppItemInfo app : apps) {
            if(app != null) {
                app.isChecked = false;
                mDeleteList.add(app);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDeleteList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDeleteList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppDeletetemView itemView_delete = null;
        if (convertView instanceof AppDeletetemView) {
            itemView_delete = (AppDeletetemView) convertView;
        } else {
            LayoutInflater inflater =
                    LayoutInflater.from(parent.getContext());
            itemView_delete = (AppDeletetemView) inflater.inflate(
                    R.layout.listview_delete, null);
        }

        AppItemInfo app = mDeleteList.get(position);

        itemView_delete.setIcon(app.icon);
        itemView_delete.setTitle(app.label);
        
//        itemView_delete.setVersion(String.format(
//                mContext.getResources().getString(R.string.app_version),
//                app.versionName));
//        itemView_delete.setSize(mDeleteManager.getApkSize(app));
        itemView_delete.setSize(mDeleteManager.convertToSizeString(app.cacheInfo.total));
        itemView_delete.setTag(app);
        
        return itemView_delete;
    }



}
