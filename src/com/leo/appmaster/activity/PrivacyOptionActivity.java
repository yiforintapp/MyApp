package com.leo.appmaster.activity;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import com.leo.appmaster.R;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.sdk.BasePreferenceActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.utils.PrefConst;

/**
 * Created by Jasper on 2015/10/26.
 */
public class PrivacyOptionActivity extends BasePreferenceActivity implements OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    private CheckBoxPreference mMonitorAppPref;
    private CheckBoxPreference mMonitorPicPref;
    private CheckBoxPreference mMonitorVidPref;

    private CommonToolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_option);
        addPreferencesFromResource(R.xml.privacy_option);

        mMonitorAppPref = (CheckBoxPreference) findPreference(PrefConst.KEY_NOTIFY_APP);
        mMonitorPicPref = (CheckBoxPreference) findPreference(PrefConst.KEY_NOTIFY_PIC);
        mMonitorVidPref = (CheckBoxPreference) findPreference(PrefConst.KEY_NOTIFY_VID);

        mMonitorAppPref.setOnPreferenceChangeListener(this);
        mMonitorPicPref.setOnPreferenceChangeListener(this);
        mMonitorVidPref.setOnPreferenceChangeListener(this);

        mMonitorAppPref.setOnPreferenceClickListener(this);
        mMonitorPicPref.setOnPreferenceClickListener(this);
        mMonitorVidPref.setOnPreferenceClickListener(this);

        mToolbar = (CommonToolbar) findViewById(R.id.pri_opt_toobar);
        mToolbar.setToolbarTitle(R.string.home_menu_privacy);
        mToolbar.setToolbarColorResource(R.color.ctc);
        mToolbar.setOptionMenuVisible(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LeoPreference leoPreference = LeoPreference.getInstance();
        boolean notifyApp = leoPreference.getBoolean(PrefConst.KEY_NOTIFY_APP, true);
        mMonitorAppPref.setChecked(notifyApp);

        boolean notifyPic = leoPreference.getBoolean(PrefConst.KEY_NOTIFY_PIC, true);
        mMonitorPicPref.setChecked(notifyPic);

        boolean notifyVid = leoPreference.getBoolean(PrefConst.KEY_NOTIFY_VID, true);
        mMonitorVidPref.setChecked(notifyVid);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        boolean value = LeoPreference.getInstance().getBoolean(key, true);

        LeoPreference.getInstance().putBoolean(key, !value);

        CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
        checkBoxPreference.setChecked(!value);

        boolean newValue = !value;
        String description = null;
        if (PrefConst.KEY_NOTIFY_APP.equals(key)) {
            description = newValue ? "home_sv_appopen" : "home_sv_appclose";
        } else if (PrefConst.KEY_NOTIFY_PIC.equals(key)) {
            description = newValue ? "home_sv_picopen" : "home_sv_picclose";
        } else if (PrefConst.KEY_NOTIFY_VID.equals(key)) {
            description = newValue ? "home_sv_vidopen" : "home_sv_vidclose";
        }
        SDKWrapper.addEvent(this, SDKWrapper.P1, "home", description);
        return false;
    }
}
