/*
 * 文件名称 : AndroidConfig.java
 * <p>
 * 作者信息 : liuzongyao
 * <p>
 * 创建时间 : 2013-9-10, 下午8:17:50
 * <p>
 * 版权声明 : Copyright (c) 2009-2012 Hydb Ltd. All rights reserved
 * <p>
 * 评审记录 :
 * <p>
 */

package com.leo.appmaster.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Rect;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;


/**
 * 获取android的基本环境配置
 * @author liuzongyao
 *
 */
public class AndroidConfig
{
    
    private static String mIMEI = null;
    private static String mPeerId = null;
    private static Context mContext = null;
    private static final String DEFAULT_PEER_ID = "0000000000000000004V";
    private static final String DEFAULT_IMEI = "000000000000000";
    private static String mMac = null;   
    
    public static void init(Context context) {
        mContext = context;
        getScreenHeight();
        getScreenWidth();
    }
    
    public static Context getContext(){
    	return mContext;
    }
    
 // 获取peerid
    public static String getPeerid() {
        if (TextUtils.isEmpty(mPeerId) && mContext instanceof Context) {
            WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            if (null != wm && null != wm.getConnectionInfo()) {
                String mac = wm.getConnectionInfo().getMacAddress();
                mac += "004V";
                mPeerId = mac.replaceAll(":", "");
                mPeerId = mPeerId.replaceAll(",", "");
                mPeerId = mPeerId.replaceAll("[.]", "");
                mPeerId = mPeerId.toUpperCase();
            }
        }
        if (TextUtils.isEmpty(mPeerId)) {
            return DEFAULT_PEER_ID;
        } else {
            return mPeerId;
        }
    }
    
    public static String getMAC() {
    	if (TextUtils.isEmpty(mMac) && mContext instanceof Context) {
            WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            if (null != wm && null != wm.getConnectionInfo()) {
            	mMac = wm.getConnectionInfo().getMacAddress();
            	if(!TextUtils.isEmpty(mMac)) {
            		mMac = mMac.toUpperCase();
            	}
            }
        }
    	return mMac;
    }

    public static String getPeerid(final Context context) {
        String peerId = null;
        if (context instanceof Context) {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (null != wm && null != wm.getConnectionInfo()) {
                String mac = wm.getConnectionInfo().getMacAddress();
                mac += "004V";
                peerId = mac.replaceAll(":", "");
                peerId = peerId.replaceAll(",", "");
                peerId = peerId.replaceAll("[.]", "");
                peerId = peerId.toUpperCase();
            }
        }
        if (TextUtils.isEmpty(peerId)) {
            peerId = DEFAULT_PEER_ID;
        }
        return peerId;
    }

