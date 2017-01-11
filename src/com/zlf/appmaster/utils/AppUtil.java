
package com.zlf.appmaster.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.AppMasterPreference;
import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.db.LeoSettings;
import com.zlf.appmaster.utils.LeoLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppUtil {
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


    /**
     * 获取app图标Drawble
     *
     * @param pkg
     * @return
     */
    public static Drawable getAppIconDrawble(String pkg) {
        Context ctx = AppMasterApplication.getInstance();

        PackageManager pm = ctx.getPackageManager();
        Drawable appicon = null;
        try {
            appicon = pm.getApplicationIcon(pkg);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (Error error) {

        }

        return appicon;
    }

    /**
     * 获取app图标并缩放至app指定大小
     *
     * @param pkg
     * @return
     */
    public static Drawable loadAppIconDensity(String pkg) {
        Drawable appicon = getAppIconDrawble(pkg);

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
        int[] pix = AppUtil.getScreenPix(AppMasterApplication.getInstance());
        int width = pix[0];
        int height = pix[1];
        optionOne.inSampleSize = AppUtil.calculateInSampleSize(optionOne, width, height);
        optionOne.inJustDecodeBounds = false;
        Bitmap oneImage = BitmapFactory.decodeFile(onePath, optionOne);
        /*图片2*/
        Bitmap twoImage = BitmapFactory.decodeResource(AppMasterApplication.getInstance().getResources(), id);

        Matrix matrix = new Matrix();
        float oneScaleY = SPL_SHARE_SCALE_Y;
        float oneScaleX = SPL_SHARE_SCALE_X;
        matrix.postScale(oneScaleX, oneScaleY);
        /*缩放图1*/
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
        FileOutputStream fout = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.createNewFile();
            }
            fout = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
            fout.flush();
            fout.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.flush();
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /*计算InSampleSize*/
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        /*源图片的高度和宽度*/
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if ((height > reqHeight && height / reqHeight >= 2)
                || (width > reqWidth && width / reqWidth >= 2)) {
            /*计算出实际宽高和目标宽高的比率*/
            final float heightRatio = (float) height / (float) reqHeight;
            final float widthRatio = (float) width / (float) reqWidth;
            float tempInSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
            if (tempInSampleSize < 2) {
                inSampleSize = 1;
            } else {
                inSampleSize = Math.round(tempInSampleSize);
            }
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

    /***
     * 是否设置了系统密码锁，无论当前锁是否已经显示
     *
     * @param context
     * @param defValue 对于4.0以及以下系统的默认值
     * @return
     */
    public static boolean hasSecureKeyguard(Context context, boolean defValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            KeyguardManager keyguardManager = (KeyguardManager) context
                    .getSystemService(context.KEYGUARD_SERVICE);
            if (keyguardManager == null) {
                return defValue;
            }
            return keyguardManager.isKeyguardSecure();
        } else {
            // 在4.1以前的系统中，默认返回false让屏保带上FLAG_DISMISS_KEYGUARD
            return defValue;
        }
    }

    public static String getDefaultBrowser(Context context) {
        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://"));
        browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);

        String packagetName = "";
        if (resolveInfo != null) {
            packagetName = resolveInfo.activityInfo.packageName;
        }
        LeoLog.d("stone_test_browser", "packagetName=" + packagetName);
        return packagetName;
    }

    public static boolean isDefaultBrowserChrome(Context context) {
        return (AppUtil.getDefaultBrowser(context).equalsIgnoreCase(Constants.CHROME_PACKAGE_NAME));
    }

    public static boolean belongToLeoFamily(String pkgName) {
        if (pkgName.equals(Constants.LEO_FAMILY_PG)
                || pkgName.equals(Constants.LEO_FAMILY_PL)
                || pkgName.equals(Constants.LEO_FAMILY_SWIFTY)
                || pkgName.equals(Constants.LEO_FAMILY_CB)
                || pkgName.equals(Constants.LEO_FAMILY_WIFI)
                || pkgName.startsWith(Constants.LEO_FAMILY_THEMES)) {
            return true;
        }
        return false;
    }

    public static boolean notifyAvailable() {
        AppMasterApplication context = AppMasterApplication.getInstance();
        AppMasterPreference pref = AppMasterPreference.getInstance(context);
        if (pref.getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
            return false;
        }

        return true;
    }

    public static boolean hasInternet(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity == null) {
                return false;
            } else {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected() == true) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            /*
             * Some phone may got exception when getting ConnectivityManager
             * Treat it as TRUE in this case.
             */
            return true;
        }
    }


    public static String checkApkIsGood(Context mContext, String mFileAbsName2) {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(mFileAbsName2,
                PackageManager.GET_ACTIVITIES);
        String version = null;
        try {
            if (info != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                // String appName = pm.getApplicationLabel(appInfo).toString();
                // String packageName = appInfo.packageName; //得到安装包名称
                version = info.versionName; // 得到版本信息
                LeoLog.d("checkApkIsGood", " version:" + version);
            } else {
                LeoLog.d("checkApkIsGood", "apk is not available");
            }
        } catch (Exception e) {
            return null;
        }
        return version;
    }


    public static void cleanApk(String absName) {
        File f = new File(absName);
        if (f.exists()) {
            f.delete();
        }
    }


    public static String getDateTime(long time, int type) {
        String timeString;
        Date date = new Date();
        date.setTime(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String allTime = formatter.format(date);
        String[] timePart = allTime.split("-");
        if (type == 1) {
            //year
            timeString = timePart[0] + "年";
        } else if (type == 2) {
            timeString = timePart[1] + "月" + timePart[2] + "日";
        } else {
            timeString = allTime;
        }
        return timeString;
    }

    public static boolean isLogin() {
        String userName = LeoSettings.getString(PrefConst.USER_NAME, "");
        if (!TextUtils.isEmpty(userName)) {
            return true;
        }
        return false;
    }

    public static Set<String> setPushTag(Context mContext,Set<String> strings) {
        String tag = Constants.PUSH_TAG + mContext.getString(R.string.version_name);
        if(strings == null){
            Set<String> tags = new HashSet<String>();
            tags.add(tag);
            return tags;
        }else{
            strings.add(tag);
            return strings;
        }
    }

    public static Set<String> setPushTag(Context mContext) {

        String room = LeoSettings.getString(PrefConst.USER_ROOM, "");
        Set<String> tags = new HashSet<String>();
        if (TextUtils.isEmpty(room)) {
            room = " ";
        }
        tags.add(room);

        return tags;
    }
}
