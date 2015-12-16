package com.leo.appmaster.privacycontact;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.utils.LeoLog;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 隐私联系人通话记录异步处理
 * Created by runlee on 15-11-11.
 */
public class PrivacyCallAsyncTask extends AsyncTask<ContactBean, ContactBean, Cursor> {
    private static final String TAG = "PrivacyCallAsyncTask";
    private ContactBean call;
    private Cursor cursor = null;
    private Context mContext;
    private PrivacyCallHandler mPrivacyCallHandler = new PrivacyCallHandler();

    public PrivacyCallAsyncTask(Context context) {
        mContext = context.getApplicationContext();
    }

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
                    values.put(Constants.COLUMN_CALL_LOG_DURATION, cursor.getString(cursor.getColumnIndex("duration")));

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
                                PrivacyContactUtils.callLogNotification(mContext,
                                        numberToIswipe);
                            }
                        }
                    } else if (call.getAnswerType() == 0) {
                        if (CallLog.Calls.OUTGOING_TYPE != type) {
                            PrivacyContactUtils.callLogNotification(mContext,
                                    numberToIswipe);
                        }
                    }
                }
            } catch (Exception e) {

            } finally {
                cursor.close();
            }

            // 快捷手势隐私通话未读提示
            PrivacyContactManager.getInstance(mContext).noReadCallPrivacyCallTipForQuickGesture();
        }
    }

    public void sendMsgHandler(final ContactBean number) {
        if (mPrivacyCallHandler != null) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    call = number;
                    ContentResolver cr = mContext.getContentResolver();
                    String callNumber = call.getContactNumber();
                    String formatNumber = PrivacyContactUtils.formatePhoneNumber(callNumber);
                    try {
                        String select = "number LIKE  ? ";
                        String[] selectArgs = new String[]{"%" + formatNumber};
                        cursor = cr.query(PrivacyContactUtils.CALL_LOG_URI, null, select, selectArgs, null);
                    } catch (Exception e) {
                    } catch (Error error) {
                        // AM-1824, No such method error
                    }
                    Message msg = new Message();
                    msg.what = PrivacyContactUtils.MSG_PRIVACY_CALL_HANDLER;
                    msg.obj = cursor;
                    mPrivacyCallHandler.sendMessage(msg);
                }
            });
        }
    }

    private class PrivacyCallHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PrivacyContactUtils.MSG_PRIVACY_CALL_HANDLER:
                    Cursor cursor = (Cursor) msg.obj;
                    String numberToIswipe = null;
                    if (cursor != null) {
                        try {
                            while (cursor.moveToNext()) {
                                final String number = cursor.getString(cursor.getColumnIndex("number"));
                                numberToIswipe = number;
                                String name = call.getContactName();
                                int dateColum = cursor.getColumnIndex(CallLog.Calls.DATE);
                                int typeColum = cursor.getColumnIndex(CallLog.Calls.TYPE);
                                Date date = new Date(Long.parseLong(cursor.getString(dateColum)));
                                SimpleDateFormat sfd = new SimpleDateFormat(Constants.PATTERN_DATE);
                                String time = sfd.format(date);
                                int type = (cursor.getInt(typeColum));
                                ContentValues values = new ContentValues();
                                values.put(Constants.COLUMN_CALL_LOG_PHONE_NUMBER, number);
                                if (!TextUtils.isEmpty(name)) {
                                    values.put(Constants.COLUMN_CALL_LOG_CONTACT_NAME, name);
                                } else {
                                    String contactName = call.getContactName();
                                    if (!TextUtils.isEmpty(contactName)) {
                                        values.put(Constants.COLUMN_CALL_LOG_CONTACT_NAME, contactName);
                                    } else {
                                        values.put(Constants.COLUMN_CALL_LOG_CONTACT_NAME, number);
                                    }
                                }
                                values.put(Constants.COLUMN_CALL_LOG_DATE, time);
                                values.put(Constants.COLUMN_CALL_LOG_TYPE, type);
                                String duration = cursor.getString(cursor.getColumnIndex("duration"));
                                values.put(Constants.COLUMN_CALL_LOG_DURATION, duration);

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

                                /*保存记录*/
                                ContentResolver cr = mContext.getContentResolver();
                                PrivacyContactUtils.insertDbLog(cr, Constants.PRIVACY_CALL_LOG_URI, values);

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
                                            String msgEvent = PrivacyContactUtils.PRIVACY_RECEIVER_CALL_LOG_NOTIFICATION;
                                            PrivacyEditFloatEvent event = new PrivacyEditFloatEvent(msgEvent);
                                            LeoEventBus.getDefaultBus().post(event);
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
                                        String msgCallNoti = PrivacyContactUtils.PRIVACY_RECEIVER_CALL_LOG_NOTIFICATION;
                                        PrivacyEditFloatEvent event = new PrivacyEditFloatEvent(msgCallNoti);
                                        LeoEventBus.getDefaultBus().post(event);
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
                                String msgEdit = PrivacyContactUtils.PRIVACY_ALL_CALL_NOTIFICATION_HANG_UP;
                                PrivacyEditFloatEvent event = new PrivacyEditFloatEvent(msgEdit);
                                LeoEventBus.getDefaultBus().post(event);
                                // 发送通知
                                if (call.getAnswerType() == 1) {
                                    if (CallLog.Calls.OUTGOING_TYPE != type) {
                                        if (CallLog.Calls.MISSED_TYPE == type) {
                                            PrivacyContactUtils.callLogNotification(mContext,
                                                    numberToIswipe);
                                        }
                                    }
                                } else if (call.getAnswerType() == 0) {
                                    if (CallLog.Calls.OUTGOING_TYPE != type) {
                                        PrivacyContactUtils.callLogNotification(mContext,
                                                numberToIswipe);
                                    }
                                }
                            }
                        } catch (Exception e) {

                        } finally {
                            cursor.close();
                        }

                        // 快捷手势隐私通话未读提示
                        PrivacyContactManager.getInstance(mContext).noReadCallPrivacyCallTipForQuickGesture();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
