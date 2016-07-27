
package com.zlf.appmaster.utils;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class StorageUtil {
    // private final static String TAG = "StorageUtil";
    private final static int    minSpaceMB = 10;

    public static boolean createThemeDir(String file) {
        File f = new File(file);

        if (!f.isDirectory()) {
            f = f.getParentFile();
        }

        if (!f.exists()) {
            return f.mkdirs();
        }
        return true;
    }

    public static void deleteDir(File dir)
    {
        if (!dir.exists()) {
            return;
        }

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();

            if (files != null) {
                for (File f : files) {
                    deleteDir(f);
                }
            }
            dir.delete();
        }
        else {
            dir.delete();
        }
    }

    public static void mvDir(String src, String dst) {
        File file_src = new File(src);
        File file_dst = new File(dst);

        if (file_dst.exists()) {
            deleteDir(file_dst);
        }

        if (file_src.exists()) {
            file_src.renameTo(file_dst);
        }
    }

    public static int FolderCopy(String fromFile, String toFile)
    {
        int ret = 0;
        File[] currentFiles = null;
        File root = new File(fromFile);

        if (!root.exists()) {
            ret = -1;
        }

        if (ret == 0) {
            currentFiles = root.listFiles();

            File targetDir = new File(toFile);
            if (!targetDir.exists()) {
                if (!targetDir.mkdirs()) {
                    ret = -1;
                }
            }
        }

        if (ret == 0 && currentFiles != null) {
            for (int i = 0; i < currentFiles.length; i++) {
                if (currentFiles[i].isDirectory()) {

                    int _ret = FolderCopy(currentFiles[i].getPath() + File.separator,
                            toFile + currentFiles[i].getName()
                                    + File.separator);

                    if (_ret != 0) {
                        break;
                    }

                } else {
                    int _ret = FileCopy(currentFiles[i].getPath(),
                            toFile + currentFiles[i].getName());
                    if (_ret != 0) {
                        break;
                    }
                }
            }
        }
        return ret;
    }

    public static int FileCopy(String fromFile, String toFile)
    {

        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024 * 8];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return 0;

        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public static boolean isPathExists(String path) {
        if (path != null && !"".equals(path)) {
            File file = new File(path);
            if (file.exists())
                return true;
        }
        return false;
    }

    public static String getDiskRoot() {

        boolean has_sdcard = !Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_REMOVED);
        
        if (has_sdcard){
            if(Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)){
                return Environment.getExternalStorageDirectory().getAbsolutePath();
            }
        }

        return "";
    }

    public static String getDownloadTempDir() {
        String tmp = getSdCardFile() + "/BaiduLauncher/temp";

        File file = new File(tmp);
        if (!file.exists()) {
            file.mkdirs();
        }

        return tmp;
    }

    public static String getDownloadBusinessDir(String aFilename) {
        // String tmp = file;getSdCardFile() + "/BaiduLauncher/app";
        File file = new File(aFilename);
        if (!file.exists()) {
            file.mkdirs();
        }

        return aFilename;
    }

    public static String getShareTempFileDir() {
        String tmp = getSdCardFile() + "/BaiduLauncher/.shareHome/";

        File file = new File(tmp);
        if (!file.exists()) {
            file.mkdirs();
        }

        return tmp;
    }
    
    public static String getBusinessApkFileDir(String aName, long aStrategyId) {
        return getDownloadBusinessDir(getSdCardFile() + "/BaiduLauncher/app/" + aStrategyId) + "/"
                + aName + ".apk";
    }

    public static String getBusinessApkPath(long aStrategyId) {
        return getDownloadBusinessDir(getSdCardFile() + "/BaiduLauncher/app/" + aStrategyId) + "/";
    }


    public static boolean isExternalSpaceInsufficient(long requireSize) {
        try {
            String path = getDiskRoot();
            if (path.equals("")) {
                return false;
            }
            StatFs statFs = new StatFs(path);
            long blocSize = statFs.getBlockSize();
            long availaBlock = statFs.getAvailableBlocks();
            long availableSpace = availaBlock * blocSize / (1024 * 1024);
            if(availableSpace > minSpaceMB + requireSize/(1024*1024)){
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean IsSdCardMounted() {
        try {
            boolean has_sdcard =  Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED);
            if(has_sdcard){
                if(getDiskRoot().equals("") || getDiskRoot() == ""){ //can not get sdcard root path
                    has_sdcard = false;
                }
            }
            return has_sdcard;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static String getVersionUpdateFileDir() {
    	String sdc = getSdCardFile();
    	if(sdc == null) {
    		return null;
    	} else {
    		return getDownloadBusinessDir(sdc  + "/BaiduLauncher/version/");
    	}
    }

    public static String getSdCardFile() {
        if (IsSdCardMounted()) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }
}
