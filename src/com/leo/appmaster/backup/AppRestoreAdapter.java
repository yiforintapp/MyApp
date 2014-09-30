package com.leo.appmaster.backup;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.leo.appmaster.R;
import com.leo.appmaster.model.AppDetailInfo;

public class AppRestoreAdapter extends BaseAdapter {
    
    private ArrayList<AppDetailInfo> mRestoreList; 
    
    private AppBackupRestoreManager mBackupManager;
    
    public AppRestoreAdapter(AppBackupRestoreManager manager) {
        mBackupManager = manager;
        mRestoreList = new ArrayList<AppDetailInfo>();
    }
    
    public void updateData() {
        mRestoreList.clear();
        ArrayList<AppDetailInfo> apps = mBackupManager.getRestoreList();
        for(AppDetailInfo app : apps) {
            mRestoreList.add(app);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mRestoreList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mRestoreList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        AppRestoreItemView itemView = null;
        if(arg1 instanceof AppBackupItemView) {
            itemView = (AppRestoreItemView) arg1;
        } else {
            LayoutInflater inflater = LayoutInflater.from(arg2.getContext());
            itemView = (AppRestoreItemView)inflater.inflate(R.layout.item_app_restore, null);
        }
        AppDetailInfo app = mRestoreList.get(arg0);
        itemView.setIcon(app.getAppIcon());
        itemView.setTitle(app.getAppLabel());
        itemView.setTag(app);
        return itemView;
    }

}
