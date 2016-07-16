
package com.leo.appmaster.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Utilities {

    private static final int MAX_ICON = 4;
    public static int mCurrentCreenChangeStatus = 1;
    public final static String PKG_SYSTEM_UI = "com.android.systemui";
    public final static String APP_URL_KEY = "#Intent;component="; // 应用功能推广标识
    public final static int LESS_THIRTY_VERSION_CODE= 60; // 3.0release版本的最低versionCode

    public static int[] getScreenSize(Context ctx) {
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);
        Display displayer = wm.getDefaultDisplay();
        int size[] = new int[2];
        size[0] = displayer.getWidth();
        size[1] = displayer.getHeight();
        return size;
    }

    public static boolean hasNavigationBar(Context ctx) {
        boolean hasNavigationBar = false;
        Resources rs = ctx.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
        }

        return hasNavigationBar;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            return toHex(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "NUHC";
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

    public static boolean isPackageInsalled(Context context, String packageName) {
        if (packageName == null)
            return false;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            if (info != null) {
                return true;
            }
        } catch (NameNotFoundException e) {
            return false;
        }
        return false;
    }

    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }

        return false;
    }

    public static String trimString(String s) {
        return s.replaceAll("\u00A0", "").trim();
    }

    public static String getURL(String suffix) {
        // LeoLog.d("httpurl", "gameFragment Http is :::"+"http://" +
        // SDKWrapper.getBestServerDomain() + suffix);

        // return "http://api.leomaster.com" + suffix;
        return "http://" + suffix;
    }

    public static String getCountryID(Context context) {
        // AM-1764 android.os.TransactionTooLargeException
        TelephonyManager tm;
        try {
            tm = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String id = tm.getSimCountryIso();
            if (isEmpty(id)) {
                id = Locale.getDefault().getCountry();
            }
            if (id == null) {
                id = "d";
            }
            id = id.toLowerCase();
            return id;
        } catch (Exception e) {
            return "d";
        }

    }

    // 判断当前是否为桌面
    public static boolean isHome(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);

        List<String> names = new ArrayList<String>();
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names.contains(rti.get(0).topActivity.getPackageName());
    }

    // 横竖屏判断
    public static int isScreenType(Context context) {
        Configuration configuiation = context.getResources().getConfiguration();
        int ori = configuiation.orientation;
        if (ori == configuiation.ORIENTATION_LANDSCAPE) {
            // 横屏
            mCurrentCreenChangeStatus = -1;
            return -1;
        } else if (ori == configuiation.ORIENTATION_PORTRAIT) {
            // 竖屏
            mCurrentCreenChangeStatus = 1;
            return 1;
        }
        return 0;
    }

    // 判断屏幕是否发生改变
    public static boolean isScreenChange(Context context) {
        Configuration configuiation = context.getResources().getConfiguration();
        int ori = configuiation.orientation;
        if (ori == configuiation.ORIENTATION_LANDSCAPE) {
            if (mCurrentCreenChangeStatus != -1) {
                mCurrentCreenChangeStatus = -1;
                return true;
            }
        } else if (ori == configuiation.ORIENTATION_PORTRAIT) {
            if (mCurrentCreenChangeStatus != 1) {
                mCurrentCreenChangeStatus = 1;
                return true;
            }
        }
        return false;
    }

    @SuppressLint("NewApi")
    public static boolean isActivityOnTop(Context context, String ActivityName) {
        if (!isAppOnTop(context)) {
            return false;
        }
        /* now our Application on top, check activity */
        if (Build.VERSION.SDK_INT > 19) {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            try {
                List<ActivityManager.AppTask> tasks = am.getAppTasks();
                if (tasks != null && tasks.size() > 0) {
                    ActivityManager.RecentTaskInfo rti = tasks.get(0).getTaskInfo();
                    if (rti != null) {
                        Intent intent = rti.baseIntent;
                        ComponentName cn = intent.getComponent();
                        if (cn != null && cn.getClassName().equals(context.getClass().getName())) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        } else {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
            if (cn.getClassName().equals(ActivityName)) {
                return true;
            }
            return false;
        }
    }

    public static boolean isAppOnTop(Context context) {
        if (Build.VERSION.SDK_INT > 19) {
            return isAppOnTopAfterLolipop(context);
        } else {
            return isAppOnTopBeforeLolipop(context);
        }
    }

    private static boolean isAppOnTopAfterLolipop(Context context) {
        // Android L and above
        String pkgName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo pi : list) {
            if (pi.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE // Foreground
                    // or
                    // Visible
                    && pi.importanceReasonCode == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN // Filter
                    // provider
                    // and
                    // service
                    && (0x4 & pi.flags) > 0) { // Must have activities
                String pkgList[] = pi.pkgList;
                if (pkgList != null && pkgList.length > 0) {
                    if (pkgList[0].equals(PKG_SYSTEM_UI)) {
                        continue;
                    }
                    pkgName = pkgList[0];
                }
            }
        }

        if (pkgName == null || !pkgName.equals(context.getPackageName())) {
            return false;
        }

        return true;
    }

    private static boolean isAppOnTopBeforeLolipop(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName)
                && currentPackageName.equals(context.getPackageName())) {
            return true;
        }
        return false;
    }


}
