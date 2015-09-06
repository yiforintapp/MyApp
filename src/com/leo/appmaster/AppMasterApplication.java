
package com.leo.appmaster;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Handler;
import android.os.UserManager;

import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.bootstrap.Bootstrap;
import com.leo.appmaster.bootstrap.BootstrapGroup;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.imageloader.ImageLoader;

public class AppMasterApplication extends Application {

    private static AppMasterApplication mInstance;
    private static List<WeakReference<Activity>> mActivityList;
    private static List<WeakReference<Activity>> sResumedList;

    private static int[] mRootSteps = {
            Bootstrap.STEP_FOREGROUND,
            Bootstrap.STEP_BACKGROUND,
            Bootstrap.STEP_BACKGROUND_DELAY
    };

    public Handler mHandler;
    public static SharedPreferences sharedPreferences;
    public static String usedThemePackage;

    private ScheduledExecutorService mExecutorService;

    private Bootstrap mRootBootstrap;
    private Thread mUiThread;

    static {
        // For android L and above, daemon service is not work, so disable it
        if (PhoneInfo.getAndroidVersion() < 20) {
            System.loadLibrary("leo_service");
        }
    }

    private native void restartApplocker(int sdk, String userSerial);

    @Override
    public void onCreate() {
        super.onCreate();
        if (mInstance != null)
            return;

        mUiThread = Thread.currentThread();
        mInstance = this;
        mActivityList = new ArrayList<WeakReference<Activity>>();
        sResumedList = new ArrayList<WeakReference<Activity>>();
        mExecutorService = Executors.newScheduledThreadPool(3);
        mHandler = new Handler();

        sharedPreferences = getSharedPreferences("lockerTheme", Context.MODE_WORLD_WRITEABLE);
        usedThemePackage = sharedPreferences.getString("packageName", Constants.DEFAULT_THEME);

        // For android L and above, daemon service is not work, so disable it
        if (PhoneInfo.getAndroidVersion() < 20) {
            restartApplocker(PhoneInfo.getAndroidVersion(), getUserSerial());
        }

        mRootBootstrap = new BootstrapGroup();
        mRootBootstrap.mStepIds = mRootSteps;

        // 启动引导程序，包含：前台、后台、延时程序
        mRootBootstrap.execute();
      
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private String getUserSerial() {
        String userSerial = null;
        if (PhoneInfo.getAndroidVersion() >= 17) {
            try {
                UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);
                if (userManager != null) {
                    userSerial = String.valueOf(userManager
                            .getSerialNumberForUser(android.os.Process.myUserHandle()));
                }
            } catch (Exception e) {
            } catch (Error error) {
            }
        }
        return userSerial;
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ImageLoader.getInstance().clearMemoryCache();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ImageLoader.getInstance().clearMemoryCache();
        // mBackupManager.onDestory(this);
        // unregisterReceiver(mAppsEngine);
        // mAppsEngine.onDestroyed();
        LockManager.getInstatnce().unInit();
        SDKWrapper.endSession(this);
        // unregisterReceiver(mPrivacyReceiver);
        // ContentResolver cr = getContentResolver();
        // if (cr != null) {
        // cr.unregisterContentObserver(mCallLogObserver);
        // cr.unregisterContentObserver(mMessageObserver);
        // cr.unregisterContentObserver(mContactObserver);
        // }
    }

    public static AppMasterApplication getInstance() {
        return mInstance;
    }

//    public void postInAppThreadPool(Runnable runable) {
//        mExecutorService.execute(runable);
//    }
//
//    public void postInAppThreadPool(Runnable runable, long delay) {
//        mExecutorService.schedule(runable, delay, TimeUnit.MILLISECONDS);
//    }

    public void postInMainThread(Runnable runnable) {
        mHandler.post(runnable);
    }

    public boolean isUiThread() {
        return Thread.currentThread() == mUiThread;
    }

    // for force update strategy to exit application completely
    public synchronized void addActivity(Activity activity) {
        // mActivityList.add(activity);
        Iterator<WeakReference<Activity>> iterator = mActivityList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            Activity ac = reference.get();
            if (ac != null && ac == activity) {
                return;
            } else if (ac == null) {
                // 存放的activity已经被释放掉，移除引用
                iterator.remove();
            }
        }

        mActivityList.add(new WeakReference<Activity>(activity));
    }

    public synchronized void removeActivity(Activity activity) {
        Iterator<WeakReference<Activity>> iterator = mActivityList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            Activity ac = reference.get();
            if (ac == null || ac == activity) {
                // 移除掉已经被释放掉的ref
                iterator.remove();
            }
        }
    }

    public synchronized void exitApplication() {
        Iterator<WeakReference<Activity>> iterator = mActivityList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            Activity ac = reference.get();
            if (ac != null) {
                ac.finish();
            }
            iterator.remove();
        }

    }
    
    /**
     * 添加resumed过的Activity
     * @param activity
     */
    public synchronized void resumeActivity(Activity activity) {
        Iterator<WeakReference<Activity>> iterator = sResumedList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            Activity ac = reference.get();
            if (ac != null && ac == activity) {
                return;
            } else if (ac == null) {
                // 存放的activity已经被释放掉，移除引用
                iterator.remove();
            }
        }

        sResumedList.add(new WeakReference<Activity>(activity));
    }
    
    public synchronized void pauseActivity(Activity activity) {
        Iterator<WeakReference<Activity>> iterator = sResumedList.iterator();
        while (iterator.hasNext()) {
            WeakReference<Activity> reference = iterator.next();
            Activity ac = reference.get();
            if (ac == null || ac == activity) {
                // 移除掉已经被释放掉的ref
                iterator.remove();
            }
        }
    }
    
    /**
     * 是否在前台
     * @return
     */
    public boolean isForeground() {
        for (WeakReference<Activity> weakReference : sResumedList) {
            if (weakReference.get() != null) {
                return true;
            }
        }
        
        return false;
    }

    public static void setSharedPreferencesValue(String lockerTheme) {
        Editor editor = sharedPreferences.edit();
        editor.putString("packageName", lockerTheme);
        editor.apply();
        usedThemePackage = lockerTheme;
    }

    public static String getSelectedTheme() {
        return usedThemePackage;
    }

}
