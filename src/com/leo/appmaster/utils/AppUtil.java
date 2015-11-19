
package com.leo.appmaster.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.model.AppItemInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class AppUtil {
    private static final String TAG = "AppUtil";
    public static final float SPL_SHARE_SCALE_X = 1.0f;
    public static final float SPL_SHARE_SCALE_Y = 1.0f;

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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            ctx.startActivity(intent);
        } catch (Exception e) {
        }
    }

    public static void downloadFromBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
        }
    }

    public static void downloadFromGp(Context context, String packageGp) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + packageGp));
        intent.setPackage(Constants.GP_PACKAGE);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
        }
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

    public static Drawable getAppIcon(PackageManager pm, String pkg) {
        Drawable d = AppLoadEngine.getInstance(AppMasterApplication.getInstance()).getAppIcon(pkg);
        if (d == null) {
            d = loadAppIconDensity(pkg);
        }
        return d;
    }

    public static String getAppLabel(PackageManager pm, String pkg) {
        String label = null;
        AppItemInfo app = AppLoadEngine.getInstance(AppMasterApplication.getInstance()).getAppInfo(pkg);
        if (app != null) {
            label = app.label;
        }
        if (Utilities.isEmpty(label)) {
            try {
                label = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
            } catch (Exception e) {
                label = "";
            }
        }
        return label;
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
            src.setFilterBitmap(true);
            src.draw(c);
        }

        int size = ctx.getResources().getDimensionPixelSize(R.dimen.app_size);
//        LeoLog.d("testBug","size is : " + size);

        if (bitmap != null) {
            if (bitmap.getWidth() > size || bitmap.getHeight() > size) {
                bitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
            } else {
                return src;
            }
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

    /**
     * 分享图片
     *
     * @param photoUri
     * @return 返回Intent 需要指定分享具体客户端的自己添加处理
     */
    public static Intent shareImageToApp(String photoUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        File file = new File(photoUri);
        Uri uri = Uri.fromFile(file);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        return shareIntent;
    }

    /**
     * 判断是否安装指定应用，true：安装，false：未安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isInstallPkgName(Context context, String packageName) {
        Context ctx = context.getApplicationContext();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setPackage(packageName);
        List<ResolveInfo> resolveInfo = ctx.getPackageManager().queryIntentActivities(
                intent, 0);
        if (resolveInfo.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 分享闪屏图片拼接
     *
     * @param onePath
     * @return
     */
    public static Bitmap add2Bitmap(String onePath, int id) {
        /*图片1*/
        BitmapFactory.Options optionOne = new BitmapFactory.Options();
        optionOne.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(onePath, optionOne);
        optionOne.inSampleSize = 3;
        optionOne.inJustDecodeBounds = false;
        Bitmap oneImage = BitmapFactory.decodeFile(onePath, optionOne);
        /*图片2*/
        Bitmap twoImage = BitmapFactory.decodeResource(AppMasterApplication.getInstance().getResources(), id);

        Matrix matrix = new Matrix();
        float oneScaleY = SPL_SHARE_SCALE_Y;
        float oneScaleX = SPL_SHARE_SCALE_X;
        matrix.postScale(oneScaleX, oneScaleY);
        /*缩放图1*/
        //x + width must be <= bitmap.width()
        oneImage = Bitmap.createBitmap(oneImage, 0, 0, oneImage.getWidth(), oneImage.getHeight(), matrix, true);
         /*创建拼接Bitmap*/
        int resultWidth = oneImage.getWidth();
        int resultHeight = oneImage.getHeight() + oneImage.getWidth();
        Bitmap result = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(oneImage, 0, 0, null);
        /*缩放图2*/
        twoImage = Bitmap.createScaledBitmap(twoImage, oneImage.getWidth(), oneImage.getWidth(), true);
        canvas.drawBitmap(twoImage, 0, oneImage.getHeight(), null);
        oneImage.recycle();
        twoImage.recycle();
        return result;
    }

    /*保存图片到指定路径*/
    public static boolean outPutImage(String path, Bitmap bitmap) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.createNewFile();
            }
            FileOutputStream fout = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
            fout.flush();
            fout.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*计算InSampleSize*/
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        /*源图片的高度和宽度*/
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if ((height > reqHeight && height / reqHeight >= 2) || (width > reqWidth && width / reqWidth >= 2)) {
            /*计算出实际宽高和目标宽高的比率*/
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            /*选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高 一定都会大于等于目标的宽和高。*/
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }


    /*屏幕宽,高*/
    public static int[] getScreenPix(Context context) {
        int[] pix = new int[2];
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        pix[0] = width;
        int height = displayMetrics.heightPixels;
        pix[1] = height;
        return pix;
    }
}
