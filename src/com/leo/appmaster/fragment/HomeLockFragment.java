
package com.leo.appmaster.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.LockModeActivity;
import com.leo.appmaster.applocker.LockModeView;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.WeiZhuangActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.LockModeEvent;
import com.leo.appmaster.eventbus.event.NewThemeEvent;
import com.leo.appmaster.home.AutoStartGuideList;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.lockertheme.LockerTheme;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.sdk.update.UIHelper;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;

public class HomeLockFragment extends BaseFragment implements OnClickListener, Selectable {

    private LockModeView mLockModeCircle;
    private TextView mAppLockBtn;
    private TipTextView mLockThemeBtn;
    private TextView mLockModeBtn;
    private TextView mLockSettingBtn;
    private ImageView mIvDisguiseIconShadow;
    private boolean isFromAppLockList = false;
    private boolean isDisguiseIconWithShadow = false;

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_home_lock;
    }

    @Override
    protected void onInitUI() {
        mLockModeCircle = (LockModeView) findViewById(R.id.lock_mode_circle);
        // mLockModeCircle.setOnClickListener(this);
        mAppLockBtn = (TextView) findViewById(R.id.app_lock);
        mAppLockBtn.setOnClickListener(this);
        mLockThemeBtn = (TipTextView) findViewById(R.id.lock_theme);
        mLockThemeBtn.setOnClickListener(this);
        mLockModeBtn = (TextView) findViewById(R.id.lock_mode);
        mLockModeBtn.setOnClickListener(this);
        mLockSettingBtn = (TextView) findViewById(R.id.lock_setting);
        mLockSettingBtn.setOnClickListener(this);
        mIvDisguiseIconShadow = (ImageView) findViewById(R.id.iv_home_lock_disguise_shadow);
        mLockModeCircle.startAnimation();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LeoEventBus.getDefaultBus().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        if (isDisguiseIconWithShadow)
        {
            mIvDisguiseIconShadow.setVisibility(View.GONE);
            // Drawable drawable = getResources().getDrawable(
            // R.drawable.disguise_icon);
            // drawable.setBounds(0, 0, drawable.getMinimumWidth(),
            // drawable.getMinimumHeight());
            // mLockSettingBtn.setCompoundDrawables(drawable, null, null, null);

            isDisguiseIconWithShadow = false;
        }

        updateModeUI();
        checkNewTheme();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mLockModeCircle != null) {
            mLockModeCircle.stopAnimation();
        }
    }

    @Override
    public void onDestroy() {
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroy();
    }

    private void checkNewTheme() {
        String locSerial = AppMasterPreference.getInstance(mActivity)
                .getLocalThemeSerialNumber();
        String onlineSerial = AppMasterPreference.getInstance(mActivity)
                .getOnlineThemeSerialNumber();
        if (!locSerial.equals(onlineSerial)) {
            mLockThemeBtn.showTip(true);
        } else {
            mLockThemeBtn.showTip(false);
        }
    }

    public void onEventMainThread(NewThemeEvent event) {
        mLockThemeBtn.showTip(event.newTheme);
    }

    public void onEventMainThread(LockModeEvent event) {
        mLockModeCircle.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateModeUI();
            }
        }, 500);
    }

    private void updateModeUI() {
        LockManager lm = LockManager.getInstatnce();
        mLockModeCircle.updateMode(lm.getCurLockName(), lm.getLockedAppCount() + "");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.app_lock:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home",
                        "lock");
                LockManager lm = LockManager.getInstatnce();
                LockMode curMode = lm.getCurLockMode();
                if (curMode != null && curMode.defaultFlag == 1 &&
                        !curMode.haveEverOpened) {
                    startRcommendLock(0);
                    AppMasterPreference.getInstance(mActivity).setIsHomeToLockList(true);
                    LeoLog.e("lockmore",
                            "enter lock lis tand set home to list true");
                    curMode.haveEverOpened = true;
                    lm.updateMode(curMode);
                } else {
                    enterLockList();
                    AppMasterPreference.getInstance(mActivity).setIsHomeToLockList(true);
                    LeoLog.e("lockmore",
                            "enter lock lis tand set home to list true");
                }
