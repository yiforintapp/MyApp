
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
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.utils.LeoLog;

public class AppBackupAdapter extends BaseAdapter {

    private boolean isAllcheck = false;
    private ArrayList<AppItemInfo> mBackupList;
    private AppBackupRestoreManager mBackupManager;
    private LayoutInflater layoutInflater;
    private Context mContext;

    public AppBackupAdapter(AppBackupRestoreManager manager, Context context) {
        this.mContext = context;
        mBackupManager = manager;
        mBackupList = new ArrayList<AppItemInfo>();
        layoutInflater = LayoutInflater.from(mContext);
    }

    public void setisAllCheck(boolean check) {
        this.isAllcheck = check;
    }

    public boolean getisAllCheck() {
        return isAllcheck;
    }

    public ArrayList<AppItemInfo> getSelectedItems() {
        ArrayList<AppItemInfo> selectedItems = new ArrayList<AppItemInfo>();
        for (AppItemInfo app : mBackupList) {
            if (app.isChecked) {
                selectedItems.add(app);
            }
        }
        return selectedItems;
    }

    public boolean hasBackupApp() {
        for (AppItemInfo app : mBackupList) {
            if (!app.isBackuped) {
                return true;
            }
        }
        return false;
    }


    public void updateData() {
        mBackupList.clear();
        ArrayList<AppItemInfo> apps = ((ThirdAppManager) MgrContext.
                getManager(MgrContext.MGR_THIRD_APP)).
                getBackupList(AppBackupRestoreManager.BACKUP_PATH);
        for (AppItemInfo app : apps) {

            if (app.isBackuped) {
                app.isChecked = false;
            }

            mBackupList.add(app);
        }
        notifyDataSetChanged();
    }

    public void checkAll(boolean check) {
        LeoLog.d("testresume", "mBackupList size is : " + mBackupList.size());
        for (AppItemInfo app : mBackupList) {
            if (app.isBackuped)
                continue;
            app.isChecked = check;
        }
        notifyDataSetChanged();
    }

    public boolean checkAllIsFill(boolean isChecked) {
        if (!isChecked) {
            int totalAppNum = mBackupList.size();
            int countBackUpNum = 0;
            int countCheckNum = 0;
            for (AppItemInfo app : mBackupList) {
                if (app.isBackuped) {
                    countBackUpNum++;
                    continue;
                }
                if (app.isChecked) {
                    countCheckNum++;
                }
            }

            LeoLog.d("BackUpFragment", "totalAppNum : " + totalAppNum + "--countBackUpNum:"
                    + countBackUpNum + "--countCheckNum:" + countCheckNum);
            if (totalAppNum - 1 == countBackUpNum + countCheckNum) {
                return true;
            }
        }
        return false;

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
        MaterialRippleLayout headView = (MaterialRippleLayout) arg1;
        if (headView == null) {
            headView = (MaterialRippleLayout) layoutInflater.inflate(R.layout.item_app_backup, null);
        } else {
            headView = (MaterialRippleLayout) arg1;
        }

        AppBackupItemView itemView = (AppBackupItemView) headView.findViewById(R.id.content_all_view);

//        if (arg1 instanceof AppBackupItemView) {
//            itemView = (AppBackupItemView) arg1;
//        } else {
//            LayoutInflater inflater = LayoutInflater.from(arg2.getContext());
//            itemView = (AppBackupItemView) inflater.inflate(
//                    R.layout.item_app_backup, null);
//        }

        AppItemInfo app = mBackupList.get(arg0);
//        Context context = itemView.getContext();
        itemView.setIcon(app.icon);
        itemView.setTitle(app.label);

        itemView.setSize(mBackupManager.getApkSize(app));
        itemView.setState(app.isBackuped ? AppBackupItemView.STATE_BACKUPED
                : app.isChecked ? AppBackupItemView.STATE_SELECTED
                : AppBackupItemView.STATE_UNSELECTED);
        headView.setTag(app);
        return headView;
    }

}
