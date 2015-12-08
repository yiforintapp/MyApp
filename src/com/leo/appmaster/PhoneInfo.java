package com.leo.appmaster;

import java.util.Locale;

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
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

public class PhoneInfo {

//	private final static String TAG = PhoneInfo.class.getSimpleName();

	public PhoneInfo() {
	}

	// public static String getChannelID(Context aContext) {
	// return getContentFromIni(aContext, R.raw.tnconfig);
	// }

	/**
	 * get android sdk version
	 * 
	 * @return
	 */
	public static int getAndroidVersion() {
		int androidVersion = android.os.Build.VERSION.SDK_INT;
		return androidVersion;
	}

	public static String getPhoneBrand() {
		return Build.BRAND;
	}

	public static String getPhoneDeviceModel() {
		return Build.MODEL;
	}

	public static String getDevicever() {
		return Build.DISPLAY;
	}

	public static String getSysterVersion() {
		return Build.VERSION.RELEASE;
	}

	public static String getResolution(Context aContext) {
		DisplayMetrics display = aContext.getResources().getDisplayMetrics();
		return display.widthPixels + "*" + display.heightPixels;
	}

	public static String getLanguage() {
		return Locale.getDefault().getLanguage();
	}

	public static String getMacAddress(Context aContext) {
		String result = null;
		WifiManager wifiManager = (WifiManager) aContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		result = wifiInfo.getMacAddress();
		return result;
	}

	public static String getImei(Context aContext) {
		TelephonyManager tm = (TelephonyManager) aContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	public static String getOperatorName(Context aContext) {
		TelephonyManager tm = (TelephonyManager) aContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		String simoperator = tm.getSimOperator();

		String[] liantong = { "46001", "46006" };
		String[] yidong = { "46000", "46002", "46007" };
		String[] dianxin = { "46003", "46005" };

		if (simoperator == null || simoperator.trim().equals("")) {
			// return
			// aContext.getResources().getString(R.string.operator_unknown);
		} else {
			try {
				simoperator = simoperator.substring(0, 5);
			} catch (Exception e) {
				// return
				// aContext.getResources().getString(R.string.operator_unknown);
			}

		}
		for (String data : liantong) {
			if (data.equals(simoperator)) {
				// return
				// aContext.getResources().getString(R.string.operator_liantong);
			}
		}
		for (String data : yidong) {
			if (data.equals(simoperator)) {
				// return
				// aContext.getResources().getString(R.string.operator_yidong);
			}
		}
		for (String data : dianxin) {
			if (data.equals(simoperator)) {
				// return
				// aContext.getResources().getString(R.string.operator_dianxin);
			}
		}

		// return aContext.getResources().getString(R.string.operator_other);

		return null;
	}

	public static String getPhoneNumber(Context aContext) {
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

	public static int getVersionCode(Context aContext) {
		try {
			PackageManager packageManager = aContext.getPackageManager();
			PackageInfo packInfo = packageManager.getPackageInfo(
					aContext.getPackageName(), 0);
			return packInfo.versionCode;
		} catch (NameNotFoundException e) {
			return -1;
		}
	}

	/**
	 * locationdata[0] : Latitude locationdata[1] : Longitude;
	 * 
	 * @param aContext
	 * @return
	 */
	public static double[] getLocation(Context aContext) {
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

	// public static static String getBuildVersion(Context aContext) {
	// return getContentFromIni(aContext, R.raw.buildversion);
	// }

	// public static static String getCUID(Context aContext) {
	// return StatService.getCuid(aContext);
	// }

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

	public static boolean IsSdCardMounted() {
		try {
			return Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String getIpAddresses(Context aContext) {
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
	
    public static boolean isSimAvailable(Context mContext) {
        try {
            TelephonyManager mgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            SmsManager smsManager = SmsManager.getDefault();
            return (TelephonyManager.SIM_STATE_READY == mgr.getSimState())
                    && (smsManager != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
}