//                QuickGestureManager.getInstance(getActivity()).privacyContactSendReceiverToSwipe(
//                        QuickGestureManager.PRIVACY_CALL);
                break;
            case R.id.lock_theme:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "theme");
                if (mLockThemeBtn != null && mLockThemeBtn.isShowingTip()) {
                    mLockThemeBtn.showTip(false);
                }
                enterLockTheme();
                break;
            case R.id.lock_mode:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "modes");
                enterLockMode();
                break;
            case R.id.lock_setting:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "appcover");
                // enterLockSetting();
                enterAppWeiZhuang();
                break;
            case R.id.lock_mode_circle:
                SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "home", "changemode");
                // int[] locations = view.getLocationOnScreen();
                //
                // int width = view.getWidth();
                // int height = view.getHeight();
                //
                // int[] center = new int[2];
                // center[0] = locations[0] + width / 2;
                // center[1] = locations[1] + height / 2;

                ((HomeActivity) mActivity).showModePages(true/* , center */);
                break;

            default:
                break;
        }

    }

    private void enterAppWeiZhuang() {

        if (AppMasterPreference.getInstance(mActivity).getIsNeedDisguiseTip())
        {
            Drawable drawable = getResources().getDrawable(
                    R.drawable.disguise_icon);

            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            mLockSettingBtn.setCompoundDrawables(drawable, null, null, null);
            AppMasterPreference.getInstance(mActivity).setIsNeedDisguiseTip(false);
        }
        Intent intent;
        intent = new Intent(mActivity, WeiZhuangActivity.class);
        mActivity.startActivity(intent);
    }

    // private void enterLockSetting() {
    // Intent intent = new Intent(mActivity, LockOptionActivity.class);
    // intent.putExtra(LockOptionActivity.TAG_COME_FROM,
    // LockOptionActivity.FROM_HOME);
    // mActivity.startActivity(intent);
    // }

    private void enterLockMode() {
        Intent intent = new Intent(mActivity, LockModeActivity.class);
        intent.putExtra("isFromHomeToLockMode", true);

        mActivity.startActivity(intent);
    }

    private void enterLockTheme() {
        Intent intent = new Intent(mActivity, LockerTheme.class);

        mActivity.startActivity(intent);
    }

    private void enterLockList() {
        Intent intent = null;
        intent = new Intent(mActivity, AppLockListActivity.class);

        startActivity(intent);
    }

    private void startRcommendLock(int target) {
        Intent intent = new Intent(mActivity, RecommentAppLockListActivity.class);
        intent.putExtra("target", target);
        startActivity(intent);
    }

    @Override
    public void onSelected(int position) {
        if (mLockModeCircle != null) {
            mLockModeCircle.startAnimation();
        }
    }

    @Override
    public void onScrolling() {
        if (mLockModeCircle != null) {
            mLockModeCircle.stopAnimation();
        }

    }

    public void playPretendEnterAnim() {
        if (null == mLockSettingBtn) {
            return;
        }
        final ObjectAnimator lastAlphaAnimator = ObjectAnimator.ofFloat(mLockSettingBtn, "alpha",
                0f, 1.0f).setDuration(300);

        ObjectAnimator alphaAnimator = ObjectAnimator
                .ofFloat(mLockSettingBtn, "alpha", 0f, 1.0f, 0f).setDuration(800);
        alphaAnimator.setRepeatCount(2);
        alphaAnimator.setRepeatMode(ValueAnimator.RESTART);
        alphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLockSettingBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                lastAlphaAnimator.start();
                // AppMasterPreference.getInstance(mActivity).setIsNeedDisguiseTip(true);
                // if(AppMasterPreference.getInstance(mActivity).getIsNeedDisguiseTip())
                {
                    mIvDisguiseIconShadow.setVisibility(View.VISIBLE);
                    // mLockSettingBtn.setCompoundDrawablePadding(pad)
                    isDisguiseIconWithShadow = true;
                }
            }
        });

        PropertyValuesHolder smallX = PropertyValuesHolder
                .ofFloat("scaleX", 1.0f, 1.2f, 1.0f);
        PropertyValuesHolder smallY = PropertyValuesHolder
                .ofFloat("scaleY", 1.0f, 1.2f, 1.0f);
        ObjectAnimator gestureSmall = (ObjectAnimator) ObjectAnimator.ofPropertyValuesHolder(
                mLockSettingBtn, smallX, smallY);
        gestureSmall.setDuration(800);
        gestureSmall.setRepeatCount(2);
        gestureSmall.setRepeatMode(ValueAnimator.RESTART);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(alphaAnimator, gestureSmall);
        set.start();
    }

}
