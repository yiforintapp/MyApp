
package com.leo.appmaster;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.receiver.LockReceiver;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.appmanage.business.AppBusinessManager;
import com.leo.appmaster.backup.AppBackupRestoreManager;
import com.leo.appmaster.cleanmemory.HomeBoostActivity;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.NewThemeEvent;
import com.leo.appmaster.home.ProxyActivity;
import com.leo.appmaster.home.SplashActivity;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.http.HttpRequestAgent.RequestListener;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.privacycontact.MessagePrivacyReceiver;
import com.leo.appmaster.privacycontact.PrivacyContactManager;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.privacycontact.PrivacyMessageContentObserver;
import com.leo.appmaster.privacycontact.PrivacyTrickUtil;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.QuickGestureProxyActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NotificationUtil;
import com.leo.appmaster.utils.Utilities;
import com.leo.imageloader.ImageLoader;
import com.leo.imageloader.ImageLoaderConfiguration;

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
    private static List<WeakReference<Activity>> mActivityList;

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
    public static volatile boolean mSplashFlag;
    public static volatile boolean mIsEmptyForSplashUrl;
    public static volatile int mSplashDelayTime;
    // public ExecutorService cachedThreadPool;
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
        initDensity(this);
        mActivityList = new ArrayList<WeakReference<Activity>>();
        mInstance = this;
        mExecutorService = Executors.newScheduledThreadPool(3);
        // cachedThreadPool = Executors.newCachedThreadPool();
        mHandler = new Handler();
        mAppsEngine = AppLoadEngine.getInstance(this);
        mBackupManager = new AppBackupRestoreManager(this);
        initImageLoader();
        sharedPreferences = getSharedPreferences("lockerTheme",
                Context.MODE_WORLD_WRITEABLE);
        usedThemePackage = sharedPreferences.getString("packageName",
                Constants.DEFAULT_THEME);

        registerPackageChangedBroadcast();
        SDKWrapper.iniSDK(this);
        AppBusinessManager.getInstance(mInstance).init();
        startInitTask(this);

        // init lock manager
        LockManager.getInstatnce().init();
        // loadSplashDate();
        mExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                checkNew();
            }
        }, 10, TimeUnit.SECONDS);
        postInAppThreadPool(new Runnable() {

            @Override
            public void run() {
                // 获取闪屏数据
                loadSplashDate();
            }
        });
        if (AppMasterPreference.getInstance(getApplicationContext()).getIsFirstInstallApp()) {
            SplashActivity.deleteImage();
            AppMasterPreference.getInstance(getApplicationContext()).setIsFirstInstallApp(false);
        }
        // For android L and above, daemon service is not work, so disable it
        if (PhoneInfo.getAndroidVersion() < 20) {
            restartApplocker(PhoneInfo.getAndroidVersion(), getUserSerial());
        }
        registerReceiveMessageCallIntercept();
        PrivacyHelper.getInstance(this).computePrivacyLevel(PrivacyHelper.VARABLE_ALL);
        QuickGestureManager.getInstance(getApplicationContext()).screenSpace = AppMasterPreference
                .getInstance(getApplicationContext()).getRootViewAndWindowHeighSpace();
        registerLanguageChangeReceiver();
        // Log.e(Constants.RUN_TAG,
        // "悬浮窗权限："+BuildProperties.isFloatWindowOpAllowed(getApplicationContext()));
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

    public ExecutorService getExecutorService() {
        // return cachedThreadPool;
        return mExecutorService;
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
        // 升级用户更换快捷手势
        // if (AppMasterPreference.getInstance(ctx)
        // .getFristSlidingTip()) {
        // //在首次引导滑动没有执行之前不会执行此处
        // checkRemoveQuickGestureIcon(ctx);
        // }
        checkUpdateFinish();
        postInAppThreadPool(new Runnable() {
            @Override
            public void run() {
                mAppsEngine.preloadAllBaseInfo();
                // 初始化快捷手势数据
                if (AppMasterPreference.getInstance(getApplicationContext())
                        .getSwitchOpenQuickGesture()) {
                    QuickGestureManager.getInstance(AppMasterApplication.this).init();
                }
                /* checkUpdateFinish(); */
                quickGestureTipInit();
                mBackupManager.getBackupList();
                PrivacyContactManager.getInstance(ctx).getPrivateContacts();
                // GP check
                boolean isAppInstalled;

                try {
                    isAppInstalled = AppUtil.appInstalled(AppMasterApplication.this,
                            Constants.GP_PACKAGE);
                } catch (Exception e) {
                    isAppInstalled = false;
                }

                if (!isAppInstalled) {
                    SDKWrapper.addEvent(AppMasterApplication.this, SDKWrapper.P1, "gp_check",
                            "nogp");
                }
            }

        });
        initSplashData();
        // TEST
//         setSplashData();
    }

    private void setSplashData() {
        mSplashFlag = true;
        mIsEmptyForSplashUrl = false;
        mSplashDelayTime = 5000;
         AppMasterPreference.getInstance(getApplicationContext()).setSplashSkipMode(
         Constants.SPLASH_SKIP_PG_CLIENT);
         AppMasterPreference.getInstance(getApplicationContext()).setSplashSkipToClient("fb://page/1709302419294051");
//        AppMasterPreference.getInstance(getApplicationContext()).setSplashSkipUrl(
//                "fb://page/1709302419294051");
        SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");
        try {
            AppMasterPreference.getInstance(getApplicationContext()).setSplashStartShowTime(
                    dateFormate.parse("2015-08-05").getTime());
            AppMasterPreference.getInstance(getApplicationContext()).setSplashEndShowTime(
                    dateFormate.parse("2015-08-20").getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void quickGestureTipInit() {
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        if (!pref.getLastVersion().equals(PhoneInfo.getVersionCode(this))) {
            pref.setNewUserUnlockCount(0);
        }
    }

    private void checkRemoveQuickGestureIcon(final Context ctx) {
        AppMasterPreference pref = AppMasterPreference.getInstance(this);
        String lastVercode = pref.getLastVersion();
        String versionCode = PhoneInfo.getVersionCode(this);
        // if (lastVercode != null) {
        // Log.e(FloatWindowHelper.RUN_TAG, "记录的版本号："+lastVercode);
        // }
        // first install
        if (!TextUtils.isEmpty(lastVercode)
                && (Integer.parseInt(lastVercode) < Integer.parseInt(versionCode))) {
            // update
            removeQuickGestureIcon();
            // Log.e(FloatWindowHelper.RUN_TAG, "更新用户");
        } else {
            // Log.e(FloatWindowHelper.RUN_TAG, "记录的版本号为空");
        }
    }

    private void removeQuickGestureIcon() {
        if (!AppMasterPreference.getInstance(getApplicationContext())
                .getRemoveQuickGestureIcon()) {
            // remove unlock all shortcut
            Intent quickGestureShortIntent = new Intent(getApplicationContext(),
                    QuickGestureProxyActivity.class);
            quickGestureShortIntent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                    StatusBarEventService.EVENT_BUSINESS_QUICK_GUESTURE);
            quickGestureShortIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            quickGestureShortIntent.setAction(Intent.ACTION_VIEW);
            Intent quickGestureShortcut = new Intent(
                    "com.android.launcher.action.UNINSTALL_SHORTCUT");
            ShortcutIconResource quickGestureIconRes = Intent.ShortcutIconResource.fromContext(
                    getApplicationContext(), R.drawable.gesture_desktopo_icon);
            quickGestureShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                    getString(R.string.pg_appmanager_quick_gesture_name));
            quickGestureShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    quickGestureIconRes);
            quickGestureShortcut
                    .putExtra(Intent.EXTRA_SHORTCUT_INTENT, quickGestureShortIntent);
            quickGestureShortcut.putExtra("duplicate", false);
            quickGestureShortcut.putExtra("from_shortcut", true);
            getApplicationContext().sendBroadcast(quickGestureShortcut);

            AppMasterPreference.getInstance(getApplicationContext())
                    .setRemoveQuickGestureIcon(true);
            SharedPreferences prefernece = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            prefernece.edit().putBoolean("shortcut_quickGesture", false).commit();
            QuickGestureManager.getInstance(getApplicationContext()).createShortCut();
        }
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
        LeoLog.i("value", "lastVercode=" + lastVercode);
        LeoLog.i("value", "versionCode=" + versionCode);
        if (TextUtils.isEmpty(lastVercode)) {
            // first install
            if (Integer.parseInt(versionCode) == 34) {
                // remove unlock-all shortcut v2.1
                tryRemoveUnlockAllShortcut(this);
            } else if (Integer.parseInt(versionCode) == 41) {
                installBoostShortcut();
            }
            pref.setIsUpdateQuickGestureUser(false);
        } else {
            if (Integer.parseInt(lastVercode) < Integer.parseInt(versionCode)) {
                // hit update
                if (Integer.parseInt(versionCode) == 34) {
                    // remove unlock-all shortcut v2.1
                    tryRemoveUnlockAllShortcut(this);
                } else if (Integer.parseInt(versionCode) == 41) {
                    installBoostShortcut();
                }

                pref.setGuidePageFirstUse(true);
                pref.setIsUpdateQuickGestureUser(true);
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
        if (pref.getCurrentAppVersionCode() != Integer.valueOf(PhoneInfo.getVersionCode(this))) {
            pref.setNewUserUnlockCount(0);
        }
    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .threadPoolSize(Constants.MAX_THREAD_POOL_SIZE)
                .threadPriority(Thread.NORM_PRIORITY)
                .memoryCacheSizePercentage(8)
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
            timer.schedule(recheckTask, delay < 0 ? 0 : delay);
        }

    }

    public void checkUBC() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                String pkgName = getTopPackage();
                if (pkgName != null && pkgName.equals(getApplicationContext().getPackageName())) {
                    return;
                }
                AppMasterPreference pref = AppMasterPreference.getInstance(getApplicationContext());
                long curTime = System.currentTimeMillis();
                long lastUBC = pref.getLastUBCTime();
                if (Math.abs(curTime - lastUBC) > AppMasterConfig.TIME_12_HOUR) {
                    pref.setLastUBCTime(curTime);
                    if (lastUBC > 0) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), ProxyActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
        });
    }

    public void checkNewTheme() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm.isScreenOn()) {
            checkUBC();
        }
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
        // Log.e(Constants.RUN_TAG, "开始拉取");
        if (lastLoadTime == 0
                || (curTime - pref.getLastLoadSplashTime()) >
                pref.getSplashCurrentStrategy()) {
            if (Constants.SPLASH_REQUEST_FAIL_DATE.equals(pref.getSplashLoadFailDate())
                    || pref.getSplashLoadFailNumber() < 0
                    || !failDate.equals(pref.getSplashLoadFailDate())
                    || (failDate.equals(pref.getSplashLoadFailDate()) && pref
                            .getSplashLoadFailNumber() <= 2)) {
                /* 日期变化数据初始化 */
                if (!failDate.equals(pref.getSplashLoadFailDate())) {
                    if (pref.getSplashLoadFailNumber() != 0) {
                        pref.setSplashLoadFailNumber(0);
                    }
                    if (!Constants.SPLASH_REQUEST_FAIL_DATE
                            .equals(pref.getSplashLoadFailDate())) {
                        pref.setSplashLoadFailDate(Constants.SPLASH_REQUEST_FAIL_DATE);
                    }
                }
                SplashRequestListener splashListener = new SplashRequestListener(mInstance, pref,
                        dateFormate);
                HttpRequestAgent.getInstance(this).loadSplashDate(splashListener, splashListener);
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
            if (delay < 0) {
                delay = AppMasterConfig.TIME_12_HOUR;
            }
            // 调试
            // delay=2000;

            timer.schedule(recheckTask, delay);
        }
    }

    /* 闪屏网络请求监听 */
    private class SplashRequestListener extends RequestListener<AppMasterApplication> {
        AppMasterPreference pref = null;
        SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");
        long curTime = System.currentTimeMillis();
        Date currentDate = new Date(curTime);
        String failDate = dateFormate.format(currentDate);

        public SplashRequestListener(AppMasterApplication outerContext,
                AppMasterPreference preference, SimpleDateFormat formate) {
            super(outerContext);
            pref = preference;
            // dateFormate = formate;
        }

        @Override
        public void onResponse(JSONObject response, boolean noMidify) {
            if (response != null) {
                // Log.e(Constants.RUN_TAG, "拉取成功:");
                try {
                    /* 起始时间 */
                    String startDate = response.getString(Constants.REQUEST_SPLASH_SHOW_STARTDATE);
                    if (startDate != null) {
                        Log.e(Constants.RUN_TAG, "闪屏起始时间：" + startDate);
                    }
                    /* 图片url */
                    String imageUrl = response.getString(Constants.REQUEST_SPLASH_IMAGEURL);
                    if (imageUrl != null) {
                        Log.e(Constants.RUN_TAG, "闪屏图片链接：" + imageUrl);
                    }
                    /* 结束时间 */
                    String endDate = response.getString(Constants.REQUEST_SPLASH_SHOW_ENDDATE);
                    if (endDate != null) {
                        Log.e(Constants.RUN_TAG, "闪屏结束时间：" + endDate);
                    }
                    /* 闪屏延迟时间 */
                    String splashDelayTime = response
                            .getString(Constants.REQUEST_SPLASH_DELAY_TIME);
                    if (splashDelayTime != null) {
                        Log.e(Constants.RUN_TAG, "闪屏延迟时间：" + splashDelayTime);
                    }
                    /* 跳转链接 */
                    String splashSkipUrl = response.getString(Constants.REQUEST_SPLASH_SKIP_URL);
                    if (splashSkipUrl != null) {
                        Log.e(Constants.RUN_TAG, "闪屏跳转链接：" + splashSkipUrl);
                    }
                    /* 跳转方式 */
                    String splashSkipMode = response.getString(Constants.REQUEST_SPLASH_SKIP_FLAG);
                    if (splashSkipMode != null) {
                        Log.e(Constants.RUN_TAG, "闪屏跳转方式：" + splashSkipMode);
                    }
                    /* 跳转客户端的链接 */
                    String splashSkipToClient = response
                            .getString(Constants.SPLASH_SKIP_TO_CLIENT_URL);
                    if (splashSkipToClient != null) {
                        Log.e(Constants.RUN_TAG, "闪屏跳转客户端的链接：" + splashSkipToClient);
                    }
                    /**
                     * 闪屏Button文案
                     * 
                     * @该字段目前未使用所以没有做保存只是打Log供测试测试用后续有使用的对该字段再作处理
                     */
                    String spalshBtText = response.getString(Constants.SPLASH_BUTTON_TEXT);
                    if (spalshBtText != null) {
                        Log.e(Constants.RUN_TAG, "闪屏Button的文案：" + spalshBtText);
                    }
                    StringBuilder stringBuilder = constructionSplashFlag(startDate, imageUrl,
                            endDate, splashDelayTime, splashSkipUrl, splashSkipMode,
                            splashSkipToClient);
                    String splashUriFlag = stringBuilder.toString();
                    String prefStringUri = pref.getSplashUriFlag();
                    int prefInt = pref.getSaveSplashIsMemeryEnough();
                    if (!prefStringUri.equals(splashUriFlag) || prefInt != -1) {
                        if (!prefStringUri.equals(splashUriFlag)) {
                            if (!Utilities.isEmpty(splashUriFlag)) {
                                pref.setSplashUriFlag(splashUriFlag);
                                /* 后台拉取成功更新缓存数据 */
                                // mSplashFlag = true;
                                /* 初始化显示时间段 */
                                if (pref.getSplashStartShowTime() != -1) {
                                    pref.setSplashStartShowTime(-1);
                                }
                                if (pref.getSplashEndShowTime() != -1) {
                                    pref.setSplashEndShowTime(-1);
                                }
                                clearSpSplashFlagDate();
                            }
                            // Log.e(Constants.RUN_TAG, "闪屏发起网络请求");
                        }
                        SplashActivity.deleteImage();
                        if (prefInt != -1) {
                            pref.setSaveSplashIsMemeryEnough(-1);
                        }
                        if (!Utilities.isEmpty(endDate)) {
                            long end = 0;
                            try {
                                end = dateFormate.parse(endDate).getTime();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            pref.setSplashEndShowTime(end);
                        }
                        if (!Utilities.isEmpty(startDate)) {
                            long start = 0;
                            try {
                                start = dateFormate.parse(startDate).getTime();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            pref.setSplashStartShowTime(start);
                        }
                        if (!Utilities.isEmpty(imageUrl)) {
                            getSplashImage(imageUrl);
                        }
                        /* 闪屏跳转链接 */
                        if (!Utilities.isEmpty(splashSkipUrl)) {
                            pref.setSplashSkipUrl(splashSkipUrl);
                            /* 后台拉取成功更新缓存数据 */
                            mIsEmptyForSplashUrl = false;
                        }
                        /* 闪屏跳转方式标志 */
                        if (!Utilities.isEmpty(splashSkipMode)) {
                            pref.setSplashSkipMode(splashSkipMode);
                        }
                        /* 闪屏显示时间 */
                        if (!Utilities.isEmpty(splashDelayTime)) {
                            int delayTime = Integer.valueOf(splashDelayTime);
                            pref.setSplashDelayTime(delayTime);
                            /* 后台拉取成功更新缓存数据 */
                            mSplashDelayTime = delayTime;
                        }
                        /* 指定需要跳转的客户端的链接 */
                        if (!Utilities.isEmpty(splashSkipToClient)) {
                            pref.setSplashSkipToClient(splashSkipToClient);
                        }
                    }
                    long successStrategy = pref.getThemeSuccessStrategy();
                    long failStrategy = pref.getThemeFailStrategy();
                    pref.setThemeStrategy(successStrategy, successStrategy,
                            failStrategy);
                    pref.setLoadSplashStrategy(successStrategy,
                            successStrategy, failStrategy);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            /* 拉取成功数据初始化 */
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
            long delay = pref.getSplashCurrentStrategy();

            if (delay < 0) {
                delay = AppMasterConfig.TIME_12_HOUR;
            }

            // 调试
            // delay=2000;

            timer.schedule(recheckTask, delay);
        }

        private StringBuilder constructionSplashFlag(String startDate, String imageUrl,
                String endDate, String splashDelayTime, String splashSkipUrl,
                String splashSkipFlag, String splashSkipToClient) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(imageUrl);
            stringBuilder.append(startDate);
            stringBuilder.append(endDate);
            stringBuilder.append(splashDelayTime);
            stringBuilder.append(splashSkipUrl);
            stringBuilder.append(splashSkipFlag);
            stringBuilder.append(splashSkipToClient);
            return stringBuilder;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            LeoLog.e("loadSplash", error.getMessage());
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
                    Log.e(Constants.RUN_TAG, "失败再次拉取");
                    loadSplashDate();
                }
            };
            Timer timer = new Timer();
            long delay = pref.getSplashCurrentStrategy();

            if (delay < 0) {
                delay = AppMasterConfig.TIME_12_HOUR;
            }

            // 调试
            // delay=1000;

            timer.schedule(recheckTask, delay);
        }

    }

    /* 对后台配置的过期闪屏数据初始化 */
    private void clearSpSplashFlagDate() {
        AppMasterPreference.getInstance(getApplicationContext()).setSplashUriFlag(
                Constants.SPLASH_FLAG);
        AppMasterPreference.getInstance(this).setSplashDelayTime(Constants.SPLASH_DELAY_TIME);
        mSplashDelayTime = Constants.SPLASH_DELAY_TIME;
        AppMasterPreference.getInstance(this).setSplashSkipUrl(null);
        mIsEmptyForSplashUrl = true;
    }

    /* 闪屏跳转连接是否为空：true-链接为空，false-链接不为空 */
    private boolean isEmptySplashUrl() {
        return Utilities.isEmpty(AppMasterPreference.getInstance(this)
                .getSplashSkipUrl()) && Utilities.isEmpty(AppMasterPreference.getInstance(this)
                .getSplashSkipToClient());
    }

    /* 后台是否配置了新的闪屏:true-闪屏有更新，false-没有更新 */
    private boolean splashIsChanageFlag() {
        return !AppMasterPreference.getInstance(getApplicationContext()).getSplashUriFlag()
                .equals(Constants.SPLASH_FLAG);
    }

    /* 初始化闪屏所需数据缓存 */
    private void initSplashData() {
        /* 闪屏是否发生变化 */
        // mSplashFlag = splashIsChanageFlag();
        /* 闪屏跳转连接 */
        mIsEmptyForSplashUrl = isEmptySplashUrl();
        /* 闪屏延时时间 */
        mSplashDelayTime = AppMasterPreference.getInstance(getApplicationContext())
                .getSplashDelayTime();
    }

    /* 加载闪屏图 */
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
        ImageLoader.getInstance().clearMemoryCache();
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

    // // init ImageLoader
    // public static void initImageLoader(Context context) {
    // ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
    // context).threadPriority(Thread.NORM_PRIORITY)
    // .memoryCacheSizePercentage(3)
    // .diskCacheFileNameGenerator(new Md5FileNameGenerator())
    // .diskCacheSize(100 * 1024 * 1024)
    // .tasksProcessingOrder(QueueProcessingType.FIFO)
    // .writeDebugLogs().build();
    // ImageLoader.getInstance().init(config);
    // }

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
        mActivityList.remove(activity);
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
        // for (Activity activity : mActivityList) {
        // activity.finish();
        // }
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

    private String getTopPackage() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > 19) { // Android L and above
            List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
            for (RunningAppProcessInfo pi : list) {
                if ((pi.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND || pi.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE)
                        /*
                         * Foreground or Visible
                         */
                        && pi.importanceReasonCode == RunningAppProcessInfo.REASON_UNKNOWN
                        /*
                         * Filter provider and service
                         */
                        && (0x4 & pi.flags) > 0
                        && pi.processState == ActivityManager.PROCESS_STATE_TOP) {
                    /*
                     * Must have activities and one activity is on the top
                     */
                    String pkgList[] = pi.pkgList;
                    if (pkgList != null && pkgList.length > 0) {
                        String pkgName = pkgList[0];
                        if (TaskDetectService.SYSTEMUI_PKG.equals(pkgName)) {
                            continue;
                        }
                        return pkgName;
                    }
                }
            }
        } else {
            List<RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (tasks != null && tasks.size() > 0) {
                RunningTaskInfo topTaskInfo = tasks.get(0);
                if (topTaskInfo.topActivity != null) {
                    return topTaskInfo.topActivity.getPackageName();
                }
            }
        }
        return null;
    }

    private void installBoostShortcut() {
        Intent shortcutIntent = new Intent(this, HomeBoostActivity.class);
        ShortcutIconResource iconRes = Intent.ShortcutIconResource
                .fromContext(this, R.drawable.booster_icon);
        Intent shortcut = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.accelerate));
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        shortcut.putExtra("duplicate", false);
        shortcut.putExtra("from_shortcut", true);
        sendBroadcast(shortcut);
    }

    /* 本地语言改变监听 */
    private void registerLanguageChangeReceiver() {
        BroadcastReceiver receiv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {
                    // Log.e(Constants.RUN_TAG, "语言变化");
                    // 初始化快捷手势数据
                    if (AppMasterPreference.getInstance(getApplicationContext())
                            .getSwitchOpenQuickGesture()) {
                        QuickGestureManager.getInstance(AppMasterApplication.this).unInit();
                        QuickGestureManager.getInstance(AppMasterApplication.this).init();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        registerReceiver(receiv, filter);
    }
}
