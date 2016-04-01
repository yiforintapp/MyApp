
package com.leo.appmaster.bootstrap;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.text.format.Time;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.NewThemeEvent;
import com.leo.appmaster.home.ProxyActivity;
import com.leo.appmaster.schedule.ScreenRecommentJob;
import com.leo.appmaster.sdk.push.PushNotification;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

/**
 * 检查更新延时程序 1、checkNewTheme 2、checkNewAppBusiness
 *
 * @author Jasper
 */
public class CheckNewBootstrap extends Bootstrap {
    private static final String TAG = "CheckNewBootstrap";

    private static NewAppRequestListener mAppRequestListener;
    private static NewThemeRequestListener mThemeRequestListener;

    public static final int CHECKTHEME = 0;
    public static final int CHECKHOTAPP = 1;
    private static boolean isFromPush = false;

    CheckNewBootstrap() {
        super();

        mAppRequestListener = new NewAppRequestListener();
        mThemeRequestListener = new NewThemeRequestListener();
        LeoLog.d(TAG, "instance !");
    }

    @Override
    protected boolean doStrap() {
        PowerManager pm = (PowerManager) mApp.getSystemService(Context.POWER_SERVICE);
        if (pm.isScreenOn()) {
            checkProxy();
        }

        checkNewTheme();

        ScreenRecommentJob.initialize();
//        checkNewAppBusiness();
//        ISwipUpdateRequestManager.getInstance(mApp).loadIswipCheckNew();
        return true;
    }

    @Override
    public String getClassTag() {
        return TAG;
    }

    public static void checkNewTheme() {
        LeoLog.d(TAG, "checkNewTheme !");
        AppMasterApplication app = AppMasterApplication.getInstance();
        final AppMasterPreference pref = AppMasterPreference.getInstance(app);
        long curTime = System.currentTimeMillis();

        long lastCheckTime = pref.getLastCheckThemeTime();
        if ((lastCheckTime > 0
                && (Math.abs(curTime - lastCheckTime)) > pref.getThemeCurrentStrategy())
                || isFromPush) {
            LeoLog.d(TAG, "NewTheme时间符合 !");
            HttpRequestAgent.getInstance(app).checkNewTheme(mThemeRequestListener,
                    mThemeRequestListener);
        } else {
            LeoLog.d(TAG, "NewTheme时间不符合 !");
            TimerTask recheckTask = new TimerTask() {
                @Override
                public void run() {
                    checkNewTheme();
                }
            };
            Timer timer = ThreadManager.getTimer();
            if (lastCheckTime == 0) { // First time, check theme after 24 hours
                lastCheckTime = curTime;
                pref.setLastCheckThemeTime(curTime);
                pref.setThemeStrategy(AppMasterConfig.TIME_24_HOUR, AppMasterConfig.TIME_12_HOUR,
                        AppMasterConfig.TIME_2_HOUR);
            }
            long delay = pref.getThemeCurrentStrategy()
                    - (curTime - lastCheckTime);
            // long delay = 180000;
            LeoLog.d(TAG, "delay : " + delay);
            timer.schedule(recheckTask, delay);
        }
    }

