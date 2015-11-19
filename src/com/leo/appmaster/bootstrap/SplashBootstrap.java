
package com.leo.appmaster.bootstrap;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.home.SplashActivity;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.HttpRequestAgent.RequestListener;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.FileOperationUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

/**
 * 闪屏相关初始化
 *
 * @author Jasper
 */
public class SplashBootstrap extends Bootstrap {
    private static final String TAG = "SplashBootstrap";

    public static volatile boolean mSplashFlag;
    public static volatile boolean mIsEmptyForSplashUrl;
    public static volatile int mSplashDelayTime;

    SplashBootstrap() {
        super();
    }

    @Override
    protected boolean doStrap() {
        // initStrap();

        loadSplashDate(false);
        return true;
    }

    // public static void initStrap() {
    // AppMasterApplication mApp = AppMasterApplication.getInstance();
    // mIsEmptyForSplashUrl = isEmptySplashUrl();
    // /* 闪屏延时时间 */
    // mSplashDelayTime =
    // AppMasterPreference.getInstance(mApp).getSplashDelayTime();
    // Log.i(Constants.RUN_TAG, "initStrap拉取配置闪屏时间:" +
    // SplashBootstrap.mSplashDelayTime);
    // }

    @Override
    public String getClassTag() {
        return TAG;
    }

    // /* 闪屏跳转连接是否为空：true-链接为空，false-链接不为空 */
    // private static boolean isEmptySplashUrl() {
    // AppMasterApplication mApp = AppMasterApplication.getInstance();
    // String splashSkipUrl =
    // AppMasterPreference.getInstance(mApp).getSplashSkipUrl();
    // String splashSkipToClient =
    // AppMasterPreference.getInstance(mApp).getSplashSkipToClient();
    //
    // return TextUtils.isEmpty(splashSkipUrl) &&
    // TextUtils.isEmpty(splashSkipToClient);
    // }

    /**
     * 加载闪屏
     */
    public static void loadSplashDate(boolean isFromPush) {
        LeoLog.d(TAG, "start load splash");
        AppMasterApplication mApp = AppMasterApplication.getInstance();
        final AppMasterPreference pref = AppMasterPreference.getInstance(mApp);
        final SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");
        long curTime = System.currentTimeMillis();
        Date currentDate = new Date(curTime);
        final String failDate = dateFormate.format(currentDate);
        long lastLoadTime = pref.getLastLoadSplashTime();
        if (lastLoadTime == 0
                || (curTime - pref.getLastLoadSplashTime()) >
                pref.getSplashCurrentStrategy() || isFromPush) {
            if (Constants.SPLASH_REQUEST_FAIL_DATE.equals(pref.getSplashLoadFailDate())
                    || pref.getSplashLoadFailNumber() < 0
                    || !failDate.equals(pref.getSplashLoadFailDate())
                    || (failDate.equals(pref.getSplashLoadFailDate()) && pref
                    .getSplashLoadFailNumber() <= 2) || isFromPush) {
                /* 日期变化数据初始化 */
                if (!failDate.equals(pref.getSplashLoadFailDate()) || isFromPush) {
                    if (pref.getSplashLoadFailNumber() != 0) {
                        pref.setSplashLoadFailNumber(0);
                    }
                    if (!Constants.SPLASH_REQUEST_FAIL_DATE
                            .equals(pref.getSplashLoadFailDate())) {
                        pref.setSplashLoadFailDate(Constants.SPLASH_REQUEST_FAIL_DATE);
                    }
                }
                LeoLog.d(TAG, "http request");
                SplashRequestListener splashListener = new SplashRequestListener(mApp, pref,
                        dateFormate);
                HttpRequestAgent.getInstance(mApp).loadSplashDate(splashListener, splashListener);
            }
        } else {
            pref.setLoadSplashStrategy(pref.getSplashFailStrategy(),
                    pref.getSplashSuccessStrategy(), pref.getSplashFailStrategy());
            TimerTask recheckTask = new TimerTask() {
                @Override
                public void run() {
                    loadSplashDate(false);
                }
            };
            Timer timer = ThreadManager.getTimer();
            long delay = pref.getSplashCurrentStrategy() - (curTime - lastLoadTime);
            if (delay < 0) {
                delay = AppMasterConfig.TIME_12_HOUR;
            }
            timer.schedule(recheckTask, delay);
        }
    }

