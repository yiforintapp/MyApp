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
import java.util.TimeZone;

/**
 * Created by Run on 2016/2/24.
 * 收集上传印度手机视频大小
 */
public class CollectVideoUtils {

    public static int M = 1 * 1024 * 1024;
    private static final String SYSTEM_PREFIX = "/system";
    //印度时间GMT
    private static final String INDIA_GMT = "GMT+05:30";

    public static void getAllVideoData() {

        //条件1：用户人群限制,通过时区
        TimeZone tz = TimeZone.getDefault();
        String s = tz.getDisplayName(false, TimeZone.SHORT);
        LeoLog.d("getAllVideo", "TimeZone:" + s);
        boolean isIndUs = s.equals(INDIA_GMT);
        if (!isIndUs) {
            return;
        }

        //条件2：每个用户只上传一次
        PreferenceTable pt = PreferenceTable.getInstance();
        boolean isReport = pt.getBoolean(PrefConst.KEY_REPORT_VIDEO_SIZE, false);
        if (isReport) {
            LeoLog.d("getAllVideo", "-----------report finish...");
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
                if (!TextUtils.isEmpty(sb.toString())) {
                    SDKWrapper.addEvent(AppMasterApplication.getInstance(), SDKWrapper.P1, "vidups", sb.toString());
                    pt.putBoolean(PrefConst.KEY_REPORT_VIDEO_SIZE, true);
                    LeoLog.d("getAllVideo", "-----------video collect data:" + sb.toString());
                }
            }
        }

    }

    private static String getKey(long size) {
        String key1 = "v1";
        String key2 = "v2";
        String key3 = "v3";
        String key4 = "v4";
        String key5 = "v5";
        String key6 = "v6";
        String key7 = "v7";
        String key8 = "v8";
        String key9 = "v9";
        String key10 = "v10";
        String key11 = "v11";
        String key12 = "v12";
        String key13 = "v13";
        String key = "";
        if (size <= 20) {
            key = key1;
        } else if (size > 20 && size <= 50) {
            key = key2;
        } else if (size > 50 && size <= 80) {
            key = key3;
        } else if (size > 80 && size <= 100) {
            key = key4;
        } else if (size > 100 && size <= 200) {
            key = key5;
        } else if (size > 200 && size <= 500) {
            key = key6;
        } else if (size > 500 && size <= 800) {
            key = key7;
        } else if (size > 800 && size <= 1024) {
            key = key8;
        } else if (size > 1024 && size <= 2 * 1024) {
            key = key9;
        } else if (size > 2 * 1024 && size <= 5 * 1024) {
            key = key10;
        } else if (size > 5 * 1024 && size <= 8 * 1024) {
            key = key11;
        } else if (size > 8 * 1024 && size <= 10 * 1024) {
            key = key12;
        } else if (size > 10 * 1024) {
            key = key13;
        }
        return key;
    }
}
