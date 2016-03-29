package com.leo.appmaster.home;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.applocker.receiver.DeviceReceiverNewOne;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;

/**
 * Created by chenfs on 16-3-28.
 */
public class MainSettingActivity extends BaseActivity implements View.OnClickListener {
    private PreferenceTable mPt;

    private static final int STRID_OPENED = R.string.has_opened;
    private static final int STRID_DID_NOT_OPEN = R.string.did_not_open;
    private static final int STRID_SIGN_LOCK = R.string.airsig_settings_activity_title;
    private static final int STRID_GESTURE_OR_PSW = R.string.gesture_or_password;
    private static final int STRID_ADVANCED_PROTECT_ON = R.string.forbid_uninstall_on;
    private static final int STRID_ADVANCED_PROTECT_OFF = R.string.forbid_uninstall_off;
    private static final int STRID_DIALOG_TITLE_ADVANCED_PROTECT = R.string.title_close_advanced_protect;
    private static final int STRID_DIALOG_CONTENT_ADVANCED_PROTECT = R.string.content_close_advanced_protect;


    private RippleView mRvChangeGstOrPsw;
    private RippleView mRvSignatureLock;
    private RippleView mRvDefaultLockType;
    private RippleView mRvPswQuestion;
    private RippleView mRvPswTip;
    private RippleView mRvPrivacyListen;
    private RippleView mRvAdvancedProtect;

    private CheckBox mCbAdvancedProtect;

    private TextView mTvSignatureLockOpenOrNot;
    private TextView mTvDefaultLock;
    private TextView mTvAdvancedProtectSmr;

    private LEOAlarmDialog mConfrimCloseDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_setting);
//        mPt = PreferenceTable.getInstance();
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSwitch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConfrimCloseDialog != null) {
            mConfrimCloseDialog.dismiss();
        }
    }

    private void updateSwitch() {
        if (isOldAdminActive() || isNewAdminActive()) {
            mCbAdvancedProtect.setChecked(true);
            mTvAdvancedProtectSmr.setText(STRID_ADVANCED_PROTECT_ON);
        } else {
            mCbAdvancedProtect.setChecked(false);
            mTvAdvancedProtectSmr.setText(STRID_ADVANCED_PROTECT_OFF);
        }
    }

    private void initUI() {
        mRvChangeGstOrPsw = (RippleView) findViewById(R.id.rv_setting_change_lock_type);
        mRvChangeGstOrPsw.setOnClickListener(this);
        mRvSignatureLock = (RippleView) findViewById(R.id.rv_setting_sign_lock);
        mRvSignatureLock.setOnClickListener(this);
        mRvDefaultLockType = (RippleView) findViewById(R.id.rv_setting_default_lock);
        mRvDefaultLockType.setOnClickListener(this);
        mRvPswQuestion = (RippleView) findViewById(R.id.rv_setting_pswprotect);
        mRvPswQuestion.setOnClickListener(this);
        mRvPswTip = (RippleView) findViewById(R.id.rv_setting_pswtip);
        mRvPswTip.setOnClickListener(this);
        mRvPrivacyListen = (RippleView) findViewById(R.id.rv_setting_privacy_listen);
        mRvPrivacyListen.setOnClickListener(this);
        mRvAdvancedProtect = (RippleView) findViewById(R.id.rv_setting_advanced_protect);
        mRvAdvancedProtect.setOnClickListener(this);
        mCbAdvancedProtect = (CheckBox) findViewById(R.id.cb_setting_advanced_protect);

        mTvSignatureLockOpenOrNot = (TextView) findViewById(R.id.tv_sign_lock_summary);
        mTvDefaultLock = (TextView) findViewById(R.id.tv_default_lock_summary);
        mTvAdvancedProtectSmr = (TextView) findViewById(R.id.tv_advanced_protect_summary);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rv_setting_change_lock_type:

                break;
            case R.id.rv_setting_sign_lock:

                break;
            case R.id.rv_setting_default_lock:

                break;
            case R.id.rv_setting_pswprotect:

                break;
            case R.id.rv_setting_pswtip:

                break;
            case R.id.rv_setting_privacy_listen:

                break;
            case R.id.rv_setting_advanced_protect:
                if (mCbAdvancedProtect.isChecked()) {
                    showConfirmDialog();
                } else {
                    requestDeviceAdmin();
                }
                break;
            default:
                break;
        }
    }

    private void requestDeviceAdmin() {
        Intent intent = null;
        ComponentName component = new ComponentName(this,DeviceReceiver.class);
        mLockManager.filterSelfOneMinites();
        mLockManager.filterPackage(Constants.PKG_SETTINGS, 1000);
        intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,component);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,getString(R.string.device_admin_extra));
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showConfirmDialog() {
        if (mConfrimCloseDialog == null) {
            mConfrimCloseDialog = new LEOAlarmDialog(this);
        }
        mConfrimCloseDialog.setTitle(STRID_DIALOG_TITLE_ADVANCED_PROTECT);
        mConfrimCloseDialog.setContent(getString(STRID_DIALOG_CONTENT_ADVANCED_PROTECT));
        mConfrimCloseDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeActiveAdmin();
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSwitch();
                        mConfrimCloseDialog.dismiss();
                    }
                },500);
            }
        });
        mConfrimCloseDialog.show();
    }

    private void removeActiveAdmin() {
        ComponentName component = new ComponentName(this,DeviceReceiver.class);
        ComponentName component2 = new ComponentName(this,DeviceReceiverNewOne.class);
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (isOldAdminActive()) {
            dpm.removeActiveAdmin(component);
        } else if (isNewAdminActive()) {
            dpm.removeActiveAdmin(component2);
        }
    }


    private boolean isOldAdminActive() {
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
}
