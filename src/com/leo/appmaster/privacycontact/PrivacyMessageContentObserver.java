
package com.leo.appmaster.privacycontact;

import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyDeletEditEvent;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;

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
            if (mLastMessage != null) {
                try {
                    AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {

                        @Override
                        public void run() {
                            cr.delete(
                                    PrivacyContactUtils.SMS_INBOXS,
                                    "address =  " + "\"" + mLastMessage.getPhoneNumber()
                                            + "\" and " + "body = \""
                                            + mLastMessage.getMessageBody() + "\"", null);
                        }
                    });
                } catch (Exception e) {
                }
            }

            // 快捷手势未读短信提醒
            AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
                @Override
                public void run() {
                    if (AppMasterPreference.getInstance(mContext).getSwitchOpenNoReadMessageTip()) {
                        List<MessageBean> messages = PrivacyContactUtils
                                .getSysMessage(mContext, cr,
                                        "read=0 AND type=1", null, false);
                        if (messages != null && messages.size() > 0) {
                            QuickGestureManager.getInstance(mContext).mMessages = messages;
                            LockManager.getInstatnce().isShowSysNoReadMessage = true;
                            FloatWindowHelper.removeShowReadTipWindow(mContext);
                        }
                    }
                }
            });
        } else if (CALL_LOG_MODEL.equals(mFlag)) {
            ContactBean call = PrivacyContactManager.getInstance(mContext).getLastCall();
            if (call != null) {
                CallLogTask task = new CallLogTask();
                task.execute(call);
            } else {
                AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {

                    @Override
                    public void run() {
                        PrivacyContactManager.getInstance(mContext).updateSysCallLog();
                    }
                });
            }
            // 快捷手势未读通话记录提醒
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
                        if (callLogs != null && callLogs.size() > 0) {
                            QuickGestureManager.getInstance(mContext).mCallLogs = callLogs;
                            LockManager.getInstatnce().isShowSysNoReadMessage = true;
                            FloatWindowHelper.removeShowReadTipWindow(mContext);
                        }
                    }
                }
            });
        } else if (CONTACT_MODEL.equals(mFlag)) {
        }
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
            String formatNumber = PrivacyContactUtils.formatePhoneNumber(call
                    .getContactNumber());
            cursor = mContext.getContentResolver().query(PrivacyContactUtils.CALL_LOG_URI, null,
                    "number LIKE  ? ", new String[] {
                        "%" + formatNumber
                    }, null, null);

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
                                int count = pre.getMessageNoReadCount();
                                if (count > 0) {
                                    pre.setCallLogNoReadCount(count + 1);
                                } else {
                                    pre.setCallLogNoReadCount(1);
                                }
                                LeoEventBus
                                        .getDefaultBus()
                                        .post(
                                                new PrivacyDeletEditEvent(
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
                                            new PrivacyDeletEditEvent(
                                                    PrivacyContactUtils.PRIVACY_RECEIVER_CALL_LOG_NOTIFICATION));
                        }
                    }
                    // 通知更新通话记录
                    LeoEventBus
                            .getDefaultBus()
                            .post(
                                    new PrivacyDeletEditEvent(
                                            PrivacyContactUtils.PRIVACY_ALL_CALL_NOTIFICATION_HANG_UP));
                    // 删除系统记录
                    AppMasterApplication.getInstance().postInAppThreadPool(new Runnable() {
                        @Override
                        public void run() {
                            PrivacyContactUtils.deleteCallLogFromSystem("number LIKE ?",
                                    number,
                                    mContext);
                        }
                    });
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
            }
        }
    }
}
