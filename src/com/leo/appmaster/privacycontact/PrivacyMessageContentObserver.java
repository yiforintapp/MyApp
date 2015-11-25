
package com.leo.appmaster.privacycontact;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.CallLog.Calls;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.phoneSecurity.PhoneSecurityManager;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

@SuppressLint("NewApi")
public class PrivacyMessageContentObserver extends ContentObserver {
    private static final String TAG = "PrivacyMessageContentObserver";
    private static final boolean DBG = false;
    private Context mContext;
    public static String CALL_LOG_MODEL = "call_log_model";
    public static String MESSAGE_MODEL = "message_model";
    public static String CONTACT_MODEL = "contact_model";
    public static final String SPACE_STRING = "\u00A0";
    private String mFlag;
    private MessageBean mLastMessage;

    public PrivacyMessageContentObserver(Context context, Handler handler, String flag) {
        super(handler);
        mContext = context;
        this.mFlag = flag;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (DBG) {
            /* 测试打印系统据库变化情况 */
            printTestObserverLog();
        }
        final ContentResolver cr = mContext.getContentResolver();
        PrivacyContactManager pcm = PrivacyContactManager.getInstance(mContext);
        if (MESSAGE_MODEL.equals(mFlag)) {
             /*手机防盗功能处理:防止手机防盗号码为隐私联系人时拦截掉放在最前面*/
            PhoneSecurityManager.getInstance(mContext).securityPhoneOberserHandler();
            mLastMessage = pcm.getLastMessage();
            if (mLastMessage != null) {
               /*4.4以上去做短信操作*/
                boolean isLessLeve19 = PrivacyContactUtils.isLessApiLeve19();
                if (!isLessLeve19) {
                    return;
                }
                try {
                    ThreadManager.executeOnAsyncThread(new Runnable() {

                        @Override
                        public void run() {
                            String where = "address =  " + "\""
                                    + mLastMessage.getPhoneNumber()
                                    + "\" and " + "body = \""
                                    + mLastMessage.getMessageBody()
                                    + "\"";
                            Uri uri = PrivacyContactUtils.SMS_INBOXS;
                            cr.delete(uri, "address =  " + "\"" + where, null);
                        }
                    });
                } catch (Exception e) {
                }
            }
            /* 快捷手势未读短信提醒 */
            PrivacyContactManager.getInstance(mContext).noReadMsmTipForQuickGesture();
        } else if (CALL_LOG_MODEL.equals(mFlag)) {
            final ContactBean call = PrivacyContactManager.getInstance(mContext).getLastCall();
            if (call != null) {
                PrivacyCallAsyncTask task = new PrivacyCallAsyncTask(mContext);
                task.execute(call);
            }
            /* 快捷手势未读通话记录提醒 */
            PrivacyContactManager.getInstance(mContext).noReadCallForQuickGesture(call);
        }
    }


    /* 测试来新短信或者来电能否触发系统数据库变化 */
    private void printTestObserverLog() {
        if (MESSAGE_MODEL.equals(mFlag)) {
            LeoLog.i(Constants.RUN_TAG, "短信变化引起系统短信数据库改变!");
        } else if (CALL_LOG_MODEL.equals(mFlag)) {
            LeoLog.i(Constants.RUN_TAG, "有来电引起系统通话数据库改变!");
        }

        boolean flag = PrivacyContactManager.getInstance(mContext).testValue;
        if (!flag) {
            if (DBG) {
                LeoLog.d(TAG, "onReceive没有执行！");
            }
        } else {
            if (DBG) {
                LeoLog.d(TAG, "onReceive触发执行了!");
            }
            PrivacyContactManager.getInstance(mContext).testValue = false;
        }
    }
}
