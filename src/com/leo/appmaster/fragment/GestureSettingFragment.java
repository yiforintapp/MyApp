
package com.leo.appmaster.fragment;

import java.util.List;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.animation.AnimationListenerAdapter;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.PasswdProtectActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.WeiZhuangActivity;
import com.leo.appmaster.applocker.gesture.LockPatternView;
import com.leo.appmaster.applocker.gesture.LockPatternView.Cell;
import com.leo.appmaster.applocker.gesture.LockPatternView.DisplayMode;
import com.leo.appmaster.applocker.gesture.LockPatternView.OnPatternListener;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.appmanage.BackUpActivity;
import com.leo.appmaster.appmanage.EleActivity;
import com.leo.appmaster.appmanage.FlowActivity;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.quickgestures.ui.QuickGestureActivity;
import com.leo.appmaster.quickgestures.ui.QuickGestureMiuiTip;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOMessageDialog;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LockPatternUtils;
import com.leo.appmaster.videohide.VideoHideMainActivity;

public class GestureSettingFragment extends BaseFragment implements
        OnClickListener, OnPatternListener, OnDismissListener,
        OnDiaogClickListener {

    private TextView mTvGestureTip, mTvPasswdFuncTip, mTvBottom;
    private ImageView iv_reset_icon;
    private View reset_button_content, reset_buton_content_activity;
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
        iv_reset_icon = (ImageView) findViewById(R.id.iv_reset_icon);
        mLockPatternView = (LockPatternView) findViewById(R.id.gesture_lockview);
        reset_button_content = findViewById(R.id.reset_button_content);
        reset_buton_content_activity = mActivity.findViewById(R.id.switch_bottom_content);
        mLockPatternView.setOnPatternListener(this);
        mTvGestureTip = (TextView) findViewById(R.id.tv_gesture_tip);
        mTvPasswdFuncTip = (TextView) findViewById(R.id.tv_passwd_function_tip);
        mTvBottom = (TextView) findViewById(R.id.tv_bottom);
        reset_button_content.setOnClickListener(this);

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
            case R.id.reset_button_content:
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

        reset_buton_content_activity.setVisibility(View.VISIBLE);
        reset_button_content.setVisibility(View.INVISIBLE);
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
            reset_buton_content_activity.setVisibility(View.INVISIBLE);
            reset_button_content.setVisibility(View.VISIBLE);
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
        int type = ((LockSettingActivity) mActivity).getFromDeskId();
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
                /*
                 * if (((LockSettingActivity) mActivity).mFromQuickMode) { int
                 * modeId = ((LockSettingActivity) mActivity).mModeId;
                 * LockManager lm = LockManager.getInstatnce(); List<LockMode>
                 * modeList = lm.getLockMode(); for (LockMode lockMode :
                 * modeList) { if (lockMode.modeId == modeId) {
                 * showModeActiveTip(lockMode); break; } } }
                 */
            } else if (type != StatusBarEventService.EVENT_EMPTY) {
                // from desk
                if (type == ((LockSettingActivity) mActivity).mAppLockType) {
                    goToAppLock();
                } else if (type == ((LockSettingActivity) mActivity).mAppWeiZhuang) {
                    goToAppWeiZhuang();
                } else if (type == ((LockSettingActivity) mActivity).mPicHide) {
                    goToAppHidePic();
                } else if (type == ((LockSettingActivity) mActivity).mVioHide) {
                    goToAppHideVio();
                } else if (type == ((LockSettingActivity) mActivity).mPrivateSms) {
                    goToPrivateSms();
                } else if (type == ((LockSettingActivity) mActivity).mFlow) {
                    goToFlow();
                } else if (type == ((LockSettingActivity) mActivity).mElec) {
                    gotoEle(type);
                } else if (type == ((LockSettingActivity) mActivity).mBackup) {
                    gotoBackUp(type);
                } else if (type == ((LockSettingActivity) mActivity).mQuickGues) {
                    gotoQuickGues(type);
                }else if (type == ((LockSettingActivity) mActivity).mLockThem) {
                    gotoLockThem(type);
                }else {
                    gotoHome();
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
                LockManager.getInstatnce().startLockService();
            }
        }, 2000);
        mActivity.finish();
    }

    /**
     * show the tip when mode success activating
     */
    /*
     * private void showModeActiveTip(LockMode mode) { View mTipView =
     * LayoutInflater.from(mActivity).inflate(R.layout.lock_mode_active_tip,
     * null); TextView mActiveText = (TextView)
     * mTipView.findViewById(R.id.active_text);
     * mActiveText.setText(this.getString(R.string.mode_change, mode.modeName));
     * Toast toast = new Toast(mActivity); toast.setView(mTipView);
     * toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0,
     * 0); toast.setDuration(Toast.LENGTH_SHORT); toast.show(); }
     */

    @Override
    public void onClick(int which) {
        if (which == 0) {
        } else if (which == 1) {
            mGotoPasswdProtect = true;
        }
    }

    private void gotoHome() {
        Intent intent = new Intent(mActivity, HomeActivity.class);
        mActivity.startActivity(intent);
    }
    
    private void gotoLockThem(int type) {
        Intent intent = new Intent(mActivity, LockerTheme.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    
    private void gotoQuickGues(int type) {
        boolean checkHuaWei = BuildProperties.isHuaWeiTipPhone(mActivity);
        boolean checkFloatWindow = BuildProperties.isFloatWindowOpAllowed(mActivity);
        boolean checkMiui = BuildProperties.isMIUI();
        boolean isOppoOs = BuildProperties.isYiJia();
        boolean isOpenWindow =
                BuildProperties.isFloatWindowOpAllowed(mActivity);

        if (checkMiui && !isOpenWindow) {
            // MIUI
            Intent intentv6 = new
                    Intent("miui.intent.action.APP_PERM_EDITOR");
            intentv6.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            intentv6.putExtra("extra_pkgname", mActivity.getPackageName());
            intentv6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            try {
                LockManager.getInstatnce().addFilterLockPackage("com.miui.securitycenter",
                        false);
                LockManager.getInstatnce().filterAllOneTime(2000);
                startActivity(intentv6);
            } catch (Exception e) {
                LockManager.getInstatnce().addFilterLockPackage("com.android.settings",
                        false);
                LockManager.getInstatnce().filterAllOneTime(1000);
                Intent intentv5 = new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri
                        .fromParts("package", mActivity.getPackageName(), null);
                intentv5.setData(uri);
                intentv5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                try {
                    startActivity(intentv5);
                } catch (Exception e1) {
                    SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "qs_open_error", "reason_"
                            + BuildProperties.getPoneModel());
                }
            }
            LockManager.getInstatnce().addFilterLockPackage("com.leo.appmaster", false);
            LockManager.getInstatnce().filterAllOneTime(1000);
            Intent quickIntent = new Intent(mActivity, QuickGestureMiuiTip.class);
            quickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(quickIntent);
        } else if (checkHuaWei && !checkFloatWindow) {
            BuildProperties.isToHuaWeiSystemManager(mActivity);
            LockManager.getInstatnce().addFilterLockPackage("com.leo.appmaster", false);
            Intent quickIntent = new Intent(mActivity, QuickGestureMiuiTip.class);
            quickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            quickIntent.putExtra("sys_name", "huawei");
            try {
                startActivity(quickIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (isOppoOs && !isOpenWindow) {
            boolean backFlag = BuildProperties.startOppoManageIntent(mActivity);
            LockManager.getInstatnce().addFilterLockPackage("com.leo.appmaster", false);
            Intent quickIntent = new Intent(mActivity, QuickGestureMiuiTip.class);
            quickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            quickIntent.putExtra("sys_name", "huawei");
            try {
                startActivity(quickIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Intent quickIntent = new Intent(mActivity, QuickGestureActivity.class);
            this.startActivity(quickIntent);
        }
    }

    private void gotoBackUp(int type) {
        Intent intent = new Intent(mActivity, BackUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void gotoEle(int type) {
        Intent dlIntent = new Intent(mActivity, EleActivity.class);
        dlIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dlIntent);
    }

    private void goToFlow() {
        Intent mIntent = new Intent(mActivity, FlowActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mIntent);
    }

    private void goToPrivateSms() {
        Intent intent = new Intent(mActivity,
                PrivacyContactActivity.class);
        intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goToAppHideVio() {
        String mPathString = ((LockSettingActivity) mActivity).getCoolBrowserPath();
        Intent intent = new Intent(mActivity, VideoHideMainActivity.class);
        intent.putExtra("cb_download_path", mPathString);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goToAppHidePic() {
        Intent intent = new Intent(mActivity, ImageHideMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goToAppWeiZhuang() {
        Intent intent = new Intent(mActivity, WeiZhuangActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void goToAppLock() {
        LockManager lm = LockManager.getInstatnce();
        LockMode curMode = lm.getCurLockMode();
        Intent intent;
        if (curMode != null && curMode.defaultFlag == 1 && !curMode.haveEverOpened) {
            intent = new Intent(mActivity, RecommentAppLockListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("target", 0);
            startActivity(intent);
            curMode.haveEverOpened = true;
            lm.updateMode(curMode);
        } else {
            intent = new Intent(mActivity, AppLockListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
