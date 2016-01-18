package com.leo.appmaster.schedule;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.FileRequest;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.callfilter.BlackListInfo;
import com.leo.appmaster.callfilter.CallFilterConstants;
import com.leo.appmaster.callfilter.CallFilterUtils;
import com.leo.appmaster.cloud.crypto.CryptoUtils;
import com.leo.appmaster.db.BlacklistTab;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.MsgCenterEvent;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterManagerImpl;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.Utilities;
import com.leo.imageloader.utils.IoUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectTimeoutException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

/**
 * Created by runlee on 15-12-24.
 */
public class BlackListFileFetchJob extends FetchScheduleJob {

    public static final String TAG = "BlackListFileFetchJob";
    /**
     * 拉取间隔，24小时
     */
    private static final int FETCH_PERIOD = 24 * 60 * 60 * 1000;

    /**
     * 直接请求
     *
     * @param flag 是否需要直接请求，还是遵守策略请求
     */
    public static void startImmediately(boolean flag) {
        FetchScheduleJob job = new BlackListFileFetchJob();
        if (flag) {
            //直接请求
            startWorkImmediately(job);
        } else {
            //遵守策略请求
            long time = job.getScheduleTime();
            int requestState = job.getScheduleValue();
            long currTime = System.currentTimeMillis();

            if (FetchScheduleJob.STATE_SUCC == requestState) {
                if ((currTime - time) >= FetchScheduleJob.FETCH_PERIOD) {
                    startWorkImmediately(job);
                }
            } else if (FetchScheduleJob.STATE_FAIL == requestState) {
                if ((currTime - time) >= FetchScheduleJob.FETCH_FAIL_ERIOD) {
                    startWorkImmediately(job);
                }
            }
        }
    }

    @Override
    protected void work() {
        startWorkImmediately(this);
    }

    @Override
    protected void onFetchSuccess(Object response, boolean noMidify) {
        super.onFetchSuccess(response, noMidify);
        Context context = AppMasterApplication.getInstance();
        SDKWrapper.addEvent(context, SDKWrapper.P1, "block", "server_lib");
//        StringBuilder sbName = new StringBuilder();
//        String countryId = Utilities.getCountryID(AppMasterApplication.getInstance());
//        sbName.append(countryId);
//        sbName.append(CallFilterConstants.GZIP);
//
//        StringBuilder sb = new StringBuilder();
//        sb.append(CallFilterUtils.getBlackPath());
//        sb.append(sbName.toString());
//        String filePath = sb.toString();
//        CallFilterUtils.parseBlactList(filePath);
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
    }

