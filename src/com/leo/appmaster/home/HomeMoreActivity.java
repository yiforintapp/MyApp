package com.leo.appmaster.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.activity.QuickHelperActivity;
import com.leo.appmaster.airsig.AirSigActivity;
import com.leo.appmaster.airsig.AirSigSettingActivity;
import com.leo.appmaster.airsig.airsigsdk.ASGui;
import com.leo.appmaster.appmanage.BackUpActivity;
import com.leo.appmaster.appmanage.FlowActivity;
import com.leo.appmaster.appmanage.UninstallActivity;
import com.leo.appmaster.battery.BatteryMainActivity;
import com.leo.appmaster.battery.BatterySettingActivity;
import com.leo.appmaster.callfilter.CallFilterMainActivity;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.intruderprotection.IntruderprotectionActivity;
import com.leo.appmaster.mgr.CallFilterManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.phoneSecurity.PhoneSecurityActivity;
import com.leo.appmaster.phoneSecurity.PhoneSecurityConstants;
import com.leo.appmaster.phoneSecurity.PhoneSecurityGuideActivity;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonSettingItem;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;

/**
 * Created by chenfs on 16-3-28.
 */
public class HomeMoreActivity extends BaseActivity implements View.OnClickListener {
    private final int STRID_SIGNATURE_LOCK = R.string.airsig_settings_activity_title;
    private final int STRID_GESTURE_OR_PSW = R.string.gesture_or_password;
    private final int STRID_OPENED = R.string.has_opened;
    private final int STRID_DID_NOT_OPEN = R.string.did_not_open;
    private final int STRID_DEFAULT_LOCK_TYPE = R.string.airsig_settings_activity_two_set_title;

    private CommonToolbar mCtbMain;
    private RippleView mRvIntruderEntry;
    private RippleView mRvFindLostEntry;
    private RippleView mRvBatteryScreenEntry;
    private RippleView mRvSettingEntry;

    private RippleView mRvCallfilterEntry;
    private RippleView mRvQuickHelperEntry;
    private RippleView mRvWifiEntry;
    private RippleView mRvFlowEntry;
    private RippleView mRvBatteryEntry;

    private RippleView mRvPrivacyContactEntry;
    private RippleView mRvAppBackupEntry;
    private RippleView mRvAppUninstallEntry;

    private RelativeLayout mRlCallFilter;
    private RelativeLayout mRlWifi;
    private RelativeLayout mRlFlowManagement;
    private RelativeLayout mRlBatteryManagement;

    private View mVLine2;
    private View mVLine3;
    private View mVLine4;

    private RippleView mAirSigSwitch;
    private RippleView mDefultLockType;
    private TextView mTvAirSigSwitch;
    private TextView mTvDefultLockType;

