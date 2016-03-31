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

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.AppLockListActivity;
import com.leo.appmaster.applocker.RecommentAppLockListActivity;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.callfilter.CallFilterMainActivity;
import com.leo.appmaster.callfilter.TestDemo;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.imagehide.ImageHideMainActivity;
import com.leo.appmaster.mgr.CallFilterManager;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.model.AppItemInfo;
import com.leo.appmaster.phoneSecurity.PhoneSecurityActivity;
import com.leo.appmaster.phoneSecurity.PhoneSecurityConstants;
import com.leo.appmaster.phoneSecurity.PhoneSecurityGuideActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.MaterialRippleLayout;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;
import com.leo.appmaster.videohide.VideoHideMainActivity;
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
    private static final boolean DBG = false;

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
    private boolean mIsHasCallFilterRecords = false;

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
        checkCallFilterRecordCount();
        checkNewTheme();
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

        mIntruderView = view.findViewById(R.id.home_video);
        MaterialRippleLayout.on(mIntruderView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleDuration(250)
                .rippleHover(true)
                .create();
        mIntruderView.setOnClickListener(this);

        mWifiSecurityView = view.findViewById(R.id.home_img);
        MaterialRippleLayout.on(mWifiSecurityView)
                .rippleColor(getResources().getColor(R.color.home_tab_pressed))
                .rippleAlpha(1f)
                .rippleDuration(250)
                .rippleHover(true)
                .create();
        mWifiSecurityView.setOnClickListener(this);

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
                    LockManager mLockManager = (LockManager) MgrContext.
                            getManager(MgrContext.MGR_APPLOCKER);
                    LockMode curMode = mLockManager.getCurLockMode();
                    if (curMode != null && curMode.defaultFlag == 1 &&
                            !curMode.haveEverOpened) {
                        startRcommendLock(0);
                        curMode.haveEverOpened = true;
                        mLockManager.updateMode(curMode);
                    } else {
                        intent = new Intent(getActivity(), AppLockListActivity.class);
                        startActivity(intent);
                    }
                    break;
                case R.id.home_img:
                    // 图片隐藏
                    intent = new Intent(activity, ImageHideMainActivity.class);
                    activity.startActivity(intent);
                    break;
                case R.id.home_video:
                     //视频隐藏
                    intent = new Intent(activity, VideoHideMainActivity.class);
                    activity.startActivity(intent);
                    break;
                case R.id.home_more:
                    intent = new Intent(activity, HomeMoreActivity.class);
                    activity.startActivity(intent);
                    // 更多
                    break;
            }
        }
    }
}
