package com.leo.appmaster.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.Log;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.imagehide.PhotoAibum;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.mgr.impl.PrivacyDataManagerImpl;

public class FileOperationUtil {
    private static final String TAG = "FileOperationUtil";
    private static final String SYSTEM_PREFIX = "/system";

    public static final String SDCARD_DIR_NAME = ".DefaultGallery";
    public static final String OLD_SDCARD_DIR_NAME = "PravicyLock";
    //在隐藏图片时内存不足
    public static final String HIDE_PIC_NO_MEMERY = "4";
    //在隐藏图片时复制隐藏成功
    public static final String HIDE_PIC_COPY_SUCESS = "0";
    // 复制隐藏失败
    public static final String HIDE_PIC_COPY_RENAME_FAIL = "-2";
    // 复制隐藏时复制失败
    public static final String HIDE_PIC_COPY_FAIL = "-1";
    //非复制情况下，隐藏图片图片成功
    public static final String HIDE_PIC_SUCESS = "3";
    //需要隐藏的图片uri为空时
    public static final String HIDE_PIC_PATH_EMPTY = "2";

    //默认隐藏方式
    public static final int DEF_HIDE = -1;
    //重命名的方式隐藏
    public static final int RENAME_HIDE = 0;
    //复制的方式隐藏
    public static final int COPY_HIDE = 1;

    private static int mHideTpye = -1;

    public static int getHideTpye() {
        return mHideTpye;
    }

    public static void setHideTpye(int hideTpye) {
        FileOperationUtil.mHideTpye = hideTpye;
    }

    public static final String[] STORE_IMAGES = {
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            // dir name
    };

    public static Comparator<PhotoAibum> mFolderCamparator = new Comparator<PhotoAibum>() {

        public final int compare(PhotoAibum a, PhotoAibum b) {
            if (a.getLastmodified().before(b.getLastmodified()))
                return 1;
            if (a.getLastmodified().after(b.getLastmodified()))
                return -1;
            return 0;
        }
    };

    public static String getNameFromFilepath(String filepath) {
        if (filepath != null) {
            String filename;
            int pos = filepath.lastIndexOf('/');
            if (pos != -1) {
                filename = filepath.substring(pos + 1);
                return filename;
            }
        }
        return "";
    }

    public static String getNoExtNameFromHideFilepath(String filepath) {
        if (filepath != null) {
            String filename;
            int pos = filepath.lastIndexOf('/');
            if (pos > -1) {
                filename = filepath.substring(pos + 1);
                if (filename.startsWith(".")) {
                    filename = filename.substring(1);
//                    int index = filename.indexOf(".");
                    //从最后往前数两个 点
                    int index = filename.lastIndexOf(".");
                    if (index >= 0) {
                        filename = filename.substring(0, index);
                        index = filename.lastIndexOf(".");
                        if (index >= 0) {
                            filename = filename.substring(0, index);
                        }
                    }

                } else {
//                    int index = filename.indexOf(".");
                    //从最后往前数两个 点
                    int index = filename.lastIndexOf(".");
                    if (index >= 0) {
                        filename = filename.substring(0, index);
                        index = filename.lastIndexOf(".");
                        if (index >= 0) {
                            filename = filename.substring(0, index);
                        }
                    }
                }
                return filename;
            }
        }
        return "";
    }

    public static String getDirPathFromFilepath(String filepath) {
        if (filepath != null) {
            int pos = filepath.lastIndexOf('/');
            if (pos >= 0) {
                return filepath.substring(0, pos);
            }
        }
        return "";
    }

    public static String getSecondDirNameFromFilepath(String path) {
        if (path != null) {
            String dirName;
            int pos = path.lastIndexOf('/');
            if (pos != -1) {
                dirName = path.substring(0, pos);
                pos = dirName.lastIndexOf('/');
                if (pos != -1) {
                    dirName = path.substring(0, pos);
                    pos = dirName.lastIndexOf("/");
                    if (pos != -1) {
                        dirName = dirName.substring(pos + 1);
                        return dirName;
                    }
                }
            }
        }
        return "";
    }

    public static String makePath(String path1, String path2) {
        if (path1 != null && path2 != null) {
            if (path1.endsWith(File.separator)) {
                return path1 + path2;
            }
            return path1 + File.separator + path2;
        }
        return "";
    }

