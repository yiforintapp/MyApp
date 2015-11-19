package com.leo.appmaster.sdk;

import android.app.Activity;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.privacy.PrivacyHelper;

/**
 * Created by Jasper on 2015/11/19.
 */
public class ActivityLifeCircle {
    private Activity mActivity;
    private AppMasterApplication mApplication;

    public ActivityLifeCircle(Activity activity) {
        mActivity = activity;
        mApplication = AppMasterApplication.getInstance();
    }

    protected void onCreate() {
        mApplication.addActivity(mActivity);
    }

    protected void onStart() {
        mApplication.resumeActivity(mActivity);
        // 到前台，停止定时扫描
        PrivacyHelper.getInstance(mApplication).stopIntervalScanner();
    }

    protected void onResume() {
        if (mActivity instanceof HomeActivity) return;

        // 页面切换，执行一次扫描
        PrivacyHelper.getInstance(mApplication).scanOneTime();
    }

    protected void onPause() {

    }

    protected void onStop() {
        mApplication.pauseActivity(mActivity);
    }

    protected void onDestroy() {
        mApplication.removeActivity(mActivity);
        mActivity = null;

        if (!mApplication.isForeground()) {
            PrivacyHelper.getInstance(mApplication).startIntervalScanner(0);
        }
    }
}
