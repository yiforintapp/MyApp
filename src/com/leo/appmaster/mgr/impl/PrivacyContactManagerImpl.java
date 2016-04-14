package com.leo.appmaster.mgr.impl;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.CallLog;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.Constants;
import com.leo.appmaster.db.LeoSettings;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.PrivacyContactManager;
import com.leo.appmaster.phoneSecurity.PhoneSecurityConstants;
import com.leo.appmaster.phoneSecurity.PhoneSecurityManager;
import com.leo.appmaster.privacycontact.ContactBean;
import com.leo.appmaster.privacycontact.ContactCallLog;
import com.leo.appmaster.privacycontact.MessageBean;
import com.leo.appmaster.privacycontact.MessageCallLogBean;
import com.leo.appmaster.privacycontact.PrivacyContactUtils;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.DeviceUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.PrefConst;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PrivacyContactManagerImpl extends PrivacyContactManager {
    public static final String TAG = "PrivacyContactManagerImpl";
    public static final Boolean DBG = false;
    public static final Uri SMS_INBOXS = Uri.parse("content://sms/");
    public static final Uri CALL_LOG_URI = android.provider.CallLog.Calls.CONTENT_URI;
    /*隐私联系人列表*/
    private ArrayList<ContactBean> mContacts;
    private boolean mContactLoaded = false;

    @Override
    public void onDestory() {

    }

    public PrivacyContactManagerImpl() {
        mContacts = new ArrayList<ContactBean>();
    }

    @Override
    public Cursor getPrivacyContactList() {
        ContentResolver cr = mContext.getContentResolver();
        Cursor cur = cr.query(Constants.PRIVACY_CONTACT_URI, null,
                null, null, "_id desc");
        return cur;
    }

    @Override
    public MessageCallLogBean addPrivacyContact(List<ContactBean> addContacts) {
        if (addContacts == null || addContacts.size() <= 0) {
            return null;
        }
        com.leo.appmaster.privacycontact.PrivacyContactManager pcm = com.leo.appmaster.privacycontact.PrivacyContactManager
                .getInstance(AppMasterApplication.getInstance());
        List<MessageBean> messages = null;
        List<ContactCallLog> callLogs = null;
        MessageCallLogBean messageCalls = null;
        for (ContactBean contact : addContacts) {
            String name = contact.getContactName();
            String number = PrivacyContactUtils.simpleFromateNumber(contact.getContactNumber());

            if (TextUtils.isEmpty(number)) {
                continue;
            }

//            CallFilterManagerImpl cmp = (CallFilterManagerImpl) MgrContext.getManager(MgrContext.MGR_CALL_FILTER);
//            boolean isHaveBlackNum = cmp.isExistBlackList(number);
//            if (isHaveBlackNum) {
//                continue;
//            }
            Bitmap contactIcon = contact.getContactIcon();
            /*隐私联系人去重*/
            boolean flagContact = PrivacyContactUtils.pryContRemovSame(number);

            if (!flagContact) {
                ContentValues contactValues = new ContentValues();
                contactValues.put(Constants.COLUMN_PHONE_NUMBER, number);
                contactValues.put(Constants.COLUMN_CONTACT_NAME, name);
                contactValues.put(Constants.COLUMN_PHONE_ANSWER_TYPE, 1);
                byte[] icon = PrivacyContactUtils.formateImg(contactIcon);
                contactValues.put(Constants.COLUMN_ICON, icon);
                mContext.getContentResolver().insert(Constants.PRIVACY_CONTACT_URI, contactValues);
                pcm.addContact(new ContactBean(0, name, number, null, contactIcon, null,
                        false, 1, null, 0, 0, 0));
            }
            com.leo.appmaster.privacycontact.PrivacyContactManager pm = com.leo.appmaster.privacycontact.PrivacyContactManager.getInstance(mContext);
           /*4.4以上不去做短信操作*/
            boolean isLessLeve19 = PrivacyContactUtils.isLessApiLeve19();
            if (isLessLeve19) {
                if (messages == null) {
                    messages = pm.queryMsmsForNumber(number);
                } else {
                    List<MessageBean> addMessages = pm.queryMsmsForNumber(number);
                    messages.addAll(addMessages);
                }
            }
            if (callLogs == null) {
                callLogs = pm.queryCallsForNumber(number);
            } else {
                List<ContactCallLog> addCalllog = pm.queryCallsForNumber(number);
                callLogs.addAll(addCalllog);
            }
        }
        if ((messages != null && messages.size() > 0) || (callLogs != null && callLogs.size() > 0)) {
            messageCalls = new MessageCallLogBean(callLogs, messages);
        }

        return messageCalls;
    }

    @Override
    public int deletePrivacyContact(String phoneNumber) {
        ContentResolver cr = mContext.getContentResolver();
        int count = cr.delete(Constants.PRIVACY_CONTACT_URI, Constants.COLUMN_PHONE_NUMBER + " = ? ",
                new String[]{phoneNumber});
        return count;
    }

    @Override
    public int getPrivacyCallNoReadCount() {
        return 0;
    }

    @Override
    public int getPrivacyMessageNoReadCount() {
        return 0;
    }

    @Override
    public Cursor getSystemContacts(String selection, String[] selectionArgs) {
        Cursor cursor = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
            cursor = cr.query(PrivacyContactUtils.CONTACT_PHONE_URL,
                    null, selection, selectionArgs, "sort_key");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return cursor;
    }

    @Override
    public Cursor getSystemCalls(String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
            if (TextUtils.isEmpty(sortOrder)) {
                sortOrder = CallLog.Calls.DEFAULT_SORT_ORDER;
            }
            cursor = cr.query(CALL_LOG_URI, null, selection, selectionArgs, sortOrder);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return cursor;
    }

    @Override
    public Cursor getSystemMessages(String selection, String[] selectionArgs) {

        Cursor cur = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
            cur = cr.query(SMS_INBOXS, null, selection, selectionArgs, "_id desc");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return cur;
    }

    @Override
    public List<ContactBean> getFrequentContacts() {
        /*本次查询的记录*/
        List<ContactBean> contactsCurrent = null;
         /*当前通话列表所有的电话号码*/
        List<String> callNumbers = new ArrayList<String>();
        /*当前短信列表所有的电话号码*/
        List<String> messageNumber = new ArrayList<String>();
        /*取通话列表和短信列表电话的并集，别且去除掉重复的电话号码*/
        Set<String> phoneNumbers = new HashSet<String>();
        /*本次查询日期*/
        long current = System.currentTimeMillis();
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy/MM/dd");
        String time = sfd.format(new Date(current));
        long currentDate = 0;
        try {
            currentDate = Date.parse(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //查询开始时间
        long quTime = System.currentTimeMillis();
        long beforeCurrentTime = currentDate - PhoneSecurityConstants.DAY_TIME;
        String selectionCall = "date <= ? and date >= ? ";
        String[] selectionArgsCall = new String[]{String.valueOf(currentDate), String.valueOf(beforeCurrentTime)};
        /*系统当前所有的通话记录列表*/
        //查询本次查询的前一天
        List<ContactCallLog> calls = PrivacyContactUtils.getSysCallLog(mContext, selectionCall, selectionArgsCall, null, false, true);
        if (calls == null || (calls != null && calls.size() <= 0)) {
            //前一天为空则，查询当天
            selectionCall = "date >= ? ";
            selectionArgsCall = new String[]{String.valueOf(currentDate)};
            calls = PrivacyContactUtils.getSysCallLog(mContext, selectionCall, selectionArgsCall, null, false, true);
        }

        String selectionMessage = "date <= ? and date >= ? ";
        String[] selectionArgsMessage = new String[]{String.valueOf(currentDate), String.valueOf(beforeCurrentTime)};
        /*系统当前的所有短信列表*/
        List<MessageBean> messages = PrivacyContactUtils.getSysMessage(mContext, selectionMessage, selectionArgsMessage, false, true);
        if (messages == null || (messages != null && messages.size() <= 0)) {
            selectionMessage = "date >= ? ";
            selectionArgsMessage = new String[]{String.valueOf(currentDate)};
            messages = PrivacyContactUtils.getSysMessage(mContext, selectionMessage, selectionArgsMessage, false, true);
        }

        if ((calls == null || (calls != null && calls.size() <= 0))
                && (messages == null || (messages != null && messages.size() <= 0))) {
            /*通话查询*/
            //前一天和当天都为空则，查询整个记录
            selectionCall = null;
            selectionArgsCall = null;
            calls = PrivacyContactUtils.getSysCallLog(mContext, selectionCall, selectionArgsCall, null, false, false);
            /*短信查询*/
            selectionMessage = null;
            selectionArgsMessage = null;
            messages = PrivacyContactUtils.getSysMessage(mContext, selectionMessage, selectionArgsMessage, false, false);
            LeoLog.i(TAG, "查询整个记录");
        }
        LeoLog.d(TAG, "查询通话，短信记录耗时：" + (System.currentTimeMillis() - quTime));
        //通话，短信去重
        long fileTime = System.currentTimeMillis();
        if (calls != null) {
            for (ContactCallLog call : calls) {
                callNumbers.add(call.getCallLogNumber());
            }
        } else {
            calls = new ArrayList<ContactCallLog>();
        }


        if (messages != null) {
            for (MessageBean message : messages) {
                messageNumber.add(message.getPhoneNumber());
            }
        } else {
            messages = new ArrayList<MessageBean>();
        }
        /*通话记录号码和短信号码去重*/
        for (String msmNumber : messageNumber) {
            String formateNumber = PrivacyContactUtils.formatePhoneNumber(msmNumber);
            Iterator callIt = callNumbers.iterator();
            while (callIt.hasNext()) {
                String number = (String) callIt.next();
                if (number != null && formateNumber != null && number.contains(formateNumber)) {
                    callIt.remove();
                }
            }
        }
        callNumbers.addAll(messageNumber);
        phoneNumbers.addAll(callNumbers);
        LeoLog.d(TAG, "通话，短信去重耗时：" + (System.currentTimeMillis() - fileTime));
        //短信，通话记录统计
        long countTime = System.currentTimeMillis();
        if (!phoneNumbers.isEmpty()) {
            contactsCurrent = new ArrayList<ContactBean>();
        /*统计存在于通话列表和短信列表的电话号码的短信数和电话数*/
            for (String number : phoneNumbers) {
                ContactBean contact = new ContactBean();
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                int callCount = 0;
                int messageCount = 0;
                String phoneNumber = null;
                for (ContactCallLog call : calls) {
                    phoneNumber = call.getCallLogNumber();
                    if (phoneNumber != null && formateNumber != null && phoneNumber.contains(formateNumber)) {
                    /*存在于通话记录中，保存通话记录的条数*/
                        int count = call.getCallLogCount();
                        callCount = count;
                        break;
                    } else {
                    /*通话列表中没有，通话记录为0*/
                        callCount = 0;
                    }
                }
                for (MessageBean message : messages) {
                    phoneNumber = message.getPhoneNumber();
                    if (phoneNumber != null && formateNumber != null && phoneNumber.contains(formateNumber)) {
                    /*存在于短信列表中，保存短信列表条数*/
                        int count = message.getMessageCount();
                        messageCount = count;
                        break;
                    } else {
                    /*短信列表中没有，短信条数为0*/
                        messageCount = 0;
                    }
                }
                List<ContactBean> contacts = PrivacyContactUtils.getSysContact(mContext, null, null, true);
                ContactBean tempContact = null;
                boolean isExistContact = false;
                for (ContactBean contactBean : contacts) {
                    String contactNumber = contactBean.getContactNumber();
                    contactNumber = PrivacyContactUtils.simpleFromateNumber(contactNumber);
                    if (contactNumber != null && formateNumber != null && contactNumber.contains(formateNumber)) {
                        isExistContact = true;
                        tempContact = contactBean;
                        break;
                    }
                }
                if (isExistContact) {
                    contact.setContactName(tempContact.getContactName());
                    contact.setContactNumber(tempContact.getContactNumber());
                    contact.setContactIcon(tempContact.getContactIcon());
                } else {
//                    contact.setContactName(number);
//                    contact.setContactNumber(number);
                    continue;
                }
                if (DBG) {
                    LeoLog.i(TAG, contact.getContactName() + ",短信数：" + messageCount + ",通话数：" + callCount);
                }
                contact.setCount(callCount + messageCount);
                contact.setCallCount(callCount);
                contact.setMessageCount(messageCount);
                contactsCurrent.add(contact);
            }
        } else {
            return null;
        }
        LeoLog.d(TAG, "短信，通话记录统计耗时：" + (System.currentTimeMillis() - countTime));
        //隐私联系人过滤
        long privacyFilterTime = System.currentTimeMillis();
        /*过滤掉已经为隐私联系人的频繁联系人*/
        Set<ContactBean> cloneContacts = new HashSet<ContactBean>();
        cloneContacts.addAll(contactsCurrent);
        com.leo.appmaster.privacycontact.PrivacyContactManager pcm = com.leo.appmaster.privacycontact.PrivacyContactManager
                .getInstance(AppMasterApplication.getInstance());
        /*获取隐私联系人*/
        ArrayList<ContactBean> contacts = pcm.getPrivateContacts();
        if (contacts != null && contacts.size() != 0) {
            String number = null;
            for (ContactBean contactPrivacy : contacts) {
                String tempNumber = PrivacyContactUtils.formatePhoneNumber(contactPrivacy.getContactNumber());
                Iterator it = cloneContacts.iterator();
                while (it.hasNext()) {
                    ContactBean contact = (ContactBean) it.next();
                    number = contact.getContactNumber();
                    if (number != null && tempNumber != null && number.contains(tempNumber)/*是否为隐私联系人号码*/
                            || contact.getCount() <= 0 /*过滤掉通话，短信小于等于0的联系人*/) {
                        it.remove();
                        break;
                    }
                }
            }
        }
        contactsCurrent = new ArrayList<ContactBean>(cloneContacts);
        LeoLog.d(TAG, "隐私联系人过滤耗时：" + (System.currentTimeMillis() - privacyFilterTime));
        //频繁隐私联系人排序
        long freTime = System.currentTimeMillis();
        Collections.sort(contactsCurrent, new FrequentContactComparator());
        LeoLog.d(TAG, "频繁隐私联系人排序耗时：" + (System.currentTimeMillis() - freTime));
        if (!contactsCurrent.isEmpty()) {
            if (contactsCurrent.size() > PhoneSecurityConstants.FREQUENT_CONTACT_COUNT) {
                return contactsCurrent.subList(0, 5);
            } else {
                return contactsCurrent;
            }
        }
        return null;
    }

    private class FrequentContactComparator implements Comparator<ContactBean> {

        @Override
        public int compare(ContactBean contact1, ContactBean contact2) {
            if (contact1.getCount() > contact2.getCount()) {
                return -1;
            } else if (contact1.getCount() == contact2.getCount()) {
                return 0;
            } else {
                return 1;
            }
        }

    }

    @Override
    public boolean sendMessage(String number, String content, int fromId) {
        /*有发送短信，恢复短信发送失败Toast标志值*/
        com.leo.appmaster.privacycontact.PrivacyContactManager.getInstance(mContext).mSendMsmFail = false;

        //设置本次发送短信来自何处
        PhoneSecurityManager pm = PhoneSecurityManager.getInstance(mContext);
        pm.setQiKuSendFlag(BuildProperties.isQiKu());
        pm.setMtkFromSendId(fromId);
        pm.setIsTryMtk(false);
        pm.setIsSonyMc(BuildProperties.isSonyM35c());


        SmsManager sms = SmsManager.getDefault();
        if (!TextUtils.isEmpty(content)) {
            LeoLog.d("MTKSendMsmHandler", "Send Msm content:" + content);
        }
        try {
            if (content.length() > 70) {
                ArrayList<String> messageContents = sms.divideMessage(content);
                ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                for (int i = 0; i < messageContents.size(); i++) {
                    Intent sentIntent = new Intent(PrivacyContactUtils.SENT_SMS_ACTION);
                    PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0,
                            sentIntent, 0);
                    sentIntents.add(sentPI);
                }
                sms.sendMultipartTextMessage(number, null, messageContents, sentIntents, null);
            } else {
                Intent sentIntent = new Intent(PrivacyContactUtils.SENT_SMS_ACTION);
                PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0,
                        sentIntent, 0);
                sms.sendTextMessage(number, null, content, sentPI, null);
            }
            LeoLog.d(TAG, "msm send success!");

            //记录短信发送次数
            int sendCount = LeoSettings.getInteger(PrefConst.KEY_SEND_MSM_COUNT, 0) + 1;
            LeoSettings.setInteger(PrefConst.KEY_SEND_MSM_COUNT, sendCount);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取新增联系人
     *
     * @return
     */
    @Override
    public Cursor getNewIncreaseContacts() {
        return null;
    }

    @Override
    public int getPrivacyContactCount() {

        return 0;
    }

    @Override
    public List<ContactCallLog> getPrivacyCallList() {
        return null;
    }

    @Override
    public int getPrivacyCallCount() {
        return 0;
    }

    @Override
    public List<MessageBean> getPrivacyMessage() {
        return null;
    }

    @Override
    public int getPrivacyMessageCount() {
        return 0;
    }

    @Override
    public boolean deletePrivacyCall(String phoneNumber) {
        return false;
    }

    @Override
    public boolean deletePrivacyMessage(String phoneNumber) {
        return false;
    }

    @Override
    public boolean clearPrivacyContacts() {
        return false;
    }

    @Override
    public boolean clearPrivacyCalls() {
        return false;
    }

    @Override
    public boolean clearPrivacyMessages() {
        return false;
    }

    @Override
    public boolean importCallAndMsms(MessageCallLogBean messageCall) {
        if (messageCall == null) {
            return false;
        }
        try {
            List<MessageBean> messages = messageCall.msms;
            List<ContactCallLog> calls = messageCall.calls;
            // 导入短信和通话记录
            if (messages != null && messages.size() != 0) {
                for (MessageBean message : messages) {
                    String contactNumber = message.getPhoneNumber();
                    String number = PrivacyContactUtils.simpleFromateNumber(contactNumber);
                    String name = message.getMessageName();
                    String body = message.getMessageBody();
                    String time = message.getMessageTime();
//                    String threadId = message.getMessageThreadId();
                    int isRead = 1;// 0未读，1已读
                    int type = message.getMessageType();// 短信类型1是接收到的，2是已发出
                    ContentValues values = new ContentValues();
                    values.put(Constants.COLUMN_MESSAGE_PHONE_NUMBER, number);
                    values.put(Constants.COLUMN_MESSAGE_CONTACT_NAME, name);
                    String bodyTrim = body.trim();
                    values.put(Constants.COLUMN_MESSAGE_BODY, bodyTrim);
                    values.put(Constants.COLUMN_MESSAGE_DATE, time);

                    int thread = PrivacyContactUtils.queryContactId(mContext, message.getPhoneNumber());
                    values.put(Constants.COLUMN_MESSAGE_THREAD_ID, thread);
                    values.put(Constants.COLUMN_MESSAGE_IS_READ, isRead);
                    values.put(Constants.COLUMN_MESSAGE_TYPE, type);
                    mContext.getContentResolver().insert(Constants.PRIVACY_MESSAGE_URI, values);
                    PrivacyContactUtils.deleteMessageFromSystemSMS("address = ?",
                            new String[]{
                                    number
                            }, mContext);
                }
            }
            // 导入通话记录
            if (calls != null && calls.size() != 0) {
                for (ContactCallLog calllog : calls) {
                    String number = calllog.getCallLogNumber();
                    String name = calllog.getCallLogName();
                    String date = calllog.getClallLogDate();
                    int type = calllog.getClallLogType();
                    ContentValues values = new ContentValues();
                    values.put(Constants.COLUMN_CALL_LOG_PHONE_NUMBER, number);
                    values.put(Constants.COLUMN_CALL_LOG_CONTACT_NAME, name);
                    values.put(Constants.COLUMN_CALL_LOG_DATE, date);
                    values.put(Constants.COLUMN_CALL_LOG_TYPE, type);
                    values.put(Constants.COLUMN_CALL_LOG_IS_READ, 1);
                    values.put(Constants.COLUMN_CALL_LOG_DURATION, calllog.getCallLogDuraction());
                    mContext.getContentResolver().insert(Constants.PRIVACY_CALL_LOG_URI, values);
                    PrivacyContactUtils.deleteCallLogFromSystem("number LIKE ?", number, mContext);
                }
            }
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public void reportSendMsm() {
        AppMasterApplication context = AppMasterApplication.getInstance();
        String version = DeviceUtil.getAndroidVersion();
        String vender = DeviceUtil.getVendor();
        String deviceName = DeviceUtil.getDeviceName();
        String apiLevel = DeviceUtil.getApiLevel();
        String appVer = DeviceUtil.getAppVer(context);
        String channelCode = DeviceUtil.getChannelCode(context);
        String contry = DeviceUtil.getCountry(context);
        String language = DeviceUtil.getLanguage(context);

        StringBuilder sb = new StringBuilder();
        sb.append(version);
        sb.append(vender);
        sb.append(deviceName);
        sb.append(apiLevel);
        sb.append(appVer);
        sb.append(channelCode);
        sb.append(contry);
        sb.append(language);

        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "theft1", "message_$" + sb.toString());
    }
}
