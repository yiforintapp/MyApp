
package com.leo.appmaster.privacycontact;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.leo.appmaster.Constants;
import com.leo.appmaster.eventbus.LeoEventBus;

@SuppressLint("NewApi")
public class PrivacyMessageContentObserver extends ContentObserver {
    private Context mContext;
    public static final Uri CALL_LOG_URI = android.provider.CallLog.Calls.CONTENT_URI;
    public static String CALL_LOG_MODEL = "call_log_model";
    public static String MESSAGE_MODEL = "message_model";
    public static final String spaceString = "\u00A0";
    private String mFlag;

    public PrivacyMessageContentObserver(Context context, Handler handler, String flag) {
        super(handler);
        mContext = context;
        this.mFlag = flag;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        ContentResolver cr = mContext.getContentResolver();
        PrivacyContactManager pcm = PrivacyContactManager.getInstance(mContext);
        if (MESSAGE_MODEL.equals(mFlag)) {
            MessageBean mb = pcm.getLastMessage();
            if (mb != null) {
                String number = mb.getPhoneNumber();
                try {
                    cr.delete(
                            PrivacyContactUtils.SMS_INBOXS,
                            "address =  " + "\"" + number + "\" and " + "body = \""
                                    + mb.getMessageBody() + "\"", null);
                } catch (Exception e) {
                }
            }
        } else if (CALL_LOG_MODEL.equals(mFlag)) {
            ContactBean call = PrivacyContactManager.getInstance(mContext).getLastCall();
            if (call != null) {
                String formatNumber = PrivacyContactUtils.formatePhoneNumber(call
                        .getContactNumber());
                Cursor cursor = cr.query(CALL_LOG_URI, null,
                        "number LIKE  ? ", new String[] {
                            "%" + formatNumber
                        }, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String number = cursor.getString(cursor.getColumnIndex("number"));
                        String name = cursor.getString(cursor.getColumnIndex("name"));
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
                        values.put(Constants.COLUMN_CALL_LOG_IS_READ, 0);
                        try {
                            // 保存记录
                            Uri uri = cr.insert(Constants.PRIVACY_CALL_LOG_URI, values);
                            // 通知更新通话记录
                            LeoEventBus
                                    .getDefaultBus()
                                    .post(
                                            new PrivacyDeletEditEventBus(
                                                    PrivacyContactUtils.PRIVACY_ALL_CALL_NOTIFICATION_HANG_UP));
                            /**
                             * 删除系统记录
                             */
                            PrivacyContactUtils.deleteCallLogFromSystem("number LIKE ?",
                                    number,
                                    mContext);
                        } catch (Exception e) {
                        }
                    }
                    cursor.close();
                }

            }
        }
    }

}
