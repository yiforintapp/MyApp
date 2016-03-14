
package com.leo.appmaster.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.engine.AppLoadEngine.AppChangeListener;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.BackupEvent;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.ThirdAppManager;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;

public class AppBackupRestoreManager implements AppChangeListener {
    public static final String MESSAGE_DELETE_APP = "message_delete_app";
    public static final String MESSAGE_ADD_APP = "message_add_app";

    public static final int FAIL_TYPE_NONE = -1;
    public static final int FAIL_TYPE_FULL = 0;
    public static final int FAIL_TYPE_SDCARD_UNAVAILABLE = 1;
    public static final int FAIL_TYPE_SOURCE_NOT_FOUND = 2;
    public static final int FAIL_TYPE_OTHER = 3;
    public static final int FAIL_TYPE_CANCELED = 4;

    public static final String BACKUP_PATH = "appmaster/backup/";
    private static final String PATH_ASSETMANAGER = "android.content.res.AssetManager";
    private static final String METHOD_ADD_ASSET = "addAssetPath";
    private static final String FILE_SCHEME = "file://";
    private static final String DATA_TYPE = "application/vnd.android.package-archive";

    private static final long sKB = 1024;
    private static final long sMB = sKB * sKB;
    private static final long sGB = sMB * sKB;

    private static final String sUnitB = "B";
    private static final String sUnitKB = "KB";
    private static final String sUnitMB = "MB";
    private static final String sUnitGB = "GB";
    private Context mContext;

    private static final DecimalFormat sFormat = new DecimalFormat("#.0");


    public interface AppBackupDataListener {
        public void onDataReady();

        public void onDataUpdate();

        public void onBackupProcessChanged(int doneNum, int totalNum,
                                           String currentApp);

        public void onBackupFinish(boolean success, int successNum,
                                   int totalNum, String message);

        public void onApkDeleted(boolean success);
    }

    private class SDCardReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                onSDCardChange(true);
            } else {
                onSDCardChange(false);
            }
        }
    }

    private SDCardReceiver mSDReceiver;

    // private AppBackupDataListener mBackupListener;
    private ArrayList<AppBackupDataListener> mBackupListeners;

    private ArrayList<AppItemInfo> mSavedList;
    private ArrayList<AppItemInfo> mBackupList;
    private ArrayList<AppItemInfo> mDeleteList;

    private boolean mDataReady = false;

    private boolean mBackupCanceled = false;

    private PackageManager mPackageManager;

    private static AppBackupRestoreManager mInstance;

    public static synchronized AppBackupRestoreManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppBackupRestoreManager(context.getApplicationContext());
        }

        return mInstance;
    }

    private AppBackupRestoreManager(Context context) {
        mContext = context.getApplicationContext();
        mPackageManager = mContext.getPackageManager();
        mBackupListeners = new ArrayList<AppBackupRestoreManager.AppBackupDataListener>();
        mSavedList = new ArrayList<AppItemInfo>();
        mBackupList = new ArrayList<AppItemInfo>();
        mDeleteList = new ArrayList<AppItemInfo>();
        AppLoadEngine.getInstance(context).registerAppChangeListener(this);


        mSDReceiver = new SDCardReceiver();
        IntentFilter intentFilter = new IntentFilter(
                Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.setPriority(1000);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addDataScheme("file");
        context.registerReceiver(mSDReceiver, intentFilter);
    }

    public void registerBackupListener(AppBackupDataListener listener) {
        if (!mBackupListeners.contains(listener)) {
            mBackupListeners.add(listener);
        }
    }

    public void unregisterBackupListener(AppBackupDataListener listener) {
        mBackupListeners.remove(listener);
    }

    public void prepareDate() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
//                getBackupList(saveFileName);
                ((ThirdAppManager) MgrContext.
                        getManager(MgrContext.MGR_THIRD_APP)).
                        getBackupList(AppBackupRestoreManager.BACKUP_PATH);
                for (AppBackupDataListener listener : mBackupListeners) {
                    listener.onDataReady();
                }
            }
        });
    }

    public void prepareDate_restore() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                for (AppBackupDataListener listener : mBackupListeners) {
                    listener.onDataReady();
                }
            }
        });
    }

    public void prepareDate_delete() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                getDeleteList();
                for (AppBackupDataListener listener : mBackupListeners) {
                    listener.onDataReady();
                }
            }
        });
    }

