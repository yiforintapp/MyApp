package com.leo.appmaster.applocker;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.ui.CommonTitleBar;

public class LockOptionActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	private CommonTitleBar mTtileBar;
	private SharedPreferences mSp;

	private CheckBoxPreference mForbidUninstall, mAutoLock;
	private Preference mSetProtect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_option);
		addPreferencesFromResource(R.xml.setting);
		initUI();
		setupPreference();
	}

	private void setupPreference() {
		mForbidUninstall = (CheckBoxPreference) findPreference(AppLockerPreference.PREF_FORBIND_UNINSTALL);
		mAutoLock = (CheckBoxPreference) findPreference(AppLockerPreference.PREF_AUTO_LOCK);
		mSetProtect = findPreference(AppLockerPreference.PREF_SET_PROTECT);
		mForbidUninstall.setOnPreferenceChangeListener(this);
		mAutoLock.setOnPreferenceChangeListener(this);
	}

	private boolean isAdminActive() {
		DevicePolicyManager manager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		ComponentName mAdminName = new ComponentName(this, DeviceReceiver.class);
		if (manager.isAdminActive(mAdminName)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onResume() {
		if (isAdminActive()) {
			mForbidUninstall.setChecked(true);
		} else {
			mForbidUninstall.setChecked(false);
		}

		if (haveProtect()) {
			mSetProtect.setTitle(R.string.passwd_protect);
		} else {
			mSetProtect.setTitle(getString(R.string.passwd_protect) + "("
					+ getString(R.string.not_set) + ")");
		}

		super.onResume();
	}

	private boolean haveProtect() {
		return AppLockerPreference.getInstance(this).hasPswdProtect();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initUI() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.app_lock);
		mTtileBar.openBackView();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();
		if (AppLockerPreference.PREF_FORBIND_UNINSTALL.equals(key)) {
			Intent intent = null;
			ComponentName component = new ComponentName(this,
					DeviceReceiver.class);
			if (isAdminActive()) {
				intent = new Intent();
				intent.setClassName("com.android.settings",
						"com.android.settings.DeviceAdminAdd");
				intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
						component);
				startActivity(intent);
			} else {
				intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
				intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
						component);
				startActivity(intent);
			}
		} else if (AppLockerPreference.PREF_AUTO_LOCK.equals(key)) {
			mAutoLock.setChecked((Boolean) newValue);
		}

		return false;
	}
}
