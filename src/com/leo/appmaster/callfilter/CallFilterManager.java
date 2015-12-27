package com.leo.appmaster.callfilter;

import android.content.Context;
import android.os.RemoteException;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.mgr.CallFilterContextManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by runlee on 15-12-18.
 */
public class CallFilterManager {
    public static final String TAG = "CallFilterManager";

    private static CallFilterManager mInstance;
    private Context mContext;
    private CallFilterContextManager mCFCManager;
    private long mLastOffHookTime = 0;
    private List<BlackListInfo> mBlackList;
    private List<BlackListInfo> mSerBlackList;
    private CallFilterToast mTipToast;
    /**
     * 拨出电话
     */
    private boolean mIsComingOut = false;

    /**
     * 是否加载过黑名单
     */
    private boolean mIsBlackLoad = false;

    /**
     * 是否执行“判断是否需要通话提示”
     */
    private boolean mIsCallFilterTip = false;

    public boolean isIsCallFilterTip() {
        return mIsCallFilterTip;
    }

    public void setIsCallFilterTip(boolean isCallFilterTip) {
        this.mIsCallFilterTip = isCallFilterTip;
    }

    public boolean isComingOut() {
        return mIsComingOut;
    }

    /**
     * 获取黑名单列表
     *
     * @return
     */
    public List<BlackListInfo> getBlackList() {
        loadBlackList();
        return mBlackList;
    }

    /**
     * 获取服务器下发黑名单列表
     *
     * @return
     */
    public List<BlackListInfo> getSerBlackList() {
        loadSerBlackList();
        return mSerBlackList;
    }

    public void setIsComingOut(boolean isComingOut) {
        this.mIsComingOut = isComingOut;
    }

