package com.leo.appmaster.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.utils.LeoLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 拉取业务基类
 * 每12小时拉取一次，失败每3小时拉取一次，持续3次
 * 维护下面三个变量到pref
 * 1、状态 —— 成功、失败
 * 2、失败次数
 * 3、请求时间 —— 不区分成功和失败，使用变量1区分
 * Created by Jasper on 2015/9/8.
 */
public abstract class FetchScheduleJob extends ScheduleJob {
    public static final String KEY_JOB = "key_job";

    /**
     * 默认拉取间隔，12小时
     */
    protected static final int FETCH_PERIOD = 12 * 60 * 60 * 1000;
//    private static final int FETCH_PERIOD = 12 * 1000;
    /**
     * 失败拉取间隔，2小时
     */
    protected static final int FETCH_FAIL_ERIOD = 2 * 60 * 60 * 1000;
//    private static final int FETCH_FAIL_ERIOD = 10 * 1000;
    /**
     * 默认失败重试次数
     */
    private static final int FETCH_FAIL_COUNT = 3;

    protected static final int STATE_SUCC = 1;
    protected static final int STATE_FAIL = 0;

    private static final String[] FETCH_JOBS = {
            "com.leo.appmaster.schedule.MsgCenterFetchJob",
            "com.leo.appmaster.schedule.ADFetchJob",
            "com.leo.appmaster.schedule.LockRecommentFetchJob",
            "com.leo.appmaster.schedule.SwiftyFetchJob",
            "com.leo.appmaster.schedule.LockRecommentFetchJob",
            "com.leo.appmaster.schedule.PhoneSecurityFetchJob",
            "com.leo.appmaster.schedule.CardFetchJob",
            "com.leo.appmaster.schedule.BlackConfigFetchJob",
            "com.leo.appmaster.schedule.ShareFetchJob",
            "com.leo.appmaster.schedule.LockPermissionTipStringFetchJob"
    };

