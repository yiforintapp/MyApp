
package com.leo.appmaster;

import java.io.File;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Handler;
import android.os.UserManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;

import com.android.internal.telephony.ITelephony;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.receiver.LockReceiver;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.NewThemeEvent;
import com.leo.appmaster.home.SplashActivity;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.privacycontact.MessagePrivacyReceiver;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.privacycontact.PrivacyMessageContentObserver;
import com.leo.appmaster.privacycontact.PrivacyTrickUtil;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NotificationUtil;
import com.leo.appmaster.utils.Utilities;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.ImageLoaderConfiguration;
import com.leo.imageloader.cache.Md5FileNameGenerator;
import com.leo.imageloader.core.QueueProcessingType;

@SuppressLint({
        "NewApi", "SimpleDateFormat"
})
public class AppMasterApplication extends Application {

    private AppLoadEngine mAppsEngine;
    private AppBackupRestoreManager mBackupManager;
    private ITelephony mITelephony;
    private AudioManager mAudioManager;

    private PrivacyMessageContentObserver mMessageObserver;
    private PrivacyMessageContentObserver mCallLogObserver;
    private PrivacyMessageContentObserver mContactObserver;
    private MessagePrivacyReceiver mPrivacyReceiver;

    private static AppMasterApplication mInstance;
    private static List<Activity> mActivityList;

    public Handler mHandler;
    public static SharedPreferences sharedPreferences;
    public static String usedThemePackage;

    private ScheduledExecutorService mExecutorService;
    // public static String number;

    public static int SDK_VERSION;
    public static float density;
    public static int densityDpi;
    public static String densityString;
    public static int MAX_OUTER_BLUR_RADIUS;
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
        mExecutorService = Executors.newScheduledThreadPool(3);
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

        mExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                checkNew();
                // 获取闪屏数据
                loadSplashDate();
            }
        }, 10, TimeUnit.SECONDS);
        if (AppMasterPreference.getInstance(getApplicationContext()).getIsFirstInstallApp()) {
            SplashActivity.deleteImage();
            AppMasterPreference.getInstance(getApplicationContext()).setIsFirstInstallApp(false);
        }
        restartApplocker(PhoneInfo.getAndroidVersion(), getUserSerial());
        registerReceiveMessageCallIntercept();
        PrivacyHelper.getInstance(this).computePrivacyLevel(PrivacyHelper.VARABLE_ALL);
        startService(new Intent(this, StatusBarEventService.class));
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
        ContentResolver cr = getContentResolver();
        if (cr != null) {
            mMessageObserver = new PrivacyMessageContentObserver(this, mHandler,
                    PrivacyMessageContentObserver.MESSAGE_MODEL);
            cr.registerContentObserver(PrivacyContactUtils.SMS_INBOXS, true,
                    mMessageObserver);
            mCallLogObserver = new PrivacyMessageContentObserver(this, mHandler,
                    PrivacyMessageContentObserver.CALL_LOG_MODEL);
            cr.registerContentObserver(PrivacyContactUtils.CALL_LOG_URI, true, mCallLogObserver);
            mContactObserver = new PrivacyMessageContentObserver(this, mHandler,
                    PrivacyMessageContentObserver.CONTACT_MODEL);
            cr.registerContentObserver(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, true,
                    mContactObserver);
        }
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
                checkUpdateFinish();
