
package com.leo.appmaster.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;

import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppDetailInfo;

public class AppBackupRestoreManager {

    private static final String BACKUP_PATH = "leo/appmaster/.backup/";
    private static final String INSTALL_PACKAGE = "com.android.packageinstaller";
    
    private static final long sKB =  1024;
    private static final long sMB =  sKB * sKB;
    private static final long sGB =  sMB * sKB;
    
    private static final String sUnitB = "B";
    private static final String sUnitKB = "KB";
    private static final String sUnitMB = "MB";
    private static final String sUnitGB = "GB";
    
    private static final DecimalFormat sFormat = new DecimalFormat("#.0");
    
    public interface AppBackupDataListener {
        public void onDataReady();
        public void onDataUpdate();
        public void onBackupProcessChanged(int doneNum, int totalNum);
        public void onBackupFinish(boolean success, int successNum, int totalNum, String message);
        public void onApkDeleted(boolean success);
    }

    private  ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    
    private AppBackupDataListener mBackupListener;
    
    private ArrayList<AppDetailInfo> mSavedList;
    private ArrayList<AppDetailInfo> mBackupList;
    
    private boolean mDataReady = false;
    
    private boolean mBackupCanceled = false;
    
    private PackageManager mPackageManager;

    public AppBackupRestoreManager(Context context, AppBackupDataListener listener) {
        mPackageManager = context.getPackageManager();
        mBackupListener = listener;
        mSavedList = new ArrayList<AppDetailInfo>();
        mBackupList = new ArrayList<AppDetailInfo>();
    }
    
    public void prepareDate() {
        mExecutorService.execute(new Runnable() {      
            @Override
            public void run() {
                getBackupList();
                mBackupListener.onDataReady();
            }
        });
    }

    public void backupApps(final ArrayList<AppDetailInfo> apps) {
        mBackupCanceled = false;
        String backupPath = getBackupPath();
        final int totalNum = apps.size();
        if(backupPath == null) {
            mBackupListener.onBackupFinish(false, 0, totalNum, "No sd crad available");
        } else {
            mExecutorService.execute(new Runnable() {              
                @Override
                public void run() {
                    int doneNum = 0;
                    int successNum = 0;
                    for(AppDetailInfo app : apps) {
                        if(mBackupCanceled) {
                            break;
                        }
                        doneNum ++;
                        if(tryBackupApp(app)) {
                            successNum++;
                            mBackupListener.onBackupProcessChanged(doneNum, totalNum);
                        }
                    }
                    mBackupListener.onBackupFinish(true, successNum, totalNum, null);
                }
            });
        }
    }

    public void cancelBackup() {
        mBackupCanceled = true;
    }
    
    public void deleteApp(final AppDetailInfo app) {
        mExecutorService.execute(new Runnable() {              
            @Override
            public void run() {
                File apkFile = new File(app.getSourceDir());
                boolean success = false;
                if(apkFile.exists()) {
                    success = apkFile.delete();
                } else {
                    success = true;
                }
                if(success) {
                    mSavedList.remove(app);
                    String pName = app.getPkg();
                    for(AppDetailInfo a : mBackupList) {
                        if(pName.equals(a.getPkg())) {
                            a.isBackuped = false;
                            break;
                        }
                    }
                }
                mBackupListener.onApkDeleted(success);
            }
        });
    }
    
    public void restoreApp(Context context, AppDetailInfo app) {
        Intent intent = new Intent();    
        intent.setDataAndType(Uri.fromFile(new File(app.getSourceDir())),  "application/vnd.android.package-archive");    
        try {
            // check android package installer
            mPackageManager.getPackageInfo(INSTALL_PACKAGE, 0);
            intent.setPackage(INSTALL_PACKAGE);
        } catch (NameNotFoundException e) {
        }
        context.startActivity(intent);    
    }
    
    public void checkDataUpdate() {
        if(mDataReady) {
            mExecutorService.execute(new Runnable() {           
                @Override
                public void run() {
                    ArrayList<AppDetailInfo> deleteSavedList = new ArrayList<AppDetailInfo>();
                    for(AppDetailInfo app : mSavedList) {
                        File apkFile = new File(app.getSourceDir());
                        if(!apkFile.isFile() || !apkFile.exists()) {
                            deleteSavedList.add(app);
                            for(AppDetailInfo a : mBackupList) {
                                if(app.getPkg().equals(a.getPkg())) {
                                    a.isBackuped = false;
                                }
                            }
                        }
                    }
                    if(deleteSavedList.size() > 0) {
                        mSavedList.removeAll(deleteSavedList);
                        mBackupListener.onDataUpdate();
                    }
                }
            });
        }
    }
    

    public String getBackupTips(Context context) {
        int installedSize = mBackupList.size();
        String avaiableSize = getAvaiableSize();
        String tips = "Installed: " + installedSize + "    Avaiable storage: " + avaiableSize;
        return tips;
    }
    
