package com.leo.appmaster.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.airsig.AirSigActivity;
import com.leo.appmaster.airsig.airsigsdk.ASGui;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.callfilter.CallFilterMainActivity;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.fragment.GuideFragment;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.mgr.CallFilterManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.phoneSecurity.PhoneSecurityActivity;
import com.leo.appmaster.phoneSecurity.PhoneSecurityConstants;
import com.leo.appmaster.phoneSecurity.PhoneSecurityGuideActivity;
import com.leo.appmaster.privacy.ImagePrivacy;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.privacy.VideoPrivacy;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.videohide.VideoHideMainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页下方4个tab
 *
 * @author Jasper
 */
public class HomeTabFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "HomeTabFragment";
    private static final boolean DBG = false;

    private ImageView mRedDot;
    private View mAppLockView;
    private View mIntruderView;
    private View mWifiSecurityView;
    private View mLostSecurityView;
    private ImageView mIvTabIcon1;
    private ImageView mIvTabIcon2;
    private ImageView mIvTabIcon3;
    private ImageView mIvTabIcon4;
    private boolean mIsHasCallFilterRecords = false;

    private ImageView mIvRedDotAtMore;

    private View mRootView;
    private HomeActivity mActivity;

    private boolean mAnimating;
    private View mInterceptView;
    private View mMagicLockView;
    private View mPhoneLostView;
    public static final String FROM_HOME_APP = "from_home_app";

    private boolean isAirSigCanUse = false;

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
        checkCallFilterRecordCount();
        checkNewTheme();
        updateRedDot();
    }

    private void updateRedDot() {
        if (!LeoSettings.getBoolean(PrefConst.KEY_HOME_MORE_CONSUMED, false)) {
            mIvRedDotAtMore.setVisibility(View.VISIBLE);
        } else {
            mIvRedDotAtMore.setVisibility(View.GONE);
        }
    }

    private void checkCallFilterRecordCount() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                CallFilterManager mCallManger = (CallFilterManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                LeoLog.i("tess", "getCallFilterGrCount = " + mCallManger.getCallFilterGrCount());
                if (mCallManger.getCallFilterGrCount() != 0) {
                    mIsHasCallFilterRecords = true;
                } else {
                    mIsHasCallFilterRecords = false;
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isAirSigCanUse = ASGui.getSharedInstance().isSensorAvailable();
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

        mIvRedDotAtMore = (ImageView) view.findViewById(R.id.more_consumed_red_dot);

        mAppLockView = view.findViewById(R.id.home_app_lock_tv);
        MaterialRippleLayout.on(mAppLockView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleDuration(250)
                .rippleHover(true)
                .create();
        mAppLockView.setOnClickListener(this);

        mWifiSecurityView = view.findViewById(R.id.home_img);
        MaterialRippleLayout.on(mWifiSecurityView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleDuration(250)
                .rippleHover(true)
                .create();
        mWifiSecurityView.setOnClickListener(this);

        mIntruderView = view.findViewById(R.id.home_video);
        MaterialRippleLayout.on(mIntruderView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleDuration(250)
                .rippleHover(true)
                .create();
        mIntruderView.setOnClickListener(this);

        mInterceptView = view.findViewById(R.id.home_intercept);
        MaterialRippleLayout.on(mInterceptView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleDuration(250)
                .rippleHover(true)
                .create();
        mInterceptView.setOnClickListener(this);


//        if (isAirSigCanUse) {
        mMagicLockView = view.findViewById(R.id.home_magiclock);
        MaterialRippleLayout.on(mMagicLockView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleDuration(250)
                .rippleHover(true)
                .create();
        mMagicLockView.setOnClickListener(this);

        ImageView iv_icon = (ImageView) view.findViewById(R.id.home_ic_magiclock);
        TextView tv_icon = (TextView) view.findViewById(R.id.tv_magiclock);
        if (!isAirSigCanUse) {
            iv_icon.setBackgroundResource(R.drawable.icon_home_antitheft);
            tv_icon.setText(getString(R.string.home_tab_lost));
        } else {
            iv_icon.setBackgroundResource(R.drawable.icon_home_magiclock);
            tv_icon.setText(getString(R.string.airsig_settings_activity_title));
        }


//        } else {
//            mPhoneLostView = view.findViewById(R.id.home_phone_protect);
//            MaterialRippleLayout.on(mPhoneLostView)
//                    .rippleColor(getResources().getColor(R.color.home_tab_pressed))
//                    .rippleAlpha(1f)
//                    .rippleDuration(250)
//                    .rippleHover(true)
//                    .create();
//            mPhoneLostView.setOnClickListener(this);
//        }


        mLostSecurityView = view.findViewById(R.id.home_more);
        MaterialRippleLayout.on(mLostSecurityView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleDuration(250)
                .rippleHover(true)
                .create();
        mLostSecurityView.setOnClickListener(this);
        mRedDot = (ImageView) view.findViewById(R.id.have_theme_red_dot);

        mIvTabIcon1 = (ImageView) view.findViewById(R.id.home_ic_applcok_img);
        mIvTabIcon2 = (ImageView) view.findViewById(R.id.home_ic_img_img);
        mIvTabIcon3 = (ImageView) view.findViewById(R.id.home_ic_video);
        mIvTabIcon4 = (ImageView) view.findViewById(R.id.home_ic_more);

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
//                mActivity.onTabAnimationFinish();
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
        mAnimating = false;
        if (isRemoving() || isDetached() || getActivity() == null
                || mRootView.getVisibility() == View.VISIBLE) {
            return;
        }

        mRootView.setVisibility(View.VISIBLE);
    }

    public boolean isTabDismiss() {
        return mRootView.getVisibility() == View.GONE;
    }

    private void startRcommendLock(int target) {
        LockManager mLockManager = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
        List<String> lockList = mLockManager.getCurLockList();
        //include appmaster
        int lockStringsNum = lockList.size() - 1;

        ArrayList<AppItemInfo> localAppList = AppLoadEngine.getInstance(mActivity).getAllPkgInfo();
        List<String> defaultLockList = AppLoadEngine.getInstance(mActivity).getRecommendLockList();

        int mRecommandNum = 0;
        for (int i = 0; i < localAppList.size(); i++) {
            AppItemInfo info = localAppList.get(i);
            String pckName = info.packageName;
            for (int j = 0; j < defaultLockList.size(); j++) {
                String recommandPckName = defaultLockList.get(j);
                if (pckName.equals(recommandPckName)) {
                    mRecommandNum++;
                }
            }
        }

        if (mRecommandNum > lockStringsNum) {
            Intent intent = new Intent(getActivity(), RecommentAppLockListActivity.class);
            intent.putExtra("target", target);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), AppLockListActivity.class);
            startActivity(intent);
        }
    }

    private void checkNewTheme() {
//        boolean isClickLockTab = LeoPreference.getInstance().
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
            LeoPreference table = LeoPreference.getInstance();
            Intent intent = null;
            switch (view.getId()) {
                case R.id.home_app_lock_tv:
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "lock");
                    LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                    LockMode curMode = lm.getCurLockMode();
                    if (curMode != null && curMode.defaultFlag == 1 && !curMode.haveEverOpened) {
                        startRcommendLock(0);
                        curMode.haveEverOpened = true;
                        lm.updateMode(curMode);
                    } else {
                        intent = new Intent(getActivity(), AppLockListActivity.class);
                        intent.putExtra(FROM_HOME_APP, true);
                        LeoSettings.setBoolean(PrefConst.KEY_APP_COMSUMED, true);
                        startActivity(intent);
                    }
                    break;
                case R.id.home_img:
                    // 图片隐藏
                    int count = table.getInt(PrefConst.KEY_TOTAL_ENTER_HIDE_PIC, 0);
                    table.putInt(PrefConst.KEY_TOTAL_ENTER_HIDE_PIC, count + 1);
                    ImageHideMainActivity.mFromHomeEnter = false;
                    intent = new Intent(activity, ImageHideMainActivity.class);
                    activity.startActivity(intent);
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "hidpic");
                    break;
                case R.id.home_video:
                    //视频隐藏
                    int count2 = table.getInt(PrefConst.KEY_TOTAL_ENTER_HIDE_VIDEO, 0);
                    table.putInt(PrefConst.KEY_TOTAL_ENTER_HIDE_VIDEO, count2 + 1);
                    VideoHideMainActivity.mFromHomeEnter = false;
                    intent = new Intent(activity, VideoHideMainActivity.class);
                    activity.startActivity(intent);
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "hidvideo");
                    break;
                case R.id.home_intercept:
                    goToCallfilter();
//                    CallFilterManager mCmp = (CallFilterManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
//
//                    List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
//                    BlackListInfo info = new BlackListInfo();
//                    info.number = "18790729990";
//                    info.name = "run";
//                    info.blackNum = 24123412;
//                    info.markNum = 1231;
//                    infost.add(info);
//                    mCmp.addBlackList(infost, false);
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "block");
                    break;
                case R.id.home_magiclock:
                    if (isAirSigCanUse) {
                        Intent intent2 = new Intent(activity, AirSigActivity.class);
                        startActivity(intent2);
                        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "airsig");
                    } else {
                        gotoPhoneLost();
                    }
                    break;
                case R.id.home_more:
                    LeoSettings.setBoolean(PrefConst.KEY_HOME_MORE_CONSUMED, true);
                    LeoSettings.setBoolean(PrefConst.KEY_HOME_GUIDE_MORE, true);
                    intent = new Intent(activity, HomeMoreActivity.class);
                    activity.startActivity(intent);
                    SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "more");
                    // 更多
//                    Intent intent1 = new Intent(mActivity,PhoneSecurityActivity.class);
//                    startActivity(intent1);
                    if (GuideFragment.mIsClickMoreTip) {
                        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "home", "more_gd_cli");
                        GuideFragment.mIsClickMoreTip = false;
                    }
                    break;
