package com.leo.appmaster.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.leo.appmaster.R;
import com.leo.appmaster.battery.BatterySettingActivity;
import com.leo.appmaster.intruderprotection.IntruderprotectionActivity;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.phoneSecurity.PhoneSecurityActivity;
import com.leo.appmaster.phoneSecurity.PhoneSecurityConstants;
import com.leo.appmaster.phoneSecurity.PhoneSecurityGuideActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;

/**
 * Created by chenfs on 16-3-28.
 */
public class HomeMoreActivity extends BaseActivity implements View.OnClickListener {
    private CommonToolbar mCtbMain;
    private RippleView mRvIntruderEntry;
    private RippleView mRvFindLostEntry;
    private RippleView mRvBatteryScreenEntry;
    private RippleView mRvSettingEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_more);
        initUI();
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

    private void initUI() {
        mCtbMain = (CommonToolbar) findViewById(R.id.ctb_main);
        mCtbMain.setToolbarTitle(R.string.lock_more);
        mRvIntruderEntry = (RippleView) findViewById(R.id.rv_home_more_intruder);
        mRvIntruderEntry.setOnClickListener(this);
        mRvFindLostEntry = (RippleView) findViewById(R.id.rv_home_more_findlost);
        mRvFindLostEntry.setOnClickListener(this);
        mRvBatteryScreenEntry = (RippleView) findViewById(R.id.rv_home_more_screenview);
        mRvBatteryScreenEntry.setOnClickListener(this);
        mRvSettingEntry = (RippleView) findViewById(R.id.rv_home_more_setting);
        mRvSettingEntry.setOnClickListener(this);
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
            case R.id.rv_home_more_setting:
                goToMainSetting();
                break;
            default:
                break;
        }
    }

    private void goToMainSetting() {
//        Intent intent = new Intent(this, IntruderprotectionActivity.class);
//        startActivity(intent);
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

