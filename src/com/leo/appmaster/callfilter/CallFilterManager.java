
package com.leo.appmaster.callfilter;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
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
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.CommonEvent;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.mgr.CallFilterContextManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.CallFilterContextManagerImpl;
import com.leo.appmaster.mgr.impl.PrivacyContactManagerImpl;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.MultiChoicesWitchSummaryDialog;
import com.leo.appmaster.utils.LeoLog;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

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
    private static String mPhoneNumber = null;
    private MultiChoicesWitchSummaryDialog mDialogAskAddWithSmrMark;
    private MultiChoicesWitchSummaryDialog mDialogAskAddWithSmr;
    private LEOAlarmDialog mDialogAskAdd;
    private MultiChoicesWitchSummaryDialog mDialogTooShort;
    private boolean mIsDialogShowWhenOffhookAndIdle = false;
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

    /**
     * 本次通话的号码
     */
    private String mCurrentRecePhNum;

    /**
     * 当前是否在拦截TAB
     */
    private boolean mIsFilterTab = false;

    /**
     * 当前拦截时间
     */
    private long mFilterTime = 0;
    /**
     * 当前来电号码
     */
    private String mFilterNum;

    public String getFilterNum() {
        return mFilterNum;
    }

    public void setFilterNum(String filterNum) {
        this.mFilterNum = filterNum;
    }

    public long getFilterTime() {
        return mFilterTime;
    }

    public void setFilterTime(long filterTime) {
        this.mFilterTime = filterTime;
    }

    public boolean isIsFilterTab() {
        return mIsFilterTab;
    }

    public void setIsFilterTab(boolean isFilterTab) {
        this.mIsFilterTab = isFilterTab;
    }

    public String getCurrentRecePhNum() {
        return mCurrentRecePhNum;
    }

    public void setCurrentRecePhNum(String currentRecePhNum) {
        this.mCurrentRecePhNum = currentRecePhNum;
    }

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
    public void filterCallHandler(String action, final String phoneNumber, String state,
                                  final ITelephony iTelephony) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumber = phoneNumber;
        }
        /* 判断骚扰拦截是否打开 */
        final CallFilterContextManager cmp = (CallFilterContextManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        boolean filOpSta = cmp.getFilterOpenState();
        if (!filOpSta) {
            return;
        }
        /* 去除重复广播 */
        long time = System.currentTimeMillis();
        if ((time - getCurrentCallTime()) < CallFilterConstants.CALL_RECEIV_DURAT) {
            setCurrentCallTime(-1);
            return;
        }
        LeoLog.i(TAG, "state:" + state + ":" + System.currentTimeMillis() + "-call-" + phoneNumber);
        setIsReceiver(true);

        BlackListInfo info = null;
        BlackListInfo serInfo = null;
        if (PrivacyContactUtils.NEW_OUTGOING_CALL.equals(action)) {
            /* 通话类型：拨出 */
            CallFilterManager.getInstance(mContext).setIsComingOut(true);
            LeoLog.i("PrivacyContactReceiver", "拨打电话");
        }
        if (!TextUtils.isEmpty(phoneNumber)) {
            // 刚刚受到来电 （还没有接听）
            setCurrentRecePhNum(phoneNumber);
            mPhoneNumber = phoneNumber;
            info = getBlackFroNum(phoneNumber);
            serInfo = getSerBlackForNum(phoneNumber);
            LeoLog.i("testdata", "firstly, phoneNumber is not null, info = " + (info == null ? "null" : "not null"));
            LeoLog.i("testdata", "firstly, phoneNumber is not null, serInfo = " + (serInfo == null ? "null" : "not null"));
        }
        //自己拨出，return
        if (TextUtils.isEmpty(state) || isComingOut()) {
            LeoLog.i("testdata", "isComingOut = " + isComingOut());
            LeoLog.i("testdata", "state = " + state.toString());
            return;
        }
        if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
            if (!TextUtils.isEmpty(phoneNumber)) {
                mPhoneNumber = phoneNumber;
                LeoLog.i("testdata", "ringing... and number is not null , get! mPhoneNumber = " + mPhoneNumber);
            } else {
                LeoLog.i("testdata", "ringing... and number is null , ignore!  mPhoneNumber = " + (mPhoneNumber == null ? "null" : mPhoneNumber));
            }
            final BlackListInfo blackInfo = info;
            final BlackListInfo blackSerInfo = serInfo;
            if (blackInfo != null) {
                //本地黑名单，直接拦截
                endCallAndRecord(phoneNumber, iTelephony, cmp);
            } else if (blackSerInfo != null) {
                /* 为服务器黑名单：弹窗提醒 */
                int[] filterTip = cmp.isCallFilterTip(phoneNumber);
                if (filterTip == null) {
                    /* 存在于隐私联系人中 */
                    return;
                }
                /* 判断是否满足弹框条件 */
                int isTip = filterTip[0];
                int tipType = filterTip[1];
                int showValue = filterTip[2];
                int filterType = filterTip[3];
                LeoLog.i("testdata", "ringing... and info null serinfo not null ,try toast.. " + "filterTip[0]=" + filterTip[0] 
                        +"    filterTip[1]=" + filterTip[1] + "    filterTip[2]=" + filterTip[2] + "    filterTip[3]=" + filterTip[3]);
                if (CallFilterConstants.IS_TIP_DIA[0] == isTip) {
                    return;
                }
                // if (PrivacyContactUtils.NEW_OUTGOING_CALL.equals(action))
                // {
                // /*通话类型：拨出*/
                // CallFilterManager.getInstance(mContext).setIsComingOut(true);
                // LeoLog.i("PrivacyContactReceiver", "拨打电话");
                // } else {
                /* 通话类型：来电，无状态 */
                boolean isComOut = CallFilterManager.getInstance(mContext).isComingOut();
                if (!isComOut) {
                    if (CallFilterConstants.DIALOG_TYPE[0] == tipType) {
                        /* 标记弹框 */
                        mTipToast = CallFilterToast.makeText(mContext, phoneNumber, showValue, CallFilterToast.FILTER_TYPE, filterType);
                    } else {
                        /* 黑名单弹框 */
                        mTipToast = CallFilterToast.makeText(mContext, phoneNumber, showValue, CallFilterToast.BLACK_LIST_TYPE, 0);
                    }
                    if (mTipToast != null) {
                        mTipToast.show();
                        ThreadManager.getTimer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (mTipToast != null) {
                                    mTipToast.hide();
                                }
                            }
                        }, 1000 * 60);
                    }
                    LeoLog.i(TAG, "Black and marker tip show!");
                }
            }
        } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
