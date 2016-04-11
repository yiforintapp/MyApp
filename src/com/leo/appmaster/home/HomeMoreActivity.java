package com.leo.appmaster.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.activity.QuickHelperActivity;
import com.leo.appmaster.airsig.AirSigActivity;
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
    private final int STRID_DEFAULT_LOCK_TYPE = R.string.airsig_settings_activity_two_set_title;
    private final int STRID_INTRUDER = R.string.home_tab_instruder;
    private final int STRID_FINDLOST = R.string.home_tab_lost;
    private final int STRID_CHARGESCR = R.string.batterymanage_switch_screen;
    private final int STRID_CALLFILTER = R.string.quick_helper_callfilter;
    private final int STRID_QUICKHELPER = R.string.hp_helper_shot;
    private final int STRID_WIFI = R.string.home_tab_wifi;
    private final int STRID_FLOW = R.string.quick_helper_flow_manage;
    private final int STRID_BATTERY = R.string.quick_helper_elec_manage;
    private final int STRID_UNINSTALL = R.string.quick_helper_app_uninstall;
    private final int STRID_BACKUP = R.string.quick_helper_app_backup;
    private final int STRID_PRIVACY_CONTACT = R.string.privacy_contacts;
    private final int STRID_SETTING = R.string.setting;

    private final int ICON_ID_SIGNATURE_LOCK = R.drawable.ic_more_airsig;
    private final int ICON_ID_DEFAULT_LOCK_TYPE = R.drawable.ic_more_defaultlock;
    private final int ICON_ID_INTRUDER = R.drawable.ic_more_intruder;
    private final int ICON_ID_FINDLOST = R.drawable.ic_more_findlost;
    private final int ICON_ID_CHARGESCR = R.drawable.ic_more_chargescr;
    private final int ICON_ID_CALLFILTER = R.drawable.ic_more_callfilter;
    private final int ICON_ID_QUICKHELPER = R.drawable.ic_more_quickhelper;
    private final int ICON_ID_WIFI = R.drawable.ic_more_wifi;
    private final int ICON_ID_FLOW = R.drawable.ic_more_flow;
    private final int ICON_ID_BATTERY = R.drawable.ic_more_battery;
    private final int ICON_ID_UNINSTALL = R.drawable.ic_more_uninstall;
    private final int ICON_ID_BACKUP = R.drawable.ic_more_backup;
    private final int ICON_ID_PRIVACY_CONTACT = R.drawable.ic_more_privacy_contact;
    private final int ICON_ID_SETTING = R.drawable.ic_more_setting;

    //Summary部分的文案
    private final int STRID_GESTURE_OR_PSW = R.string.gesture_or_password;
    private final int STRID_OPENED = R.string.has_opened;
    private final int STRID_DID_NOT_OPEN = R.string.did_not_open;

    private CommonToolbar mCtbMain;

    private CommonSettingItem mItemAirSigEntry;

    private CommonSettingItem mItemIntruderEntry;
    private CommonSettingItem mItemFindLostEntry;
    private CommonSettingItem mItemBatteryScreenEntry;
    private CommonSettingItem mItemSettingEntry;
    private CommonSettingItem mItemCallfilterEntry;
    private CommonSettingItem mItemQuickHelperEntry;
    private CommonSettingItem mItemWifiEntry;
    private CommonSettingItem mItemFlowEntry;
    private CommonSettingItem mItemBatteryEntry;
    private CommonSettingItem mItemPrivacyContactEntry;
    private CommonSettingItem mItemAppBackupEntry;
    private CommonSettingItem mItemAppUninstallEntry;

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
            mItemWifiEntry.setVisibility(View.GONE);
            mItemFlowEntry.setVisibility(View.GONE);
            mItemBatteryEntry.setVisibility(View.GONE);

            findViewById(R.id.line_8).setVisibility(View.GONE);
            findViewById(R.id.line_9).setVisibility(View.GONE);
            findViewById(R.id.line_7).setVisibility(View.GONE);
        }
        boolean hide2 = AppMasterPreference.getInstance(this).getIsNeedCutBackupUninstallAndPrivacyContact();
        LeoLog.i("need hide", "when use ,hide2  = " + hide2);
        if (hide2) {
            mItemAppBackupEntry.setVisibility(View.GONE);
            mItemAppUninstallEntry.setVisibility(View.GONE);
            mItemPrivacyContactEntry.setVisibility(View.GONE);

            findViewById(R.id.line_11).setVisibility(View.GONE);
            findViewById(R.id.line_12).setVisibility(View.GONE);
            findViewById(R.id.line_10).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkCallFilterRecordCount();
        updateAirSig();
    }

    private void initUI() {
        mCtbMain = (CommonToolbar) findViewById(R.id.ctb_main);
        mCtbMain.setToolbarTitle(R.string.lock_more);

        //签字解锁部分
        mItemAirSigEntry = (CommonSettingItem) findViewById(R.id.item_airsig);
        mItemAirSigEntry.setIcon(ICON_ID_SIGNATURE_LOCK);
        mItemAirSigEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToOpenAirSig();
            }
        });

        boolean isAigSigCanUse = ASGui.getSharedInstance().isSensorAvailable();
        if (isAigSigCanUse) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "settings", "airsig_sh");
            //签字解锁部分
            mItemAirSigEntry.setTitle(STRID_SIGNATURE_LOCK);
            //设置默认解锁方式部分
        } else {
            View line1 = findViewById(R.id.line_1);
            mItemAirSigEntry.setVisibility(View.GONE);
            line1.setVisibility(View.GONE);
        }

        //快捷小助手部分
        mItemQuickHelperEntry = (CommonSettingItem) findViewById(R.id.item_quickhelper);
        mItemQuickHelperEntry.setIcon(ICON_ID_QUICKHELPER);
        mItemQuickHelperEntry.setTitle(STRID_QUICKHELPER);
        mItemQuickHelperEntry.setSummaryVisable(false);
        mItemQuickHelperEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToQuickHelper();
            }
        });

        //隐私联系人部分
        mItemPrivacyContactEntry = (CommonSettingItem) findViewById(R.id.item_privacycontact);
        mItemPrivacyContactEntry.setIcon(ICON_ID_PRIVACY_CONTACT);
        mItemPrivacyContactEntry.setTitle(STRID_PRIVACY_CONTACT);
        mItemPrivacyContactEntry.setSummaryVisable(false);
        mItemPrivacyContactEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPrivacyContact();
            }
        });

        //应用备份
        mItemAppBackupEntry = (CommonSettingItem) findViewById(R.id.item_backup);
        mItemAppBackupEntry.setIcon(ICON_ID_BACKUP);
        mItemAppBackupEntry.setTitle(STRID_BACKUP);
        mItemAppBackupEntry.setSummaryVisable(false);
        mItemAppBackupEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAppBackup();
            }
        });

        //应用卸载
        mItemAppUninstallEntry = (CommonSettingItem) findViewById(R.id.item_uninstall);
        mItemAppUninstallEntry.setIcon(ICON_ID_UNINSTALL);
        mItemAppUninstallEntry.setTitle(STRID_UNINSTALL);
        mItemAppUninstallEntry.setSummaryVisable(false);
        mItemAppUninstallEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAppUninstall();
            }
        });

        //入侵者
        mItemIntruderEntry = (CommonSettingItem) findViewById(R.id.item_intruder);
        mItemIntruderEntry.setIcon(ICON_ID_INTRUDER);
        mItemIntruderEntry.setTitle(STRID_INTRUDER);
        mItemIntruderEntry.setSummaryVisable(false);
        mItemIntruderEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToIntruderPretection();
            }
        });

        //手机防盗
        mItemFindLostEntry = (CommonSettingItem) findViewById(R.id.item_findlost);
        mItemFindLostEntry.setIcon(ICON_ID_FINDLOST);
        mItemFindLostEntry.setTitle(STRID_FINDLOST);
        mItemFindLostEntry.setSummaryVisable(false);
        mItemFindLostEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFindLost();
            }
        });

        //充电屏保
        mItemBatteryScreenEntry = (CommonSettingItem) findViewById(R.id.item_chargescr);
        mItemBatteryScreenEntry.setIcon(ICON_ID_CHARGESCR);
        mItemBatteryScreenEntry.setTitle(STRID_CHARGESCR);
        mItemBatteryScreenEntry.setSummaryVisable(false);
        mItemBatteryScreenEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToBatteryScreen();
            }
        });

        //设置
        mItemSettingEntry = (CommonSettingItem) findViewById(R.id.item_setting);
        mItemSettingEntry.setIcon(ICON_ID_SETTING);
        mItemSettingEntry.setTitle(STRID_SETTING);
        mItemSettingEntry.setSummaryVisable(false);
        mItemSettingEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMainSetting();
            }
        });

        //骚扰拦截
        mItemCallfilterEntry = (CommonSettingItem) findViewById(R.id.item_callfilter);
        mItemCallfilterEntry.setIcon(ICON_ID_CALLFILTER);
        mItemCallfilterEntry.setTitle(STRID_CALLFILTER);
        mItemCallfilterEntry.setSummaryVisable(false);
        mItemCallfilterEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCallfilter();
            }
        });

        //wifi
        mItemWifiEntry = (CommonSettingItem) findViewById(R.id.item_wifi);
        mItemWifiEntry.setIcon(ICON_ID_WIFI);
        mItemWifiEntry.setTitle(STRID_WIFI);
        mItemWifiEntry.setSummaryVisable(false);
        mItemWifiEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToWifi();
            }
        });

        //流量管理
        mItemFlowEntry = (CommonSettingItem) findViewById(R.id.item_flow);
        mItemFlowEntry.setIcon(ICON_ID_FLOW);
        mItemFlowEntry.setTitle(STRID_FLOW);
        mItemFlowEntry.setSummaryVisable(false);
        mItemFlowEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFlowManagement();
            }
        });

        //电量管理
        mItemBatteryEntry = (CommonSettingItem) findViewById(R.id.item_battery);
        mItemBatteryEntry.setIcon(ICON_ID_BATTERY);
        mItemBatteryEntry.setTitle(STRID_BATTERY);
        mItemBatteryEntry.setSummaryVisable(false);
        mItemBatteryEntry.setRippleViewOnClickLinstener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToBatteryManagement();
            }
        });

    }

    private void updateAirSig() {
        boolean isAirSigVaild = ASGui.getSharedInstance().isValidLicense();
        if (!isAirSigVaild) {
            mItemAirSigEntry.setSummary(STRID_DID_NOT_OPEN);
            return;
        }

        boolean isAirsigOn = LeoSettings.getBoolean(AirSigActivity.AIRSIG_SWITCH, false);
        if (isAirsigOn) {
            mItemAirSigEntry.setSummary(STRID_OPENED);
        } else {
            mItemAirSigEntry.setSummary(STRID_DID_NOT_OPEN);
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


    private void goToOpenAirSig() {
        SDKWrapper.addEvent(this, SDKWrapper.P1, "settings", "airsig");
        Intent intent = new Intent(this, AirSigActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

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