//                case R.id.home_phone_protect:
//
//                    break;
            }
        }
    }

    private void gotoPhoneLost() {
        LostSecurityManagerImpl manager = (LostSecurityManagerImpl) MgrContext.getManager(MgrContext.MGR_LOST_SECURITY);
        boolean flag = manager.isUsePhoneSecurity();
        Intent lostIntent = null;
        if (!flag) {
            lostIntent = new Intent(mActivity, PhoneSecurityGuideActivity.class);
            lostIntent.putExtra(PhoneSecurityConstants.KEY_FORM_HOME_SECUR, true);
        } else {
            lostIntent = new Intent(mActivity, PhoneSecurityActivity.class);
        }
        try {
            SDKWrapper.addEvent(mActivity, SDKWrapper.P1, "more", "theft");
            startActivity(lostIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void goToCallfilter() {
        int count = LeoSettings.getInteger(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_CALLFILTER, 0);
        LeoSettings.setInteger(PrefConst.KEY_ACCUMULATIVE_TOTAL_ENTER_CALLFILTER, count + 1);
        Intent callFilter = new Intent(getActivity(), CallFilterMainActivity.class);
        if (mIsHasCallFilterRecords) {
            callFilter.putExtra("needMoveToTab2", true);
        }
        SDKWrapper.addEvent(getActivity(), SDKWrapper.P1, "more", "block");
        startActivity(callFilter);
    }
}
