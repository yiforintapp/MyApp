package com.leo.appmaster.schedule;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.callfilter.BlackListInfo;
import com.leo.appmaster.callfilter.CallFilterConstants;
import com.leo.appmaster.callfilter.CallFilterManager;
import com.leo.appmaster.callfilter.CallFilterUtils;
import com.leo.appmaster.db.BlacklistTab;
import com.leo.appmaster.mgr.CallFilterContextManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.utils.DeviceUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by runlee on 15-12-23.
 */
public class BlackUploadFetchJob extends FetchScheduleJob {

    public static final String TAG = "BlackUploadFetchJob";

    public static final String NUMBER = "number";
    public static final String TYPE = "type";
    public static final String COUNTRY = "country";
    public static final String ANDROID_ID = "android_id";
    public static final String BODY = "body";
    /**
     * 拉取间隔，24小时
     */
    private static final int FETCH_PERIOD = 24 * 60 * 60 * 1000;
    private static List<BlackListInfo> mInfos;
    private static List<BlackListInfo> mFilUpInfos;

    /**
     * 直接请求
     *
     * @param flag 是否需要直接请求，还是遵守策略请求
     */
    public static void startImmediately(boolean flag) {
        FetchScheduleJob job = new BlackUploadFetchJob();
        if (flag) {
            //直接请求
            startWork(job);
        } else {
            //遵循策略请求
            long time = job.getScheduleTime();
            int requestState = job.getScheduleValue();
            long currTime = System.currentTimeMillis();

            if (FetchScheduleJob.STATE_SUCC == requestState) {
                if ((currTime - time) >= FetchScheduleJob.FETCH_PERIOD) {
                    startWork(job);
                }
            } else if (FetchScheduleJob.STATE_FAIL == requestState) {
                if ((currTime - time) >= FetchScheduleJob.FETCH_FAIL_ERIOD) {
                    startWork(job);
                }
            }
        }
    }

    @Override
    protected void work() {
        startWork(this);
    }

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
        if (mInfos != null) {
            for (BlackListInfo info : mInfos) {
                if (info == null) {
                    continue;
                }
                info.uploadState = CallFilterConstants.UPLOAD;
            }
            BlacklistTab.getInstance().updateUploadState(mInfos);
            mInfos.clear();
        }

        if (mFilUpInfos != null) {
            for (BlackListInfo info : mFilUpInfos) {
                if (info == null) {
                    continue;
                }
                info.filtUpState = CallFilterConstants.FIL_UP_NO;
            }
            BlacklistTab.getInstance().updateIntercept(mFilUpInfos);
            mFilUpInfos.clear();
        }

    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
        if (mInfos != null) {
            mInfos.clear();
            mInfos = null;
        }
        if (mFilUpInfos != null) {
            mFilUpInfos.clear();
            mFilUpInfos = null;
        }
    }

    private static void startWork(FetchScheduleJob job) {
         /*存在wifi网络再去拉取*/
        if (NetWorkUtil.isWifiConnected(AppMasterApplication.getInstance())) {
            FetchScheduleListener listener = job.newJsonObjListener();
            Context context = AppMasterApplication.getInstance();

            CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
            if (mInfos == null) {
                mInfos = new ArrayList<BlackListInfo>();
            }
            if (mFilUpInfos == null) {
                mFilUpInfos = new ArrayList<BlackListInfo>();
            }
            mInfos.clear();
            mFilUpInfos.clear();
            int i = 1;
            //已上传的拦截名单
            List<BlackListInfo> filInfos = pm.getUpBlackListLimit(i);
            List<BlackListInfo> tmpFilInfos = new ArrayList<BlackListInfo>();
            if (filInfos != null && filInfos.size() > 0) {
                for (BlackListInfo info : filInfos) {
                    if (info.filtUpState == CallFilterConstants.FIL_UP) {
                        tmpFilInfos.add(info);
                    }
                }
                mFilUpInfos.addAll(tmpFilInfos);
            }

            //未上传的黑名单
            List<BlackListInfo> infos = new ArrayList<BlackListInfo>(100);
            infos = pm.getNoUpBlackListLimit(i);
            if (infos == null) {
                infos = new ArrayList<BlackListInfo>();
            }
            mInfos.addAll(infos);
            infos.addAll(tmpFilInfos);
            if (infos == null || infos.size() <= 0) {
                return;
            }
            String bodyString = getJsonString(infos);
            if (!TextUtils.isEmpty(bodyString)) {
                HttpRequestAgent.getInstance(context).commitBlackList(listener, listener, bodyString);
            }
        }
    }

    public static String getJsonString(List<BlackListInfo> infos) {
        if (infos == null || infos.size() <= 0) {
            return null;
        }

        Context context = AppMasterApplication.getInstance();

        JSONArray array = new JSONArray();
        int type = 0;
        for (BlackListInfo info : infos) {
            JSONObject object = new JSONObject();
            try {
                object.put(NUMBER, info.number);
                if (info.filtUpState == CallFilterConstants.FIL_UP) {
                    type = -1;
                } else {
                    type = info.markType;
                }
                object.put(TYPE, type);
                object.put(COUNTRY, Utilities.getCountryID(context));
                object.put(ANDROID_ID, DeviceUtil.getAndroidId(context));

                array.put(object);
            } catch (JSONException e) {
                LeoLog.e(TAG, "getJsonString object put ex.");
            }
        }
        String result = null;
        if (array.length() > 0) {
            result = array.toString();
        }
        LeoLog.i(TAG, "getJsonString, result: " + result);

        return result;
    }

    @Override
    protected int getPeriod() {
        return FETCH_PERIOD;
    }

    @Override
    protected long getScheduleTime() {
        return super.getScheduleTime();
    }

    @Override
    protected int getScheduleValue() {
        return super.getScheduleValue();
    }

    @Override
    protected int getRetryValue() {
        return super.getRetryValue();
    }

    @Override
    protected boolean onInterceptSchedule() {
        return true;
    }
}
