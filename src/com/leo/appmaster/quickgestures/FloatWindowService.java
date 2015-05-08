
package com.leo.appmaster.quickgestures;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyDeletEditEvent;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * FloatWindowService
 * 
 * @author run
 */
public class FloatWindowService extends Service {

    /**
     * 用于在线程中创建或移除悬浮窗。
     */
    private Handler mHandler = new Handler();

    /**
     * 定时器，定时进行检测当前应该创建还是移除悬浮窗。
     */
    private Timer timer;
    private boolean mIsRefreshTask = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LeoEventBus
                .getDefaultBus().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 开启定时器，每隔0.5秒刷新一次
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new RefreshTask(), 0, 2500);
        }
        return START_STICKY;
    }

    private void stopFloatWindowServiceTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void stopFloatWindow() {
        stopFloatWindowServiceTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(getApplicationContext(), FloatWindowService.class);
        startService(intent);
        timer.cancel();
        timer = null;
        LeoEventBus
                .getDefaultBus().unregister(this);
    }

    public void onEventMainThread(PrivacyDeletEditEvent event) {
        String flag = event.editModel;
        if (FloatWindowHelper.QUICK_GESTURE_SETTING_DIALOG_RADIO_FINISH_NOTIFICATION
                .equals(flag)) {
            // QuickGestureWindowManager.createFloatWindow(mHandler,
            // getApplicationContext());
            // 左
            if (!AppMasterPreference.getInstance(this).getDialogRadioLeftBottom()) {
                FloatWindowHelper.removeSwipWindow(this, 1);
                FloatWindowHelper.removeSwipWindow(this, 2);
                FloatWindowHelper.removeSwipWindow(this, 3);
            } else {
                FloatWindowHelper
                        .createFloatLeftBottomWindow(this);
                FloatWindowHelper
                        .createFloatLeftCenterWindow(this);
                FloatWindowHelper
                        .createFloatLeftTopWindow(this);
            }
            // 右
            if (!AppMasterPreference.getInstance(this).getDialogRadioRightBottom()) {
                FloatWindowHelper.removeSwipWindow(this, -1);
                FloatWindowHelper.removeSwipWindow(this, -2);
                FloatWindowHelper.removeSwipWindow(this, -3);
            } else {
                FloatWindowHelper
                        .createFloatRightBottomWindow(this);
                FloatWindowHelper
                        .createFloatRightCenterWindow(this);
                FloatWindowHelper
                        .createFloatRightTopWindow(this);
            }
        }

    }

    class RefreshTask extends TimerTask {
        @Override
        public void run() {
//            QuickGestureWindowManager.createFloatWindow(mHandler, getApplicationContext());
            // 当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗。
            // else if (!isHome() && MyWindowManager.isWindowShowing()) {
            // handler.post(new Runnable() {
            // @Override
            // public void run() {
            // Log.d("testservice", "当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗!!");
            // MyWindowManager.removeSmallWindow(getApplicationContext());
            // MyWindowManager.removeBigWindow(getApplicationContext());
            // }
            // });
            // }
            // 当前界面是桌面，且有悬浮窗显示，则更新内存数据。
            // else if (isHome() && MyWindowManager.isWindowShowing()) {
            // handler.post(new Runnable() {
            // @Override
            // public void run() {
            // Log.d("testservice", "当前界面是桌面，且有悬浮窗显示，则更新内存数据!!");
            // MyWindowManager.updateUsedPercent(getApplicationContext());
            // }
            // });
            // }
        }
    }

    /**
     * 判断当前界面是否是桌面
     */
    private boolean isHome() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }

    /**
     * 获得属于桌面的应用的应用包名称
     * 
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

}