    public static void startFetchJobs() {
        for (String fetchJob : FETCH_JOBS) {
            try {
                Class<?> clazz = Class.forName(fetchJob);
                Object object = clazz.newInstance();

                if (object instanceof ScheduleJob) {
//                    if (object instanceof MsgCenterFetchJob && BuildProperties.isApiLevel14()) {
//                        continue;
//                    }
                    final ScheduleJob job = (ScheduleJob) object;
                    job.start();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getId() {
        return getClass().getSimpleName().hashCode();
    }

    protected String getJobKey() {
        return getClass().getSimpleName();
    }

    protected String getJobTimeKey() {
        return getClass().getSimpleName() + "_time";
    }

    protected String getJobFailCountKey() {
        return getClass().getSimpleName() + "_fail";
    }

    protected String getJobStateKey() {
        return getClass().getSimpleName() + "_state";
    }

    @Override
    public void start() {
        LeoLog.i(getJobKey(), "start job.");
        AppMasterApplication ctx = AppMasterApplication.getInstance();
        AppMasterPreference pref = AppMasterPreference.getInstance(ctx);
        long lastTime = pref.getScheduleTime(getJobTimeKey());
        if (lastTime <= 0 || AppMasterApplication.isAppUpgrade()) {
            // 两种情况会直接开始执行：1、之前未加载过，2、升级
            LeoLog.i(getJobKey(), "Haven't worked before, start work.");
            ThreadManager.executeOnNetworkThread(new Runnable() {
                @Override
                public void run() {
                    work();
                }
            });
        } else {
            int state = pref.getScheduleValue(getJobStateKey(), STATE_SUCC);
            startInner(state == STATE_SUCC);
        }

    }

    private void startInner(boolean success) {
        LeoLog.i(getJobKey(), "startInner, success: " + success);
        AppMasterApplication ctx = AppMasterApplication.getInstance();
        AppMasterPreference pref = AppMasterPreference.getInstance(ctx);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ScheduleReceiver.ACTION);
        intent.putExtra(KEY_JOB, getClass().getName());
        int period = getPeriod();
        if (!success) {
            int currentRetryCount = pref.getScheduleValue(getJobFailCountKey(), 0);

            if (currentRetryCount <= getRetryCount()) {
                LeoLog.i(getJobKey(), "haven't overlimit max retry count.");
                period = getFailPeriod();
            } else {
                LeoLog.i(getJobKey(), "have overlimit max retry count.");
                // 重试次数已经大于了设定的次数，则把状态及次数设置为true
                // 保存重试次数
                pref.setScheduleValue(getJobFailCountKey(), 0);
                // 保存重试状态
                pref.setScheduleValue(getJobStateKey(), STATE_SUCC);
            }
        }
        long lastTime = pref.getScheduleTime(getJobTimeKey());
        long goesBy = System.currentTimeMillis() - lastTime;
        if (goesBy >= period) {
            ThreadManager.executeOnNetworkThread(new Runnable() {
                @Override
                public void run() {
                    work();
                }
            });
        } else {
            period -= goesBy;
            LeoLog.i(getJobKey(), "period is : " + period);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, getId(),
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.RTC, System.currentTimeMillis() + period, pendingIntent);
        }
    }

    @Override
    public void stop() {
        LeoLog.i(getJobKey(), "stop job.");
        AppMasterApplication ctx = AppMasterApplication.getInstance();
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ScheduleReceiver.ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, getId(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pendingIntent);
    }

    /**
     * 获取拉取间隔
     *
     * @return
     */
    protected int getPeriod() {
        return FETCH_PERIOD;
    }

    /**
     * 获取失败后的拉取间隔
     *
     * @return
     */
    protected int getFailPeriod() {
        return FETCH_FAIL_ERIOD;
    }

    /**
     * 获取失败后的重试次数
     *
     * @return
     */
    protected int getRetryCount() {
        return FETCH_FAIL_COUNT;
    }

    /**
     * 拉取数据成功
     */
    protected void onFetchSuccess(Object response, boolean noMidify) {
        LeoLog.i(getJobKey(), "onFetchSuccess, response: " + response + " | noModify: " + noMidify);
        AppMasterApplication context = AppMasterApplication.getInstance();
        AppMasterPreference pref = AppMasterPreference.getInstance(context);
        // 保存时间
        pref.setScheduleTime(getJobTimeKey(), System.currentTimeMillis());
        // 保存状态
        pref.setScheduleValue(getJobStateKey(), STATE_SUCC);
        // 保存重试次数
        pref.setScheduleValue(getJobFailCountKey(), 0);
        if (!onInterceptSchedule()) {
            startInner(true);
        }
    }

    /**
     * 拉取时间失败
     */
    protected void onFetchFail(VolleyError error) {
        LeoLog.i(getJobKey(), "onFetchFail, error: " + error);
        AppMasterApplication context = AppMasterApplication.getInstance();
        AppMasterPreference pref = AppMasterPreference.getInstance(context);
        // 保存时间
        pref.setScheduleTime(getJobTimeKey(), System.currentTimeMillis());
        // 保存状态
        pref.setScheduleValue(getJobStateKey(), STATE_FAIL);
        // 保存重试次数
        int count = pref.getScheduleValue(getJobFailCountKey(), 0);
        pref.setScheduleValue(getJobFailCountKey(), ++count);
        if (!onInterceptSchedule()) {
            startInner(false);
        }
    }

    protected FetchScheduleListener newJsonObjListener() {
        return new FetchScheduleListener<JSONObject>();
    }

    protected FetchScheduleListener newJsonArrayListener() {
        return new FetchScheduleListener<JSONArray>();
    }

    public class FetchScheduleListener<T> implements Response.Listener<T>, Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            onFetchFail(error);
        }

        @Override
        public void onResponse(T response, boolean noMidify) {
            onFetchSuccess(response, noMidify);
        }
    }

    /**
     * 存储卡片需要的数据
     */
    protected void setValue(JSONObject object, String key,
                            String prefKey, PreferenceTable preferenceTable) {
        try {
            if (!object.isNull(key)) {
                if (TextUtils.isEmpty(object.getString(key))) {
                    preferenceTable.putString(prefKey, "");
                } else {
                    preferenceTable.putString(prefKey, object.getString(key));
                }
            } else {
                preferenceTable.putString(prefKey, "");
            }
        } catch (JSONException e) {
            preferenceTable.putString(prefKey, "");
        }
    }

    /**
     * 获取上次请求完成保存的时间
     *
     * @return
     */
    protected long getScheduleTime() {
        AppMasterApplication context = AppMasterApplication.getInstance();
        AppMasterPreference pref = AppMasterPreference.getInstance(context);
        return pref.getScheduleTime(getJobTimeKey());
    }

    /**
     * 获取上次请求后状态
     *
     * @return
     */
    protected int getScheduleValue() {
        AppMasterApplication context = AppMasterApplication.getInstance();
        AppMasterPreference pref = AppMasterPreference.getInstance(context);
        return pref.getScheduleValue(getJobStateKey(), STATE_SUCC);
    }

    /**
     * 获取重试次数
     *
     * @return
     */
    protected int getRetryValue() {
        AppMasterApplication context = AppMasterApplication.getInstance();
        AppMasterPreference pref = AppMasterPreference.getInstance(context);
        return pref.getScheduleValue(getJobFailCountKey(), 0);
    }

    /**
     * 是否阻断定时
     * @return
     */
    protected boolean onInterceptSchedule() {
        return false;
    }
}