//    public void backupApp(final AppItemInfo app) {
//        mBackupCanceled = false;
//        String backupPath = getBackupPath(BACKUP_PATH);
//        final int totalNum = 1;
//        if (backupPath == null) {
//            for (AppBackupDataListener listener : mBackupListeners) {
//                listener.onBackupFinish(false, 0, totalNum,
//                        getFailMessage(FAIL_TYPE_SDCARD_UNAVAILABLE));
//            }
//        } else {
//            ThreadManager.executeOnAsyncThread(new Runnable() {
//                @Override
//                public void run() {
//                    int failType = FAIL_TYPE_NONE;
//                    boolean success = false;
//                    failType = tryBackupApp(app, true);
//                    if (failType == FAIL_TYPE_NONE) {
//                        success = true;
//                    }
//                    for (AppBackupDataListener listener : mBackupListeners) {
//                        listener.onBackupFinish(success, 1, 1,
//                                getFailMessage(failType));
//                    }
//                }
//            });
//        }
//    }

    public void backupApps(final ArrayList<AppItemInfo> apps) {
        mBackupCanceled = false;
        final String backupPath = getBackupPath(BACKUP_PATH);
        final int totalNum = apps.size();
        if (backupPath == null) {
            for (AppBackupDataListener listener : mBackupListeners) {
                listener.onBackupFinish(false, 0, totalNum,
                        getFailMessage(FAIL_TYPE_SDCARD_UNAVAILABLE));
            }
        } else {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    int doneNum = 0;
                    int successNum = 0;
                    int failType = FAIL_TYPE_NONE;
                    boolean success = true;
                    for (AppItemInfo app : apps) {
                        if (mBackupCanceled) {
                            failType = FAIL_TYPE_CANCELED;
                            success = false;
                            break;
                        }
                        for (AppBackupDataListener listener : mBackupListeners) {
                            listener.onBackupProcessChanged(doneNum, totalNum,
                                    app.label);
                        }
                        doneNum++;
                        failType = ((ThirdAppManager) MgrContext.getManager(MgrContext.MGR_THIRD_APP)).
                                backupApp(app, BACKUP_PATH);

//                        failType = tryBackupApp(app, false);
                        if (failType == FAIL_TYPE_NONE) {
                            successNum++;
//                            LeoLog.d("testfuckbackup", app.packageName);
                            SDKWrapper.addEvent(mContext, SDKWrapper.P1, "backup", "backup_" + app.packageName);
                        } else if (failType == FAIL_TYPE_SDCARD_UNAVAILABLE
                                || failType == FAIL_TYPE_FULL) {
                            success = false;
                            break;
                        }
                    }
                    if (successNum == 0) {
                        success = false;
                    }

                    for (AppBackupDataListener listener : mBackupListeners) {
                        if (doneNum == totalNum) {
                            listener.onBackupProcessChanged(doneNum, totalNum,
                                    null);
                        }
                        listener.onBackupFinish(success, successNum, totalNum,
                                getFailMessage(failType));
                    }
                }
            });
        }
    }

    public void cancelBackup() {
        mBackupCanceled = true;
    }

    public void deleteApp(final AppItemInfo app) {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                File apkFile = new File(app.sourceDir);
                boolean success = false;
                if (apkFile.exists()) {
                    success = apkFile.delete();
                } else {
                    success = true;
                }
                if (success) {
                    mSavedList.remove(app);
                    String pName = app.packageName;
                    for (AppItemInfo a : mBackupList) {
                        if (pName.equals(a.packageName)) {
                            a.isBackuped = false;
                            break;
                        }
                    }
                }

                for (AppBackupDataListener listener : mBackupListeners) {
                    listener.onApkDeleted(success);
                }
            }
        });
    }
    
    private void installApk(Context context, String filepath){
        try {
            LeoLog.d("stone_install", "download done, installing ....");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(FILE_SCHEME + filepath),
                    DATA_TYPE);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restoreApp(Context context, AppItemInfo app) {
        LockManager manager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        manager.filterSelfOneMinites();
        
        // AM-2950: fix install fail issue
        installApk(context, app.sourceDir);
        
        SDKWrapper.addEvent(context, SDKWrapper.P1, "backup", "recover_" + app.packageName);
    }

    public void checkDataUpdate() {
        if (mDataReady) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<AppItemInfo> deleteSavedList = new ArrayList<AppItemInfo>();
                    for (AppItemInfo app : mSavedList) {
                        File apkFile = new File(app.sourceDir);
                        if (!apkFile.isFile() || !apkFile.exists()) {
                            deleteSavedList.add(app);
                            for (AppItemInfo a : mBackupList) {
                                if (app.packageName.equals(a.packageName)) {
                                    a.isBackuped = false;
                                }
                            }
                        }
                    }
                    if (deleteSavedList.size() > 0) {
                        mSavedList.removeAll(deleteSavedList);

                        for (AppBackupDataListener listener : mBackupListeners) {
                            listener.onDataUpdate();
                        }
                    }

                }
            });
        }
    }

    public String getInstalledAppSize() {
        int installedSize = mDeleteList.size();
        Resources res = AppMasterApplication.getInstance().getResources();
        String tips = String.format(res.getString(R.string.installed_app_size),
                installedSize);
        return tips;
    }

    public String getApkSize(AppItemInfo app) {
        // String s = AppMasterApplication.getInstance().getString(
        // R.string.apk_size);
        try {
            File file = new File(app.sourceDir);
            if (file.isFile() && file.exists()) {
                long size = file.length();
                return convertToSizeString(size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }

    public String getAvaiableSizeString() {
        String tips = AppMasterApplication.getInstance().getString(
                R.string.storage_size);
        if (isSDReady()) {
            return String.format(tips, convertToSizeString(getAvaiableSize()));
        }
        return String.format(tips, AppMasterApplication.getInstance()
                .getString(R.string.unavailable));
    }

    public long getUsedSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        long allBlocks = stat.getBlockCount();
        return (allBlocks - availableBlocks) * blockSize;
    }

    private long getAvaiableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public String convertToSizeString(long size) {
        String sSize = null;
        if (size > sGB) {
            sSize = sFormat.format((float) size / sGB) + sUnitGB;
        } else if (size > sMB) {
            sSize = sFormat.format((float) size / sMB) + sUnitMB;
        } else if (size > sKB) {
            sSize = sFormat.format((float) size / sKB) + sUnitKB;
        } else {
            sSize = size + sUnitB;
        }
        return sSize;
    }

    public int tryBackupApp(AppItemInfo app, boolean single, String saveFileName) {
        File apkFile = new File(app.sourceDir);
        if (apkFile.exists() == false) {
            return FAIL_TYPE_SOURCE_NOT_FOUND;
        }
        String dest = getBackupPath(saveFileName);
        if (dest == null) {
            return FAIL_TYPE_SDCARD_UNAVAILABLE;
        }
        long totalSize = apkFile.length();
        if (totalSize > getAvaiableSize()) {
            return FAIL_TYPE_FULL;
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(apkFile);
        } catch (FileNotFoundException e) {
            return FAIL_TYPE_SOURCE_NOT_FOUND;
        }
        String pName = app.packageName;
        // do file copy operation
        byte[] c = new byte[1024 * 5];
        int slen;
        long writeSize = 0;
        FileOutputStream out = null;
        long lastUpdateTime = System.currentTimeMillis();
        try {
            dest += pName + ".apk";
            out = new FileOutputStream(dest);
            while ((slen = in.read(c, 0, c.length)) != -1) {
                if (mBackupCanceled) {
                    break;
                }
                out.write(c, 0, slen);
                writeSize += slen;
                if (single) {
                    int currentPercent = (int) ((((float) writeSize) / totalSize) * 100);
                    if (currentPercent > 100) {
                        currentPercent = 100;
                    }
                    long current = System.currentTimeMillis();
                    if (current - lastUpdateTime > 200) {
                        lastUpdateTime = current;
                        for (AppBackupDataListener listener : mBackupListeners) {
                            listener.onBackupProcessChanged(currentPercent, 1, app.label);
                        }
                    }
                }
            }
            SDKWrapper.addEvent(AppMasterApplication.getInstance(), SDKWrapper.P1, "backup", app.packageName);
        } catch (IOException e) {
            return FAIL_TYPE_OTHER;
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    return FAIL_TYPE_OTHER;
                }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    return FAIL_TYPE_OTHER;
                }
            }
            if (mBackupCanceled) {
                File f = new File(dest);
                if (f.exists()) {
                    f.delete();
                }
                return FAIL_TYPE_CANCELED;
            }
        }

        File f = new File(dest);
        long backupTime = f.lastModified();
        AppItemInfo newApp = null;
        boolean add = true;
        for (AppItemInfo appInfo : mSavedList) {
            if (pName.equals(appInfo.packageName)) {
                newApp = appInfo;
                add = false;
                break;
            }
        }
        if (newApp == null) {
            newApp = new AppItemInfo();
        }
        newApp.label = app.label;
        newApp.icon = app.icon;
        newApp.packageName = app.packageName;
        newApp.versionCode = app.versionCode;
        newApp.versionName = app.versionName;
        newApp.backupTime = backupTime;
        newApp.sourceDir = dest;
        app.isBackuped = true;
        app.isChecked = false;
        if (add) {
            mSavedList.add(newApp);
        }
        return FAIL_TYPE_NONE;
    }

    public synchronized ArrayList<AppItemInfo> getDeleteList() {
        mDeleteList.clear();
        List<String> mDropList = getDropList();
        mDeleteList = ((ThirdAppManager) MgrContext.getManager(MgrContext.MGR_THIRD_APP)).
                getDeleteList(mDropList);
//        mDeleteList = AppLoadEngine.getInstance(mContext).getDeleteableApps();
        mDataReady = true;
        return mDeleteList;
    }

    public List<String> getDropList() {
        List<String> droplist = new ArrayList<String>();
        String startwith = "s:com.leo.theme";
        droplist.add(startwith);
        String contain = "a:" + AppMasterApplication.getInstance().getPackageName();
        droplist.add(contain);
        return droplist;
    }

    public synchronized ArrayList<AppItemInfo> getBackupList(String saveFileName) {
//        mBackupList.clear();
        if (mBackupList.isEmpty()) {
            ((ThirdAppManager) MgrContext.getManager(MgrContext.MGR_THIRD_APP)).
                    getRestoreList(saveFileName);
//            getRestoreList(saveFileName);
            ArrayList<AppItemInfo> allApps = AppLoadEngine.getInstance(null)
                    .getAllPkgInfo();
            for (AppItemInfo app : allApps) {
                /** V2.0 on backup page . show the systemApp **/
                // if (app.systemApp) {
                // continue;
                // }
                String pName = app.packageName;
                int versionCode = app.versionCode;
                for (AppItemInfo a : mSavedList) { // check if already
                    // backuped
                    if (pName.equals(a.packageName)
                            && versionCode == a.versionCode) {
                        app.isBackuped = true;
                        break;
                    }
                }
                mBackupList.add(app);
            }

        }
        // AM-872
        try {
            Collections.sort(mBackupList, new Comparator<AppItemInfo>() {
                @Override
                public int compare(AppItemInfo lhs, AppItemInfo rhs) {
                    if (lhs.isBackuped && !rhs.isBackuped) {
                        return 1;
                    }
                    if (!lhs.isBackuped && rhs.isBackuped) {
                        return -1;
                    }
                    return 0;
                }
            });
        } catch (Exception e) {

        }
        mDataReady = true;
        return mBackupList;
    }

    public synchronized ArrayList<AppItemInfo> getRestoreList(String saveFileName) {

        try {
            if (!mSavedList.isEmpty()) {
                return mSavedList;
            }
            String path = getBackupPath(saveFileName);
            if (path != null) {
                File backupDir = new File(path);
                File[] fs = backupDir.listFiles();
                for (File f : fs) {
                    String fPath = f.getAbsolutePath();
                    if (f.isFile() && fPath.endsWith(".apk")) {
                        PackageInfo pInfo = mPackageManager
                                .getPackageArchiveInfo(fPath, 0);
                        if (pInfo != null) {
                            long backupTime = f.lastModified();
                            AppItemInfo app = new AppItemInfo();
                            Resources res = getResources(fPath);
                            ApplicationInfo appInfo = pInfo.applicationInfo;
                            String label = null;
                            Drawable icon = null;
                            if (res != null) {
                                try {
                                    label = res.getString(appInfo.labelRes);
                                    Drawable d = res.getDrawable(appInfo.icon);
                                    if (d != null) {
                                        icon = AppUtil.getScaledAppIcon((BitmapDrawable) d);
                                    }
                                } catch (Exception e) {

                                }
                            }
                            if (label == null) {
                                label = mPackageManager.getApplicationLabel(
                                        appInfo).toString();
                            }
                            if (icon == null) {
//                                icon = mPackageManager
//                                        .getApplicationIcon(appInfo);
                                icon = AppUtil.loadAppIconDensity(appInfo.packageName);
                            }
                            app.label = label;
                            app.icon = icon;
                            app.sourceDir = fPath;
                            app.packageName = appInfo.packageName;
                            app.versionCode = pInfo.versionCode;
                            app.versionName = pInfo.versionName;
                            app.backupTime = backupTime;
                            mSavedList.add(app);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LeoLog.e("Exception", e.getMessage());
        }

        return mSavedList;
    }

    private Resources getResources(String apkPath) {
        try {
            Class assetMagCls = Class.forName(PATH_ASSETMANAGER);
            Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
            Object assetMag = assetMagCt.newInstance((Object[]) null);
            Class[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
                    METHOD_ADD_ASSET, typeArgs);
            Object[] valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
            Resources res = AppMasterApplication.getInstance().getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor resCt = Resources.class.getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            res = (Resources) resCt.newInstance(valueArgs);
            return res;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isSDReady() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    public String getBackupPath(String saveFileName) {
        if (isSDReady()) {
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            path += saveFileName;
//            path += BACKUP_PATH;
            File backupDir = new File(path);
            if (!backupDir.exists()) {
                boolean success = backupDir.mkdirs();
                if (!success) {
                    return null;
                }
            }
            return path;
        }
        return null;
    }

    public String getDisplayPath() {
        return getFailMessage(FAIL_TYPE_NONE);
    }

    private String getFailMessage(int failType) {
        switch (failType) {
            case FAIL_TYPE_CANCELED:
                return AppMasterApplication.getInstance().getString(
                        R.string.bakup_fail_cancel);
            case FAIL_TYPE_FULL:
                return AppMasterApplication.getInstance().getString(
                        R.string.bakup_fail_full);
            case FAIL_TYPE_SDCARD_UNAVAILABLE:
                return AppMasterApplication.getInstance().getString(
                        R.string.bakup_fail_unavailable);
        }
        return "sdcard/" + BACKUP_PATH;
    }

    public void onDestory(Context context) {
        mBackupListeners.clear();
        mBackupListeners = null;
        mSavedList.clear();
        mBackupList.clear();
        AppLoadEngine.getInstance(null).unregisterAppChangeListener(this);
        context.unregisterReceiver(mSDReceiver);
    }

    public void resetList() {
        mSavedList.clear();
        mBackupList.clear();
        mDataReady = false;
        ((ThirdAppManager) MgrContext.
                getManager(MgrContext.MGR_THIRD_APP)).
                getBackupList(AppBackupRestoreManager.BACKUP_PATH);
//        getBackupList(saveFileName);
    }

    @Override
    public void onAppChanged(ArrayList<AppItemInfo> changes, int type) {
        if (type == AppChangeListener.TYPE_ADD
                || type == AppChangeListener.TYPE_AVAILABLE) {
            mBackupList.addAll(changes);
            //add some app , event bus to HomeFragment to refresh the data
            LeoEventBus.getDefaultBus().post(
                    new BackupEvent(MESSAGE_ADD_APP));
        } else if (type == AppChangeListener.TYPE_REMOVE
                || type == AppChangeListener.TYPE_UNAVAILABLE) {
            mBackupList.removeAll(changes);
            //delete some app , event bus to HomeFragment to refresh the data
            LeoEventBus.getDefaultBus().post(
                    new BackupEvent(MESSAGE_DELETE_APP));
        } else if (type == AppChangeListener.TYPE_UPDATE) {
            for (AppItemInfo app : changes) {
                if (app.isBackuped) {
                    String pkg = app.packageName;
                    int vCode = app.versionCode;
                    for (AppItemInfo a : mSavedList) {
                        if (pkg.equals(a.packageName) && vCode != a.versionCode) {
                            app.isBackuped = false;
                            break;
                        }
                    }
                }
            }
        }
        for (AppBackupDataListener listener : mBackupListeners) {
            listener.onDataUpdate();
        }
    }

    private void onSDCardChange(boolean mounted) {
        String backupPath = getBackupPath(BACKUP_PATH);
        if (backupPath == null) {
            mSavedList.clear();
            for (AppItemInfo app : mBackupList) {
                app.isBackuped = false;
            }
            for (AppBackupDataListener listener : mBackupListeners) {
                listener.onDataUpdate();
            }
        } else {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    mBackupList.clear();
                    mSavedList.clear();
                    mDataReady = false;
                    ((ThirdAppManager) MgrContext.
                            getManager(MgrContext.MGR_THIRD_APP)).
                            getBackupList(AppBackupRestoreManager.BACKUP_PATH);
//                    getBackupList();
                    for (AppBackupDataListener listener : mBackupListeners) {
                        listener.onDataUpdate();
                    }
                }
            });
        }
    }

}
