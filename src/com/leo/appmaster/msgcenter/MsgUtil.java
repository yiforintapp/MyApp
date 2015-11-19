package com.leo.appmaster.msgcenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.leo.appmaster.Constants;
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

//        boolean hasFb = AppUtil.isInstallPkgName(context, Constants.PKG_FACEBOOK);
        Intent intent = new Intent();
        if (schema.equals("fb")) {
            if (AppUtil.isInstallPkgName(context, Constants.PKG_FACEBOOK)) {
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(uri);
                ComponentName cn = new ComponentName(Constants.PKG_FACEBOOK,
                        "com.facebook.katana.IntentUriHandler");
                intent.setComponent(cn);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                String id = url.substring(url.lastIndexOf("/") + 1);
                uri = Uri.parse("https://www.facebook.com/pages/App-Master/" + id);
                intent.setData(uri);
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        } else if (schema.equals("https") || schema.equals("http")) {
            intent.setData(uri);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

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

        Intent intent = new Intent();
        if (schema.equals("market")) {
            if (AppUtil.isInstallPkgName(context, Constants.PKG_GOOLEPLAY)) {
                intent.setAction(Intent.ACTION_VIEW);
                intent.setPackage(Constants.PKG_GOOLEPLAY);
                intent.setData(uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                String suffix = url.substring(url.indexOf("?id=") + 1);
                uri = Uri.parse("https://play.google.com/store/apps/details?" + suffix);
                intent.setData(uri);
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        } else if (schema.equals("https") || schema.equals("http")) {
            intent.setData(uri);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
