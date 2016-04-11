package com.leo.appmaster.home;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.activity.PrivacyOptionActivity;
import com.leo.appmaster.airsig.AirSigActivity;
import com.leo.appmaster.airsig.airsigsdk.ASGui;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.PasswdProtectActivity;
import com.leo.appmaster.applocker.PasswdTipActivity;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.applocker.receiver.DeviceReceiverNewOne;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonSettingItem;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAnimationDialog;
import com.leo.appmaster.utils.LeoLog;

/**
 * Created by chenfs on 16-3-28.
 */
public class MainSettingActivity extends BaseActivity implements View.OnClickListener {
    private final int STRID_ADVANCED_PROTECT_ON = R.string.forbid_uninstall_on;
    private final int STRID_ADVANCED_PROTECT_OFF = R.string.forbid_uninstall_off;
    private final int STRID_DIALOG_TITLE_ADVANCED_PROTECT = R.string.title_close_advanced_protect;
    private final int STRID_DIALOG_CONTENT_ADVANCED_PROTECT = R.string.content_close_advanced_protect;
    private final int STRID_SETTING = R.string.setting;
    private final int STRID_ADVANCED_PROTECT_TITLE = R.string.forbid_uninstall;
    private final int STRID_PRIVACY_LISTEN = R.string.home_menu_privacy;
    private final int STRID_PSWTIP = R.string.passwd_notify;
    private final int STRID_PSW_QUESTION = R.string.passwd_protect;
    private final int STRID_CHANGE_LOCK_TYPE = R.string.change_gesture_or_password;

    private CommonToolbar mCtbMain;
    private LEOAnimationDialog mMessageDialog;
    private CommonSettingItem mCsiChangeGstOrPsw;
    private CommonSettingItem mCsiPswQuestion;
    private CommonSettingItem mCsiPswTip;
    private CommonSettingItem mCsiPrivacyListen;
    private CommonSettingItem mCsiAdvancedProtect;

