package com.leo.appmaster.applocker;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class AppLockerPreference implements OnSharedPreferenceChangeListener {

	private static final String PREF_APPLICATION_LIST = "application_list";
	private static final String PREF_LOCK_TYPE = "lock_type";
	private static final String PREF_PASSWORD = "password";
	private static final String PREF_GESTURE = "gesture";
	private static final String PREF_LOCK_POLICY = "lock_policy";
	private static final String PREF_RELOCK_TIME = "relock_time";
	private static final String PREF_HAVE_PSWD_PROTECTED = "have_setted_pswd";
	private static final String PREF_PASSWD_QUESTION = "passwd_question";
	private static final String PREF_PASSWD_ANWSER = "passwd_anwser";
	private static final String PREF_PASSWD_TIP = "passwd_tip";

	private List<String> mLockedAppList;
	private String mPassword;
	private String mGesture;
	private String mLockPolicy;
	private int mRelockTimeout;
	private boolean mHavePswdProtect;

	public static final int LOCK_TYPE_NONE = -1;
	public static final int LOCK_TYPE_PASSWD = 0;
	public static final int LOCK_TYPE_GESTURE = 1;
	private int mLockType = LOCK_TYPE_NONE;

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
		mLockPolicy = policy;
		mPref.edit().putString(PREF_LOCK_POLICY, policy).commit();
	}

	public int getRelockTimeout() {
		return mRelockTimeout;
	}

	public void setRelockTimeout(int timeout) {
		mRelockTimeout = timeout;
		mPref.edit().putInt(PREF_RELOCK_TIME, timeout).commit();
	}

	public String getPassword() {
		return mPassword;
	}

	public String getGesture() {
		return mGesture;
	}

	public void savePassword(String password) {
		mPassword = password;
		mPref.edit().putString(PREF_PASSWORD, password).commit();
		mPref.edit().putInt(PREF_LOCK_TYPE, LOCK_TYPE_PASSWD).commit();
		mLockType = LOCK_TYPE_PASSWD;
	}

	public void saveGesture(String gesture) {
		mGesture = gesture;
		mPref.edit().putString(PREF_GESTURE, gesture).commit();
		mPref.edit().putInt(PREF_LOCK_TYPE, LOCK_TYPE_GESTURE).commit();
		mLockType = LOCK_TYPE_GESTURE;

	}

	public int getLockType() {
		return mLockType;
	}

	public boolean hasPswdProtect() {
		return mHavePswdProtect;
	}

	public String getPpQuestion() {
		return mPref.getString(PREF_PASSWD_QUESTION, "");
	}

	public String getPpAnwser() {
		return mPref.getString(PREF_PASSWD_ANWSER, "");
	}

	public String getPpTip() {
		return mPref.getString(PREF_PASSWD_TIP, "");
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
		mLockType = mPref.getInt(PREF_LOCK_TYPE, LOCK_TYPE_NONE);
		mHavePswdProtect = mPref.getBoolean(PREF_HAVE_PSWD_PROTECTED, false);
		mLockPolicy = mPref.getString(PREF_LOCK_POLICY, null);
		mRelockTimeout = mPref.getInt(PREF_RELOCK_TIME, 0);
		if(mLockType == LOCK_TYPE_GESTURE) {
			mGesture = mPref.getString(PREF_GESTURE, null);
		} else if(mLockType == LOCK_TYPE_PASSWD){
			mPassword = mPref.getString(PREF_PASSWORD, null);
		}
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

	public void savePasswdProtect(String qusetion, String answer, String tip) {
		mHavePswdProtect = true;
		mPref.edit().putBoolean(PREF_HAVE_PSWD_PROTECTED, true).commit();
		mPref.edit().putString(PREF_PASSWD_QUESTION, qusetion).commit();
		mPref.edit().putString(PREF_PASSWD_ANWSER, answer).commit();
		mPref.edit().putString(PREF_PASSWD_TIP, tip).commit();
	}

}
