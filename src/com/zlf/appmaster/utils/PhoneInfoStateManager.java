package com.zlf.appmaster.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public class PhoneInfoStateManager {

	private final static String TAG = PhoneInfoStateManager.class
			.getSimpleName();

	public PhoneInfoStateManager() {
	}

	// public static String getChannelID(Context aContext) {
	// return getContentFromIni(aContext, R.raw.tnconfig);
	// }

	public String getPhoneBrand() {
		return Build.BRAND;
	}

	public String getPhoneDeviceModel() {
		return Build.MODEL;
	}

	public String getDevicever() {
		return Build.DISPLAY;
	}

	public String getSysterVersion() {
		return Build.VERSION.RELEASE;
	}

	public String getResolution(Context aContext) {
		DisplayMetrics display = aContext.getResources().getDisplayMetrics();
		return display.widthPixels + "*" + display.heightPixels;
	}

	public String getLanguage() {
		return Locale.getDefault().getLanguage();
	}

	public String getMacAddress(Context aContext) {
		String result = null;
		WifiManager wifiManager = (WifiManager) aContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		result = wifiInfo.getMacAddress();
		return result;
	}

	public String getImei(Context aContext) {
		TelephonyManager tm = (TelephonyManager) aContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	public String getPhoneNumber(Context aContext) {
		TelephonyManager tm = (TelephonyManager) aContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getLine1Number();
	}

	public static String getVersionName(Context aContext) {
		try {
			PackageManager packageManager = aContext.getPackageManager();
			PackageInfo packInfo = packageManager.getPackageInfo(
					aContext.getPackageName(), 0);
			String version = packInfo.versionName;
			return version;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getVersionCode(Context aContext) {
		try {
			PackageManager packageManager = aContext.getPackageManager();
			PackageInfo packInfo = packageManager.getPackageInfo(
					aContext.getPackageName(), 0);
			int versioncode = packInfo.versionCode;
			return String.valueOf(versioncode);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//is pl google play pkg
	public static boolean isGooglePlayPkg() {
	    String channelCode = AppMasterApplication.getInstance().getString(R.string.channel_code);
	    if("0001a".equals(channelCode))
	        return true;
	    return false;
	}

	/**
	 * locationdata[0] : Latitude locationdata[1] : Longitude;
	 * 
	 * @param aContext
	 * @return
	 */
	public double[] getLocation(Context aContext) {
		final LocationManager locationManager = (LocationManager) aContext
				.getSystemService(Context.LOCATION_SERVICE);
		Location location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		double[] locationdata = new double[] { 0.0, 0.0 };
		if (location != null) {
			locationdata[0] = location.getLatitude();
			locationdata[1] = location.getLongitude();
		} else {
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location != null) {
				locationdata[0] = location.getLatitude();
				locationdata[1] = location.getLongitude();
			}
		}
		return locationdata;
	}

	public String getTotalMemory(Context aContext) {
		ActivityManager am = (ActivityManager) aContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(mi);
		long totalmem = 0;
		String memfile = "/proc/meminfo";
		String memcontent;
		String[] arrayofstring;
		try {
			FileReader localFileReader = new FileReader(memfile);
			BufferedReader buffer = new BufferedReader(localFileReader, 8192);
			memcontent = buffer.readLine();
			if (memcontent != null) {
				arrayofstring = memcontent.split("\\s+");
				totalmem = Integer.valueOf(arrayofstring[1]).intValue() / 1024;
				buffer.close();
			}
		} catch (IOException e) {
			com.zlf.appmaster.utils.LeoLog.i(TAG, e.getStackTrace().toString());
		}
		return String.valueOf(totalmem);
	}

	public static boolean isNetworkConnectivity(Context aContext) {
		ConnectivityManager manager = (ConnectivityManager) aContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null == manager) {
			return false;
		}
		NetworkInfo activeInfo = manager.getActiveNetworkInfo();
		if (activeInfo != null && activeInfo.isConnected()) {
			int type = activeInfo.getType();
			if (ConnectivityManager.TYPE_WIFI == type) {
				return true;
			} else if (ConnectivityManager.TYPE_MOBILE == type) {
				return true;
			}
		}
		return false;
	}

	public static boolean isWifiConnection(Context aContext) {
		ConnectivityManager manager = (ConnectivityManager) aContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null == manager) {
			return false;
		}
		NetworkInfo activeInfo = manager.getActiveNetworkInfo();
		if (activeInfo != null && activeInfo.isConnected()) {
			int type = activeInfo.getType();
			if (ConnectivityManager.TYPE_WIFI == type) {
				return true;
			}
		}
		return false;
	}

	public boolean IsSdCardMounted() {
		try {
			return Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getIpAddresses(Context aContext) {
//		ConnectivityManager cm = (ConnectivityManager) aContext
//				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// LinkProperties prop = cm.getActiveLinkProperties();
		// return formatIpAddresses(prop);
		return null;
	}

//	private static String getContentFromIni(Context aContext, int aResId) {
//		InputStream iniFile = null;
//		iniFile = aContext.getResources().openRawResource(aResId);
//		BufferedReader bufferedReader = null;
//		try {
//			bufferedReader = new BufferedReader(new InputStreamReader(iniFile,
//					"gb2312"));
//		} catch (UnsupportedEncodingException e) {
//			LeoLog.w(TAG, e.getStackTrace().toString());
//		}
//
//		String content = "";
//		String tmp = null;
//		try {
//			while ((tmp = bufferedReader.readLine()) != null) {
//				content += tmp;
//				LeoLog.d(TAG, tmp);
//			}
//			bufferedReader.close();
//			iniFile.close();
//		} catch (IOException e) {
//			LeoLog.w(TAG, e.getStackTrace().toString());
//		}
//		return content;
//	}
}
