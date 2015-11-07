
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
    public static final String spaceString = "\u00A0";
    private String mFlag;
    private MessageBean mLastMessage;
    private OnMessageObserverListener mPrivacyMessageListener;
    private OnPrivacyCallListener mPrivacyCallListener;


    public PrivacyMessageContentObserver(Handler handler) {
        super(handler);
    }

    public PrivacyMessageContentObserver(Context context, Handler handler, String flag) {
        super(handler);
        mContext = context;
        this.mFlag = flag;
    }

    public void setOnMessageObserverListener(OnMessageObserverListener messageListener) {
        mPrivacyMessageListener = messageListener;
    }

    public void setOnPrivacyCallListener(OnPrivacyCallListener privacyCallListener) {
        mPrivacyCallListener = privacyCallListener;
    }

    /*隐私短信回调接口*/
    public interface OnMessageObserverListener {
        public boolean onMessageObserverListener();
    }

    /*隐私通话回调接口*/
    public interface OnPrivacyCallListener {
        public boolean onPrivacyCall();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (DBG) {
            /* 测试打印系统据库变化情况 */
            printTestObserverLog();
        }
        int privateContacts = PrivacyContactManager.getInstance(mContext).getPrivacyContactsCount();
        AppMasterPreference pref = AppMasterPreference.getInstance(mContext);
        boolean isOpenNoReadMessageTip = pref.getSwitchOpenNoReadMessageTip();
        boolean isOpenNoReadCallLogTip = pref.getSwitchOpenRecentlyContact();
        if (privateContacts == 0 && !isOpenNoReadMessageTip && !isOpenNoReadCallLogTip) {
            return;
        }
        final ContentResolver cr = mContext.getContentResolver();
        PrivacyContactManager pcm = PrivacyContactManager.getInstance(mContext);
        if (MESSAGE_MODEL.equals(mFlag)) {
             /*手机防盗功能处理:防止手机防盗号码为隐私联系人时拦截掉放在最前面*/
            PhoneSecurityManager.getInstance(mContext).securityPhoneOberserHandler();
            mLastMessage = pcm.getLastMessage();
            if (mLastMessage != null) {
                try {
                    ThreadManager.executeOnAsyncThread(new Runnable() {

                        @Override
                        public void run() {
                            int count = cr.delete(
                                    PrivacyContactUtils.SMS_INBOXS,
                                    "address =  " + "\"" + mLastMessage.getPhoneNumber()
                                            + "\" and " + "body = \""
                                            + mLastMessage.getMessageBody() + "\"", null);
                            if (count > 0) {
                                LeoLog.i(TAG, "在监控短信时，删除系统短信成功！！");
                            }
                        }
                    });
                } catch (Exception e) {
                }
            }
            if (mPrivacyMessageListener != null) {
                mPrivacyMessageListener.onMessageObserverListener();
            }
            /* 快捷手势未读短信提醒 */
            noReadMsmTipForQuickGesture(cr);
        } else if (CALL_LOG_MODEL.equals(mFlag)) {
            final ContactBean call = PrivacyContactManager.getInstance(mContext).getLastCall();
            if (call != null) {
                CallLogTask task = new CallLogTask();
                task.execute(call);
            }
            /* 快捷手势未读通话记录提醒 */
            noReadCallForQuickGesture(call);
        }
        boolean flag = PrivacyContactManager.getInstance(mContext).testValue;
        if (!flag) {
            if (DBG) {
                LeoLog.i(TAG, "onReceive没有执行！！！");
            }
        } else {
            if (DBG) {
                LeoLog.i(TAG, "onReceive触发执行了！！！！");
            }
            PrivacyContactManager.getInstance(mContext).testValue = false;
        }
    }


    /* 测试来新短信或者来电能否触发系统数据库变化 */
    private void printTestObserverLog() {
        if (MESSAGE_MODEL.equals(mFlag)) {
            LeoLog.e(Constants.RUN_TAG, "短信变化引起系统短信数据库改变");
        } else if (CALL_LOG_MODEL.equals(mFlag)) {
            LeoLog.e(Constants.RUN_TAG, "有来电引起系统通话数据库改变");
        }
    }

    private void filterPrivacyContactMsm(final ContentResolver cr) {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                List<MessageBean> messages = PrivacyContactUtils
                        .getSysMessage(mContext, "read=0 AND type=1", null, false,false);
                if (messages != null && messages.size() > 0) {
                    ContactBean contact = PrivacyContactManager.getInstance(mContext)
                            .getLastMessageContact();
                    Iterator<MessageBean> iter = messages.iterator();
                    while (iter.hasNext()) {
                        if (contact != null
                                && !Utilities.isEmpty(contact.getContactNumber())) {
                            String number = contact.getContactNumber();
                            /* 查询短信中未读的号码是否为隐私联系人 */
                            ContactBean privacyContact = MessagePrivacyReceiver.getPrivateMessage(
                                    number,
                                    mContext);
                            if (privacyContact != null) {
                                /* 该联系人的所有未读短信添加到到隐私列表 */
                                /* 删除所有的短信 */
                                PrivacyContactUtils.deleteMessageFromSystemSMS("address = ?",
                                        new String[]{
                                                number
                                        }, mContext);
                                // 过滤监控短信记录数据库，隐私联系人删除未读短信记录时引发数据库变化而做的操作（要在执行删除操作之前去赋值）
                                // PrivacyContactManager.getInstance(mContext).deleteMsmDatebaseFlag
                                // = true;
                            }
                        }
                    }
                }
            }
        });
    }

    private void noReadCallForQuickGesture(final ContactBean call) {
        ThreadManager.executeOnAsyncThread(new Runnable() {

            @Override
            public void run() {
                if (AppMasterPreference.getInstance(mContext).getSwitchOpenRecentlyContact()) {
                    String selection = Calls.TYPE + "=? and " + Calls.NEW + "=?";
                    String[] selectionArgs = new String[]{
                            String.valueOf(Calls.MISSED_TYPE), String.valueOf(1)
                    };
                    ArrayList<ContactCallLog> callLogs = (ArrayList<ContactCallLog>) PrivacyContactUtils
                            .getSysCallLog(mContext, selection,
                                    selectionArgs,false,false);
                    ArrayList<ContactCallLog> cloneCallLog = (ArrayList<ContactCallLog>) callLogs
                            .clone();
                    if (cloneCallLog != null && cloneCallLog.size() > 0) {
                        if (call != null && !Utilities.isEmpty(call
                                .getContactNumber())) {
                            for (ContactCallLog contactCallLog : cloneCallLog) {
                                String formateLastCall = PrivacyContactUtils
                                        .formatePhoneNumber(call
                                                .getContactNumber());
                                String contactCallFromate = PrivacyContactUtils
                                        .formatePhoneNumber(contactCallLog.getCallLogNumber());
                                if (formateLastCall.equals(contactCallFromate)) {
                                    callLogs.remove(contactCallLog);
                                }
                            }
                        }
                        /**
                         * 用于解决系统无法接收系统来电广播恢复isCallLogRead的默认值，用此来恢复 目前解决方法
                         * ：每次去记录上次未读数量，用当前未读数量与上次相比如果大于则有新的未读，如果小于则读取了一些未读
                         * ，如果等于则没有未读被读
                         */
                        restoreRedTipValueForCall();
                    }
                }
            }
        });
    }

    private void noReadMsmTipForQuickGesture(final ContentResolver cr) {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                if (AppMasterPreference.getInstance(mContext).getSwitchOpenNoReadMessageTip()) {
                    ArrayList<MessageBean> messages = (ArrayList<MessageBean>) PrivacyContactUtils
                            .getSysMessage(mContext, "read=0 AND type=1", null, false,false);
                    ArrayList<MessageBean> cloneMessage = (ArrayList<MessageBean>) messages.clone();
                    if (cloneMessage != null && cloneMessage.size() > 0) {
                        ContactBean contact = PrivacyContactManager.getInstance(mContext)
                                .getLastMessageContact();
                        if (contact != null
                                && !Utilities.isEmpty(contact.getContactNumber())) {
                            for (MessageBean message : cloneMessage) {
                                String formateLastMessage = PrivacyContactUtils
                                        .formatePhoneNumber(contact.getContactNumber());
                                String contactMessageFromate = PrivacyContactUtils
                                        .formatePhoneNumber(message.getPhoneNumber());
                                if (formateLastMessage.equals(contactMessageFromate)) {
                                    /* 过略掉隐私联系人，留下非隐私联系人 */
                                    messages.remove(message);
                                }
                            }
                        }
                        /**
                         * 用于解决系统无法接收系统短信广播恢复isMessageReadRedTip的默认值，用此来恢复
                         * 目前解决方法
                         * ：每次去记录上次未读数量，用当前未读数量与上次相比如果大于则有新的未读，如果小于则读取了一些未读
                         * ，如果等于则没有未读被读
                         */
                        restoreRedTipValueForMsm();
                    }
                }
            }

            private void restoreRedTipValueForMsm() {
                if (PrivacyContactManager.getInstance(mContext).checkPhoneModelForRestoreRedTip()) {
                    List<MessageBean> messageList = PrivacyContactUtils
                            .getSysMessage(mContext, "read=0 AND type=1", null, true,false);
                    if (messageList != null) {
                        int currentCount = messageList.size();
                        PrivacyContactManager.getInstance(mContext).messageSize = currentCount;
                    }
                }
            }
        });
    }

    private void restoreRedTipValueForCall() {
        if (PrivacyContactManager.getInstance(mContext).checkPhoneModelForCallRestoreRedTip()) {
            String selection = Calls.TYPE + "=? and " + Calls.NEW + "=?";
            String[] selectionArgs = new String[]{
                    String.valueOf(Calls.MISSED_TYPE), String.valueOf(1)
            };
            ArrayList<ContactCallLog> callLogs = (ArrayList<ContactCallLog>) PrivacyContactUtils
                    .getSysCallLog(mContext, selection, selectionArgs,true,true);
            if (callLogs != null) {
                int currentCount = callLogs.size();
                PrivacyContactManager.getInstance(mContext).mUnCalls = currentCount;
            }
        }
    }

    /* 通话记录异步处理 */
    class CallLogTask extends AsyncTask<ContactBean, ContactBean, Cursor> {
        private ContactBean call;
        private Cursor cursor = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Cursor doInBackground(ContactBean... arg0) {
            call = arg0[0];
            ContentResolver cr = mContext.getContentResolver();
            String formatNumber = PrivacyContactUtils.formatePhoneNumber(call
                    .getContactNumber());
            try {
                cursor = cr.query(PrivacyContactUtils.CALL_LOG_URI, null,
                        "number LIKE  ? ", new String[]{
                                "%" + formatNumber
                        }, null);
            } catch (Exception e) {
            } catch (Error error) {
                // AM-1824, No such method error
            }

            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            String numberToIswipe = null;
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        final String number = cursor.getString(cursor.getColumnIndex("number"));
                        numberToIswipe = number;
                        String name = call.getContactName();
                        Date date = new Date(Long.parseLong(cursor.getString(cursor
                                .getColumnIndex(CallLog.Calls.DATE))));
                        SimpleDateFormat sfd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        String time = sfd.format(date);
                        int type = (cursor
                                .getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)));
                        ContentValues values = new ContentValues();
                        values.put(Constants.COLUMN_CALL_LOG_PHONE_NUMBER, number);
                        if (!"".equals(name) && name != null) {
                            values.put(Constants.COLUMN_CALL_LOG_CONTACT_NAME, name);
                        } else {
                            if (call.getContactName() != null
                                    && !"".equals(call.getContactName())) {
                                values.put(Constants.COLUMN_CALL_LOG_CONTACT_NAME,
                                        call.getContactName());
                            } else {
                                values.put(Constants.COLUMN_CALL_LOG_CONTACT_NAME, number);
                            }
                        }
                        values.put(Constants.COLUMN_CALL_LOG_DATE, time);
                        values.put(Constants.COLUMN_CALL_LOG_TYPE, type);
                        values.put(Constants.COLUMN_CALL_LOG_DURATION,cursor.getString(cursor.getColumnIndex("duration")));

                        if (call.getAnswerType() == 1) {
                            if (CallLog.Calls.OUTGOING_TYPE == type
                                    || CallLog.Calls.MISSED_TYPE != type) {
                                values.put(Constants.COLUMN_CALL_LOG_IS_READ, 1);
                            } else {
                                values.put(Constants.COLUMN_CALL_LOG_IS_READ, 0);
                            }
                        } else if (call.getAnswerType() == 0) {
                            if (CallLog.Calls.OUTGOING_TYPE == type) {
                                values.put(Constants.COLUMN_CALL_LOG_IS_READ, 1);
                            } else {
                                values.put(Constants.COLUMN_CALL_LOG_IS_READ, 0);
                            }
                        }
                        // 保存记录
                        mContext.getContentResolver().insert(Constants.PRIVACY_CALL_LOG_URI, values);
                        if (call.getAnswerType() == 1) {
                            if (CallLog.Calls.OUTGOING_TYPE != type) {
                                if (CallLog.Calls.MISSED_TYPE == type) {
                                    AppMasterPreference pre = AppMasterPreference
                                            .getInstance(mContext);
                                    int count = pre.getCallLogNoReadCount();
                                    if (count > 0) {
                                        pre.setCallLogNoReadCount(count + 1);
                                    } else {
                                        pre.setCallLogNoReadCount(1);
                                    }
                                    LeoEventBus
                                            .getDefaultBus()
                                            .post(
                                                    new PrivacyEditFloatEvent(
                                                            PrivacyContactUtils.PRIVACY_RECEIVER_CALL_LOG_NOTIFICATION));
                                }
                            }
                        } else if (call.getAnswerType() == 0) {
                            if (CallLog.Calls.OUTGOING_TYPE != type) {
                                AppMasterPreference pre = AppMasterPreference
                                        .getInstance(mContext);
                                int count = pre.getCallLogNoReadCount();
                                if (count > 0) {
                                    pre.setCallLogNoReadCount(count + 1);
                                } else {
                                    pre.setCallLogNoReadCount(1);
                                }
                                LeoEventBus
                                        .getDefaultBus()
                                        .post(
                                                new PrivacyEditFloatEvent(
                                                        PrivacyContactUtils.PRIVACY_RECEIVER_CALL_LOG_NOTIFICATION));
                            }
                        }
                        // 删除系统记录
                        ThreadManager.executeOnAsyncThread(new Runnable() {
                            @Override
                            public void run() {
                                int count = PrivacyContactUtils.deleteCallLogFromSystem(
                                        "number LIKE ?",
                                        number,
                                        mContext);
                                if (count > 0) {
                                    // 过滤上面监控通话记录数据库，隐私联系人删除未接来电记录时引发数据库变化而做的操作（要在执行删除操作之前去赋值）
                                    PrivacyContactManager.getInstance(mContext).deleteCallLogDatebaseFlag = true;
                                    LeoLog.i(TAG, "在监控通话时，删除系统通话记录成功！！！");
                                }
                            }
                        });
                        AppMasterPreference.getInstance(mContext).setQuickGestureCallLogTip(true);
                        // 通知更新通话记录
                        LeoEventBus
                                .getDefaultBus()
                                .post(
                                        new PrivacyEditFloatEvent(
                                                PrivacyContactUtils.PRIVACY_ALL_CALL_NOTIFICATION_HANG_UP));
                        // 发送通知
                        if (call.getAnswerType() == 1) {
                            if (CallLog.Calls.OUTGOING_TYPE != type) {
                                if (CallLog.Calls.MISSED_TYPE == type) {
                                    new MessagePrivacyReceiver().callLogNotification(mContext,
                                            numberToIswipe);
                                }
                            }
                        } else if (call.getAnswerType() == 0) {
                            if (CallLog.Calls.OUTGOING_TYPE != type) {
                                new MessagePrivacyReceiver().callLogNotification(mContext,
                                        numberToIswipe);
                            }
                        }
                    }
                } catch (Exception e) {
                    
                } finally {
                    cursor.close();
                }

                // 快捷手势隐私通话未读提示
                noReadCallPrivacyCallTipForQuickGesture();
                if (mPrivacyCallListener != null) {
                    mPrivacyCallListener.onPrivacyCall();
                }
            }
        }

        private void noReadCallPrivacyCallTipForQuickGesture() {
            AppMasterPreference mPreference = AppMasterPreference.getInstance(mContext);
            // AppMasterPreference.getInstance(mContext).setQuickGestureCallLogTip(true);
            if (mPreference.getSwitchOpenPrivacyContactMessageTip()
                    && mPreference.getQuickGestureCallLogTip()) {
                if (PrivacyContactManager.getInstance(mContext).deleteCallLogDatebaseFlag) {
                    PrivacyContactManager.getInstance(mContext).deleteCallLogDatebaseFlag = false;
                }
            }
        }
    }
}
