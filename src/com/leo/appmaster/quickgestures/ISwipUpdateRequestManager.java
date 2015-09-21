
package com.leo.appmaster.quickgestures;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.RemoteViews;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.PhoneInfo;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.service.StatusBarEventService;
import com.leo.appmaster.cleanmemory.HomeBoostActivity;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.home.ProtocolActivity;
import com.leo.appmaster.http.HttpRequestAgent;
import com.leo.appmaster.http.HttpRequestAgent.RequestListener;
import com.leo.appmaster.privacycontact.PrivacyContactActivity;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.quickgestures.receiver.IswipeNetworkStateListener;
import com.leo.appmaster.quickgestures.receiver.IswipePackageChangedListener;
import com.leo.appmaster.quickgestures.ui.IswipUpdateTipDialog;
import com.leo.appmaster.quickgestures.ui.QuickGestureActivity;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NotificationUtil;
import com.leo.appmaster.utils.Utilities;
import com.leo.appmater.globalbroadcast.LeoGlobalBroadcast;
import com.leo.appmater.globalbroadcast.PackageChangedListener;

public class ISwipUpdateRequestManager {
    /* 是否升级 */
    private static final String ISWIP_CHECKNEW = "a";
    private static final String ISWIP_TIP_FREQUENCY = "b";// 提示频率
    private static final String ISWIP_TIP_NUMBER = "c";// 提示次数
    private static final String ISWIP_GP_DOWN_URL = "d";// gp下载地址
    private static final String ISWIP_BROWSER_DOWN_URL = "e";// 浏览器下载地址
    private static final String ISWIP_DOWN_TYPE = "f";// 下载方式
    public static final int NOTIFICATION_CHECKNEW_ISWIP = 129;
    public static final int LOAD_DATA_FAIL_NUMBER = 3;
    public static final String ISWIP_NOTIFICATION_TO_PG_HOME = "iswip_to_home";
    public static final String kEY_PG_TO_ISWIPE = "pg_to_iswipe";// key
    public static final String VALUE_ISWIPE_FIRST_TIP = "pg_iswipe_open";// value：使用过快捷手势，并且快捷手势现在打开的老用户
    public static final String VALUE_ISWIPE_FIRST_TIP_CLOSE = "pg_iswipe_close";// value：使用过快捷手势，但是快捷手势现在关闭状态的老用户
    private static ISwipUpdateRequestManager mInstance;
    private Context mContext;
    private SimpleDateFormat mDateFormate;
    /* 是否为push吊起加载 */
    private volatile boolean mIsPushLoadIswipe;
    private boolean mIswipeUpdateTip;
    private boolean mNetworkState;
    private boolean mNoNetworkShowFlag;
    private IswipUpdateTipDialog mIswipDialog;
    private static final String TAG = "ISwipUpdateRequestManager";
    private static boolean DBG = true;
    private IswipeNetworkStateListener mIswipeNetworkListener;
    private IswipePackageChangedListener mIswipePackageListener;

    public static enum IswipeNotificationType {
        NOTIFICATION, DIALOG
    }

    private ISwipUpdateRequestManager(Context context) {
        mContext = context;
        mDateFormate = new SimpleDateFormat("yyyy-MM-dd");
        mIswipeNetworkListener = new IswipeNetworkStateListener();
        mIswipePackageListener = new IswipePackageChangedListener();
        LeoGlobalBroadcast.registerBroadcastListener(mIswipeNetworkListener);
        LeoGlobalBroadcast.registerBroadcastListener(mIswipePackageListener);
    }

