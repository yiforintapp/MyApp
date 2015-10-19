package com.leo.appmaster.quickgestures;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.engine.AppLoadEngine;

public class IswipeManager {
    
    
    /* 判断是否安装ISwipe，true：安装，false：未安装 */
    public static boolean isInstallIsiwpe(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setPackage(AppLoadEngine.ISWIPE_PACKAGENAME);
        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(
                intent, 0);
        if (resolveInfo != null && resolveInfo.size() > 0) {
            return true;
        }
        return false;
    }
    
    /* iswip下载处理 */
    public static void iSwipDownLoadHandler() {
        Context context = AppMasterApplication.getInstance();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(Constants.ISWIPE_TO_GP_CLIENT_RUL);
        intent.setData(uri);
        intent.setPackage("com.android.vending");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            uri = Uri.parse(Constants.ISWIPE_TO_GP_BROWSER_RUL);
            intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
            } catch (Exception ex) {
                
            }
        }
        
    }

}
