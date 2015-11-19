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

        boolean hasFb = AppUtil.isInstallPkgName(context, Constants.PKG_FACEBOOK);
        Intent intent = new Intent();
        if (hasFb) {
            ComponentName cn = new ComponentName(Constants.PKG_FACEBOOK,
                    "com.facebook.katana.IntentUriHandler");
            intent.setComponent(cn);
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
}
