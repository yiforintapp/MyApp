package com.leo.appmaster.mgr.impl;

import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.ThirdAppManager;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.List;

public class ThridAppManagerImpl extends ThirdAppManager {
    @Override
    public void onDestory() {

    }

    @Override
    public ArrayList<AppItemInfo> getDeleteList(List<String> dropList) {
        //dropList have two part , 1,s:startwith_packagename 2,a:contain_packagename
        return AppLoadEngine.getInstance(mContext).getDeleteableApps(dropList);
    }

    @Override
    public void uninstallApp(String packName) {
        AppLoadEngine.getInstance(mContext).uninstallApp(packName);
    }

    @Override
    public ArrayList<AppItemInfo> getBackupList(String saveFileName) {
        //BACKUP_PATH = "appmaster/backup/" , other app advice "appmaster/coolbrowserbackup"
        return AppBackupRestoreManager.getInstance(mContext).getBackupList(saveFileName);
    }

    //    int FAIL_TYPE_NONE = -1; mean ok , other is not ok
    //    int FAIL_TYPE_FULL = 0;
    //    int FAIL_TYPE_SDCARD_UNAVAILABLE = 1;
    //    int FAIL_TYPE_SOURCE_NOT_FOUND = 2;
    //    int FAIL_TYPE_OTHER = 3;
    //    int FAIL_TYPE_CANCELED = 4;
    @Override
    public int backupApp(AppItemInfo info, String saveFileName) {
        return AppBackupRestoreManager.getInstance(mContext).
                tryBackupApp(info, false, saveFileName);
    }

//    @Override
//    public boolean backupApps(List<String> backupList, String saveFileName) {
//        return false;
//    }

    @Override
    public ArrayList<AppItemInfo> getRestoreList(String saveFileName) {
        return AppBackupRestoreManager.getInstance(mContext).getRestoreList(saveFileName);
    }

    @Override
    public boolean deleteRestoreApp(AppItemInfo info) {
        AppBackupRestoreManager.getInstance(mContext).deleteApp(info);
        return false;
    }

    @Override
    public void restoreApp(AppItemInfo info) {
        AppBackupRestoreManager.getInstance(mContext).restoreApp(mContext, info);
    }

}
