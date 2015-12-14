package com.leo.appmaster.mgr.impl;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.imagehide.PhotoAibum;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.mgr.Manager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyDataManager;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.videohide.VideoBean;
import com.leo.appmaster.videohide.VideoItemBean;
import com.leo.imageloader.utils.IoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivacyDataManagerImpl extends PrivacyDataManager {
    private static final int API_LEVEL_19 = 19;
    public final static String CHECK_APART = "check_apart";
    private final static int MAX_NUM = 702;
    public static final String[] STORE_IMAGES = {
            MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID, //
            MediaStore.Images.Media.BUCKET_ID, // dir id
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            // dir name
    };

    public static final String[] NEW_ADD_IMAGES = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID
    };

    private static final String SYSTEM_PREFIX = "/system";

    String[] STORE_HIDEIMAGES = new String[]{
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns._ID, //
    };

    private Handler mHandler = new Handler();
    private MediaStoreChangeObserver mMediaStoreChangeObserver;
    private static int mScore = 0;
    private int mScanAddPicNum = 0;
    private int mScanAddVidNum = 0;


    @Override
    public void onDestory() {

    }

    @Override
    public void init() {

    }

    @Override
    public void notifySecurityChange() {
        super.notifySecurityChange();

    }

    @Override
    public void registerSecurityListener(SecurityChangeListener listener) {
        super.registerSecurityListener(listener);
//        mMediaStoreChangeObserver = new MediaStoreChangeObserver(mHandler);
//        mMediaStoreChangeObserver.startObserver();
//        LeoLog.d("monitorMedia", "registerContentObserver!");
    }

    private class MediaStoreChangeObserver extends ContentObserver {
        ContentResolver MediaStoreResolver;

        public MediaStoreChangeObserver(Handler handler) {
            super(handler);
            MediaStoreResolver = mContext.getContentResolver();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            LeoLog.d("monitorMedia", "onChange");
            //change the score
            notifySecurityChange();
        }


        // 注册观察
        public void startObserver() {
            MediaStoreResolver.registerContentObserver
                    (MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mMediaStoreChangeObserver);
        }

        // 解除观察
        public void stopObserver() {
            MediaStoreResolver.unregisterContentObserver(this);
        }
    }

    @Override
    public void unregisterSecurityListener(SecurityChangeListener listener) {
        super.unregisterSecurityListener(listener);
//        mMediaStoreChangeObserver.stopObserver();
    }

    @Override
    public List<PhotoAibum> getHidePicAlbum(String mSuffix) {
        //mSuffix is 后缀 , some 后缀 you want , and replace it
        List<PhotoAibum> aibumList = new ArrayList<PhotoAibum>();
        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.MediaColumns.DATA + " LIKE '%.leotmp'" + " or " + MediaStore.MediaColumns.DATA
                + " LIKE '%.leotmi'";

        int picNumFromDir = 1;
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(uri, STORE_HIDEIMAGES, selection, null,
                    MediaStore.MediaColumns.DATE_ADDED + " desc");
            Map<String, PhotoAibum> countMap = new HashMap<String, PhotoAibum>();
            PhotoAibum pa = null;
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String path = cursor.getString
                            (cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    LeoLog.d("testPicLoadTime", "hide album path : " + path);
                    String dirName = FileOperationUtil.getDirNameFromFilepath(path);
                    String dirPath = FileOperationUtil.getDirPathFromFilepath(path);
                    if (!countMap.containsKey(dirPath)) {
//                        LeoLog.d("testDirName", "dirPath is : " + dirPath);

                        pa = new PhotoAibum();
                        pa.setName(dirName);
                        pa.setCount("1");
                        pa.setDirPath(dirPath);
                        pa.getBitList().add(new PhotoItem(path));
                        countMap.put(dirPath, pa);

                    } else {
                        if (mSuffix != null && mSuffix.equals(CHECK_APART)) {
                            picNumFromDir = pa.getBitList().size();
//                            LeoLog.d("testhowDir", "n : " + picNumFromDir + ",p : " + path);
                            if (picNumFromDir < MAX_NUM) {
                                File f = new File(path);
                                if (f.exists()) {
                                    pa = countMap.get(dirPath);
                                    pa.setCount(String.valueOf(Integer.parseInt(pa.getCount()) + 1));
                                    pa.getBitList().add(new PhotoItem(path));
                                }
                            } else {
                                pa = countMap.get(dirPath);
                                pa.setCount(String.valueOf(Integer.parseInt(pa.getCount()) + 1));
                                pa.getBitList().add(new PhotoItem(path));
                            }
                        } else {
                            File f = new File(path);
                            if (f.exists()) {
                                pa = countMap.get(dirPath);
                                pa.setCount(String.valueOf(Integer.parseInt(pa.getCount()) + 1));
                                pa.getBitList().add(new PhotoItem(path));
                            }
                        }

                    }
                }
            }
            Iterable<String> it = countMap.keySet();
            for (String key : it) {
                aibumList.add(countMap.get(key));
            }
            Collections.sort(aibumList, FileOperationUtil.mFolderCamparator);
        } catch (Exception e) {

        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }

        return aibumList;
    }


    @Override
    public List<PhotoItem> getHidePicFile(PhotoAibum mFileInfo) {
        List<PhotoItem> mPicturesList = mFileInfo.getBitList();
        return mPicturesList;
    }

    @Override
    public String cancelHidePic(String mPicPath) {
        long totalSize = new File(mPicPath).length();
        String newPaht = FileOperationUtil.unhideImageFile(
                mContext, mPicPath, totalSize);
        return newPaht;
    }

    @Override
    public boolean deleteHidePic(String mPicPath) {
        return FileOperationUtil.deleteFile(mPicPath);
    }

    @Override
    public List<PhotoAibum> getAllPicFile() {
        return FileOperationUtil.getPhotoAlbum(mContext);
    }

    @Override
    public String onHidePic(String mPicPath, String mSuffix) {
        //mSuffix , 暂时用不上
        long totalSize = new File(mPicPath).length();

        String newFileName = FileOperationUtil
                .getNameFromFilepath(mPicPath);
        newFileName = newFileName + ".leotmi";
        String newPath = FileOperationUtil.hideImageFile(mContext,
                mPicPath, newFileName, totalSize);

        return newPath;
    }

    @Override
    public int onHideAllPic(List<String> mString) {
        // 大量图片的情况，每隐藏一张图片就通知更新UI，造成卡顿，并且分数错乱
//        mMediaStoreChangeObserver.stopObserver();
        try {
            int newAddPicNum = getAddPicNum();
            int num = mString.size();
            for (int i = 0; i < num; i++) {
                String path = mString.get(i);
                String newPath = onHidePic(path, "");
                if (newPath != null) {
                    if ("-2".equals(newPath)) {
                    } else if ("0".equals(newPath)) {
                    } else if ("-1".equals(newPath)) {
                    } else if ("4".equals(newPath)) {
                        break;
                    } else {
                        FileOperationUtil.saveFileMediaEntry(newPath,
                                mContext);
                        FileOperationUtil.deleteImageMediaEntry(
                                path, mContext);
                    }
                }
            }

            if (newAddPicNum < 30) {
                return newAddPicNum;
            } else {
                return 30;
            }
        } finally {
//            mMediaStoreChangeObserver.startObserver();
        }


    }


    @Override
    public List<VideoBean> getHideVidAlbum(String mVidSuffix) {
        List<VideoBean> videoBeans = new ArrayList<VideoBean>();
        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.MediaColumns.DATA + " LIKE '%.leotmv'";
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(uri, null, selection, null,
                    MediaStore.MediaColumns.DATE_ADDED + " desc");
            if (cursor != null) {
                Map<String, VideoBean> countMap = new HashMap<String, VideoBean>();
                while (cursor.moveToNext()) {
                    VideoBean video = new VideoBean();
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                    LeoLog.d("checkVidId", "path is : " + path);

//                    int currSDK_INT = Build.VERSION.SDK_INT;
//                    if (currSDK_INT >= API_LEVEL_19) {
//                        File externalStorageDirectory = Environment.getExternalStorageDirectory();
//                        String store = externalStorageDirectory.getPath();
//                        LeoLog.d("checkVidId", "store is : " + store);
//                        if (!path.startsWith(store)) {
//                            LeoLog.d("checkVidId", "out card");
//                            continue;
//                        } else {
//                            LeoLog.d("checkVidId", "inner card");
//                        }
//                    }

                    String dirName = FileOperationUtil.getDirNameFromFilepath(path);
                    String dirPath = FileOperationUtil.getDirPathFromFilepath(path);
                    video.setDirPath(dirPath);
                    video.setName(dirName);
                    File videoFile = new File(path);
                    boolean videoExists = videoFile.exists();
                    if (videoExists) {
                        VideoBean vb = null;
                        if (!countMap.containsKey(dirPath)) {
                            vb = new VideoBean();
                            vb.setName(dirName);
                            vb.setDirPath(dirPath);
                            vb.getBitList().add(new VideoItemBean(path));
                            vb.setPath(path);
                            countMap.put(dirPath, vb);
                        } else {
                            vb = countMap.get(dirPath);
                            vb.getBitList().add(new VideoItemBean(path));
                        }
                    }
                }

                Iterable<String> it = countMap.keySet();
                for (String key : it) {
                    videoBeans.add(countMap.get(key));
                }
                Collections.sort(videoBeans, mFolderCamparator);
            }
        } catch (Exception e) {
        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }

        return videoBeans;
    }

    @Override
    public List<VideoItemBean> getHideVidFile(VideoBean mFileInfo) {
        List<VideoItemBean> mVideoItems = mFileInfo.getBitList();
        return mVideoItems;
    }

    @Override
    public boolean cancelHideVid(String mVidPath) {
        String newFileName = FileOperationUtil.getNameFromFilepath(mVidPath);
        newFileName = newFileName.substring(0,
                newFileName.indexOf(".leotmv"));
        return FileOperationUtil.renameFile(mVidPath, newFileName);
    }

    @Override
    public boolean deleteHideVid(String mVidPath) {
        return FileOperationUtil.deleteFile(mVidPath);
    }

    @Override
    public List<VideoBean> getAllVidFile() {
        List<VideoBean> videoBeans = new ArrayList<VideoBean>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = Constants.VIDEO_FORMAT;
        Cursor cursor = null;
        try {
            //old
            cursor = mContext.getContentResolver().query(uri, null, selection, null,
                    MediaStore.MediaColumns.DATE_MODIFIED + " desc");
            //new
//            cursor = mContext.getContentResolver().query(uri, null, selection, null,
//                    MediaStore.MediaColumns._ID + " desc");
            if (cursor != null) {
                Map<String, VideoBean> countMap = new HashMap<String, VideoBean>();
                while (cursor.moveToNext()) {
                    VideoBean video = new VideoBean();
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));
                    LeoLog.d("checkVidId", "path is : " + path);

                    if (path.startsWith(SYSTEM_PREFIX)) {
                        continue;
                    }

//                    int currSDK_INT = Build.VERSION.SDK_INT;
//                    if (currSDK_INT >= API_LEVEL_19) {
//                        File externalStorageDirectory = Environment.getExternalStorageDirectory();
//                        String store = externalStorageDirectory.getPath();
//                        LeoLog.d("checkVidId", "store is : " + store);
//                        String outstore = FileOperationUtil.getSdDirectory();
//                        LeoLog.d("checkVidId", "outstore is : " + outstore);
//                        if (!path.startsWith(store)) {
//                            LeoLog.d("checkVidId", "out card");
//                            continue;
//                        } else {
//                            LeoLog.d("checkVidId", "inner card");
//                        }
//                    }

                    String dirName = FileOperationUtil.getDirNameFromFilepath(path);
                    String dirPath = FileOperationUtil.getDirPathFromFilepath(path);
                    video.setDirPath(dirPath);
                    video.setName(dirName);
                    File videoFile = new File(path);
                    boolean videoExists = videoFile.exists();
                    if (videoExists) {
                        VideoBean vb = null;
                        if (!countMap.containsKey(dirPath)) {
                            vb = new VideoBean();
                            vb.setName(dirName);
                            vb.setDirPath(dirPath);
                            vb.getBitList().add(new VideoItemBean(path));
                            vb.setPath(path);
                            countMap.put(dirPath, vb);
                        } else {
                            vb = countMap.get(dirPath);
                            vb.getBitList().add(new VideoItemBean(path));
                        }
                    }
                }
                Iterable<String> it = countMap.keySet();
                for (String key : it) {
                    videoBeans.add(countMap.get(key));
                }
                Collections.sort(videoBeans, mFolderCamparator);
            }
        } catch (Exception e) {
        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }

        return videoBeans;

    }

    @Override
    public boolean onHideVid(String mVidPath, String mVidSuffix) {
        //mVidSuffix , 暂时用不上
        String newFileName =
                FileOperationUtil.getNameFromFilepath(mVidPath);
        newFileName = newFileName + ".leotmv";
        return FileOperationUtil.renameFile(mVidPath, newFileName);
    }

    @Override
    public int onHideAllVid(List<String> mString) {
        // 大量图片的情况，每隐藏一张图片就通知更新UI，造成卡顿，并且分数错乱
//        mMediaStoreChangeObserver.stopObserver();
        try {
            int newAddVidNum = getAddVidNum();

            int num = mString.size();
            for (int i = 0; i < num; i++) {
                String path = mString.get(i);
                String newFileName =
                        FileOperationUtil.getNameFromFilepath(path);
                newFileName = newFileName + ".leotmv";
                boolean isSuccess = onHideVid(path, "");
                if (isSuccess) {
                    FileOperationUtil.saveFileMediaEntry(FileOperationUtil
                            .makePath(
                                    FileOperationUtil
                                            .getDirPathFromFilepath(path),
                                    newFileName), mContext);
                    FileOperationUtil.deleteVideoMediaEntry(path,
                            mContext);
//                    FileOperationUtil.updateVedioMediaEntry(path, true, "text/plain", mContext);
                }
            }

            if (newAddVidNum < getMaxVidNum()) {
                return newAddVidNum * SPA_VID;
            } else {
                return MAX_VID_SCORE;
            }
        } finally {
//            mMediaStoreChangeObserver.startObserver();
        }
    }

    public String getSplashDirPath() {
        String path = FileOperationUtil.getSplashPath();
        if (path != null) {
            StringBuffer str = new StringBuffer(path);
            String lastIndex = String.valueOf(str.charAt(str.length() - 1));
            if ("/".equals(lastIndex)) {
                int pos = path.lastIndexOf('/');
                path = path.substring(0, pos);
                return path;
            }
        }
        return "";
    }

    @Override
    public List<PhotoItem> getAddPic() {
        int picNum = 0;
        int mRecordNum;
        int lastPic = PreferenceTable.getInstance().getInt(PrefConst.KEY_NEW_ADD_PIC, 0);
        LeoLog.d("getAddPic", "lastPic is : " + lastPic);
        List<String> filterVideoTypes = getFilterVideoType();
        List<PhotoItem> aibumList = new ArrayList<PhotoItem>();
        Cursor cursor = null;

        String splashPath = getSplashDirPath();

        int currSDK_INT = Build.VERSION.SDK_INT;
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        String store = externalStorageDirectory.getPath();

        try {
            cursor = MediaStore.Images.Media.query(mContext.getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, NEW_ADD_IMAGES,
                    MediaStore.MediaColumns._ID + ">?",
                    new String[]{String.valueOf(lastPic)},
                    MediaStore.MediaColumns._ID + " desc");
            LeoLog.d("getAddPic", "cursor size : " + cursor.getCount());
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    //record the last pic id
                    mRecordNum = id;
                    String path = cursor.getString
                            (cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    String dirPath = FileOperationUtil.getDirPathFromFilepath(path);

                    if (picNum == 0) {
                        picNum = id;
                    }

                    if (splashPath != null && dirPath.equals(splashPath)) {
                        continue;
                    }

                    if (path.startsWith(SYSTEM_PREFIX)) {
                        continue;
                    }

                    if (currSDK_INT >= API_LEVEL_19) {
                        if (!path.startsWith(store)) {
                            continue;
                        }
                    }

                    File f = new File(path);
                    if (!f.exists()) {
                        continue;
                    }

                    boolean isFilterVideoType = false;
                    for (String videoType : filterVideoTypes) {
                        isFilterVideoType = isFilterVideoType(path, videoType);
                    }
                    if (isFilterVideoType) {
                        continue;
                    }

                    if (lastPic == 0) {
                        //all in
                        aibumList.add(new PhotoItem(path, mRecordNum));
                    } else {
                        //in some
                        if (id > lastPic) {
                            aibumList.add(new PhotoItem(path, mRecordNum));
                        } else {
                            break;
                        }
                    }
                }
                if (cursor.getCount() != 0) {
                    LeoLog.d("getAddPic", "getAddPic save id is : " + picNum);
                    PreferenceTable.getInstance().putInt(PrefConst.KEY_NEW_LAST_ADD_PIC, picNum);
                }
            }
        } catch (Exception e) {

        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }

        mScanAddPicNum = aibumList.size();
        return aibumList;
    }

    @Override
    public int getAddPicNum() {
        int record = 0;
        int picNum = 0;
        int lastPic = PreferenceTable.getInstance().getInt(PrefConst.KEY_NEW_ADD_PIC, 0);
        List<String> filterVideoTypes = getFilterVideoType();
        Cursor cursor = null;

        int currSDK_INT = Build.VERSION.SDK_INT;
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        String store = externalStorageDirectory.getPath();
        String splashPath = getSplashDirPath();

        try {
            cursor = MediaStore.Images.Media.query(mContext.getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, NEW_ADD_IMAGES,
                    MediaStore.MediaColumns._ID + ">?",
                    new String[]{String.valueOf(lastPic)},
                    MediaStore.MediaColumns._ID + " desc");

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (picNum > getMaxPicNum()) {
                        break;
                    }
                    int id = cursor.getInt
                            (cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    if (record == 0) {
                        record = id;
                    }

                    String path = cursor.getString
                            (cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    String dirPath = FileOperationUtil.getDirPathFromFilepath(path);

                    if (path.startsWith(SYSTEM_PREFIX)) {
                        continue;
                    }
                    if (splashPath != null && dirPath.equals(splashPath)) {
                        continue;
                    }

                    if (currSDK_INT >= API_LEVEL_19) {
                        if (!path.startsWith(store)) {
                            continue;
                        }
                    }

                    File f = new File(path);
                    if (!f.exists()) {
                        continue;
                    }

                    boolean isFilterVideoType = false;
                    for (String videoType : filterVideoTypes) {
                        isFilterVideoType = isFilterVideoType(path, videoType);
                    }
                    if (isFilterVideoType) {
                        continue;
                    }

                    picNum++;
                }
                if (cursor.getCount() != 0) {
                    LeoLog.d("checkPicId", "getAddPicNum save id is : " + record);
                    PreferenceTable.getInstance().putInt(PrefConst.KEY_NEW_LAST_ADD_PIC, record);
                }
            }
        } catch (Exception e) {

        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }

        return picNum;
    }


    @Override
    public int haveCheckedPic() {
        int i = getAddPicNum();
        LeoLog.d("testHidePic", "haveCheckedPic : " + i);
        int lastRecord = PreferenceTable.getInstance().getInt(PrefConst.KEY_NEW_LAST_ADD_PIC, 0);
        LeoLog.d("testHidePic", "lastRecord : " + lastRecord);
        PreferenceTable.getInstance().putInt(PrefConst.KEY_NEW_ADD_PIC, lastRecord);

        if (mScanAddPicNum != 0) {
            if (mScanAddPicNum > 30) {
                return MAX_PIC_SCORE;
            } else {
                if (i != mScanAddPicNum) {
                    return mScanAddPicNum * SPA_PIC;
                } else {
                    return i * SPA_PIC;
                }
            }
        } else {
            return 0;
        }

    }


    @Override
    public List<VideoItemBean> getAddVid() {
        LeoLog.d("testhidevid", "come to getAddVid");
        int vidNum = 0;
        int mRecordNum;
        int lastVid = PreferenceTable.getInstance().getInt(PrefConst.KEY_NEW_ADD_VID, 0);
        LeoLog.d("checkVidId", "lastVid is : " + lastVid);
        List<VideoItemBean> videoBeans = new ArrayList<VideoItemBean>();
//        Uri uri = MediaStore.Files.getContentUri("external");
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = Constants.VIDEO_FORMAT;
        Cursor cursor = null;

        int currSDK_INT = Build.VERSION.SDK_INT;
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        String store = externalStorageDirectory.getPath();

        try {
//            cursor = mContext.getContentResolver().query(uri,
//                    null,
//                    selection + " and " + MediaStore.MediaColumns._ID + ">?",
//                    new String[]{String.valueOf(lastVid)},
//                    MediaStore.MediaColumns._ID + " desc");
            cursor = mContext.getContentResolver().query(uri, null, selection, null,
                    MediaStore.MediaColumns._ID + " desc");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    LeoLog.d("checkVidId", "id is : " + id);
                    mRecordNum = id;
                    if (vidNum == 0) {
                        vidNum = id;
                    }
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));
                    LeoLog.d("testhidevid", "path is :" + path);
                    String name = FileOperationUtil.getNoExtNameFromHideFilepath(path);
                    if (path.startsWith(SYSTEM_PREFIX)) {
                        continue;
                    }

                    if (currSDK_INT >= API_LEVEL_19) {
                        if (!path.startsWith(store)) {
                            continue;
                        }
                    }

                    File f = new File(path);
                    if (!f.exists()) {
                        continue;
                    }

                    if (lastVid == 0) {
                        //all in
                        videoBeans.add(new VideoItemBean(path, mRecordNum, name));
                    } else {
                        //in some
                        if (id > lastVid) {
                            LeoLog.d("checkVidId", "new path : " + path);
                            videoBeans.add(new VideoItemBean(path, mRecordNum, name));
                        } else {
                            break;
                        }

                    }
                }
                if (cursor.getCount() != 0) {
                    LeoLog.d("checkVidId", "save id is : " + vidNum);
                    PreferenceTable.getInstance().putInt(PrefConst.KEY_NEW_LAST_ADD_VID, vidNum);
                }
            }

        } catch (Exception e) {
            LeoLog.d("checkVidId", "catch");
        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }
        LeoLog.d("checkVidId", "final size is : " + videoBeans.size());
        mScanAddVidNum = videoBeans.size();
        return videoBeans;
    }

    @Override
    public int getAddVidNum() {
        int vidNum = 0;
        int record = 0;
        int lastVid = PreferenceTable.getInstance().getInt(PrefConst.KEY_NEW_ADD_VID, 0);
        LeoLog.d("checkVidId", "lastVid is : " + lastVid);
//        Uri uri = MediaStore.Files.getContentUri("external");
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = Constants.VIDEO_FORMAT;
        Cursor cursor = null;
        int currSDK_INT = Build.VERSION.SDK_INT;
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        String store = externalStorageDirectory.getPath();
        try {
//            cursor = mContext.getContentResolver().query(uri,
//                    null,
//                    selection + " and " + MediaStore.MediaColumns._ID + ">?",
//                    new String[]{String.valueOf(lastVid)},
//                    MediaStore.MediaColumns._ID + " desc");
            cursor = mContext.getContentResolver().query(uri, null, selection, null,
                    MediaStore.MediaColumns._ID + " desc");
            if (cursor != null) {
                while (cursor.moveToNext()) {

                    if (vidNum > 4) {
                        break;
                    }

                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    LeoLog.d("checkVidId", "id is : " + id);

                    if (record == 0) {
                        record = id;
                    }

                    if (id <= lastVid) {
                        break;
                    }
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));

                    if (currSDK_INT >= API_LEVEL_19) {
                        if (!path.startsWith(store)) {
                            continue;
                        }
                    }

                    File f = new File(path);
                    if (!f.exists()) {
                        continue;
                    }

                    vidNum++;
                }
                if (cursor.getCount() != 0) {
                    LeoLog.d("checkVidId", "save id is : " + record);
                    PreferenceTable.getInstance().putInt(PrefConst.KEY_NEW_LAST_ADD_VID, record);
                }
            }

        } catch (Exception e) {
        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }
        return vidNum;
    }

    public int getNextToTargetId(int targetId) {
        int vidNum = 0;
        int lastVid = PreferenceTable.getInstance().getInt(PrefConst.KEY_NEW_ADD_VID, 0);
        LeoLog.d("checkVidId", "getNextToTargetId lastVid is : " + lastVid);
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = Constants.VIDEO_FORMAT;
        Cursor cursor = null;

        try {
            cursor = mContext.getContentResolver().query(uri, null, selection, null,
                    MediaStore.MediaColumns._ID + " desc");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    LeoLog.d("checkVidId", "id is : " + id);
                    if (lastVid != id) {
                        vidNum = id;
                    } else {
                        break;
                    }

                }
            }
        } catch (Exception e) {
        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }
        return vidNum;
    }

    @Override
    public void registerMediaListener() {
//        if (mMediaStoreChangeObserver == null) {
//            mMediaStoreChangeObserver = new MediaStoreChangeObserver(mHandler);
//        }
//        mMediaStoreChangeObserver.startObserver();
    }

    @Override
    public void unregisterMediaListener() {
//        if (mMediaStoreChangeObserver != null) {
//            mMediaStoreChangeObserver.stopObserver();
//        }
    }

    @Override
    public int getMaxPicNum() {
        return MAX_PIC_SCORE / SPA_PIC;
    }

    @Override
    public int getMaxVidNum() {
        return MAX_VID_SCORE / SPA_VID;
    }

    @Override
    public int getPicScore(int newPicNum) {
        int picScore = 0;
        if (newPicNum < getMaxPicNum()) {
            picScore = MAX_PIC_SCORE - newPicNum * SPA_PIC;
        }
        return picScore;
    }

    @Override
    public int getVidScore(int newVidNum) {
        int vidScore = 0;
        if (newVidNum < getMaxVidNum()) {
            vidScore = MAX_VID_SCORE - newVidNum * SPA_VID;
        }
        return vidScore;
    }

    @Override
    public int haveCheckedVid() {
        int i = getAddVidNum();
        int lastRecord = PreferenceTable.getInstance().getInt(PrefConst.KEY_NEW_LAST_ADD_VID, 0);
        PreferenceTable.getInstance().putInt(PrefConst.KEY_NEW_ADD_VID, lastRecord);

        if (mScanAddVidNum != 0) {
            if (mScanAddVidNum > 4) {
                return MAX_VID_SCORE;
            } else {
                if (i != mScanAddVidNum) {
                    return mScanAddVidNum * SPA_VID;
                } else {
                    return i * SPA_VID;
                }
            }
        } else {
            return 0;
        }

    }

    //耗时操作
    @Override
    public int getSecurityScore() {
        long one = System.currentTimeMillis();
        int score = getScoreRightNow();
        long two = System.currentTimeMillis();
        LeoLog.d("checkScore", "获取得分耗时：" + (two - one));
        return score;
    }

    @Override
    public int ignore() {
        return haveCheckedPic() + haveCheckedVid();
    }

    public int getScoreRightNow() {
        int score = 0;

        int picScore = 0;
        int vidScore = 0;
        int picNum = getAddPicNum();
        int vidNum = getAddVidNum();
        if (picNum < getMaxPicNum()) {
            picScore = MAX_PIC_SCORE - picNum * SPA_PIC;
        }
        if (vidNum < getMaxVidNum()) {
            vidScore = MAX_VID_SCORE - vidNum * SPA_VID;
        }

        score = picScore + vidScore;
        LeoLog.d("checkScore", "pic score is : " + picScore);
        LeoLog.d("checkScore", "vid score is : " + vidScore);
        return score;
    }

    /**
     * Comparator date
     */
    public Comparator<VideoBean> mFolderCamparator = new Comparator<VideoBean>() {

        public final int compare(VideoBean a, VideoBean b) {
            if (a.getmLastModifyDate().before(b.getmLastModifyDate()))
                return 1;
            if (a.getmLastModifyDate().after(b.getmLastModifyDate()))
                return -1;
            return 0;
        }
    };

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
}
