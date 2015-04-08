
package com.leo.appmaster.privacycontact;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.utils.NotificationUtil;

public class PrivacyContactManager {

    private static PrivacyContactManager sInstance;
    private Context mContext;
    private ArrayList<ContactBean> mContacts;
    private MessageBean mMessage;
    private MessageBean mLastMessage;
    private ContactBean mLastCallContact;
    private ArrayList<MessageBean> mSysMessageBean;
    private ArrayList<ContactCallLog> mSysCallLogs;
    private ArrayList<ContactBean> mSysContactBean;
    private boolean mContactLoaded = false;
    private boolean mSysMessageLoaded = false;
    private boolean mSysContactsLoaded = false;
    private boolean mSysCallLogLoaded = false;

    private PrivacyContactManager(Context context) {
        this.mContext = context;
        mContacts = new ArrayList<ContactBean>();
        mSysMessageBean = new ArrayList<MessageBean>();
        mSysCallLogs = new ArrayList<ContactCallLog>();
        mSysContactBean = new ArrayList<ContactBean>();

    }

    public static synchronized PrivacyContactManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PrivacyContactManager(context);
        }
        return sInstance;
    }

    public int getAllContactsCount() {
        List<ContactBean> contacts = PrivacyContactUtils.getSysContact(mContext,
                mContext.getContentResolver(), null, null);
        int count = 0;
        if (contacts != null) {
            count = contacts.size();
        }
        return count;
    }

    public int getPrivacyContactsCount() {
        List<ContactBean> contacts = getPrivateContacts();
        return contacts.size();
    }

    public ArrayList<ContactBean> getPrivateContacts() {
        loadPrivateContacts();
        return (ArrayList<ContactBean>) mContacts.clone();
    }

    public void addContact(ContactBean contact) {
        loadPrivateContacts();
        mContacts.add(contact);
    }

    public void removeContact(ContactBean contact) {
        loadPrivateContacts();
        mContacts.remove(contact);
    }

    public void updateContact() {
        mContacts.clear();
        mContacts = (ArrayList<ContactBean>) PrivacyContactUtils.loadPrivateContacts(mContext);
    }

    private synchronized void loadPrivateContacts() {
        if (!mContactLoaded) {
            mContacts.clear();
            Cursor cur = mContext.getContentResolver().query(Constants.PRIVACY_CONTACT_URI, null,
                    null, null, "_id desc");
            if (cur != null) {
                while (cur.moveToNext()) {
                    ContactBean mb = new ContactBean();
                    String number = cur.getString(cur
                            .getColumnIndex(Constants.COLUMN_PHONE_NUMBER));
                    String name = cur.getString(cur
                            .getColumnIndex(Constants.COLUMN_CONTACT_NAME));
                    int answerType = cur.getInt(cur
                            .getColumnIndex(Constants.COLUMN_PHONE_ANSWER_TYPE));
                    byte[] icon = cur.getBlob(cur.getColumnIndex(Constants.COLUMN_ICON));
                    switch (answerType) {
                        case 0:
                            mb.setAnswerStatus(mContext
                                    .getString(R.string.privacy_contact_activity_input_checkbox_hangup));
                            break;
                        case 1:
                            mb.setAnswerStatus(mContext
                                    .getString(R.string.privacy_contact_activity_input_checkbox_normal));
                            break;
                        default:
                            break;
                    }
                    mb.setContactName(name);
                    mb.setContactNumber(number);
                    if (icon != null) {
                        Bitmap contactIcon = PrivacyContactUtils.getBmp(icon);
                        mb.setContactIcon(contactIcon);
                    } else {
                        BitmapDrawable drawable = (BitmapDrawable) mContext.getResources()
                                .getDrawable(
                                        R.drawable.default_user_avatar);
                        mb.setContactIcon(drawable.getBitmap());
                    }
                    mb.setAnswerType(answerType);
                    mContacts.add(mb);
                }
                cur.close();
            }
            mContactLoaded = true;
        }
    }

    public void setLastCall(ContactBean contact) {
        mLastCallContact = contact;
    }

    public ContactBean getLastCall() {
        ContactBean contact = mLastCallContact;
        mLastCallContact = null;
        return contact;
    }

    public synchronized MessageBean getLastMessage() {
        MessageBean message = mLastMessage;
        mLastMessage = null;
        return message;
    }

    // 拦截短信处理
    public synchronized void synMessage(SimpleDateFormat sdf, MessageBean message,
            Context mContext, Long sendDate) {
        boolean messageItemRuning = AppMasterPreference.getInstance(mContext)
                .getMessageItemRuning();
        if (mMessage != null) {
            long date = 0;
            try {
                date = sdf.parse(mMessage.getMessageTime()).getTime();
            } catch (ParseException e) {
            }
            if (mMessage.getPhoneNumber().equals(message.getPhoneNumber())
                    && sendDate == date) {
                mMessage = message;
            } else {
                ContentValues values = new ContentValues();
                values.put(Constants.COLUMN_MESSAGE_PHONE_NUMBER, message.getPhoneNumber());
                values.put(Constants.COLUMN_MESSAGE_CONTACT_NAME, message.getMessageName());
                values.put(Constants.COLUMN_MESSAGE_BODY, message.getMessageBody());
                values.put(Constants.COLUMN_MESSAGE_DATE, message.getMessageTime());
                values.put(Constants.COLUMN_MESSAGE_IS_READ, message.isMessageIsRead());
                values.put(Constants.COLUMN_MESSAGE_TYPE, message.getMessageType());
                int threadId = PrivacyContactUtils.queryContactId(mContext,
                        message.getPhoneNumber());
                values.put(Constants.COLUMN_MESSAGE_THREAD_ID, threadId);
                String number = PrivacyContactUtils.formatePhoneNumber(message.getPhoneNumber());
                ContentResolver mCr = mContext.getContentResolver();
                Uri messageFlag = mCr.insert(Constants.PRIVACY_MESSAGE_URI, values);
                // 发送拦截通知
                if (messageItemRuning) {
                    NotificationManager notificationManager = (NotificationManager) mContext
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notification = new Notification();
                    Intent intentPending = new Intent(mContext, PrivacyContactActivity.class);
                    intentPending.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intentPending.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                            PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG);
                    intentPending.putExtra("message_notifi", true);
                    PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                            intentPending, PendingIntent.FLAG_UPDATE_CURRENT);
                    notification.icon = R.drawable.ic_launcher_notification;
                    notification.tickerText = mContext
                            .getString(R.string.privacy_contact_notification_title_big);
                    notification.flags = Notification.FLAG_AUTO_CANCEL;
                    notification
                            .setLatestEventInfo(
                                    mContext,
                                    mContext.getString(R.string.privacy_contact_notification_title_big),
                                    mContext.getString(R.string.privacy_contact_notification_title_small),
                                    contentIntent);
                    NotificationUtil.setBigIcon(notification,
                            R.drawable.ic_launcher_notification_big);
                    notification.when = System.currentTimeMillis();
                    notificationManager.notify(20140901, notification);
                }
                // 删除系统信息
                // int flag = mCr.delete(PrivacyContactUtils.SMS_INBOXS,
                // "address LIKE ?",
                // new String[] {
                // "%" + number
                // });
                String dateFrom = sdf.format(new Date(sendDate));
                message.setMessageTime(dateFrom);
                mMessage = message;
                mLastMessage = message;
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(Constants.COLUMN_MESSAGE_PHONE_NUMBER, message.getPhoneNumber());
            values.put(Constants.COLUMN_MESSAGE_CONTACT_NAME, message.getMessageName());
            values.put(Constants.COLUMN_MESSAGE_BODY, message.getMessageBody());
            values.put(Constants.COLUMN_MESSAGE_DATE, message.getMessageTime());
            values.put(Constants.COLUMN_MESSAGE_IS_READ, message.isMessageIsRead());
            values.put(Constants.COLUMN_MESSAGE_TYPE, message.getMessageType());
            String formateNumber = PrivacyContactUtils.formatePhoneNumber(message
                    .getPhoneNumber());
            int threadId = PrivacyContactUtils.queryContactId(mContext, message.getPhoneNumber());
            values.put(Constants.COLUMN_MESSAGE_THREAD_ID, threadId);
            String number = PrivacyContactUtils.formatePhoneNumber(message.getPhoneNumber());
            ContentResolver mCr = mContext.getContentResolver();
            Uri messageFlag = mCr.insert(Constants.PRIVACY_MESSAGE_URI, values);
            // 发送拦截通知
            if (messageItemRuning) {
                NotificationManager notificationManager = (NotificationManager) mContext
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notification = new Notification();
                Intent intentPending = new Intent(mContext, PrivacyContactActivity.class);
                intentPending.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                        PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG);
                intentPending.putExtra("message_call_notifi", true);
                PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                        intentPending, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.icon = R.drawable.ic_launcher_notification;
                notification.tickerText = mContext
                        .getString(R.string.privacy_contact_notification_title_big);
                notification.flags = Notification.FLAG_AUTO_CANCEL;
                notification
                        .setLatestEventInfo(
                                mContext,
                                mContext.getString(R.string.privacy_contact_notification_title_big),
                                mContext.getString(R.string.privacy_contact_notification_title_small),
                                contentIntent);
                NotificationUtil.setBigIcon(notification,
                        R.drawable.ic_launcher_notification_big);
                notification.when = System.currentTimeMillis();
                notificationManager.notify(20140901, notification);
            }
            // 删除系统信息
            /*
             * mCr.delete(PrivacyContactUtils.SMS_INBOXS, "address LIKE ?", new
             * String[] { "%" + number });
             */
        }
        // 通知更新短信记录
        LeoEventBus.getDefaultBus().post(
                new PrivacyDeletEditEventBus(
                        PrivacyContactUtils.PRIVACY_RECEIVER_MESSAGE_NOTIFICATION));
        LeoEventBus.getDefaultBus().post(
                new PrivacyDeletEditEventBus(
                        PrivacyContactUtils.MESSAGE_PRIVACY_INTERCEPT_NOTIFICATION));
        String dateFrom = sdf.format(new Date(sendDate));
        message.setMessageTime(dateFrom);
        mMessage = message;
        mLastMessage = message;
    }

    // loadSysMessage
    public synchronized void loadSysMessage() {
        if (!mSysMessageLoaded) {
            mSysMessageBean.clear();
            mSysMessageBean = (ArrayList<MessageBean>) PrivacyContactUtils
                    .queryMessageList(mContext);
            mSysMessageLoaded = true;
        }
    }

    public ArrayList<MessageBean> getSysMessage() {
        loadSysMessage();
        return (ArrayList<MessageBean>) mSysMessageBean.clone();

    }

    public void removeSysMessage(MessageBean messageBean) {
        loadSysMessage();
        mSysMessageBean.remove(messageBean);
    }

    public void updateSysMessage() {
        mSysMessageBean.clear();
        mSysMessageBean = (ArrayList<MessageBean>) PrivacyContactUtils.queryMessageList(mContext);
    }

    // loadSysCallLog
    public synchronized void loadSysCallLog() {
        if (!mSysCallLogLoaded) {
            mSysCallLogs.clear();
            mSysCallLogs = (ArrayList<ContactCallLog>) PrivacyContactUtils.getSysCallLog(mContext,
                    mContext.getContentResolver(), null, null);
            mSysCallLogLoaded = true;
        }
    }

    public ArrayList<ContactCallLog> getSysCallLog() {
        loadSysCallLog();
        return (ArrayList<ContactCallLog>) mSysCallLogs.clone();
    }

    public void removeSysCallLog(ContactCallLog callLog) {
        loadSysCallLog();
        mSysCallLogs.remove(callLog);
    }

    public void updateSysCallLog() {
        mSysCallLogs.clear();
        mSysCallLogs = (ArrayList<ContactCallLog>) PrivacyContactUtils.getSysCallLog(mContext,
                mContext.getContentResolver(), null, null);
    }

    // loadSysContact
    public synchronized void loadSysContacts() {
        if (!mSysContactsLoaded) {
            mSysContactBean.clear();
            mSysContactBean = (ArrayList<ContactBean>) PrivacyContactUtils.getSysContact(mContext,
                    mContext.getContentResolver(), null, null);
            mSysContactsLoaded = true;
        }
    }

    public ArrayList<ContactBean> getSysContacts() {
        loadSysContacts();
         return (ArrayList<ContactBean>) mSysContactBean.clone();
    }

    public void removeSysContact(ContactBean contact) {
        loadSysContacts();
        mSysContactBean.remove(contact);
    }

    public void updateSysContact() {
        mSysContactBean.clear();
        mSysContactBean = (ArrayList<ContactBean>) PrivacyContactUtils.getSysContact(mContext,
                mContext.getContentResolver(), null, null);
    }

}
