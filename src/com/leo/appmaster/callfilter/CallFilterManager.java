package com.leo.appmaster.callfilter;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.mgr.CallFilterContextManager;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.utils.LeoLog;

/**
 * Created by runlee on 15-12-18.
 */
public class CallFilterManager {
    private static CallFilterManager mInstance;
    private Context mContext;
    private CallFilterContextManager mCFCManager;
    private long mLastOffHookTime = 0;
    /**
     * 拨出电话
     */
    private boolean mIsComingOut = false;

    public boolean isComingOut() {
        return mIsComingOut;
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

    public void filterCallHandler(String action,String phoneNumber,String state) {
        mCFCManager = (CallFilterContextManager) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
        if (PrivacyContactUtils.NEW_OUTGOING_CALL.equals(action)) {
                /*拨出*/
            CallFilterManager.getInstance(mContext).setIsComingOut(true);
            LeoLog.i("PrivacyContactReceiver", "拨打电话");
        } else {
                /*1.来电，2.无状态*/
            LeoLog.i("PrivacyContactReceiver", "来电电话");
            final CallFilterToast toast = CallFilterToast.makeText(mContext, "13632840685", "已被1234人拉入", "黑名单");
            boolean isComOut = CallFilterManager.getInstance(mContext).isComingOut();
            if (!isComOut) {
                toast.show();
            }
            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {

            } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
                CallFilterManager.getInstance(mContext).setIsComingOut(false);
                toast.hide();
                //挂断后，判断当前时间和之前接听的时间的差值，小于配置的判定时间则在挂断后弹出对话框
                if (System.currentTimeMillis() - mLastOffHookTime < 1000) {
                    CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(phoneNumber, AppMasterApplication.getInstance(), true, 0).show();
                }
            } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                mLastOffHookTime = System.currentTimeMillis();
            }
        }
        //定义接听状态的监听
//            PhoneStateListener listener = new PhoneStateListener() {
//                @Override
//                public void onCallStateChanged(int state, String incomingNumber) {
//                    super.onCallStateChanged(state, incomingNumber);
//                    switch (state) {
//                        case TelephonyManager.CALL_STATE_IDLE:
//                            CallFilterManager.getInstance(mContext).setIsComingOut(false);
//                            toast.hide();
//                            //挂断后，判断当前时间和之前接听的时间的差值，小于配置的判定时间则在挂断后弹出对话框
//                            if (System.currentTimeMillis() - mLastOffHookTime < 1000) {
//                                LeoLog.i("temp", System.currentTimeMillis() - mLastOffHookTime + " vs max :" + mCFCManager.getCallDurationMax());
//                                CallFIlterUIHelper.getInstance().getCallHandleDialogWithSummary(phoneNumber, AppMasterApplication.getInstance(), true, 0).show();
//                            }
//                            break;
//                        case TelephonyManager.CALL_STATE_OFFHOOK:
//                            mLastOffHookTime = System.currentTimeMillis();
//                            break;
//                        case TelephonyManager.CALL_STATE_RINGING:
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            };
//            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

    }
}