    /* 闪屏网络请求监听 */
    private static class SplashRequestListener extends RequestListener<AppMasterApplication> {
        AppMasterPreference pref = null;
        SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
                LeoLog.d(TAG, "拉取成功！");
                try {
                    /* 起始时间 */
                    String startDate = response.getString(Constants.REQUEST_SPLASH_SHOW_STARTDATE);
                    /* 图片url */
                    String imageUrl = response.getString(Constants.REQUEST_SPLASH_IMAGEURL);
                    /* 结束时间 */
                    String endDate = response.getString(Constants.REQUEST_SPLASH_SHOW_ENDDATE);
                    /* 闪屏延迟时间 */
                    int splashDelayTime = response
                            .getInt(Constants.REQUEST_SPLASH_DELAY_TIME);
                    /* 跳转链接 */
                    String splashSkipUrl = response.getString(Constants.REQUEST_SPLASH_SKIP_URL);
                    /* 跳转方式 */
                    String splashSkipMode = response.getString(Constants.REQUEST_SPLASH_SKIP_FLAG);
                    /* 跳转客户端的链接 */
                    String splashSkipToClient = response
                            .getString(Constants.SPLASH_SKIP_TO_CLIENT_URL);

                    LeoLog.d(TAG, "起始时间：" + startDate);
                    LeoLog.d(TAG, "图片url：" + imageUrl);
                    LeoLog.d(TAG, "结束时间：" + endDate);
                    LeoLog.d(TAG, "闪屏延迟时间：" + splashDelayTime);
                    LeoLog.d(TAG, "跳转链接：" + splashSkipUrl);
                    LeoLog.d(TAG, "跳转方式：" + splashSkipMode);
                    LeoLog.d(TAG, "跳转客户端的链接：" + splashSkipToClient);

                    /**
                     * 闪屏Button文案
                     *
                     * @该字段目前未使用所以没有做保存只是打Log供测试测试用后续有使用的对该字段再作处理
                     */
                    String spalshBtText = response.getString(Constants.SPLASH_BUTTON_TEXT);
                    if (spalshBtText != null) {
                        Log.d(Constants.RUN_TAG, "闪屏Button的文案：" + spalshBtText);
                        LeoLog.d(TAG, "闪屏Button的文案：" + spalshBtText);
                    }
                    StringBuilder stringBuilder = constructionSplashFlag(startDate, imageUrl,
                            endDate, String.valueOf(splashDelayTime), splashSkipUrl,
                            splashSkipMode,
                            splashSkipToClient);
                    String splashUriFlag = stringBuilder.toString();
                    String prefStringUri = pref.getSplashUriFlag();
                    int prefInt = pref.getSaveSplashIsMemeryEnough();
                    if (!prefStringUri.equals(splashUriFlag) || prefInt != -1) {
                        if (!prefStringUri.equals(splashUriFlag)) {
                            if (!Utilities.isEmpty(splashUriFlag)) {
                                pref.setSplashUriFlag(splashUriFlag);
                                /* 初始化显示时间段 */
                                if (pref.getSplashStartShowTime() != -1) {
                                    pref.setSplashStartShowTime(-1);
                                }
                                if (pref.getSplashEndShowTime() != -1) {
                                    pref.setSplashEndShowTime(-1);
                                }
                                clearSpSplashFlagDate();
                            }
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
                        if (splashDelayTime > 0) {
                            int delayTime = Integer.valueOf(splashDelayTime);
                            pref.setSplashDelayTime(delayTime);
                            /* 后台拉取成功更新缓存数据 */
                            mSplashDelayTime = delayTime;
                        } else {
                            pref.setSplashDelayTime(Constants.SPLASH_DELAY_TIME);
                            /* 后台拉取成功更新缓存数据 */
                            mSplashDelayTime = Constants.SPLASH_DELAY_TIME;
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
                    loadSplashDate(false);
                }
            };
            Timer timer = ThreadManager.getTimer();
            long delay = pref.getSplashCurrentStrategy();

            if (delay < 0) {
                delay = AppMasterConfig.TIME_12_HOUR;
            }
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
            LeoLog.d(TAG, "拉取失败！");
            LeoLog.e(TAG, error.getMessage());
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
                    loadSplashDate(false);
                }
            };
            Timer timer = ThreadManager.getTimer();
            long delay = pref.getSplashCurrentStrategy();