    /**
     * 获取设备的IMEI，即设备标识
     * 
     * @author 倪翔
     */
    public static String getIMEI(final Context context) {
        String imei = null;
        if (context instanceof Context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                imei = tm.getDeviceId();
            }
        }
        if (TextUtils.isEmpty(imei)) {
            imei = DEFAULT_IMEI;
        }
        return imei;
    }

    /**
     * 获取设备的IMEI，即设备标识
     * 
     * @author 倪翔
     */
    public static String getIMEI() {
        if (mContext instanceof Context && TextUtils.isEmpty(mIMEI)) {
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                mIMEI = tm.getDeviceId();
            }
        }
        if (TextUtils.isEmpty(mIMEI)) {
            return DEFAULT_IMEI;
        } else {
            return mIMEI;
        }
    }
    
    
    public static String getPartnerId(Context ctx) {
//        if (ctx != null) {
//            return ctx.getResources().getString(R.string.pid);
//        }
        return "";
    }
    
    public static String getPartnerId() {
        return getPartnerId(mContext);
    }

    public static String getProductId(Context ctx) {
//        if (ctx != null) {
//            return ctx.getResources().getString(R.string.product_id);
//        }
        return "";
    }
    
    public static String getProductId() {
        return getProductId(mContext);
     }
    
    
    /**
     * 获取android版本号
     * 
     * @return
     */
    public static int getAndroidVersion() {
        int androidVersion = android.os.Build.VERSION.SDK_INT;
        return androidVersion;
    }

    /**
     * 获取手机型号
     * 
     * @return
     */
    public static String getPhoneModel() {
        return Build.MODEL;
    }

    /**
     * 获取手机厂商
     * 
     * @return
     */
    public static String getPhoneBrand() {
        return Build.BRAND;
    }    
    
    public static int getScreenWidth() {
    	int screenWidth = 0;
        if (mContext instanceof Context) {
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
            screenWidth = dm.widthPixels;
        }
        
        return screenWidth;
    }

    public static int getScreenHeight() {
    	int screenHeight = 0;
        if (mContext instanceof Context) {
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);
            screenHeight = dm.heightPixels;
        }
        return screenHeight;
    }
    
    /**
     * 检查app是否已安装
     * 
     * @param packageName
     *            需要匹配的包名
     * @param versionCode
     *            需要匹配的版本号，如果选择忽略版本号则传0
     */
    public static boolean isInstalledApk(String packageName, int versionCode) {
        boolean result = false;
        if (mContext instanceof Context) {
            try {
                PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(packageName, 0);
                if (packageInfo != null && (packageInfo.versionCode == versionCode || versionCode == 0)) {
                    result = true;
                }
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 判断当前系统SDK版本是否至少满足apiLevel版本
     * 
     * @param apiLevel
     *            从Build.VERSION_CODES取相应的版本号
     */
    public static boolean isSDKSupport(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }


    /**
     * 获取应用的版本号
     * 
     * @param context
     */
    public static String getVersion(Context context) {
        String version = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionCode + "";
        } catch (Exception e) {
            Log.e("AndroidConfig", "getVersion error " + e.getMessage());
        }
        return version;
    }
    
    public static String getVersionName(Context context) {
        String version = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (Exception e) {
        	 Log.e("AndroidConfig", "getVersionName error " + e.getMessage());
        }
        return version;
    }
    
    public static String getVersionName() {
        return getVersionName(mContext);
    }
    
    public static int getVersionCode() {
        int version = 1;
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            version = info.versionCode;
        } catch (Exception e) {
        	 Log.e("AndroidConfig", "getVersion error " + e.getMessage());
        }
        return version;
    }
    
    public static String getDeviceInfo() {
        String device = getPhoneBrand() + "|" + getPhoneModel();
        try
        {
            device = URLEncoder.encode(device,"iso-8859-1");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return device;
    }
    
    /**
     * 获取进程名
     * 
     * @param context
     * @return
     */
    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
    
    
       /**
     * 获取进程名
     * 
     * @param context
     * @return
     */
    public static boolean isExistProcessName(Context context, String name) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.processName.equals(name) && appProcess.pid != pid) {
                return  true;
            }
        }
        return false;
    }
    
    
    // 得到进程ID
    static List<RunningAppProcessInfo> apps = null;
    static int mgprocessId = 0;

    public static int getProcessId(Context con) {
        if (mgprocessId == 0) {
            ActivityManager am = (ActivityManager) con.getSystemService(Context.ACTIVITY_SERVICE);
            apps = am.getRunningAppProcesses();// 返回进程列表信息
            for (RunningAppProcessInfo p : apps) {
                if (p.processName.equals("com.xunlei.kankan")) {
                    mgprocessId = p.pid;
                    break;
                }
            }
        }
        return mgprocessId;
    }
    
    
    /**
     * 隐藏软键盘
     * @param ctx
     * @param v
     */
    public static void hiddenInput(Context ctx, View v) {
        if (ctx instanceof Context && v instanceof View) {
            InputMethodManager inputMethodManager = (InputMethodManager) ctx
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    /**
     * 显示软键盘
     * @param ctx
     */
    public static void showInput(Context ctx) {
        if (ctx instanceof Context) {
            InputMethodManager inputMethodManager = (InputMethodManager) ctx
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    
    /**
     * 查询输入法面板是否已弹出
     * @param ctx
     * @return
     */
    public static boolean isInputActive(Context ctx) {
        if (ctx instanceof Context) {
            InputMethodManager inputMethodManager = (InputMethodManager) ctx
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            return inputMethodManager.isActive();
        }
        return false;
    }
    
    
    
    // 手动计算listView的高度，避免嵌套的listView显示不全
    public static void setListViewHeightBaseOnChildren(ListView listView)
    {
        if (listView != null)
        {
            ListAdapter adapter = listView.getAdapter();
            int totalHeight = 0;
            int count = adapter.getCount();
            for (int i = 0; i < count; i++)
            {
                // View child = listView.getChildAt(i);crash
                View child = adapter.getView(i, null, listView);
                child.measure(0, 0);
                totalHeight += child.getMeasuredHeight();
            }
            if (count > 0)
            {
                totalHeight += (count - 1) * listView.getDividerHeight();
            }
            LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight;
            listView.setLayoutParams(params);
        }
    }
    
    //返回listView的高度
    public static int getListViewHeightBaseOnChildren(ListView listView)
    {
        if (listView != null)
        {
            ListAdapter adapter = listView.getAdapter();
            int totalHeight = 0;
            int count = adapter.getCount();
            for (int i = 0; i < count; i++)
            {
                // View child = listView.getChildAt(i);crash
                View child = adapter.getView(i, null, listView);
                child.measure(0, 0);
                totalHeight += child.getMeasuredHeight();
            }
            if (count > 0)
            {
                totalHeight += (count - 1) * listView.getDividerHeight();
            }
            return totalHeight;
        }
        return 0;
    }
    
    public static int getTitleBarHeight(Activity activity)
    {
        Rect fram = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(fram);
        int statusBarHeight = fram.top;
        int contentTop = activity.getWindow()
                .findViewById(Window.ID_ANDROID_CONTENT)
                .getTop();
        int titleBarHeight = contentTop - statusBarHeight;
        
        return statusBarHeight + titleBarHeight;
    }
    
    public static String getUrlPF(Activity ctx)
    {
        DisplayMetrics dm = new DisplayMetrics();
        ctx.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = (int) (dm.widthPixels);
        if (width < 480)
        {
            return "pf=320";
        }
        else if (width >= 480 && width < 540)
        {
            return "pf=480";
        }
        else if (width >= 540)
        {
            return "pf=540";
        }
        else
        {
            return "pf=540";
        }
    }
    
    
   


}
