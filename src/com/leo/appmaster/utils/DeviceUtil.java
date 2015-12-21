package com.leo.appmaster.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.leo.appmaster.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Created by Jasper on 2015/12/21.
 */
public class DeviceUtil {

    private static final String NULL = "null";

    public static Map<String, String> getFeedbackData(Context ctx) {
        HashMap<String, String> object = new HashMap<String, String>();
        object.put("market", ctx.getString(R.string.channel_code));
        object.put("guid", genGUID(ctx));
        object.put("appid", "appmaster");
        object.put("app_ver", getAppVer(ctx));
        object.put("os_name", getOSName());
        object.put("android_ver", getAndroidVersion());
        object.put("vendor", getVendor());
        object.put("model", getModel());
        object.put("scr_res", getScreenResolution(ctx));
        object.put("scr_dpi", getScreenDpi(ctx));
        object.put("languge", getLanguage());
        object.put("time_zone", getTimezone());
        object.put("imei", getIMEI(ctx));
        object.put("imsi", getIMSI(ctx));
        object.put("mac", getMAC(ctx));
        object.put("android_id", getAndroidId(ctx));

        return object;
    }

    public static String getAndroidId(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String genGUID(Context ctx) {
        try {
            UUID guid = UUID.nameUUIDFromBytes(getMAC(ctx).getBytes());
            // UUID guid = UUID.nameUUIDFromBytes("this".getBytes());
            return guid.toString().replace("-", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "invalid_mac_device";
        }
    }

    public static String getOSName() {
        return android.os.Build.DISPLAY.replace("\"", "");
    }

    public static String getAndroidVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static String getVendor() {
        return android.os.Build.MANUFACTURER.replace("\"", "");
    }

    public static String getModel() {
        return android.os.Build.MODEL.replace("\"", "");
    }

    public static String getScreenResolution(Context ctx) {
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        String res = dm.heightPixels + "x" + dm.widthPixels;
        return res;
    }

    public static String getScreenDpi(Context ctx) {
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        return Integer.toString(dm.densityDpi);
    }

    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static String getTimezone() {
        try {
            int offsetSecond = TimeZone.getDefault().getRawOffset() / 1000;
            StringBuilder sb = new StringBuilder();
            sb.append("GMT");
            sb.append(offsetSecond >= 0 ? "+" : "-");
            offsetSecond = offsetSecond > 0 ? offsetSecond : -offsetSecond;
            int hour = offsetSecond / (60 * 60);
            int min = (offsetSecond / 60) % 60;
            sb.append(String.format("%02d", hour) + ":"
                    + String.format("%02d", min));
            return sb.toString();
        } catch (Exception e) {
            return "GMT+00:00";
        }
    }

    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return NULL;
        }
        String imei = tm.getDeviceId();
        return imei == null ? NULL : imei;
    }

    public static String getIMSI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return NULL;
        }
        String imsi = tm.getSubscriberId();
        return imsi == null ? NULL : imsi;
    }

    public static String getMAC(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null || wifiManager.getConnectionInfo() == null) {
            return NULL;
        }
        String mac = wifiManager.getConnectionInfo().getMacAddress();
        return mac == null ? NULL : mac;
    }

    public static String getAppVer(Context ctx) {
        String mAppVer = null;
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            mAppVer = pi.versionName;
            if (mAppVer == null || mAppVer == "") {
                mAppVer = "unknow";
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        return mAppVer;
    }
}
