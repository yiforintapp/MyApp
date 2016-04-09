package com.leo.appmaster.airsig;


import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.airsig.airsigengmulti.ASEngine;
import com.leo.appmaster.R;
import com.leo.appmaster.airsig.airsigsdk.ASGui;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.LeoLog;


public class AirSigSettingActivity extends BaseActivity implements View.OnClickListener {
    public final static String UNLOCK_TYPE = "unlock_type";
    public final static int NOMAL_UNLOCK = 1;
    public final static int AIRSIG_UNLOCK = 2;

    private final static int SET_DONE = 1;

    private CommonToolbar mTitleBar;
    private RippleView rpBtn;
    private RippleView rpBtnTwo;
    private ImageView mIvShowOne;
    private ImageView mIvShowTwo;
    private LEOAlarmDialog mConfirmCloseDialog;
    private long inTime;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SET_DONE:
                    LeoSettings.setBoolean(AirSigActivity.AIRSIG_SWITCH, true);
                    mIvShowOne.setVisibility(View.VISIBLE);
                    mIvShowTwo.setVisibility(View.GONE);
                    LeoSettings.setInteger(UNLOCK_TYPE, AIRSIG_UNLOCK);
                    showMessage(getString(R.string.airsig_settings_activity_toast));
                    SDKWrapper.addEvent(AirSigSettingActivity.this, SDKWrapper.P1, "settings", "airsig_def");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airsig_activity_select_setting);
        initUI();
        inTime = System.currentTimeMillis();
    }

    private void initUI() {
        mTitleBar = (CommonToolbar) findViewById(R.id.ctb_main);
        mTitleBar.setToolbarTitle(R.string.airsig_settings_activity_two_set_title);
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

        mIvShowOne = (ImageView) findViewById(R.id.iv_bg_one);
        mIvShowTwo = (ImageView) findViewById(R.id.iv_bg_two);
    }


    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }


    private void fillData() {
        boolean isAirSigVaild = ASGui.getSharedInstance().isValidLicense();
        int unlockType = LeoSettings.getInteger(UNLOCK_TYPE, NOMAL_UNLOCK);
        if (unlockType == AIRSIG_UNLOCK) {
            boolean isAirsigOn = LeoSettings.getBoolean(AirSigActivity.AIRSIG_SWITCH, false);
            if (isAirsigOn) {
                if (isAirSigVaild) {
                    mIvShowOne.setVisibility(View.VISIBLE);
                    mIvShowTwo.setVisibility(View.GONE);
                } else {
                    mIvShowOne.setVisibility(View.GONE);
                    mIvShowTwo.setVisibility(View.VISIBLE);
                }
            } else {
                mIvShowOne.setVisibility(View.GONE);
                mIvShowTwo.setVisibility(View.VISIBLE);
            }
        } else {
            mIvShowOne.setVisibility(View.GONE);
            mIvShowTwo.setVisibility(View.VISIBLE);
        }
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
                if (mIvShowOne.getVisibility() == View.VISIBLE) return;
                openAirSig();
                break;
            case R.id.rv_item_reset_airsig:
                if (mIvShowTwo.getVisibility() == View.VISIBLE) return;
                mIvShowOne.setVisibility(View.GONE);
                mIvShowTwo.setVisibility(View.VISIBLE);
//                LeoPreference.getInstance().putInt(UNLOCK_TYPE, NOMAL_UNLOCK);
                LeoSettings.setInteger(UNLOCK_TYPE, NOMAL_UNLOCK);
                SDKWrapper.addEvent(this, SDKWrapper.P1, "settings", "password_def");
                break;
        }
    }

    private void openAirSig() {
        boolean isAirsigOn = LeoSettings.getBoolean(AirSigActivity.AIRSIG_SWITCH, false);
        boolean isAirsigReady = ASGui.getSharedInstance().isSignatureReady(1);

        boolean isAirSigVaild = ASGui.getSharedInstance().isValidLicense();

        if (!isAirSigVaild) {
            showUpdateDialog();
            return;
        }

        if (isAirsigOn) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "settings", "airsig_def");
            //select
            mIvShowOne.setVisibility(View.VISIBLE);
            mIvShowTwo.setVisibility(View.GONE);
            LeoSettings.setInteger(UNLOCK_TYPE, AIRSIG_UNLOCK);
        } else if (isAirsigReady) {
            //dialog to ask open or not
            showAigSigDialog(true);
        } else {
            showAigSigDialog(false);
        }

    }

    private void showAigSigDialog(final boolean isAirsigReady) {
        if (mConfirmCloseDialog == null) {
            mConfirmCloseDialog = new LEOAlarmDialog(this);
        }
        mConfirmCloseDialog.setContent(getString(R.string.airsig_settings_activity_two_set_dialog));
        mConfirmCloseDialog.setRightBtnStr(getString(R.string.open_weizhuang_dialog_sure));
        mConfirmCloseDialog.setLeftBtnStr(getString(R.string.close_batteryview_confirm_cancel));
        mConfirmCloseDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (isAirsigReady) {
                    SDKWrapper.addEvent(AirSigSettingActivity.this, SDKWrapper.P1, "settings", "airsig_def");
                    LeoSettings.setBoolean(AirSigActivity.AIRSIG_SWITCH, true);
                    mIvShowOne.setVisibility(View.VISIBLE);
                    mIvShowTwo.setVisibility(View.GONE);
                    LeoSettings.setInteger(UNLOCK_TYPE, AIRSIG_UNLOCK);
                } else {
                    setAirsig();
                }

                mConfirmCloseDialog.dismiss();
            }
        });
        if (!isFinishing()) {
            mConfirmCloseDialog.show();
        }
    }

    private void showUpdateDialog() {
        if (mConfirmCloseDialog == null) {
            mConfirmCloseDialog = new LEOAlarmDialog(this);
        }
        mConfirmCloseDialog.setContent(getString(R.string.airsig_tip_toast_update_text));
        mConfirmCloseDialog.setRightBtnStr(getString(R.string.makesure));
        mConfirmCloseDialog.setLeftBtnStr(getString(R.string.close_batteryview_confirm_cancel));
        mConfirmCloseDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SDKWrapper.checkUpdate();
                mConfirmCloseDialog.dismiss();
            }
        });
        if (!isFinishing()) {
            mConfirmCloseDialog.show();
        }
    }

    private void setAirsig() {
        ASGui.getSharedInstance().showTrainingActivity(1, new ASGui.OnTrainingResultListener() {
            @Override
            public void onResult(boolean isRetrain, boolean success, ASEngine.ASAction action) {
                if (success) {
                    long now = System.currentTimeMillis();
                    if ((now - inTime > 3000)) {
                        inTime = System.currentTimeMillis();
                        mHandler.sendEmptyMessage(SET_DONE);
                    }
                }
            }
        });
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
