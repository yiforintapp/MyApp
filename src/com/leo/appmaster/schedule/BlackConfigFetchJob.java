package com.leo.appmaster.schedule;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.callfilter.CallFilterConstants;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by runlee on 15-12-23.
 */
public class BlackConfigFetchJob extends FetchScheduleJob {

    public static final boolean DBG = false;

    /**
     * 骚扰拦截用户数量参数
     */
    public static final String FIL_USER = "filter_user";
    /**
     * 骚扰拦截对比显示参数
     */
    public static final String FIL_TP = "filter_tp";
    /**
     * 通话时长参数
     */
    public static final String CALL_DR = "call_dr";
    /**
     * 黑名单，标记人数显示倍率参数
     */
    public static final String BLK_TP_PAR = "blk_tp_par";
    /**
     * 黑名单列表
     */
    public static final String BLK_LIST = "blk_list";
    /**
     * 陌生人通知提示倍率参数
     */
    public static final String STRA_TP = "stra_tp";
    /**
     * 黑名单来电提示显示的人数参数
     */
    public static final String BLK_TP = "blk_tp";
    /**
     * 标记来电提示显示的人数参数
     */
    public static final String MARK_TP = "mark_tp";
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
        FetchScheduleJob job = new BlackConfigFetchJob();
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
        JSONObject jo = (JSONObject) response;
        try {
            int user = jo.getInt(FIL_USER);
            int tipUser = jo.getInt(FIL_TP);
            long duration = jo.getLong(CALL_DR);
            int blkMarkTpPar = jo.getInt(BLK_TP_PAR);
            String blkFileStr = jo.getString(BLK_LIST);
            int starNotiTip = jo.getInt(STRA_TP);
            int blkTip = jo.getInt(BLK_TP);
            int markTip = jo.getInt(MARK_TP);
            CallFilterContextManagerImpl lsm = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
            if (user > 0) {
                lsm.setFilterUserNumber(user);
            }
            if (tipUser > 0) {
                lsm.setFilterTipFroUser(tipUser);
            }
            if (duration > 0) {
                lsm.setCallDurationMax(duration);
            }
            if (blkMarkTpPar > 0) {
                lsm.setBlackMarkTipParam(blkMarkTpPar);
            }
            /*下载黑名单链接*/
            if (!TextUtils.isEmpty(blkFileStr)) {
                StringBuilder sb = new StringBuilder();
                String countryId = Utilities.getCountryID(AppMasterApplication.getInstance());
                sb.append(blkFileStr);
                sb.append(countryId);
                sb.append(CallFilterConstants.GZIP);
                String file = sb.toString();
                lsm.setSerBlackFilePath(file);
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        BlackListFileFetchJob.startImmediately(true);
                    }
                });
            }
            if (starNotiTip > 0) {
                lsm.setStraNotiTipParam(starNotiTip);
            }
            if (blkTip > 0) {
                lsm.setSerBlackTipNum(blkTip);
            }
            if (markTip > 0) {
                lsm.setSerMarkTipNum(markTip);
            }
            if (DBG) {
                LeoLog.i("BlackConfigFetchJob:", "user-" + user);
                LeoLog.i("BlackConfigFetchJob", "tipUser:" + "" + tipUser);
                LeoLog.i("BlackConfigFetchJob", "duration:" + "" + duration);
                LeoLog.i("BlackConfigFetchJob", "blkMarkTpPar:" + "" + blkMarkTpPar);
                LeoLog.i("BlackConfigFetchJob", "blkFileStr:" + "" + blkFileStr);
                LeoLog.i("BlackConfigFetchJob", "starNotiTip:" + "" + starNotiTip);
                LeoLog.i("BlackConfigFetchJob", "blkTip:" + "" + blkTip);
                LeoLog.i("BlackConfigFetchJob", "markTip:" + "" + markTip);

                //查看是否保存成功
                LeoLog.i("BlackConfigFetchJob:", "------是否保存成功----------");
                LeoLog.i("BlackConfigFetchJob:", "否保存user-" + lsm.getFilterUserNumber());
                LeoLog.i("BlackConfigFetchJob", "否保存tipUser:" + "" + lsm.getFilterTipFroUser());
                LeoLog.i("BlackConfigFetchJob", "否保存duration:" + "" + lsm.getCallDurationMax());
                LeoLog.i("BlackConfigFetchJob", "否保存blkMarkTpPar:" + "" + lsm.getBlackMarkTipParam());
                LeoLog.i("BlackConfigFetchJob", "否保存blkFileStr:" + "" + lsm.getSerBlackFilePath());
                LeoLog.i("BlackConfigFetchJob", "否保存starNotiTip:" + "" + lsm.getStraNotiTipParam());
                LeoLog.i("BlackConfigFetchJob", "否保存blkTip:" + "" + lsm.getSerBlackTipCount());
                LeoLog.i("BlackConfigFetchJob", "否保存markTip:" + "" + lsm.getSerMarkTipCount());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
    }

    private static void startWork(FetchScheduleJob job) {
        FetchScheduleListener listener = job.newJsonObjListener();
        Context context = AppMasterApplication.getInstance();
        HttpRequestAgent.getInstance(context).loadBlackList(listener, listener);
    }

    @Override
    protected int getRetryValue() {
        return super.getRetryValue();
    }

    @Override
    protected int getScheduleValue() {
        return super.getScheduleValue();
    }

    @Override
    protected long getScheduleTime() {
        return super.getScheduleTime();
    }
}
