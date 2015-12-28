package com.leo.appmaster.callfilter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.RemoteException;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.mgr.CallFilterContextManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.MultiChoicesWitchSummaryDialog;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by runlee on 15-12-18.
 */
public class CallFilterManager {
    public static final String TAG = "CallFilterManager";
    private static final boolean DBG = false;

    private static CallFilterManager mInstance;
    private Context mContext;
    private long mLastOffHookTime = 0;
    private List<BlackListInfo> mBlackList;
    private List<BlackListInfo> mSerBlackList;
    private CallFilterToast mTipToast;
    private long mCurrentCallTime = -1;
    private String mPhoneNumber = null;
    /**
     * 拨出电话
     */
    private boolean mIsComingOut = false;

    private boolean mIsOffHook = false;


    /**
     * 是否加载过黑名单
     */
    private boolean mIsBlackLoad = false;

    /**
     * 是否执行“判断是否需要通话提示”
     */
    private boolean mIsCallFilterTip = false;

    private boolean mIsReceiver = false;

    private boolean mReceiverHanl = false;

    public long getCurrentCallTime() {
        return mCurrentCallTime;
    }

    public synchronized void setCurrentCallTime(long currentCallTime) {
        this.mCurrentCallTime = currentCallTime;
    }

    public boolean isReceiverHanl() {
        return mReceiverHanl;
    }

    public void setReceiverHanl(boolean receiverHanl) {
        this.mReceiverHanl = receiverHanl;
    }

    public boolean isReceiver() {
        return mIsReceiver;
    }

