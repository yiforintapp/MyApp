package com.leo.appmaster.msgcenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.leo.appmaster.Constants;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;

/**
 * Created by Jasper on 2015/11/18.
 */
public class MsgUtil {
    private static final String TAG = "MsgUtil";

    public static void openFacebook(String url, Context context) {
        Uri uri = null;
        try {
            uri = Uri.parse(url);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String schema = uri.getScheme();
        if (TextUtils.isEmpty(schema)) {
            LeoLog.i(TAG, "openFacebook, schema is empty.");
            return;
        }

        boolean hasFb = AppUtil.isInstallPkgName(context, Constants.PKG_FACEBOOK);
        Intent intent = new Intent();
        if (hasFb) {
            ComponentName cn = new ComponentName(Constants.PKG_FACEBOOK,
                    "com.facebook.katana.IntentUriHandler");
            intent.setComponent(cn);
            LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            mLockManager.filterPackage(Constants.PKG_FACEBOOK, 1000);
        }
        intent.setData(uri);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openGooglePlay(String url, Context context) {
        Uri uri = null;
        try {
            uri = Uri.parse(url);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String schema = uri.getScheme();
        if (TextUtils.isEmpty(schema)) {
            LeoLog.i(TAG, "openGooglePlay, schema is empty.");
            return;
        }

        boolean hasGp = AppUtil.isInstallPkgName(context, Constants.PKG_GOOLEPLAY);
        Intent intent = new Intent();
        if (hasGp) {
            intent.setPackage(Constants.PKG_GOOLEPLAY);
            LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
            mLockManager.filterPackage(Constants.PKG_GOOLEPLAY, 1000);
        }
        intent.setData(uri);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * app推广，已安装，直接打开，未安装有GP跳转GP，无GP跳转浏览器打开
     * @param url App下载地址
     * @param pkg
     *
     * @return
     */
    public static boolean openPromotion(Context context, String url, String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            LeoLog.d(TAG, "openPromotion, pkg is null.");
            return false;
        }

        Intent intent = null;
        boolean pkgInstalled = AppUtil.isInstallPkgName(context, pkg);
        if (pkgInstalled) {
            PackageManager pm = context.getPackageManager();
            intent = pm.getLaunchIntentForPackage(pkg);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            boolean hasGp = AppUtil.isInstallPkgName(context, Constants.PKG_GOOLEPLAY);
            if (hasGp) {
                intent.setPackage(Constants.PKG_GOOLEPLAY);
                StringBuilder dataBuilder = new StringBuilder("https://play.google.com/store/apps/details?id=")
                            .append(pkg)
                            .append("&referrer=utm_source=PG_MSGCENTER");

                intent.setData(Uri.parse(dataBuilder.toString()));

                LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                mLockManager.filterPackage(Constants.PKG_GOOLEPLAY, 1000);

            } else {
                // 无gp，直接使用浏览器打开下载链接
                if (TextUtils.isEmpty(url)) {
                    return false;
                }

                intent.setData(Uri.parse(url));
            }
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
