
package com.leo.appmaster.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.net.Uri;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;

public class AppUtil {
    public static boolean isSystemApp(ApplicationInfo info) {
        // 有些系统应用是可以更新的，如果用户自己下载了一个系统的应用来更新了原来的，
        // 它就不是系统应用，这个就是判断这种情况的
        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return true;
        } else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0)// 判断是不是系统应用
        {
            return true;
        }
        return false;
    }

    public static boolean appInstalled(Context ctx, String pkg) {

        PackageManager pm;
        try {
            pm = ctx.getPackageManager();
            pm.getApplicationInfo(pkg, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void uninstallApp(Context ctx, String pkg) {
        Uri uri = Uri.fromParts("package", pkg, null);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        ctx.startActivity(intent);
    }

    public static void downloadFromBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    public static void downloadFromGp(Context context, String packageGp) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + packageGp));
        intent.setPackage(Constants.GP_PACKAGE);
        context.startActivity(intent);
    }

    public static ApplicationInfo getApplicationInfo(String pkg, Context ctx) {
        ApplicationInfo info = null;
        try {
            ctx.getPackageManager().getApplicationInfo(pkg,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    public static String getAppLabel(String pkg, Context ctx) {
        try {
            return ctx
                    .getPackageManager()
                    .getApplicationLabel(
                            ctx.getPackageManager().getApplicationInfo(pkg, 0))
                    .toString();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isInstalledInSDcard(ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
            return true;
        }
        return false;
    }

    public long getMobileTraffic() {
        return TrafficStats.getMobileRxBytes()
                + TrafficStats.getMobileTxBytes();
    }

    public long getWifiTraffic() {
        long totalTraffic = TrafficStats.getTotalRxBytes()
                + TrafficStats.getTotalTxBytes();
        return totalTraffic - getMobileTraffic();
    }

    public static Drawable getDrawable(PackageManager pm, String pkg) {
        Drawable d = AppLoadEngine.getInstance(AppMasterApplication.getInstance()).getAppIcon(pkg);
        if (d == null) {
            d = loadAppIconDensity(pkg);
        }
        return d;
    }

    /**
     * 获取app图标并缩放至app指定大小
     * 
     * @param pkg
     * @return
     */
    public static Drawable loadAppIconDensity(String pkg) {
        Context ctx = AppMasterApplication.getInstance();

        PackageManager pm = ctx.getPackageManager();
        Drawable appicon = null;
        try {
            appicon = pm.getApplicationIcon(pkg);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (Error error) {
            
        }
        
        if (appicon == null) {
            return appicon;
        }

        return getScaledAppIcon(appicon);
    }

    /**
     * 缩放appicon到指定大小
     * 
     * @param src
     * @return
     */
    public static Drawable getScaledAppIcon(Drawable src) {
        if (src == null) return null;
        
        Context ctx = AppMasterApplication.getInstance();

        Bitmap bitmap = null;
        if (src instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) src).getBitmap();
        } else {
            int width = src.getIntrinsicWidth();
            int height = src.getIntrinsicHeight();
            if (width <= 0 || height <= 0) {
                Rect bounds = src.getBounds();
                if (bounds != null) {
                    width = bounds.width();
                    height = bounds.height();
                }

                if (width <= 0 || height <= 0) {
                    // 如果通过bounds还是无法获取宽高信息，就直接返回
                    return src;
                }
            }

            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            src.setBounds(0, 0, width, height);
            Canvas c = new Canvas(bitmap);
            src.draw(c); 
        }

        int size = ctx.getResources().getDimensionPixelSize(R.dimen.app_size);
        if (bitmap.getWidth() > size || bitmap.getHeight() > size) {
            bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
        } else {
            return src;
        }

        return new BitmapDrawable(ctx.getResources(), bitmap);
    }

    public static long getTotalTriffic() {
        return TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
    }

    /* 判断打开屏幕是否存在解锁界面 */
    public static boolean isScreenLocked(Context mContext) {
        KeyguardManager mKeyguardManager = (KeyguardManager) mContext
                .getSystemService(mContext.KEYGUARD_SERVICE);
        return mKeyguardManager.inKeyguardRestrictedInputMode();
    }
}