    public void setIsReceiver(boolean isReceiver) {
        this.mIsReceiver = isReceiver;
    }

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
    public void filterCallHandler(String action, final String phoneNumber, String state, final ITelephony iTelephony) {
        /*去除重复广播*/
        long time = System.currentTimeMillis();
        if ((time - getCurrentCallTime()) < CallFilterConstants.CALL_RECEIV_DURAT) {
            setCurrentCallTime(-1);
            return;
        }

        LeoLog.i(TAG, "state:" + state + ":" + System.currentTimeMillis() + "-call-" + phoneNumber);
        setIsReceiver(true);
        final CallFilterContextManager mCFCManager = (CallFilterContextManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        boolean isShortTime = false;
        int serBlackCt = getSerBlackCount();
        int blackCt = getBlackListCount();
        if (serBlackCt <= 0 && blackCt <= 0) {
            /*黑名单无数据*/
//            return;   TODO PH
        }
        BlackListInfo info = null;
        BlackListInfo serInfo = null;
        if (!TextUtils.isEmpty(phoneNumber)) {
            setReceiverHanl(false);
            mPhoneNumber = phoneNumber;
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
                int[] filterTip = mCFCManager.isCallFilterTip(mPhoneNumber);
                CallFilterManager.getInstance(mContext).setIsComingOut(false);

                if (mTipToast != null) {
                    mTipToast.hide();
                    mTipToast = null;
                }
                //挂断后，判断当前时间和之前接听的时间的差值，小于配置的判定时间则在挂断后弹出对话框
                long durationMax = mCFCManager.getCallDurationMax();
                durationMax = 15000;
                if (filterTip == null) {
                    return;
                }
                LeoLog.i(TAG, "idle : mLastOffHookTime =" + mLastOffHookTime);
                if (System.currentTimeMillis() - mLastOffHookTime < durationMax && info == null && serInfo == null) {
                    //通话时间过短的提醒加入黑名单对话框
                    final MultiChoicesWitchSummaryDialog dialog1 = CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(mPhoneNumber, AppMasterApplication.getInstance(), true, 0);
                    dialog1.getListView().setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                long id) {
                            dialog1.setNowItemPosition(position);
                        }
                    });
                    dialog1.setRightBtnListener(new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
                            BlackListInfo infot = new BlackListInfo();
                            int nowItemPosition = dialog1.getNowItemPosition();
                            infot.setNumber(mPhoneNumber);
                            switch (nowItemPosition) {
                                case 0:
                                    infot.setMarkerType(CallFilterConstants.FILTER_CALL_TYPE);
                                    break;
                                case 1:
                                    infot.setMarkerType(CallFilterConstants.AD_SALE_TYPE);
                                    break;
                                case 2:
                                    infot.setMarkerType(CallFilterConstants.CHEAT_NUM_TYPE);
                                    break;
                                default:
                                    break;
                            }
                            infost.add(infot);
                            mCFCManager.addBlackList(infost, false);
                            dialog1.dismiss();
                        }
                    });
                    /*恢复默认值*/
                    setCurrentCallTime(-1);
                } else if (mIsOffHook && CallFilterConstants.DIALOG_TYPE[0] == filterTip[1]) {
                    LeoLog.i(TAG, "idle : mIsOffHook =" + mIsOffHook + "ask marked");
                    mIsOffHook = false;
//                  挂断后接听 询问是否家黑名单且展示标记人数
                    final MultiChoicesWitchSummaryDialog callHandleDialogWithSummary = CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(mPhoneNumber, mContext, true, 0);
                    String summaryS = mContext.getResources().getString(R.string.call_filter_confirm_ask_mark_summary);
                    String summaryF = String.format(summaryS, filterTip[2], filterTip[2]);
                    callHandleDialogWithSummary.setContent(summaryF);
                    callHandleDialogWithSummary.getListView().setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                long id) {
                            callHandleDialogWithSummary.setNowItemPosition(position);
                        }
                    });
                    callHandleDialogWithSummary.setRightBtnListener(new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
                            BlackListInfo infot = new BlackListInfo();
                            int nowItemPosition = callHandleDialogWithSummary.getNowItemPosition();
                            infot.setNumber(mPhoneNumber);
                            switch (nowItemPosition) {
                                case 0:
                                    infot.setMarkerType(CallFilterConstants.FILTER_CALL_TYPE);
                                    break;
                                case 1:
                                    infot.setMarkerType(CallFilterConstants.AD_SALE_TYPE);
                                    break;
                                case 2:
                                    infot.setMarkerType(CallFilterConstants.CHEAT_NUM_TYPE);
                                    break;
                                default:
                                    break;
                            }
                            infost.add(infot);
                            mCFCManager.addBlackList(infost, false);
                            callHandleDialogWithSummary.dismiss();
                        }
                    });
                    callHandleDialogWithSummary.show();
                } else if (mIsOffHook && CallFilterConstants.DIALOG_TYPE[1] == filterTip[1]) {
                    LeoLog.i(TAG, "idle : mIsOffHook =" + mIsOffHook + "ask add to blacklist");
                    mIsOffHook = false;
                    //挂断后接听 询问是否加入黑名单且展示加入黑名单人数
                    final MultiChoicesWitchSummaryDialog callHandleDialogWithSummary = CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(phoneNumber, mContext, true, 0);
                    String summaryS = mContext.getResources().getString(R.string.call_filter_confirm_add_to_blacklist_summary);
                    String summaryF = String.format(summaryS, filterTip[2]);
                    callHandleDialogWithSummary.setContent(summaryF);
                    callHandleDialogWithSummary.setRightBtnListener(new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            callHandleDialogWithSummary.dismiss();
                        }
                    });
                    callHandleDialogWithSummary.show();
                } else if (!mIsOffHook) {
                    int tipType = filterTip[1];
                    //TODO 这里开始判断条件然后弹出对应的对话框
                    final LEOAlarmDialog confirmAddToBlacklistDialog = CallFIlterUIHelper.getInstance().getConfirmAddToBlacklistDialog(mContext, mPhoneNumber, String.valueOf(filterTip[2]));
                    confirmAddToBlacklistDialog.setRightBtnListener(new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO
                            //点击确定后直接添加记录
//                                mCFCManager.addBlackList(blackList, update);
                        }
                    });
                    confirmAddToBlacklistDialog.show();
                }
            } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                mLastOffHookTime = System.currentTimeMillis();
                mIsOffHook = true;
                LeoLog.i(TAG, "offhook : mLastOffHookTime =" + mLastOffHookTime);
            }
            mPhoneNumber = null;
            return;
        }

       /*区分本地黑名单和服务器黑名单*/
        if (info != null) {
            /*为本地黑名单：拦截*/
            try {
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (iTelephony != null) {
                                setIsReceiver(false);
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
                callInfo.setTimeLong(System.currentTimeMillis());
                callInfo.setNumber(PrivacyContactUtils.simpleFromateNumber(phoneNumber));
                callInfo.setCallType(CallLog.Calls.INCOMING_TYPE);
                callInfo.setReadState(CallFilterConstants.READ_NO);
                infos.add(callInfo);
                LeoLog.i(TAG, "add fiter detail ");
                mCFCManager.addFilterDet(infos, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            CallFIlterUIHelper.getInstance().showReceiveCallNotification();
        } else if (serInfo != null) {
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
            int filterType = filterTip[3];
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
                        mTipToast = CallFilterToast.makeText(mContext, phoneNumber, showValue, CallFilterToast.FILTER_TYPE, filterType);
                    } else {
                        /*黑名单弹框*/
                        mTipToast = CallFilterToast.makeText(mContext, phoneNumber, showValue, CallFilterToast.BLACK_LIST_TYPE, 0);
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
                }
            }
        }
        setCurrentCallTime(System.currentTimeMillis());
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

    /**
     * 陌生人多个来电通知处理
     */
    public void filterNotiTipHandler() {
        String selection = CallLog.Calls.TYPE + " = ? and " + CallLog.Calls.NEW + " = ? ";
        String[] selectionArgs = new String[]{
                String.valueOf(CallLog.Calls.MISSED_TYPE), String.valueOf(1)
        };

        ArrayList<ContactCallLog> callLogs = (ArrayList<ContactCallLog>) PrivacyContactUtils
                .getSysCallLog(mContext, selection, selectionArgs, false, false);
        if (callLogs != null && callLogs.size() > 0) {
            int count = callLogs.size();
            CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
            int param = pm.getStraNotiTipParam();

            if (DBG) {
                count = 3;
                param = 1;
            }

            int remainder = count % param;
            if (remainder == 0) {
                /*多个未接来电为指定倍数通知提示*/
                CallFIlterUIHelper.getInstance().showStrangerNotification(count);
            } else {
                /*未接陌生人来电通知提示*/
                if (isReceiver()) {
                    CallFIlterUIHelper.getInstance().showStrangerNotification(count);
                }
            }
            setIsReceiver(false);
        } else {
            setIsReceiver(false);
            return;
        }
    }


}
