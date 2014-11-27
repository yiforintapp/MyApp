package com.leo.appmaster.applocker;

import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.sdk.BasePreferenceActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leoers.leoanalytics.LeoStat;

public class LockOptionActivity extends BasePreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener {

	private CommonTitleBar mTtileBar;
	private SharedPreferences mSp;
	private Preference mTheme,mLockTime, mResetPasswd, mChangeProtectQuestion,
			mChangePasswdTip;

	private CheckBoxPreference mForbidUninstall, mAutoLock;
	private Preference mLockerTheme;
	private Preference mSetProtect;
	private boolean mShouldLockOnRestart = true;

	public static final String TAG_COME_FROM = "come_from";
	public static final int FROM_APPLOCK = 0;
	public static final int FROM_IMAGEHIDE = 1;

	private int mComeFrom = FROM_APPLOCK;

    private SharedPreferences mySharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_option);
		addPreferencesFromResource(R.xml.setting);
		initIntent();
		initUI();
		setupPreference();
	}

	private void initIntent() {
		Intent intent = getIntent();
		mComeFrom = intent.getIntExtra(TAG_COME_FROM, 0);
	}

	private void setupPreference() {
	    mLockerTheme = findPreference(AppMasterPreference.PREF_LOCKER_THEME);
		mForbidUninstall = (CheckBoxPreference) findPreference(AppMasterPreference.PREF_FORBIND_UNINSTALL);
		mAutoLock = (CheckBoxPreference) findPreference(AppMasterPreference.PREF_AUTO_LOCK);
		mSetProtect = findPreference(AppMasterPreference.PREF_SET_PROTECT);
		mLockTime = (Preference) findPreference(AppMasterPreference.PREF_RELOCK_TIME);
		mResetPasswd = (Preference) findPreference("change_passwd");
		mChangeProtectQuestion = (Preference) findPreference("set_passwd_protect");
		mTheme= findPreference("set_locker_theme");
		mChangePasswdTip = (Preference) findPreference("set_passwd_tip");
        mySharedPreferences= getSharedPreferences("LockerThemeOption",LockOptionActivity.this.MODE_WORLD_WRITEABLE);           

		if (!mySharedPreferences.getBoolean("themeOption",false) && mComeFrom != FROM_IMAGEHIDE) {
		    Spanned buttonText = Html.fromHtml(getString(R.string.lockerThemePoit));
            mLockerTheme.setTitle(buttonText);
		}
		
		if (mComeFrom == FROM_IMAGEHIDE) {
            getPreferenceScreen().removePreference(mLockerTheme);
			getPreferenceScreen().removePreference(mAutoLock);
			getPreferenceScreen().removePreference(mLockTime);
			getPreferenceScreen().removePreference(
					findPreference(AppMasterPreference.PREF_NEW_APP_LOCK_TIP));
		}
		mResetPasswd.setOnPreferenceClickListener(this);
		mForbidUninstall.setOnPreferenceChangeListener(this);
		if (mComeFrom == FROM_APPLOCK) {
			mAutoLock.setOnPreferenceChangeListener(this);
			mLockTime.setOnPreferenceClickListener(this);
		}
		mTheme.setOnPreferenceClickListener(this);
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

	private void showLockPage() {
		LeoLog.d("LockOptionActivity", "showLockPage");
		Intent intent = new Intent(this, LockScreenActivity.class);
		int lockType = AppMasterPreference.getInstance(this).getLockType();
		if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_PASSWD);
		} else {
			intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
					LockFragment.LOCK_TYPE_GESTURE);
		}
		intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
				LockFragment.FROM_SELF);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		startActivityForResult(intent, 1000);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LeoLog.d("LockOptionActivity", "onActivityResault: requestCode = "
				+ requestCode + "    resultCode = " + resultCode);
		mShouldLockOnRestart = false;
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (mShouldLockOnRestart) {
			showLockPage();
		} else {
			mShouldLockOnRestart = true;
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
		
        if (!mySharedPreferences.getBoolean("themeOption", false)) {
            Spanned buttonText = Html.fromHtml(getString(R.string.lockerThemePoit));
            mLockerTheme.setTitle(buttonText);
        } else {
            mLockerTheme.setTitle(R.string.lockerTheme);
        }
		super.onResume();
		SDKWrapper.addEvent(this, LeoStat.P1, "lock_setting", "enter");
	}

	private boolean haveProtect() {
		return AppMasterPreference.getInstance(this).hasPswdProtect();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initUI() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.setTitle(R.string.setting);
		mTtileBar.openBackView();
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();
	
		if (AppMasterPreference.PREF_FORBIND_UNINSTALL.equals(key)) {
			mShouldLockOnRestart = true;
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
			if ((Boolean) newValue) {
				SDKWrapper.addEvent(this, LeoStat.P1, "lock_setting",
						"banremove");
			}
		} else if (AppMasterPreference.PREF_AUTO_LOCK.equals(key)) {
			mAutoLock.setChecked((Boolean) newValue);
			if (!((Boolean) newValue)) {
				SDKWrapper.addEvent(this, LeoStat.P1, "lock_setting",
						"cancel_auto");
			}
		}

		return false;
	}
	
	

	@Override
	public void onBackPressed() {
		setResult(11);
		super.onBackPressed();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		
		if (AppMasterPreference.PREF_RELOCK_TIME.equals(key)) {
			onCreateChoiceDialog(AppMasterPreference.getInstance(this)
					.getRelockTimeout());
		} else if ("change_passwd".equals(key)) {
			Intent intent = new Intent(this, LockSettingActivity.class);
			intent.putExtra(LockSettingActivity.RESET_PASSWD_FLAG, true);
			startActivityForResult(intent, 0);
			SDKWrapper.addEvent(this, LeoStat.P1, "lock_setting", "changepwd");
		} else if ("set_passwd_protect".equals(key)) {
			Intent intent = new Intent(this, PasswdProtectActivity.class);
			startActivityForResult(intent, 0);
			SDKWrapper.addEvent(this, LeoStat.P1, "lock_setting", "pwdp");
		} else if ("set_passwd_tip".equals(key)) {
			Intent intent = new Intent(this, PasswdTipActivity.class);
			startActivityForResult(intent, 0);
			SDKWrapper.addEvent(this, LeoStat.P1, "lock_setting", "pwdn");
		}else if("set_locker_theme".equals(key)){
            Editor editor=mySharedPreferences.edit();
            editor.putBoolean("themeOption", true);
            editor.commit();
		    
			Intent intent=new Intent(LockOptionActivity.this,LockerTheme.class);
			intent.putExtra("need_lock", true);
			startActivityForResult(intent, 0);
			SDKWrapper.addEvent(LockOptionActivity.this, LeoStat.P1, "theme_enter", "setting");
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
								AppMasterPreference.getInstance(
										LockOptionActivity.this)
										.setRelockTimeout(
												valueString[whichButton]);
								SDKWrapper.addEvent(LockOptionActivity.this,
										LeoStat.P1, "lock_setting",
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
