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

        CallFilterContextManagerImpl cmp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        List<BlackListInfo> blacks = new ArrayList<BlackListInfo>();
        if (mInfos != null && mInfos.size() > 0) {
            for (BlackListInfo info : mInfos) {
                info.setUploadState(CallFilterConstants.UPLOAD);
                blacks.add(info);
            }
            mInfos.clear();
            mInfos = null;
        }
        cmp.addBlackList(blacks, true);

        if (mFilUpInfos != null && mFilUpInfos.size() > 0) {
            for (BlackListInfo info : mFilUpInfos) {
                BlackListInfo black = new BlackListInfo();
                black.setNumber(info.getNumber());
                black.setFiltUpState(CallFilterConstants.FIL_UP_NO);
                Context context = AppMasterApplication.getInstance();
                CallFilterManager.getInstance(context).updateUpBlack(black);
            }
            mFilUpInfos.clear();
            mFilUpInfos = null;
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
            int i = 1;
            //已上传的拦截名单
            List<BlackListInfo> filInfos = pm.getUpBlackListLimit(i);
            List<BlackListInfo> tmpFilInfos = new ArrayList<BlackListInfo>();
            if (filInfos != null && filInfos.size() > 0) {
                for (BlackListInfo info : filInfos) {
                    if (info.getFiltUpState() == CallFilterConstants.FIL_UP) {
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
            HttpRequestAgent.getInstance(context).commitBlackList(listener, listener, bodyString);
        }
    }

    public static String getJsonString(List<BlackListInfo> infos) {
        if (infos == null || infos.size() <= 0) {
            return "";
        }

        Context context = AppMasterApplication.getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 1; i <= infos.size(); i++) {
            int type = 0;
            if (infos.get(i - 1).getFiltUpState() == CallFilterConstants.FIL_UP) {
                type = -1;
            } else {
                type = infos.get(i - 1).getLocHandlerType();
            }
            sb.append("{");
            sb.append("\"" + NUMBER + "\":");
            String number = infos.get(i - 1).getNumber();
            sb.append("\"" + number + "\",");
            sb.append("\"" + COUNTRY + "\":");
            sb.append("\"" + Utilities.getCountryID(context) + "\",");
            sb.append("\"" + TYPE + "\":");
            sb.append(type + ",");
            sb.append("\"" + ANDROID_ID + "\":");
            sb.append("\"" + DeviceUtil.getAndroidId(context) + "\"");
            sb.append("}");
            if (i != infos.size()) {
                sb.append(",");
            }
        }
        sb.append("]");
        LeoLog.i(TAG, "" + sb.toString());
        return sb.toString();
    }

    @Override
    protected int getPeriod() {
        return FETCH_PERIOD;
    }

    public static void filterUpload(String number) {
        if (TextUtils.isEmpty(number)) {
            return;
        }
        CallFilterContextManagerImpl cmp = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        List<BlackListInfo> blacks = cmp.getNoUploadBlackList();
        String formateNum = PrivacyContactUtils.formatePhoneNumber(number);
        for (BlackListInfo info : blacks) {
            if (info.getNumber().contains(formateNum)) {
                BlackUploadFetchJob job = new BlackUploadFetchJob();
                FetchScheduleListener listener = job.newJsonObjListener();
                Context context = AppMasterApplication.getInstance();
                CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                List<BlackListInfo> infos = new ArrayList<BlackListInfo>();
                BlackListInfo black = new BlackListInfo();
                black.setNumber(info.getNumber());
                black.setLocHandlerType(-1);
                infos.add(black);
                String bodyString = getJsonString(infos);

                HttpRequestAgent.getInstance(context).commitBlackList(listener, listener, bodyString);

                break;
            }

        }
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
}
