package com.leo.appmaster.backup;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.leo.appmaster.R;
import com.leo.appmaster.model.AppDetailInfo;

public class AppBackupAdapter extends BaseAdapter {
    
    private ArrayList<AppDetailInfo> mBackupList; 
    
    private AppBackupRestoreManager mBackupManager;
    
    public AppBackupAdapter(AppBackupRestoreManager manager) {
        mBackupManager = manager;
        mBackupList = new ArrayList<AppDetailInfo>();
    }
    
    public ArrayList<AppDetailInfo> getSelectedItems() {
        ArrayList<AppDetailInfo> selectedItems = new ArrayList<AppDetailInfo>();
        for(AppDetailInfo app : mBackupList) {
            if(app.isChecked) {
                selectedItems.add(app);
            }
        }
        return selectedItems;
    }
    
    public void updateData() {
        mBackupList.clear();
        ArrayList<AppDetailInfo> apps = mBackupManager.getBackupList();
        for(AppDetailInfo app : apps) {
            if(!app.isSystemApp()) {
                app.isChecked = false;
                mBackupList.add(app);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mBackupList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mBackupList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        AppBackupItemView itemView = null;
        if(arg1 instanceof AppBackupItemView) {
            itemView = (AppBackupItemView) arg1;
        } else {
            LayoutInflater inflater = LayoutInflater.from(arg2.getContext());
            itemView = (AppBackupItemView)inflater.inflate(R.layout.item_app_backup, null);
        }
        AppDetailInfo app = mBackupList.get(arg0);
        itemView.setIcon(app.getAppIcon());
        itemView.setTitle(app.getAppLabel());
        itemView.setVersion(app.getVersionName());
        itemView.setState(app.isBackuped ? AppBackupItemView.STATE_BACKUPED : app.isChecked ? AppBackupItemView.STATE_SELECTED : AppBackupItemView.STATE_UNSELECTED);
        itemView.setTag(app);
        return itemView;
    }

}