    public static String getSdDirectory() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static String getDirNameFromFilepath(String path) {
        if (path != null) {
            String dirName;
            int pos = path.lastIndexOf('/');
            if (pos >= 0) {
                dirName = path.substring(0, pos);
                pos = dirName.lastIndexOf('/');
                dirName = dirName.substring(pos + 1);
                return dirName;
            }
        }
        return "";
    }

    /**
     * rename a file
     *
     * @param filePath
     * @param newName
     * @return
     */
    public static boolean renameFile(String filePath, String newName) {
        if (filePath == null || newName == null) {
            LeoLog.e("RenameFile", "Rename: null parameter");
            return false;
        }

        File file = new File(filePath);
        String newPath = FileOperationUtil.makePath(
                FileOperationUtil.getDirPathFromFilepath(filePath), newName);
        LeoLog.e("RenameFile", "newPath=" + newPath);
        try {
            if (file.isFile()) {
                boolean ret = file.renameTo(new File(newPath));
                LeoLog.e("RenameFile", ret + " to rename file");
                return ret;
            } else {
                return false;
            }
        } catch (SecurityException e) {
            LeoLog.e("RenameFile", "Fail to rename file," + e.toString());
        }
        return false;
    }

    public static void deleteFileMediaEntry(String imagePath, Context context) {
        if (imagePath != null) {
            String params[] = new String[]{
                    imagePath
            };
            Uri uri = Files.getContentUri("external");
            int result = context.getContentResolver().delete(uri,
                    MediaColumns.DATA + " LIKE ?", params);
            LeoLog.e("deleteFileMedia", "result----:" + result);
        }
    }

    // 内存大小
    public static boolean isMemeryEnough(long fileSize, Context ctx, String sdPath, int size) {
        StatFs sf = new StatFs(sdPath);
        @SuppressWarnings("deprecation")
        long blockSize = sf.getBlockSize();
        @SuppressWarnings("deprecation")
        long nAvailableblock = sf.getAvailableBlocks();
        long availableblock = (blockSize * nAvailableblock) / (1024 * 1024);
        long temp = fileSize / (1024 * 1024) + size;
        return temp > availableblock ? false : true;
    }

