
package com.leo.appmaster;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserManager;

import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.bootstrap.Bootstrap;
import com.leo.appmaster.bootstrap.BootstrapGroup;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.imageloader.ImageLoader;

public class AppMasterApplication extends Application {

    private static AppMasterApplication sInstance;
    private static List<WeakReference<Activity>> sActivityList;
    private static List<WeakReference<Activity>> sResumedList;
    public static boolean sCheckTs = true;
    public static long sAppOnCrate;
    public static boolean sIsSplashActioned = false;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private static int[] mRootSteps = {
            Bootstrap.STEP_FOREGROUND,
            Bootstrap.STEP_BACKGROUND,
            Bootstrap.STEP_BACKGROUND_DELAY
    };

    public Handler mHandler;
    public static SharedPreferences sharedPreferences;
    public static String usedThemePackage;
    private static int sLastVersion;

    private Bootstrap mRootBootstrap;

    static {
        // For android L and above, daemon service is not work, so disable it
        if (PhoneInfo.getAndroidVersion() < 20) {
            try {
                System.loadLibrary("leo_service");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private native void restartApplocker(int sdk, String userSerial);

    @Override
    public void onCreate() {
        super.onCreate();
        sAppOnCrate = SystemClock.elapsedRealtime();
        if (sInstance != null)
            return;

        sInstance = this;
        // Use old sort
        try {
            System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        } catch (Exception e) {
        }

        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        String lastVer = pref.getLastVersion();
        try {
            sLastVersion = Integer.parseInt(lastVer);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        sActivityList = new ArrayList<WeakReference<Activity>>();
        sResumedList = new ArrayList<WeakReference<Activity>>();
        mHandler = new Handler();

        sharedPreferences = getSharedPreferences("lockerTheme", Context.MODE_WORLD_WRITEABLE);
        usedThemePackage = sharedPreferences.getString("packageName", Constants.DEFAULT_THEME);

        // For android L and above, daemon service is not work, so disable it
        if (PhoneInfo.getAndroidVersion() < 20) {
            try {
                restartApplocker(PhoneInfo.getAndroidVersion(), getUserSerial());
            } catch (Throwable e) {
                e.printStackTrace();
            }
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
        SDKWrapper.endSession(this);
    }

    public static AppMasterApplication getInstance() {
        return sInstance;
    }

    // for force update strategy to exit application completely
    public synchronized void addActivity(Activity activity) {
        // sActivityList.add(activity);
        Iterator<WeakReference<Activity>> iterator = sActivityList.iterator();
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

        sActivityList.add(new WeakReference<Activity>(activity));
    }

    public synchronized void removeActivity(Activity activity) {
        Iterator<WeakReference<Activity>> iterator = sActivityList.iterator();
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
        Iterator<WeakReference<Activity>> iterator = sActivityList.iterator();
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
     *
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
     *
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

    /**
     * home是否在栈顶，即只有Home还存活, 用户在首页点击home键回桌面
     *
     * @return
     */
    public boolean isHomeOnTopAndBackground() {
        if (!sResumedList.isEmpty() || sActivityList.isEmpty()) {
            // resume list 不为空，说明在前台
            // activity list 为空，说明所有activity被销毁
            return false;
        }

        if (sActivityList.size() == 1) {
            WeakReference<Activity> reference = sActivityList.get(0);
            Activity activity = reference.get();
            if (activity != null && (activity instanceof HomeActivity)) {
                return true;
            }
        } else if (sActivityList.size() > 1) {
            WeakReference<Activity> reference0 = sActivityList.get(0);
            WeakReference<Activity> reference1 = sActivityList.get(1);
            Activity activity0 = reference0.get();
            Activity activity1 = reference1.get();
            if (activity0 != null && (activity0 instanceof HomeActivity)
                    && activity1 != null && (activity1 instanceof LockScreenActivity)) {
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

    /**
     * 是否是升级
     * 注意：进程起来后，此接口会一直有效，除非进程挂掉
     *
     * @return
     */
    public static boolean isAppUpgrade() {
        int versionCode = PhoneInfo.getVersionCode(sInstance);
        return versionCode > sLastVersion;
    }
}