    private boolean mIsHasCallFilterRecords = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_more);
        initUI();
        tryShowOldEntry();
    }

    private void tryShowOldEntry() {
        boolean needhide = LeoSettings.getBoolean(PrefConst.KEY_NEED_HIDE_BATTERY_FLOW_AND_WIFI, false);
        LeoLog.i("need hide", "when use ,need hide = " + needhide);
        if (needhide) {
            mRlWifi.setVisibility(View.GONE);
            mRlFlowManagement.setVisibility(View.GONE);
            mRlBatteryManagement.setVisibility(View.GONE);
            mVLine2.setVisibility(View.GONE);
            mVLine3.setVisibility(View.GONE);
            mVLine4.setVisibility(View.GONE);
        }
        boolean hide2 = AppMasterPreference.getInstance(this).getIsNeedCutBackupUninstallAndPrivacyContact();
        LeoLog.i("need hide", "when use ,hide2  = " + hide2);
        if (hide2) {
            findViewById(R.id.rl_backup_content).setVisibility(View.GONE);
            findViewById(R.id.rl_uninstall_content).setVisibility(View.GONE);
            findViewById(R.id.rl_privacycontact_content).setVisibility(View.GONE);
            findViewById(R.id.v_line_backup).setVisibility(View.GONE);
            findViewById(R.id.v_line_uninstall).setVisibility(View.GONE);
            findViewById(R.id.v_line_battery).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkCallFilterRecordCount();
        updateAirSig();
    }

    private void updateAirSig() {
        boolean isAirSigVaild = ASGui.getSharedInstance().isValidLicense();
        if (!isAirSigVaild) {
            mTvAirSigSwitch.setText(STRID_DID_NOT_OPEN);
            mTvDefultLockType.setText(STRID_GESTURE_OR_PSW);
            return;
        }

        boolean isAirsigOn = LeoSettings.getBoolean(AirSigActivity.AIRSIG_SWITCH, false);
        if (isAirsigOn) {
            mTvAirSigSwitch.setText(STRID_OPENED);
        } else {
            mTvAirSigSwitch.setText(STRID_DID_NOT_OPEN);
        }

        int unlockType = LeoSettings.getInteger(AirSigSettingActivity.UNLOCK_TYPE,
                AirSigSettingActivity.NOMAL_UNLOCK);

        if (isAirsigOn) {
            if (unlockType == AirSigSettingActivity.NOMAL_UNLOCK) {
                mTvDefultLockType.setText(STRID_GESTURE_OR_PSW);
            } else {
                mTvDefultLockType.setText(STRID_SIGNATURE_LOCK);
            }
        } else {
            mTvDefultLockType.setText(STRID_GESTURE_OR_PSW);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void checkCallFilterRecordCount() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                CallFilterManager mCallManger = (CallFilterManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                LeoLog.i("tess", "getCallFilterGrCount = " + mCallManger.getCallFilterGrCount());
                if (mCallManger.getCallFilterGrCount() != 0) {
                    mIsHasCallFilterRecords = true;
                } else {
                    mIsHasCallFilterRecords = false;
                }
            }
        });
    }

    private void initUI() {
        boolean isAigSigCanUse = ASGui.getSharedInstance().isSensorAvailable();

        mCtbMain = (CommonToolbar) findViewById(R.id.ctb_main);
        mCtbMain.setToolbarTitle(R.string.lock_more);

        //签字解锁部分
        mAirSigSwitch = (RippleView) findViewById(R.id.rv_airsig_visi);
        mAirSigSwitch.setOnClickListener(this);
        mTvAirSigSwitch = (TextView) findViewById(R.id.tv_airsig_summary);
        //设置默认解锁方式部分
        mDefultLockType = (RippleView) findViewById(R.id.rv_airsig_visi_two);
        mDefultLockType.setOnClickListener(this);
        mTvDefultLockType = (TextView) findViewById(R.id.tv_airsig_summary_two);

        if (isAigSigCanUse) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "settings", "airsig_sh");
            //签字解锁部分
            mTvAirSigSwitch.setText(STRID_SIGNATURE_LOCK);
            //设置默认解锁方式部分
            mTvDefultLockType.setText(STRID_DEFAULT_LOCK_TYPE);
            LeoLog.d("testUse", "can use");
        } else {
            View lineView = findViewById(R.id.white_line_airsig);
            lineView.setVisibility(View.GONE);
            View lineViewTwo = findViewById(R.id.line_airsig_two);
            lineViewTwo.setVisibility(View.GONE);
            mAirSigSwitch.setVisibility(View.GONE);
            mDefultLockType.setVisibility(View.GONE);
            LeoLog.d("testUse", "can not use");
        }

        mVLine2 = findViewById(R.id.v_line2);
        mVLine3 = findViewById(R.id.v_line3);
        mVLine4 = findViewById(R.id.v_line4);

        mRvQuickHelperEntry = (RippleView) findViewById(R.id.rv_home_more_quickhelper);
        mRvQuickHelperEntry.setOnClickListener(this);

        mRvPrivacyContactEntry = (RippleView) findViewById(R.id.rv_home_more_privacycontact);
        mRvPrivacyContactEntry.setOnClickListener(this);

        mRvAppBackupEntry = (RippleView) findViewById(R.id.rv_home_more_backup);
        mRvAppBackupEntry.setOnClickListener(this);

        mRvAppUninstallEntry = (RippleView) findViewById(R.id.rv_home_more_uninstall);
        mRvAppUninstallEntry.setOnClickListener(this);


        mRvIntruderEntry = (RippleView) findViewById(R.id.rv_home_more_intruder);
        mRvIntruderEntry.setOnClickListener(this);
        mRvFindLostEntry = (RippleView) findViewById(R.id.rv_home_more_findlost);
        mRvFindLostEntry.setOnClickListener(this);
        mRvBatteryScreenEntry = (RippleView) findViewById(R.id.rv_home_more_screenview);
        mRvBatteryScreenEntry.setOnClickListener(this);
        mRvSettingEntry = (RippleView) findViewById(R.id.rv_home_more_setting);
        mRvSettingEntry.setOnClickListener(this);

        mRvCallfilterEntry = (RippleView) findViewById(R.id.rv_home_more_callfilter);
        mRvCallfilterEntry.setOnClickListener(this);
        mRvWifiEntry = (RippleView) findViewById(R.id.rv_home_more_wifi);
        mRvWifiEntry.setOnClickListener(this);
        mRvFlowEntry = (RippleView) findViewById(R.id.rv_home_more_flow);
        mRvFlowEntry.setOnClickListener(this);
        mRvBatteryEntry = (RippleView) findViewById(R.id.rv_home_more_battery);
        mRvBatteryEntry.setOnClickListener(this);

        mRlCallFilter = (RelativeLayout) findViewById(R.id.rl_callfilter_content);
        mRlWifi = (RelativeLayout) findViewById(R.id.rl_wifi_content);
        mRlFlowManagement = (RelativeLayout) findViewById(R.id.rl_flow_content);
        mRlBatteryManagement = (RelativeLayout) findViewById(R.id.rl_battery_content);

    }

    private void goToOpenAirSig() {
        SDKWrapper.addEvent(this, SDKWrapper.P1, "settings", "airsig");
        Intent intent = new Intent(this, AirSigActivity.class);
        startActivity(intent);
    }

    private void gotoSetAirSigLock() {
        Intent intent = new Intent(this, AirSigSettingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rv_home_more_quickhelper:
                goToQuickHelper();
                break;
            case R.id.rv_home_more_privacycontact:
                goToPrivacyContact();
                break;
            case R.id.rv_home_more_backup:
                goToAppBackup();
                break;
            case R.id.rv_home_more_uninstall:
                goToAppUninstall();
                break;
            case R.id.rv_home_more_intruder:
                goToIntruderPretection();
                break;
            case R.id.rv_home_more_findlost:
                goToFindLost();
                break;
            case R.id.rv_home_more_screenview:
                goToBatteryScreen();
                break;
            case R.id.rv_home_more_callfilter:
                goToCallfilter();
                break;
            case R.id.rv_home_more_wifi:
                goToWifi();
                break;
            case R.id.rv_home_more_flow:
                goToFlowManagement();
                break;
            case R.id.rv_home_more_battery:
                goToBatteryManagement();
                break;
            case R.id.rv_home_more_setting:
                goToMainSetting();
                break;
            case R.id.rv_airsig_visi:
                goToOpenAirSig();
                break;
            case R.id.rv_airsig_visi_two:
                gotoSetAirSigLock();
                break;
            default:
                break;
        }
    }

    private void goToQuickHelper() {
        Intent dlIntent = new Intent(this, QuickHelperActivity.class);
        startActivity(dlIntent);
    }

    private void goToAppUninstall() {

        Intent dlIntent = new Intent(this, UninstallActivity.class);
        startActivity(dlIntent);
    }

    private void goToAppBackup() {
        Intent dlIntent = new Intent(this, BackUpActivity.class);
        startActivity(dlIntent);
    }

    private void goToPrivacyContact() {
        Intent dlIntent = new Intent(this, PrivacyContactActivity.class);
        startActivity(dlIntent);
    }

    private void goToBatteryManagement() {
        Intent dlIntent = new Intent(this, BatteryMainActivity.class);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "more", "battery");
        startActivity(dlIntent);
    }

    private void goToFlowManagement() {
        Intent intent = new Intent(this, FlowActivity.class);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "more", "dataflow");
        startActivity(intent);
    }

    private void goToWifi() {
        int count2 = LeoSettings.getInteger(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_WIFI_SECURITY, 0);
        LeoSettings.setInteger(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_WIFI_SECURITY, count2 + 1);
        Intent mIntent = new Intent(this, WifiSecurityActivity.class);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "more", "wifi");
        startActivity(mIntent);
    }

    private void goToCallfilter() {
        int count = LeoSettings.getInteger(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_CALLFILTER, 0);
        LeoSettings.setInteger(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_CALLFILTER, count + 1);
        Intent callFilter = new Intent(this, CallFilterMainActivity.class);
        if (mIsHasCallFilterRecords) {
            callFilter.putExtra("needMoveToTab2", true);
        }
        SDKWrapper.addEvent(this, SDKWrapper.P1, "more", "block");
        startActivity(callFilter);
    }

    private void goToMainSetting() {
        Intent intent = new Intent(this, MainSettingActivity.class);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "more", "settings");
        startActivity(intent);
    }

    private void goToBatteryScreen() {
        Intent intent = new Intent(this, BatterySettingActivity.class);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "more", "charge_scr");
        startActivity(intent);
    }

    private void goToFindLost() {
        LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        boolean flag = manager.isUsePhoneSecurity();
        Intent intent = null;
        if (!flag) {
            intent = new Intent(this, PhoneSecurityGuideActivity.class);
            intent.putExtra(PhoneSecurityConstants.KEY_FORM_HOME_SECUR, true);
        } else {
            intent = new Intent(this, PhoneSecurityActivity.class);
        }
        try {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "more", "theft");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToIntruderPretection() {
        Intent intent = new Intent(this, IntruderprotectionActivity.class);
        SDKWrapper.addEvent(this, SDKWrapper.P1, "more", "intruder");
        startActivity(intent);
    }

}

