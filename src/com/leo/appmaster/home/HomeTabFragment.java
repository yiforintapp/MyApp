package com.leo.appmaster.home;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.manager.ChangeThemeManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.callfilter.BlackListInfo;
import com.leo.appmaster.callfilter.CallFilterConstants;
import com.leo.appmaster.callfilter.CallFilterInfo;
import com.leo.appmaster.callfilter.CallFilterManager;
import com.leo.appmaster.callfilter.CallFilterUtils;
import com.leo.appmaster.callfilter.TestDemo;
import com.leo.appmaster.intruderprotection.IntruderprotectionActivity;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.phoneSecurity.PhoneSecurityActivity;
import com.leo.appmaster.phoneSecurity.PhoneSecurityConstants;
import com.leo.appmaster.phoneSecurity.PhoneSecurityGuideActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.schedule.BlackDownLoadFetchJob;
import com.leo.appmaster.schedule.BlackUploadFetchJob;
import com.leo.appmaster.schedule.DownBlackFileFetchJob;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;
import com.leo.appmaster.wifiSecurity.WifiSecurityActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页下方4个tab
 *
 * @author Jasper
 */
public class HomeTabFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "HomeTabFragment";
    private static final boolean DBG = true;

    private ImageView mRedDot;
    // 首页4个tab
    private View mAppLockView;
    private View mIntruderView;
    private View mWifiSecurityView;
    private View mLostSecurityView;
    private ImageView mIvTabIcon1;
    private ImageView mIvTabIcon2;
    private ImageView mIvTabIcon3;
    private ImageView mIvTabIcon4;

    private View mRootView;
    private HomeActivity mActivity;

    private boolean mAnimating;

    public HomeTabFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (HomeActivity) activity;
    }

    public interface OnShowTabListener {
        void onShowTabListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkNewTheme();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRootView = view;

        mAppLockView = view.findViewById(R.id.home_app_lock_tv);
        MaterialRippleLayout.on(mAppLockView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleDuration(250)
                .rippleHover(true)
                .create();
        mAppLockView.setOnClickListener(this);

        mIntruderView = view.findViewById(R.id.home_intruder_tv);
        MaterialRippleLayout.on(mIntruderView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleDuration(250)
                .rippleHover(true)
                .create();
        mIntruderView.setOnClickListener(this);

        mWifiSecurityView = view.findViewById(R.id.home_wifi_tab);
        MaterialRippleLayout.on(mWifiSecurityView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleDuration(250)
                .rippleHover(true)
                .create();
        mWifiSecurityView.setOnClickListener(this);

        mLostSecurityView = view.findViewById(R.id.home_lost_tab);
        MaterialRippleLayout.on(mLostSecurityView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleDuration(250)
                .rippleHover(true)
                .create();
        mLostSecurityView.setOnClickListener(this);
        mRedDot = (ImageView) view.findViewById(R.id.have_theme_red_dot);

        mIvTabIcon1 = (ImageView) view.findViewById(R.id.home_ic_applcok_img);
        mIvTabIcon2 = (ImageView) view.findViewById(R.id.home_ic_wifi_img);
        mIvTabIcon3 = (ImageView) view.findViewById(R.id.home_ic_lost_img);
        mIvTabIcon4 = (ImageView) view.findViewById(R.id.home_ic_intruder_img);

        tryChangeToChrismasTheme();
    }

    private void tryChangeToChrismasTheme() {
        Drawable drawable1 = ChangeThemeManager.getChrismasThemeDrawbleBySlotId(ChangeThemeManager.BG_HOME_TAB1, getActivity());
        if (drawable1 != null) {
            mIvTabIcon1.setImageDrawable(drawable1);
            LayoutParams layoutParams = mIvTabIcon1.getLayoutParams();
            layoutParams.height = DipPixelUtil.dip2px(mActivity, 38);
            layoutParams.width = DipPixelUtil.dip2px(mActivity, 38);
            mIvTabIcon1.setLayoutParams(layoutParams);
        }
        Drawable drawable2 = ChangeThemeManager.getChrismasThemeDrawbleBySlotId(ChangeThemeManager.BG_HOME_TAB2, getActivity());
        if (drawable2 != null) {
            mIvTabIcon2.setImageDrawable(drawable2);
            LayoutParams layoutParams = mIvTabIcon2.getLayoutParams();
            layoutParams.height = DipPixelUtil.dip2px(mActivity, 38);
            layoutParams.width = DipPixelUtil.dip2px(mActivity, 38);
            mIvTabIcon2.setLayoutParams(layoutParams);
        }
        Drawable drawable3 = ChangeThemeManager.getChrismasThemeDrawbleBySlotId(ChangeThemeManager.BG_HOME_TAB3, getActivity());
        if (drawable3 != null) {
            mIvTabIcon3.setImageDrawable(drawable3);
            LayoutParams layoutParams = mIvTabIcon3.getLayoutParams();
            layoutParams.height = DipPixelUtil.dip2px(mActivity, 38);
            layoutParams.width = DipPixelUtil.dip2px(mActivity, 38);
            mIvTabIcon3.setLayoutParams(layoutParams);
        }
        Drawable drawable4 = ChangeThemeManager.getChrismasThemeDrawbleBySlotId(ChangeThemeManager.BG_HOME_TAB4, getActivity());
        if (drawable4 != null) {
            mIvTabIcon4.setImageDrawable(drawable4);
            LayoutParams layoutParams = mIvTabIcon4.getLayoutParams();
            layoutParams.height = DipPixelUtil.dip2px(mActivity, 38);
            layoutParams.width = DipPixelUtil.dip2px(mActivity, 38);
            mIvTabIcon4.setLayoutParams(layoutParams);
        }
    }

    public void dismissTab() {
        if (isRemoving() || isDetached() || getActivity() == null
                || mRootView.getVisibility() == View.GONE) {
            return;
        }

        mRootView.setVisibility(View.GONE);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_up_to_down);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mActivity.onTabAnimationFinish();
                mAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRootView.startAnimation(animation);
    }

    public boolean isAnimating() {
        return mAnimating;
    }

    public void showTab(final OnShowTabListener listener) {
        if (isRemoving() || isDetached() || getActivity() == null
                || mRootView.getVisibility() == View.VISIBLE) {
            return;
        }

        mRootView.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_down_to_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mActivity.onTabAnimationFinish();
                mAnimating = false;
                listener.onShowTabListener();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mRootView.startAnimation(animation);
    }

    public boolean isTabDismiss() {
        return mRootView.getVisibility() == View.GONE;
    }


    /*进入手机防盗*/
    private void startPhoneSecurity() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
            boolean flag = manager.isUsePhoneSecurity();
            Intent intent = null;
            if (!flag) {
                intent = new Intent(activity, PhoneSecurityGuideActivity.class);
                intent.putExtra(PhoneSecurityConstants.KEY_FORM_HOME_SECUR, true);
            } else {
                intent = new Intent(activity, PhoneSecurityActivity.class);
            }
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startRcommendLock(int target) {
        Intent intent = new Intent(getActivity(), RecommentAppLockListActivity.class);
        intent.putExtra("target", target);
        startActivity(intent);
    }

    private void checkNewTheme() {
//        boolean isClickLockTab = PreferenceTable.getInstance().
//                getBoolean(Constants.IS_CLICK_LOCK_TAB, false);
        String locSerial = AppMasterPreference.getInstance(mActivity)
                .getLocalThemeSerialNumber();
        String onlineSerial = AppMasterPreference.getInstance(mActivity)
                .getOnlineThemeSerialNumber();
        if (mRedDot != null) {
            if (!locSerial.equals(onlineSerial)) {
                mRedDot.setVisibility(View.VISIBLE);
            } else {
                mRedDot.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            switch (view.getId()) {
                case R.id.home_app_lock_tv:
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "lock");
                    LockManager mLockManager = (LockManager) MgrContext.
                            getManager(MgrContext.MGR_APPLOCKER);
                    LockMode curMode = mLockManager.getCurLockMode();
                    if (curMode != null && curMode.defaultFlag == 1 &&
                            !curMode.haveEverOpened) {
                        startRcommendLock(0);
                        curMode.haveEverOpened = true;
                        mLockManager.updateMode(curMode);
                    } else {
                        Intent intent = new Intent(getActivity(), AppLockListActivity.class);
                        startActivity(intent);
                    }
                    break;
                case R.id.home_intruder_tv:
                    // 入侵者防护
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "home_intruder");
                    Intent intent = new Intent(getActivity(), IntruderprotectionActivity.class);
                    startActivity(intent);
                    break;
                case R.id.home_wifi_tab:
//                     wifi安全
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "home_wifi");
                    Intent mIntent = new Intent(getActivity(), WifiSecurityActivity.class);
                    startActivity(mIntent);
                    break;
                case R.id.home_lost_tab:
                    // 手机防盗
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "home_theft");
                    startPhoneSecurity();
                    if (DBG) {
                        int[] pix = AppUtil.getScreenPix(getActivity());
                        LeoLog.i(TAG, "X=" + pix[0] + ",Y=" + pix[1]);
                        
                        CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                        pm.setFilterUserNumber(50000);
                        pm.setFilterTipFroUser(3000);
                        pm.setSerBlackTipNum(3000);
                        pm.setSerMarkTipNum(50);
                        BlackListInfo info = new BlackListInfo();
                        info.setNumber("13632840685");
                        info.setAddBlackNumber(2258);
                        info.setMarkerType(0);
                        info.setMarkerNumber(50000);
                        CallFilterManager cm = CallFilterManager.getInstance(AppMasterApplication.getInstance());
                        cm.addFilterFroParse(info);
                        Intent intent2 = new Intent(getActivity(), TestDemo.class);
                        startActivity(intent2);
//
                    }
                    break;
            }
        }
    }
}