            if (delay < 0) {
                delay = AppMasterConfig.TIME_12_HOUR;
            }
            timer.schedule(recheckTask, delay);
        }

    }

    /**
     * 对后台配置的过期闪屏数据初始化
     */
    private static void clearSpSplashFlagDate() {
        AppMasterApplication mApp = AppMasterApplication.getInstance();
        AppMasterPreference.getInstance(mApp).setSplashUriFlag(
                Constants.SPLASH_FLAG);
        AppMasterPreference.getInstance(mApp).setSplashDelayTime(Constants.SPLASH_DELAY_TIME);
        mSplashDelayTime = Constants.SPLASH_DELAY_TIME;
        AppMasterPreference.getInstance(mApp).setSplashSkipUrl(null);
        mIsEmptyForSplashUrl = true;
    }

    /* 加载闪屏图 */
    private static void getSplashImage(String url) {
        final AppMasterApplication mApp = AppMasterApplication.getInstance();
        final SimpleDateFormat dateFormate = new SimpleDateFormat("yyyy-MM-dd");
        final AppMasterPreference pref = AppMasterPreference.getInstance(mApp);
        Date currentDate = new Date(System.currentTimeMillis());
        final String failDate = dateFormate.format(currentDate);
        String dir = FileOperationUtil.getSplashPath() + Constants.SPLASH_NAME;
        HttpRequestAgent.getInstance(mApp).loadSplashImage(url, dir, new Listener<File>() {

            @Override
            public void onResponse(File response, boolean noMidify) {
                pref.setLastLoadSplashTime(System.currentTimeMillis());
                /*下载成功后生成二维码图保存*/
                boolean isSaveShare = shareSplashImage();
                /*如果保存失败尝试再次保存一次*/
                if (!isSaveShare) {
                    shareSplashImage();
                }
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                AppMasterPreference.getInstance(mApp).setSaveSplashIsMemeryEnough(2);
                if ("splash_fail_default_date".equals(pref.getSplashLoadFailDate())) {
                    pref.setSplashLoadFailDate(failDate);
                } else if (pref.getSplashLoadFailNumber() >= 0
                        && pref.getSplashLoadFailNumber() <= 2) {
                    pref.setSplashLoadFailNumber(pref.getSplashLoadFailNumber() + 1);
                }
                pref.setLoadSplashStrategy(pref.getSplashFailStrategy(),
                        pref.getSplashSuccessStrategy(), pref.getSplashFailStrategy());
                pref.setLastLoadSplashTime(System.currentTimeMillis());
                TimerTask recheckTask = new TimerTask() {
                    @Override
                    public void run() {
                        loadSplashDate(false);
                    }
                };
                Timer timer = ThreadManager.getTimer();
                timer.schedule(recheckTask, pref.getSplashCurrentStrategy());
            }
        });
    }

    /*生成闪屏分享图片*/
    private static boolean shareSplashImage() {
        String path = FileOperationUtil.getSplashPath();
        StringBuilder sb = new StringBuilder(path);
        sb.append(Constants.SPLASH_NAME);
        if (Utilities.isEmpty(sb.toString())) {
            return false;
        }
        Bitmap result = AppUtil.add2Bitmap(sb.toString(), R.drawable.spl_share_qr);
        StringBuilder sbShar = new StringBuilder(path);
        sbShar.append(Constants.SPL_SHARE_QR_NAME);
        boolean isOutPutSuc = AppUtil.outPutImage(sbShar.toString(), result);
        return isOutPutSuc;
    }
}
