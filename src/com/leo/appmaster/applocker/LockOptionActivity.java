package com.leo.appmaster.applocker;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;

public class LockOptionActivity extends PreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener {

	private CommonTitleBar mTtileBar;
	private SharedPreferences mSp;
	private Preference mLockTime, mResetPasswd, mChangeProtectQuestion,
			mChangePasswdTip;

	private CheckBoxPreference mForbidUninstall, mAutoLock;
	private Preference mSetProtect;
	private boolean mGotoSetting;

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
		mLockTime = (Preference) findPreference(AppLockerPreference.PREF_RELOCK_TIME);
		mResetPasswd = (Preference) findPreference("change_passwd");
		mChangeProtectQuestion = (Preference) findPreference("set_passwd_protect");
		mChangePasswdTip = (Preference) findPreference("set_passwd_tip");
		mResetPasswd.setOnPreferenceClickListener(this);
		mForbidUninstall.setOnPreferenceChangeListener(this);
		mAutoLock.setOnPreferenceChangeListener(this);
		mLockTime.setOnPreferenceClickListener(this);
		mChangeProtectQuestion.setOnPreferenceClickListener(this);
		mChangePasswdTip.setOnPreferenceClickListener(this);
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
	protected void onRestart() {
		super.onRestart();
		if (!mGotoSetting) {
			Intent intent = new Intent(this, LockScreenActivity.class);
			int lockType = AppLockerPreference.getInstance(this).getLockType();
			if (lockType == AppLockerPreference.LOCK_TYPE_PASSWD) {
				intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
						LockFragment.LOCK_TYPE_PASSWD);
			} else {
				intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
						LockFragment.LOCK_TYPE_GESTURE);
			}
			intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
					LockFragment.FROM_SELF);
			intent.putExtra(LockScreenActivity.EXTRA_FROM_ACTIVITY,
					LockOptionActivity.class.getName());
			startActivity(intent);
			finish();
		}
		mGotoSetting = false;
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
			mGotoSetting = true;
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

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();

		if (AppLockerPreference.PREF_RELOCK_TIME.equals(key)) {
			onCreateChoiceDialog(AppLockerPreference.getInstance(this)
					.getRelockTimeout());
		} else if ("change_passwd".equals(key)) {
			mGotoSetting = true;
			Intent intent = new Intent(this, LockSettingActivity.class);
			intent.putExtra(LockSettingActivity.RESET_PASSWD_FLAG, true);
			startActivity(intent);
		} else if ("set_passwd_protect".equals(key)) {
			mGotoSetting = true;
			Intent intent = new Intent(this, PasswdProtectActivity.class);
			startActivity(intent);
		} else if ("set_passwd_tip".equals(key)) {
			mGotoSetting = true;
			Intent intent = new Intent(this, PasswdTipActivity.class);
			startActivity(intent);
		}

		return false;
	}

	private void onCreateChoiceDialog(int id) {
		final String[] valueString = getResources().getStringArray(
				R.array.lock_time_items);
		int index = 0;
		for (index = 0; index < valueString.length; index++) {
			if (valueString[index].equals(String.valueOf(id / 1000))) {
				break;
			}
		}
		if (index >= valueString.length) {
			index = 0;
		}

		AlertDialog scaleIconListDlg = new AlertDialog.Builder(this)
				.setTitle(R.string.change_lock_time)
				.setSingleChoiceItems(R.array.lock_time_entrys, index,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								AppLockerPreference.getInstance(
										LockOptionActivity.this)
										.setRelockTimeout(
												valueString[whichButton]);
								dialog.dismiss();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked No so do some stuff */
							}
						}).create();
		// int divierId =
		// scaleIconListDlg.getContext().getResources().getIdentifier("android:id/titleDivider",
		// "id", "android");
		// View divider = scaleIconListDlg.findViewById(divierId);
		// divider.setBackgroundColor(getResources().getColor(R.color.transparent));
		TextView title = new TextView(this);
		title.setText(getString(R.string.change_lock_time));
		title.setTextColor(Color.WHITE);
		title.setTextSize(20);
		title.setPadding(DipPixelUtil.dip2px(this, 20),
				DipPixelUtil.dip2px(this, 10), 0, DipPixelUtil.dip2px(this, 10));
		title.setBackgroundColor(getResources().getColor(
				R.color.dialog_title_area_bg));
		scaleIconListDlg.setCustomTitle(title);
		scaleIconListDlg.show();
	}

}