    public static void checkNewAppBusiness() {
        LeoLog.d(TAG, "checkNewAppBusiness !");
        AppMasterApplication app = AppMasterApplication.getInstance();
        final AppMasterPreference pref = AppMasterPreference.getInstance(app);
        long curTime = System.currentTimeMillis();

        long lastCheckTime = pref.getLastCheckBusinessTime();
        if ((lastCheckTime > 0
                && (Math.abs(curTime - lastCheckTime)) >
                pref.getBusinessCurrentStrategy()) || isFromPush
        /* 2 * 60 * 1000 */) {
            // if (true) {
            LeoLog.d(TAG, "NewApp时间符合 !");
            HttpRequestAgent.getInstance(app).checkNewBusinessData(mAppRequestListener,
                    mAppRequestListener);
        } else {
            LeoLog.d(TAG, "NewApp时间不符合 !");
            TimerTask recheckTask = new TimerTask() {
                @Override
                public void run() {
                    checkNewAppBusiness();
                }
            };
            Timer timer = ThreadManager.getTimer();
            if (lastCheckTime == 0) { // First time, check business after 24
                // hours
                lastCheckTime = curTime;
                pref.setLastCheckBusinessTime(curTime);
                pref.setBusinessStrategy(AppMasterConfig.TIME_24_HOUR,
                        AppMasterConfig.TIME_12_HOUR, AppMasterConfig.TIME_2_HOUR);
            }
            long delay = pref.getBusinessCurrentStrategy() - (curTime -
                    lastCheckTime);
            // long delay = 180000;
            LeoLog.d(TAG, "delay : " + delay);
            timer.schedule(recheckTask, delay < 0 ? 0 : delay);
        }

    }

