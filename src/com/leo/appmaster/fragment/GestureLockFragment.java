
package com.leo.appmaster.fragment;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.airsig.AirSigActivity;
import com.leo.appmaster.airsig.AirSigSettingActivity;
import com.leo.appmaster.airsig.airsigsdk.ASGui;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.gesture.LockPatternView;
import com.leo.appmaster.applocker.gesture.LockPatternView.Cell;
import com.leo.appmaster.applocker.gesture.LockPatternView.OnPatternListener;
import com.leo.appmaster.applocker.lockswitch.SwitchGroup;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.SubmaineAnimEvent;
import com.leo.appmaster.intruderprotection.CameraSurfacePreview;
import com.leo.appmaster.lockertheme.ResourceName;
import com.leo.appmaster.mgr.IntrudeSecurityManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.theme.LeoResources;
import com.leo.appmaster.theme.ThemeUtils;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.LockPatternUtils;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.utils.Utilities;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.core.FailReason;
import com.leo.imageloader.core.ImageLoadingListener;
import com.leo.imageloader.core.ImageSize;

public class GestureLockFragment extends LockFragment implements
        OnPatternListener, OnClickListener {
    private LockPatternView mLockPatternView;
    private boolean mNeedIntruderProtection = false;
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
    private int mShowType;

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

        mViewBottom = findViewById(R.id.switch_bottom_content);
        mViewBottom.setOnClickListener(this);
        mIvBottom = (ImageView) findViewById(R.id.iv_reset_icon);
        mTvBottom = (TextView) findViewById(R.id.switch_bottom);
        initAirSig();

        LeoEventBus.getDefaultBus().register(this);
    }

    private void initAirSig() {
        boolean isAirsigOn = PreferenceTable.getInstance().getBoolean(AirSigActivity.AIRSIG_SWITCH, false);
        boolean isAirsigReady = ASGui.getSharedInstance().isSignatureReady(1);

        if (true) {
            mViewBottom.setVisibility(View.VISIBLE);
            int unlockType = PreferenceTable.getInstance().
                    getInt(AirSigSettingActivity.UNLOCK_TYPE, AirSigSettingActivity.NOMAL_UNLOCK);
            if (unlockType == AirSigSettingActivity.NOMAL_UNLOCK) {
                mShowType = AirSigSettingActivity.NOMAL_UNLOCK;
                mTvBottom.setText(getString(R.string.airsig_settings_lock_fragment_airsig));
                mIvBottom.setBackgroundResource(
                        R.drawable.reset_pass_number);
            } else {
                mShowType = AirSigSettingActivity.AIRSIG_UNLOCK;
                mTvBottom.setText(getString(R.string.airsig_settings_lock_fragment_normal));
                mIvBottom.setBackgroundResource(
                        R.drawable.reset_pass_gesture);
            }
        } else {
            mViewBottom.setVisibility(View.GONE);
        }
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
                //TODO 整个背景的引用
                RelativeLayout layout = (RelativeLayout) getActivity().findViewById(
                        R.id.activity_lock_layout);
                Drawable bgDrawable = themeRes.getDrawable(layoutBgRes);
                layout.setBackgroundDrawable(bgDrawable);
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
                            PreferenceTable.getInstance().putBoolean(PrefConst.KEY_IS_DELAY_TO_SHOW_CATCH, false);
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
            mShowType = AirSigSettingActivity.AIRSIG_UNLOCK;
            mTvBottom.setText(getString(R.string.airsig_settings_lock_fragment_normal));
            mIvBottom.setBackgroundResource(
                    R.drawable.reset_pass_gesture);
        } else {
            mShowType = AirSigSettingActivity.NOMAL_UNLOCK;
            mTvBottom.setText(getString(R.string.airsig_settings_lock_fragment_airsig));
            mIvBottom.setBackgroundResource(
                    R.drawable.reset_pass_number);
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