    public static synchronized CallFilterManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CallFilterManager(context.getApplicationContext());
        }
        return mInstance;
    }

    private CallFilterManager(Context context) {
        mContext = context;
    }

    /**
     * 骚扰拦截处理
     *
     * @param action
     * @param phoneNumber
     * @param state
     * @param iTelephony
     */
    public void filterCallHandler(String action, String phoneNumber, String state, final ITelephony iTelephony) {
        LeoLog.i(TAG, "state:" + state);
        int serBlackCt = getSerBlackCount();
        int blackCt = getBlackListCount();
        if (serBlackCt <= 0 && blackCt <= 0) {
            /*黑名单无数据*/
            return;
        }
        BlackListInfo info = null;
        BlackListInfo serInfo = null;
        if (!TextUtils.isEmpty(phoneNumber)) {
            info = getBlackFroNum(phoneNumber);
            serInfo = getSerBlackForNum(phoneNumber);
            if (info == null && serInfo == null) {
            /*该号码不存在黑名单中*/
                return;
            }
        } else {
            if (TextUtils.isEmpty(state)) {
                return;
            }
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {

            } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
                CallFilterManager.getInstance(mContext).setIsComingOut(false);
                if (mTipToast != null) {
                    mTipToast.hide();
                    mTipToast = null;
                }
                //挂断后，判断当前时间和之前接听的时间的差值，小于配置的判定时间则在挂断后弹出对话框
                if (System.currentTimeMillis() - mLastOffHookTime < 1000) {
                    CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(phoneNumber, AppMasterApplication.getInstance(), true, 0).show();
                }
            } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                mLastOffHookTime = System.currentTimeMillis();
            }
            return;
        }

        mCFCManager = (CallFilterContextManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
                /*判断是否为服务器黑名单*/
        if (info != null) {
                    /*为本地黑名单：拦截*/
            try {
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (iTelephony != null) {
                                iTelephony.endCall();
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });

                LeoLog.i(TAG, "iTelephony endCall()");
                List<CallFilterInfo> infos = new ArrayList<CallFilterInfo>();
                CallFilterInfo callInfo = new CallFilterInfo();
                callInfo.setNumber(PrivacyContactUtils.simpleFromateNumber(phoneNumber));
                callInfo.setTimeLong(System.currentTimeMillis());
                callInfo.setCallType(CallLog.Calls.INCOMING_TYPE);
                callInfo.setReadState(CallFilterConstants.READ_NO);
                infos.add(callInfo);
                mCFCManager.addFilterDet(infos, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            CallFIlterUIHelper.getInstance().showReceiveCallNotification();
        } else if (serInfo != null)

        {
                    /*为服务器黑名单：弹窗提醒*/

            int[] filterTip = mCFCManager.isCallFilterTip(phoneNumber);
            if (filterTip == null) {
                /*存在于隐私联系人中*/
                return;
            }
                    /*判断是否满足弹框条件*/
            int isTip = filterTip[0];
            int tipType = filterTip[1];
            int showValue = filterTip[2];
            if (CallFilterConstants.IS_TIP_DIA[0] == isTip) {
                return;
            }
            if (PrivacyContactUtils.NEW_OUTGOING_CALL.equals(action)) {
                        /*通话类型：拨出*/
                CallFilterManager.getInstance(mContext).setIsComingOut(true);
                LeoLog.i("PrivacyContactReceiver", "拨打电话");
            } else {
                        /*通话类型：来电，无状态*/
                LeoLog.i("PrivacyContactReceiver", "来电电话");
                boolean isComOut = CallFilterManager.getInstance(mContext).isComingOut();
                if (!isComOut) {
                    if (CallFilterConstants.DIALOG_TYPE[0] == tipType) {
                        /*标记弹框*/
                        mTipToast = CallFilterToast.makeText(mContext, phoneNumber, "已被" + String.valueOf(showValue) + "人拉入", "标记");
                    } else {
                        /*黑名单弹框*/
                        mTipToast = CallFilterToast.makeText(mContext, "13632840685", "已被" + String.valueOf(showValue) + "人拉入", "黑名单");
                    }
                    if (mTipToast != null) {
                        mTipToast.show();
                    }
                    LeoLog.i(TAG, "Black and marker tip show!");
                }
                if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {

                } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
                    LeoLog.i(TAG, "挂断！");
                    CallFilterManager.getInstance(mContext).setIsComingOut(false);
                    if (mTipToast != null) {
                        mTipToast.hide();
                        mTipToast = null;
                    }
                    //挂断后，判断当前时间和之前接听的时间的差值，小于配置的判定时间则在挂断后弹出对话框
                    if (System.currentTimeMillis() - mLastOffHookTime < 1000) {
                        CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(phoneNumber, AppMasterApplication.getInstance(), true, 0).show();
                    }
                } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    mLastOffHookTime = System.currentTimeMillis();
                }
            }
        }

    }

    /**
     * 加载本地黑名单列表
     */
    private synchronized void loadBlackList() {
        if (!mIsBlackLoad) {
            if (mBlackList != null) {
                mBlackList.clear();
            }
            /*加载黑名单*/
            CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
            mBlackList = pm.getBlackList();
//            mIsBlackLoad = true;
        }
    }

    /**
     * 获取本地黑名单列表数量
     *
     * @return
     */
    public int getBlackListCount() {
        List<BlackListInfo> infos = getBlackList();
        if (infos != null && infos.size() > 0) {
            return infos.size();
        }
        return 0;
    }


    /**
     * 指定号码查询本地黑名单是否存在该号码
     *
     * @param number
     * @return
     */
    public BlackListInfo getBlackFroNum(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        List<BlackListInfo> infos = getBlackList();
        if (infos != null && infos.size() > 0) {
            String formateNum = PrivacyContactUtils.formatePhoneNumber(number);
            for (BlackListInfo info : infos) {
                if (info.getNumber().contains(formateNum)) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * 服务器下发所有黑名单
     *
     * @return
     */
    public synchronized void loadSerBlackList() {
        if (mSerBlackList != null) {
            mSerBlackList.clear();
        }
        CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        mSerBlackList = pm.getServerBlackList();


    }

    /**
     * 获取服务器下发黑名单数量
     *
     * @return
     */
    public int getSerBlackCount() {
        List<BlackListInfo> infos = getSerBlackList();
        if (infos != null && infos.size() > 0) {
            return infos.size();
        }
        return 0;
    }

    /**
     * 指定号码查询服务器黑名单列表是否存在该号码
     *
     * @param number
     * @return
     */
    public BlackListInfo getSerBlackForNum(String number) {
        if (TextUtils.isEmpty(number)) {
            return null;
        }
        List<BlackListInfo> infos = getSerBlackList();
        if (infos != null && infos.size() > 0) {
            String formateNum = PrivacyContactUtils.formatePhoneNumber(number);
            for (BlackListInfo info : infos) {
                if (info.getNumber().contains(formateNum)) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * 解析黑名单列表后加入到黑名单数据库
     *
     * @param info
     */
    public void addFilterFroParse(BlackListInfo info) {
        CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        List<BlackListInfo> infos = new ArrayList<BlackListInfo>();
        String number = info.getNumber();
        int blackCount = info.getAddBlackNumber();
        int markType = info.getMarkerType();
        int markCount = info.getMarkerNumber();
        info.setNumber(number);
        info.setAddBlackNumber(blackCount);
        info.setMarkerType(markType);
        info.setMarkerNumber(markCount);
        infos.add(info);
        pm.addSerBlackList(infos);
    }


}
