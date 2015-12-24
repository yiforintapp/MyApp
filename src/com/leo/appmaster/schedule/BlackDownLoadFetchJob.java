package com.leo.appmaster.schedule;

import android.content.Context;

import com.android.volley.VolleyError;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.HttpRequestAgent;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.mgr.impl.LostSecurityManagerImpl;
import com.leo.appmaster.utils.NetWorkUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by runlee on 15-12-23.
 */
public class BlackDownLoadFetchJob extends FetchScheduleJob {

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
            String blkFile = jo.getString(BLK_LIST);
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
//                lsm
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onFetchFail(VolleyError error) {
        super.onFetchFail(error);
    }

    private static void startWork() {
        BlackDownLoadFetchJob job = new BlackDownLoadFetchJob();
        FetchScheduleListener listener = job.newJsonObjListener();
        Context context = AppMasterApplication.getInstance();
        HttpRequestAgent.getInstance(context).loadBlackList(listener, listener);
    }
}