    private LEOAlarmDialog mConfrimCloseDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_setting);
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
        updateAdvancedProtectSwitch();
    }


    private void updateAdvancedProtectSwitch() {
        if (isOldAdminActive() || isNewAdminActive()) {
            mCsiAdvancedProtect.setChecked(true);
            mCsiAdvancedProtect.setSummary(STRID_ADVANCED_PROTECT_ON);
        } else {
            mCsiAdvancedProtect.setChecked(false);
            mCsiAdvancedProtect.setSummary(STRID_ADVANCED_PROTECT_OFF);
        }
    }

    private void initUI() {
        mCtbMain = (CommonToolbar) findViewById(R.id.ctb_main);
        mCtbMain.setToolbarTitle(STRID_SETTING);
        boolean isAigSigCanUse = ASGui.getSharedInstance().isSensorAvailable();

        //更改手势/数字密码部分
        mCsiChangeGstOrPsw = (CommonSettingItem) findViewById(R.id.csi_change_lock_type);
        mCsiChangeGstOrPsw.setTitle(STRID_CHANGE_LOCK_TYPE);
        mCsiChangeGstOrPsw.setSummaryVisable(false);
        mCsiChangeGstOrPsw.setIconVisable(false);
        mCsiChangeGstOrPsw.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChangeLockType();
            }
        });

        //密码问题部分
        mCsiPswQuestion = (CommonSettingItem) findViewById(R.id.csi_pswprotect);
        mCsiPswQuestion.setTitle(STRID_PSW_QUESTION);
        mCsiPswQuestion.setSummaryVisable(false);
        mCsiPswQuestion.setIconVisable(false);
        mCsiPswQuestion.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPswProtect();
            }
        });

        //密码提示部分
        mCsiPswTip = (CommonSettingItem) findViewById(R.id.csi_pswtip);
        mCsiPswTip.setTitle(STRID_PSWTIP);
        mCsiPswTip.setSummaryVisable(false);
        mCsiPswTip.setIconVisable(false);
        mCsiPswTip.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPswTip();
            }
        });

        //智能隐私监控部分
        mCsiPrivacyListen = (CommonSettingItem) findViewById(R.id.csi_privacy_listen);
        mCsiPrivacyListen.setTitle(STRID_PRIVACY_LISTEN);
        mCsiPrivacyListen.setSummaryVisable(false);
        mCsiPrivacyListen.setIconVisable(false);
        mCsiPrivacyListen.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPrivacyListen();
            }
        });

        //高级保护部分
        mCsiAdvancedProtect = (CommonSettingItem) findViewById(R.id.csi_advanced_protect);
        mCsiAdvancedProtect.setType(CommonSettingItem.TYPE_CHECKBOX);
        mCsiAdvancedProtect.setTitle(STRID_ADVANCED_PROTECT_TITLE);
        mCsiAdvancedProtect.setIconVisable(false);
        mCsiAdvancedProtect.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCsiAdvancedProtect.isChecked()) {
                    SDKWrapper.addEvent(MainSettingActivity.this, SDKWrapper.P1, "settings", "pro_off");
                    showConfirmDialog();
                } else {
                    SDKWrapper.addEvent(MainSettingActivity.this, SDKWrapper.P1, "settings", "pro_on");
                    requestDeviceAdmin();
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.rv_setting_sign_lock:
//                goToOpenAirSig();
//                break;
//            case R.id.rv_setting_privacy_listen:
//                goToPrivacyListen();
//                break;
            default:
                break;
        }

    }

    private void goToOpenAirSig() {
        Intent intent = new Intent(this, AirSigActivity.class);
        startActivity(intent);
    }

    private void goToChangeLockType() {
        Intent intent = new Intent(this, LockSettingActivity.class);
        intent.putExtra("reset_passwd", true);
        startActivity(intent);
    }

    private void goToPswProtect() {
        Intent intent = new Intent(this, PasswdProtectActivity.class);
        startActivity(intent);
    }

    private void goToPswTip() {
        Intent intent = new Intent(this, PasswdTipActivity.class);
        startActivity(intent);
    }

    private void goToPrivacyListen() {
        Intent intent = new Intent(this, PrivacyOptionActivity.class);
        startActivity(intent);
    }

    private void requestDeviceAdmin() {
        Intent intent = null;
        ComponentName component = new ComponentName(this, DeviceReceiver.class);
        mLockManager.filterSelfOneMinites();
        mLockManager.filterPackage(Constants.PKG_SETTINGS, 1000);
        intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_extra));
        try {
            startActivityForResult(intent ,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && isOldAdminActive()) {
            SDKWrapper.addEvent(MainSettingActivity.this, SDKWrapper.P1, "settings", "pro_on_suc");
            openAdvanceProtectDialogHandler();
        }
    }

    private void openAdvanceProtectDialogHandler() {
        boolean isTip = AppMasterPreference.getInstance(this)
                .getAdvanceProtectOpenSuccessDialogTip();
        if (isTip) {
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
                    AppMasterPreference.getInstance(MainSettingActivity.this)
                            .setAdvanceProtectOpenSuccessDialogTip(false);
                }
            });
        }
        String content = getString(R.string.prot_open_suc_tip_cnt);
        mMessageDialog.setContent(content);
        mMessageDialog.show();
    }

    private void showConfirmDialog() {
        if (mConfrimCloseDialog == null) {
            mConfrimCloseDialog = new LEOAlarmDialog(this);
        }
        mConfrimCloseDialog.setTitle(this.getResources().getString((STRID_DIALOG_TITLE_ADVANCED_PROTECT)));
        mConfrimCloseDialog.setContent(getString(STRID_DIALOG_CONTENT_ADVANCED_PROTECT));
        mConfrimCloseDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeActiveAdmin();
                SDKWrapper.addEvent(MainSettingActivity.this, SDKWrapper.P1, "settings", "pro_off_suc");
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateSwitch();
                        mConfrimCloseDialog.dismiss();
                    }
                }, 500);
            }
        });
        mConfrimCloseDialog.show();
    }

    private void removeActiveAdmin() {
        ComponentName component = new ComponentName(this, DeviceReceiver.class);
        ComponentName component2 = new ComponentName(this, DeviceReceiverNewOne.class);
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
