
package com.leo.appmaster.privacycontact;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import android.util.Log;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.quickgestures.FloatWindowHelper;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.NotificationUtil;

public class PrivacyContactManager {
    /* 手机名称 */
    public static String COOLPAD_YULONG = "YuLong";
    public static String NUBIA = "nubia";
    public static final String ZTEU817 = "ZTE U817";
    /* 跳转通话记录特别处理机型数组 */
    public static final String[] filterPhoneMode = {
            "SM-N9150", "SM-G9250"
    };
    private static PrivacyContactManager sInstance;
    private Context mContext;
    private ArrayList<ContactBean> mContacts;
    private MessageBean mMessage;
    private MessageBean mLastMessage;
    private ContactBean mLastCallContact;
    private ContactBean mLastMessageContact;
    private boolean mContactLoaded = false;
    /* 用来做隐私联系人通话删除时的通话标志 */
    public boolean deleteCallLogDatebaseFlag;
    /* 用来做隐私联系人短信删除时的标志 */
    public boolean deleteMsmDatebaseFlag;
    /* 对不能接受短信广播未读短信数量统计，与下次来短信进行对比，如果增加则将显示是否显示过红点的标志为变为false */
    public int messageSize;
    /* 对不能接受来电广播未读短信数量统计，与下次来短信进行对比，如果增加则将显示是否显示过红点的标志为变为false */
    public int mUnCalls;
    private boolean mCallsLoaded;
    private ArrayList<ContactCallLog> mSysCalls;
    /* 当前页是否为PrivacyContactActivity */
    public boolean mIsOpenPrivacyContact;
    /* 用来做测试 */
    public boolean testValue;

