package com.leo.appmaster.battery;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.CallFilterContextManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;

public class BatterySettingActivity extends BaseActivity implements View.OnClickListener {
    private CommonToolbar mTitleBar;
    private ImageView checkBox;
    private ImageView checkBoxTwo;
    private RippleView rpBtn;
    private RippleView rpBtnTwo;
//    private boolean mNotiStatusWhenSwitch = false;
    private BatteryManager mBtrManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_setting);
        mBtrManager = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);
        initUI();
        fillData();
    }

    private void fillData() {
        boolean isScreenViewOpen = mBtrManager.getScreenViewStatus();
        if (isScreenViewOpen) {
            checkBox.setImageResource(R.drawable.switch_on);
        } else {
            checkBox.setImageResource(R.drawable.switch_off);
        }

        boolean isBatteryNotiOpen = mBtrManager.getBatteryNotiStatus();
        if (isBatteryNotiOpen) {
            checkBoxTwo.setImageResource(R.drawable.switch_on);
        } else {
            checkBoxTwo.setImageResource(R.drawable.switch_off);
        }
    }

    private void initUI() {
        mTitleBar = (CommonToolbar) findViewById(R.id.ctb_main);
        mTitleBar.setToolbarTitle(R.string.batterymanage_setting_title);
        mTitleBar.setToolbarColorResource(R.color.cb);
        mTitleBar.setOptionMenuVisible(false);

        rpBtn = (RippleView) findViewById(R.id.rv_item_screenview);
        rpBtn.setOnClickListener(this);
        rpBtnTwo = (RippleView) findViewById(R.id.rv_item_noti);
        rpBtnTwo.setOnClickListener(this);

        checkBox = (ImageView) findViewById(R.id.iv_switch_screenview);
        checkBoxTwo = (ImageView) findViewById(R.id.iv_switch_noti);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rv_item_screenview:
                boolean isScreenViewOpen = mBtrManager.getScreenViewStatus();
                if (!isScreenViewOpen) {
                    checkBox.setImageResource(R.drawable.switch_on);
                    mBtrManager.setScreenViewStatus(true);
                } else {
                    checkBox.setImageResource(R.drawable.switch_off);
                    mBtrManager.setScreenViewStatus(false);
                }
                break;
            case R.id.rv_item_noti:
                boolean isBatteryNotiOpen = mBtrManager.getBatteryNotiStatus();
                if (!isBatteryNotiOpen) {
                    checkBoxTwo.setImageResource(R.drawable.switch_on);
                    mBtrManager.setBatteryNotiStatus(true);
                } else {
                    checkBoxTwo.setImageResource(R.drawable.switch_off);
                    mBtrManager.setBatteryNotiStatus(false);
                }
                break;
        }
    }


}
