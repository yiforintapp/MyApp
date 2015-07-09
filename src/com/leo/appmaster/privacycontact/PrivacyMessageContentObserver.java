
package com.leo.appmaster.privacycontact;

import java.text.SimpleDateFormat;
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
import android.util.Log;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.Utilities;

@SuppressLint("NewApi")
public class PrivacyMessageContentObserver extends ContentObserver {
    private Context mContext;
    public static String CALL_LOG_MODEL = "call_log_model";
    public static String MESSAGE_MODEL = "message_model";
    public static String CONTACT_MODEL = "contact_model";
    public static final String spaceString = "\u00A0";
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
        // Log.e(FloatWindowHelper.RUN_TAG, "监控短信");
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
            mLastMessage = pcm.getLastMessage();
            List<MessageBean> messages = null;
            if (mLastMessage != null) {
                try {
                    AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {

                        @Override
                        public void run() {
                            int count = cr.delete(
                                    PrivacyContactUtils.SMS_INBOXS,
                                    "address =  " + "\"" + mLastMessage.getPhoneNumber()
                                            + "\" and " + "body = \""
                                            + mLastMessage.getMessageBody() + "\"", null);
                        }
                    });
                } catch (Exception e) {
                }
                // 快捷手势隐私短信未读提醒
                noReadPrivacyMsmTipForQuickGesture(pref);
            }
            // 快捷手势未读短信提醒
            noReadMsmTipForQuickGesture(cr);
        } else if (CALL_LOG_MODEL.equals(mFlag)) {
            final ContactBean call = PrivacyContactManager.getInstance(mContext).getLastCall();
            if (call != null) {
                CallLogTask task = new CallLogTask();
                task.execute(call);
            }
            // 快捷手势未读通话记录提醒
            noReadCallForQuickGesture(call);
        }
    }

    private void noReadPrivacyMsmTipForQuickGesture(AppMasterPreference pref) {
        if (pref.getSwitchOpenPrivacyContactMessageTip() && pref.getQuickGestureMsmTip()) {
            QuickGestureManager.getInstance(mContext).isShowPrivacyMsm = true;
            QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage = true;
            FloatWindowHelper.removeShowReadTipWindow(mContext);
        }
    }

    private void noReadCallForQuickGesture(final ContactBean call) {
        AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {

            @Override
            public void run() {
                if (AppMasterPreference.getInstance(mContext).getSwitchOpenRecentlyContact()) {
                    String selection = Calls.TYPE + "=? and " + Calls.NEW + "=?";
                    String[] selectionArgs = new String[] {
                            String.valueOf(Calls.MISSED_TYPE), String.valueOf(1)
                    };
                    List<ContactCallLog> callLogs = PrivacyContactUtils
                            .getSysCallLog(mContext,
                                    mContext.getContentResolver(), selection,
                                    selectionArgs);
                    Iterator<ContactCallLog> ite = callLogs.iterator();
                    while (ite.hasNext()) {
                        if (call != null && !Utilities.isEmpty(call
                                .getContactNumber())) {
                            ContactCallLog contactCallLog = ite.next();
                            String formateLastCall = PrivacyContactUtils
                                    .formatePhoneNumber(call
                                            .getContactNumber());
                            String contactCallFromate = PrivacyContactUtils
                                    .formatePhoneNumber(contactCallLog.getCallLogNumber());
                            if (formateLastCall.equals(contactCallFromate)) {
                                callLogs.remove(contactCallLog);
                            }
                        } else {
                            break;
                        }
                    }
                    // 查看通话记录时，清除未读操作（包括第三方，或者系统自带通话记录列表查看）
                    /*
                     * deleteCallLogDatebaseFlag=true
                     * 时，则说明是隐私联系人在拦截通话记录时做的删除操作引起的数据库变化，所以不去做下面的操作
                     */
                    if ((callLogs == null || callLogs.size() <= 0)
                            && !PrivacyContactManager.getInstance(mContext).deleteCallLogDatebaseFlag) {
                        /*
                         * 全部已读，去除热区红点
                         */
                        // 判断隐私联系人，短信，运营是否还有显示红点的需求，没有了将红点标志设为false
                        // Log.e(Constants.RUN_TAG,
                        // "未读短信："+QuickGestureManager.getInstance(mContext).mMessages.size());
                        if ((QuickGestureManager.getInstance(mContext).mMessages == null || QuickGestureManager
                                .getInstance(mContext).mMessages.size() <= 0)/* 未读短信 */
                                && AppMasterPreference.getInstance(mContext)
                                        .getCallLogNoReadCount() <= 0/* 隐私通话 */
                                && AppMasterPreference.getInstance(mContext)
                                        .getMessageNoReadCount() <= 0/* 隐私短信 */
                                && AppMasterPreference.getInstance(mContext)
                                        .getLastBusinessRedTipShow()/* 运营 */) {
                            QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage = false;
                        }
                        if (QuickGestureManager.getInstance(mContext).mCallLogs != null) {
                            QuickGestureManager.getInstance(mContext).mCallLogs.clear();
                        }
                        FloatWindowHelper.removeShowReadTipWindow(mContext);
                    } else {
                        if (PrivacyContactManager.getInstance(mContext).deleteCallLogDatebaseFlag) {
                            // Log.e(FloatWindowHelper.RUN_TAG, "隐私删除时赋值");
                            // QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                            // = true;
                        }
                    }
                    // 有未读通话时操作
                    if (callLogs != null && callLogs.size() > 0) {
                        // Log.e(FloatWindowHelper.RUN_TAG, "未读数量数量：" +
                        // callLogs.size());
                        if (AppMasterPreference.getInstance(mContext)
                                .getSwitchOpenRecentlyContact()) {
                            if (!QuickGestureManager.getInstance(mContext).mToCallFlag
                                    && !QuickGestureManager.getInstance(mContext).isCallLogRead) {
                                QuickGestureManager.getInstance(mContext).mCallLogs = callLogs;
                                QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage = true;
                                FloatWindowHelper.removeShowReadTipWindow(mContext);
                                if (PrivacyContactManager.getInstance(mContext).deleteCallLogDatebaseFlag) {
                                    PrivacyContactManager.getInstance(mContext).deleteCallLogDatebaseFlag = false;
                                }
                            } else {
                                QuickGestureManager.getInstance(mContext).mToCallFlag = false;
                            }
                        }
                    }
                }
            }
        });
    }

    private void noReadMsmTipForQuickGesture(final ContentResolver cr) {
        AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
            @Override
            public void run() {
                if (AppMasterPreference.getInstance(mContext).getSwitchOpenNoReadMessageTip()) {
                    List<MessageBean> messages = PrivacyContactUtils
                            .getSysMessage(mContext, cr,
                                    "read=0 AND type=1", null, false);
                    if (messages != null && messages.size() > 0) {
                        ContactBean contact = PrivacyContactManager.getInstance(mContext)
                                .getLastMessageContact();
                        Iterator<MessageBean> ite = messages.iterator();
                        while (ite.hasNext()) {
                            if (contact != null
                                    && !Utilities.isEmpty(contact.getContactNumber())) {
                                MessageBean message = ite.next();
                                String formateLastCall = PrivacyContactUtils
                                        .formatePhoneNumber(contact.getContactNumber());
                                String contactCallFromate = PrivacyContactUtils
                                        .formatePhoneNumber(message.getPhoneNumber());
                                if (formateLastCall.equals(contactCallFromate)) {
                                    //过略掉隐私联系人，留下非隐私联系人
                                    messages.remove(message);
                                }
                            } else {
                                break;
                            }
                        }
                        /*
                         * 用于解决MIUI系统无法接收系统短信广播恢复isMessageReadRedTip的默认值，用此来恢复
                         * 目前解决方法
                         * ：每次去记录上次未读数量，用当前未读数量与上次相比如果大于则有新的未读，如果小于则读取了一些未读
                         * ，如果等于则没有未读被读
                         */
                        restoreRedTipValueForMIUI(cr);
                    }
                    // 查看未读短信时，清除未读操作（包括第三方，或者系统自带短信列表查看）
                    if (messages == null
                            || messages.size() <= 0
                            && !PrivacyContactManager.getInstance(mContext).deleteMsmDatebaseFlag) {
                        /*
                         * 全部已读，去除热区红点
                         */
                        if ((QuickGestureManager.getInstance(mContext).mCallLogs == null || QuickGestureManager
                                .getInstance(mContext).mCallLogs.size() <= 0)/* 未读通话 */
                                && AppMasterPreference.getInstance(mContext)
                                        .getCallLogNoReadCount() <= 0/* 隐私通话 */
                                && AppMasterPreference.getInstance(mContext)
                                        .getMessageNoReadCount() <= 0/* 隐私短信 */
                                && AppMasterPreference.getInstance(mContext)
                                        .getLastBusinessRedTipShow()/* 运营 */) {
                            // Log.e(Constants.RUN_TAG, "设置短信显示");
                            QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage = false;
                        }
                        if (QuickGestureManager.getInstance(mContext).mMessages != null) {
                            QuickGestureManager.getInstance(mContext).mMessages.clear();
                        }
                        FloatWindowHelper.removeShowReadTipWindow(mContext);
                    } else {
                        if (PrivacyContactManager.getInstance(mContext).deleteMsmDatebaseFlag) {
                            // Log.e(FloatWindowHelper.RUN_TAG, "隐私联系人赋值");
                            // QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage
                            // = true;
                        }

                    }
                    // 有未读短信时操作
                    if (messages != null && messages.size() > 0) {
                        if (AppMasterPreference.getInstance(mContext)
                                .getSwitchOpenNoReadMessageTip()) {
                            /*
                             * QuickGestureManager.getInstance(mContext).
                             * isMessageRead
                             * =false时，说明该短信没有经过红点提示，如果为true说明该短信经过红点提示并且提示已经打开
                             */
                            // Log.e(Constants.RUN_TAG,
                            // "操作未读短信"
                            // +
                            // QuickGestureManager.getInstance(mContext).isMessageReadRedTip);
                            if (!QuickGestureManager.getInstance(mContext).mToMsmFlag
                                    && !QuickGestureManager.getInstance(mContext).isMessageReadRedTip) {
                                // Log.e(Constants.RUN_TAG, "进来");
                                QuickGestureManager.getInstance(mContext).mMessages = messages;
                                QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage = true;
                                FloatWindowHelper.removeShowReadTipWindow(mContext);
                            } else {
                                QuickGestureManager.getInstance(mContext).mToMsmFlag = false;
                            }
                        }
                    }
                }
            }

            private void restoreRedTipValueForMIUI(final ContentResolver cr) {
                if (BuildProperties.isMIUI()) {
                    List<MessageBean> messageList = PrivacyContactUtils
                            .getSysMessage(mContext, cr,
                                    "read=0 AND type=1", null, true);
                    if (messageList != null) {
                        if (messageList.size() > PrivacyContactManager
                                .getInstance(mContext).messageSize) {
                            if (QuickGestureManager.getInstance(mContext).isMessageReadRedTip) {
                                QuickGestureManager.getInstance(mContext).isMessageReadRedTip = false;
                                AppMasterPreference.getInstance(mContext)
                                        .setMessageIsRedTip(false);
                            }
                        }
                        PrivacyContactManager.getInstance(mContext).messageSize = messageList
                                .size();
                    }
                }
            }
        });
    }

    // 通话记录异步处理
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
                        "number LIKE  ? ", new String[] {
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
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    final String number = cursor.getString(cursor.getColumnIndex("number"));
                    String name = call.getContactName();
                    cursor.getString(cursor.getColumnIndex("duration"));
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
                            int count = pre.getMessageNoReadCount();
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
                    AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
                        @Override
                        public void run() {
                            int count = PrivacyContactUtils.deleteCallLogFromSystem(
                                    "number LIKE ?",
                                    number,
                                    mContext);
                            if (count > 0) {
                                // 过滤上面监控通话记录数据库，隐私联系人删除未接来电记录时引发数据库变化而做的操作（要在执行删除操作之前去赋值）
                                PrivacyContactManager.getInstance(mContext).deleteCallLogDatebaseFlag = true;
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
                                new MessagePrivacyReceiver().callLogNotification(mContext);
                            }
                        }
                    } else if (call.getAnswerType() == 0) {
                        if (CallLog.Calls.OUTGOING_TYPE != type) {
                            new MessagePrivacyReceiver().callLogNotification(mContext);
                        }
                    }
                }
                cursor.close();
                // 快捷手势隐私通话未读提示
                noReadCallPrivacyCallTipForQuickGesture();

            }
        }

        private void noReadCallPrivacyCallTipForQuickGesture() {
            AppMasterPreference mPreference = AppMasterPreference.getInstance(mContext);
            // AppMasterPreference.getInstance(mContext).setQuickGestureCallLogTip(true);
            if (mPreference.getSwitchOpenPrivacyContactMessageTip()
                    && mPreference.getQuickGestureCallLogTip()) {
                QuickGestureManager.getInstance(mContext).isShowPrivacyCallLog = true;
                QuickGestureManager.getInstance(mContext).isShowSysNoReadMessage = true;
                FloatWindowHelper.removeShowReadTipWindow(mContext);
                if (PrivacyContactManager.getInstance(mContext).deleteCallLogDatebaseFlag) {
                    PrivacyContactManager.getInstance(mContext).deleteCallLogDatebaseFlag = false;
                }
            }
        }
    }
}
