
package com.leo.appmaster.applocker;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.fragment.LockFragment;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.BasePreferenceActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.DipPixelUtil;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.TextView;

public class LockTimeSetting extends BasePreferenceActivity implements OnPreferenceChangeListener,
        OnPreferenceClickListener {
    private CommonTitleBar mTitle;
    private Preference mChangeLockTime;
    private CheckBoxPreference mAutoLock;
    private boolean mShouldLockOnRestart = true;
    private int mHelpSettingCurrent;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_time_setting);
        addPreferencesFromResource(R.xml.setting);
        initUI();
        handIntent();
        setupPreference();
        createResultToHelpSetting();
    }

    private void initUI() {
        mTitle = (CommonTitleBar) findViewById(R.id.layout_lock_time_title_bar);
        mTitle.setTitle(R.string.lock_help_lock_setting_button);
        mTitle.openBackView();
    }

    @SuppressWarnings("deprecation")
    private void setupPreference() {
        mAutoLock = (CheckBoxPreference) findPreference(AppMasterPreference.PREF_AUTO_LOCK);
        mChangeLockTime = (Preference) findPreference(AppMasterPreference.PREF_RELOCK_TIME);
        // mUnLockAllApp = (CheckBoxPreference)
        // findPreference(AppMasterPreference.PREF_UNLOCK_ALL_APP);
        mAutoLock.setOnPreferenceChangeListener(this);
        // mUnLockAllApp.setOnPreferenceChangeListener(this);
        mChangeLockTime.setOnPreferenceClickListener(this);
        getPreferenceScreen().removePreference(
                findPreference(AppMasterPreference.PREF_LOCKER_THEME));
        getPreferenceScreen().removePreference(
                findPreference(AppMasterPreference.PREF_FORBIND_UNINSTALL));
        getPreferenceScreen().removePreference(findPreference("app_lock_clean"));
        getPreferenceScreen()
                .removePreference(findPreference(AppMasterPreference.PREF_SET_PROTECT));
        getPreferenceScreen().removePreference(
                findPreference(AppMasterPreference.PREF_LOCK_SETTING));
        getPreferenceScreen().removePreference(findPreference("change_passwd"));
        getPreferenceScreen().removePreference(findPreference("set_passwd_tip"));
        getPreferenceScreen().removePreference(
                findPreference(AppMasterPreference.PREF_NEW_APP_LOCK_TIP));
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

    private void showLockPage() {
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
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityForResult(intent, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mShouldLockOnRestart = false;
        super.onActivityResult(requestCode, resultCode, data);

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
                                        LockTimeSetting.this)
                                        .setRelockTimeout(
                                                valueString[whichButton]);
                                SDKWrapper.addEvent(LockTimeSetting.this,
                                        SDKWrapper.P1, "lock_setting",
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

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (AppMasterPreference.PREF_RELOCK_TIME.equals(key)) {
            onCreateChoiceDialog(AppMasterPreference.getInstance(this)
                    .getRelockTimeout());
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (AppMasterPreference.PREF_AUTO_LOCK.equals(key)) {
            mAutoLock.setChecked((Boolean) newValue);
            if (!((Boolean) newValue)) {
                SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting",
                        "cancel_auto");
            }
        }
        // else if (AppMasterPreference.PREF_UNLOCK_ALL_APP.equals(key)) {
        // mUnLockAllApp.setChecked((Boolean) newValue);
        // AppMasterPreference.getInstance(this).setUnlockAllApp((Boolean)
        // newValue);
        // }
        return false;
    }

    private void createResultToHelpSetting() {
        Intent intent = new Intent();
        intent.putExtra("help_setting_current", mHelpSettingCurrent);
        this.setResult(RESULT_OK, intent);
    }

    private void handIntent() {
        Intent intent = getIntent();
        int current = intent.getIntExtra("help_setting_current", 10001);
        if (current != 10001) {
            mHelpSettingCurrent = current;
        }
    }
}
