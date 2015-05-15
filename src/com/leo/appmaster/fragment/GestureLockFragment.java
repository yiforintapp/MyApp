
package com.leo.appmaster.fragment;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.gesture.LockPatternView;
import com.leo.appmaster.applocker.gesture.LockPatternView.Cell;
import com.leo.appmaster.applocker.gesture.LockPatternView.OnPatternListener;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.lockertheme.ResourceName;
import com.leo.appmaster.theme.LeoResources;
import com.leo.appmaster.theme.ThemeUtils;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.LockPatternUtils;

public class GestureLockFragment extends LockFragment implements
        OnPatternListener {
    private LockPatternView mLockPatternView;
    private TextView mGestureTip;
    private RelativeLayout mIconLayout;
    private ImageView mAppIcon;
    private ImageView mAppIconTop;
    private ImageView mAppIconBottom;
    private int mBottomIconRes = 0;
    private int mTopIconRes = 0;
    private Animation mShake;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_lock_gesture;
    }

    @Override
    protected void onInitUI() {
        mLockPatternView = (LockPatternView) findViewById(R.id.gesture_lockview);
        mLockPatternView.setOnPatternListener(this);
        mLockPatternView.setLockMode(mLockMode);
        mGestureTip = (TextView) findViewById(R.id.tv_gesture_tip);
        mIconLayout = (RelativeLayout) findViewById(R.id.iv_app_icon_layout);
        if (mLockMode == LockManager.LOCK_MODE_FULL && mPackageName != null) {
            mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
            mAppIcon.setImageDrawable(AppUtil.getDrawable(
                    mActivity.getPackageManager(), mPackageName));
            mAppIcon.setVisibility(View.VISIBLE);
            if (needChangeTheme()) {
                mAppIconTop = (ImageView) findViewById(R.id.iv_app_icon_top);
                mAppIconBottom = (ImageView) findViewById(R.id.iv_app_icon_bottom);
                mAppIconTop.setVisibility(View.VISIBLE);
                mAppIconBottom.setVisibility(View.VISIBLE);
                changeActivityBgAndIconByTheme();
            }
        }

    }

    private void changeActivityBgAndIconByTheme() {
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
        final String gesture = LockPatternUtils.patternToString(pattern);
        mLockPatternView.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkGesture(gesture);
            }
        }, 200);

    }

    private void checkGesture(String gesture) {
        mInputCount++;
        AppMasterPreference pref = AppMasterPreference.getInstance(mActivity);
        String savedGesture = pref.getGesture();
        if (savedGesture != null && savedGesture.equals(gesture)) {
            ((LockScreenActivity) mActivity).onUnlockSucceed();
        } else {
            if (mInputCount >= mMaxInput) {
                ((LockScreenActivity) mActivity).onUnlockOutcount();
                mGestureTip.setText(R.string.please_input_gesture);
                mInputCount = 0;
            } else {
                mGestureTip.setText(String.format(
                        mActivity.getString(R.string.input_error_tip),
                        mInputCount + "", (mMaxInput - mInputCount) + ""));
            }
            mLockPatternView.clearPattern();
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
        mIconLayout.startAnimation(mShake);
    }

    @Override
    public void onLockPackageChanged(String lockedPackage) {
        if (!TextUtils.equals(lockedPackage, mPackageName)) {
            mPackageName = lockedPackage;
            mInputCount = 0;
            mGestureTip.setText(R.string.please_input_gesture);
            if (mAppIcon == null) {
                mAppIcon = (ImageView) findViewById(R.id.iv_app_icon);
            }
            mAppIcon.setImageDrawable(AppUtil.getDrawable(
                    mActivity.getPackageManager(), mPackageName));
        }

        mAppIcon.setImageDrawable(AppUtil.getDrawable(
                mActivity.getPackageManager(), mPackageName));
        mAppIcon.setVisibility(View.VISIBLE);
    }
}
