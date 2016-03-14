package com.leo.appmaster.backup;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.leo.appmaster.R;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.ThirdAppManager;
import com.leo.appmaster.model.AppItemInfo;

public class AppRestoreAdapter extends BaseAdapter {

    private ArrayList<AppItemInfo> mRestoreList;

    public AppRestoreAdapter(AppBackupRestoreManager manager) {
        mRestoreList = new ArrayList<AppItemInfo>();
    }

    public void updateData() {

        mRestoreList.clear();
        ArrayList<AppItemInfo> apps = ((ThirdAppManager) MgrContext.getManager(MgrContext.MGR_THIRD_APP)).
                getRestoreList(AppBackupRestoreManager.BACKUP_PATH);
//		ArrayList<AppItemInfo> apps = mBackupManager.
//                getRestoreList(AppBackupRestoreManager.BACKUP_PATH);
        if (apps != null) {
            for (AppItemInfo app : apps) {
                mRestoreList.add(app);
            }
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
        if (arg1 instanceof AppBackupItemView) {
            itemView = (AppRestoreItemView) arg1;
        } else {
            LayoutInflater inflater = LayoutInflater.from(arg2.getContext());
            itemView = (AppRestoreItemView) inflater.inflate(
                    R.layout.item_app_restore, null);
        }
        AppItemInfo app = mRestoreList.get(arg0);
        Context context = itemView.getContext();
        itemView.setIcon(app.icon);
        itemView.setTitle(app.label);
        itemView.setVersion(String.format(
                context.getResources().getString(R.string.app_version),
                app.versionName));
//		itemView.setSize(mBackupManager.getApkSize(app));
        itemView.setTag(app);
        return itemView;
    }

}
