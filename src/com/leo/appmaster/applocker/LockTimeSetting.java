
package com.leo.appmaster.applocker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BasePreferenceActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;
import com.leo.appmaster.utils.LeoLog;

import java.util.Arrays;

public class LockTimeSetting extends BasePreferenceActivity implements OnPreferenceChangeListener,
        OnPreferenceClickListener {
    private CommonToolbar mTitle;
    private Preference mChangeLockTime;
    private CheckBoxPreference mAutoLock;
    private int mHelpSettingCurrent;
    private LEOChoiceDialog mTimeSettingDialog;
    private String[] mTimeValue;

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
        mTitle = (CommonToolbar) findViewById(R.id.layout_lock_time_title_bar);
        mTitle.setToolbarTitle(R.string.lock_help_lock_setting_button);
        mTitle.setToolbarColorResource(R.color.ctc);
        mTitle.setOptionMenuVisible(false);
//        mTitle.setTitle(R.string.lock_help_lock_setting_button);
//        mTitle.openBackView();

        mTimeValue = getResources().getStringArray(
                R.array.lock_time_items);
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
//        getPreferenceScreen().removePreference(
//                findPreference(AppMasterPreference.PREF_FORBIND_UNINSTALL));
        getPreferenceScreen().removePreference(findPreference("app_lock_clean"));
        getPreferenceScreen().removePreference(
                findPreference(AppMasterPreference.PREF_HIDE_LOCK_LINE));
        getPreferenceScreen()
                .removePreference(findPreference(AppMasterPreference.PREF_SET_PROTECT));
        getPreferenceScreen()
                .removePreference(findPreference(AppMasterPreference.AIRSIG_SETTING));
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
    }

    @Override
    protected void onResume() {
        String which = AppMasterPreference.getInstance(LockTimeSetting.this).getRelockStringTime();
        LeoLog.d("whatisthis", "which : " + which);

        int j = 0;
        for (int i = 0; i < mTimeValue.length; i++) {
            if (which.equals(mTimeValue[i])) {
                j = i;
                break;
            }
        }

        setName(j);

        super.onResume();
    }

    private void setName(int j) {
        if (j == 0) {
            mChangeLockTime.setSummary(R.string.lock_right_now);
        } else if (j == 1) {
            mChangeLockTime.setSummary(R.string.lock_15_sec);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting", "locktime_15");
        } else if (j == 2) {
            mChangeLockTime.setSummary(R.string.lock_30_sec);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting", "locktime_30");
        } else if (j == 3) {
            mChangeLockTime.setSummary(R.string.lock_1_min);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting", "locktime_60");
        } else if (j == 4) {
            mChangeLockTime.setSummary(R.string.lock_3_min);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting", "locktime_180");
        } else {
            mChangeLockTime.setSummary(R.string.lock_5_min);
            SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting", "locktime_300");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private int getDialogCurrentIndex() {
        String[] mTimeStringValue = getResources().getStringArray(R.array.lock_time_entrys);
        String preTimeValue = AppMasterPreference.getInstance(LockTimeSetting.this)
                .getRelockStringTime();
        int index = -1;
        for (int i = 0; i < mTimeStringValue.length; i++) {
            if (preTimeValue.equals(mTimeValue[i])) {
                index = i;
            }
        }
        return index;
    }

    private void onCreateChoiceDialog(int id) {
        int index = 0;
        for (index = 0; index < mTimeValue.length; index++) {
            if (mTimeValue[index].equals(String.valueOf(id / 1000))) {
                break;
            }
        }
        if (index >= mTimeValue.length) {
            index = 0;
        }

        if (null == mTimeSettingDialog) {
            mTimeSettingDialog = new LEOChoiceDialog(this);
        }
        mTimeSettingDialog.setTitle(getResources().getString(R.string.change_lock_time));
        mTimeSettingDialog.setItemsWithDefaultStyle(Arrays.asList(getResources().getStringArray(
                R.array.lock_time_entrys)), getDialogCurrentIndex());
        mTimeSettingDialog.getItemsListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppMasterPreference.getInstance(LockTimeSetting.this).setRelockTimeout(
                        mTimeValue[position]);
                setName(position);
                SDKWrapper.addEvent(LockTimeSetting.this, SDKWrapper.P1, "lock_setting",
                        mTimeValue[position]);
                mTimeSettingDialog.dismiss();
            }
        });
        mTimeSettingDialog.show();

//        mTimeListView = (ListView) mTimeSettingDialog.findViewById(R.id.item_list);
//        View parentView = (View) mTimeListView.getParent();
//        RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) parentView
//                .getLayoutParams(); // 取控件textView当前的布局参数
//        linearParams.height = (int) (getResources().getDimension(R.dimen.dialog_list_item_height) * 6);
//        Log.i("hasFocus", "  " + linearParams.height);
//        parentView.setLayoutParams(linearParams);
//        mTimeListView.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position,
//                    long id) {
//                AppMasterPreference.getInstance(LockTimeSetting.this).setRelockTimeout(
//                        mTimeValue[position]);
//                setName(position);
//                SDKWrapper.addEvent(LockTimeSetting.this, SDKWrapper.P1, "lock_setting",
//                        mTimeValue[position]);
//                mTimeSettingDialog.dismiss();
//            }
//        });
//        View cancel = mTimeSettingDialog.findViewById(R.id.dlg_bottom_btn);
//        cancel.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mTimeSettingDialog.dismiss();
//            }
//        });
//
//        TextView mTitle = (TextView) mTimeSettingDialog.findViewById(R.id.dlg_title);
//        mTitle.setText(getResources().getString(R.string.change_lock_time));
//        ListAdapter adapter = new TimeListAdapter(this);
//        mTimeListView.setAdapter(adapter);
//        mTimeSettingDialog.show();
//        LeoLog.i("poha_locktime", "getDialogCurrentIndex :"+getDialogCurrentIndex());
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
            AppMasterPreference.getInstance(LockTimeSetting.this).
                    setAutoLock((Boolean) newValue);
            if (!((Boolean) newValue)) {
                SDKWrapper.addEvent(this, SDKWrapper.P1, "lock_setting",
                        "cancel_auto");
            }
        }
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


    class TimeListAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private String[] mTimeStringValue = getResources().getStringArray(R.array.lock_time_entrys);
        String preTimeValue = AppMasterPreference.getInstance(LockTimeSetting.this)
                .getRelockStringTime();

        public TimeListAdapter(Context ctx) {
            inflater = LayoutInflater.from(ctx);
        }

        @Override
        public int getCount() {
            return mTimeValue.length;
        }

        @Override
        public Object getItem(int position) {
            return mTimeValue[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_checkbox_select, parent, false);
                holder = new Holder();
                holder.name = (TextView) convertView.findViewById(R.id.tv_item_content);
                holder.selecte = (ImageView) convertView.findViewById(R.id.iv_selected);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.name.setText(mTimeStringValue[position]);
            if (preTimeValue.equals(mTimeValue[position])) {
                holder.selecte.setImageResource(R.drawable.radio_buttons);
            } else {
                holder.selecte.setImageResource(R.drawable.unradio_buttons);
            }
            return convertView;
        }
    }

    public static class Holder {
        TextView name;
        ImageView selecte;
    }
}
