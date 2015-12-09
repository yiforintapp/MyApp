
package com.leo.appmaster.quickgestures;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.engine.AppLoadEngine;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.HttpRequestAgent.RequestListener;
import com.leo.appmaster.mgr.LockManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

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
    private static final String TAG = "ISwipUpdateRequestManager";

    public static enum IswipeNotificationType {
        NOTIFICATION, DIALOG
    }

    private ISwipUpdateRequestManager(Context context) {
        mContext = context;
        mDateFormate = new SimpleDateFormat("yyyy-MM-dd");
    }

    public static synchronized ISwipUpdateRequestManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ISwipUpdateRequestManager(context);
        }
        return mInstance;
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

            CheckNewIswipRequestListener checkNewIswipListener = new CheckNewIswipRequestListener(
                    ISwipUpdateRequestManager.getInstance(mContext));
            HttpRequestAgent.getInstance(AppMasterApplication.getInstance()).loadISwipCheckNew(
                    checkNewIswipListener,
                    checkNewIswipListener);
        } else {
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
            if (response != null && !noMidify) {
                if (AppMasterConfig.LOGGABLE) {
                    LeoLog.i(TAG, "Success: " + response.toString());
                }
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

                    AppMasterPreference.getInstance(context).setIswipUpdateLoadingStrategy(
                            AppMasterConfig.TIME_24_HOUR);
                    AppMasterPreference.getInstance(context).setIswipUpateLastLoadingTime(
                            System.currentTimeMillis());
                    /* 保存数据服务器加载出来的数据 */
                    saveIswipUpdateDate(checkUpdate, frequency, number,
                            gpUrl, browserUrl, downType);
                } catch (JSONException e) {

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


        @Override
        public void onErrorResponse(VolleyError error) {
            /* 拉取失败 */
            if (AppMasterConfig.LOGGABLE) {
                LeoLog.i(TAG, "Fail!!");
            }
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

    /* iswip下载处理 */
    public void iSwipDownLoadHandler() {
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

        if (AppMasterConfig.LOGGABLE) {
            LeoLog.d(TAG, "GP: " + gpDownLoadUrl);
            LeoLog.d(TAG, "Browser: " + browserDownLoadUrl);
        }
        downLoadISwipToGP(gpDownLoadUrl, browserDownLoadUrl);

    }

    private void downLoadISwipToGP(String gpDownLoadUrl, String browserUrl) {
        if (gpDownLoadUrl != null) {
            Intent intent = startPG(gpDownLoadUrl);
            try {
                LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                lm.filterSelfOneMinites();
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
                LockManager lm = (LockManager) MgrContext.getManager(MgrContext.MGR_APPLOCKER);
                lm.filterSelfOneMinites();
                mContext.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (AppMasterConfig.LOGGABLE) {
                LeoLog.d(TAG, "下载ISwip的浏览器链接为空");
            }
        }
    }

    private Intent startPG(String gpDownLoadUrl) {
        Intent intent;
        intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(gpDownLoadUrl);
        intent.setData(uri);
//        ComponentName cn = new ComponentName(
//                "com.android.vending",
//                "com.google.android.finsky.activities.MainActivity");
//        intent.setComponent(cn);
        intent.setPackage("com.android.vending");
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

    /* 判断是否安装ISwipe，true：安装，false：未安装 */
    public static boolean isInstallIsiwpe(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setPackage(AppLoadEngine.ISWIPE_PACKAGENAME);
        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(
                intent, 0);
        if (resolveInfo != null && resolveInfo.size() > 0) {
            return true;
        }
        return false;
    }

}
