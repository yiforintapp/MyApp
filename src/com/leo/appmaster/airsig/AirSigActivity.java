package com.leo.appmaster.airsig;


import com.airsig.airsigengmulti.ASEngine;
import com.leo.appmaster.R;
import com.leo.appmaster.airsig.airsigsdk.ASGui;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.LeoLog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class AirSigActivity extends BaseActivity implements View.OnClickListener {
    public final static String AIRSIG_SWITCH = "airsig_switch";
    private final static int SET_DONE = 1;
    private final static int SET_FAILED = 2;
    private CommonToolbar mTitleBar;
    private RippleView rpBtn;
    private RippleView rpBtnTwo;

    private TextView mTvSetOne;
    private TextView mTvSetTwo;

    private LEOAlarmDialog mConfirmCloseDialog;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SET_DONE:
//                    LeoPreference.getInstance().putBoolean(AIRSIG_SWITCH, true);
                    LeoSettings.setBoolean(AIRSIG_SWITCH, true);
                    LeoSettings.setInteger(AirSigSettingActivity.UNLOCK_TYPE, AirSigSettingActivity.AIRSIG_UNLOCK);
                    switchOn();
                    showMessage(getString(R.string.airsig_settings_activity_toast));
                    break;
                case SET_FAILED:
                    showMessage("Not Completed");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airsig_activity_select);


        if (ASGui.getSharedInstance().isValidLicense()) {
            LeoLog.d("testAirSig", "no");
        } else {
            LeoLog.d("testAirSig", "yes");
        }

        initUI();
        fillData();
    }


    private void initUI() {
        mTitleBar = (CommonToolbar) findViewById(R.id.ctb_main);
        mTitleBar.setToolbarTitle(R.string.airsig_settings_activity_title);
        mTitleBar.setToolbarColorResource(R.color.cb);
        mTitleBar.setOptionMenuVisible(false);
        mTitleBar.setNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        rpBtn = (RippleView) findViewById(R.id.rv_item_open_airsig);
        rpBtn.setOnClickListener(this);
        rpBtnTwo = (RippleView) findViewById(R.id.rv_item_reset_airsig);
        rpBtnTwo.setOnClickListener(this);
        mTvSetOne = (TextView) findViewById(R.id.tv_title_airsig);
        mTvSetTwo = (TextView) findViewById(R.id.tv_title_reset_airsig);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void fillData() {
//        boolean isAirsigOn = LeoPreference.getInstance().getBoolean(AIRSIG_SWITCH, false);
        boolean isAirsigOn = LeoSettings.getBoolean(AIRSIG_SWITCH, false);
        if (isAirsigOn) {
            switchOn();
        } else {
            switchOff();
        }
    }

    private void switchOff() {
        mTvSetOne.setText(getString(R.string.airsig_settings_activity_set_one_on));
        mTvSetTwo.setTextColor(getResources().getColor(R.color.cgy));
        rpBtnTwo.setFocusable(false);
        rpBtnTwo.setEnabled(false);
    }

    private void switchOn() {
        mTvSetOne.setText(getString(R.string.airsig_settings_activity_set_one_off));
        mTvSetTwo.setTextColor(getResources().getColor(R.color.c2));
        rpBtnTwo.setFocusable(true);
        rpBtnTwo.setEnabled(true);
    }

    private void showMessage(final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rv_item_open_airsig:
                switchAirsig();
                break;
            case R.id.rv_item_reset_airsig:
                setAirsig();
                break;
        }
    }

    private void setAirsig() {
        ASGui.getSharedInstance().showTrainingActivity(1, new ASGui.OnTrainingResultListener() {
            @Override
            public void onResult(boolean isRetrain, boolean success, ASEngine.ASAction action) {
                if (success) {
                    mHandler.sendEmptyMessage(SET_DONE);
                } else {
                    mHandler.sendEmptyMessage(SET_FAILED);
                }
            }
        });
    }

    private void switchAirsig() {
//        boolean isAirsigOn = LeoPreference.getInstance().getBoolean(AIRSIG_SWITCH, false);
        boolean isAirsigOn = LeoSettings.getBoolean(AIRSIG_SWITCH, false);
        boolean isAirsigReady = ASGui.getSharedInstance().isSignatureReady(1);
        if (isAirsigOn) {
            //dialog to close
            if (mConfirmCloseDialog == null) {
                mConfirmCloseDialog = new LEOAlarmDialog(this);
            }
            mConfirmCloseDialog.setContent(getString(R.string.airsig_settings_activity_dialog));
            mConfirmCloseDialog.setRightBtnStr(getString(R.string.close_batteryview_confirm_sure));
            mConfirmCloseDialog.setLeftBtnStr(getString(R.string.close_batteryview_confirm_cancel));
            mConfirmCloseDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    LeoPreference.getInstance().putBoolean(AIRSIG_SWITCH, false);
                    LeoSettings.setBoolean(AIRSIG_SWITCH, false);
                    switchOff();
                    mConfirmCloseDialog.dismiss();
                }
            });
            if (!isFinishing()) {
                mConfirmCloseDialog.show();
            }
        } else if (isAirsigReady) {
            //open
//            LeoPreference.getInstance().putBoolean(AIRSIG_SWITCH, true);
            LeoSettings.setInteger(AirSigSettingActivity.UNLOCK_TYPE, AirSigSettingActivity.AIRSIG_UNLOCK);
            LeoSettings.setBoolean(AIRSIG_SWITCH, true);
            switchOn();
        } else {
            //set Airsig
            setAirsig();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConfirmCloseDialog != null && mConfirmCloseDialog.isShowing()) {
            mConfirmCloseDialog.dismiss();
            mConfirmCloseDialog = null;
        }
    }
}
