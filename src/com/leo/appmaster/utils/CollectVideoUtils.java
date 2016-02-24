package com.leo.appmaster.utils;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.imageloader.utils.IoUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by Run on 2016/2/24.
 * 收集上传印度手机视频大小
 */
public class CollectVideoUtils {

    public static int M = 1 * 1024 * 1024;
    private static final String SYSTEM_PREFIX = "/system";
    //印地语
    private static final String IN = "IN";
    //印尼语
    private static final String ID = "ID";

    public static void getAllVideoData() {

        //条件1：用户人群限制
        String countryId = Locale.getDefault().getCountry();
        LeoLog.d("getAllVideo", "country id:" + countryId);
        boolean isIndUs = countryId.equalsIgnoreCase(IN) || countryId.equalsIgnoreCase(ID);
        if (!isIndUs) {
            return;
        }
        //条件2：每个用户只上传一次
        PreferenceTable pt = PreferenceTable.getInstance();
        boolean isReport = pt.getBoolean(PrefConst.KEY_REPORT_VIDEO_SIZE, false);
        if (isReport) {
            LeoLog.d("getAllVideo", "report finish...");
            return;
        }

        HashMap<String, Integer> videoMap = new HashMap<String, Integer>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = Constants.VIDEO_FORMAT;
        Cursor cursor = null;
        try {
            cursor = AppMasterApplication.getInstance().getContentResolver().query(uri, null, selection, null,
                    MediaStore.MediaColumns.DATE_MODIFIED + " desc");
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String path = cursor
                            .getString(cursor
                                    .getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));
                    if (path.startsWith(SYSTEM_PREFIX)) {
                        continue;
                    }
                    File videoFile = new File(path);
                    if (!videoFile.exists() || videoFile.length() <= 0) {
                        continue;
                    }
                    LeoLog.d("getAllVideo", "video path:" + path + ",size:" + videoFile.length() / M);
                    Set<String> videoKey = videoMap.keySet();
                    long size = (videoFile.length()) / M;
                    String key = getKey(size);

                    if (videoKey.contains(key)) {
                        Integer value = videoMap.get(key);
                        value = value + 1;
                        videoMap.put(key, value);
                    } else {
                        videoMap.put(key, 1);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
            //上报手机中所有视频大小数据
            if (videoMap != null && !videoMap.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Integer> entry : videoMap.entrySet()) {
                    String key = entry.getKey();
                    Integer value = entry.getValue();
                    sb.append(key + ":" + value);
                    sb.append(";");
                    LeoLog.d("getAllVideo", "key:" + key + ",value:" + value);
                }
                LeoLog.d("getAllVideo", "video collect data:" + sb.toString());
                if (!TextUtils.isEmpty(sb.toString())) {
                    SDKWrapper.addEvent(AppMasterApplication.getInstance(), SDKWrapper.P1, "handled", sb.toString());
                    pt.putBoolean(PrefConst.KEY_REPORT_VIDEO_SIZE, true);
                }
            }
        }

    }

    private static String getKey(long size) {
        String key1 = "vid_chk_0~20M";
        String key2 = "vid_chk_20~50M";
        String key3 = "vid_chk_50~80M";
        String key4 = "vid_chk_80~100M";
        String key5 = "vid_chk_100~200M";
        String key6 = "vid_chk_200~500M";
        String key7 = "vid_chk_500~800M";
        String key8 = "vid_chk_800~1024M";
        String key9 = "vid_chk_1~2G";
        String key10 = "vid_chk_2~5G";
        String key11 = "vid_chk_5~8G";
        String key12 = "vid_chk_8~10G";
        String key13 = "vid_chk_10G+";
        String key = "";
        if (size <= 20) {
//                        0<x≤20M
            key = key1;
        } else if (size > 20 && size <= 50) {
//                        20<x≤50M
            key = key2;
        } else if (size > 50 && size <= 80) {
//                        50<x≤80M
            key = key3;
        } else if (size > 80 && size <= 100) {
//                        80<x≤100M
            key = key4;
        } else if (size > 100 && size <= 200) {
//                        100<x≤200M
            key = key5;
        } else if (size > 200 && size <= 500) {
//                        200<x≤500M
            key = key6;
        } else if (size > 500 && size <= 800) {
//                        500<x≤800M
            key = key7;
        } else if (size > 800 && size <= 1024) {
//                        800<x≤1024M
            key = key8;
        } else if (size > 1024 && size <= 2 * 1024) {
//                        1<x≤2G
            key = key9;
        } else if (size > 2 * 1024 && size <= 5 * 1024) {
//                        2<x≤5G
            key = key10;
        } else if (size > 5 * 1024 && size <= 8 * 1024) {
//                        5<x≤8G
            key = key11;
        } else if (size > 8 * 1024 && size <= 10 * 1024) {
//                        8<x≤10G
            key = key12;
        } else if (size > 10 * 1024) {
//                        10G<x
            key = key13;
        }
        return key;
    }
}
