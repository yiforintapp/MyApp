package com.leo.appmaster.intruderprotection;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.receiver.DeviceReceiver;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAnimationDialog;
import com.leo.appmaster.ui.dialog.LEOChoiceDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenfs on 16-3-14.
 */
public class IntruderSettingActivity extends BaseActivity implements View.OnClickListener {
    private TextView mTvFailTimesToCatch;
    private ImageView mIvSwitchSyst;
    private ImageView mIvSwitchNormal;
    private IntrudeSecurityManager mISManager;
    private LEOChoiceDialog mDialog;
    private LEOAlarmDialog mAskOpenDeviceAdminDialog;
    private LEOAlarmDialog mOpenForbinDialog;
    private LEOAnimationDialog mMessageDialog;
    private LEOAlarmDialog mConfirmCloseDialog;
    private CommonToolbar mCtbMain;
    private int[] mTimes = {
            1, 2, 3, 5
    };
    private final int REQUEST_CODE_TO_REQUEST_ADMIN = 1;
    private final int TIMES_1 = 1;
    private final int TIMES_2 = 2;
    private final int TIMES_3 = 3;
    private final int TIMES_4 = 5;
    private RelativeLayout mRvItem1;
    private RelativeLayout mRvItem2;
    private RelativeLayout mRvItem3;
    private boolean mIsFromScan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruder_setting);
        mISManager = (IntrudeSecurityManager) MgrContext.getManager(MgrContext.MGR_INTRUDE_SECURITY);
        initUI();
        handleIntent();
    }

    private void handleIntent() {
        try {
            Intent i = getIntent();
            boolean isFromScan = i.getBooleanExtra(Constants.EXTRA_IS_FROM_SCAN, false);
            if (isFromScan) {
                mIsFromScan = true;
            }
        } catch (Exception e) {

        }
    }

    private void initUI() {
        mCtbMain = (CommonToolbar) findViewById(R.id.ctb_main);
        mCtbMain.setToolbarTitle(R.string.home_tab_instruder);
        mTvFailTimesToCatch = (TextView) findViewById(R.id.tv_intruder_setting_summary3);
        mIvSwitchSyst = (ImageView) findViewById(R.id.iv_intruder_setting_switch1);
        mIvSwitchNormal = (ImageView) findViewById(R.id.iv_intruder_setting_switch2);
        mRvItem1 = (RippleView) findViewById(R.id.rv_intruder_setting_content1);
        mRvItem2 = (RippleView) findViewById(R.id.rv_intruder_setting_content2);
        mRvItem3 = (RippleView) findViewById(R.id.rv_intruder_setting_content3);
        mRvItem1.setOnClickListener(this);
        mRvItem2.setOnClickListener(this);
        mRvItem3.setOnClickListener(this);
    }

    private void showConfirmCloseDialog() {
        if (mConfirmCloseDialog == null) {
            mConfirmCloseDialog = new LEOAlarmDialog(IntruderSettingActivity.this);
        }
        if (mConfirmCloseDialog.isShowing()) {
            return;
        }
        mConfirmCloseDialog.setTitle(R.string.intruder_setting_title_1);
        mConfirmCloseDialog.setContent(getString(R.string.intruder_systemlock_alarm));
        mConfirmCloseDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mISManager.setSystIntruderProtectionSwitch(false);
                mIvSwitchSyst.setImageResource(R.drawable.switch_off);
                mConfirmCloseDialog.dismiss();
            }
        });
        mConfirmCloseDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rv_intruder_setting_content1:
                turnSwitch1();
                break;
            case R.id.rv_intruder_setting_content2:
                turnSwitch2();
                break;
            case R.id.rv_intruder_setting_content3:
                showChangeTimesDialog();
                break;
            default:
                break;
        }
    }

    private void turnSwitch2() {
        if (!mISManager.getIsIntruderSecurityAvailable()) {
            showForbitDialog();
            return;
        }
        if (mISManager.getIntruderMode()) {
            mISManager.switchIntruderMode(false);
            mIvSwitchNormal.setImageResource(R.drawable.switch_off);
            Toast.makeText(IntruderSettingActivity.this,
                            getString(R.string.intruder_close), Toast.LENGTH_SHORT).show();
        } else {
            mISManager.switchIntruderMode(true);
            mIvSwitchNormal.setImageResource(R.drawable.switch_on);
            if (mIsFromScan) {
                ShowToast.showGetScoreToast(IntrudeSecurityManager.VALUE_SCORE,
                        IntruderSettingActivity.this);
                mIsFromScan = false;
            } else {
                Toast.makeText(IntruderSettingActivity.this, getString(R.string.intruder_open), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void turnSwitch1() {
        if (mISManager.getSystIntruderProtecionSwitch()) {
            showConfirmCloseDialog();
        } else {
            if (DeviceReceiver.isActive(IntruderSettingActivity.this)) {
                mISManager.setSystIntruderProtectionSwitch(true);
                mIvSwitchSyst.setImageResource(R.drawable.switch_on);
            } else {
                showAskOpenDeviceAdminDialog();
            }
        }
    }

    private void showAskOpenDeviceAdminDialog() {
        if (mAskOpenDeviceAdminDialog == null) {
            mAskOpenDeviceAdminDialog = new LEOAlarmDialog(IntruderSettingActivity.this);
        }
        if (mAskOpenDeviceAdminDialog.isShowing()) {
            return;
        }
        mAskOpenDeviceAdminDialog.setTitle(R.string.intruder_setting_title_1);
        mAskOpenDeviceAdminDialog.setContent(getString(R.string.intruder_device_admin_guide_content));
        mAskOpenDeviceAdminDialog.setRightBtnListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestDeviceAdmin();
                mAskOpenDeviceAdminDialog.dismiss();
            }
        });
        mAskOpenDeviceAdminDialog.show();
    }

    private void requestDeviceAdmin() {
        mLockManager.filterSelfOneMinites();
        mLockManager.filterPackage(Constants.PKG_SETTINGS, 1000);
        ComponentName mAdminName = new ComponentName(IntruderSettingActivity.this, DeviceReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
        startActivityForResult(intent, REQUEST_CODE_TO_REQUEST_ADMIN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_TO_REQUEST_ADMIN == requestCode && DeviceReceiver.isActive(IntruderSettingActivity.this)) {
            mISManager.setSystIntruderProtectionSwitch(true);
            mIvSwitchSyst.setImageResource(R.drawable.switch_on);
            openAdvanceProtectDialogHandler();
        }
    }

    private void openAdvanceProtectDialogHandler() {
        boolean isTip = AppMasterPreference.getInstance(this)
                .getAdvanceProtectOpenSuccessDialogTip();
        if (isTip) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "gd_dcnts", "gd_dput_real");
            openAdvanceProtectDialogTip();
        }
    }

    private void openAdvanceProtectDialogTip() {
        if (mMessageDialog == null) {
            mMessageDialog = new LEOAnimationDialog(this);
            mMessageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (mMessageDialog != null) {
                        mMessageDialog = null;
                    }
                    AppMasterPreference.getInstance(IntruderSettingActivity.this)
                            .setAdvanceProtectOpenSuccessDialogTip(false);
                }
            });
        }
        String content = getString(R.string.prot_open_suc_tip_cnt);
        mMessageDialog.setContent(content);
        mMessageDialog.show();
    }




    protected void showForbitDialog() {
        if (mOpenForbinDialog == null) {
            mOpenForbinDialog = new LEOAlarmDialog(this);
        }
        mOpenForbinDialog.setContent(getResources().getString(
                R.string.intruderprotection_forbit_content));
        mOpenForbinDialog.setRightBtnStr(getResources().getString(
                R.string.secur_help_feedback_tip_button));
        mOpenForbinDialog.setLeftBtnStr(getResources().getString(
                R.string.no_image_hide_dialog_button));
        mOpenForbinDialog.setRightBtnListener(new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(IntruderSettingActivity.this, FeedbackActivity.class);
                intent.putExtra("isFromIntruderProtectionForbiden", true);
                startActivity(intent);
                mOpenForbinDialog.dismiss();
            }
        });
        mOpenForbinDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateSwitch();
        updateSwitchummary();
    }

    private void updateSwitchummary() {
        String times = getResources().getString(R.string.times_choose);
        mTvFailTimesToCatch.setText(String.format(times, mISManager.getTimesForTakePhoto()));
    }

    private void updateSwitch() {
        if (mISManager.getIntruderMode()) {
            mIvSwitchNormal.setImageResource(R.drawable.switch_on);
        } else {
            mIvSwitchNormal.setImageResource(R.drawable.switch_off);
        }
        if (mISManager.getSystIntruderProtecionSwitch()) {
            mIvSwitchSyst.setImageResource(R.drawable.switch_on);
        } else {
            mIvSwitchSyst.setImageResource(R.drawable.switch_off);
        }
    }


    private void showChangeTimesDialog() {
        if (mDialog == null) {
            mDialog = new LEOChoiceDialog(IntruderSettingActivity.this);
        }
        mDialog.setTitle(getResources().getString(R.string.ask_for_times_for_catch));
        String times = getResources().getString(R.string.times_choose);
        List<String> timesArray = new ArrayList<String>();
        for (int i = 0; i < mTimes.length; i++) {
            timesArray.add(String.format(times, mTimes[i]));
        }

        int currentTimes = mISManager.getTimesForTakePhoto();
        int currentIndex = -1;
        switch (currentTimes) {
            case TIMES_1:
                currentIndex = 0;
                break;
            case TIMES_2:
                currentIndex = 1;
                break;
            case TIMES_3:
                currentIndex = 2;
                break;
            case TIMES_4:
                currentIndex = 3;
                break;
            default:
                break;
        }
        mDialog.setItemsWithDefaultStyle(timesArray, currentIndex);
        mDialog.getItemsListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mISManager.setTimesForTakePhoto(TIMES_1);
                        SDKWrapper.addEvent(IntruderSettingActivity.this, SDKWrapper.P1,
                                "intruder", "intruder_times_" + TIMES_1);
                        updateSwitchummary();
                        break;
                    case 1:
                        mISManager.setTimesForTakePhoto(TIMES_2);
                        SDKWrapper.addEvent(IntruderSettingActivity.this, SDKWrapper.P1,
                                "intruder", "intruder_times_" + TIMES_2);
                        updateSwitchummary();
                        break;
                    case 2:
                        mISManager.setTimesForTakePhoto(TIMES_3);
                        SDKWrapper.addEvent(IntruderSettingActivity.this, SDKWrapper.P1,
                                "intruder", "intruder_times_" + TIMES_3);
                        updateSwitchummary();
                        break;
                    case 3:
                        mISManager.setTimesForTakePhoto(TIMES_4);
                        SDKWrapper.addEvent(IntruderSettingActivity.this, SDKWrapper.P1,
                                "intruder", "intruder_times_" + TIMES_4);
                        updateSwitchummary();
                        break;
                    default:
                        break;
                }
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }
}