    private static void startWorkImmediately(FetchScheduleJob job) {
         /*存在wifi网络再去拉取*/
        if (NetWorkUtil.isWifiConnected(AppMasterApplication.getInstance())) {
            Context context = AppMasterApplication.getInstance();
            SDKWrapper.addEvent(context, SDKWrapper.P1, "block", "server_lib_try");
            FetchScheduleListener listener = job.newJsonObjListener();
            String filePath = getBlackFilePath();
            LeoLog.d(TAG, "start work immediately. path: " + filePath);
            CallFilterManagerImpl pm = (CallFilterManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
            String uri = pm.getSerBlackFilePath();
            if (TextUtils.isEmpty(uri)) {
                return;
            }
            requestBlackListFile(listener);
//            HttpRequestAgent.getInstance(context).downloadBlackList(filePath, listener, listener);
        }
    }

    @Override
    protected int getPeriod() {
        return FETCH_PERIOD;
    }

    public static String getBlackFilePath() {
        StringBuilder sbName = new StringBuilder();
        String countryId = Utilities.getCountryID(AppMasterApplication.getInstance());
        sbName.append(countryId);
        sbName.append(CallFilterConstants.GZIP);

        StringBuilder sb = new StringBuilder();
        sb.append(CallFilterUtils.getBlackPath());
        sb.append(sbName.toString());
        return sb.toString();
    }

    @Override
    protected boolean onInterceptSchedule() {
        return true;
    }

    private static void requestBlackListFile(FetchScheduleListener listener) {
        CallFilterManagerImpl pm = (CallFilterManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        String url = pm.getSerBlackFilePath();

        HttpStack stack = new HurlStack();
        FileRequest request = new FileRequest(url, null, null, null);

        int maxRetryCount = 3;
        int retryCount = 0;

        List<BlackListInfo> listInfos = new ArrayList<BlackListInfo>();
        BufferedReader reader = null;
        boolean success = false;
        try {
            while (retryCount < maxRetryCount) {
                try {
                    HttpResponse response = stack.performRequest(request, new HashMap<String, String>());

                    Map<String, String> responseHeaders = Collections.emptyMap();
                    responseHeaders = convertHeaders(response.getAllHeaders());

                    String contentEncoding = responseHeaders.get("Content-Encoding");
                    boolean supportGzip = contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip");

                    String contentAes = responseHeaders.get("Content_Cpto");
                    boolean supportAes = contentAes != null && contentAes.equalsIgnoreCase("aes");

                    HttpEntity entity = response.getEntity();
                    InputStream inputStream = entity.getContent();
                    if (supportAes) {
                        inputStream = CryptoUtils.newInputStream(inputStream);
                    }
                    if (supportGzip) {
                        inputStream = new GZIPInputStream(inputStream);
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        try {
                            String[] dates = line.split(",");
                            final String number = dates[0];
                            if (TextUtils.isEmpty(number)) {
                                continue;
                            }
                            String blackCountStr = dates[1];
                            String markTypeStr = dates[2];
                            String markCountStr = dates[3];
                            final int blackCount = Float.valueOf(blackCountStr).intValue();
                            final int markType = Float.valueOf(markTypeStr).intValue();
                            final int markCount = Float.valueOf(markCountStr).intValue();

                            LeoLog.i(TAG, "number-" + number + " | " + "blackCount-" + blackCount +
                                    " | " + "markType-" + markType + " | " + "markCount-" + markCount);

                            BlackListInfo info = new BlackListInfo();
                            info.number = number;
                            info.blackNum = blackCount;
                            info.markType = markType;
                            info.markNum = markCount;
                            listInfos.add(info);
                        } catch (Throwable e) {
                            LeoLog.e(TAG, "requestBlackListFile, parse line ex." + e.toString());
                        }
                    }
                    LeoLog.i(TAG, "requestBlackListFile res file succ.");
                    LeoEventBus.getDefaultBus().post(new MsgCenterEvent(MsgCenterEvent.ID_RES));
                    success = true;
                    break;
                } catch (SocketTimeoutException e) {
                    // retry
                    retryCount++;
                    LeoLog.e(TAG, "requestBlackListFile socket timeout ex, retrycount: " + retryCount);
                } catch (ConnectTimeoutException e) {
                    // retry
                    retryCount++;
                    LeoLog.e(TAG, "requestBlackListFile connect timeout ex, retrycount: " + retryCount);
                } catch (Exception e) {
                    LeoLog.e(TAG, "requestBlackListFile socket timeout ex, e: " + e.toString());
                    e.printStackTrace();
                    break;
                }
            }
        } finally {
            IoUtils.closeSilently(reader);
        }

        if (success) {
            if (!listInfos.isEmpty()) {
                BlacklistTab.getInstance().addServerBlackList(listInfos);
            }
            BlacklistTab.getInstance().initEncryptList();
            if (listener != null) {
                listener.onResponse(null, false);
            }
        } else {
            listener.onErrorResponse(null);
        }
    }

    protected static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new TreeMap<String, String>(
                String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }

    public static void resetTimesAndCounts() {
        LeoLog.i(TAG, "resetTimesAndCounts....");
        AppMasterApplication context = AppMasterApplication.getInstance();
        AppMasterPreference pref = AppMasterPreference.getInstance(context);

        FetchScheduleJob job = new BlackListFileFetchJob();
        // 保存时间
        pref.setScheduleTime(job.getJobTimeKey(), 0);
        // 保存状态
        pref.setScheduleValue(job.getJobStateKey(), STATE_FAIL);
        // 保存重试次数
        pref.setScheduleValue(job.getJobFailCountKey(), 0);
    }
}