    public String getApkSize(AppDetailInfo app) {
        File file = new File(app.getSourceDir());
        if(file.isFile() && file.exists()) {
            long size = file.length();
            return convertToSizeString(size);
        }
        return "0";
    }
    
    private String getAvaiableSize() {
        if(isSDReady()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            long avaiableSize =  availableBlocks * blockSize;
            return convertToSizeString(avaiableSize);
        }
        return "Not avaiable";
    }
    
    private String convertToSizeString(long size) {
        String sSize = null;
        if(size > sGB) {
            sSize = sFormat.format((float)size / sGB ) + sUnitGB;
        } else if(size > sMB) {
            sSize = sFormat.format((float)size / sMB ) + sUnitMB;
        } else if(size > sKB) {
            sSize = sFormat.format((float)size / sKB ) + sUnitKB;
        } else {
            sSize = size + sUnitB;
        }
        return sSize;
    }

    private boolean tryBackupApp(AppDetailInfo app) {
        File apkFile = new File(app.getSourceDir());
        if (apkFile.exists() == false) {
            return false;
        }
        String dest = getBackupPath();
        if(dest == null) {
            return false;
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(apkFile);
        } catch (FileNotFoundException e) {
            return false;
        }
        // do file copy operation
        byte[] c = new byte[1024 * 5];
        int slen;
        FileOutputStream out = null;
        try {
            String pName = app.getPkg();
            dest += pName + ".apk";
            out = new FileOutputStream(dest);
            while ((slen = in.read(c, 0, c.length)) != -1) {
                if(mBackupCanceled) {
                    break;
                }
                out.write(c, 0, slen);
            }
        } catch (IOException e) {
            return false;
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    return false;
                }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    return false;
                }
            }
            if(mBackupCanceled) {
                File f = new File(dest);
                if(f.exists()) {
                    f.delete();
                }
                return false;
            }
        }
        
        AppDetailInfo newApp = new AppDetailInfo();
        newApp.setAppLabel(app.getAppLabel());
        newApp.setAppIcon(app.getAppIcon());
        newApp.setPkg(app.getPkg());
        newApp.setVersionCode(app.getVersionCode());
        newApp.setVersionName(app.getVersionName());
        newApp.setSourceDir(dest);
        app.isBackuped = true;
        app.isChecked = false;
        mSavedList.add(newApp);
        
        return true;
    }

    public synchronized ArrayList<AppDetailInfo> getBackupList() {
        if(mBackupList.isEmpty()) {
            getRestoreList();
            ArrayList<AppDetailInfo> allApps = AppLoadEngine.getInstance(null).getAllPkgInfo();
            for(AppDetailInfo app : allApps) {
                if(app.isSystemApp()) {
                    continue;
                }
                String pName = app.getPkg();
                int versionCode = app.getVersionCode();
                for(AppDetailInfo a : mSavedList) { // check if already backuped
                    if(pName.equals(a.getPkg()) && versionCode == a.getVersionCode()) {
                        app.isBackuped = true;
                        break;
                    }                
                }
                mBackupList.add(app);
            }
           
        }
        
        Collections.sort(mBackupList, new Comparator<AppDetailInfo>() {
            @Override
            public int compare(AppDetailInfo lhs, AppDetailInfo rhs) {
                if(lhs.isBackuped && !rhs.isBackuped) {
                    return 1;
                }
                if(!lhs.isBackuped && rhs.isBackuped) {
                    return -1;
                }
                return 0;
            }            
        });
        mDataReady = true;
        return mBackupList;
    }

    public synchronized ArrayList<AppDetailInfo> getRestoreList() {
        if (!mSavedList.isEmpty()) {
            return mSavedList;
        }
        String path = getBackupPath();
        if(path != null) {
            File backupDir = new File(path);
            File[] fs = backupDir.listFiles();
            for (File f : fs) {
                String fPath = f.getAbsolutePath();
                if (f.isFile() && fPath.endsWith(".apk")) {
                    PackageInfo pInfo = mPackageManager.getPackageArchiveInfo(fPath, 0);
                    if(pInfo != null) {
                        AppDetailInfo app = new AppDetailInfo();
                        ApplicationInfo appInfo = pInfo.applicationInfo;
                        app.setAppLabel(mPackageManager.getApplicationLabel(appInfo).toString());
                        app.setSourceDir(fPath);
                        app.setAppIcon(mPackageManager.getApplicationIcon(appInfo));
                        app.setPkg(appInfo.packageName);
                        app.setVersionCode(pInfo.versionCode);
                        app.setVersionName(pInfo.versionName);
                        mSavedList.add(app);
                    }
                }
            }
        }

        return mSavedList;
    }
    
    private boolean isSDReady() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private String getBackupPath() {
        if (isSDReady()) {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            path += BACKUP_PATH;
            File backupDir = new File(path);
            if(!backupDir.exists()) {
               boolean success =  backupDir.mkdirs();
               if(!success) {
                   return null;
               }
            }
            return path;
        }
        return null;
    }

}
