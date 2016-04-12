package com.leo.appmaster.battery;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.mgr.BatteryManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.Utilities;

public class BatterySettingActivity extends BaseActivity implements View.OnClickListener {
    private CommonToolbar mTitleBar;
    private ImageView checkBox;
    private RippleView rpBtn;
    private ImageView checkBoxThree;
    private RippleView rpBtnThree;
    private String mFromWhere;
    private BatteryManager.BatteryState newState;
    private LEOAlarmDialog mConfirmCloseDialog;
    private int mRemainTime;
    private int[] mRemainTimeArr;
    //    private boolean mNotiStatusWhenSwitch = false;
    private BatteryManager mBtrManager;
    private TextView mChargeCleanTitle; // 自动省电优化开关title
    private TextView mChargeCleanSummary; // 自动省电优化开关内容

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_setting);
        mBtrManager = (BatteryManager) MgrContext.getManager(MgrContext.MGR_BATTERY);
        handleIntent();
        initUI();
        fillData();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mFromWhere = intent.getStringExtra(Constants.BATTERY_FROM);
        if (!Utilities.isEmpty(mFromWhere) && mFromWhere.equals(Constants.FROM_BATTERY_PROTECT)) {
            mRemainTime = intent.getIntExtra(BatteryManager.REMAIN_TIME, 0);
            newState = (BatteryManager.BatteryState)
                    intent.getExtras().get(BatteryManager.SEND_BUNDLE);
            mRemainTimeArr = intent.getIntArrayExtra(BatteryManager.ARR_REMAIN_TIME);
        }
    }

    private void fillData() {
        boolean isScreenViewOpen = mBtrManager.getScreenViewStatus();
        if (isScreenViewOpen) {
            checkBox.setImageResource(R.drawable.switch_on);

            rpBtnThree.setFocusable(true);
            rpBtnThree.setEnabled(true);
            mChargeCleanTitle.setTextColor(getResources().getColor(R.color.c2));
            mChargeCleanSummary.setTextColor(getResources().getColor(R.color.c4));
            boolean isBatteryPowSavOpen = mBtrManager.getBatteryPowSavStatus();
            if (isBatteryPowSavOpen) {
                checkBoxThree.setImageResource(R.drawable.switch_on);
            } else {
                checkBoxThree.setImageResource(R.drawable.switch_off);
            }
        } else {
            checkBox.setImageResource(R.drawable.switch_off);

            rpBtnThree.setFocusable(false);
            rpBtnThree.setEnabled(false);
            checkBoxThree.setImageResource(R.drawable.switch_untouch);
            mChargeCleanTitle.setTextColor(getResources().getColor(R.color.cgy));
            mChargeCleanSummary.setTextColor(getResources().getColor(R.color.cgy));
        }

        boolean isBatteryNotiOpen = mBtrManager.getBatteryNotiStatus();
//        if (isBatteryNotiOpen) {
//            checkBoxTwo.setImageResource(R.drawable.switch_on);
//        } else {
//            checkBoxTwo.setImageResource(R.drawable.switch_off);
//        }

    }

    private void initUI() {
        mTitleBar = (CommonToolbar) findViewById(R.id.ctb_main);
        mTitleBar.setToolbarTitle(R.string.batterymanage_switch_screen);
        mTitleBar.setToolbarColorResource(R.color.ctc);
        mTitleBar.setOptionMenuVisible(false);
        mTitleBar.setNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        rpBtn = (RippleView) findViewById(R.id.rv_item_screenview);
        rpBtn.setOnClickListener(this);
//        rpBtnTwo.setOnClickListener(this);

        rpBtnThree = (RippleView) findViewById(R.id.rv_item_pow_sav);
        mChargeCleanTitle = (TextView) findViewById(R.id.tv_title_pow_sav);
        mChargeCleanSummary = (TextView) findViewById(R.id.tv_summary_pow_sav);
        rpBtnThree.setOnClickListener(this);

        checkBox = (ImageView) findViewById(R.id.iv_switch_screenview);
        checkBoxThree = (ImageView) findViewById(R.id.iv_switch_pow_sav);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        boolean isScreenViewOpen = mBtrManager.getScreenViewStatus();
        boolean isCharing = mBtrManager.getIsCharing();

        if (!Utilities.isEmpty(mFromWhere) && mFromWhere.equals(Constants.FROM_BATTERY_PROTECT)
                && isScreenViewOpen && isCharing) {
            Intent intent = new Intent(this, BatteryShowViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(BatteryManager.PROTECT_VIEW_TYPE, BatteryManager.SHOW_TYPE_IN);
            intent.putExtra(BatteryManager.REMAIN_TIME, mRemainTime);
            intent.putExtra(BatteryManager.ARR_REMAIN_TIME, mRemainTimeArr);
            Bundle bundle = new Bundle();
            bundle.putSerializable(BatteryManager.SEND_BUNDLE, newState);
            intent.putExtras(bundle);
            startActivity(intent);

            TaskDetectService tds = TaskDetectService.getService();
            if (tds != null) {
                tds.ignoreBatteryPage(true);
            }
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mConfirmCloseDialog != null && mConfirmCloseDialog.isShowing()) {
            mConfirmCloseDialog.dismiss();
            mConfirmCloseDialog = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rv_item_screenview:
                boolean isScreenViewOpen = mBtrManager.getScreenViewStatus();
                if (!isScreenViewOpen) {
                    checkBox.setImageResource(R.drawable.switch_on);
                    mBtrManager.setScreenViewStatus(true);
                    SDKWrapper.addEvent(BatterySettingActivity.this, SDKWrapper.P1,
                            "batterypage", "setting_scr_on");

                    boolean isBatteryPowSavOpen = mBtrManager.getBatteryPowSavStatus();
                    if (isBatteryPowSavOpen) {
                        checkBoxThree.setImageResource(R.drawable.switch_on);
                    } else {
                        checkBoxThree.setImageResource(R.drawable.switch_off);
                    }
                    rpBtnThree.setFocusable(true);
                    rpBtnThree.setEnabled(true);
                    mChargeCleanTitle.setTextColor(getResources().getColor(R.color.c2));
                    mChargeCleanSummary.setTextColor(getResources().getColor(R.color.c4));

                } else {
                    if (mConfirmCloseDialog == null) {
                        mConfirmCloseDialog = new LEOAlarmDialog(this);
                    }
                    mConfirmCloseDialog.setContent(getString(R.string.close_batteryview_confirm_content));
                    mConfirmCloseDialog.setRightBtnStr(getString(R.string.close_batteryview_confirm_sure));
                    mConfirmCloseDialog.setLeftBtnStr(getString(R.string.close_batteryview_confirm_cancel));
                    mConfirmCloseDialog.setRightBtnListener(new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkBox.setImageResource(R.drawable.switch_off);
                            mBtrManager.setScreenViewStatus(false);
                            SDKWrapper.addEvent(BatterySettingActivity.this, SDKWrapper.P1,
                                    "batterypage", "setting_scr_off");

                            checkBoxThree.setImageResource(R.drawable.switch_untouch);
                            rpBtnThree.setFocusable(false);
                            rpBtnThree.setEnabled(false);
                            mChargeCleanTitle.setTextColor(getResources().getColor(R.color.cgy));
                            mChargeCleanSummary.setTextColor(getResources().getColor(R.color.cgy));

                            mConfirmCloseDialog.dismiss();
                        }
                    });
                    if (!isFinishing()) {
                        mConfirmCloseDialog.show();
                    }
                }
                break;
//            case R.id.rv_item_noti:
//                boolean isBatteryNotiOpen = mBtrManager.getBatteryNotiStatus();
//                if (!isBatteryNotiOpen) {
//                    checkBoxTwo.setImageResource(R.drawable.switch_on);
//                    mBtrManager.setBatteryNotiStatus(true);
//                    SDKWrapper.addEvent(BatterySettingActivity.this, SDKWrapper.P1,
//                            "batterypage", "setting_ntf_on");
//                } else {
//                    checkBoxTwo.setImageResource(R.drawable.switch_off);
//                    mBtrManager.setBatteryNotiStatus(false);
//                    SDKWrapper.addEvent(BatterySettingActivity.this, SDKWrapper.P1,
//                            "batterypage", "setting_ntf_off");
//                }
//                break;
            case R.id.rv_item_pow_sav:
                boolean isBatteryPowSavOpen = mBtrManager.getBatteryPowSavStatus();
                if (!isBatteryPowSavOpen) {
                    checkBoxThree.setImageResource(R.drawable.switch_on);
                    mBtrManager.setBatteryPowSavStatus(true);
                    SDKWrapper.addEvent(this, SDKWrapper.P1,
                            "batterypage",
                            "setting_sav_on");
                } else {
                    checkBoxThree.setImageResource(R.drawable.switch_off);
                    mBtrManager.setBatteryPowSavStatus(false);
                    SDKWrapper.addEvent(this, SDKWrapper.P1,
                            "batterypage",
                            "setting_sav_off");
                }
                break;
        }
    }


}
