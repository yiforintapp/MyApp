
package com.leo.wifichecker.wifi;

import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by luqingyuan on 15/9/7.
 */
public class APInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final int SEC_UNKNOWN = -1;
    public static final int SEC_OPEN = 1;
	public static final int SEC_WEP = 2;
	public static final int SEC_PSK = 3;
	public static final int SEC_EAP = 4;

	public String mPassword;
	public String mKeyMgmt;
	public String mEap;
	public String mIdentity;
	/**
	 * 长字符串
	 */
	public String mOtherSettings;

	/**
	 * ScanResult
	 */
	public String mSSID;
	public String mBSSID;
	public String mCapabilities;
	public int mSecLevel;
	public int mLevel;
	public int mFrequency;

	/** 位置信息*/
	public double mLatitude;
	public double mLongitude;
    public float mAccuracy;
    public boolean mIsChanged;
	public APInfo() {
		super();
		mLevel = 0;
		mFrequency = 0;
		mLatitude = 0;
		mLongitude = 0;
		mAccuracy = 0;
		mIsChanged = true;
	}

	public void setCapabilities (String capabilities) {
        mCapabilities = capabilities;
		if(capabilities.contains("WEP")) {
			mSecLevel = SEC_WEP;
		} else if(capabilities.contains("PSK")) {
			mSecLevel = SEC_PSK;
		} else if(capabilities.contains("EAP")) {
			mSecLevel = SEC_EAP;
		} else {
			mSecLevel = SEC_OPEN;
		}
	}

	public void setmKeyMgmtByWifiConf(WifiConfiguration configuration) {

		if (configuration == null) {
			mSecLevel = SEC_UNKNOWN;
			return;
		}
		mKeyMgmt = "";
		if(configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
			mKeyMgmt = "WPA_PSK";
			mSecLevel = SEC_PSK;
		}
		if ((configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP))){
			mKeyMgmt += " WPA_EAP";
			mSecLevel = SEC_EAP;
		}
		if((configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X))) {
			mKeyMgmt += " IEEE8021X";
			mSecLevel = SEC_EAP;
		}
		if (configuration.wepKeys[0] != null) {
			mSecLevel = SEC_WEP;
			mKeyMgmt += " WEP";
		}
		if(TextUtils.isEmpty(mKeyMgmt)) {
			mKeyMgmt = "NONE";
			mSecLevel = SEC_OPEN;
		}
	}

	public static String stripLeadingAndTrailingQuotes(String str){
		if(str == null || str.length() <=0){
			return "";
		}

		if (str.startsWith("\"")){
			str = str.substring(1, str.length());
		}
		if (str.endsWith("\"")){
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	/**
	 * 是否数据有改变
	 * @return
	 */
	public boolean isChanged() {
		return mIsChanged;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SSID: ").append(mSSID).
				append(", BSSID=").append(mBSSID).
				append(", capabilities=").append(mCapabilities).
				append(", level=").append(mLevel).
				append(", frequency=").append(mFrequency).
				append(", password=").append(mPassword).
				append(", keyMgmt=").append(mKeyMgmt).
				append(", secLevel=").append(mSecLevel).
				append(", eap=").append(mEap).
				append(", identity=").append(mIdentity).
				append(", otherSettings=").append(mOtherSettings).
				append(", latitude=").append(mLatitude).
				append(", longitude=").append(mLongitude).
		        append(", accuracy=").append(mAccuracy).
		        append(", isChanged=").append(mIsChanged);

		return sb.toString();
	}

}