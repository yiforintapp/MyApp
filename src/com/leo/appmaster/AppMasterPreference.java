package com.leo.appmaster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.leo.appmaster.applocker.AppLockListActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class AppMasterPreference implements OnSharedPreferenceChangeListener {

	// about lock
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
	public static final String PREF_LAST_PULL_LOCK_LIST_TIME = "last_pull_lock_list_time";
	public static final String PREF_PULL_INTERVAL = "pull_interval";
	public static final String PREF_RECOMMEND_LOCK_LIST = "recommend_app_lock_list";
	public static final String PREF_LAST_ALARM_SET_TIME = "last_alarm_set_time";
	public static final String PREF_RECOMMEND_LOCK_PERCENT = "recommend_lock_percent";
	public static final String PREF_UNLOCK_COUNT = "unlock_count";

	// other
	public static final String PREF_LAST_VERSION = "last_version";
	public static final String PREF_LAST_VERSION_INSTALL_TIME = "last_version_install_tiem";
	public static final String PREF_LOCK_REMIND = "lock_remind";

	private List<String> mLockedAppList;
	private List<String> mRecommendList;
	private String mPassword;
	private String mGesture;
	private String mLockPolicy;

	public static final int LOCK_TYPE_NONE = -1;
	public static final int LOCK_TYPE_PASSWD = 0;
	public static final int LOCK_TYPE_GESTURE = 1;
	private int mLockType = LOCK_TYPE_NONE;

	private SharedPreferences mPref;
	private static AppMasterPreference mInstance;

	private AppMasterPreference(Context context) {
		mPref = PreferenceManager.getDefaultSharedPreferences(context);
		mPref.registerOnSharedPreferenceChangeListener(this);
		loadPreferences();
	}

	public static synchronized AppMasterPreference getInstance(Context context) {
		return mInstance == null ? (mInstance = new AppMasterPreference(context))
				: mInstance;
	}

	public void setUnlockCount(long count) {
		mPref.edit().putLong(PREF_UNLOCK_COUNT, count).commit();
	}

	public long getUnlockCount() {
		return mPref.getLong(PREF_UNLOCK_COUNT, 0);
	}

	public void setRecommendLockPercent(float percent) {
		mPref.edit().putFloat(PREF_RECOMMEND_LOCK_PERCENT, percent).commit();
	}

	public float getRecommendLockPercent() {
		return mPref.getFloat(PREF_RECOMMEND_LOCK_PERCENT, 0.0f);
	}

	public void setReminded(boolean reminded) {
		mPref.edit().putBoolean(PREF_LOCK_REMIND, reminded).commit();
	}

	public boolean isReminded() {
		return mPref.getBoolean(PREF_LOCK_REMIND, false);
	}

	public void setLastVersion(String lastVersion) {
		mPref.edit().putString(PREF_LAST_VERSION, lastVersion).commit();
	}

	public String getLastVersion() {
		return mPref.getString(PREF_LAST_VERSION, "");
	}

	public void setLastVersionInstallTime(long time) {
		mPref.edit().putLong(PREF_LAST_VERSION_INSTALL_TIME, time).commit();
	}

	public long getLastVersionInstallTime() {
		return mPref.getLong(PREF_LAST_VERSION_INSTALL_TIME, 0l);
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

	public List<String> getRecommendList() {
		return mRecommendList;
	}

	public void setRecommendList(List<String> applicationList) {
		mRecommendList = applicationList;
		String combined = "";
		for (String string : applicationList) {
			combined = combined + string + ";";
		}
		mPref.edit().putString(PREF_RECOMMEND_LOCK_LIST, combined).commit();
	}

	private void loadPreferences() {
		String lockList = mPref.getString(PREF_APPLICATION_LIST, "");
		if (lockList.equals("")) {
			mLockedAppList = new ArrayList<String>(0);
		} else {
			mLockedAppList = Arrays.asList(mPref.getString(
					PREF_APPLICATION_LIST, "").split(";"));
		}
		mRecommendList = Arrays.asList(mPref.getString(
				PREF_RECOMMEND_LOCK_LIST, "").split(";"));
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
			String lockList = mPref.getString(PREF_APPLICATION_LIST, "");
			if (lockList.equals("")) {
				mLockedAppList = new ArrayList<String>(0);
			} else {
				mLockedAppList = Arrays.asList(mPref.getString(
						PREF_APPLICATION_LIST, "").split(";"));
			}
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

	public void setLastLocklistPullTime(long time) {
		mPref.edit().putLong(PREF_LAST_PULL_LOCK_LIST_TIME, time).commit();
	}

	public void setPullInterval(long interval) {
		mPref.edit().putLong(PREF_PULL_INTERVAL, interval).commit();
	}

	public long getLastLocklistPullTime() {
		return mPref.getLong(PREF_LAST_PULL_LOCK_LIST_TIME, 0l);
	}

	public long getPullInterval() {
		return mPref.getLong(PREF_PULL_INTERVAL, 0l);
	}

	public void setLastAlarmSetTime(long currentTimeMillis) {
		mPref.edit().putLong(PREF_LAST_ALARM_SET_TIME, currentTimeMillis)
				.commit();
	}

	public long getInstallTime() {
		return mPref.getLong(PREF_LAST_ALARM_SET_TIME, 0l);
	}

}
