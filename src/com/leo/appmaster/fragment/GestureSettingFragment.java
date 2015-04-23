
package com.leo.appmaster.fragment;

import java.util.List;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.animation.AnimationListenerAdapter;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.PasswdProtectActivity;
import com.leo.appmaster.applocker.gesture.LockPatternView;
import com.leo.appmaster.applocker.gesture.LockPatternView.Cell;
import com.leo.appmaster.applocker.gesture.LockPatternView.DisplayMode;
import com.leo.appmaster.applocker.gesture.LockPatternView.OnPatternListener;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOMessageDialog;
import com.leo.appmaster.utils.LockPatternUtils;

public class GestureSettingFragment extends BaseFragment implements
        OnClickListener, OnPatternListener, OnDismissListener,
        OnDiaogClickListener {

    private TextView mTvGestureTip, mTvPasswdFuncTip, mTvBottom, mSwitchBottom;
    private LockPatternView mLockPatternView;
    private int mInputCount = 1;
    private String mTempGesture1, mTempGesture2;
    private boolean mGotoPasswdProtect;
    private Animation mShake;
    private static final String TO_ACTIVITY_PACKAGE_NAME = "to_activity_package_name";

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_gesture_setting;
    }

    @Override
    protected void onInitUI() {
        mLockPatternView = (LockPatternView) findViewById(R.id.gesture_lockview);
        mLockPatternView.setOnPatternListener(this);
        mTvGestureTip = (TextView) findViewById(R.id.tv_gesture_tip);
        mTvPasswdFuncTip = (TextView) findViewById(R.id.tv_passwd_function_tip);
        mTvBottom = (TextView) findViewById(R.id.tv_bottom);
        mTvBottom.setOnClickListener(this);
        mSwitchBottom = (TextView) mActivity.findViewById(R.id.switch_bottom);

        if (AppMasterPreference.getInstance(mActivity).getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
            mTvGestureTip.setText(R.string.first_set_passwd_hint);
        } else {
            mTvGestureTip.setText(R.string.set_gesture);
        }
        mTvPasswdFuncTip.setText(R.string.gestur_passwd_function_hint);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_bottom:
                resetGesture();
                break;
            default:
                break;
        }
    }

    private void resetGesture() {
        cancelShake();
        mInputCount = 1;
        mTempGesture1 = mTempGesture2 = "";

        if (AppMasterPreference.getInstance(mActivity).getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
            mTvGestureTip.setText(R.string.first_set_passwd_hint);
        } else {
            mTvGestureTip.setText(R.string.set_gesture);
        }
        mTvPasswdFuncTip.setText(R.string.gestur_passwd_function_hint);

        mSwitchBottom.setVisibility(View.VISIBLE);
        mTvBottom.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPatternStart() {
        cancelShake();
        mTvPasswdFuncTip.setText(R.string.gesture_start_hint);
    }

    @Override
    public void onPatternCleared() {

    }

    @Override
    public void onPatternCellAdded(List<Cell> pattern) {

    }

    @Override
    public void onPatternDetected(final List<Cell> pattern) {
        mLockPatternView.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkGesture(pattern);
            }
        }, 300);

    }

    private void checkGesture(List<Cell> pattern) {
        int patternSize = pattern.size();
        if (mInputCount == 1) {
            if (patternSize < 4) {
                mTvPasswdFuncTip.setText(R.string.passwd_set_gesture_tip);
                shakeGestureTip();
                // mLockPatternView.clearPattern();
                return;
            }
            mTempGesture1 = LockPatternUtils.patternToString(pattern);

            mTvGestureTip.setText(R.string.make_passwd);
            mTvPasswdFuncTip.setText(R.string.input_again);
            mLockPatternView.clearPattern();
            mInputCount++;
            mSwitchBottom.setVisibility(View.INVISIBLE);
            mTvBottom.setVisibility(View.VISIBLE);
        } else {
            mTempGesture2 = LockPatternUtils.patternToString(pattern);
            if (mTempGesture2.equals(mTempGesture1)) {
                AppMasterPreference pref = AppMasterPreference.getInstance(mActivity);
                Intent intent = null;
                if (pref.getLockType() == AppMasterPreference.LOCK_TYPE_NONE) {
                    SDKWrapper.addEvent(GestureSettingFragment.this.mActivity, SDKWrapper.P1,
                            "first",
                            "usehand");
                }

                pref.saveGesture(
                        mTempGesture2);
                if (((LockSettingActivity) mActivity).isResetPasswd()) {
                    ((LockSettingActivity) mActivity).mJustFinish = true;
                    showResetSuc();
                    // notify lock theme change, this is a camouflage
                    // notify to change lock screen UI
                    Intent intent2 = new Intent(LockScreenActivity.THEME_CHANGE);
                    GestureSettingFragment.this.mActivity.sendBroadcast(intent2);
                    return;
                }

                LockManager.getInstatnce().timeFilter(mActivity.getPackageName(), 500);

                Toast.makeText(mActivity, R.string.set_gesture_suc, 1).show();
                if (!pref.hasPswdProtect()) {
                    showGestureProtectDialog();
                } else {
                    mActivity.finish();
                }
            } else {
                shakeGestureTip();
                mLockPatternView.clearPattern();
                mTvPasswdFuncTip.setText(R.string.tip_no_the_same_pswd);
            }
        }
    }

    private void showResetSuc() {
        LEOMessageDialog d = new LEOMessageDialog(mActivity);
        d.setTitle(mActivity.getString(R.string.reset_gesture_passwd));
        d.setContent(mActivity.getString(R.string.reset_passwd_successful));
        d.setOnDismissListener(this);
        d.setCanceledOnTouchOutside(false);
        d.show();
    }

    private void showGestureProtectDialog() {
        LEOAlarmDialog d = new LEOAlarmDialog(mActivity);
        d.setTitle(mActivity.getString(R.string.set_protect_or_not));
        d.setContent(mActivity.getString(R.string.set_protect_message));
        d.setLeftBtnStr(mActivity.getString(R.string.cancel));
        d.setRightBtnStr(mActivity.getString(R.string.makesure));
        d.setOnClickListener(this);
        d.setOnDismissListener(this);
        d.show();
    }

    private void cancelShake() {
        if (mShake != null && mShake.hasStarted()) {
            mShake.cancel();
            mShake.reset();
        }
    }

    private void shakeGestureTip() {
        if (mShake == null) {
            mShake = AnimationUtils.loadAnimation(mActivity,
                    R.anim.left_right_shake);
            mShake.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mLockPatternView.clearPattern();
                }

            });
        }
        mTvPasswdFuncTip.startAnimation(mShake);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Intent intent;
        if (mGotoPasswdProtect) {
            if (((LockSettingActivity) mActivity).mToLockList) {
                intent = new Intent(mActivity, PasswdProtectActivity.class);
                intent.putExtra("to_lock_list", true);
                mActivity.startActivity(intent);
            } else if (((LockSettingActivity) mActivity).mJustFinish) {
                // todo nothing
                if (((LockSettingActivity) mActivity).mFromQuickMode) {
                    intent = new Intent(mActivity, PasswdProtectActivity.class);
                    int modeId = ((LockSettingActivity) mActivity).mModeId;
                    intent.putExtra("quick_mode", true);
                    intent.putExtra("mode_id", modeId);
                    mActivity.startActivity(intent);
                }
            } else {
                intent = new Intent(mActivity, PasswdProtectActivity.class);
                intent.putExtra("to_home", true);
                mActivity.startActivity(intent);
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "first", "setpwdp");
            }
        } else {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "first", "setpwdp_cancel");
            if (((LockSettingActivity) mActivity).mToLockList) {
                // to lock list
                intent = new Intent(mActivity, AppLockListActivity.class);
                startActivity(intent);
            } else if (((LockSettingActivity) mActivity).mJustFinish) {
                // do nothing
                if (((LockSettingActivity) mActivity).mFromQuickMode) {
                    int modeId = ((LockSettingActivity) mActivity).mModeId;
                    LockManager lm = LockManager.getInstatnce();
                    List<LockMode> modeList = lm.getLockMode();
                    for (LockMode lockMode : modeList) {
                        if (lockMode.modeId == modeId) {
                            showModeActiveTip(lockMode);
                            break;
                        }
                    }
                }
            } else {
                LockManager.getInstatnce().timeFilter(mActivity.getPackageName(), 500);
                intent = new Intent(mActivity, HomeActivity.class);
                mActivity.startActivity(intent);
            }
        }
        mTvGestureTip.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (LockManager.getInstatnce().serviceBound()) {
                    LockManager.getInstatnce().startLockService();
                } else {
                    LockManager.getInstatnce().bindService();
                }
            }
        }, 2000);
        mActivity.finish();
    }

    /**
     * show the tip when mode success activating
     */
    private void showModeActiveTip(LockMode mode) {
        View mTipView = LayoutInflater.from(mActivity).inflate(R.layout.lock_mode_active_tip, null);
        TextView mActiveText = (TextView) mTipView.findViewById(R.id.active_text);
        mActiveText.setText(this.getString(R.string.mode_change, mode.modeName));
        Toast toast = new Toast(mActivity);
        toast.setView(mTipView);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onClick(int which) {
        if (which == 0) {
        } else if (which == 1) {
            mGotoPasswdProtect = true;
        }
    }
}
