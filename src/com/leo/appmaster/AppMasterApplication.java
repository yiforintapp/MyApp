
package com.leo.appmaster;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.DisplayMetrics;

import com.android.internal.telephony.ITelephony;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.receiver.LockReceiver;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.appmanage.business.AppBusinessManager;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.NewThemeEvent;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.privacycontact.MessagePrivacyReceiver;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.privacycontact.PrivacyMessageContentObserver;
import com.leo.appmaster.privacycontact.PrivacyTrickUtil;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NotificationUtil;
import com.leo.appmaster.utils.Utilities;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

@SuppressLint("NewApi")
public class AppMasterApplication extends Application {
    private static final String MOVE_TO_NEW_APP = "move_to_new_app";

    private AppLoadEngine mAppsEngine;
    private AppBackupRestoreManager mBackupManager;
    private ITelephony mITelephony;
    private AudioManager mAudioManager;

    private PrivacyMessageContentObserver mMessageObserver;
    private PrivacyMessageContentObserver mCallLogObserver;
    private MessagePrivacyReceiver mPrivacyReceiver;

    private static AppMasterApplication mInstance;
    private static List<Activity> mActivityList;

    public Handler mHandler;
    public static SharedPreferences sharedPreferences;
    public static String usedThemePackage;
    // public static String number;

    public static int SDK_VERSION;
    public static float density;
    public static int densityDpi;
    public static String densityString;
    public static int MAX_OUTER_BLUR_RADIUS;
    public static final Uri CALL_LOG_URI = android.provider.CallLog.Calls.CONTENT_URI;

    private ExecutorService mExecutorService;

    static {
        System.loadLibrary("leo_service");
    }

    private native void restartApplocker(int sdk, String userSerial);

