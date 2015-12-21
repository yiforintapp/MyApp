
package com.leo.appmaster.utils;

import android.content.Context;
import android.graphics.Color;

import com.leo.appmaster.R;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.videohide.VideoItemBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DataUtils {

    public static long SECOND = 1000;
    public static long MINUTE = 60 * SECOND;
    public static long HOUR = 60 * MINUTE;

    public static String millsecondToTime(long mills) {
        return null;
    }

    public static boolean differentDirVid(List<VideoItemBean> list) {
        if (list == null) return false;

        String parent = null;
        boolean foundDifferent = false;
        for (VideoItemBean item : list) {
            String path = item.getPath();
            String parentPath = path.substring(0, path.lastIndexOf("/"));
            if (parent == null) {
                parent = parentPath;
                continue;
            }

            if (!parent.equals(parentPath)) {
                foundDifferent = true;
                break;
            }
        }

        return foundDifferent;
    }

    public static boolean differentDirPic(List<PhotoItem> list) {
        if (list == null) return false;

        String parent = null;
        boolean foundDifferent = false;
        for (PhotoItem item : list) {
            String path = item.getPath();
            String parentPath = path.substring(0, path.lastIndexOf("/"));
            if (parent == null) {
                parent = parentPath;
                continue;
            }

            if (!parent.equals(parentPath)) {
                foundDifferent = true;
                break;
            }
        }

        return foundDifferent;
    }

    public static int getGradientColor(float ratio, int from, int to) {
        if (from == to) return from;

        int fromR = Color.red(from);
        int fromG = Color.green(from);
        int fromB = Color.blue(from);

        int toR = Color.red(to);
        int toG = Color.green(to);
        int toB = Color.blue(to);
        int tarR = (int) (fromR + (toR - fromR) * ratio);
        int tarG = (int) (fromG + (toG - fromG) * ratio);
        int tarB = (int) (fromB + (toB - fromB) * ratio);

        return Color.rgb(tarR, tarG, tarB);
    }
}
