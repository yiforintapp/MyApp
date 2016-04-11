package com.leo.appmaster.airsig;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airsig.airsigengmulti.ASEngine;
import com.leo.appmaster.R;
import com.leo.appmaster.airsig.airsigsdk.ASGui;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.home.HomeMoreActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.LeoLog;


public class AirSigActivity extends BaseActivity implements View.OnClickListener {
    public final static String UNLOCK_TYPE = "unlock_type";
    public final static int NOMAL_UNLOCK = 1;
    public final static int AIRSIG_UNLOCK = 2;

    public final static int UPDATE_DIALOG = 1;
    public final static int CLOSE_DIALOG = 2;
    public final static int CAN_NOT_USE_DIALOG = 3;

    public final static String AIRSIG_SWITCH = "airsig_switch";
    public final static String AIRSIG_OPEN_EVER = "airsig_open_ever";
    public final static String AIRSIG_TIMEOUT_EVER = "airsig_timeout_ever";
    private final static int SET_DONE = 1;
    private CommonToolbar mTitleBar;
    private RippleView rpBtn;
    private RippleView rpBtnTwo;

    private TextView mTvSetOne;
    private TextView mTvSetTwo;
    private RippleView mRippView;

    private LEOAlarmDialog mConfirmCloseDialog;
    private long inTime;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SET_DONE:
                    LeoSettings.setBoolean(AIRSIG_SWITCH, true);
                    LeoSettings.setInteger(UNLOCK_TYPE, AIRSIG_UNLOCK);
                    switchOn();
                    showMessage(getString(R.string.airsig_settings_activity_toast));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airsig_activity_select);

        initUI();
        inTime = System.currentTimeMillis();
    }


    private void initUI() {
        mTitleBar = (CommonToolbar) findViewById(R.id.ctb_main);
        mTitleBar.setToolbarTitle(R.string.airsig_settings_activity_title);
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
        mRippView = (RippleView) findViewById(R.id.success_airsig);
        mRippView.setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        boolean isAirsigOn = LeoSettings.getBoolean(AIRSIG_SWITCH, false);
        boolean isAirSigVaild = ASGui.getSharedInstance().isValidLicense();
        if (isAirsigOn) {
            if (isAirSigVaild) {
                switchOn();
            } else {
                switchOff();
            }
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
                SDKWrapper.addEvent(this, SDKWrapper.P1, "settings", "airsig_reset");
                setAirsig(false);
                break;
            case R.id.success_airsig:
                Intent intent = new Intent(AirSigActivity.this,
                        FeedbackActivity.class);
                intent.putExtra("from", "airsig");
                startActivity(intent);
                break;
        }
    }

    private void setAirsig(final boolean isNormalSet) {

        boolean isAirSigVaild = ASGui.getSharedInstance().isValidLicense();

        if (!isAirSigVaild) {
            return;
        }

        ASGui.getSharedInstance().showTrainingActivity(1, new ASGui.OnTrainingResultListener() {
            @Override
            public void onResult(boolean isRetrain, boolean success, ASEngine.ASAction action) {


                if (success) {
                    long now = System.currentTimeMillis();
                    LeoLog.d("testTime", "now : " + now);
                    LeoLog.d("testTime", "inTime : " + inTime);
                    if ((now - inTime > 3000)) {
                        inTime = System.currentTimeMillis();
                        if (isNormalSet) {
                            SDKWrapper.addEvent(AirSigActivity.this, SDKWrapper.P1, "settings", "airsig_enable_suc");
                        } else {
                            SDKWrapper.addEvent(AirSigActivity.this, SDKWrapper.P1, "settings", "airsig_reset_suc");
                        }
                        mHandler.sendEmptyMessage(SET_DONE);
                    }
                }
            }
        });
    }

    private void switchAirsig() {
        boolean isAirsigOn = LeoSettings.getBoolean(AIRSIG_SWITCH, false);
        boolean isAirsigReady = ASGui.getSharedInstance().isSignatureReady(1);
        boolean isAirSigVaild = ASGui.getSharedInstance().isValidLicense();

        if (!isAirSigVaild) {
            showDialog(getString(R.string.airsig_tip_toast_update_text), getString(R.string.close_batteryview_confirm_cancel)
                    , getString(R.string.makesure), UPDATE_DIALOG);
            return;
        }

        if (isAirsigOn) {
            SDKWrapper.addEvent(AirSigActivity.this, SDKWrapper.P1, "settings", "airsig_off");
            //dialog to close
            showDialog(getString(R.string.airsig_settings_activity_dialog), getString(R.string.close_batteryview_confirm_cancel)
                    , getString(R.string.close_batteryview_confirm_sure), CLOSE_DIALOG);
        }
//        else if (isAirsigReady) {
//            SDKWrapper.addEvent(this, SDKWrapper.P1, "settings", "airsig_enable");
//            //open
//            LeoSettings.setInteger(UNLOCK_TYPE, AIRSIG_UNLOCK);
//            LeoSettings.setBoolean(AIRSIG_SWITCH, true);
//            switchOn();
//            SDKWrapper.addEvent(AirSigActivity.this, SDKWrapper.P1, "settings", "airsig_enable_suc");
//        }
        else {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "settings", "airsig_enable");

            boolean isAigSigCanUse = ASGui.getSharedInstance().isSensorAvailable();
            if (isAigSigCanUse) {
                //set Airsig
                setAirsig(true);
            } else {
                showDialog(getString(R.string.air_sig_tips_content), getString(R.string.airsig_training_err_default_button)
                        , getString(R.string.secur_help_feedback_tip_button), CAN_NOT_USE_DIALOG);
            }


        }
    }

    private void showDialog(String content, String leftBtn, String rightBtn, final int type) {
        if (mConfirmCloseDialog == null) {
            mConfirmCloseDialog = new LEOAlarmDialog(this);
        }
        mConfirmCloseDialog.setContent(content);
        mConfirmCloseDialog.setRightBtnStr(rightBtn);
        mConfirmCloseDialog.setLeftBtnStr(leftBtn);
        mConfirmCloseDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (type == UPDATE_DIALOG) {
                    SDKWrapper.checkUpdate();
                } else if (type == CLOSE_DIALOG) {
                    SDKWrapper.addEvent(AirSigActivity.this, SDKWrapper.P1, "settings", "airsig_off_suc");
                    LeoSettings.setInteger(UNLOCK_TYPE, NOMAL_UNLOCK);
                    LeoSettings.setBoolean(AIRSIG_SWITCH, false);
                    switchOff();
                } else {
                    Intent intent = new Intent(AirSigActivity.this,
                            FeedbackActivity.class);
                    intent.putExtra("from", "airsig");
                    startActivity(intent);
                }
                mConfirmCloseDialog.dismiss();
            }
        });
        if (!isFinishing()) {
            mConfirmCloseDialog.show();
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
