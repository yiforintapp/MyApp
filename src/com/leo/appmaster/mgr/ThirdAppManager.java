package com.leo.appmaster.mgr;

import com.leo.appmaster.model.AppItemInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用管理
 * Created by Jasper on 2015/9/28.
 */
public abstract class ThirdAppManager extends Manager {

    public static interface AppManagerListener {
        /**
         * called when backuping(standby)
         */
        public void onBackUping();

        /**
         * called when backup cancel
         */
        public void onBackUpCancel();

        /**
         * called when deleteing(standby)
         */
        public void onDeleteing();
    }

    @Override
    public String description() {
        return MgrContext.MGR_THIRD_APP;
    }

    /**
     * 获取卸载应用列表(standby)
     *
     * @param dropList
     */
    public abstract ArrayList<AppItemInfo> getDeleteList(List<String> dropList);

    /**
     * 删除应用
     *
     * @param packName
     */
    public abstract void uninstallApp(String packName);

    /**
     * 获取可备份应用(standby)
     *
     * @param saveFileName
     */
    public abstract ArrayList<AppItemInfo> getBackupList(String saveFileName);

    /**
     * 备份
     *
     * @param info
     * @param saveFileName
     */
    public abstract int backupApp(AppItemInfo info,String saveFileName);

//    /**
//     * 备份
//     *
//     * @param backupList
//     * @param saveFileName
//     */
//    public abstract boolean backupApps(List<String> backupList, String saveFileName);

    /**
     * 获取可恢复应用
     *
     * @param saveFileName
     */
    public abstract ArrayList<AppItemInfo> getRestoreList(String saveFileName);

    /**
     * 删除恢复文件
     *
     * @param info
     */
    public abstract boolean deleteRestoreApp(AppItemInfo info);

    /**
     * 恢复应用
     *
     * @param info
     */
    public abstract void restoreApp(AppItemInfo info);
}