//            LeoLog.i("testdata", "idle mPhoneNumber = "+(mPhoneNumber == null ? "null" : mPhoneNumber));
//            LeoLog.i("testdata", "idle phoneNumber = "+phoneNumber);
            LeoLog.i(TAG, "挂断！");
            // toast消失，并置为null，保证为null即消失
            if (mTipToast != null) {
                mTipToast.hide();
                mTipToast = null;
            }
            //如果带了号码，更新之前记录的号码
            if (!TextUtils.isEmpty(phoneNumber)) {
                mPhoneNumber = phoneNumber;
                LeoLog.i("testdata", "idle number != null get! " + mPhoneNumber);
            }
            /* 恢复默认值 */
            setCurrentCallTime(-1);
            info = getBlackFroNum(mPhoneNumber);
            serInfo = getSerBlackForNum(mPhoneNumber);
            int[] filterTip = cmp.isCallFilterTip(mPhoneNumber);
            CallFilterManager.getInstance(mContext).setIsComingOut(false);
            // 挂断后，判断当前时间和之前接听的时间的差值，小于配置的判定时间则在挂断后弹出对话框
            long durationMax = cmp.getCallDurationMax();
//                durationMax = 7955;
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - mLastOffHookTime;
            LeoLog.i(TAG, "idle : mLastOffHookTime =" + mLastOffHookTime);
            LeoLog.i(TAG, "idle : System.currentTimeMillis() =" + currentTime);
            LeoLog.i("allnull", "deltaTime = " + deltaTime);
            
            
            LeoLog.i("testdata", "idle... " + "info = " + (info == null ? "null" : "not null"));
            LeoLog.i("testdata", "idle... " + "serinfo = " + (serInfo == null ? "null" : "not null"));
            if (filterTip != null) {
                LeoLog.i("testdata", "idle... and info null serinfo not null ,try toast.. " + "filterTip[0]=" + filterTip[0] 
                        +"    filterTip[1]=" + filterTip[1] + "    filterTip[2]=" + filterTip[2] + "    filterTip[3]=" + filterTip[3]);
            } else {
                LeoLog.i("testdata", "idle... " + "filterTip = null");
            }
            LeoLog.i("testdata", "deltaTime = " + deltaTime);

            
            // 时间过短 且 服务器和本地都没有数据