    @Override
    public void onCreate() {
        super.onCreate();
        initDensity(this);
        mActivityList = new ArrayList<Activity>();
        mInstance = this;
        mExecutorService = Executors.newFixedThreadPool(3);
        mHandler = new Handler();
        mAppsEngine = AppLoadEngine.getInstance(this);
        mBackupManager = new AppBackupRestoreManager(this);
        initImageLoader(getApplicationContext());
        sharedPreferences = getSharedPreferences("lockerTheme",
                Context.MODE_WORLD_WRITEABLE);
        usedThemePackage = sharedPreferences.getString("packageName",
                Constants.DEFAULT_THEME);

        registerPackageChangedBroadcast();
        SDKWrapper.iniSDK(this);
        startInitTask(this);

        // init lock manager
        LockManager.getInstatnce().init();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!AppMasterPreference.getInstance(AppMasterApplication.this).getFirstUse()) {
                    checkNew();
                }
            }
        }, 10000);
        restartApplocker(PhoneInfo.getAndroidVersion(), getUserSerial());
        registerReceiveMessageCallIntercept();
        PrivacyHelper.getInstance(this).computePrivacyLevel(PrivacyHelper.VARABLE_ALL);
    }

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

    /**
     * 短信拦截, 电话拦截
     */
    private void registerReceiveMessageCallIntercept() {
        mMessageObserver = new PrivacyMessageContentObserver(this, mHandler,
                PrivacyMessageContentObserver.MESSAGE_MODEL);
        getContentResolver().registerContentObserver(PrivacyContactUtils.SMS_INBOXS, true,
                mMessageObserver);
        mCallLogObserver = new PrivacyMessageContentObserver(this, mHandler,
                PrivacyMessageContentObserver.CALL_LOG_MODEL);
        getContentResolver().registerContentObserver(CALL_LOG_URI, true, mCallLogObserver);
        openEndCall();
        mPrivacyReceiver = new MessagePrivacyReceiver(mITelephony, mAudioManager);
        IntentFilter filter = new IntentFilter();
        filter.setPriority(Integer.MAX_VALUE);
        filter.addAction(PrivacyContactUtils.MESSAGE_RECEIVER_ACTION);
        filter.addAction(PrivacyContactUtils.MESSAGE_RECEIVER_ACTION2);
        filter.addAction(PrivacyContactUtils.MESSAGE_RECEIVER_ACTION3);
        filter.addAction(PrivacyContactUtils.CALL_RECEIVER_ACTION);
        filter.addAction(PrivacyContactUtils.SENT_SMS_ACTION);
        registerReceiver(mPrivacyReceiver, filter);
        postInAppThreadPool(new Runnable() {
            @Override
            public void run() {
                PrivacyTrickUtil.clearOtherApps(getApplicationContext());
            }
        });
    }

    // 打开endCall
    private void openEndCall() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getITelephonyMethod = TelephonyManager.class.getDeclaredMethod("getITelephony",
                    (Class[]) null);
            getITelephonyMethod.setAccessible(true);
            mITelephony = (ITelephony) getITelephonyMethod.invoke(mTelephonyManager,
                    (Object[]) null);
        } catch (Exception e) {
        }
    }

    private void registerPackageChangedBroadcast() {
        // Register intent receivers
        /*
         * IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
         * // 成功删除某个APK之后发出的广播 filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
         * filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
         * filter.addDataScheme("package"); registerReceiver(mAppsEngine,
         * filter);
         */
        IntentFilter filter = new IntentFilter();
        // 移动App完成之后发生的广播
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        // 正在移动App是发出的广播
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        // 设备当前区域设置已更改是发出的广播
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        // recommend list change
        filter.addAction(AppLoadEngine.ACTION_RECOMMEND_LIST_CHANGE);
        registerReceiver(mAppsEngine, filter);
    }

    private void startInitTask(final Context ctx) {
        postInAppThreadPool(new Runnable() {
            @Override
            public void run() {
                mAppsEngine.preloadAllBaseInfo();
                AppBusinessManager.getInstance(mInstance).init();
                mBackupManager.getBackupList();
                judgeLockService();
                judgeLockAlert();
                // judgeStatictiUnlockCount();
                initImageLoader();
            }
        });
    }

    private void judgeStatictiUnlockCount() {
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        if (!pref.getLastVersion().equals(PhoneInfo.getVersionCode(this))) {
            pref.setUnlockCount(0);
        }
    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .threadPoolSize(Constants.MAX_THREAD_POOL_SIZE)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSizePercentage(12)
                .diskCacheSize(Constants.MAX_DISK_CACHE_SIZE) // 100 Mb
                .denyCacheImageMultipleSizesInMemory().build();
        ImageLoader.getInstance().init(config);
    }

    private void judgeLockAlert() {
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        if (pref.isReminded()) {
            return;
        }
        Calendar calendar;
        Intent intent;
        AlarmManager am = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        if (!pref.getLastVersion().equals(PhoneInfo.getVersionCode(this))) { // is
                                                                             // new
                                                                             // version
            pref.setHaveEverAppLoaded(false);
            pref.setLastVersion(PhoneInfo.getVersionCode(this));
            intent = new Intent(this, LockReceiver.class);
            intent.setAction(LockReceiver.ALARM_LOCK_ACTION);

            calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            pref.setLastAlarmSetTime(calendar.getTimeInMillis());
            calendar.add(Calendar.DATE, Constants.LOCK_TIP_INTERVAL_OF_DATE);
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        } else { // not new install
            calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            long detal = calendar.getTimeInMillis() - pref.getInstallTime();
            intent = new Intent(this, LockReceiver.class);
            intent.setAction(LockReceiver.ALARM_LOCK_ACTION);
            if (detal < Constants.LOCK_TIP_INTERVAL_OF_MS) {
                PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                        + Constants.LOCK_TIP_INTERVAL_OF_MS - detal, pi);
                pref.setLastAlarmSetTime(calendar.getTimeInMillis());
            } else {
                sendBroadcast(intent);
            }
        }
    }

    private void judgeLockService() {
        if (AppMasterPreference.getInstance(this).getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
            Intent serviceIntent = new Intent(this, TaskDetectService.class);
            serviceIntent.putExtra(TaskDetectService.EXTRA_STARTUP_FROM,
                    "main activity");

            startService(serviceIntent);
        }
    }

    public AppBackupRestoreManager getBuckupManager() {
        return mBackupManager;
    }

    public void checkNew() {
        checkNewTheme();
        checkNewAppBusiness();
    }

    private void showNewThemeTip(String title, String content) {
        if (shouldShowTip()) {
            // send new theme broadcast
            Intent intent = new Intent(Constants.ACTION_NEW_THEME);
            sendBroadcast(intent);

            if (Utilities.isEmpty(title)) {
                title = getString(R.string.find_new_theme);
            }

            if (Utilities.isEmpty(content)) {
                content = getString(R.string.find_new_theme_content);
            }

            // show new theme status tip
            Notification notif = new Notification();
            intent = new Intent(this, StatusBarEventService.class);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                    StatusBarEventService.EVENT_NEW_THEME);
            PendingIntent contentIntent = PendingIntent.getService(this, 0,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notif.icon = R.drawable.ic_launcher_notification;
            notif.tickerText = title;
            notif.flags = Notification.FLAG_AUTO_CANCEL;
            notif.setLatestEventInfo(this, title, content, contentIntent);
            NotificationUtil.setBigIcon(notif, R.drawable.ic_launcher_notification_big);
            notif.when = System.currentTimeMillis();
            NotificationManager nm = (NotificationManager) this
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(0, notif);
        }
    }

    private void showNewBusinessTip(String title, String content) {
        if (shouldShowTip()) {
            if (Utilities.isEmpty(title)) {
                title = getString(R.string.new_app_tip_title);
            }

            if (Utilities.isEmpty(content)) {
                content = getString(R.string.new_app_tip_content);
            }

            // show red tip
            AppMasterPreference sp_red_ti = AppMasterPreference.getInstance(this);
            sp_red_ti.setHomeFragmentRedTip(true);
            sp_red_ti.setHotAppActivityRedTip(true);

            // show business status tip
            Intent intent = null;
            Notification notif = new Notification();
            intent = new Intent(this, StatusBarEventService.class);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                    StatusBarEventService.EVENT_BUSINESS_APP);

            PendingIntent contentIntent = PendingIntent.getService(this, 1,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notif.icon = R.drawable.ic_launcher_notification;
            notif.tickerText = title;
            notif.flags = Notification.FLAG_AUTO_CANCEL;
            notif.setLatestEventInfo(this, title, content, contentIntent);
            NotificationUtil.setBigIcon(notif, R.drawable.ic_launcher_notification_big);
            notif.when = System.currentTimeMillis();
            NotificationManager nm = (NotificationManager) this
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(1, notif);
        }
    }

    private boolean shouldShowTip() {
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        long lastTime = pref.getLastShowTime();
        long nowTime = System.currentTimeMillis();
        if (lastTime > 0) {
            Time time = new Time();
            time.set(lastTime);
            int lastYear = time.year;
            int lastDay = time.yearDay;
            time.set(nowTime);
            if (lastYear == time.year && lastDay == time.yearDay) {
                return false;
            }
        }
        pref.setLastShowTime(nowTime);
        return true;
    }

    protected void checkNewAppBusiness() {
        final AppMasterPreference pref = AppMasterPreference.getInstance(this);
        long curTime = System.currentTimeMillis();

        long lastCheckTime = pref.getLastCheckBusinessTime();
        if (lastCheckTime == 0
                || (curTime - lastCheckTime) > pref.getBusinessCurrentStrategy()
        /* 2 * 60 * 1000 */) {
            HttpRequestAgent.getInstance(this).checkNewBusinessData(
                    new Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response,
                                boolean noMidify) {
                            if (response != null) {
                                try {
                                    JSONObject jsonObject = response.getJSONObject("data");
                                    JSONObject strategyObject = response.getJSONObject("strategy");
                                    JSONObject noticeObject = response.getJSONObject("notice");

                                    long successStrategy = pref.getBusinessSuccessStrategy();
                                    long failStrategy = pref.getBusinessFailStrategy();
                                    if (strategyObject != null) {
                                        successStrategy = strategyObject.getLong("s");
                                        if (successStrategy < AppMasterConfig.MIN_PULL_TIME) {
                                            successStrategy = AppMasterConfig.MIN_PULL_TIME;
                                        }
                                        failStrategy = strategyObject.getLong("f");
                                        if (failStrategy < AppMasterConfig.MIN_PULL_TIME) {
                                            failStrategy = AppMasterConfig.MIN_PULL_TIME;
                                        }
                                    }
                                    pref.setBusinessStrategy(successStrategy, successStrategy,
                                            failStrategy);
                                    if (jsonObject != null) {
                                        boolean hasNewBusinessData = jsonObject
                                                .getBoolean("need_update");
                                        String serialNumber = jsonObject
                                                .getString("update_flag");

                                        if (!hasNewBusinessData) {
                                            pref.setLocalBusinessSerialNumber(serialNumber);
                                        }
                                        pref.setOnlineBusinessSerialNumber(serialNumber);

                                        if (hasNewBusinessData) {
                                            String title = null;
                                            String content = null;
                                            if (noticeObject != null) {
                                                title = noticeObject.getString("title");
                                                content = noticeObject.getString("content");
                                            }
                                            showNewBusinessTip(title, content);
                                            AppMasterPreference pref = AppMasterPreference
                                                    .getInstance(AppMasterApplication.this);
                                            pref.setHomeBusinessTipClick(false);
                                        }
                                        pref.setLastCheckBusinessTime(System
                                                .currentTimeMillis());
                                    }

                                    TimerTask recheckTask = new TimerTask() {
                                        @Override
                                        public void run() {
                                            checkNewAppBusiness();
                                        }
                                    };
                                    Timer timer = new Timer();
                                    timer.schedule(recheckTask, pref.getBusinessCurrentStrategy());

                                } catch (JSONException e) {
                                    LeoLog.e("checkNewAppBusiness",
                                            e.getMessage());
                                }
                            }
                        }

                    }, new ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            pref.setThemeStrategy(pref.getBusinessFailStrategy(),
                                    pref.getBusinessSuccessStrategy(),
                                    pref.getBusinessFailStrategy());
                            TimerTask recheckTask = new TimerTask() {
                                @Override
                                public void run() {
                                    checkNewAppBusiness();
                                }
                            };
                            Timer timer = new Timer();
                            timer.schedule(recheckTask, pref.getBusinessCurrentStrategy());
                        }
                    });
        } else {
            TimerTask recheckTask = new TimerTask() {
                @Override
                public void run() {
                    checkNewAppBusiness();
                }
            };
            Timer timer = new Timer();
            long delay = pref.getBusinessCurrentStrategy()
                    - (curTime - lastCheckTime);
            timer.schedule(recheckTask, delay);
        }

    }

    public void checkNewTheme() {
        final AppMasterPreference pref = AppMasterPreference.getInstance(this);
        long curTime = System.currentTimeMillis();

        long lastCheckTime = pref.getLastCheckThemeTime();
        if (lastCheckTime == 0
                || (curTime - pref.getLastCheckThemeTime()) > pref.getThemeCurrentStrategy()) {
            HttpRequestAgent.getInstance(this).checkNewTheme(
                    new Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response,
                                boolean noMidify) {
                            if (response != null) {
                                try {
                                    JSONObject dataObject = response.getJSONObject("data");
                                    JSONObject strategyObject = response.getJSONObject("strategy");
                                    JSONObject noticeObject = response.getJSONObject("notice");

                                    long successStrategy = pref.getThemeSuccessStrategy();
                                    long failStrategy = pref.getThemeFailStrategy();
                                    if (strategyObject != null) {
                                        successStrategy = strategyObject.getLong("s");
                                        if (successStrategy < AppMasterConfig.MIN_PULL_TIME) {
                                            successStrategy = AppMasterConfig.MIN_PULL_TIME;
                                        }
                                        failStrategy = strategyObject.getLong("f");
                                        if (failStrategy < AppMasterConfig.MIN_PULL_TIME) {
                                            failStrategy = AppMasterConfig.MIN_PULL_TIME;
                                        }
                                    }
                                    pref.setThemeStrategy(successStrategy, successStrategy,
                                            failStrategy);

                                    if (dataObject != null) {
                                        boolean hasNewTheme = dataObject
                                                .getBoolean("need_update");
                                        String serialNumber = dataObject
                                                .getString("update_flag");

                                        if (!hasNewTheme) {
                                            pref.setLocalThemeSerialNumber(serialNumber);
                                        }
                                        pref.setOnlineThemeSerialNumber(serialNumber);

                                        if (hasNewTheme) {
                                            String title = null;
                                            String content = null;
                                            if (noticeObject != null) {
                                                title = noticeObject.getString("title");
                                                content = noticeObject.getString("content");
                                            }

                                            LeoEventBus.getDefaultBus().postSticky(
                                                    new NewThemeEvent(EventId.EVENT_NEW_THEME,
                                                            "new theme", true));

                                            showNewThemeTip(title, content);
                                        }
                                        pref.setLastCheckThemeTime(System
                                                .currentTimeMillis());
                                    }

                                    TimerTask recheckTask = new TimerTask() {
                                        @Override
                                        public void run() {
                                            checkNewTheme();
                                        }
                                    };
                                    Timer timer = new Timer();
                                    timer.schedule(recheckTask,
                                            pref.getThemeCurrentStrategy());

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    LeoLog.e("checkNewTheme", e.getMessage());
                                }
                            }
                        }

                    }, new ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            LeoLog.e("checkNewTheme", error.getMessage());
                            pref.setThemeStrategy(pref.getThemeFailStrategy(),
                                    pref.getThemeSuccessStrategy(), pref.getThemeFailStrategy());
                            TimerTask recheckTask = new TimerTask() {
                                @Override
                                public void run() {
                                    checkNewTheme();
                                }
                            };
                            Timer timer = new Timer();
                            timer.schedule(recheckTask, pref.getThemeCurrentStrategy());
                        }
                    });
        } else {
            TimerTask recheckTask = new TimerTask() {
                @Override
                public void run() {
                    checkNewTheme();
                }
            };
            Timer timer = new Timer();
            long delay = pref.getThemeCurrentStrategy()
                    - (curTime - lastCheckTime);
            timer.schedule(recheckTask, delay);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mBackupManager.onDestory(this);
        unregisterReceiver(mAppsEngine);
        mAppsEngine.onDestroyed();
        LockManager.getInstatnce().unInit();
        SDKWrapper.endSession(this);
        unregisterReceiver(mPrivacyReceiver);
        getContentResolver().unregisterContentObserver(mCallLogObserver);
        getContentResolver().unregisterContentObserver(mMessageObserver);
    }

    public static AppMasterApplication getInstance() {
        return mInstance;
    }

    public void postInAppThreadPool(Runnable runable) {
        mExecutorService.execute(runable);
    }

    // init ImageLoader
    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY)
                .memoryCacheSizePercentage(3)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(100 * 1024 * 1024)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .writeDebugLogs().build();
        ImageLoader.getInstance().init(config);
    }

    // for force update strategy to exit application completely
    public void addActivity(Activity activity) {
        mActivityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        mActivityList.remove(activity);
    }

    public void exitApplication() {
        for (Activity activity : mActivityList) {
            activity.finish();
        }
    }

    public static void setSharedPreferencesValue(String lockerTheme) {
        Editor editor = sharedPreferences.edit();
        editor.putString("packageName", lockerTheme);
        editor.commit();
        usedThemePackage = lockerTheme;
    }

    // public static void setSharedPreferencesNumber(String lockerThemeNumber) {
    // Editor editor = sharedPreferences.edit();
    // editor.putString("firstNumber", lockerThemeNumber);
    // editor.commit();
    // number = lockerThemeNumber;
    // }

    public static String getSelectedTheme() {
        return usedThemePackage;
    }

    private static void initDensity(Context context) {
        SDK_VERSION = android.os.Build.VERSION.SDK_INT;
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        density = dm.density;
        densityDpi = dm.densityDpi;
        switch (densityDpi) {
            case DisplayMetrics.DENSITY_XHIGH:
                densityString = "xhdpi";
                break;
        }
        MAX_OUTER_BLUR_RADIUS = (int) (density * 12.0f);
    }

    /**
     * Whether the sdk level is higher than 14 (android 4.0)
     * 
     * @return true if sdk level is higher than android 4.0, false otherwise
     */
    public static boolean isAboveICS() {
        return AppMasterApplication.SDK_VERSION >= 14;
    }
}