    public static synchronized ISwipUpdateRequestManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ISwipUpdateRequestManager(context);
        }
        return mInstance;
    }

    public void setNoNetworkShow(boolean flag) {
        mNoNetworkShowFlag = flag;
    }

    public boolean getNoNetworkShow() {
        return mNoNetworkShowFlag;
    }

    public void setIswipeUpdateTip(boolean flag) {
        mIswipeUpdateTip = flag;
    }

    public boolean getIswipeUpdateTip() {
        return mIswipeUpdateTip;
    }

    public void setPushLoad(boolean flag) {
        mIsPushLoadIswipe = flag;
    }

    public void setNetworkStatus(boolean flag) {
        mNetworkState = flag;
    }

    public boolean getNetworkStatus() {
        return mNetworkState;
    }

    public void onDestroyIswipeNetworkStateListener() {
        LeoGlobalBroadcast.unregisterBroadcastListener(mIswipeNetworkListener);
    }

    public void onDestroyIswipePackageListener() {
        LeoGlobalBroadcast.registerBroadcastListener(mIswipePackageListener);
    }

    /* 加载Iswipe更新数据 */
    public void loadIswipCheckNew() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                requestISwipeUpdateDate();
            }
        });
    }

    private void requestISwipeUpdateDate() {
        /* 当前时间 */
        long currentTime = System.currentTimeMillis();
        /* 上次加载时间,-1:首次加载 */
        long lastLoadTime = AppMasterPreference.getInstance(mContext)
                .getIswipUpateLastLoadingTime();
        /* 当前加载策略,-1:首次加载的策略 */
        long currentStrategy = AppMasterPreference.getInstance(mContext)
                .getIswipUpdateLoadingStrategy();
        /* 失败的日期 */
        String fialDate = AppMasterPreference.getInstance(mContext).getIswipeLoadFailDate();
        /* 失败的次数,-1:默认次数 */
        int failNumber = AppMasterPreference.getInstance(mContext).getIswipUpdateLoadingNumber();
        /* 当前日期 */
        String currentDate = mDateFormate.format(new Date(currentTime));
        // LeoLog.e(TAG, "当前拉取策略：" + currentStrategy);
        if (currentStrategy < 0/* 首次拉取数据 */
                || isLoadIswipe(currentTime, lastLoadTime, currentStrategy, fialDate, failNumber,
                        currentDate)/* 根据策略拉取数据 */
                || mIsPushLoadIswipe/* 是否为push拉取数据 */) {
            if ((fialDate != null && !currentDate.equals(fialDate))/* 每天的日期改变 */) {
                AppMasterPreference.getInstance(mContext).setIswipUpdateLoadingNumber(-1);
                AppMasterPreference.getInstance(mContext).setIswipeLoadFailDate(null);
            }
            if (mIsPushLoadIswipe) {
                /* 恢复定时器已经通知的次数默认值 */
                AppMasterPreference.getInstance(mContext).setIswipeAlarmNotifiNumber(1);
                /* 恢复上次iwipe更新提示时间默认值 */
                AppMasterPreference.getInstance(mContext).setIswipeUpdateTipTime(-1);
                AppMasterPreference.getInstance(mContext).setIswipUpdateLoadingNumber(-1);
                AppMasterPreference.getInstance(mContext).setIswipeLoadFailDate(null);
            }

            LeoLog.e(TAG, "--------------准备去拉取iSwipe数据----------------");
            CheckNewIswipRequestListener checkNewIswipListener = new CheckNewIswipRequestListener(
                    ISwipUpdateRequestManager.getInstance(mContext));
            HttpRequestAgent.getInstance(AppMasterApplication.getInstance()).loadISwipCheckNew(
                    checkNewIswipListener,
                    checkNewIswipListener);
        } else {
            LeoLog.e(TAG, "--------------还未满足拉取的条件，请稍等");
        }
    }

    private boolean isLoadIswipe(long currentTime, long lastLoadTime, long currentStrategy,
            String fialDate, int failNumber, String currentDate) {
        /* 当前策略是否大于0 */
        boolean isDefaultCurrentStrategy = currentStrategy > 0;
        /* 当前时间减去上次拉取时间是否达到当前策略的拉取时间 */
        boolean achieveCurrentStrategy = (currentTime - lastLoadTime) >= currentStrategy;
        /* 在当前拉取失败的次数大于0的情况下,当前拉取次数是否小于指定的失败次数 */
        boolean isAchieveFailNumber = failNumber > 0 && failNumber < LOAD_DATA_FAIL_NUMBER;
        /* 日期是否改变，用于解决当天失败3次不再拉取，到第二天日期改变了仍然可以拉取 */
        boolean changeFialDate = (fialDate != null) && (!currentDate.equals(fialDate));
        /* 拉取的失败次数是否大于指定的次数3 */
        boolean achieveFailNumber = failNumber >= LOAD_DATA_FAIL_NUMBER;
        /* 在失败次数达到的情况下，查看日期是否改变，如果改变可以进入拉取 */
        boolean isDateChange = achieveFailNumber && changeFialDate;
        /* 是否为默认失败次数0,如果为0则说明没有失败过，下次到拉取时间可以可以进入 */
        boolean defaultFailNumber = failNumber <= 0;
        /*
         * 对于拉取失败的进入条件： 1.失败次数为0; 2.拉取失败了但是当天的失败次数为达到指定值3;
         * 3.失败次数大于3但是日期改变了已经不是之前的当天
         */
        boolean failRequst = defaultFailNumber || (isAchieveFailNumber || isDateChange);
        /* 不是首次进入拉取swipe更新时要满足：当前策略不为默认值，达到拉取时间，对于拉取失败的进入调节满足 */
        boolean isRequestIswipeData = isDefaultCurrentStrategy && achieveCurrentStrategy
                && failRequst;
        return isRequestIswipeData;
        // return currentStrategy > 0
        // && (currentTime - lastLoadTime) >= currentStrategy
        // && (failNumber <= 0
        // || ((failNumber > 0 && failNumber < LOAD_DATA_FAIL_NUMBER) ||
        // (failNumber >= LOAD_DATA_FAIL_NUMBER
        // && fialDate != null && !currentDate
        // .equals(fialDate))));
    }

    private static class CheckNewIswipRequestListener extends
            RequestListener<ISwipUpdateRequestManager> {
        private ISwipUpdateRequestManager arm;
        private AppMasterApplication context;

        public CheckNewIswipRequestListener(ISwipUpdateRequestManager outerContext) {
            super(outerContext);
            arm = getOuterContext();
            context = AppMasterApplication.getInstance();
        }

        @Override
        public void onResponse(JSONObject response, boolean noMidify) {
            /* noMidify， true：缓存，false：后台数据更改重新拉取 */
            if (response != null
                    && (!noMidify || arm.mIsPushLoadIswipe)) {
                /* 记录加载成功的版本 */
                saveLastLoadIswipeVersion();
                /* 后台数据修改，恢复之前保存定时提示记录为默认值 */
                if (!noMidify) {
                    /* 恢复定时器已经通知的次数默认值 */
                    AppMasterPreference.getInstance(context).setIswipeAlarmNotifiNumber(1);
                    /* 保存本次拉取成功时间为本次通知时间 */
                    AppMasterPreference.getInstance(context).setIswipeUpdateTipTime(
                            System.currentTimeMillis());
                }
                try {
                    /* 是否升级 */
                    int checkUpdate = response.getInt(ISWIP_CHECKNEW);
                    /* 提示频率 */
                    int frequency = response.getInt(ISWIP_TIP_FREQUENCY);
                    if (frequency == 0) {
                        frequency = 1;
                    }
                    /* 提示次数 */
                    int number = response.getInt(ISWIP_TIP_NUMBER);
                    /* gp下载地址 */
                    String gpUrl = response.getString(ISWIP_GP_DOWN_URL);
                    /* 浏览器下载地址 */
                    String browserUrl = response.getString(ISWIP_BROWSER_DOWN_URL);
                    /* 下载方式 */
                    int downType = response.getInt(ISWIP_DOWN_TYPE);
                    /* 拉取成功 */
                    LeoLog.e(TAG, "--------------拉取成功--------------");
                    // LeoLog.e(TAG, "是否为使用过快捷手势的老用户：" +
                    // isUseIswipUser());
                    // 测试日志
                    if (DBG) {
                        testLog(checkUpdate, frequency, number, gpUrl, browserUrl, downType);
                    }
                    boolean isPushLoad = arm.mIsPushLoadIswipe;
                    /* 是否显示更新通知逻辑 */
                    iswipeUpdateTipHandler(checkUpdate, isPushLoad);
                    /* 发起iswipe更新通知 */
                    boolean isUseIswipe = arm.isUseIswipUser();// 是否使用过快捷手势
                    if (isUseIswipe/* 使用过快捷手势 */
                            || isPushLoad/* push吊起 */) {
                        notificationIswipeUpdate(checkUpdate);
                    }
                    AppMasterPreference.getInstance(context).setIswipUpdateLoadingStrategy(
                            AppMasterConfig.TIME_24_HOUR);
                    AppMasterPreference.getInstance(context).setIswipUpateLastLoadingTime(
                            System.currentTimeMillis());
                    /* 保存数据服务器加载出来的数据 */
                    saveIswipUpdateDate(checkUpdate, frequency, number,
                            gpUrl, browserUrl, downType);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (arm.mIsPushLoadIswipe) {
                    arm.mIsPushLoadIswipe = false;
                }
            }
        }

        private void iswipeUpdateTipHandler(int checkUpdate, boolean isPushLoad) {
            int flag = AppMasterPreference.getInstance(context)
                    .getIswipeUpdateTip();
            if (checkUpdate == 1) {
                LeoLog.e(TAG, "Iswipe需要升级:" + checkUpdate);
                /**
                 * 负1:首次使用, <br>
                 * 0:拉取成功显示升级提示,<br>
                 * 1:首次拉取成功但是不显示升级提示,<br>
                 * 2:除了首次拉取成功之外,不显示通知<br>
                 */
                if (flag == -1) {
                    /* 首次加载为升级标志 */
                    AppMasterPreference.getInstance(context).setIswipeUpdateTip(0);
                    LeoLog.e(TAG, "首次拉取到需要Iswipe升级通知的标志");
                } else {
                    LeoLog.e(TAG, "之后拉取到Iswipe升级通知的标志");
                    AppMasterPreference.getInstance(context).setIswipeUpdateTip(1);
                }
                /* 是否为push吊起的显示iswipe升级方式 */
                if (isPushLoad) {
                    LeoLog.e(TAG, "push直接加载Iswipe需要升级通知的标志，需要直接显示更新通知");
                    AppMasterPreference.getInstance(context).setIswipeUpdateTip(0);
                }
            } else {
                LeoLog.e(TAG, "Iswipe不升级:" + checkUpdate);
                if (flag != -1) {
                    AppMasterPreference.getInstance(context).setIswipeUpdateTip(1);
                    LeoLog.e(TAG, "Iswipe本版本内有拉取到需要升级通知的标志");
                } else {
                    LeoLog.e(TAG, "首次拉取到Iswipe不去升级通知标志以及之后拉取到Iswipe不去升级通知的标志");
                }
            }
        }

        private void saveLastLoadIswipeVersion() {
            String versionCode = String.valueOf(PhoneInfo.getVersionCode(context));
            AppMasterPreference.getInstance(context).setLoadIswipVersion(versionCode);
        }

        private void notificationIswipeUpdate(int flag) {
            /* 1:升级 */
            if (flag == 1) {
                /* 发送更新通知 */
                int isTip = AppMasterPreference.getInstance(context).getIswipeUpdateTip();
                /* 首次拉取会立即显示提示对话框，如果是后面后台更新数据重新拉取提示则不会立即发出更新提示 */
                LeoLog.e(TAG, "isTip=" + isTip);
                if (isTip == 0) {
                    arm.showIswipeUpdateNotificationTip();
                }
            }
        }

        private void saveIswipUpdateDate(int checkUpdate, int frequency, int number, String gpUrl,
                String browserUrl, int downType) {
            AppMasterPreference preference = AppMasterPreference.getInstance(AppMasterApplication
                    .getInstance());

            preference.setIswipUpdateFlag(checkUpdate);
            preference.setIswipUpdateFre(frequency);
            preference.setIswipUpdateNumber(number);
            if (!Utilities.isEmpty(gpUrl)) {
                preference.setIswipUpdateGpUrl(gpUrl);
            }
            if (!Utilities.isEmpty(browserUrl)) {
                preference.setIswipUpdateBrowserUrl(browserUrl);
            }
            preference.setIswipUpdateDownType(downType);
        }

        private void testLog(int checkUpdate, int frequency, int number, String gpUrl,
                String browserUrl, int downType) {
            LeoLog.e(TAG, "是否升级：" + checkUpdate);
            LeoLog.e(TAG, "提示频率：" + frequency);
            LeoLog.e(TAG, "提示次数：" + number);
            LeoLog.e(TAG, "gp下载地址：" + gpUrl);
            LeoLog.e(TAG, "浏览器下载地址：" + browserUrl);
            LeoLog.e(TAG, "下载方式：" + downType);

        }

        @Override
        public void onErrorResponse(VolleyError error) {
            /* 拉取失败 */
            LeoLog.e(TAG, "--------------拉取失败--------------");
            AppMasterPreference.getInstance(context).setIswipUpdateLoadingStrategy(
                    AppMasterConfig.TIME_2_HOUR);
            AppMasterPreference.getInstance(context).setIswipeLoadFailDate(
                    arm.mDateFormate.format(new Date(System.currentTimeMillis())));
            AppMasterPreference.getInstance(context).setIswipUpateLastLoadingTime(
                    System.currentTimeMillis());
            if (AppMasterPreference.getInstance(context).getIswipUpdateLoadingNumber() < 0) {
                AppMasterPreference.getInstance(context).setIswipUpdateLoadingNumber(0);
            } else {
                int number = AppMasterPreference.getInstance(context)
                        .getIswipUpdateLoadingNumber() + 1;
                AppMasterPreference.getInstance(context).setIswipUpdateLoadingNumber(number);
                // LeoLog.e(TAG, "失败" +
                // (AppMasterPreference.getInstance(mContext)
                // .getIswipUpdateLoadingNumber()) + "次");
            }
        }
    }

    /* ISwip更新通知 */
    public void showISwipCheckNewNotification() {
        RemoteViews custom = new RemoteViews(mContext.getPackageName(),
                R.layout.iswipe_update_notify);
        custom.setImageViewResource(R.id.appwallIV, R.drawable.iswip_icon);
        custom.setTextViewText(R.id.appwallNameTV,
                mContext.getString(R.string.iswip_update_notifi_title));
        custom.setTextViewText(R.id.appwallDescTV,
                mContext.getString(R.string.iswip_update_notifi_content));
        NotificationManager notificationManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        notification.contentView = custom;
        Intent intent = new Intent(mContext, IswipeUpdateNotifiProxyActivity.class);
        // intent.putExtra(ISWIP_NOTIFICATION_TO_PG_HOME,
        // ISWIP_NOTIFICATION_TO_PG_HOME);
        intent.getIntExtra(StatusBarEventService.EXTRA_EVENT_TYPE,
                StatusBarEventService.EVENT_ISWIPE_UPDATE_NOTIFICATION);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.icon = R.drawable.ic_launcher_notification;
        notification.tickerText = mContext
                .getString(R.string.iswip_update_notifi_title);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationUtil.setBigIcon(notification,
                R.drawable.ic_launcher_notification_big);
        notification.contentIntent = contentIntent;
        notification.when = System.currentTimeMillis();
        notificationManager.notify(NOTIFICATION_CHECKNEW_ISWIP, notification);
    }

    /* iswip下载处理 */
    public void iSwipDownLoadHandler() {
        int downLoadType = AppMasterPreference.getInstance(mContext).getIswipUpdateDownType();
        String gpDownLoadUrl = AppMasterPreference.getInstance(mContext).getIswipUpdateGpUrl();
        String browserDownLoadUrl = AppMasterPreference.getInstance(mContext)
                .getIswipUpdateBrowserUrl();
        if (Utilities.isEmpty(gpDownLoadUrl)) {
            /* 默认Iswipe到GP下载链接 */
            gpDownLoadUrl = Constants.ISWIPE_TO_GP_CLIENT_RUL;
        }
        if (Utilities.isEmpty(browserDownLoadUrl)) {
            /* 默认Iswipe到浏览器下载链接 */
            browserDownLoadUrl = Constants.ISWIPE_TO_GP_BROWSER_RUL;
        }
        if (downLoadType == 1) {
            // LeoLog.d(TAG, "跳转到GP");
            /* googlePaly下载 */
            downLoadISwipToGP(gpDownLoadUrl, browserDownLoadUrl);
        } else {
            // LeoLog.d(TAG, "跳转到浏览器");
            /* 浏览器下载 */
            downLoadISwipToBrowser(browserDownLoadUrl);
        }
    }

    private void downLoadISwipToGP(String gpDownLoadUrl, String browserUrl) {
        if (gpDownLoadUrl != null) {
            Intent intent = startPG(gpDownLoadUrl);
            try {
                LockManager.getInstatnce().timeFilterSelf();
                mContext.startActivity(intent);
            } catch (Exception e) {
                /* 本地没有GP则跳浏览器 */
                if (browserUrl != null) {
                    downLoadISwipToBrowser(browserUrl);
                } else {
                    // LeoLog.d(TAG, "跳转GP失败后，下载ISwip的浏览器链接为空");
                }
            }
        } else {
            // LeoLog.d(TAG, "下载ISwip的GP链接为空");
        }
    }

    private void downLoadISwipToBrowser(String browserUrl) {
        if (browserUrl != null) {
            Intent intent = startBrowser(browserUrl);
            try {
                LockManager.getInstatnce().timeFilterSelf();
                mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (DBG) {
                LeoLog.d(TAG, "下载ISwip的浏览器链接为空");
            }
        }
    }

    private Intent startPG(String gpDownLoadUrl) {
        Intent intent;
        intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(gpDownLoadUrl);
        intent.setData(uri);
        ComponentName cn = new ComponentName(
                "com.android.vending",
                "com.google.android.finsky.activities.MainActivity");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private Intent startBrowser(String browserUrl) {
        Intent intent;
        Uri uri = Uri.parse(browserUrl);
        intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /* iswipe更新通知显示处理 */
    public void showIswipeUpdateNotificationTip() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                showISwipCheckNewNotification();
            }
        });
        /* 进入主页是否需要对话框提示 */
        showIswipUpdate();
        /* 记录本次通知时间 */
        AppMasterPreference.getInstance(mContext)
                .setIswipeUpdateTipTime(System.currentTimeMillis());
    }

    /* 让主页不显示iswipe升级通知 */
    public void cancelShowIswipUpdate() {
        AppMasterPreference.getInstance(mContext).setIswipeDialogTip(false);
        ISwipUpdateRequestManager.getInstance(mContext).setIswipeUpdateTip(
                false);
    }

    /* 让主页显示iswipe升级通知 */
    public void showIswipUpdate() {
        ISwipUpdateRequestManager.getInstance(mContext).setIswipeUpdateTip(true);
        AppMasterPreference.getInstance(mContext).setIswipeDialogTip(true);
    }

    /* 判断新用户即未使用过快捷手势的老用户,true:老用户，false：新用户 */
    public boolean isUseIswipUser() {
        /* 是否使用过快捷手势 */
        boolean quickGestureFristTip = AppMasterPreference.getInstance(mContext)
                .getFristSlidingTip();
        return quickGestureFristTip;
    }

    /* 判断是否安装ISwipe，true：安装，false：未安装 */
    public boolean isInstallIsiwpe() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setPackage(AppLoadEngine.ISWIPE_PACKAGENAME);
        List<ResolveInfo> resolveInfo = mContext.getPackageManager().queryIntentActivities(
                intent, 0);
        if (resolveInfo.size() > 0) {
            return true;
        }
        return false;
    }

    private void alarmTask() {
        showIswipeUpdateNotificationTip();
    }

    public void showIswipeAlarmNotificationHandler() {
        long currentTime = System.currentTimeMillis();
        long lastLoadTime = AppMasterPreference.getInstance(mContext).getIswipeUpdateTipTime();
        long fre = AppMasterPreference.getInstance(mContext).getIswipUpdateFre();
        if (lastLoadTime > 0 && (currentTime - lastLoadTime) >= fre) {
            /* 定时器已经通知的次数 */
            int alarmUseNumbers = AppMasterPreference.getInstance(mContext)
                    .getIswipeAlarmNotifiNumber();
            /* 定时器需要通知的次数 */
            int alarmNumber = AppMasterPreference.getInstance(mContext).getIswipUpdateNumber();
            ISwipUpdateRequestManager im = ISwipUpdateRequestManager.getInstance(mContext);
            if (im.getNetworkStatus()) {
                if ((alarmNumber > 0 && alarmUseNumbers <= alarmNumber) || alarmNumber == 0) {
                    /* 每次执行定时器任务时，查看是否已安装ISwipe */
                    boolean installIswipe = ISwipUpdateRequestManager.getInstance(mContext)
                            .isInstallIsiwpe();
                    if (!installIswipe) {
                        if (DBG) {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd H:m:s");
                            String date = sf.format(new Date(System.currentTimeMillis()));
                            LeoLog.e(TAG, "定时器本次执行任务  " + alarmUseNumbers + "  的时间："
                                    + date);
                        }
                        alarmTask();
                        AppMasterPreference.getInstance(mContext)
                                .setIswipeAlarmNotifiNumber(alarmUseNumbers + 1);
                    } else {
                        /* ISwipe已安装结束定时器 */
                        /* 恢复定时器已经通知的次数默认值 */
                        AppMasterPreference.getInstance(mContext).setIswipeAlarmNotifiNumber(1);
                        /* 恢复最后默认时间默认值 */
                        AppMasterPreference.getInstance(mContext).setIswipeUpdateTipTime(-1);
                    }
                } else {
                    /* 定时器执行任务次数够了，结束定时器 */
                    if (DBG) {
                        LeoLog.e(TAG, "定时任务次数完成不再执行，终于可以歇歇了");
                    }
                    /* 恢复定时器已经通知的次数默认值 */
                    AppMasterPreference.getInstance(mContext).setIswipeAlarmNotifiNumber(1);
                    /* 恢复最后默认时间默认值 */
                    AppMasterPreference.getInstance(mContext).setIswipeUpdateTipTime(-1);

                }
            } else {
                /* 网络到显示时间但是未显示标志 */
                if (DBG) {
                    LeoLog.e(TAG, "没有网络不去执行  " + alarmUseNumbers + "  任务");
                }
                im.setNoNetworkShow(true);
            }
        }
    }

    /* 是否拉取iswip更新判断,true:加载iswipe更新数据，false：不加载iswipe更新数据 */
    public boolean isLoadIswipeData() {
        return !isInstallIsiwpe();
    }

    private void showDownLoadISwipDialog(Context context, final boolean isShow) {
        if (mIswipDialog == null) {
            mIswipDialog = new IswipUpdateTipDialog(context);
        }
        mIswipDialog.setLeftListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /* 稍后再说 */
                if (!isShow) {
                    cancelShowIswipUpdate();
                }
                if (mIswipDialog != null) {
                    mIswipDialog.dismiss();
                }
            }
        });
        mIswipDialog.setRightListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /* 立即下载 */
                iSwipDownLoadHandler();
                if (!isShow) {
                    cancelShowIswipUpdate();
                }
                if (mIswipDialog != null) {
                    mIswipDialog.dismiss();
                }
            }
        });
        mIswipDialog.setFlag("homeactivity");
        mIswipDialog.show();
    }

    /* 显示iswipe更新对话框 */
    public void showIswipeUpdateTip(Context context, boolean iswipeUpdateShow) {
        boolean flag = ISwipUpdateRequestManager.getInstance(mContext).getIswipeUpdateTip();
        if ((flag && !isInstallIsiwpe()) || iswipeUpdateShow/* 是否直接显示对话框 */) {
            showDownLoadISwipDialog(context, iswipeUpdateShow);
        }
    }

    /* 关闭本地iswipe插件，通知iswipe应用开启处理 */
    public void iswipeOpenHandler() {
        boolean checkIsFilterModel = checkIsISwipeFilterPhoneModel();
        boolean isOpenIswipe = AppMasterPreference.getInstance(mContext)
                .getSwitchOpenQuickGesture();
        if (!checkIsFilterModel) {
            /* 直接启动iswipe服务 */
            /* 此处调用iswipe不能用显式，正能用隐式 */
            Intent intent = new Intent();
            intent.setAction("com.iswipe.startfrompg");
            if (isOpenIswipe) {
                intent.putExtra(kEY_PG_TO_ISWIPE, VALUE_ISWIPE_FIRST_TIP);
            } else {
                intent.putExtra(kEY_PG_TO_ISWIPE, VALUE_ISWIPE_FIRST_TIP_CLOSE);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startService(intent);
                if (DBG) {
                    LeoLog.e(TAG, "启动iswipe服务");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            /* 进入iswipe主页 */
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cn = new ComponentName(AppLoadEngine.ISWIPE_PACKAGENAME,
                    "com.leo.iswipe.activity.SplashActivity");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(intent);
                if (DBG) {
                    LeoLog.e(TAG, "启动iswipe闪屏");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        QuickGestureManager.getInstance(mContext)
                .stopFloatWindow();
        FloatWindowHelper.removeAllFloatWindow(mContext);
        if (AppMasterPreference.getInstance(mContext).getSwitchOpenStrengthenMode()) {
            FloatWindowHelper.removeWhiteFloatView(mContext);
            AppMasterPreference.getInstance(mContext).setWhiteFloatViewCoordinate(0, 0);
        }
        AppMasterPreference.getInstance(mContext).setSwitchOpenQuickGesture(false);
    }

    private boolean checkIsISwipeFilterPhoneModel() {
        boolean checkHuaWei = BuildProperties.isHuaWeiTipPhone(mContext);
        boolean checkMiui = BuildProperties.isMIUI();
        boolean isOppoOs = BuildProperties.isYiJia();
        return checkHuaWei || checkMiui || isOppoOs;
    }
}
