package com.leo.appmaster.airsig;


import com.airsig.airsigengmulti.ASEngine;
import com.leo.appmaster.R;
import com.leo.appmaster.airsig.airsigsdk.ASGui;
import com.leo.appmaster.airsig.airsigsdk.ASSetting;
import com.leo.appmaster.battery.BatterySettingActivity;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class AirSigActivity extends BaseActivity implements View.OnClickListener {
    public final static String AIRSIG_SWITCH = "airsig_switch";

    private CommonToolbar mTitleBar;
    private RippleView rpBtn;
    private RippleView rpBtnTwo;

    private TextView mTvSetOne;
    private TextView mTvSetTwo;

    private LEOAlarmDialog mConfirmCloseDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airsig_activity_select);
        initAirSig();
        initUI();
    }


    private void initAirSig() {
        ASSetting setting = new ASSetting();
        setting.engineParameters = ASEngine.ASEngineParameters.Unlock;


        ASGui.getSharedInstance(getApplicationContext(), null, setting, null); // Database is in /data/data/...
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
        fillData();
    }

    private void fillData() {
        boolean isAirsigOn = PreferenceTable.getInstance().getBoolean(AIRSIG_SWITCH, false);
        if (isAirsigOn) {
            mTvSetOne.setText(getString(R.string.airsig_settings_activity_set_one_off));
            mTvSetTwo.setTextColor(getResources().getColor(R.color.c2));
            rpBtnTwo.setFocusable(true);
            rpBtnTwo.setEnabled(true);
        } else {
            mTvSetOne.setText(getString(R.string.airsig_settings_activity_set_one_on));
            mTvSetTwo.setTextColor(getResources().getColor(R.color.cgy));
            rpBtnTwo.setFocusable(false);
            rpBtnTwo.setEnabled(false);
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
                switchAirsig();
                break;
            case R.id.rv_item_reset_airsig:
                setAirsig();
                break;
//            case R.id.button_set_signature:
//                ASGui.getSharedInstance().showTrainingActivity(1, new ASGui.OnTrainingResultListener() {
//                    @Override
//                    public void onResult(boolean isRetrain, boolean success, ASEngine.ASAction action) {
//                        showMessage((isRetrain ? "Re-set Signature" + ", " : "")
//                                + (success ? "Completed" : "Not Completed"));
//                    }
//
//                });
//                break;
//            case R.id.button_verify_signature:
//
//                break;
//            case R.id.button_clean_db:
//                ASGui.getSharedInstance().deleteSignature(1);
//                ASEngine.getSharedInstance().getAction(1, new ASEngine.OnGetActionResultListener() {
//                    @Override
//                    public void onResult(final ASEngine.ASAction action, final ASEngine.ASError error) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (null != action && action.strength != ASEngine.ASStrength.ASStrengthNoData) {
//                                    showMessage("delete Signature fail");
//                                } else {
//                                    showMessage("delete Signature done");
//                                }
//                            }
//                        });
//                    }
//                });
//                break;
        }
    }

    private void setAirsig() {
        ASGui.getSharedInstance().showTrainingActivity(1, new ASGui.OnTrainingResultListener() {
            @Override
            public void onResult(boolean isRetrain, boolean success, ASEngine.ASAction action) {
                showMessage((isRetrain ? "Re-set Signature" + ", " : "")
                        + (success ? "Completed" : "Not Completed"));
            }
        });
    }

    private void switchAirsig() {
        boolean isAirsigOn = PreferenceTable.getInstance().getBoolean(AIRSIG_SWITCH, false);
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
                    PreferenceTable.getInstance().putBoolean(AIRSIG_SWITCH, false);
                    mTvSetOne.setText(getString(R.string.airsig_settings_activity_set_one_on));
                    mTvSetTwo.setTextColor(getResources().getColor(R.color.cgy));
                    rpBtnTwo.setFocusable(false);
                    rpBtnTwo.setEnabled(false);
                    mConfirmCloseDialog.dismiss();
                }
            });
            if (!isFinishing()) {
                mConfirmCloseDialog.show();
            }
        } else if (isAirsigReady) {
            //open
            PreferenceTable.getInstance().putBoolean(AIRSIG_SWITCH, true);
            mTvSetOne.setText(getString(R.string.airsig_settings_activity_set_one_off));
            mTvSetTwo.setTextColor(getResources().getColor(R.color.c2));
            rpBtnTwo.setFocusable(true);
            rpBtnTwo.setEnabled(true);
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
