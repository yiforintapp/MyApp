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
import com.leo.appmaster.utils.NetWorkUtil;
import com.leo.appmaster.utils.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by runlee on 15-12-23.
 */
public class BlackDownLoadFetchJob extends FetchScheduleJob {

    public static final boolean DBG = true;

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

    public static void startImmediately() {
        /*存在wifi网络再去拉取*/
        if (NetWorkUtil.isWifiConnected(AppMasterApplication.getInstance())) {
            startWork();
        }
    }

    @Override
    protected void work() {
        startWork();
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
                if (DBG) {
                    file = "http://192.168.1.205/telintercept/cn.gz";
                }
                lsm.setSerBlackFilePath(file);
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        DownBlackFileFetchJob.startWork();
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
                LeoLog.i("BlackDownLoadFetchJob:", "user-" + user);
                LeoLog.i("BlackDownLoadFetchJob", "tipUser:" + "" + tipUser);
                LeoLog.i("BlackDownLoadFetchJob", "duration:" + "" + duration);
                LeoLog.i("BlackDownLoadFetchJob", "blkMarkTpPar:" + "" + blkMarkTpPar);
                LeoLog.i("BlackDownLoadFetchJob", "blkFileStr:" + "" + blkFileStr);
                LeoLog.i("BlackDownLoadFetchJob", "starNotiTip:" + "" + starNotiTip);
                LeoLog.i("BlackDownLoadFetchJob", "blkTip:" + "" + blkTip);
                LeoLog.i("BlackDownLoadFetchJob", "markTip:" + "" + markTip);

                //查看是否保存成功
                LeoLog.i("BlackDownLoadFetchJob:", "------是否保存成功----------");
                LeoLog.i("BlackDownLoadFetchJob:", "否保存user-" + lsm.getFilterUserNumber());
                LeoLog.i("BlackDownLoadFetchJob", "否保存tipUser:" + "" + lsm.getFilterTipFroUser());
                LeoLog.i("BlackDownLoadFetchJob", "否保存duration:" + "" + lsm.getCallDurationMax());
                LeoLog.i("BlackDownLoadFetchJob", "否保存blkMarkTpPar:" + "" + lsm.getBlackMarkTipParam());
                LeoLog.i("BlackDownLoadFetchJob", "否保存blkFileStr:" + "" + lsm.getSerBlackFilePath());
                LeoLog.i("BlackDownLoadFetchJob", "否保存starNotiTip:" + "" + lsm.getStraNotiTipParam());
                LeoLog.i("BlackDownLoadFetchJob", "否保存blkTip:" + "" + lsm.getSerBlackTipCount());
                LeoLog.i("BlackDownLoadFetchJob", "否保存markTip:" + "" + lsm.getSerMarkTipCount());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
    }

    public static void startWork() {
        BlackDownLoadFetchJob job = new BlackDownLoadFetchJob();
        FetchScheduleListener listener = job.newJsonObjListener();
        Context context = AppMasterApplication.getInstance();
        HttpRequestAgent.getInstance(context).loadBlackList(listener, listener);
    }
}
