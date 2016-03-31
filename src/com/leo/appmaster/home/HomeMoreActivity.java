package com.leo.appmaster.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.appmanage.FlowActivity;
import com.leo.appmaster.battery.BatteryMainActivity;
import com.leo.appmaster.battery.BatterySettingActivity;
import com.leo.appmaster.callfilter.CallFilterMainActivity;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.intruderprotection.IntruderprotectionActivity;
import com.leo.appmaster.mgr.CallFilterManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.phoneSecurity.PhoneSecurityActivity;
import com.leo.appmaster.phoneSecurity.PhoneSecurityConstants;
import com.leo.appmaster.phoneSecurity.PhoneSecurityGuideActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;

/**
 * Created by chenfs on 16-3-28.
 */
public class HomeMoreActivity extends BaseActivity implements View.OnClickListener {
    private CommonToolbar mCtbMain;
    private RippleView mRvIntruderEntry;
    private RippleView mRvFindLostEntry;
    private RippleView mRvBatteryScreenEntry;
    private RippleView mRvSettingEntry;

    private RippleView mRvCallfilterEntry;
    private RippleView mRvWifiEntry;
    private RippleView mRvFlowEntry;
    private RippleView mRvBatteryEntry;

    private RelativeLayout mRlCallFilter;
    private RelativeLayout mRlWifi;
    private RelativeLayout mRlFlowManagement;
    private RelativeLayout mRlBatteryManagement;

    private View mVLine2;
    private View mVLine3;
    private View mVLine4;


    private boolean mIsHasCallFilterRecords = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_more);
        initUI();
        tryShowOldEntry();
    }

    private void tryShowOldEntry() {
        boolean needhide = LeoSettings.getBoolean(PrefConst.KEY_NEED_HIDE_BATTERY_FLOW_AND_WIFI,false);
        if (needhide) {
            mRlWifi.setVisibility(View.GONE);
            mRlFlowManagement.setVisibility(View.GONE);
            mRlBatteryManagement.setVisibility(View.GONE);
            mVLine2.setVisibility(View.GONE);
            mVLine3.setVisibility(View.GONE);
            mVLine4.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        mCtbMain = (CommonToolbar) findViewById(R.id.ctb_main);
        mCtbMain.setToolbarTitle(R.string.lock_more);

        mVLine2 = findViewById(R.id.v_line2);
        mVLine3 = findViewById(R.id.v_line3);
        mVLine4 = findViewById(R.id.v_line4);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
            default:
                break;
        }
    }

    private void goToBatteryManagement() {
        Intent dlIntent = new Intent(this, BatteryMainActivity.class);
        startActivity(dlIntent);
    }

    private void goToFlowManagement() {
        Intent intent = new Intent(this, FlowActivity.class);
        startActivity(intent);
    }

    private void goToWifi() {
        int count2 = LeoSettings.getInteger(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_WIFI_SECURITY, 0);
        LeoSettings.setInteger(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_WIFI_SECURITY, count2 + 1);
        Intent mIntent = new Intent(this, WifiSecurityActivity.class);
        startActivity(mIntent);
    }

    private void goToCallfilter() {
        int count = LeoSettings.getInteger(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_CALLFILTER, 0);
        LeoSettings.setInteger(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_CALLFILTER, count + 1);
        Intent callFilter = new Intent(this, CallFilterMainActivity.class);
        if (mIsHasCallFilterRecords) {
            callFilter.putExtra("needMoveToTab2", true);
        }
        startActivity(callFilter);
    }

    private void goToMainSetting() {
        Intent intent = new Intent(this, MainSettingActivity.class);
        startActivity(intent);
    }

    private void goToBatteryScreen() {
        Intent intent = new Intent(this, BatterySettingActivity.class);
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
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToIntruderPretection() {
        Intent intent = new Intent(this, IntruderprotectionActivity.class);
        startActivity(intent);
    }

}

