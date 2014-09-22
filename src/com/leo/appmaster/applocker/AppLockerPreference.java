package com.leo.appmaster.applocker;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class AppLockerPreference implements OnSharedPreferenceChangeListener {

	private static final String PREF_APPLICATION_LIST = "application_list";
	private static final String PREF_PASSWORD = "password";
	private static final String PREF_LOCK_POLICY = "lock_policy";
	private static final String PREF_RELOCK_TIME = "relock_time";

	private List<String> mLockedAppList;
	private String mPassword;
	private String mLockPolicy;
	private int mRelockTimeout;

	private SharedPreferences mPref;
	private static AppLockerPreference mInstance;

	private AppLockerPreference(Context context) {
		mPref = PreferenceManager.getDefaultSharedPreferences(context);
		mPref.registerOnSharedPreferenceChangeListener(this);
		loadPreferences();
	}

	public static synchronized AppLockerPreference getInstance(Context context) {
		return mInstance == null ? (mInstance = new AppLockerPreference(context))
				: mInstance;
	}

	public String getLockPolicy() {
		return mLockPolicy;
	}

	public void setLockPolicy(String policy) {
		mPref.edit().putString(PREF_LOCK_POLICY, policy).commit();
	}

	public int getRelockTimeout() {
		return mRelockTimeout;
	}

	public void setRelockTimeout(int timeout) {
		mPref.edit().putInt(PREF_RELOCK_TIME, timeout).commit();
	}

	public String getPassword() {
		return mPassword;
	}

	public void savePassword(String password) {
		mPassword = password;
		mPref.edit().putString(PREF_PASSWORD, password);
	}

	public List<String> getLockedAppList() {
		return mLockedAppList;
	}

	public void setLockedAppList(List<String> applicationList) {
		mLockedAppList = applicationList;
		String combined = "";
		for (String string : applicationList) {
			combined = combined + string + ";";
		}
		mPref.edit().putString(PREF_APPLICATION_LIST, combined).commit();
	}

	private void loadPreferences() {
		mLockedAppList = Arrays.asList(mPref.getString(PREF_APPLICATION_LIST,
				"").split(";"));
		mPassword = mPref.getString(PREF_PASSWORD, "1234");

		mLockPolicy = mPref.getString(PREF_LOCK_POLICY, null);
		mRelockTimeout = mPref.getInt(PREF_RELOCK_TIME, 0);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (PREF_APPLICATION_LIST.equals(key)) {
			mLockedAppList = Arrays.asList(mPref.getString(
					PREF_APPLICATION_LIST, "").split(";"));
		} else if (PREF_PASSWORD.equals(key)) {
			mPassword = mPref.getString(PREF_PASSWORD, "1234");
		} else if (PREF_LOCK_POLICY.equals(key)) {
			mLockPolicy = mPref.getString(PREF_LOCK_POLICY, null);
		} else if (PREF_RELOCK_TIME.equals(key)) {
			mRelockTimeout = mPref.getInt(PREF_RELOCK_TIME, 0);
		}
	}
}
