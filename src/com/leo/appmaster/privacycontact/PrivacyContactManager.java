
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
import android.os.Bundle;
import android.provider.CallLog;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NotificationUtil;
import com.leo.appmaster.utils.Utilities;

public class PrivacyContactManager {
    public static final String TAG = "PrivacyContactManager";
    /* 接受隐私联系人存在未读广播的权限 */
    private static final String SEND_RECEIVER_TO_SWIPE_PERMISSION = "com.leo.appmaster.RECEIVER_TO_ISWIPE";
    private static final String RECEIVER_TO_SWIPE_ACTION = "com.leo.appmaster.ACTION_PRIVACY_CONTACT";
    private static final String RECEIVER_TO_SWIPE_ACTION_CANCEL_PRIVACY_TIP = "com.leo.appmaster.ACTION_CANCEL_PRIVACY_TIP";
    private static final String PRIVACY_MSM_OR_CALL = "privacy_msm_or_call";
    public static final String PRIVACY_MSM = "privacy_msm";
    public static final String PRIVACY_CALL = "privacy_call";
    public static final String PRIVACYCONTACT_TO_IWIPE_KEY = "privacycontact_to_iswipe";
    public static final String PRIVACY_CONTACT_NUMBER = "private_number";
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
    public static volatile int mNoReadCalls;
    /*发送短信失败提示标志*/
    public volatile boolean mSendMsmFail = false;

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
        List<ContactBean> contacts = PrivacyContactUtils.getSysContact(mContext, null, null, false);
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
            Cursor cur = null;
            try {
                cur = mContext.getContentResolver().query(Constants.PRIVACY_CONTACT_URI, null,
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
                        try {
                            byte[] icon = cur.getBlob(cur.getColumnIndex(Constants.COLUMN_ICON));
                            if (icon != null) {
                                Bitmap contactIcon = PrivacyContactUtils.getBmp(icon);
                                int size = (int) mContext.getResources().getDimension(R.dimen.privacy_contact_icon_size);
                                contactIcon = PrivacyContactUtils.getScaledContactIcon(contactIcon, size);
                                mb.setContactIcon(contactIcon);
                            }
                        } catch (Error e) {
                        }
                        if (mb.getContactIcon() == null) {
                            BitmapDrawable drawable = (BitmapDrawable) mContext.getResources()
                                    .getDrawable(
                                            R.drawable.default_user_avatar);
                            mb.setContactIcon(drawable.getBitmap());
                        }
                        mb.setAnswerType(answerType);
                        mContacts.add(mb);
                    }
                }
            } catch (Exception e) {

            } finally {
                if (cur != null) {
                    cur.close();
                }
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
        privacyContactSendReceiverToSwipe(
                PRIVACY_MSM, 0, mLastMessage.getPhoneNumber());
    }


    /* 通话记录预加载 */
    private synchronized void loadCallLogs() {
        if (!mCallsLoaded) {
            mSysCalls = (ArrayList<ContactCallLog>) PrivacyContactUtils.getSysCallLog(mContext, null, null, false, false);
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

    /* 隐私联系人有未读发送广播到iswipe */
    public void privacyContactSendReceiverToSwipe(final String flag, int actiontype, String number) {
        Intent privacyIntent = null;
        String msmOrCall = null;
        if (!Utilities.isEmpty(flag)) {
            if (PRIVACY_MSM.equals(flag)) {
                LeoLog.i(TAG, "隐私联系人有未读短信");
                privacyIntent = getPrivacyMsmIntent();
                msmOrCall = PRIVACY_MSM;
            } else if (PRIVACY_CALL.equals(flag)) {
                LeoLog.i(TAG, "隐私联系人有未读通话");
                privacyIntent = getPrivacyCallIntent();
                msmOrCall = PRIVACY_CALL;
            }
        }
        Intent intent = new Intent();
        if (actiontype == 0) {
            LeoLog.i(TAG, "有未读隐私");
            /* 通知有未读 */
            intent.setAction(RECEIVER_TO_SWIPE_ACTION);
            Bundle bundle = new Bundle();
            bundle.putParcelable(PRIVACYCONTACT_TO_IWIPE_KEY, privacyIntent);
            intent.putExtras(bundle);
            if (!Utilities.isEmpty(number)) {
                intent.putExtra(PRIVACY_CONTACT_NUMBER, number);
            }
            intent.putExtra(PRIVACY_MSM_OR_CALL, msmOrCall);
        } else if (actiontype == 1) {
            /* 通知取消未读 */
            intent.setAction(RECEIVER_TO_SWIPE_ACTION_CANCEL_PRIVACY_TIP);
            LeoLog.i(TAG, "通知取消隐私未读标志！！");
        }
        // intent.putExtra(PRIVACYCONTACT_TO_IWIPE_KEY, flag);
        try {
            mContext.sendBroadcast(intent, SEND_RECEIVER_TO_SWIPE_PERMISSION);
            LeoLog.i(TAG, "隐私联系人广播发送成功～～～");
        } catch (Exception e) {
            LeoLog.i(TAG, "隐私联系人广播发送失败！！");
        }
    }

    public Intent getPrivacyMsmIntent() {
        Intent privacyMsmIntent = new Intent(mContext, PrivacyContactActivity.class);
        privacyMsmIntent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                PrivacyContactUtils.TO_PRIVACY_MESSAGE_FLAG);
        return privacyMsmIntent;
    }

    public Intent getPrivacyCallIntent() {
        Intent privacyCallIntent = new Intent(mContext,
                PrivacyContactActivity.class);
        privacyCallIntent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT,
                PrivacyContactUtils.TO_PRIVACY_CALL_FLAG);
        return privacyCallIntent;
    }

    /* 对于iswipe没有隐私未读处理 */
    public void cancelPrivacyTipFromPrivacyCall() {
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        if (amp.getMessageNoReadCount() <= 0) {
            privacyContactSendReceiverToSwipe(null, 1, null);
        }
    }

    public void cancelPrivacyTipFromPrivacyMsm() {
        AppMasterPreference amp = AppMasterPreference.getInstance(mContext);
        if (amp.getCallLogNoReadCount() <= 0) {
            privacyContactSendReceiverToSwipe(null, 1, null);
        }
    }

    /*快捷隐私通话处理*/
    public void noReadCallPrivacyCallTipForQuickGesture() {
        AppMasterPreference mPreference = AppMasterPreference.getInstance(mContext);
        // AppMasterPreference.getInstance(mContext).setQuickGestureCallLogTip(true);
        if (mPreference.getSwitchOpenPrivacyContactMessageTip()
                && mPreference.getQuickGestureCallLogTip()) {
            if (PrivacyContactManager.getInstance(mContext).deleteCallLogDatebaseFlag) {
                PrivacyContactManager.getInstance(mContext).deleteCallLogDatebaseFlag = false;
            }
        }
    }

    /**
     * 快捷手势未读通话处理
     */
    public void noReadCallForQuickGesture(final ContactBean call) {
        ThreadManager.executeOnAsyncThread(new Runnable() {

            @Override
            public void run() {
                if (AppMasterPreference.getInstance(mContext).getSwitchOpenRecentlyContact()) {
                    String selection = CallLog.Calls.TYPE + "=? and " + CallLog.Calls.NEW + "=?";
                    String[] selectionArgs = new String[]{
                            String.valueOf(CallLog.Calls.MISSED_TYPE), String.valueOf(1)
                    };
                    ArrayList<ContactCallLog> callLogs = (ArrayList<ContactCallLog>) PrivacyContactUtils
                            .getSysCallLog(mContext, selection,
                                    selectionArgs, false, false);
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

    /**
     * 快捷手势未读短信处理
     */
    public void noReadMsmTipForQuickGesture() {
        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                ContentResolver cr = mContext.getContentResolver();
                if (AppMasterPreference.getInstance(mContext).getSwitchOpenNoReadMessageTip()) {
                    ArrayList<MessageBean> messages = (ArrayList<MessageBean>) PrivacyContactUtils
                            .getSysMessage(mContext, "read=0 AND type=1", null, false, false);
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
                            .getSysMessage(mContext, "read=0 AND type=1", null, true, false);
                    if (messageList != null) {
                        int currentCount = messageList.size();
                        PrivacyContactManager.getInstance(mContext).messageSize = currentCount;
                    }
                }
            }
        });
    }

    /**
     * 快捷手势未读通话红点恢复处理
     */
    private void restoreRedTipValueForCall() {
        if (PrivacyContactManager.getInstance(mContext).checkPhoneModelForCallRestoreRedTip()) {
            String selection = CallLog.Calls.TYPE + "=? and " + CallLog.Calls.NEW + "=?";
            String[] selectionArgs = new String[]{
                    String.valueOf(CallLog.Calls.MISSED_TYPE), String.valueOf(1)
            };
            ArrayList<ContactCallLog> callLogs = (ArrayList<ContactCallLog>) PrivacyContactUtils
                    .getSysCallLog(mContext, selection, selectionArgs, true, true);
            if (callLogs != null) {
                int currentCount = callLogs.size();
                PrivacyContactManager.getInstance(mContext).mUnCalls = currentCount;
            }
        }
    }
}
