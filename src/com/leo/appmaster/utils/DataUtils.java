
package com.leo.appmaster.utils;

import android.content.Context;
import android.graphics.Color;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.ListAppLockAdapter;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.imagehide.PhotoItem;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.videohide.VideoItemBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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

    private static  List<AppItemInfo> changeTopPos(List<AppItemInfo> mDataList) {

        for (int i = 0; i < mDataList.size(); i++) {
            AppItemInfo info = mDataList.get(i);
            info.topPos = ListAppLockAdapter.fixPosEqules(info);
        }

        return mDataList;
    }

    /** 获得高危应用需要展示的应用 */
    public static ArrayList<String> getThreeRandomAppName(List<AppItemInfo> list, Context context) {
        changeTopPos(list);
        Collections.sort(list, new RecommentAppLockListActivity.DefalutAppComparator());
        int theSize =list.size();
        List<AppItemInfo> highList = new ArrayList<AppItemInfo>();
        for (AppItemInfo info: list) {
            if (info.topPos > -1) {
                highList.add(info);
            }
        }
        ArrayList arrayList = new ArrayList();
        StringBuffer appNameBuilder = new StringBuffer();
        int theHighSize = highList.size();

        if (theHighSize == 0) {  //无推荐应用未加锁
            arrayList.add(context.getResources().getString(R.string.scan_app_content_zero));
            arrayList.add(context.getResources().getString(R.string.scan_app_content_zero));
           return arrayList;

        } else if (theHighSize == 1 && theSize == 1) { // 只有1个应用且是推荐应用
            arrayList.add(context.getResources().getString(R.string.scan_app_content_only_high));
            arrayList.add(context.getResources().getString(R.string.scan_app_content_only_high));
            return arrayList;

        } else if (theHighSize == 1 && theSize > 1) { // 有1个推荐应用和其他应用
            for (AppItemInfo appItemInfo:highList) {
                appNameBuilder.append(appItemInfo.label).append(",");
            }
            String appName = appNameBuilder.toString().substring(0, appNameBuilder.length() - 1);
            arrayList.add(context.getResources().getString(
                    R.string.scan_app_content_less_three, appName));
            arrayList.add(context.getResources().getString(
                    R.string.scan_app_content_less_three_app, appName));
            return  arrayList;

        } else if (theHighSize == 2 && theSize == 2) {  // 有2个应用且都是推荐应用
            arrayList.add(context.getResources().getString(R.string.scan_app_content_only_high));
            arrayList.add(context.getResources().getString(R.string.scan_app_content_only_high));
            return arrayList;

        } else if (theHighSize == 2 && theSize > 2) { // 有2个推荐应用和其他应用
            for (AppItemInfo appItemInfo:highList) {
                appNameBuilder.append(appItemInfo.label).append(",");
            }
            String appName = appNameBuilder.toString().substring(0, appNameBuilder.length() - 1);
            arrayList.add(context.getResources().getString(
                    R.string.scan_app_content_less_three, appName));
            arrayList.add(context.getResources().getString(
                    R.string.scan_app_content_less_three_app, appName));
            return  arrayList;

        } else if (theHighSize == 3 && theSize == 3) { // 有3个应用且都是推荐应用
            arrayList.add(context.getResources().getString(R.string.scan_app_content_only_high));
            arrayList.add(context.getResources().getString(R.string.scan_app_content_only_high));
            return arrayList;

        } else if (theHighSize == 3 && theSize > 3) { // 有3个推荐应用和其他应用
            for (AppItemInfo appItemInfo:highList) {
                appNameBuilder.append(appItemInfo.label).append("、");
            }
            String appName = appNameBuilder.toString().substring(0, appNameBuilder.length() - 1);
            arrayList.add(context.getResources().getString(
                    R.string.scan_app_content_less_three, appName));
            arrayList.add(context.getResources().getString(
                    R.string.scan_app_content_less_three_app, appName));
            return  arrayList;

        } else if (theHighSize > 3) { //  超过3个推荐应用
            HashSet<Integer> set = new HashSet<Integer>();
//            randomSet(theHighSize, 3, set);
//            Iterator<Integer> iterator = set.iterator(); //迭代遍历
//            while (iterator.hasNext()) {
//                appNameBuilder.append(highList.get(iterator.next()).label).append(",");
//            }
            for (int i = 0; i < 3; i++) {
                appNameBuilder.append(highList.get(i).label).append(",");
            }
            String appName = appNameBuilder.toString().substring(0, appNameBuilder.length() - 1);
            arrayList.add(context.getResources().getString(
                    R.string.scan_app_content, appName));
            arrayList.add(context.getResources().getString(
                    R.string.scan_app_content_app, appName));
            return  arrayList;
        }

        arrayList.add("");
        arrayList.add("");
        return arrayList;

    }

    /** 获得随机数集合 */
    private static void randomSet(int size, int n, HashSet<Integer> set) {
        int length = 3;

        for (int i = 0; i < n; i++) {
            Random random = new Random();
            int num = random.nextInt(size);
            set.add(num); // 将不同的数存入HashSet中
        }
        int setSize = set.size();

        if (setSize < length) {
            randomSet(size, length - setSize, set);// 递归
        }
    }
}
