package com.leo.appmaster.sdk;

import android.app.Activity;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.update.UpdateActivity;

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
        if ((mActivity instanceof HomeActivity) ||
                (mActivity instanceof UpdateActivity) ||
                (mActivity instanceof LockScreenActivity)) {
            return;
        }

        // 页面切换，执行一次扫描
        PrivacyHelper.getInstance(mApplication).scanOneTimeSilenty();
    }

    protected void onPause() {

    }

    protected void onStop() {
        mApplication.pauseActivity(mActivity);

        if (!mApplication.isForeground()) {
            PrivacyHelper.getInstance(mApplication).startIntervalScanner(0);
        }
    }

    protected void onDestroy() {
        mApplication.removeActivity(mActivity);
        mActivity = null;
    }
}
