
package com.leo.appmaster.fragment;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airsig.airsigengmulti.ASEngine;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.airsig.AirSigActivity;
import com.leo.appmaster.airsig.AirSigSettingActivity;
import com.leo.appmaster.airsig.airsigsdk.ASGui;
import com.leo.appmaster.airsig.airsigutils.EventLogger;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.gesture.LockPatternView;
import com.leo.appmaster.applocker.gesture.LockPatternView.Cell;
import com.leo.appmaster.applocker.gesture.LockPatternView.OnPatternListener;
import com.leo.appmaster.applocker.lockswitch.SwitchGroup;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.SubmaineAnimEvent;
import com.leo.appmaster.intruderprotection.CameraSurfacePreview;
import com.leo.appmaster.lockertheme.ResourceName;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.theme.LeoResources;
import com.leo.appmaster.theme.ThemeUtils;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.LockPatternUtils;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageSize;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class GestureLockFragment extends LockFragment implements
        OnPatternListener, OnClickListener {
    private LockPatternView mLockPatternView;
    private boolean mNeedIntruderProtection = false;
    private final static int DISMISSRESULT = 1;


    // GP包
    public static final String GPPACKAGE = "com.android.vending";
    // ----------------------
    private TextView mGestureTip;
    private RelativeLayout mIconLayout;
    private ImageView mAppIcon;
    private ImageView mAppIconTop;
    private ImageView mAppIconBottom;

    private View mViewBottom;
    private ImageView mIvBottom;
    private TextView mTvBottom;
    private View mAirSigTouchView;
    private TextView mTvMessage;
    private TextView mTvResult;
    private ProgressBar mProgressBar;

    private int mBottomIconRes = 0;
    private int mTopIconRes = 0;
    private FrameLayout mFlPreview;
    private CameraSurfacePreview mCameraSurPreview;
    // private ImageView mAdPic;
    private Animation mShake;
    private boolean mIsShowAnim/* 是否显示动画 */, mIsLoadAdSuccess/* 是否显示动画 */,
            mIsCamouflageLockSuccess/* 伪装是否解锁成功 */;

    private IntrudeSecurityManager mISManager;

    private boolean mCameraReleased = false;
    private LEOAlarmDialog mConfirmCloseDialog;

    private android.os.Handler mHandler = new android.os.Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DISMISSRESULT:
                    mTvResult.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };


    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_lock_gesture;
    }

    @Override
    protected void onInitUI() {
        mFlPreview = (FrameLayout) findViewById(R.id.camera_preview);

        mLockPatternView = (LockPatternView) findViewById(R.id.gesture_lockview);
        mLockPatternView.setOnPatternListener(this);
        mLockPatternView.setLockMode(mLockMode);
        mLockPatternView.setIsFromLockScreenActivity(true);

        mGestureTip = (TextView) findViewById(R.id.tv_gesture_tip);
        if (isShowTipFromScreen) {
            mGestureTip.setVisibility(View.VISIBLE);
        } else {
            mGestureTip.setVisibility(View.GONE);
        }
        mIconLayout = (RelativeLayout) findViewById(R.id.iv_app_icon_layout);

        mViewBottom = findViewById(R.id.switch_bottom_content);
        mViewBottom.setOnClickListener(this);
        mIvBottom = (ImageView) findViewById(R.id.iv_reset_icon);
        mTvBottom = (TextView) findViewById(R.id.switch_bottom);
        mAirSigTouchView = findViewById(R.id.airsig_lock);
        mAirSigTouchView.setOnTouchListener(new ImageButton.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onTouchThumb(v, event);
            }
        });
        mTvResult = (TextView) findViewById(R.id.textResultMessage);
        mTvMessage = (TextView) findViewById(R.id.textTouchAreaMessage);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarWaiting);

        initAirSig();

        if (mLockMode == LockManager.LOCK_MODE_FULL) {
            mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
            mAppIcon.setVisibility(View.VISIBLE);
            LockScreenActivity lsa = (LockScreenActivity) mActivity;
            if (lsa.mQuickLockMode) {
                List<LockMode> modes = mLockManager.getLockMode();
                LockMode targetMode = null;
                for (LockMode lockMode : modes) {
                    if (lockMode.modeId == lsa.mQuiclModeId) {
                        targetMode = lockMode;
                        break;
                    }
                }
                if (targetMode != null) {
                    mAppIcon.setImageDrawable(targetMode.getModeDrawable());
                    // mAppIcon.setImageDrawable(new
                    // BitmapDrawable(getResources(),
                    // targetMode.modeIcon));
                } else {
                    mAppIcon.setImageDrawable(AppUtil.getAppIcon(
                            mActivity.getPackageManager(), mActivity.getPackageName()));
                }
            } else if (mPackageName != null) {
                //wifi && blueTooth lock
                Drawable bd = getBd(mPackageName);
                mAppIcon.setImageDrawable(bd);
            }
            if (needChangeTheme()) {
                mAppIconTop = (ImageView) findViewById(R.id.iv_app_icon_top);
                mAppIconBottom = (ImageView) findViewById(R.id.iv_app_icon_bottom);
                mAppIconTop.setVisibility(View.VISIBLE);
                mAppIconBottom.setVisibility(View.VISIBLE);
                checkApplyTheme();
            }
        }

        LeoEventBus.getDefaultBus().register(this);
    }

    private void initAirSig() {
        boolean isAirsigOn = LeoSettings.getBoolean(AirSigActivity.AIRSIG_SWITCH, false);
        boolean isAirsigReady = ASGui.getSharedInstance().isSignatureReady(1);

        boolean isAirSigVaild = ASGui.getSharedInstance().isValidLicense();

        if (mLockMode == LockManager.LOCK_MODE_PURE) {
            //锁屏界面 帮助小贴士进入LockScreen
            mViewBottom.setVisibility(View.GONE);
        } else {
            if (isAirsigOn && isAirsigReady) {
                mViewBottom.setVisibility(View.VISIBLE);
                if (!isAirSigVaild) {
                    mLockPatternView.setVisibility(View.VISIBLE);
                    mAirSigTouchView.setVisibility(View.GONE);
                    mShowType = AirSigSettingActivity.NOMAL_UNLOCK;
                    mTvBottom.setText(getString(R.string.airsig_settings_lock_fragment_airsig));
                    mIvBottom.setBackgroundResource(
                            R.drawable.reset_airsig_gesture);
                } else {
                    int unlockType = LeoSettings.getInteger(AirSigSettingActivity.UNLOCK_TYPE, AirSigSettingActivity.NOMAL_UNLOCK);
                    if (unlockType == AirSigSettingActivity.NOMAL_UNLOCK) {
                        mLockPatternView.setVisibility(View.VISIBLE);
                        mAirSigTouchView.setVisibility(View.GONE);
                        mShowType = AirSigSettingActivity.NOMAL_UNLOCK;
                        mTvBottom.setText(getString(R.string.airsig_settings_lock_fragment_airsig));
                        mIvBottom.setBackgroundResource(
                                R.drawable.reset_airsig_gesture);
                    } else {
                        mLockPatternView.setVisibility(View.GONE);
                        mAirSigTouchView.setVisibility(View.VISIBLE);
                        mShowType = AirSigSettingActivity.AIRSIG_UNLOCK;
                        mTvBottom.setText(getString(R.string.airsig_settings_lock_fragment_normal));
                        mIvBottom.setBackgroundResource(
                                R.drawable.reset_pass_gesture);
                    }
                }

            } else {
                mViewBottom.setVisibility(View.GONE);
            }
        }
    }

    private boolean onTouchThumb(final View v, final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pressThumb(true);
            ASEngine.getSharedInstance().startRecordingSensor(null);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            showWaiting(true);
            ASEngine.getSharedInstance().completeRecordSensorToIdentifyAction(null, new ASEngine.OnIdentifySignatureResultListener() {
                @Override
                public void onResult(ASEngine.ASAction action, ASEngine.ASError error) {
                    showWaiting(false);
                    pressThumb(false);

                    if (null != action) {
                        showMatch(true, null);
                    } else if (null != error) {
                        switch (error) {
                            case NOT_FOUND:
                                if (error.userData.containsKey(ASEngine.ASError.KEY_VERIFICATION_TIMES_LEFT)) {
                                    int timesLeft = ((Integer) error.userData.get(ASEngine.ASError.KEY_VERIFICATION_TIMES_LEFT)).intValue();
                                    if (timesLeft == 0) {
                                        if (error.userData.containsKey(ASEngine.ASError.KEY_VERIFY_BLOCKED_SECONDS)) {
                                            showMatch(false, String.format(getString(R.string.airsig_verify_too_many_fails_wait), error.userData.get(ASEngine.ASError.KEY_VERIFY_BLOCKED_SECONDS)));
                                        } else {
//                                            alertTooManyFails();
                                        }
                                    } else {
                                        showMatch(false, String.format(getString(R.string.airsig_verify_not_match_times_left), timesLeft));
                                    }
                                } else {
                                    showMatch(false, null);
                                }
                                break;
                            case VERIFY_TOO_MANY_FAILED_TRIALS:
                                if (error.userData.containsKey(ASEngine.ASError.KEY_VERIFY_BLOCKED_SECONDS)) {
                                    showMatch(false, String.format(getString(R.string.airsig_verify_too_many_fails_wait), error.userData.get(ASEngine.ASError.KEY_VERIFY_BLOCKED_SECONDS)));
                                } else {
//                                    alertTooManyFails();
                                }
                                break;
                            default:
                                showMatch(false, null);
                                break;
                        }
                    }
//                    toneGenerator(mVerifyResult);
                }
            });
//            showThumb(false);
            return true;
        }
        return false;
    }

    private void showMatch(final boolean match, final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // show result
                if (null != message && message.length() > 0) {
                    mTvResult.setText(message);
                } else {
                    mTvResult.setText(match ? R.string.airsig_verify_match : R.string.airsig_verify_not_match);
                }
                mTvResult.setTextColor(getResources().getColor(match ? R.color.airsig_text_bright_blue : R.color.airsig_text_bright_red));
                mTvResult.setVisibility(View.VISIBLE);


                // Callback
                if (match) {
                    ((LockScreenActivity) mActivity).onUnlockSucceed();
                } else {
                    //dismiss result tv 1.5s later
                    mHandler.sendEmptyMessageDelayed(DISMISSRESULT, 1000);
                }

            }
        });
    }


    private void pressThumb(final boolean pressed) {
        if (mActivity == null) return;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pressed) {
                    // Touch Area
                    mAirSigTouchView.setBackground(getResources().getDrawable(R.drawable.airsig_bg_pre));
                    mTvMessage.setVisibility(View.INVISIBLE);
                    mTvResult.setVisibility(View.INVISIBLE);
                } else {
                    mAirSigTouchView.setBackground(getResources().getDrawable(R.drawable.airsig_bg));
                    mTvMessage.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showWaiting(final boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    mProgressBar.setVisibility(View.VISIBLE);
                } else {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void onEventMainThread(SubmaineAnimEvent event) {
        String eventMessage = event.eventMsg;
        /* 没有使用了伪装 */
        if ("no_camouflage_lock".equals(eventMessage)) {
            mIsShowAnim = true;
        } else if ("camouflage_lock_success".equals(eventMessage)) {
            mIsCamouflageLockSuccess = true;
            if (mIsLoadAdSuccess) {

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != ASEngine.getSharedInstance()) {
            ASEngine.getSharedInstance().startSensors();
        }
        checkIsAirSigTimeOut();
    }

    private void checkIsAirSigTimeOut() {
        boolean isAirSigVaild = ASGui.getSharedInstance().isValidLicense();
        if (!isAirSigVaild) {
            mLockPatternView.setVisibility(View.VISIBLE);
            mAirSigTouchView.setVisibility(View.GONE);
            mShowType = AirSigSettingActivity.NOMAL_UNLOCK;
            mTvBottom.setText(getString(R.string.airsig_settings_lock_fragment_airsig));
            mIvBottom.setBackgroundResource(
                    R.drawable.reset_airsig_gesture);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != ASEngine.getSharedInstance()) {
            ASEngine.getSharedInstance().stopSensors();
        }
    }

    public void removeCamera() {
        try {
            if (mCameraSurPreview != null) {
                mCameraSurPreview.release();
                if (mFlPreview != null) {
                    mFlPreview.removeView(mCameraSurPreview);
                }
            }
        } catch (Exception e) {
        }
        mCameraSurPreview = null;
        mCameraReleased = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mISManager = (IntrudeSecurityManager) MgrContext
                .getManager(MgrContext.MGR_INTRUDE_SECURITY);
        mNeedIntruderProtection = mISManager.getIntruderMode();
        mMaxInput = mISManager.getTimesForTakePhoto();
    }

    private void loadADPic(String url, ImageSize size, final ImageView v) {
        ImageLoader.getInstance().loadImage(
                url, size, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (loadedImage != null) {
                            v.setImageBitmap(loadedImage);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                    }
                });
    }

    @Override
    public void onDestroy() {
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onNewIntent() {
        LockScreenActivity lsa = (LockScreenActivity) mActivity;
        if (mAppIcon == null) {
            mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
        }
        if (mAppIcon == null) {
            return;
        }
        if (lsa != null && lsa.mQuickLockMode) {
            List<LockMode> modes = mLockManager.getLockMode();
            LockMode targetMode = null;
            if (modes != null) {
                for (LockMode lockMode : modes) {
                    if (lockMode != null && lockMode.modeId == lsa.mQuiclModeId) {
                        targetMode = lockMode;
                        break;
                    }
                }
            }
            if (targetMode != null) {
                // mAppIcon.setImageDrawable(new BitmapDrawable(getResources(),
                // targetMode.modeIcon));
                mAppIcon.setImageDrawable(targetMode.getModeDrawable());
            } else {
                mAppIcon.setImageDrawable(AppUtil.getAppIcon(
                        mActivity.getPackageManager(), mActivity.getPackageName()));
            }
        } else {
            if (!TextUtils.isEmpty(mPackageName)) {

                //wifi && blueTooth lock
                Drawable bd = getBd(mPackageName);
                mAppIcon.setImageDrawable(bd);

            } else {
                mAppIcon.setImageDrawable(AppUtil.getAppIcon(
                        mActivity.getPackageManager(), mActivity.getPackageName()));
            }
        }
    }

    private Drawable getBd(String mPackageName) {
        //wifi && blueTooth lock
        Drawable bd = null;
        if (mPackageName.equals(SwitchGroup.WIFI_SWITCH)) {
            bd = AppMasterApplication.getInstance().getResources().getDrawable(R.drawable.lock_wifi);
        } else if (mPackageName.equals(SwitchGroup.BLUE_TOOTH_SWITCH)) {
            bd = AppMasterApplication.getInstance().getResources().getDrawable(R.drawable.lock_bluetooth);
        } else if (mActivity != null) {
            bd = AppUtil.getAppIcon(
                    mActivity.getPackageManager(), mPackageName);
        }

        if (bd == null && mActivity != null) {
            bd = AppUtil.getAppIcon(
                    mActivity.getPackageManager(), mActivity.getPackageName());
        }
        return bd;
    }

    private void checkApplyTheme() {
        String pkgName = AppMasterApplication.getSelectedTheme();
        Context themeContext = LeoResources.getThemeContext(getActivity(), pkgName);// com.leo.appmaster:drawable/multi_theme_lock_bg
        Resources themeRes = themeContext.getResources();
        int layoutBgRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                ResourceName.THEME_GESTRUE_BG);
        if (layoutBgRes <= 0) {
            layoutBgRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                    ResourceName.THEME_GENERAL_BG);
        }

        if (themeRes != null) {
            if (layoutBgRes > 0) {
                if (mShowType == AirSigSettingActivity.NOMAL_UNLOCK) {
                    //TODO 整个背景的引用
                    RelativeLayout layout = (RelativeLayout) getActivity().findViewById(
                            R.id.activity_lock_layout);
                    Drawable bgDrawable = themeRes.getDrawable(layoutBgRes);
                    layout.setBackgroundDrawable(bgDrawable);
                } else {
                    changeBg(true, mPackageName);
                }
            }
        }

        mTopIconRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                ResourceName.THEME_TOP_ICON);
        mBottomIconRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                ResourceName.THEME_BOTTOM_ICON);
        if (themeRes != null) {
            if (mTopIconRes > 0) {
                mAppIconTop.setBackgroundDrawable(themeRes.getDrawable(mTopIconRes));
            }
            if (mBottomIconRes > 0) {
                mAppIconBottom.setBackgroundDrawable(themeRes.getDrawable(mBottomIconRes));
            }
        }
    }

    @Override
    public void onDestroyView() {
        mLockPatternView.cleangifResource();
        setShowText(false);
        removeCamera();
        super.onDestroyView();
    }

    @Override
    public void onPatternStart() {

    }

    @Override
    public void onPatternCleared() {

    }

    @Override
    public void onPatternCellAdded(List<Cell> pattern) {

    }

    @Override
    public void onPatternDetected(List<Cell> pattern) {
        LeoLog.d("testPattern", "onPatternDetected");
        final String gesture = LockPatternUtils.patternToString(pattern);
        AppMasterPreference pref = AppMasterPreference.getInstance(mActivity);
        final String savedGesture = pref.getGesture();

        if (Utilities.isEmpty(savedGesture) || savedGesture.equals(gesture)) {
            mLockPatternView.setIsUnlockSuccess(true);
        }

        mLockPatternView.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkGesture(gesture, savedGesture);
            }
        }, 200);
    }

    private void checkGesture(String gesture, String savegesture) {
        LeoLog.d("testPattern", "checkGesture");
        mInputCount++;
//        AppMasterPreference pref = AppMasterPreference.getInstance(mActivity);
//        String savedGesture = pref.getGesture();
        String savedGesture = savegesture;
        // AM-2936, no gesture, just unlock
        if (Utilities.isEmpty(savedGesture) || savedGesture.equals(gesture)) {
            ((LockScreenActivity) mActivity).onUnlockSucceed();
            mIsIntruded = false;
        } else {
            if (mInputCount >= mMaxInput) {
                // 判断是否已经开启入侵者防护
                if (mNeedIntruderProtection) {
                    // 将被入侵标记置为true
                    mIsIntruded = true;
                    if (mActivity instanceof LockScreenActivity) {
                        if (mCameraSurPreview == null && !mCameraReleased) {
                            mCameraSurPreview = new CameraSurfacePreview(mActivity);
                            mFlPreview.addView(mCameraSurPreview);
                        }
                        if (mCameraSurPreview != null) {
                            ((LockScreenActivity) mActivity).mHasTakePic = true;
                            ((LockScreenActivity) mActivity).mIsPicSaved = false;
                            LeoPreference.getInstance().putBoolean(PrefConst.KEY_IS_DELAY_TO_SHOW_CATCH, false);
                            ThreadManager.executeOnAsyncThreadDelay(new Runnable() {
                                @Override
                                public void run() {
                                    ((LockScreenActivity) mActivity).takePicture(mCameraSurPreview,
                                            mPackageName);
                                }
                            }, 1000);
                        }
                    }
                }
                ((LockScreenActivity) mActivity).onUnlockOutcount();
                mGestureTip.setText(R.string.please_input_gesture);
                mInputCount = 0;
            }
            if (!mLockPatternView.getIsStartDrawing()) {
                mLockPatternView.clearPattern();
            }
            shakeIcon();
        }
    }


    private boolean needChangeTheme() {
        return ThemeUtils.checkThemeNeed(getActivity())
                && (mLockMode == LockManager.LOCK_MODE_FULL);
    }

    private void shakeIcon() {
        if (mShake == null) {
            mShake = AnimationUtils.loadAnimation(mActivity,
                    R.anim.left_right_shake);
        }
        if (mIconLayout.getAlpha() > 0) {
            mIconLayout.startAnimation(mShake);
        }
        ((LockScreenActivity) mActivity).shakeIcon(mShake);
    }

    @Override
    public void onLockPackageChanged(String lockedPackage) {
        if (!TextUtils.equals(lockedPackage, mPackageName) && !TextUtils.isEmpty(lockedPackage)) {
            mPackageName = lockedPackage;
            mInputCount = 0;
            if (mGestureTip != null) {
                mGestureTip.setText(R.string.please_input_gesture);
            }
            if (mAppIcon == null) {
                mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
            }
            Drawable bd = getBd(mPackageName);
            mAppIcon.setImageDrawable(bd);
        }
        if (mAppIcon == null) {
            mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
        }
        mAppIcon.setVisibility(View.VISIBLE);
    }

    public void reInvalideGestureView() {
        mLockPatternView.resetIfHideLine();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.switch_bottom_content:
                switchUnlockType();
                break;
        }
    }

    private void switchUnlockType() {
        if (mShowType == AirSigSettingActivity.NOMAL_UNLOCK) {

            boolean isAirSigVaild = ASGui.getSharedInstance().isValidLicense();
            if (!isAirSigVaild) {
                showUpdateDialog();
            } else {
                mLockPatternView.setVisibility(View.GONE);
                mAirSigTouchView.setVisibility(View.VISIBLE);
                mShowType = AirSigSettingActivity.AIRSIG_UNLOCK;
                mTvBottom.setText(getString(R.string.airsig_settings_lock_fragment_normal));
                mIvBottom.setBackgroundResource(
                        R.drawable.reset_pass_gesture);
                changeBg(true, mPackageName);
            }

        } else {
            mLockPatternView.setVisibility(View.VISIBLE);
            mAirSigTouchView.setVisibility(View.GONE);
            mShowType = AirSigSettingActivity.NOMAL_UNLOCK;
            mTvBottom.setText(getString(R.string.airsig_settings_lock_fragment_airsig));
            mIvBottom.setBackgroundResource(
                    R.drawable.reset_airsig_gesture);
            changeBg(false, mPackageName);
        }
    }

    private void showUpdateDialog() {
        if (mConfirmCloseDialog == null) {
            mConfirmCloseDialog = new LEOAlarmDialog(mActivity);
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
        if (!mActivity.isFinishing()) {
            mConfirmCloseDialog.show();
        }
    }

    RelativeLayout bgLayout;

    public void changeBg(boolean isNormalbg, String packageName) {

        bgLayout = (RelativeLayout) getActivity().findViewById(
                R.id.activity_lock_layout);
        if (isNormalbg) {
            LockScreenActivity activity = (LockScreenActivity) mActivity;
            activity.setAppInfoBackground(getBd(packageName));
        } else if (needChangeTheme()) {
            LeoLog.d("testLockBg", "come in needChangeTheme");
            String pkgName = AppMasterApplication.getSelectedTheme();
            Context themeContext = LeoResources.getThemeContext(getActivity(), pkgName);// com.leo.appmaster:drawable/multi_theme_lock_bg
            Resources themeRes = themeContext.getResources();
            int layoutBgRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                    ResourceName.THEME_GESTRUE_BG);
            if (layoutBgRes <= 0) {
                layoutBgRes = ThemeUtils.getValueByResourceName(themeContext, "drawable",
                        ResourceName.THEME_GENERAL_BG);
            }

            if (themeRes != null) {
                if (layoutBgRes > 0) {
                    LeoLog.d("testLockBg", "change bg");
                    //TODO 整个背景的引用
                    Drawable bgDrawable = themeRes.getDrawable(layoutBgRes);
                    bgLayout.setBackgroundDrawable(bgDrawable);
                }
            }
        }

    }

    public View getIconView() {
        return mIconLayout;
    }


    @Override
    public void onActivityStop() {
        removeCamera();
        // Activity stopped, reset camera state
        mCameraReleased = false;
    }
}
