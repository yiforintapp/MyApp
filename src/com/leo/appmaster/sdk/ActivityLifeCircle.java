package com.leo.appmaster.sdk;

import android.app.Activity;
import android.os.SystemClock;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.update.UpdateActivity;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

/**
 * Created by Jasper on 2015/11/19.
 */
public class ActivityLifeCircle {
    private static final String TAG = "ActivityLifeCircle";
    private Activity mActivity;
    private AppMasterApplication mApplication;

    private long mResumedTs;

    public ActivityLifeCircle(Activity activity) {
        mActivity = activity;
        mApplication = AppMasterApplication.getInstance();
    }

    protected void onCreate() {
        mApplication.createActivity(mActivity);
    }

    protected void onStart() {
        mApplication.startActivity(mActivity);
        // 到前台，停止定时扫描
        PrivacyHelper.getInstance(mApplication).stopIntervalScanner();
    }

    protected void onResume() {
        LeoLog.d(TAG, "<ls> onResume..." + mActivity.getClass().getName());
        mResumedTs = SystemClock.elapsedRealtime();

        int times = LeoSettings.getInteger(PrefConst.KEY_ACTIVITY_TIMES, 0);
        times++;
        // 存储前台次数
        LeoSettings.setInteger(PrefConst.KEY_ACTIVITY_TIMES, times);

        mApplication.resumeActivity(mActivity);
        if ((mActivity instanceof HomeActivity) ||
                (mActivity instanceof UpdateActivity) ||
                (mActivity instanceof LockScreenActivity)) {
            return;
        }

        // 页面切换，执行一次扫描
        PrivacyHelper.getInstance(mApplication).scanOneTimeSilenty();
    }

    protected void onPause() {
        long showTs = SystemClock.elapsedRealtime() - mResumedTs;

        long totalTs = LeoSettings.getLong(PrefConst.KEY_ACTIVITY_TS, 0L);
        totalTs += showTs;
        mApplication.pauseActivity(mActivity);

        // 存储前台展示时间
        LeoSettings.setLong(PrefConst.KEY_ACTIVITY_TS, totalTs);
        LeoLog.d(TAG, "<ls> onPause..." + mActivity.getClass().getName() + " | totalTs: " + totalTs);
    }

    protected void onStop() {
        mApplication.stopActivity(mActivity);
        if (!mApplication.isVisible()) {
            PrivacyHelper.getInstance(mApplication).startIntervalScanner(0);
        }
    }

    protected void onDestroy() {
        mApplication.destroyActivity(mActivity);
        mActivity = null;
    }
}