    private PrivacyContactManager(Context context) {
        this.mContext = context.getApplicationContext();
        mContacts = new ArrayList<ContactBean>();
        mSysCalls = new ArrayList<ContactCallLog>();

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

    public void setLastMessageContact(ContactBean contact) {
        mLastMessageContact = contact;
    }

    public ContactBean getLastMessageContact() {
        ContactBean contact = mLastMessageContact;
        mLastCallContact = null;
        return contact;
    }

    public synchronized MessageBean getLastMessage() {
        MessageBean message = mLastMessage;
        mLastMessage = null;
        return message;
    }

    /* 拦截短信处理 */
    public synchronized void synMessage(SimpleDateFormat sdf, MessageBean message,
            Context mContext, Long sendDate) {
        AppMasterPreference pre = AppMasterPreference.getInstance(mContext);
        boolean messageItemRuning = pre.getMessageItemRuning();
        int count = pre.getMessageNoReadCount();
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
                ContentResolver mCr = mContext.getContentResolver();
                mCr.insert(Constants.PRIVACY_MESSAGE_URI, values);
                if (count > 0) {
                    pre.setMessageNoReadCount(count + 1);
                } else {
                    pre.setMessageNoReadCount(1);
                }
                /*
                 * 记录最后隐私短信和隐私通话哪个最后来电(解决：在快捷手势中有隐私联系人时，点击跳入最后记录的Tab页面)
                 */
                QuickGestureManager.getInstance(mContext).privacyLastRecord = QuickGestureManager.RECORD_MSM;
                /* 发送拦截通知 */
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
                    AppMasterPreference.getInstance(mContext).setQuickGestureMsmTip(true);
                }
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
            int threadId = PrivacyContactUtils.queryContactId(mContext, message.getPhoneNumber());
            values.put(Constants.COLUMN_MESSAGE_THREAD_ID, threadId);
            ContentResolver mCr = mContext.getContentResolver();
            mCr.insert(Constants.PRIVACY_MESSAGE_URI, values);
            if (count > 0) {
                pre.setMessageNoReadCount(count + 1);
            } else {
                pre.setMessageNoReadCount(1);
            }
            /* 发送拦截通知 */
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
                AppMasterPreference.getInstance(mContext).setQuickGestureMsmTip(true);
            }
        }
        /* 通知更新短信记录 */
        LeoEventBus.getDefaultBus().post(
                new PrivacyEditFloatEvent(
                        PrivacyContactUtils.PRIVACY_RECEIVER_MESSAGE_NOTIFICATION));
        LeoEventBus.getDefaultBus().post(
                new PrivacyEditFloatEvent(
                        PrivacyContactUtils.MESSAGE_PRIVACY_INTERCEPT_NOTIFICATION));
        String dateFrom = sdf.format(new Date(sendDate));
        message.setMessageTime(dateFrom);
        mMessage = message;
        mLastMessage = message;
        /* 隐私联系人有未读短信时发送广播 */
        QuickGestureManager.getInstance(mContext).privacyContactSendReceiverToSwipe(
                QuickGestureManager.PRIVACY_MSM,0,mLastMessage.getPhoneNumber());
    }

    /* 对快捷手势隐私联系人,消费(查看或者删除)隐私通话时，红点去除操作 */
    public void deletePrivacyCallCancelRedTip(Context context) {
        /* 隐私通话 */
        if (QuickGestureManager
                .getInstance(context).isShowPrivacyCallLog) {
            QuickGestureManager
                    .getInstance(context).isShowPrivacyCallLog = false;
            AppMasterPreference.getInstance(
                    context)
                    .setQuickGestureCallLogTip(
                            false);
            if ((QuickGestureManager
                    .getInstance(context).getQuiQuickNoReadMessage() == null || QuickGestureManager
                    .getInstance(context).getQuiQuickNoReadMessage()
                    .size() <= 0)/* 未读短信 */
                    && (QuickGestureManager
                            .getInstance(context).getQuickNoReadCall() == null || QuickGestureManager
                            .getInstance(context).getQuickNoReadCall()
                            .size() <= 0)/* 未读通话 */
                    && AppMasterPreference.getInstance(
                            context)
                            .getMessageNoReadCount() <= 0/* 隐私短信 */
                    && AppMasterPreference
                            .getInstance(context)
                            .getLastBusinessRedTipShow()/* 运营 */) {
                QuickGestureManager
                        .getInstance(context).isShowSysNoReadMessage = false;
            }
        }
        FloatWindowHelper
                .removeShowReadTipWindow(context);
    }

    /* 对快捷手势隐私联系人,消费隐(查看或者删除)私短信时，红点去除操作 */
    public void deletePrivacyMsmCancelRedTip(Context context) {
        if (QuickGestureManager
                .getInstance(context).isShowPrivacyMsm) {
            // QuickGestureManager.getInstance(context).isShowSysNoReadMessage
            // = false;
            QuickGestureManager
                    .getInstance(context).isShowPrivacyMsm = false;
            AppMasterPreference.getInstance(
                    context)
                    .setQuickGestureMsmTip(false);
            if ((QuickGestureManager
                    .getInstance(context).getQuiQuickNoReadMessage() == null || QuickGestureManager
                    .getInstance(context).getQuiQuickNoReadMessage()
                    .size() <= 0)/* 未读短信 */
                    && (QuickGestureManager
                            .getInstance(context).getQuickNoReadCall() == null || QuickGestureManager
                            .getInstance(context).getQuickNoReadCall()
                            .size() <= 0)/* 未读通话 */
                    && AppMasterPreference.getInstance(
                            context)
                            .getCallLogNoReadCount() <= 0/* 隐私通话 */
                    && AppMasterPreference
                            .getInstance(context)
                            .getLastBusinessRedTipShow()/* 运营 */) {
                QuickGestureManager
                        .getInstance(context).isShowSysNoReadMessage = false;
            }
        }
        FloatWindowHelper
                .removeShowReadTipWindow(context);
    }

    /* 通话记录预加载 */
    private synchronized void loadCallLogs() {
        if (!mCallsLoaded) {
            mSysCalls = (ArrayList<ContactCallLog>) PrivacyContactUtils.getSysCallLog(mContext,
                    mContext.getContentResolver(), null, null);
            if (mSysCalls != null && mSysCalls.size() > 0) {
                Collections.sort(mSysCalls, PrivacyContactUtils.mCallLogCamparator);
            }
            mCallsLoaded = true;
        }
    }

    public ArrayList<ContactCallLog> getSysCalls() {
        // Log.e(Constants.RUN_TAG, "预加载通话记录");
        loadCallLogs();
        return (ArrayList<ContactCallLog>) mSysCalls.clone();
    }

    public void removeCallLog(ContactCallLog callLog) {
        loadCallLogs();
        mSysCalls.remove(callLog);
    }

    public void addCallLog(ContactCallLog callLog) {
        loadCallLogs();
        mSysCalls.add(callLog);
    }

    public void updateCalls() {
        if (mSysCalls != null && mSysCalls.size() > 0) {
            mSysCalls.clear();
            mCallsLoaded = false;
            loadCallLogs();
        }
    }

    public void destroyCalls() {
        if (mSysCalls != null) {
            mSysCalls = null;
        }
        mCallsLoaded = false;
        // Log.e(Constants.RUN_TAG, "销毁内存中的通话记录");
    }

    public void initLoadData() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                PrivacyContactManager.getInstance(mContext).destroyCalls();
                PrivacyContactManager.getInstance(mContext).getSysCalls();
            }
        });
    }

    public void uninitLoadData() {
        mIsOpenPrivacyContact = false;
        ThreadManager.executeOnAsyncThread(new Runnable() {

            @Override
            public void run() {
                destroyCalls();
            }
        });
    }

    /* 检查是否为不能接受短信广播的机型 */
    public boolean clearMsmForNoReceiver() {
        return BuildProperties.checkPhoneBrand(PrivacyContactManager.NUBIA)
                || BuildProperties.checkPhoneBrand(COOLPAD_YULONG);
    }

    /* 检查是否为不能接受来电广播的机型 */
    public boolean clearCallForNoReceiver() {
        return BuildProperties.checkPhoneBrand(COOLPAD_YULONG);
    }

    /* 检查是否为需要这里恢复红点的机型(不能接受短信广播的机型) */
    public boolean checkPhoneModelForRestoreRedTip() {
        return BuildProperties.isMIUI()
                || BuildProperties.checkPhoneBrand(NUBIA)
                || BuildProperties.checkPhoneBrand(COOLPAD_YULONG);
    }

    /* 检查是否为需要这里恢复红点的机型(不能接受来电广播的机型) */
    public boolean checkPhoneModelForCallRestoreRedTip() {
        return BuildProperties.checkPhoneBrand(COOLPAD_YULONG);
    }

    /* 检查是否为不能直接跳到短信详情的机型 */
    public boolean noToMsmDetail() {
        return BuildProperties.checkPhoneModel(ZTEU817)
                || BuildProperties.checkPhoneBrand(COOLPAD_YULONG);
    }
}