    private static boolean jugdePath(String filePath, String sdcardPath) {
        if (filePath != null && sdcardPath != null) {
            String[] strings = sdcardPath.split("/");
            String[] pathStrings = filePath.split("/");
            if (strings == null || pathStrings == null) {
                return false;
            }
            for (int i = 0; i < strings.length; i++) {
                if (!strings[i].equals(pathStrings[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * cut a file to our special dir in the same sdcard
     *
     * @param filePath
     * @param newName
     * @return String[0]:返回加密后地址，String[1]:隐藏后返回值
     */
    public static synchronized String hideImageFile(Context ctx,
                                                    String filePath, String newName, long fileSize) {
        String str = FileOperationUtil.getDirPathFromFilepath(filePath);
        String fileName = FileOperationUtil.getNameFromFilepath(filePath);

        if (filePath == null || newName == null) {
            LeoLog.e("RenameFile", "Rename: null parameter");
            return null;
        }

        String[] paths = getSdCardPaths(ctx);
        int position = -1;
        if (paths == null) {
            return null;
        }

        for (int i = 0; i < paths.length; i++) {
            if (filePath.startsWith(paths[i]) && jugdePath(filePath, paths[i])) {
                position = i;
                break;
            }
        }

        String newPath;
        File file = new File(filePath);
        if (position == -1) {
            newPath = filePath + Constants.CRYPTO_SUFFIX;
        } else {
            String p = paths[position];
            newPath = FileOperationUtil.makePath(paths[position],
                    FileOperationUtil.getDirPathFromFilepath(filePath), newName);
        }

        try {
            if (file.isFile()) {
                int pos = newPath.lastIndexOf(File.separator);
                String newFileDir = null;
                if (pos >= 0) {
                    newFileDir = newPath.substring(0, pos);
                }
                File temp = new File(newFileDir);
                LeoLog.d("RenameFile", "fileDir = " + newFileDir);
                if (temp.exists()) {
                    LeoLog.d("RenameFile", temp + "    exists");
                } else {
                    LeoLog.d("RenameFile", temp + "  not   exists");
                    boolean mkRet = temp.mkdirs();
                    if (mkRet) {
                        LeoLog.e("RenameFile", "make dir " + temp
                                + "  successfully");
                    } else {
                        LeoLog.d("RenameFile", "make dir " + temp
                                + "  unsuccessfully");
                        newPath = newPath.replace(SDCARD_DIR_NAME + File.separator, "");
                    }
                }

                boolean ret = file.renameTo(new File(newPath));
                LeoLog.d("RenameFile", ret + " : rename file " + filePath
                        + " to " + newPath);
                // return ret ? newPath : null;
                if (!ret) {
                    boolean memeryFlag = isMemeryEnough(fileSize, ctx, paths[0], 10);
                    String returnValue = HIDE_PIC_NO_MEMERY;
//
                    if (memeryFlag) {
                        returnValue = hideImageFileCopy(ctx, filePath, newName);
                        LeoLog.d("testHidePic", "hide type:copy");
                    }

                    return returnValue;
                } else {
                    LeoLog.d("testHidePic", "hide type:rename");

                    //设置隐藏方式
                    setHideTpye(RENAME_HIDE);

                    return newPath;
                }

            } else {
                return null;
            }
        } catch (SecurityException e) {
            LeoLog.e("RenameFile", "Fail to rename file," + e.toString());
        }
        return null;
    }

//    private static void makeAuthorization(String newPath) {
//        File f = new File(newPath);
//        try {
//            String command = "chmod 664 " + f.getAbsolutePath();
//            LeoLog.d("testAddPath", "command = " + command);
//            Runtime runtime = Runtime.getRuntime();
//            Process proc = runtime.exec(command);
//            int status = proc.waitFor();
//            if (status == 0) {
//                //chmod succeed
//                LeoLog.d("testAddPath", "chmod succeed");
//            } else {
//                //chmod failed
//                LeoLog.d("testAddPath", "chmod failed");
//            }
//        } catch (Exception e) {
//            LeoLog.i("testAddPath", "chmod fail!!!!");
//            e.printStackTrace();
//        }
//    }

    public static synchronized String unhideImageFile(Context ctx,
                                                      String filePath, long fileSize) {
        if (filePath == null || (!filePath.endsWith(Constants.CRYPTO_SUFFIX) && !filePath.endsWith(Constants.CRYPTO_SUFFIX_OLD))) {
            LeoLog.e("RenameFile", "Rename: null parameter");
            return null;
        }
        String[] paths = getSdCardPaths(ctx);
        String replacedPath = filePath;
        if (filePath.endsWith(Constants.CRYPTO_SUFFIX_OLD)) {
            StringBuilder stringBuilder = new StringBuilder(filePath);
            replacedPath = stringBuilder.substring(0, stringBuilder.indexOf(Constants.CRYPTO_SUFFIX_OLD)) + Constants.CRYPTO_SUFFIX;
        }

        String newPath = null;
        boolean newHided = false;


        if (filePath.contains(SDCARD_DIR_NAME)) {
            newHided = true;
            newPath = replacedPath.replace(Constants.CRYPTO_SUFFIX, "").replace(
                    SDCARD_DIR_NAME + File.separator, "");
        } else if (filePath.contains(OLD_SDCARD_DIR_NAME)) {
            newHided = true;
            newPath = replacedPath.replace(Constants.CRYPTO_SUFFIX, "").replace(
                    OLD_SDCARD_DIR_NAME + File.separator, "");
        } else {
            newHided = false;
            newPath = replacedPath.replace(Constants.CRYPTO_SUFFIX, "");
        }


        String fileName = getNameFromFilepath(newPath);
        String fileDir = newPath.replace(fileName, "");
        if (fileName.startsWith(".")) {
            fileName = fileName.substring(1);
            newPath = fileDir + fileName;
        }

        File file = new File(filePath);
        if (file.isFile()) {
            String newFileDir = null;
            if (newHided) {
                try {
                    if (newPath.lastIndexOf(File.separator) >= 0) {
                        newFileDir = newPath.substring(0,
                                newPath.lastIndexOf(File.separator)).replace(
                                SDCARD_DIR_NAME + File.separator, "");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    if (newPath.lastIndexOf(File.separator) >= 0) {
                        newFileDir = newPath.substring(0,
                                newPath.lastIndexOf(File.separator));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            File temp = new File(newFileDir);
            if (temp.exists()) {
                LeoLog.d("unhideImageFile", temp + "    exists");
            } else {
                LeoLog.d("unhideImageFile", temp + "  not   exists");
                boolean mkRet = temp.mkdirs();
                if (mkRet) {
                    LeoLog.d("unhideImageFile", "make dir " + temp
                            + "  successfully");
                } else {
                    LeoLog.d("unhideImageFile", "make dir " + temp
                            + "  unsuccessfully");
                    return null;
                }
            }
            boolean ret = file.renameTo(new File(newPath));
            LeoLog.e("unhideImageFile", ret + " : rename file " + filePath
                    + " to " + newPath);
            if (!ret) {
                boolean memeryFlag = isMemeryEnough(fileSize, ctx, paths[0], 10);
                String returnValue = HIDE_PIC_NO_MEMERY;
                if (memeryFlag) {
                    returnValue = unHideImageFileCopy(ctx, filePath);
                }
                return String.valueOf(returnValue);
            } else {
                return newPath;
            }
        } else {
            return null;
        }
    }

    /**
     * @param sdcarPath
     * @param dirPathFromFilepath
     * @param newName
     * @return
     */
    private static String makePath(String sdcarPath,
                                   String dirPathFromFilepath, String newName) {

        if (!dirPathFromFilepath.startsWith(sdcarPath)) {
            return null;
        } else {
            dirPathFromFilepath = dirPathFromFilepath.replaceAll(SDCARD_DIR_NAME + File.separator,
                    "");

            String target = sdcarPath + File.separator + SDCARD_DIR_NAME
                    + dirPathFromFilepath.replace(sdcarPath, "");

            if (target.endsWith(File.separator)) {
                return target + newName;
            } else {
                return target + File.separator + newName;
            }
        }
    }

    /**
     * @param filePath
     * @return
     */
    public static boolean deleteFile(String filePath) {
        if (filePath == null) {
            LeoLog.e("DeleteFile", "Rename: null parameter");
            return false;
        }

        File file = new File(filePath);
        try {
            if (file.isFile()) {
                boolean ret = file.delete();
                LeoLog.e("DeleteFile", ret + " to rename file");
                return ret;
            } else {
                return false;
            }
        } catch (SecurityException e) {
            LeoLog.e("DeleteFile", "Fail to rename file," + e.toString());
        }
        return false;
    }

    public static Uri updateVedioMediaEntry(String vedioPath, boolean isHide, String MIME_TYPE, Context context) {
        String[] string = {vedioPath};
        ContentValues v = new ContentValues();
        v.put(MediaStore.Video.Media.MIME_TYPE, MIME_TYPE);

        ContentResolver c = context.getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        try {
            Cursor cursor = c.query(uri, null, MediaColumns.DATA + " LIKE ?", new String[]{vedioPath}, null);
            if (cursor.getCount() > 0) {
                LeoLog.d("testVedio", "update");
                if (isHide) {
                    cursor.moveToFirst();
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    LeoLog.d("testVedio", "id is:" + id);

                    int saveId = PreferenceTable.getInstance().getInt(PrefConst.KEY_NEW_ADD_VID, 0);

                    PrivacyDataManager manager = (PrivacyDataManager)
                            MgrContext.getManager(MgrContext.MGR_PRIVACY_DATA);
                    int hideId = manager.getNextToTargetId(id);
                    LeoLog.d("testVedio", "NextToTargetId is:" + hideId);
                    if (hideId == 0) {
                        if (id >= saveId) {
                            PreferenceTable.getInstance().putInt(PrefConst.KEY_NEW_ADD_VID, id);
                        }
                    } else {
                        PreferenceTable.getInstance().putInt(PrefConst.KEY_NEW_ADD_VID, hideId);
                    }
                }
                c.update(uri, v, MediaColumns.DATA + " LIKE ?", string);
            } else {
                LeoLog.d("testVedio", "insert");
                saveFileMediaEntry(vedioPath, context);
            }

            LeoLog.d("testVedio", "update done!");
        } catch (Exception e) {
            LeoLog.d("testVedio", "catch the fuck !");
        }

        return null;
    }

    public static Uri updateFileMediaEntry(String oldPath, String newPath, String MIME_TYPE, Context context) {
        String[] string = {oldPath};
        ContentValues v = new ContentValues();
        File f = new File(newPath);
        v.put(MediaStore.Video.Media.TITLE, f.getName());
        v.put(MediaStore.Video.Media.DISPLAY_NAME, f.getName());
        v.put(MediaStore.Video.Media.SIZE, f.length());
        v.put(MediaStore.Video.Media.DATA, newPath);
//        v.put(MediaColumns.MIME_TYPE, MIME_TYPE);

        ContentResolver c = context.getContentResolver();
        Uri uri = Files.getContentUri("external");
        try {
            int result = c.update(uri, v, MediaStore.Video.Media.DATA, string);
            LeoLog.d("testVedio", "result : " + result);
        } catch (Exception e) {
            LeoLog.d("testVedio", "catch the fuck !");
        }
        return null;
    }

    public static Uri saveFileMediaEntry(String imagePath, Context context) {
        ContentValues v = new ContentValues();
        File f = new File(imagePath);
        v.put(MediaColumns.TITLE, f.getName());
        v.put(MediaColumns.DISPLAY_NAME, f.getName());
        v.put(MediaColumns.SIZE, f.length());
        v.put(MediaColumns.DATA, imagePath);
        ContentResolver c = context.getContentResolver();
        Uri uri = Files.getContentUri("external");
        Uri result = null;
        String params[] = new String[]{
                imagePath
        };
        try {
            int rows = c.update(uri, v, Images.Media.DATA + " = ?", params);
            if (rows > 0) {
                LeoLog.d(TAG, "saveFileMediaEntry, update successful.");
                return null;
            }
        } catch (Exception e) {
        }
        try {
            result = c.insert(uri, v);
        } catch (Exception e) {
        }
        return result;
    }

    public static void deleteImageMediaEntry(String imagePath, Context context) {
        String params[] = new String[]{
                imagePath
        };
        Uri uri = Files.getContentUri("external");
        // context.getContentResolver().delete(
        // MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        // MediaStore.Images.Media.DATA + " LIKE ?", params);

        context.getContentResolver().delete(uri,
                MediaStore.Images.Media.DATA + " LIKE ?", params);
    }

    public static void deleteVideoMediaEntry(String videoPath, Context context) {
        String params[] = new String[]{
                videoPath
        };
        Uri uri = Files.getContentUri("external");
        context.getContentResolver().delete(uri,
                MediaStore.Files.FileColumns.DATA + " LIKE ?", params);
    }

    public static Uri saveVideoMediaEntry(String videoPath, Context context) {
        ContentValues v = new ContentValues();
        v.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        File f = new File(videoPath);
        File parent = f.getParentFile();
        String path = parent.toString().toLowerCase();
        String name = parent.getName().toLowerCase();
        v.put(MediaStore.Video.Media.TITLE, f.getName());
        v.put(MediaStore.Video.Media.DISPLAY_NAME, f.getName());
        v.put(MediaStore.Video.Media.BUCKET_ID, path.hashCode());
        v.put(MediaStore.Video.Media.BUCKET_DISPLAY_NAME, name);
        v.put(MediaStore.Video.Media.SIZE, f.length());

        v.put(MediaStore.Video.Media.DATA, videoPath);
        ContentResolver c = context.getContentResolver();

        return c.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, v);
    }

    public static Uri saveImageMediaEntry(String imagePath, Context context) {
        ContentValues v = new ContentValues();
        v.put(Images.Media.MIME_TYPE, "image/jpeg");

        File f = new File(imagePath);
        File parent = f.getParentFile();
        String path = parent.toString().toLowerCase();
        String name = parent.getName().toLowerCase();
        v.put(Images.Media.TITLE, f.getName());
        v.put(Images.Media.DISPLAY_NAME, f.getName());
        v.put(Images.Media.BUCKET_ID, path.hashCode());
        v.put(Images.Media.BUCKET_DISPLAY_NAME, name);
        v.put(Images.Media.SIZE, f.length());
        f = null;

        v.put(MediaStore.Images.Media.DATA, imagePath);
        ContentResolver c = context.getContentResolver();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri result = null;
        String params[] = new String[]{
                imagePath
        };

        try {
            int rows = c.update(uri, v, Images.Media.DATA + " = ?", params);
            if (rows > 0) {
                LeoLog.d(TAG, "saveFileMediaEntry, update successful.");
                return null;
            }
        } catch (Exception e) {
        }
        try {
            result = c.insert(uri, v);
        } catch (Exception e) {
        }

        return result;
    }

    /*
     * get image folder list
     */
    public static List<PhotoAibum> getPhotoAlbum(Context context, String mSuffix) {
        List<String> filterVideoTypes = getFilterVideoType();
        List<PhotoAibum> aibumList = new ArrayList<PhotoAibum>();
        int picNumFromDir;
        Cursor cursor = null;

        Map<String, PhotoAibum> countMap = new HashMap<String, PhotoAibum>();
        PhotoAibum pa = null;
        try {
            cursor = MediaStore.Images.Media.query(
                    context.getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, STORE_IMAGES,
                    null, MediaColumns.DATE_MODIFIED + " desc");
            LeoLog.d("getPhotoAlbum", "cursor size : " + cursor.getCount());
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString(1);
                    LeoLog.d("getPhotoAlbum", "path is : " + path);
                    String dir = cursor.getString(3);
                    String dir_path = getDirPathFromFilepath(path);


//                    LeoLog.d("testMainPic", "path is : " + path);
//                    long ADD_TIME = cursor.getLong(cursor
//                            .getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
//                    long MODYFY_TIME = cursor.getLong(cursor
//                            .getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED));
//                    LeoLog.d("testMainPic", "path is : " + path + "   ADD_TIME : " + ADD_TIME + "  MODYFY_TIME : " + MODYFY_TIME);

                    if (dir.contains("videoCache")) {
                        LeoLog.d(TAG, "Image Path：" + path);
                    }
                    if (path.startsWith(SYSTEM_PREFIX)) {
                        continue;
                    }
                    boolean isFilterVideoType = false;
                    for (String videoType : filterVideoTypes) {
                        isFilterVideoType = isFilterVideoType(path, videoType);
                    }
                    if (isFilterVideoType) {
                        continue;
                    }
                    if (!countMap.containsKey(dir_path)) {
                        pa = new PhotoAibum();
                        pa.setName(dir);
                        pa.setCount("1");
                        pa.setDirPath(dir_path);
                        pa.getBitList().add(new PhotoItem(path));
                        countMap.put(dir_path, pa);
                        LeoLog.d("testMainPic", "add dir_path is : " + dir_path);
                    } else {
                        if (mSuffix != null && mSuffix.equals(PrivacyDataManagerImpl.CHECK_APART)) {
                            picNumFromDir = pa.getBitList().size();
                            if (picNumFromDir < PrivacyDataManagerImpl.MAX_NUM) {
                                LeoLog.d("testGetAllPicFlie", "<MAXNUM picNumFromDir : " + picNumFromDir);
                                File f = new File(path);
                                if (f.exists()) {
                                    pa = countMap.get(dir_path);
                                    pa.setCount(String.valueOf(Integer.parseInt(pa.getCount()) + 1));
                                    LeoLog.d("testGetAllPicFlie", "pic_path--:" + path);
                                    pa.getBitList().add(new PhotoItem(path));
                                }
                            } else {
                                LeoLog.d("testGetAllPicFlie", ">MAXNUM picNumFromDir");
                                pa = countMap.get(dir_path);
                                pa.setCount(String.valueOf(Integer.parseInt(pa.getCount()) + 1));
                                pa.getBitList().add(new PhotoItem(path));
                            }
                        } else {
                            File f = new File(path);
                            if (f.exists()) {
                                pa = countMap.get(dir_path);
                                pa.setCount(String.valueOf(Integer.parseInt(pa.getCount()) + 1));
                                pa.getBitList().add(new PhotoItem(path));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


        //splashPath
        String splashPath = FileOperationUtil.getSplashPath();
        if (splashPath != null) {
            StringBuffer str = new StringBuffer(splashPath);
            String lastIndex = String.valueOf(str.charAt(str.length() - 1));
            if ("/".equals(lastIndex)) {
                int pos = splashPath.lastIndexOf('/');
                splashPath = splashPath.substring(0, pos);
            }
            countMap.remove(splashPath);
        }

        Iterable<String> it = countMap.keySet();
        for (String key : it) {
            LeoLog.d("testMainPic", "key is : " + key);
            aibumList.add(countMap.get(key));
        }
        Collections.sort(aibumList, FileOperationUtil.mFolderCamparator);

        return aibumList;
    }

    /*
     * 防止读取图片时读取到类似于系统Android/data等videoCache文件里的视频，需要过略的视频格式(使用时注意，返回值不能为空)
     */
    private static List<String> getFilterVideoType() {
        String[] filterVideoType = {
                ".mp4"
        };
        if (filterVideoType != null) {
            return Arrays.asList(filterVideoType);
        }
        return null;
    }

    /*
     * 过略掉视频格式（在有些类似于Android/data/的目录下的videoCache目录中的视频也会在媒体的图像数据库中读取出来，过略掉这些）
     */
    public static boolean isFilterVideoType(String path, String videoType) {
        return path.endsWith(videoType);
    }

    // 获取所有内置/外置SDCARD路径
    public static String[] getSdCardPaths(Context ctx) {
        StorageManager storageManager = (StorageManager) ctx
                .getSystemService(Context.STORAGE_SERVICE);
        Method method = null;
        try {
            method = StorageManager.class.getMethod("getVolumePaths");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (method != null) {
            try {
                String[] storagePathListt = (String[]) method
                        .invoke(storageManager);
                return storagePathListt;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    // 判断sdcard是否挂载上，返回值为true证明挂载上了，否则不存在
    public boolean checkSDCardMount(Context ctx, String mountPoint) {
        StorageManager storageManager = (StorageManager) ctx
                .getSystemService(Context.STORAGE_SERVICE);
        if (mountPoint == null) {
            return false;
        }
        String state = null;

        Method method = null;
        try {
            method = StorageManager.class.getMethod("getVolumeState",
                    String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (method != null) {
            try {
                state = (String) method.invoke(storageManager, mountPoint);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if (state == null) {
            return false;
        } else {
            return Environment.MEDIA_MOUNTED.equals(state);
        }

    }

    /**
     * FileCopy HideImage
     *
     * @param fromFile
     * @return
     */
    @SuppressWarnings("deprecation")
    public static String hideImageFileCopy(Context ctx, String fromFile, String newName) {
        String str = FileOperationUtil.getDirPathFromFilepath(fromFile);
        try {
            if (str.length() >= str.lastIndexOf("/") + 1) {
                String dirName = str.substring(str.lastIndexOf("/") + 1, str.length());
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        String fileName = FileOperationUtil.getNameFromFilepath(fromFile);
        File file = new File(fromFile);
        String[] paths = getSdCardPaths(ctx);
        int position = 0;
        if (fromFile.startsWith(paths[0])) {
            position = 1;
        } else if (fromFile.startsWith(paths[1])) {
            position = 0;
        } else {
            position = -1;
        }
        String newPath;
        File path = new File(fromFile);
        if (position == -1) {
            newPath = fromFile + Constants.CRYPTO_SUFFIX;
        } else {
            newPath = paths[position] + File.separator + SDCARD_DIR_NAME
                    + FileOperationUtil.getDirPathFromFilepath(fromFile).replace(paths[1], "")
                    + File.separator + fileName;
        }
        if (file.isFile()) {
            String newFileDir = null;
            try {
                if (newPath.lastIndexOf(File.separator) >= 0) {
                    newFileDir = newPath.substring(0,
                            newPath.lastIndexOf(File.separator));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            File temp = new File(newFileDir);
            LeoLog.d("RenameFile", "fileDir = " + newFileDir);
            if (temp.exists()) {
                LeoLog.d("RenameFile", temp + "    exists");
            } else {
                LeoLog.d("RenameFile", temp + "  not   exists");
                boolean mkRet = temp.mkdirs();
                if (mkRet) {
                    LeoLog.e("RenameFile", "make dir " + temp
                            + "  successfully");
                } else {
                    LeoLog.d("RenameFile", "make dir " + temp
                            + "  unsuccessfully");
                }
            }
        }
        try {
            InputStream fosfrom = null;
            OutputStream fosto = null;
            try {
                File copyFile = new File(newPath);
                fosfrom = new FileInputStream(fromFile);
                fosto = new FileOutputStream(newPath, true);
                byte bt[] = new byte[1024 * 8];
                int c;
                while ((c = fosfrom.read(bt)) > 0) {
                    fosto.write(bt, 0, c);
                }
            } finally {
                if (fosfrom != null) {
                    fosfrom.close();
                }
                if (fosto != null) {
                    fosto.close();
                }
            }
//            FileOperationUtil.saveFileMediaEntry(newPath, ctx);
            try {
                File imageFile = new File(newPath);
                String rename = newPath + Constants.CRYPTO_SUFFIX;
                boolean ret = imageFile.renameTo(new File(rename));
                FileOperationUtil.saveFileMediaEntry(rename, ctx);
//                FileOperationUtil.deleteImageMediaEntry(newPath, ctx);
                String resultVa = null;
                resultVa = rename;
                //设置隐藏方式
                setHideTpye(COPY_HIDE);

                return resultVa;
            } catch (Exception e) {
                return HIDE_PIC_COPY_RENAME_FAIL;
            }
        } catch (Exception ex) {
            return HIDE_PIC_COPY_FAIL;
        }
    }

    /**
     * FileCopy unHideImageFileCopy
     *
     * @param fromFile
     * @return
     */
    public static String unHideImageFileCopy(Context ctx, String fromFile) {
        String fileName = FileOperationUtil.getNameFromFilepath(fromFile);

        File file = new File(fromFile);
        String[] paths = getSdCardPaths(ctx);
        int position = 0;
        try {
            if (fromFile.startsWith(paths[0])) {
                position = 1;
            } else if (fromFile.startsWith(paths[1])) {
                position = 0;
            } else {
                position = -1;
            }
        } catch (Exception e) {
            position = -1;
        }

        String newPath = null;
        if (position == -1) {
            String pathOther = fromFile.replace(Constants.CRYPTO_SUFFIX, "");
            FileOperationUtil.saveFileMediaEntry(pathOther, ctx);
            FileOperationUtil.deleteFileMediaEntry(fromFile, ctx);
        } else {
            newPath = paths[position]
                    + FileOperationUtil.getDirPathFromFilepath(fromFile).replace(paths[1], "")
                    + File.separator + fileName;
            // newPath=FileOperationUtil.makePath(paths[position],
            // FileOperationUtil.getDirPathFromFilepath(fromFile), fileName);
        }
        if (file.isFile()) {
            String newFileDir = null;
            try {
                if (newPath.lastIndexOf(File.separator) >= 0) {
                    newFileDir = newPath.substring(0,
                            newPath.lastIndexOf(File.separator));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            File temp = new File(newFileDir);
            LeoLog.d("RenameFile", "fileDir = " + newFileDir);
            if (temp.exists()) {
                LeoLog.d("RenameFile", temp + "    exists");
            } else {
                LeoLog.d("RenameFile", temp + "  not   exists");
                boolean mkRet = temp.mkdirs();
                if (mkRet) {
                    LeoLog.e("RenameFile", "make dir " + temp
                            + "  successfully");
                } else {
                    LeoLog.d("RenameFile", "make dir " + temp
                            + "  unsuccessfully");
                }
            }
        }
        try {
            InputStream fosfrom = null;
            OutputStream fosto = null;
            try {
                fosfrom = new FileInputStream(fromFile);
                fosto = new FileOutputStream(newPath);
                byte bt[] = new byte[1024 * 8];
                int c;
                while ((c = fosfrom.read(bt)) > 0) {
                    fosto.write(bt, 0, c);
                }
            } finally {
                if (fosfrom != null) {
                    fosfrom.close();
                }
                if (fosto != null) {
                    fosto.close();
                }
            }
            try {

                String rename = newPath.replace(Constants.CRYPTO_SUFFIX, "");
                LeoLog.d("testRename", "rename:" + rename);
                File imageFile = new File(newPath);
                boolean ret = imageFile.renameTo(new File(rename));

                if (ret) {
                    FileOperationUtil.saveFileMediaEntry(rename, ctx);
                    FileOperationUtil.deleteFileMediaEntry(newPath, ctx);
                    String resultVa = null;
                    resultVa = rename;
                    // 复制取消隐藏成功
                    return resultVa;
                } else {
                    return HIDE_PIC_COPY_RENAME_FAIL;
                }

            } catch (Exception e) {
                // 取消隐藏失败
                return HIDE_PIC_COPY_RENAME_FAIL;
            }
        } catch (Exception ex) {
            // 复制失败
            return HIDE_PIC_COPY_FAIL;
        }
    }

    public static boolean isSDReady() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }

    // 获取保存路径
    public static String getSplashPath() {
        if (isSDReady()) {
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            if (TextUtils.isEmpty(path)) {
                return null;
            }
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            path += Constants.SPLASH_PATH;
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

    // 存储文件到磁盘
    public static void readAsFile(InputStream inSream, String file, Context context) {
        try {
            OutputStream outStream = new FileOutputStream(new File(file), false);
            // inSream.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            byte bt[] = new byte[1024 * 8];
            int c;
            while ((c = inSream.read(bt)) > 0) {
                outStream.write(bt, 0, c);
            }
            outStream.close();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 获取Bitmap文件大小
    @SuppressLint("NewApi")
    public static int getBitmapSize(Bitmap bitmap) {
        // API 19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        }
        {// API12
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
                return bitmap.getByteCount();
        }
        // earlier version
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    //删除图片，来自图片媒体数据库
    public static int deletePicFromDatebase(String picUri) {
        if (TextUtils.isEmpty(picUri)) {
            LeoLog.e("deletePicFromDatebase", "path----NULL");
            return -1;
        }
        LeoLog.e("deletePicFromDatebase", "path----:" + picUri);
        String params[] = new String[]{
                picUri
        };
        Uri uri = MediaStore.Files.getContentUri("external");
        int result = AppMasterApplication.getInstance().getContentResolver().delete(uri,
                MediaStore.MediaColumns.DATA + " LIKE ?", params);

        LeoLog.e("deletePicFromDatebase", "result----:" + result);
        return result;
    }
}
