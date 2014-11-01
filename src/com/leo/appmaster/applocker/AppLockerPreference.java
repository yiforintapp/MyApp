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
	private static final String PREF_HAVE_PSWD_PROTECTED = "have_setted_pswd";
	private static final String PREF_PASSWD_QUESTION = "passwd_question";
	private static final String PREF_PASSWD_ANWSER = "passwd_anwser";
	private static final String PREF_PASSWD_TIP = "passwd_tip";
	public static final String PREF_RELOCK_TIME = "relock_time";
	public static final String PREF_AUTO_LOCK = "set_auto_lock";
	public static final String PREF_SET_PROTECT = "set_passwd_protect";
	public static final String PREF_FORBIND_UNINSTALL = "set_forbid_uninstall";
	public static final String PREF_FIRST_USE_LOCKER = "first_use_locker";
	public static final String PREF_SORT_TYPE = "sort_type";
	public static final String PREF_NEW_APP_LOCK_TIP = "new_app_lock_tip";

	private List<String> mLockedAppList;
	private String mPassword;
	private String mGesture;
	private String mLockPolicy;

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

	public boolean isNewAppLockTip() {
		return mPref.getBoolean(PREF_NEW_APP_LOCK_TIP, true);
	}

	public void setSortType(int type) {
		mPref.edit().putInt(PREF_SORT_TYPE, type).commit();
	}

	public int getSortType() {
		return mPref.getInt(PREF_SORT_TYPE, AppLockListActivity.DEFAULT_SORT);
	}

	public boolean isFisrtUseLocker() {
		return mPref.getBoolean(PREF_FIRST_USE_LOCKER, true);
	}

	public void setLockerUsed() {
		mPref.edit().putBoolean(PREF_FIRST_USE_LOCKER, false).commit();
	}

	public String getLockPolicy() {
		return mLockPolicy;
	}

	public void setLockPolicy(String policy) {
		mLockPolicy = policy;
		mPref.edit().putString(PREF_LOCK_POLICY, policy).commit();
	}

	public int getRelockTimeout() {
		String time = mPref.getString(PREF_RELOCK_TIME, "0");
		return Integer.parseInt(time) * 1000;
	}

	public void setRelockTimeout(String timeout) {
		mPref.edit().putString(PREF_RELOCK_TIME, timeout + "").commit();
	}

	public String getPassword() {
		return mPassword;
	}

	public String getGesture() {
		return mGesture;
	}

	public void savePassword(String password) {
		mPassword = "";
		if (password != null) {
			mPassword = password.trim();
		}
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
		return !mPref.getString(PREF_PASSWD_QUESTION, "").equals("");
	}

	public String getPpQuestion() {
		return mPref.getString(PREF_PASSWD_QUESTION, "");
	}

	public String getPpAnwser() {
		return mPref.getString(PREF_PASSWD_ANWSER, "");
	}

	public String getPasswdTip() {
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
		mLockPolicy = mPref.getString(PREF_LOCK_POLICY, null);
		if (mLockType == LOCK_TYPE_GESTURE) {
			mGesture = mPref.getString(PREF_GESTURE, null);
		} else if (mLockType == LOCK_TYPE_PASSWD) {
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
			// String s = mPref.getString(PREF_RELOCK_TIME, "-1");
			int re = getRelockTimeout();
		}
	}

	public void savePasswdProtect(String qusetion, String answer, String tip) {

		if (qusetion != null)
			qusetion = qusetion.trim();
		if (answer != null)
			answer = answer.trim();
		if (tip != null)
			tip = tip.trim();

		mPref.edit().putBoolean(PREF_HAVE_PSWD_PROTECTED, true).commit();
		mPref.edit().putString(PREF_PASSWD_QUESTION, qusetion).commit();
		mPref.edit().putString(PREF_PASSWD_ANWSER, answer).commit();
		mPref.edit().putString(PREF_PASSWD_TIP, tip).commit();
	}

	public void setAtuoLock(boolean autoLock) {
		mPref.edit().putBoolean(PREF_AUTO_LOCK, autoLock).commit();
	}

	public boolean isAutoLock() {
		return mPref.getBoolean(PREF_AUTO_LOCK, false);
	}

}
