
package com.leo.appmaster.applocker;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.Html;
import android.text.Spanned;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.applocker.receiver.DeviceReceiverNewOne;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.DeviceAdminEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.sdk.BasePreferenceActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAnimationDialog;

public class LockOptionActivity extends BasePreferenceActivity implements
        OnPreferenceChangeListener, OnPreferenceClickListener {

    private CommonToolbar mTtileBar;
    private Preference mTheme, mLockSetting, mResetPasswd, mChangeProtectQuestion,
            mChangePasswdTip, mChangeLockTime;

    private CheckBoxPreference mForbidUninstall, mAutoLock, mLockerClean, mHideLockLine;
    private Preference mLockerTheme;
    private Preference mSetProtect;

    public static final String TAG_COME_FROM = "come_from";
    public static final int FROM_APPLOCK = 0;
    public static final int FROM_IMAGEHIDE = 1;
    public static final int FROM_HOME = 2;

    private int mComeFrom = FROM_APPLOCK;
    private LEOAnimationDialog mMessageDialog;

    private SharedPreferences mySharedPreferences;
    private boolean mNewTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_option);
        addPreferencesFromResource(R.xml.setting);
        initIntent();
        initUI();
        setupPreference();

        LeoEventBus.getDefaultBus().register(this);
    }

    private void initIntent() {
        Intent intent = getIntent();
        mComeFrom = intent.getIntExtra(TAG_COME_FROM, 0);
    }

    @SuppressWarnings("deprecation")
    private void setupPreference() {
        mChangeLockTime = (Preference) findPreference(AppMasterPreference.PREF_RELOCK_TIME);
        mLockerTheme = findPreference(AppMasterPreference.PREF_LOCKER_THEME);
        mForbidUninstall = (CheckBoxPreference) findPreference(AppMasterPreference.PREF_FORBIND_UNINSTALL);
        mAutoLock = (CheckBoxPreference) findPreference(AppMasterPreference.PREF_AUTO_LOCK);
        mLockerClean = (CheckBoxPreference) findPreference("app_lock_clean");
        mHideLockLine = (CheckBoxPreference) findPreference(AppMasterPreference.PREF_HIDE_LOCK_LINE);
        mSetProtect = findPreference(AppMasterPreference.PREF_SET_PROTECT);
        mLockSetting = (Preference) findPreference(AppMasterPreference.PREF_LOCK_SETTING);
        mResetPasswd = (Preference) findPreference("change_passwd");
        mChangeProtectQuestion = (Preference) findPreference("set_passwd_protect");
        mTheme = findPreference("set_locker_theme");
        mChangePasswdTip = (Preference) findPreference("set_passwd_tip");
        mySharedPreferences = getSharedPreferences("LockerThemeOption",
                LockOptionActivity.this.MODE_WORLD_WRITEABLE);

        if (!mySharedPreferences.getBoolean("themeOption", false)
                && mComeFrom != FROM_IMAGEHIDE) {
            Spanned buttonText = Html
                    .fromHtml(getString(R.string.lockerThemePoit));
            mLockerTheme.setTitle(buttonText);
        }
        getPreferenceScreen().removePreference(mChangeLockTime);
        getPreferenceScreen().removePreference(mAutoLock);
        if (mComeFrom == FROM_IMAGEHIDE) {
            getPreferenceScreen().removePreference(mLockerTheme);
            getPreferenceScreen().removePreference(mAutoLock);
            getPreferenceScreen().removePreference(mLockSetting);
            getPreferenceScreen().removePreference(mLockerClean);
            getPreferenceScreen().removePreference(mHideLockLine);
            getPreferenceScreen().removePreference(
                    findPreference(AppMasterPreference.PREF_NEW_APP_LOCK_TIP));
        }

        if (mComeFrom == FROM_IMAGEHIDE) {
            getPreferenceScreen().removePreference(mLockerTheme);
            getPreferenceScreen().removePreference(mAutoLock);
            getPreferenceScreen().removePreference(mLockSetting);
            getPreferenceScreen().removePreference(mLockerClean);
            getPreferenceScreen().removePreference(mHideLockLine);
            getPreferenceScreen().removePreference(
                    findPreference(AppMasterPreference.PREF_NEW_APP_LOCK_TIP));
        }

        if (mComeFrom == FROM_HOME) {
            getPreferenceScreen().removePreference(mLockerTheme);
            getPreferenceScreen().removePreference(mResetPasswd);
            getPreferenceScreen().removePreference(mChangeProtectQuestion);
            getPreferenceScreen().removePreference(mChangePasswdTip);
        }

        mResetPasswd.setOnPreferenceClickListener(this);
        mForbidUninstall.setOnPreferenceChangeListener(this);
        if (mComeFrom == FROM_APPLOCK) {
            mAutoLock.setOnPreferenceChangeListener(this);
            mLockSetting.setOnPreferenceClickListener(this);
            mLockerClean.setOnPreferenceChangeListener(this);
            mHideLockLine.setOnPreferenceChangeListener(this);
        }
        mLockerClean.setOnPreferenceChangeListener(this);
        mHideLockLine.setOnPreferenceChangeListener(this);
        mTheme.setOnPreferenceClickListener(this);
        mChangeProtectQuestion.setOnPreferenceClickListener(this);
        mChangePasswdTip.setOnPreferenceClickListener(this);
        mLockSetting.setOnPreferenceClickListener(this);
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

    private boolean isNewAdminActive() {
        DevicePolicyManager manager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName mAdminName = new ComponentName(this, DeviceReceiverNewOne.class);
        if (manager.isAdminActive(mAdminName)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (isAdminActive() || isNewAdminActive()) {
                    mForbidUninstall.setChecked(true);
                    mForbidUninstall.setSummary(R.string.forbid_uninstall_on);
                } else {
                    mForbidUninstall.setChecked(false);
                    mForbidUninstall.setSummary(R.string.forbid_uninstall_off);
                }
            }
        }, 500);

        if (haveProtect()) {
            mSetProtect.setTitle(R.string.passwd_protect);
        } else {
            mSetProtect.setTitle(getString(R.string.passwd_protect) + "("
                    + getString(R.string.not_set) + ")");
        }

        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        mNewTheme = !pref.getLocalThemeSerialNumber().equals(
                pref.getOnlineThemeSerialNumber());
        if (mNewTheme) {
            Spanned buttonText = Html
                    .fromHtml(getString(R.string.lockerThemePoit));
            mLockerTheme.setTitle(buttonText);
        } else {
            mLockerTheme.setTitle(R.string.lockerTheme);
        }

        /* 开启高级保护后提示 */
        openAdvanceProtectDialogHandler();
        super.onResume();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting", "enter");
    }

    private void openAdvanceProtectDialogHandler() {
        boolean isTip = AppMasterPreference.getInstance(this)
                .getAdvanceProtectOpenSuccessDialogTip();
        if (isAdminActive() && isTip) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_dcnts", "gd_dput_real");
            openAdvanceProtectDialogTip();
        }
    }

    private void openAdvanceProtectDialogTip() {
        if (mMessageDialog == null) {
            mMessageDialog = new LEOAnimationDialog(this);
            mMessageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mMessageDialog != null) {
                        mMessageDialog = null;
                    }
                    AppMasterPreference.getInstance(LockOptionActivity.this)
                            .setAdvanceProtectOpenSuccessDialogTip(false);
                }
            });
        }
        String content = getString(R.string.prot_open_suc_tip_cnt);
        mMessageDialog.setContent(content);
        mMessageDialog.show();
    }

    private boolean haveProtect() {
        return AppMasterPreference.getInstance(this).hasPswdProtect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMessageDialog != null) {
            mMessageDialog.dismiss();
            mMessageDialog = null;
        }
        LeoEventBus.getDefaultBus().unregister(this);
    }

    public void onEventMainThread(DeviceAdminEvent event) {
        if (event.getEventId() == EventId.EVENT_DEVICE_ADMIN_DISABLE) {
            updateButtons(false);
        } else if (event.getEventId() == EventId.EVENT_DEVICE_ADMIN_ENABLE) {
            updateButtons(true);
        }
    }

    private void updateButtons(boolean active) {
        if (active) {
            mForbidUninstall.setChecked(true);
            mForbidUninstall.setSummary(R.string.forbid_uninstall_on);
        } else {
            mForbidUninstall.setChecked(false);
            mForbidUninstall.setSummary(R.string.forbid_uninstall_off);
        }
    }

    private void initUI() {

        mTtileBar = (CommonToolbar) findViewById(R.id.layout_title_bar);
        mTtileBar.setToolbarTitle(R.string.lock_setting);
        mTtileBar.setToolbarColorResource(R.color.cb);
        mTtileBar.setOptionMenuVisible(false);
//        mTtileBar.setTitle(R.string.lock_setting);
//        mTtileBar.openBackView();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (AppMasterPreference.PREF_FORBIND_UNINSTALL.equals(key)) {
            Intent intent = null;
            ComponentName component = new ComponentName(this,DeviceReceiver.class);
            ComponentName component2 = new ComponentName(this,DeviceReceiverNewOne.class);
//            if(false){
            if (isAdminActive()) {
                DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                dpm.removeActiveAdmin(component);
            } else if (isNewAdminActive()) {
                DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                dpm.removeActiveAdmin(component2);
            }
            else {
                mLockManager.filterSelfOneMinites();
                mLockManager.filterPackage(Constants.PKG_SETTINGS, 1000);
                intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                        component);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        getString(R.string.device_admin_extra));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if ((Boolean) newValue) {
                SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting",
                        "banremove");
            }
        } else if (AppMasterPreference.PREF_AUTO_LOCK.equals(key)) {
            mAutoLock.setChecked((Boolean) newValue);
            if (!((Boolean) newValue)) {
                SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting",
                        "cancel_auto");
            }
        } else if ("app_lock_clean".equals(key)) {
            mLockerClean.setChecked((Boolean) newValue);
            AppMasterPreference.getInstance(LockOptionActivity.this)
                    .setLockerClean((Boolean) newValue);
            /* SDK:use Unlock the acceleration */
            if ((Boolean) newValue) {
                SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting", "lockboost");
            }
        } else if (AppMasterPreference.PREF_HIDE_LOCK_LINE.equals(key)) {
            if ((Boolean) newValue) {
                SDKWrapper.addEvent(this, SDKWrapper.P1, "trackhide", "setting_on");
            } else {
                SDKWrapper.addEvent(this, SDKWrapper.P1, "trackhide", "setting_off");
            }
            mHideLockLine.setChecked((Boolean) newValue);
            AppMasterPreference.getInstance(LockOptionActivity.this)
                    .setHideLine((Boolean) newValue);
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

        if (AppMasterPreference.PREF_LOCK_SETTING.equals(key)) {
            Intent intent = new Intent(LockOptionActivity.this, LockTimeSetting.class);
            try {
                LockOptionActivity.this.startActivityForResult(intent, 0);
            } catch (Exception e) {
            }
        } else if ("change_passwd".equals(key)) {
            Intent intent = new Intent(this, LockSettingActivity.class);
            intent.putExtra(LockSettingActivity.RESET_PASSWD_FLAG, true);
            startActivityForResult(intent, 0);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting", "changepwd");
        } else if ("set_passwd_protect".equals(key)) {
            Intent intent = new Intent(this, PasswdProtectActivity.class);
            startActivityForResult(intent, 0);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting", "pwdp");
        } else if ("set_passwd_tip".equals(key)) {
            Intent intent = new Intent(this, PasswdTipActivity.class);
            startActivityForResult(intent, 0);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting", "pwdn");
        } else if ("set_locker_theme".equals(key)) {
            Editor editor = mySharedPreferences.edit();
            editor.putBoolean("themeOption", true);
            editor.apply();
            Intent intent = new Intent(LockOptionActivity.this,
                    LockerTheme.class);
            startActivityForResult(intent, 0);
            SDKWrapper.addEvent(LockOptionActivity.this, SDKWrapper.P1,
                    "theme_enter", "setting");
        }
        return false;
    }

}