//            mIsOffHook = false
            if (deltaTime < durationMax && (filterTip == null || CallFilterConstants.IS_TIP_DIA[0] == filterTip[0])) {
//                mIsOffHook = false;
                if (mDialogTooShort != null && mDialogTooShort.isShowing()) {
                    mIsOffHook = false;
                    return;
                }
                // 通话时间过短的提醒加入黑名单对话框
                mDialogTooShort = CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(mPhoneNumber, AppMasterApplication.getInstance(), true, 0, true);
                String summaryF = String.format(mContext.getResources().getString(R.string.call_filter_ask_add_to_blacklist), (int) (Math.ceil(durationMax / 1000)));
                mDialogTooShort.setContent(summaryF);
                mDialogTooShort.setRightBtnListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
                        BlackListInfo infot = new BlackListInfo();
                        int nowItemPosition = mDialogTooShort.getNowItemPosition();
                        infot.setNumber(mPhoneNumber);
                        switch (nowItemPosition) {
                            case 0:
                                infot.setLocHandlerType(CallFilterConstants.FILTER_CALL_TYPE);
                                break;
                            case 1:
                                infot.setLocHandlerType(CallFilterConstants.AD_SALE_TYPE);
                                break;
                            case 2:
                                infot.setLocHandlerType(CallFilterConstants.CHEAT_NUM_TYPE);
                                break;
                            default:
                                break;
                        }
                        infost.add(infot);
                        cmp.addBlackList(infost, false);
                        notiUpdateBlackList();
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.mark_number_from_list), Toast.LENGTH_SHORT).show();
                        mDialogTooShort.dismiss();
                    }
                });
                mDialogTooShort.show();
                // 接听过 且 本地没有添加这个号码 而服务器有这个号码的数据
            } else if (mIsOffHook && info == null && filterTip != null && CallFilterConstants.DIALOG_TYPE[0] == filterTip[1] && serInfo != null
                    && CallFilterConstants.IS_TIP_DIA[1] == filterTip[0]) {
//                mIsOffHook = false;
                LeoLog.i(TAG, "idle : mIsOffHook =" + mIsOffHook + "ask marked");
                if (mDialogAskAddWithSmrMark != null && mDialogAskAddWithSmrMark.isShowing()) {
                    mIsOffHook = false;
                    return;
                }
                // 接听后挂断 询问是否家黑名单且展示标记人数
                mDialogAskAddWithSmrMark = CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(mPhoneNumber, mContext, true, 0, true);
                String summaryS = mContext.getResources().getString(R.string.call_filter_confirm_ask_mark_summary);
                String mark = mContext.getResources().getString(R.string.call_filter_black_list_tab);
                switch (filterTip[3]) {
                    case CallFilterConstants.FILTER_CALL_TYPE:
                        mark = mContext.getResources().getString(R.string.call_filter_mark_as_sr);
                        break;
                    case CallFilterConstants.AD_SALE_TYPE:
                        mark = mContext.getResources().getString(R.string.call_filter_mark_as_tx);
                        break;
                    case CallFilterConstants.CHEAT_NUM_TYPE:
                        mark = mContext.getResources().getString(R.string.call_filter_mark_as_zp);
                        break;
                    default:
                        break;
                }
                String summaryF = String.format(summaryS, filterTip[2], mark);
                mDialogAskAddWithSmrMark.setContent(summaryF);
                mDialogAskAddWithSmrMark.setRightBtnListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
                        BlackListInfo infot = new BlackListInfo();
                        infot.setLocHandlerType(CallFilterConstants.BLACK_LIST_TYP);
                        int nowItemPosition = mDialogAskAddWithSmrMark.getNowItemPosition();
                        infot.setNumber(mPhoneNumber);
                        switch (nowItemPosition) {
                            case 0:
                                infot.setLocHandlerType(CallFilterConstants.FILTER_CALL_TYPE);
                                break;
                            case 1:
                                infot.setLocHandlerType(CallFilterConstants.AD_SALE_TYPE);
                                break;
                            case 2:
                                infot.setLocHandlerType(CallFilterConstants.CHEAT_NUM_TYPE);
                                break;
                            default:
                                break;
                        }
                        infost.add(infot);
                        cmp.addBlackList(infost, false);
                        notiUpdateBlackList();
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.mark_number_from_list), Toast.LENGTH_SHORT).show();
                        mDialogAskAddWithSmrMark.dismiss();
                    }
                });
                mDialogAskAddWithSmrMark.show();
            } else if (mIsOffHook && filterTip != null && CallFilterConstants.DIALOG_TYPE[0] != filterTip[1] && info == null
                    && serInfo != null && CallFilterConstants.IS_TIP_DIA[1] == filterTip[0]) {
                LeoLog.i(TAG, "idle : mIsOffHook =" + mIsOffHook + "ask add to blacklist");
                if (mDialogAskAddWithSmr != null && mDialogAskAddWithSmr.isShowing()) {
                    mIsOffHook = false;
                    return;
                }
                // 接听后挂断 询问是否加入黑名单且展示加入黑名单人数
                mDialogAskAddWithSmr = CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(phoneNumber, mContext, true, 0, true);
                String summaryS = mContext.getResources().getString(R.string.call_filter_confirm_add_to_blacklist_summary);
                String summaryF = String.format(summaryS, filterTip[2]);
                mDialogAskAddWithSmr.setContent(summaryF);
                mDialogAskAddWithSmr.setRightBtnListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
                        BlackListInfo infot = new BlackListInfo();
                        int nowItemPosition = mDialogAskAddWithSmr.getNowItemPosition();
                        infot.setNumber(mPhoneNumber);
                        infot.setLocHandlerType(CallFilterConstants.BLACK_LIST_TYP);
                        switch (nowItemPosition) {
                            case 0:
                                infot.setLocHandlerType(CallFilterConstants.FILTER_CALL_TYPE);
                                break;
                            case 1:
                                infot.setLocHandlerType(CallFilterConstants.AD_SALE_TYPE);
                                break;
                            case 2:
                                infot.setLocHandlerType(CallFilterConstants.CHEAT_NUM_TYPE);
                                break;
                            default:
                                break;
                        }
                        infost.add(infot);
                        cmp.addBlackList(infost, false);
                        notiUpdateBlackList();
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.add_black_list_done), Toast.LENGTH_SHORT).show();
                        mDialogAskAddWithSmr.dismiss();
                    }
                });
                mDialogAskAddWithSmr.show();
                mIsOffHook = false;
            } else if (!mIsOffHook && info == null && filterTip != null && serInfo != null
                    && CallFilterConstants.IS_TIP_DIA[1] == filterTip[0]) {
                if ((mDialogAskAddWithSmr != null && mDialogAskAddWithSmr.isShowing()) || (mDialogAskAddWithSmrMark != null && mDialogAskAddWithSmrMark.isShowing())) {
                    mIsOffHook = false;
                    return;
                }
                // 没有接听就直接挂断的
                if (mDialogAskAdd != null && mDialogAskAdd.isShowing()) {
                    mIsOffHook = false;
                    return;
                }
                int tipType = filterTip[1];
                mDialogAskAdd = CallFIlterUIHelper.getInstance().getConfirmAddToBlacklistDialog(mContext, mPhoneNumber, String.valueOf(filterTip[2]));
                mDialogAskAdd.setRightBtnListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
                        BlackListInfo infot = new BlackListInfo();
                        infot.setNumber(mPhoneNumber);
                        infot.setLocHandlerType(CallFilterConstants.BLACK_LIST_TYP);
                        infost.add(infot);
                        cmp.addBlackList(infost, false);
                        notiUpdateBlackList();
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.add_black_list_done), Toast.LENGTH_SHORT).show();
                        mDialogAskAdd.dismiss();
                    }
                });
                mDialogAskAdd.show();
                mIsOffHook = false;
            }
        } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            mLastOffHookTime = System.currentTimeMillis();
            mIsOffHook = true;
            LeoLog.i(TAG, "offhook : mLastOffHookTime =" + mLastOffHookTime);
        }

        setCurrentCallTime(System.currentTimeMillis());
    }

    private void endCallAndRecord(final String phoneNumber, final ITelephony iTelephony,
                                  final CallFilterContextManager cmp) {
        /* 为本地黑名单：拦截 */
        try {
            try {
                if (iTelephony != null) {
                    iTelephony.endCall();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            LeoLog.i(TAG, "iTelephony endCall()");

            //记录当前拦截号码
            setFilterNum(phoneNumber);

            List<CallFilterInfo> infos = new ArrayList<CallFilterInfo>();
            CallFilterInfo callInfo = new CallFilterInfo();
            callInfo.setTimeLong(System.currentTimeMillis());
            callInfo.setNumber(PrivacyContactUtils.simpleFromateNumber(phoneNumber));
            callInfo.setCallType(CallLog.Calls.INCOMING_TYPE);
            callInfo.setReadState(CallFilterConstants.READ_NO);
            infos.add(callInfo);
            LeoLog.i(TAG, "add fiter detail ");
            boolean addState = cmp.addFilterDet(infos, false);
            if (addState) {
                int id = EventId.EVENT_LOAD_FIL_GR_ID;
                String msg = CallFilterConstants.EVENT_MSG_LOAD_FIL_GR;
                CommonEvent event = new CommonEvent(id, msg);
                LeoEventBus.getDefaultBus().post(event);

                // 已经上传的列表
                ThreadManager.executeOnAsyncThread(new Runnable() {
                    @Override
                    public void run() {
                        List<BlackListInfo> upBlack = cmp.getUploadBlackList();
                        if (upBlack != null && upBlack.size() > 0) {
                            String formateNum = PrivacyContactUtils
                                    .formatePhoneNumber(phoneNumber);
                            for (BlackListInfo black : upBlack) {
                                if (black.getNumber().contains(formateNum)) {
                                    black.setNumber(PrivacyContactUtils
                                            .simpleFromateNumber(phoneNumber));
                                    black.setFiltUpState(CallFilterConstants.FIL_UP);
                                    updateUpBlack(black);
                                    break;
                                }
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        CallFIlterUIHelper.getInstance().showReceiveCallNotification(phoneNumber);
        LeoLog.i("testdata", "show notification");
    }

    // 为dialog设置业务逻辑
//    private void fillDataForDialogAndShow(int[] filterTip, String summary) {
//        final MultiChoicesWitchSummaryDialog callHandleDialogWithSummary = CallFIlterUIHelper
//                .getInstance().getCallHandleDialogWithSummary(mPhoneNumber, mContext, true, 0);
//        String summaryS = mContext.getResources().getString(
//                R.string.call_filter_confirm_ask_mark_summary);
//        String mark = mContext.getResources().getString(R.string.call_filter_black_list_tab);
//        switch (filterTip[3]) {
//            case CallFilterConstants.FILTER_CALL_TYPE:
//                mark = mContext.getResources().getString(R.string.call_filter_mark_as_sr);
//                break;
//            case CallFilterConstants.AD_SALE_TYPE:
//                mark = mContext.getResources().getString(R.string.call_filter_mark_as_tx);
//                break;
//            case CallFilterConstants.CHEAT_NUM_TYPE:
//                mark = mContext.getResources().getString(R.string.call_filter_mark_as_zp);
//                break;
//            default:
//                break;
//        }
//        String summaryF = String.format(summaryS, filterTip[2], mark);
//        callHandleDialogWithSummary.setContent(summaryF);
//        callHandleDialogWithSummary.getListView().setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position,
//                                    long id) {
//                callHandleDialogWithSummary.setNowItemPosition(position);
//            }
//        });
//        callHandleDialogWithSummary.setRightBtnListener(new OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
//                BlackListInfo infot = new BlackListInfo();
//                int nowItemPosition = callHandleDialogWithSummary.getNowItemPosition();
//                infot.setNumber(mPhoneNumber);
//                switch (nowItemPosition) {
//                    case 0:
//                        infot.setLocHandlerType(CallFilterConstants.FILTER_CALL_TYPE);
//                        break;
//                    case 1:
//                        infot.setLocHandlerType(CallFilterConstants.AD_SALE_TYPE);
//                        break;
//                    case 2:
//                        infot.setLocHandlerType(CallFilterConstants.CHEAT_NUM_TYPE);
//                        break;
//                    default:
//                        break;
//                }
//                infost.add(infot);
//                CallFilterContextManager cmp = (CallFilterContextManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
//                cmp.addBlackList(infost, false);
//                Toast.makeText(mContext,
//                        mContext.getResources().getString(R.string.add_black_list_done),
//                        Toast.LENGTH_SHORT).show();
//                callHandleDialogWithSummary.dismiss();
//            }
//        });
//        callHandleDialogWithSummary.show();
//    }

    /**
     * 添加一条黑名单记录，仅包含号码和类型，并通知界面更新
     * @param type
     * @param number
     */
    private void addAnSimpleRecordsAndNotifyUpdate(int type, String number) {
        List<BlackListInfo> infost = new ArrayList<BlackListInfo>();
        BlackListInfo infot = new BlackListInfo();
        infot.setNumber(number);
        infot.setLocHandlerType(type);
        infost.add(infot);
        CallFilterContextManager cmp = (CallFilterContextManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        cmp.addBlackList(infost, false);
        notiUpdateBlackList();
    }


    /**
     * 加载本地黑名单列表
     */

    private synchronized void loadBlackList() {
        if (!mIsBlackLoad) {
            if (mBlackList != null) {
                mBlackList.clear();
            }
            /* 加载黑名单 */
            CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext
                    .getManager(MgrContext.MGR_CALL_FILTER);
            mBlackList = pm.getBlackList();
            // mIsBlackLoad = true;
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
        CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext
                .getManager(MgrContext.MGR_CALL_FILTER);
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
        CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext
                .getManager(MgrContext.MGR_CALL_FILTER);
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
     * 拦截数Observer处理
     */
    public synchronized void filterObserHandler() {
        removeSysFilterCall();
        filterNotiTipHandler();
        //恢复是否为来电后触发数据库
        setIsReceiver(false);
        setFilterNum(null);
    }

    /**
     * 陌生人多个来电通知处理
     */
    private void filterNotiTipHandler() {
        if (!isReceiver()) {
            return;
        }
        String selection = CallLog.Calls.TYPE + " = ? and " + CallLog.Calls.NEW + " = ? ";
        String[] selectionArgs = new String[]{
                String.valueOf(CallLog.Calls.MISSED_TYPE), String.valueOf(1)
        };

        ArrayList<ContactCallLog> callLogs = (ArrayList<ContactCallLog>) PrivacyContactUtils
                .getSysCallLog(mContext, selection, selectionArgs, null,true, false);
        List<ContactBean> contactsList = PrivacyContactUtils.getSysContact(mContext, null, null,
                false);
        List<ContactCallLog> straCalls = new ArrayList<ContactCallLog>();
        if (callLogs != null && callLogs.size() > 0) {
            for (ContactCallLog call : callLogs) {
                if (TextUtils.isEmpty(call.getCallLogNumber())) {
                    continue;
                }
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(call
                        .getCallLogNumber());
                boolean isExistContact = false;
                // 是否存在通讯录
                for (ContactBean contactBean : contactsList) {
                    String contactNumber = contactBean.getContactNumber();
                    if (TextUtils.isEmpty(contactNumber)) {
                        continue;
                    }
                    contactNumber = PrivacyContactUtils.simpleFromateNumber(contactNumber);
                    if (contactNumber.contains(formateNumber)) {
                        isExistContact = true;
                        break;
                    }
                }
                if (!isExistContact) {
                    // 是否存在黑名单
                    List<BlackListInfo> blackList = getBlackList();
                    if (blackList != null && blackList.size() > 0) {
                        for (BlackListInfo info : blackList) {
                            String number = info.getNumber();
                            if (TextUtils.isEmpty(number)) {
                                continue;
                            }
                            if (!number.contains(formateNumber)) {
                                straCalls.add(call);
                                break;
                            }
                        }
                    } else {
                        straCalls.add(call);
                    }
                }

            }
            int count = straCalls.size();
            if (count == 0) {
                return;
            }
            LeoLog.i(TAG, "陌生人未接来电个数：" + count);
            CallFilterContextManagerImpl pm = (CallFilterContextManagerImpl) MgrContext
                    .getManager(MgrContext.MGR_CALL_FILTER);
            int param = pm.getStraNotiTipParam();
            if (param <= 0) {
                return;
            }
            try {
                int remainder = count % param;
                if (remainder == 0) {
                    /* 多个未接来电为指定倍数通知提示 */
                    CallFIlterUIHelper.getInstance().showStrangerNotification(count);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        } else {
            return;
        }
    }

    private void removeSysFilterCall() {

        if (!isReceiver()) {
            return;
        }
        final String filterNum = getFilterNum();
        if (filterNum == null) {
            return;
        }
        final ContentResolver cr = mContext.getContentResolver();
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                try {
                    ContentResolver cr = mContext.getContentResolver();

                    //查询未读
                    String selectionQu = CallLog.Calls.TYPE + " = ? and " + CallLog.Calls.NEW + " = ? and "
                            + CallLog.Calls.NUMBER + " LIKE ? ";
                    String fromateNum = PrivacyContactUtils.formatePhoneNumber(filterNum);
                    String[] selectionArgsQu = new String[]{
                            String.valueOf(CallLog.Calls.MISSED_TYPE), String.valueOf(1), ("%" + fromateNum)
                    };
                    Cursor cur = cr.query(PrivacyContactUtils.CALL_LOG_URI, null, selectionQu, selectionArgsQu,
                            CallLog.Calls._ID + " " + CallFilterConstants.DESC);
                    int id = -1;
                    if (cur != null) {
                        while (cur.moveToFirst()) {
                            id = cur.getInt(cur.getColumnIndex(CallLog.Calls._ID));
                            break;
                        }
                    }
                    if (id < 0) {
                        return;
                    }
                    //删除未读
                    String selectionDe = CallLog.Calls._ID + " = ? ";
                    String[] selectionArgsDe = new String[]{String.valueOf(id)};
                    int count = PrivacyContactUtils.deleteDbLog(cr, PrivacyContactUtils.CALL_LOG_URI, selectionDe, selectionArgsDe);
                    LeoLog.i(TAG, count + ":id = " + id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 更新上传后的黑名单
     *
     * @param info
     */
    public void updateUpBlack(BlackListInfo info) {

        if (info == null) {
            return;
        }
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        String where = CallFilterConstants.BLACK_PHONE_NUMBER + " LIKE ? ";
        String[] selectionArgs = new String[]{
                "%" + PrivacyContactUtils.formatePhoneNumber(info.getNumber())
        };
        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CallFilterConstants.BLACK_PHONE_NUMBER, info.getNumber());
        values.put(CallFilterConstants.BLACK_FIL_UP, info.getFiltUpState());
        cr.update(uri, values, where, selectionArgs);
    }

    /**
     * 在黑名单列表时，通知黑名单列表刷新
     */
    public void notiUpdateBlackList() {
        int id = EventId.EVENT_LOAD_BLCAK_ID;
        String msg = CallFilterConstants.EVENT_MSG_LOAD_BLACK;
        CommonEvent event = new CommonEvent(id, msg);
        LeoEventBus.getDefaultBus().post(event);
    }

}
