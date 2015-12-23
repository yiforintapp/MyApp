
package com.leo.appmaster.utils;

import android.graphics.Color;

import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.videohide.VideoItemBean;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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

    /** 获得高危应用需要展示的应用 */
    public static String getThreeRandomAppName(List<AppItemInfo> list) {
        StringBuffer appName = new StringBuffer();
        int theSize = list.size();
        if (theSize == 0) {
            return "";
        } else if (theSize <= 3) {
            for (AppItemInfo appItemInfo:list) {
                appName.append(appItemInfo.packageName).append(",");
            }
        } else {
            HashSet<Integer> set = new HashSet<Integer>();
            randomSet(theSize, 3, set);
            Iterator<Integer> iterator = set.iterator(); //迭代遍历
            while (iterator.hasNext()) {
                appName.append(list.get(iterator.next()).label).append(",");
            }
        }

        return appName.toString().substring(0,appName.length() - 1);

    }

    /** 获得随机数集合 */
    private static void randomSet(int size, int n, HashSet<Integer> set) {

        for (int i = 0; i < n; i++) {
            Random random = new Random();
            int num = random.nextInt(size);
            set.add(num); // 将不同的数存入HashSet中
        }
        int setSize = set.size();

        if (setSize < n) {
            randomSet(size, n - setSize, set);// 递归
        }
    }
}
