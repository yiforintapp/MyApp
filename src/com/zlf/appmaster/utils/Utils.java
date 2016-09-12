package com.zlf.appmaster.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Toast;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;

import java.util.List;

/**
 * 公共工具类
 *
 * @author Deping Huang
 */
public class Utils {

    private static final String TAG = "Utils";

    public static String getIMEI(Context context) {
        String imei = null;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            imei = telephonyManager.getDeviceId();
        }
        if (imei == null) {
            imei = "";
        }
        return imei;
    }

    ;

    //软件版本
    public static String getSoftVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "0.0.0.0";//最低版本号
    }

    // 当前软件版本
    public static int getVersion(Context context) {
        //TODO VersionRange 服务器版本号 range 0x20000000~0x2FFFFFFF
        return 0x20000001;
        /*try {
			PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}*/
    }

    // 随机生成唯一的clientID
    public static long getClientID() {
        return System.currentTimeMillis(); // 暂用时间处理
    }

    // 得到当前用户Uin
    public static String getAccountUin(Context context) {
        return new Setting(context).getUin();
    }

    /**
     * 取用户uin
     *
     * @param context
     * @return
     */
    public static long getAccountLongUin(Context context) {
        String uinStr = getAccountUin(context);
        long uin = 0;
        if (!TextUtils.isEmpty(uinStr)) {
            uin = Long.valueOf(uinStr);
        }

        return uin;
    }


    public static long getGuestUin(Context context) {
        return new Setting(context).getGuestUin();
    }

    public static void setGuestUin(Context context, long guestUin) {
        new Setting(context).setGuestUin(guestUin);
    }


    // 得到当前的sessionID
    public static String getSessionID(Context context) {
        //直接从配置文件中读取
        return new Setting(context).getSessionId();
    }


    /**
     * 当前登录的用户是否为主播
     *
     * @param context
     * @return
     */
    public static boolean isAnchorUser(Context context) {
        int userRole = new Setting((context)).getUserRole();
        if (userRole == QConstants.ACCOUNT_ROLE_ANCHOR)
            return true;

        return false;
    }

    // 判断网络状态
    public static boolean GetNetWorkStatus(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return false;
        } else {
            return true;
        }

    }


    /**
     * 设置WebView的布局显示，如果详细内容包含table标签，则不设置webview为单列
     *
     * @param webView
     * @param content
     * @return
     */
    public static void setWebViewLayout(WebView webView, String content) {
        if (content.contains("<table")) {
            webView.getSettings().setLayoutAlgorithm(
                    LayoutAlgorithm.NARROW_COLUMNS);
        } else {
            webView.getSettings().setLayoutAlgorithm(
                    LayoutAlgorithm.SINGLE_COLUMN);
        }
    }

    /**
     * 判断是否快速点击
     */
    private static long mLastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - mLastClickTime;
        if (0 < timeD && timeD < 1000) {
            return true;
        }
        mLastClickTime = time;
        return false;
    }


    /**
     * 用来判断服务是否运行.
     *
     * @param context
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }


    //重启应用
    public static void restartApplication(Context context, String topActivity) {
        Context appContext = context.getApplicationContext();

        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (topActivity.equals(cn.getClassName())) {
            QLog.i(TAG, "已经重启过了！");
            return;
        }

//    	QLog.i(TAG, "当前activity:"+cn.getClassName()+",需要的topActivity:"+topActivity);

        final Intent intent = appContext.getPackageManager().getLaunchIntentForPackage(
                appContext.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        appContext.startActivity(intent);
        QLog.i(TAG, "restartApplication");
    }

       /*
        * 启动一个app
        */
    public static void startAPP(String appPackageName,Context mContext,String downloadUrl){
        try{
            if(Utilities.isEmpty(appPackageName)){
                appPackageName = Constants.CJLH_PACKAGENAME;
            }
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(appPackageName);
            mContext.startActivity(intent);
        }catch(Exception e){
            Toast.makeText(mContext, mContext.getString(R.string.jump_download), Toast.LENGTH_LONG).show();

            if(Utilities.isEmpty(downloadUrl)){
                downloadUrl = Constants.CJLH_DOWNLOAD_URL;
            }

            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content_url = Uri.parse(downloadUrl);
            intent.setData(content_url);
            mContext.startActivity(intent);

        }
    }

    public static int getColorByFloat(Context context, float num) {
        if (num > 0f) {
            return context.getResources().getColor(R.color.stock_rise);
        } else if (num < 0f) {
            return context.getResources().getColor(R.color.stock_slumped);
        } else {
            return 0xff000000;
        }
    }

    public static String getDevName() {

        return android.os.Build.MODEL + ","
                + android.os.Build.VERSION.SDK + ","
                + android.os.Build.VERSION.RELEASE;
    }


    public static boolean isAppRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List runningTasks = activityManager.getRunningTasks(1);
        return context.getPackageName().equalsIgnoreCase(((ActivityManager.RunningTaskInfo) runningTasks.get(0)).baseActivity.getPackageName());
    }

}
