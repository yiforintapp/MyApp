
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.model.BaseInfo;
import com.leo.appmaster.model.FolderItemInfo;
import com.leo.appmaster.sdk.SDKWrapper;

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

    public static Drawable getFolderScalePicture(Context context,
                                                 List<AppItemInfo> folderList, int type) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Resources res = context.getResources();
        int iconWidth = res.getDimensionPixelSize(R.dimen.app_size);
        Bitmap folderPic = Bitmap.createBitmap(iconWidth, iconWidth,
                Bitmap.Config.ARGB_8888);
        if (type == FolderItemInfo.FOLDER_BUSINESS_APP) {
            BitmapDrawable resault = (BitmapDrawable) res
                    .getDrawable(R.drawable.folder_icon_recommend);

            AppMasterPreference pref = AppMasterPreference.getInstance(context);
            String online = pref.getOnlineBusinessSerialNumber();
            String local = pref.getLocalBusinessSerialNumber();
            if (online != null && !online.equals(local)) {
                Bitmap newTipBitamp = BitmapFactory.decodeResource(res,
                        R.drawable.folder_new_icon);
                int tipSize = newTipBitamp.getWidth();

                Canvas canvas = new Canvas(folderPic);
                canvas.drawBitmap(resault.getBitmap(), 0, 0, paint);
                canvas.drawBitmap(newTipBitamp, iconWidth - tipSize, 0, paint);

                String newTag = context.getString(R.string.folder_new);

                Rect rect = new Rect();
                paint.setTextSize(tipSize / 2.8f);
                paint.getTextBounds(newTag, 0, newTag.length(), rect);
                int textWidth = rect.width();
                int textHeight = rect.height();

                float x = iconWidth - tipSize + (tipSize - textWidth) / 2;
                float y = (tipSize - textHeight) / 2 + tipSize / 5.5f;
                paint.setColor(Color.WHITE);
                canvas.drawText(newTag, x, y, paint);

                BitmapDrawable dd = new BitmapDrawable(res, folderPic);
                return dd;
            } else {
                return resault;
            }
        }
        Drawable folderBg;
        if (type == FolderItemInfo.FOLDER_BACKUP_RESTORE) {
            if (folderList == null || folderList.isEmpty()) {
                folderBg = res
                        .getDrawable(R.drawable.backup_folder_empty_bg_icon);
            } else {
                folderBg = res.getDrawable(R.drawable.common_folder_bg_icon);
            }
        } else {
            folderBg = res.getDrawable(R.drawable.common_folder_bg_icon);
        }

        folderBg.setAlpha(255);
        final int picWidth = folderBg.getIntrinsicWidth();
        final int picHeight = folderBg.getIntrinsicHeight();

        Canvas canvas = new Canvas(folderPic);

        canvas.save();
        if (iconWidth != picWidth || iconWidth != picHeight) {
            canvas.scale((float) iconWidth / picWidth, (float) iconWidth
                    / picHeight);
        }
        folderBg.setBounds(0, 0, picWidth, picHeight);
        folderBg.draw(canvas);
        canvas.restore();

        int row_num = 2;
        int folder_icon_x = (int) (iconWidth / 9.5);
        int folder_icon_y = folder_icon_x;
        int folder_icon_h = folder_icon_x;
        int folder_icon_v = folder_icon_x;
        int folder_icon_size = (iconWidth - (row_num + 1) * folder_icon_x)
                / row_num;

        int x = folder_icon_x;
        int y = folder_icon_y;

        for (int i = 0; i < folderList.size(); i++) {
            if (i >= MAX_ICON)
                break;
            BaseInfo info = folderList.get(i);
            int originW = info.icon.getIntrinsicWidth();
            int originH = info.icon.getIntrinsicHeight();
            float scaleW = ((float) folder_icon_size) / originW;
            float scaleH = ((float) folder_icon_size) / originH;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleW, scaleH);
            BitmapDrawable bd = (BitmapDrawable) info.icon;
            Bitmap s = Bitmap.createBitmap(bd.getBitmap(), 0, 0, originW,
                    originH, matrix, true);

            // as the content is already sorted, just draw them by order
            int cellX = i / 2;
            int cellY = i / 2;
            if (i == 0) {
                cellX = 0;
                cellY = 0;
            } else if (i == 1) {
                cellX = 1;
                cellY = 0;
            } else if (i == 2) {
                cellX = 0;
                cellY = 1;
            } else if (i == 1) {
                cellX = 1;
                cellY = 1;
            }

            x = folder_icon_x + cellX * (folder_icon_size + folder_icon_h);
            y = folder_icon_y + cellY * (folder_icon_size + folder_icon_v);
            canvas.drawBitmap(s, (float) x, (float) y, null);

            s.recycle();
        }

        BitmapDrawable dd = new BitmapDrawable(res, folderPic);
        return dd;
    }

    public static int[] getScreenSize(Context ctx) {
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);
        Display displayer = wm.getDefaultDisplay();
        int size[] = new int[2];
        size[0] = displayer.getWidth();
        size[1] = displayer.getHeight();
        return size;
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
        return "http://" + SDKWrapper.getBestServerDomain() + suffix;
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

    public static void goFiveStar(Context context, boolean isFromCard, boolean isFromPrivacy) {
        String url = Constants.RATING_ADDRESS_BROWSER;
        PreferenceTable preferenceTable = PreferenceTable.getInstance();
        if (isFromCard && isFromPrivacy) {
            if (preferenceTable.getString(PrefConst.KEY_PRI_GRADE_URL) != null &&
                    preferenceTable.getString(PrefConst.KEY_PRI_GRADE_URL).length() > 0) {
                url = preferenceTable.getString(PrefConst.KEY_PRI_GRADE_URL);
            }
        } else if (isFromCard && !isFromPrivacy) {
            if (preferenceTable.getString(PrefConst.KEY_WIFI_GRADE_URL) != null &&
                    preferenceTable.getString(PrefConst.KEY_WIFI_GRADE_URL).length() > 0) {
                url = preferenceTable.getString(PrefConst.KEY_WIFI_GRADE_URL);
            }
        }
        Intent intent = null;
        if (AppUtil.appInstalled(context,
                "com.android.vending")) {

            LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            mLockManager.filterPackage(Constants.PKG_GOOLEPLAY, 1000);

            intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri
                    .parse(url);
            intent.setData(uri);
            intent.setPackage("com.android.vending");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
                LeoLog.i("goFiveStar", "intent: " + intent.toURI());
            } catch (Exception e) {
                if (isRedMiTwo()) {
                    goRedMiTwoBrowser(url, context);
                } else {
                    goGpBrowser(url, context);
                }

            }
        } else {
            if (isRedMiTwo()) {
                goRedMiTwoBrowser(url, context);
            } else {
                goGpBrowser(url, context);
            }
        }
    }

    private static void goGpBrowser(String url, Context context) {
        Intent intent;
        intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            LeoLog.i("goFiveStar", "intent: " + intent.toURI());
        } catch (Exception e) {
        }
    }

    /**
     * 是否为红米Note2手机
     */
    private static boolean isRedMiTwo() {
        if (Constants.RED_MI_TWO_NAME.equals(android.os.Build.MODEL)) {
            return true;
        }
        return false;
    }

    /**
     * 红米Note2 进入红米默认浏览器
     */
    private static void goRedMiTwoBrowser(String url, Context context) {
        Intent intent;
        intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
        try {
            context.startActivity(intent);
            LeoLog.i("goFiveStar", "intent: " + intent.toURI());
        } catch (Exception e) {
            goGpBrowser(url, context);
        }
    }

    /**
     * 前往FaceBook
     */
    public static void goFaceBook(Context context, boolean isFromPrivacy) {
        Intent intentLikeUs = null;
        String url = Constants.FACEBOOK_PG_URL;
        PreferenceTable preferenceTable = PreferenceTable.getInstance();
        if (isFromPrivacy) {
            if (preferenceTable.getString(PrefConst.KEY_PRI_FB_URL) != null &&
                    preferenceTable.getString(PrefConst.KEY_PRI_FB_URL).length() > 0) {
                url = preferenceTable.getString(PrefConst.KEY_PRI_FB_URL);
            }
        } else {
            if (preferenceTable.getString(PrefConst.KEY_WIFI_FB_URL) != null &&
                    preferenceTable.getString(PrefConst.KEY_WIFI_FB_URL).length() > 0) {
                url = preferenceTable.getString(PrefConst.KEY_WIFI_FB_URL);
            }
        }
        if (AppUtil.appInstalled(context.getApplicationContext(),
                Constants.FACEBOOK_PKG_NAME)) {
            intentLikeUs = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(url);
            intentLikeUs.setData(uri);
            ComponentName cn = new ComponentName(Constants.FACEBOOK_PKG_NAME,
                    Constants.FACEBOOK_CLASS);
            intentLikeUs.setComponent(cn);
            intentLikeUs.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {

                LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                mLockManager.filterPackage(Constants.PKG_FACEBOOK, 1000);

                context.startActivity(intentLikeUs);
            } catch (Exception e) {
            }
        } else {
            intentLikeUs = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(url);
            intentLikeUs.setData(uri);
            intentLikeUs.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intentLikeUs);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 通过包名打开已安装的wifimaster
     */
    public static void openAPPByPkgName(Context context, String pkgName) {
        Intent intent = null;
        PackageManager pm = context.getPackageManager();
        intent = pm.getLaunchIntentForPackage(pkgName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void startISwipIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        ComponentName cn = new ComponentName(Constants.ISWIPE_PACKAGE,
                "com.leo.iswipe.activity.QuickGestureActivity");
        intent.setComponent(cn);
        boolean iswipeFirstTip = AppMasterPreference.getInstance(context)
                .getFristSlidingTip();
        if (iswipeFirstTip) {
            intent.putExtra(Constants.PG_TO_ISWIPE, Constants.ISWIPE_FIRST_TIP);
        } else {
            intent.putExtra(Constants.PG_TO_ISWIPE, Constants.ISWIPE_NO_FIRST_TIP);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 前往Gp或亚马逊云
     */
    public static void gotoGpOrBrowser(Context context, String from, boolean isFromPrivacy) {
        PreferenceTable preferenceTable = PreferenceTable.getInstance();
        if (isFromPrivacy) {  //点击隐私页按钮
            if (Constants.IS_CLICK_SWIFTY.equals(from)) {  // 点击swifty

                selectType(preferenceTable, PrefConst.KEY_SWIFTY_TYPE, PrefConst.KEY_SWIFTY_GP_URL,
                        PrefConst.KEY_SWIFTY_URL, "", context);

            } else { //点击wifimaster

                selectType(preferenceTable, PrefConst.KEY_PRI_WIFIMASTER_TYPE,
                        PrefConst.KEY_PRI_WIFIMASTER_GP_URL, PrefConst.KEY_PRI_WIFIMASTER_URL,
                        "", context);
            }
        } else {
            if (Constants.IS_CLICK_SWIFTY.equals(from)) {  // 点击swifty

                selectType(preferenceTable, PrefConst.KEY_WIFI_SWIFTY_TYPE,
                        PrefConst.KEY_WIFI_SWIFTY_GP_URL, PrefConst.KEY_WIFI_SWIFTY_URL,
                        "", context);

            } else { //点击wifimaster

                selectType(preferenceTable, PrefConst.KEY_WIFI_WIFIMASTER_TYPE,
                        PrefConst.KEY_WIFI_WIFIMASTER_GP_URL, PrefConst.KEY_WIFI_WIFIMASTER_URL,
                        "", context);
            }
        }
    }


    public static void selectType(PreferenceTable preferenceTable, String type, String gpUrl,
                                  String url, String pkgName, Context context) {

        if (Constants.BROWSER_URL_TYPE.equals(
                preferenceTable.getString(type))) { // 使用浏览器

            browserType(preferenceTable, url, pkgName, context);

        } else {  // 使用gp

            gpType(preferenceTable, gpUrl, url, pkgName, context);
        }
    }

    private static void browserType(PreferenceTable preferenceTable,
                                    String key, String pkgName, Context context) {

        if (preferenceTable.getString(key) != null &&
                preferenceTable.getString(key).length() > 0) {

            String browserUrl = preferenceTable.getString(key);
            gotoBrowser(browserUrl, pkgName, context);
        } else { // 使用包名跳转gp商店
            gotoGpByPkg(pkgName, context);
        }
    }

    private static void gpType(PreferenceTable preferenceTable, String gpUrlKey,
                               String urlKey, String pkgName, Context context) {

        String browserUrl;
        if (preferenceTable.getString(gpUrlKey) != null &&
                preferenceTable.getString(gpUrlKey).length() > 0) {
            String gpUrl = preferenceTable.getString(gpUrlKey);

            if (preferenceTable.getString(urlKey) != null &&
                    preferenceTable.getString(urlKey).length() > 0) {

                browserUrl = preferenceTable.getString(urlKey);
            } else {
                browserUrl = "";
            }
            gotoGp(gpUrl, browserUrl, pkgName, context);
        } else {
            browserType(preferenceTable, urlKey, pkgName, context);
        }
    }

    /**
     * 使用Gp,没有Gp用浏览器
     */
    private static void gotoGp(String gpUrl, String browserUrl, String pkgName, Context context) {
        Intent intent = null;
        if (AppUtil.appInstalled(context, Constants.GP_PKG_NAME)) {
            intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(gpUrl);
            intent.setData(uri);
            intent.setPackage(Constants.GP_PKG_NAME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                mLockManager.filterPackage(Constants.PKG_GOOLEPLAY, 1000);
                context.startActivity(intent);
            } catch (Exception e) {
                if (!"".equals(browserUrl)) {
                    gotoBrowser(browserUrl, pkgName, context);
                }
            }
        } else {
            if (!"".equals(browserUrl)) {
                gotoBrowser(browserUrl, pkgName, context);
            }
        }
    }

    /**
     * 是使用浏览器
     */
    private static void gotoBrowser(String url, String pkgName, Context context) {
        Intent intent;
        intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            gotoGpByPkg(pkgName, context);
        }
    }

    /**
     * 使用包名跳转Gp
     */
    private static void gotoGpByPkg(String pkgName, Context context) {
        if (TextUtils.isEmpty(pkgName)) { //包名为空不执行
            return;
        }
        if (AppUtil.appInstalled(context, Constants.GP_PKG_NAME)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            StringBuffer url = new StringBuffer(Constants.FIRST_GP_STRING);
            url.append(pkgName).append(Constants.LAST_GP_STRING);
            Uri uri = Uri.parse(url.toString());
            intent.setData(uri);
            intent.setPackage(Constants.GP_PKG_NAME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                mLockManager.filterPackage(Constants.PKG_GOOLEPLAY, 1000);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //A to a
    public static String exChange(String str) {
        StringBuffer sb = new StringBuffer();
        if (str != null) {
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                if (Character.isUpperCase(c)) {
                    sb.append(Character.toLowerCase(c));
                } else if (Character.isLowerCase(c)) {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    public static int getScanContentHeight(View v, Context context) {
        try {
            Method m = v.getClass().getDeclaredMethod("onMeasure", int.class,
                    int.class);
            m.setAccessible(true);
            m.invoke(v, View.MeasureSpec.makeMeasureSpec(
                    ((View) v.getParent()).getMeasuredWidth(),
                    View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0,
                    View.MeasureSpec.UNSPECIFIED));
        } catch (Exception e) {

        }

        return DipPixelUtil.px2dip(context, v.getMeasuredHeight());
    }

    public static void toShareApp(String shareString, String title, Context context) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
//                intent.setType("image/*");  //新浪微博只能使用这种type
        intent.putExtra(Intent.EXTRA_TEXT, shareString);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, title));
    }

}