//                judgeLockService();
                // judgeStatictiUnlockCount();
                initImageLoader();
                mAppsEngine.preloadAllBaseInfo();
                // AppBusinessManager.getInstance(mInstance).init();
                mBackupManager.getBackupList();
                // GP check
                if (!AppUtil.appInstalled(AppMasterApplication.this, Constants.GP_PACKAGE)) {
                    SDKWrapper.addEvent(AppMasterApplication.this, SDKWrapper.P1, "gp_check",
                            "nogp");
                }
            }
        });
    }

    public void tryRemoveUnlockAllShortcut(Context ctx) {
        if (!AppMasterPreference.getInstance(ctx).getRemoveUnlockAllShortcutFlag())
        {
            // remove unlock all shortcut
            Intent shortcutIntent = new Intent(ctx, LockScreenActivity.class);
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // 之前在创建快捷方式的时候，未加任何的action, 移除快捷方式时必须加Intent.ACTION_VIEW
            shortcutIntent.setAction(Intent.ACTION_VIEW);
            shortcutIntent.putExtra("quick_lock_mode", true);
            shortcutIntent.putExtra("lock_mode_id", 0);
            shortcutIntent.putExtra("lock_mode_name", ctx.getString(R.string.unlock_all_mode));
            Intent shortcut = new Intent(
                    "com.android.launcher.action.UNINSTALL_SHORTCUT");
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, ctx.getString(R.string.unlock_all_mode));
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            shortcut.putExtra("duplicate", false);
            shortcut.putExtra("from_shortcut", true);
            ctx.sendBroadcast(shortcut);

            AppMasterPreference.getInstance(ctx).setRemoveUnlockAllShortcutFlag(true);
        }

    }

    protected void checkUpdateFinish() {
        judgeLockAlert();
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        String lastVercode = pref.getLastVersion();
        String versionCode = PhoneInfo.getVersionCode(this);
        if (TextUtils.isEmpty(lastVercode)) {
            // first install
            if (Integer.parseInt(versionCode) == 34) {
                // remove unlock-all shortcut v2.1
                tryRemoveUnlockAllShortcut(this);
            }
        } else {
            if (Integer.parseInt(lastVercode) < Integer.parseInt(versionCode)) {
                // hit update
                if (Integer.parseInt(versionCode) == 34) {
                    // remove unlock-all shortcut v2.1
                    tryRemoveUnlockAllShortcut(this);
                }
            }
        }
        pref.setLastVersion(versionCode);
        tryRemoveUnlockAllShortcut(this);
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
        if (lastCheckTime > 0
                && (Math.abs(curTime - lastCheckTime)) > pref.getBusinessCurrentStrategy()
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
            if (lastCheckTime == 0) { // First time, check business after 24
                                      // hours
                lastCheckTime = curTime;
                pref.setLastCheckBusinessTime(curTime);
                pref.setBusinessStrategy(AppMasterConfig.TIME_24_HOUR,
                        AppMasterConfig.TIME_12_HOUR, AppMasterConfig.TIME_2_HOUR);
            }
            long delay = pref.getBusinessCurrentStrategy()
                    - (curTime - lastCheckTime);
            timer.schedule(recheckTask, delay);
        }

    }

    public void checkNewTheme() {
        final AppMasterPreference pref = AppMasterPreference.getInstance(this);
        long curTime = System.currentTimeMillis();

        long lastCheckTime = pref.getLastCheckThemeTime();
        if (lastCheckTime > 0
                && (Math.abs(curTime - lastCheckTime)) > pref.getThemeCurrentStrategy()) {
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
            if (lastCheckTime == 0) { // First time, check theme after 24 hours
                lastCheckTime = curTime;
                pref.setLastCheckThemeTime(curTime);
                pref.setThemeStrategy(AppMasterConfig.TIME_24_HOUR, AppMasterConfig.TIME_12_HOUR,
                        AppMasterConfig.TIME_2_HOUR);
            }
            long delay = pref.getThemeCurrentStrategy()
                    - (curTime - lastCheckTime);
            timer.schedule(recheckTask, delay);
        }
    }

    /**
     * 加载闪屏
     */
    public void loadSplashDate() {
        final AppMasterPreference pref = AppMasterPreference.getInstance(this);
        final SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");
        long curTime = System.currentTimeMillis();
        Date currentDate = new Date(curTime);
        final String failDate = dateFormate.format(currentDate);
        long lastLoadTime = pref.getLastLoadSplashTime();
        if (lastLoadTime == 0
                || (curTime - pref.getLastLoadSplashTime()) >
                pref.getSplashCurrentStrategy()) {
            if ("splash_fail_default_date".equals(pref.getSplashLoadFailDate())
                    || pref.getSplashLoadFailNumber() < 0
                    || !failDate.equals(pref.getSplashLoadFailDate())
                    || (failDate.equals(pref.getSplashLoadFailDate()) && pref
                            .getSplashLoadFailNumber() <= 2)) {
                // 日期变化数据初始化
                if (!failDate.equals(pref.getSplashLoadFailDate())) {
                    if (pref.getSplashLoadFailNumber() != 0) {
                        pref.setSplashLoadFailNumber(0);
                    }
                    if (!"splash_fail_default_date"
                            .equals(pref.getSplashLoadFailDate())) {
                        pref.setSplashLoadFailDate("splash_fail_default_date");
                    }
                }
                HttpRequestAgent.getInstance(this).loadSplashDate(new
                        Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response, boolean noMidify) {
                                // Log.e("xxxxxxx", "拉取闪屏成功");
                                if (response != null) {
                                    try {
                                        String startDate = response.getString("b");
                                        String imageUrl = response.getString("a");
                                        String endDate = response.getString("c");
                                        String splashUriFlag = imageUrl + startDate + endDate;
                                        // Log.e("xxxxxxx", "数据：" +
                                        // splashUriFlag);
                                        // 保存获取的数据
                                        String prefStringUri = pref.getSplashUriFlag();
                                        int prefInt = pref.getSaveSplashIsMemeryEnough();
                                        if (!prefStringUri.equals(splashUriFlag) || prefInt != -1) {
                                            if (!prefStringUri.equals(splashUriFlag)) {
                                                if (splashUriFlag != null
                                                        && !"".equals(splashUriFlag)) {
                                                    pref.setSplashUriFlag(splashUriFlag);
                                                    // 初始化显示时间段
                                                    if (pref.getSplashStartShowTime() != -1) {
                                                        pref.setSplashStartShowTime(-1);
                                                    }
                                                    if (pref.getSplashEndShowTime() != -1) {
                                                        pref.setSplashEndShowTime(-1);
                                                    }
                                                }
                                            }
                                            SplashActivity.deleteImage();
                                            if (prefInt != -1) {
                                                pref.setSaveSplashIsMemeryEnough(-1);
                                            }
                                            if (endDate != null && !"".equals(endDate)) {
                                                long end = 0;
                                                try {
                                                    end = dateFormate.parse(endDate).getTime();
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                                pref.setSplashEndShowTime(end);
                                            }
                                            if (startDate != null && !"".equals(startDate)) {
                                                long start = 0;
                                                try {
                                                    start = dateFormate.parse(startDate).getTime();
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                                pref.setSplashStartShowTime(start);
                                            }
                                            if (imageUrl != null && !"".equals(imageUrl)) {
                                                getSplashImage(imageUrl);
                                            }
                                        }
                                        long successStrategy = pref.getThemeSuccessStrategy();
                                        long failStrategy = pref.getThemeFailStrategy();
                                        pref.setThemeStrategy(successStrategy, successStrategy,
                                                failStrategy);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                // 拉取成功数据初始化
                                if (pref.getSplashLoadFailNumber() != 0) {
                                    pref.setSplashLoadFailNumber(0);
                                }
                                if (!"splash_fail_default_date"
                                        .equals(pref.getSplashLoadFailDate())) {
                                    pref.setSplashLoadFailDate("splash_fail_default_date");
                                }
                                TimerTask recheckTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        loadSplashDate();
                                    }
                                };
                                Timer timer = new Timer();
                                timer.schedule(recheckTask, pref.getSplashCurrentStrategy());
                            }
                        }, new ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // 拉取失败重试策略
                                LeoLog.e("loadSplash", error.getMessage());
                                // Log.e("xxxxxxxxxxxxxxx", "加载闪屏失败");
                                if ("splash_fail_default_date".equals(pref.getSplashLoadFailDate())) {
                                    pref.setSplashLoadFailDate(failDate);
                                } else if (pref.getSplashLoadFailNumber() >= 0
                                        && pref.getSplashLoadFailNumber() <= 2) {
                                    // Log.e("xxxxxxxxxxxxxxx", "失败，重试！");
                                    pref.setSplashLoadFailNumber(pref.getSplashLoadFailNumber() + 1);
                                }
                                pref.setLoadSplashStrategy(pref.getSplashFailStrategy(),
                                        pref.getSplashSuccessStrategy(),
                                        pref.getSplashFailStrategy());
                                pref.setLastLoadSplashTime(System
                                        .currentTimeMillis());
                                TimerTask recheckTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        loadSplashDate();
                                    }
                                };
                                Timer timer = new Timer();
                                timer.schedule(recheckTask, pref.getSplashCurrentStrategy());
                            }
                        });
            }
        } else {
            pref.setLoadSplashStrategy(pref.getSplashFailStrategy(),
                    pref.getSplashSuccessStrategy(), pref.getSplashFailStrategy());
            TimerTask recheckTask = new TimerTask() {
                @Override
                public void run() {
                    loadSplashDate();
                }
            };
            Timer timer = new Timer();
            long delay = pref.getSplashCurrentStrategy() - (curTime - lastLoadTime);
            timer.schedule(recheckTask, delay);
        }
    }

    // 加载闪屏图
    private void getSplashImage(String url) {
        final SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");
        final AppMasterPreference pref = AppMasterPreference.getInstance(this);
        Date currentDate = new Date(System.currentTimeMillis());
        final String failDate = dateFormate.format(currentDate);
        String dir = FileOperationUtil.getSplashPath() + Constants.SPLASH_NAME;
        HttpRequestAgent.getInstance(this).loadSplashImage(url, dir, new Listener<File>() {

            @Override
            public void onResponse(File response, boolean noMidify) {
                pref.setLastLoadSplashTime(System
                        .currentTimeMillis());
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Log.e("xxxxxxxxxxxxxxx", "加载闪屏图片失败");
                AppMasterPreference.getInstance(getApplicationContext())
                        .setSaveSplashIsMemeryEnough(2);
                if ("splash_fail_default_date".equals(pref.getSplashLoadFailDate())) {
                    pref.setSplashLoadFailDate(failDate);
                } else if (pref.getSplashLoadFailNumber() >= 0
                        && pref.getSplashLoadFailNumber() <= 2) {
                    pref.setSplashLoadFailNumber(pref.getSplashLoadFailNumber() + 1);
                }
                pref.setLoadSplashStrategy(pref.getSplashFailStrategy(),
                        pref.getSplashSuccessStrategy(),
                        pref.getSplashFailStrategy());
                pref.setLastLoadSplashTime(System
                        .currentTimeMillis());
                TimerTask recheckTask = new TimerTask() {
                    @Override
                    public void run() {
                        loadSplashDate();
                    }
                };
                Timer timer = new Timer();
                timer.schedule(recheckTask, pref.getSplashCurrentStrategy());
            }
        });
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
        ContentResolver cr = getContentResolver();
        if (cr != null) {
            cr.unregisterContentObserver(mCallLogObserver);
            cr.unregisterContentObserver(mMessageObserver);
            cr.unregisterContentObserver(mContactObserver);
        }
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
    public synchronized void addActivity(Activity activity) {
        mActivityList.add(activity);
    }

    public synchronized void removeActivity(Activity activity) {
        mActivityList.remove(activity);
    }

    public synchronized void exitApplication() {
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