    private static void showNewThemeTip(String title, String content) {
        AppMasterApplication app = AppMasterApplication.getInstance();
        if (shouldShowTip(CHECKTHEME)) {
            // send new theme broadcast
            Intent intent = new Intent(Constants.ACTION_NEW_THEME);
            app.sendBroadcast(intent);

            if (Utilities.isEmpty(title)) {
                title = app.getString(R.string.find_new_theme);
            }

            if (Utilities.isEmpty(content)) {
                content = app.getString(R.string.find_new_theme_content);
            }

            // unified notification
            PushNotification mThemeNoti = new PushNotification(app);
            intent = new Intent(app, StatusBarEventService.class);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                    StatusBarEventService.EVENT_NEW_THEME);
//            mThemeNoti.showNewThemeNoti(intent, title, content);
            mThemeNoti.showNotification(intent, title, content, 0, PushNotification.NOTI_THEME);

            isFromPush = false;
            // show new theme status tip
            // Notification notif = new Notification();
            // intent = new Intent(app, StatusBarEventService.class);
            // intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
            // StatusBarEventService.EVENT_NEW_THEME);
            // PendingIntent contentIntent = PendingIntent.getService(app, 0,
            // intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // notif.icon = R.drawable.ic_launcher_notification;
            // notif.tickerText = title;
            // notif.flags = Notification.FLAG_AUTO_CANCEL;
            // notif.setLatestEventInfo(app, title, content, contentIntent);
            // NotificationUtil.setBigIcon(notif,
            // R.drawable.ic_launcher_notification_big);
            // notif.when = System.currentTimeMillis();
            // NotificationManager nm = (NotificationManager) app
            // .getSystemService(Context.NOTIFICATION_SERVICE);
            // nm.notify(0, notif);
        }
    }

    private static void showNewBusinessTip(String title, String content) {
        AppMasterApplication app = AppMasterApplication.getInstance();
        if (shouldShowTip(CHECKHOTAPP)) {
            if (Utilities.isEmpty(title)) {
                title = app.getString(R.string.new_app_tip_title);
            }

            if (Utilities.isEmpty(content)) {
                content = app.getString(R.string.new_app_tip_content);
            }

            // show red tip
            // AppMasterPreference sp_red_ti =
            // AppMasterPreference.getInstance(app);
            // sp_red_ti.setHomeFragmentRedTip(true);
            // sp_red_ti.setHotAppActivityRedTip(true);

            // unified notification
            Intent intent = null;
            PushNotification mHotAppNoti = new PushNotification(app);
            intent = new Intent(app, StatusBarEventService.class);
            intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                    StatusBarEventService.EVENT_BUSINESS_APP);
//            mHotAppNoti.showNewAppNoti(intent, title, content);
            mHotAppNoti.showNotification(intent, title, content, 0, PushNotification.NOTI_HOTAPP);

            isFromPush = false;
            // show business status tip
            // Intent intent = null;
            // Notification notif = new Notification();
            // intent = new Intent(app, StatusBarEventService.class);
            // intent.putExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
            // StatusBarEventService.EVENT_BUSINESS_APP);
            //
            // PendingIntent contentIntent = PendingIntent.getService(app, 1,
            // intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // notif.icon = R.drawable.ic_launcher_notification;
            // notif.tickerText = title;
            // notif.flags = Notification.FLAG_AUTO_CANCEL;
            // notif.setLatestEventInfo(app, title, content, contentIntent);
            // NotificationUtil.setBigIcon(notif,
            // R.drawable.ic_launcher_notification_big);
            // notif.when = System.currentTimeMillis();
            // NotificationManager nm = (NotificationManager) app
            // .getSystemService(Context.NOTIFICATION_SERVICE);
            // nm.notify(1, notif);
        }
    }

    private static boolean shouldShowTip(int type) {
        AppMasterApplication app = AppMasterApplication.getInstance();
        AppMasterPreference pref = AppMasterPreference.getInstance(app);
        long lastTime;
        if (type == CHECKTHEME) {
            lastTime = pref.getThemeLastShowTime();
        } else {
            lastTime = pref.getHotAppLastShowTime();
        }
        long nowTime = System.currentTimeMillis();
        if (lastTime > 0 && !isFromPush) {
            Time time = new Time();
            time.set(lastTime);
            int lastYear = time.year;
            int lastDay = time.yearDay;
            time.set(nowTime);
            if (lastYear == time.year && lastDay == time.yearDay) {
                return false;
            }
        }
        if (type == CHECKTHEME) {
            // pref.setLastShowTime(nowTime);
            pref.setThemeLastShowTime(nowTime);
        } else {
            pref.setHotAppLastShowTime(nowTime);
        }

        return true;
    }

    public static void checkProxy() {
        final AppMasterApplication application = AppMasterApplication.getInstance();
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                String pkgName = getTopPackage();
                if (pkgName != null && pkgName.equals(application.getPackageName())) {
                    return;
                }
                AppMasterPreference pref = AppMasterPreference.getInstance(application);
                long curTime = System.currentTimeMillis();
                long lastUBC = pref.getLastUBCTime();
                if (Math.abs(curTime - lastUBC) > AppMasterConfig.TIME_6_HOUR) {
                    pref.setLastUBCTime(curTime);
                    if (lastUBC > 0) {
                        ThreadManager.executeOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent();
                                intent.setClass(application, ProxyActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                application.startActivity(intent);
                            }
                        });
                    }
                }
            }
        });
    }

    private static String getTopPackage() {
        final AppMasterApplication application = AppMasterApplication.getInstance();
        ActivityManager am = (ActivityManager) application
                .getSystemService(Context.ACTIVITY_SERVICE);
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

    private static class NewThemeRequestListener implements Listener<JSONObject>, ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            LeoLog.d(TAG, "checkNewTheme  onErrorResponse!");
            AppMasterApplication app = AppMasterApplication.getInstance();
            AppMasterPreference pref = AppMasterPreference.getInstance(app);
            LeoLog.e("checkNewTheme", error.getMessage());
            pref.setThemeStrategy(pref.getThemeFailStrategy(),
                    pref.getThemeSuccessStrategy(), pref.getThemeFailStrategy());
            TimerTask recheckTask = new TimerTask() {
                @Override
                public void run() {
                    checkNewTheme();
                }
            };
            Timer timer = ThreadManager.getTimer();
            timer.schedule(recheckTask, pref.getThemeCurrentStrategy());
        }

        @Override
        public void onResponse(JSONObject response, boolean noMidify) {
            LeoLog.d(TAG, "checkNewTheme  onResponse!" + response.toString());
            AppMasterApplication app = AppMasterApplication.getInstance();
            AppMasterPreference pref = AppMasterPreference.getInstance(app);
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
                    pref.setThemeStrategy(successStrategy, successStrategy, failStrategy);

                    if (dataObject != null) {
                        boolean hasNewTheme = dataObject.getBoolean("need_update");
                        String serialNumber = dataObject.getString("update_flag");

                        if (!hasNewTheme) {
                            LeoLog.d(TAG, "checkNewTheme  !!hasNewTheme!");
                            pref.setLocalThemeSerialNumber(serialNumber);
                        }
                        pref.setOnlineThemeSerialNumber(serialNumber);

                        if (hasNewTheme) {
//                            LeoPreference.getInstance().putBoolean(Constants.IS_CLICK_LOCK_TAB, false);
                            LeoLog.d(TAG, "checkNewTheme  hasNewTheme!");
                            String title = null;
                            String content = null;
                            if (noticeObject != null) {
                                title = noticeObject.getString("title");
                                content = noticeObject.getString("content");
                            }

                            LeoEventBus.getDefaultBus().postSticky(
                                    new NewThemeEvent(EventId.EVENT_NEW_THEME, "new theme", true));

                            showNewThemeTip(title, content);
                        }
                        pref.setLastCheckThemeTime(System.currentTimeMillis());
                    }

                    TimerTask recheckTask = new TimerTask() {
                        @Override
                        public void run() {
                            checkNewTheme();
                        }
                    };
                    Timer timer = ThreadManager.getTimer();
                    timer.schedule(recheckTask, pref.getThemeCurrentStrategy());
                } catch (JSONException e) {
                    e.printStackTrace();
                    LeoLog.e("checkNewTheme", e.getMessage());
                }
            }
        }

    }

    private static class NewAppRequestListener implements Listener<JSONObject>, ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            LeoLog.d(TAG, "checkNewAppBusiness onErrorResponse!");
            AppMasterApplication app = AppMasterApplication.getInstance();
            AppMasterPreference pref = AppMasterPreference.getInstance(app);
            pref.setThemeStrategy(pref.getBusinessFailStrategy(),
                    pref.getBusinessSuccessStrategy(), pref.getBusinessFailStrategy());
            TimerTask recheckTask = new TimerTask() {
                @Override
                public void run() {
                    checkNewAppBusiness();
                }
            };
            Timer timer = ThreadManager.getTimer();
            timer.schedule(recheckTask, pref.getBusinessCurrentStrategy());
        }

        @Override
        public void onResponse(JSONObject response, boolean noMidify) {
            LeoLog.d(TAG, "checkNewAppBusiness onResponse! " + response.toString());
            AppMasterApplication app = AppMasterApplication.getInstance();
            AppMasterPreference pref = AppMasterPreference.getInstance(app);
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
                    pref.setBusinessStrategy(successStrategy, successStrategy, failStrategy);
                    if (jsonObject != null) {
                        LeoLog.d(TAG, "jsonObject != null , ready to show tips");
                        boolean hasNewBusinessData = jsonObject.getBoolean("need_update");
                        String serialNumber = jsonObject.getString("update_flag");

                        if (!hasNewBusinessData) {
                            pref.setLocalBusinessSerialNumber(serialNumber);
                        }
                        pref.setOnlineBusinessSerialNumber(serialNumber);

                        if (hasNewBusinessData) {
                            LeoLog.d(TAG, "hasNewBusinessData");
                            String title = null;
                            String content = null;
                            if (noticeObject != null) {
                                title = noticeObject.getString("title");
                                content = noticeObject.getString("content");
                            }
                            showNewBusinessTip(title, content);
                            pref.setHomeBusinessTipClick(false);
                        }
                        pref.setLastCheckBusinessTime(System.currentTimeMillis());
                    }

                    TimerTask recheckTask = new TimerTask() {
                        @Override
                        public void run() {
                            checkNewAppBusiness();
                        }
                    };
                    Timer timer = ThreadManager.getTimer();
                    timer.schedule(recheckTask, pref.getBusinessCurrentStrategy());

                } catch (JSONException e) {
                    LeoLog.e("checkNewAppBusiness", e.getMessage());
                }
            }
        }
    }

    public static void setFromPush(boolean b) {
        isFromPush = b;
    }

}
