package com.leo.appmaster.backup;

import java.io.File;
import java.util.ArrayList;

import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppDetailInfo;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AppBackupAdapter extends BaseAdapter {
    
    private ArrayList<File> mBackupList; 
    
    public AppBackupAdapter() {
        mBackupList = new ArrayList<File>();
        ArrayList<AppDetailInfo> apps = AppLoadEngine.getInstance(null).getAllPkgInfo();
        for(AppDetailInfo app : apps) {
            if(!app.isSystemApp()) {
                mBackupList.add(new File(app.getSourceDir()));
            }
        }
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
        TextView text = new TextView(arg2.getContext());
        text.setText(mBackupList.get(arg0).getName());
        return text;
    }

}
