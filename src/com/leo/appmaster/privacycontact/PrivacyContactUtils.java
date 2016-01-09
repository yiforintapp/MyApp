
package com.leo.appmaster.privacycontact;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.PrivacyContactManagerImpl;
import com.leo.appmaster.phoneSecurity.AddSecurityNumberActivity;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NotificationUtil;
import com.leo.appmaster.utils.Utilities;
import com.leo.imageloader.utils.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivacyContactUtils {
    public static final int MSG_CALL = 10000;
    public static final int MSG_CONTACT = 10001;
    public static final int MSG_MESSAGE = 10002;
    public static final int MSG_ADD_CALL = 10005;
    public static final int MSG_ADD_CONTACT = 10006;
    public static final int MSG_ADD_MSM = 10007;
    public static final int MSG_ADD_SECURITY_CONTACT = 10008;
    public static final int MSG_CALL_QU = 10009;
    public static final int MSG_MSM_QU = 10010;
    public static final int MSG_CONTACT_QU = 10011;
    public static final int MSG_AUTO_ADD_QU = 10012;
    public static final int MSG_CONTACT_DE = 10013;
    public static final int MSG_EDIT_CONTACT = 10014;
    public static final int MSG_EDIT_LOG = 10015;
    public static final int MSG_PRIVACY_CALL_HANDLER = 10016;
    /*隐私短信，通话未读通知id*/
    public static final int MSM_NOTIFI_NUMBER = 20140901;
    public static final int CALL_NOTIFI_NUMBER = 20140902;

    public static final int EXIST_LOG = 1;
    public static final int NO_EXIST_LOG = 0;
    /**
     * 格式化号码长度
     */
    public static final int NUM_LEGH = 7;


    public static final Uri SMS_INBOXS = Uri.parse("content://sms/");
    // public static final Uri SMS_INBOXS = Uri.parse("content://sms/inbox");
    public static final Uri SYS_SMS = Uri.parse("content://sms/inbox");
    public static final Uri CONTACT_INBOXS = Uri.parse("content://icc/adn");
    public static final Uri CONTACT_PHONE_URL = Phone.CONTENT_URI;
    public static final Uri CONTACT_URL = Contacts.CONTENT_URI;
    public static final Uri CALL_LOG_URI = android.provider.CallLog.Calls.CONTENT_URI;
    public static final String ADD_CONTACT_MODEL = "add_contact_model";
    public static final String ADD_CALL_LOG_AND_MESSAGE_MODEL = "add_call_log_and_message_model";
    public static final String ADD_CONTACT_FROM_CONTACT_NO_REPEAT_EVENT = "add_conact_from_contact_no_repeat_event";
    public final static String KEY_PLAY_ANIM = "play_anim";
    public static final String TO_PRIVACY_CONTACT = "to_privacy_contact";
    public static final String TO_PRIVACY_CONTACT_TAB = "to_privacy_contact_tab";
    public static final String TO_PRIVACY_MESSAGE_FLAG = "from_privacy_message";
    public static final String TO_PRIVACY_CALL_FLAG = "from_privacy_call";
    public static final String TO_PRIVACY_CONTACT_FLAG = "from_privacy_contact";
    public static final String FROM_MESSAGE_EVENT = "from_message_event";
    public static final String FROM_CALL_LOG_EVENT = "from_call_log_event";
    public static final String FROM_CONTACT_EVENT = "from_contact_event";
    public static final String FROM_CONTACT_NO_SELECT_EVENT = "from_contact_no_select_event";
    public static final String FROM_MESSAGE_NO_SELECT_EVENT = "from_message_no_select_event";
    public static final String CANCEL_EDIT_MODEL = "cancel_edit_model";
    public static final String EDIT_MODEL_OPERATION_RESTORE = "edit_model_operatioin_restore";
    public static final String CALL_LOG_EDIT_MODEL_OPERATION_DELETE = "call_log_edit_model_operatioin_delete";
    public static final String MESSAGE_EDIT_MODEL_OPERATION_DELETE = "message_edit_model_operatioin_delete";
    public static final String CONTACT_EDIT_MODEL_OPERATION_DELETE = "contact_edit_model_operatioin_delete";
    public static final String EDIT_MODEL_RESTOR_TO_SMS_CANCEL = "edit_model_restore_to_sms_cancel";
    public static final String MESSAGE_RECEIVER_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    public static final String MESSAGE_RECEIVER_ACTION2 = "android.provider.Telephony.SMS_RECEIVED_2";
    public static final String MESSAGE_RECEIVER_ACTION3 = "android.provider.Telephony.GSM_SMS_RECEIVED";
    public static final String CALL_RECEIVER_ACTION = "android.intent.action.PHONE_STATE";
    public static final String PHONE_BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";
    public static final String UPDATE_MESSAGE_FRAGMENT = "update_message_fragment";
    public static final String UPDATE_CALL_LOG_FRAGMENT = "update_call_log_fragment";
    public static final String RECEIVER_NOTIFICATION_OBSERVER_NO_INSTER_CALL_LOG = "receiver_notification_observer_no_inster_call_log";
    public static final String CONTACT_CALL_LOG = "contact_call_log";
    public static final String DELETE_MESSAGE = "delete_message";
    public static final String CONTACT_DETAIL_DELETE_LOG_UPDATE_CALL_LOG_LIST = "contact_detail_detele_log_update_call_log_list";
    public static final String RED_TIP = "red_tip";
    public static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    public static final int RECEIVE_MESSAGE = 1;
    public static final int SEND_MESSAGE = 2;
    public static final String NORMAL_ANSWER_TYPE = "normal_answer";
    public static final String HANG_UP_ANSWER_TYPE = "hang_up_answer";
    public static final String CONTACT_DETAIL_DELETE_LOG = "contact_detail_delete_log";
    public static final String CONTACT_EDIT_MODEL_DELETE_CONTACT_UPDATE = "contact_edit_model_delete_contact_update";
    public static final String CONTACT_DETAIL_DELETE_LOG_UPDATE_MESSAGE_LIST = "contact_detail_detele_log_update_message_list";
    public static final String RED_TIP_FLAG_MESSAGE = "message_flag";
    public static final String RED_TIP_FLAG_CALL_LOG = "call_log_flag";
    public static final String MESSAGE_PRIVACY_RECEIVER_MESSAGE = "privacy_receiver_message";
    public static final String MESSAGE_PRIVACY_RECEIVER_CALL_LOG = "privacy_receiver_call_log";
    public static final String MESSAGE_PRIVACY_INTERCEPT_NOTIFICATION = "message_privacy_intercept_notification";

    public static final String PRIVACY_RECEIVER_MESSAGE_NOTIFICATION = "privacy_receiver_message_notification";
    public static final String PRIVACY_RECEIVER_CALL_LOG_NOTIFICATION = "privacy_receiver_call_log_notification";
    public static final String PRIVACY_MESSAGE_ITEM_RUNING_NOTIFICATION = "message_item_runing_notification";
    public static final String PRIVACY_ADD_CONTACT_UPDATE = "add_contact_update";
    public static final String PRIVACY_ALL_CALL_NOTIFICATION_HANG_UP = "all_call_notification_hang_up";
    public static final String PRIVACY_INTERCEPT_CONTACT_EVENT = "intercept_contact_event";
    public static final String PRIVACY_EDIT_NAME_UPDATE_CALL_LOG_EVENT = "edit_name_udpate_call_log_event";
    public static final String PRIVACY_EDIT_NAME_UPDATE_MESSAGE_EVENT = "edit_name_udpate_message_event";
    public static final String PRIVACY_CONTACT_ACTIVITY_CANCEL_RED_TIP_EVENT = "privacy_contact_activity_cancel_red_tip";
    public static final String PRIVACY_CONTACT_ACTIVITY_CALL_LOG_CANCEL_RED_TIP_EVENT = "privacy_contact_activity_call_log_cancel_red_tip";
    public static final String PRIVACY_MSM_CALL_NOTI = "message_call_notifi";
    public static final String PRIVACY_MSM_NORI = "message_notifi";
    public static final String NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";

    public static final int ID = 0;

    public static final int DATE = 1;

    public static final int MESSAGE_COUNT = 2;

    public static final int RECIPIENT_IDS = 3;

    public static final int SNIPPET = 4;

    public static final int SNIPPET_CS = 5;

    public static final int READ = 6;

    public static final int TYPE = 7;

    public static final int ERROR = 8;

    public static final int HAS_ATTACHMENT = 9;

    /**
     * 获取系统短信列表
     *
     * @param context
     * @param selection
     * @param selectionArgs
     * @param isItemFlag,查询类型,true详细列表,false列表
     * @param ifFrequContacts,是否为频繁隐私联系人查询
     * @return
     */
    public static List<MessageBean> getSysMessage(Context context, String
            selection, String[] selectionArgs, boolean isItemFlag, boolean ifFrequContacts) {
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        List<MessageBean> messages = new ArrayList<MessageBean>();
        Map<String, MessageBean> messageList = new HashMap<String, MessageBean>();
        List<MessageBean> messageBeans = new ArrayList<MessageBean>();
        ContentResolver cr = context.getContentResolver();
        Cursor cur = null;

        try {
            PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
            cur = mgr.getSystemMessages(selection, selectionArgs);
            if (cur != null) {
                while (cur.moveToNext()) {
                    int addressColum = cur.getColumnIndex("address");
                    int idColum = cur.getColumnIndex("_id");
                    int threadIdColum = cur.getColumnIndex("thread_id");
                    int bodyColum = cur.getColumnIndex("body");
                    int typeColum = cur.getColumnIndex("type");
                    int dateColum = cur.getColumnIndex("date");

                    MessageBean mb = new MessageBean();
                    String number = null;
                    if (ifFrequContacts) {
                        number = simpleFromateNumber(cur.getString(addressColum));
                    } else {
                        number = cur.getString(addressColum);
                    }

                    Bitmap icon = PrivacyContactUtils.getContactIconFromSystem(context, number);
                    if (icon != null) {
                        int size = (int) context.getResources().getDimension(R.dimen.contact_icon_scale_size);
                        icon = PrivacyContactUtils.getScaledContactIcon(icon, size);
                        mb.setContactIcon(icon);
                    } else {
                        Drawable drawableIcon = context.getResources().getDrawable(R.drawable.default_user_avatar);
                        BitmapDrawable bitDrawIcon = (BitmapDrawable) drawableIcon;
                        Bitmap bitmapIcon = bitDrawIcon.getBitmap();
                        mb.setContactIcon(bitmapIcon);
                    }

                    long msmId = cur.getLong(idColum);

                    String threadId = cur.getString(threadIdColum);

                    String name = PrivacyContactUtils.getContactNameFromNumber(cr, number);

                    String body = cur.getString(bodyColum);

                    int type = cur.getInt(typeColum);

                    Date date = new Date(cur.getLong(dateColum));
                    String time = sfd.format(date);
                    mb.setMsmId(msmId);
                    mb.setMessageBody(body);
                    mb.setMessageName(name);
                    mb.setPhoneNumber(number);
                    mb.setMessageType(type);
                    mb.setMessageIsRead(1);
                    mb.setMessageThreadId(threadId);
                    mb.setMessageTime(time);
                    if (number != null) {
                        if (!isItemFlag) {
                            if (!Utilities.isEmpty(threadId)) {
                                messageBeans.add(mb);
                                if (!messageList.containsKey(threadId)) {
                                    messageList.put(threadId, mb);
                                }
                            }
                        } else {
                            messages.add(mb);
                        }
                    }
                }
                if (!isItemFlag) {
                    Iterable<MessageBean> it = messageList.values();
                    for (MessageBean mb : it) {
                        String threadId = mb.getMessageThreadId();
                        Cursor msgCur = null;
                        int msmCount;
                        try {
                            if (ifFrequContacts) {
//                                String selectionMsm = selection + " and thread_id = ?";
//                                String[] selectionArgsMsm = null;
//                                if (selectionArgs.length <= 1) {
//                                    selectionArgsMsm = new String[]{selectionArgs[0], threadId};
//                                } else if (selectionArgs.length <= 2) {
//                                    selectionArgsMsm = new String[]{selectionArgs[0], selectionArgs[1], threadId};
//                                }
//                                msgCur = mgr.getSystemMessages(selectionMsm, selectionArgsMsm);
                                msmCount = countMsm(messageBeans, threadId);
                            } else {
//                                msgCur = mgr.getSystemMessages("thread_id" + " = ? ", new String[]{threadId});
                                msmCount = countMsm(messageBeans, threadId);
                            }
//                            mb.setMessageCount(msgCur.getCount());
                            mb.setMessageCount(msmCount);
                            messages.add(mb);
                        } finally {
                            if (!BuildProperties.isApiLevel14()) {
                                IoUtils.closeSilently(msgCur);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cur);
            }
        }

        return messages;
    }

    /*计算每个联系人的短信个数*/
    private static int countMsm(List<MessageBean> messages, String threadId) {
        int count = 0;
        for (MessageBean message : messages) {
            String thread = message.getMessageThreadId();

            if (threadId == null) {
                break;
            }

            if (thread == null) {
                continue;
            }
            if (thread.equals(threadId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取系统联系人列表
     *
     * @param context
     * @param selection
     * @param selectionArgs
     * @param isFreContact  是否为频繁隐私联系人查询
     * @return 联系人列表
     */
    public static List<ContactBean> getSysContact(Context context, String selection,
                                                  String[] selectionArgs, boolean isFreContact) {

        List<ContactBean> contacts = new ArrayList<ContactBean>();
        ContentResolver cr = context.getContentResolver();
        Cursor phoneCursor = null;

//        if (Utilities.isEmpty(selection)) {
//            /*默认selection*/
//            selection = Contacts.IN_VISIBLE_GROUP + "=1 and "
//                    + Phone.HAS_PHONE_NUMBER + "=1 and "
//                    + Phone.DISPLAY_NAME + " IS NOT NULL";
//        }

        try {
            PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
            phoneCursor = mgr.getSystemContacts(selection, selectionArgs);
            boolean isEmptyCur = (phoneCursor != null && phoneCursor.getCount() > 0) ? false : true;
            if (!isEmptyCur) {
                while (phoneCursor.moveToNext()) {
                    String phoneNumber = null;
                    int numberColum = phoneCursor.getColumnIndex(Phone.NUMBER);
                    int nameColum = phoneCursor.getColumnIndex(Phone.DISPLAY_NAME);
                    int contactIdColum = phoneCursor.getColumnIndex(Phone.CONTACT_ID);
                    int photoIdColum = phoneCursor.getColumnIndex(Contacts.PHOTO_ID);
                    int sortKeyPriColum = phoneCursor.getColumnIndex(Phone.SORT_KEY_PRIMARY);

                    if (isFreContact) {
                        String number = phoneCursor.getString(numberColum);
                        phoneNumber = simpleFromateNumber(number);
                    } else {
                        phoneNumber = phoneCursor.getString(numberColum);
                    }

                    if (TextUtils.isEmpty(phoneNumber)) {
                        continue;
                    }

                    String contactName = phoneCursor.getString(nameColum);
                    Long contactId = phoneCursor.getLong(contactIdColum);
                    Long photoId = phoneCursor.getLong(photoIdColum);
                    Bitmap contactPhoto = null;
                    try {
                        if (photoId > 0) {
                            Uri uriBuilder = ContactsContract.Contacts.CONTENT_URI;
                            Uri uri = ContentUris.withAppendedId(uriBuilder, contactId);
                            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
                            contactPhoto = BitmapFactory.decodeStream(input);
                            int size = (int) context.getResources().getDimension(R.dimen.contact_icon_scale_size);
                            contactPhoto = PrivacyContactUtils.getScaledContactIcon(contactPhoto, size);
                        }
                    } catch (Error e) {
                    }

                    ContactBean cb = new ContactBean();
                    cb.setContactName(contactName);
                    cb.setContactNumber(phoneNumber);
                    cb.setContactIcon(contactPhoto);
                    String sortLetter = phoneCursor.getString(sortKeyPriColum);
                    if (sortLetter == null) {
                        sortLetter = "#";
                        cb.setSortLetter(sortLetter);
                    } else {
                        if (sortLetter.trim().substring(0, 1).toUpperCase().matches("[A-Z]")) {
                            cb.setSortLetter(sortLetter.toUpperCase());
                        } else {
                            sortLetter = "#";
                            cb.setSortLetter(sortLetter);
                        }
                    }
                    if (phoneNumber != null) {
                        contacts.add(cb);
                    } else {
                        contacts = null;
                    }
                }
            } else {
                /*查询结果为空，更换URL重新查询：CONTACT_URL = Contacts.CONTENT_URI*/
                if (Utilities.isEmpty(selection)) {
                    contacts = aginGetSysContact(cr, null, null);
                } else {
                    contacts = aginGetSysContact(cr, selection, selectionArgs);
                }
            }
        } catch (Exception e) {

        } catch (Error error) {

        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(phoneCursor);
            }
        }

        return contacts;
    }

    private static List<ContactBean> aginGetSysContact(ContentResolver cr, String selection, String[] selectionArgs) {

//        if (Utilities.isEmpty(selection)) {
//            selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1 and "
//                    + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1 and "
//                    + ContactsContract.Contacts.DISPLAY_NAME + " IS NOT NULL";
//        }
        if (selectionArgs == null && selectionArgs.length <= 0) {
            selectionArgs = null;
        }

        List<ContactBean> contacts = new ArrayList<ContactBean>();
        Cursor cursorContact = null;
        try {
            cursorContact = cr.query(CONTACT_URL, null, selection, selectionArgs, Phone.SORT_KEY_PRIMARY);
            if (cursorContact != null) {
                while (cursorContact.moveToNext()) {
                    String hasPhoneNumId = ContactsContract.Contacts.HAS_PHONE_NUMBER;
                    int hasPhoneNumberColum = cursorContact.getColumnIndex(hasPhoneNumId);
                    int nameColum = cursorContact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    int contactIdColum = cursorContact.getColumnIndex(ContactsContract.Contacts._ID);
                    int photoIdColum = cursorContact.getColumnIndex(Contacts.PHOTO_ID);
                    int sortKeyPriColum = cursorContact.getColumnIndex(Phone.SORT_KEY_PRIMARY);

                    String phoneNumber = cursorContact.getString(hasPhoneNumberColum);
                    if (TextUtils.isEmpty(phoneNumber)) {
                        continue;
                    }
                    String contactName = cursorContact.getString(nameColum);
                    Long contactid = cursorContact.getLong(contactIdColum);
                    Long photoid = cursorContact.getLong(photoIdColum);
                    Bitmap contactPhoto = null;
                    try {
                        if (photoid > 0) {
                            Uri uriBuidler = ContactsContract.Contacts.CONTENT_URI;
                            Uri uri = ContentUris.withAppendedId(uriBuidler, contactid);
                            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
                            contactPhoto = BitmapFactory.decodeStream(input);
                        }
                    } catch (Error e) {

                    }
                    ContactBean cb = new ContactBean();
                    cb.setContactName(contactName);
                    cb.setContactNumber(phoneNumber);
                    cb.setContactIcon(contactPhoto);
                    String sortLetter = cursorContact.getString(sortKeyPriColum);
                    if (sortLetter == null) {
                        sortLetter = "#";
                        cb.setSortLetter(sortLetter);
                    } else {
                        if (sortLetter.trim().substring(0, 1).toUpperCase().matches("[A-Z]")) {
                            cb.setSortLetter(sortLetter.toUpperCase());
                        } else {
                            sortLetter = "#";
                            cb.setSortLetter(sortLetter);
                        }
                    }

                    if (phoneNumber != null) {
                        contacts.add(cb);
                    } else {
                        contacts = null;
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursorContact);
            }
        }
        return contacts;
    }

    private static final String TAG = "PrivacyContactUtils";

    public static List<ContactCallLog> getSysCallLogNoContact(Context context, String selection, String[] selectionArgs, String sortOrder, boolean isDetailList, boolean isFreContacts) {
        long start = SystemClock.elapsedRealtime();
        List<ContactBean> contactsList = PrivacyContactUtils.getSysContact(context, null, null, false);
        LeoLog.d(TAG, "zany, getSysContact: " + (SystemClock.elapsedRealtime() - start));

        List<ContactCallLog> calllogs = new ArrayList<ContactCallLog>();
        try {
            start = SystemClock.elapsedRealtime();
            List<ContactCallLog> calls = getSysCallLog(context, selection, selectionArgs, sortOrder, isDetailList, isFreContacts);
            LeoLog.d(TAG, "zany, getSysCallLog: " + (SystemClock.elapsedRealtime() - start));
            if (calls == null || calls.size() <= 0) {
                return calllogs;
            }

            for (ContactCallLog call : calls) {

                String formateNumber = PrivacyContactUtils.formatePhoneNumber(call.getCallLogNumber());
                boolean isExistContact = false;
                for (ContactBean contactBean : contactsList) {
                    String contactNumber = contactBean.getContactNumber();
                    contactNumber = PrivacyContactUtils.simpleFromateNumber(contactNumber);
                    if (contactNumber != null && formateNumber != null && contactNumber.contains(formateNumber)) {
                        isExistContact = true;
                        break;
                    }
                }

                if (!isExistContact && call.getCallLogNumber() != null) {
                    calllogs.add(call);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return calllogs;
    }

    /**
     * 获取系统通话记录列表
     *
     * @param context
     * @param selection
     * @param selectionArgs
     * @param isDetailList
     * @param isFreContacts
     * @return
     */
    public static List<ContactCallLog> getSysCallLog(Context context, String selection, String[] selectionArgs, String sortOrder, boolean isDetailList, boolean isFreContacts) {
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        List<ContactCallLog> calllogs = new ArrayList<ContactCallLog>();
        Map<String, ContactCallLog> calllog = new HashMap<String, ContactCallLog>();
        List<ContactCallLog> callBeans = new ArrayList<ContactCallLog>();
        ContentResolver CR = context.getContentResolver();
        Cursor cursor = null;
        try {
            PrivacyContactManagerImpl mgr = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
            long start = SystemClock.elapsedRealtime();
            cursor = mgr.getSystemCalls(selection, selectionArgs, sortOrder);
            // LeoLog.d(TAG, "zany, getSystemCalls: " + (SystemClock.elapsedRealtime() - start));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    start = SystemClock.elapsedRealtime();
                    int idColum = cursor.getColumnIndex(CallLog.Calls._ID);
                    int numberColum = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                    int nameColum = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
                    int dateColum = cursor.getColumnIndex(CallLog.Calls.DATE);
                    int callTypeColum = cursor.getColumnIndex(CallLog.Calls.TYPE);
                    int callDurationColum = cursor.getColumnIndex(CallLog.Calls.DURATION);
                    // LeoLog.d(TAG, "zany, --getColumn: " + (SystemClock.elapsedRealtime() - start));

                    start = SystemClock.elapsedRealtime();
                    ContactCallLog callLog = new ContactCallLog();
                    int count = cursor.getCount();
                    String number = null;
                    if (isFreContacts) {
                        number = simpleFromateNumber(cursor.getString(numberColum));
                    } else {
                        number = cursor.getString(numberColum);
                    }
//                    if (!calllog.containsKey(number)) {
//                        calllog.put(number, callLog);
//                        start = SystemClock.elapsedRealtime();
                        Bitmap icon = PrivacyContactUtils.getContactIconFromSystem(context, number);
                        // LeoLog.d(TAG, "zany, --getIcon, icon: " + icon + " , " + (SystemClock.elapsedRealtime() - start));
                        if (icon != null) {
                            callLog.setContactIcon(icon);
                        } else {
                            BitmapDrawable bitDra = (BitmapDrawable) context.getResources().getDrawable(R.drawable.default_user_avatar);
                            Bitmap bitmapIcon = bitDra.getBitmap();
                            callLog.setContactIcon(bitmapIcon);
                        }
//                    } else {
//                        ContactCallLog log = calllog.get(number);
//                        callLog.setContactIcon(log.getContactIcon());
//                    }
                    String name = cursor.getString(nameColum);
                    int id = cursor.getInt(idColum);
                    Date date = new Date(Long.parseLong(cursor.getString(dateColum)));
                    String time = sfd.format(date);
                    long durationTime = cursor.getLong(callDurationColum);
                    int type = (cursor.getInt(callTypeColum));
                    //TODO
                    callLog.setCallLogId(id);
                    callLog.setCallLogCount(count);
                    callLog.setCallLogDuraction(durationTime);
                    callLog.setCallLogName(name);
                    callLog.setCallLogNumber(number);
                    callLog.setClallLogDate(time);
                    callLog.setClallLogType(type);
                    if (number != null) {
                        if (!isDetailList) {
                            // isDetailList:true--详细列表，false--列表
                            if (callLog != null) {
                                callBeans.add(callLog);
                                if (!calllog.containsKey(number)) {
                                    calllog.put(number, callLog);
                                }
                            }
                        } else {
                            calllogs.add(callLog);
                        }
                    }
                }
                start = SystemClock.elapsedRealtime();
                Iterable<ContactCallLog> it = calllog.values();
                for (ContactCallLog contactCallLog : it) {
                    /*查询内每个号码在通话记录中的条数*/
                    String number = contactCallLog.getCallLogNumber();
                    int countCall;
                    Cursor cur = null;
                    try {
                        if (isFreContacts) {
                            countCall = countCalls(callBeans, number);
                        } else {
                            countCall = countCalls(callBeans, number);
                        }
                        contactCallLog.setCallLogCount(countCall);
                        calllogs.add(contactCallLog);
                    } finally {
                        if (!BuildProperties.isApiLevel14()) {
                            IoUtils.closeSilently(cur);
                        }
                    }
                }
                // LeoLog.d(TAG, "zany, --for..: " + (SystemClock.elapsedRealtime() - start));
            }
        } catch (Exception e) {

        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }

        return calllogs;
    }

    private static int countCalls(List<ContactCallLog> calls, String number) {
        int count = 0;
        for (ContactCallLog call : calls) {
            String callNumber = formatePhoneNumber(call.getCallLogNumber());
            number = formatePhoneNumber(number);
            if (number == null) {
                break;
            }
            if (callNumber == null) {
                continue;
            }

            if (callNumber.equals(number)) {
                count++;
            }

        }
        return count;
    }

    /**
     * 得到手机SIM卡联系人信息
     * private static final String[] PHONES_PROJECTION = newString[] { Phone.DISPLAY_NAME, Phone.NUMBER,
     * Photo.PHOTO_ID,Phone.CONTACT_ID };
     *
     * @param cr
     * @param selection
     * @param selectionArgs
     * @return
     */
    private List<ContactBean> getSIMContacts(ContentResolver cr, String selection,
                                             String[] selectionArgs) {
        List<ContactBean> mSIMSContact = new ArrayList<ContactBean>();
        Cursor phoneCursor = null;
        try {
            phoneCursor = cr.query(CONTACT_INBOXS, null, selection, selectionArgs, null);
            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {
                    int numberColum = phoneCursor.getColumnIndex(Phone.NUMBER);
                    int nameColum = phoneCursor.getColumnIndex(Phone.DISPLAY_NAME);

                    ContactBean cb = new ContactBean();
                    String phoneNumber = phoneCursor.getString(numberColum);
                    if (TextUtils.isEmpty(phoneNumber)) {
                        continue;
                    }
                    String contactName = phoneCursor.getString(nameColum);
                    cb.setContactName(contactName);
                    cb.setContactNumber(phoneNumber);
                    mSIMSContact.add(cb);
                }
            } else {
                mSIMSContact = null;
            }
        } catch (Exception e) {

        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(phoneCursor);
            }
        }
        return mSIMSContact;
    }

    /**
     * String reverse
     */
    public static String stringReverse(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        StringBuffer sbf = new StringBuffer(str);
        return sbf.reverse().toString();
    }

    /**
     * 简单的格式化电话号码
     *
     * @param number
     * @return
     */
    public static String simpleFromateNumber(String number) {
        String fromateNumber = null;
        if (!TextUtils.isEmpty(number)) {
            String deleteSpaceNumber = number.replace(" ", "");
            fromateNumber = deleteSpaceNumber.replace("-", "");
        }
        return fromateNumber;
    }

    /**
     * 格式化电话号码(7位)
     *
     * @param number
     * @return
     */
    public static String formatePhoneNumber(String number) {
        String temp = simpleFromateNumber(number);
        String reverseStr = null;
        if (temp != null) {
            int strLength = temp.length();
            if (strLength >= NUM_LEGH) {
                String tempStr = stringReverse(temp);
                String subNumber = tempStr.substring(0, 7);
                reverseStr = stringReverse(subNumber);
            } else {
                reverseStr = temp;
            }
        }
        return reverseStr;
    }

    /**
     * 保持原有格式只是截取后7位
     *
     * @param number
     * @return
     */
    public static String truncationNumber(String number) {
        String temp = number;
        String reverseStr = null;
        if (temp != null) {
            int strLength = temp.length();
            if (strLength >= 7) {
                String tempStr = stringReverse(temp);
                String subNumber = tempStr.substring(0, 7);
                reverseStr = stringReverse(subNumber);
            } else {
                reverseStr = temp;
            }
        }
        return reverseStr;
    }

    /**
     * 格式化电话号码(4位)
     *
     * @param number
     * @return
     */
    public static String formatePhNumberFor4(String number) {
        String temp = simpleFromateNumber(number);
        String reverseStr = null;
        if (temp != null) {
            int strLength = temp.length();
            if (strLength >= 4) {
                String tempStr = stringReverse(temp);
                String subNumber = tempStr.substring(0, 4);
                reverseStr = stringReverse(subNumber);
            } else {
                reverseStr = temp;
            }
        }
        return reverseStr;
    }


    /**
     * 通过号码查询联系人名称
     *
     * @param contentResolver
     * @param number
     * @return
     */
    public static String getContactNameFromNumber(ContentResolver contentResolver, String number) {
        String phoneName = null;
        Cursor cursor = null;

        try {
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number);
            String[] selectionArgs = new String[]{PhoneLookup.DISPLAY_NAME};
            cursor = contentResolver.query(uri, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                phoneName = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
            }
        } catch (Exception e) {
        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }

        if (TextUtils.isEmpty(phoneName)) {
            phoneName = number;
        }
        return phoneName;
    }

    /**
     * 通过id获取联系人姓名
     *
     * @param contentResolver
     * @param id
     * @return
     */
    public static String getContactNameFromId(ContentResolver contentResolver, String id) {

        String phoneName = null;
        if (id != null && contentResolver != null) {
            Cursor cursor = null;
            try {
                String[] projection = new String[]{Phone.DISPLAY_NAME};
                String selection = Phone.CONTACT_ID + " = ?  ";
                String[] selectionArgs = new String[]{id};
                Uri uri = Phone.CONTENT_URI;
                cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        phoneName = cursor.getString(cursor.getColumnIndex("display_name"));
                    } else {
                        Context context = AppMasterApplication.getInstance();
                        String noNumber = context.getResources().getString(R.string.unknow_call_first_title);
                        phoneName = noNumber;
                    }
                }
            } catch (Exception e) {
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return phoneName;
    }


    /**
     * 向系统短信数据库中插入数据
     *
     * @param values
     * @param context
     */
    public static void insertMessageToSystemSMS(ContentValues values, Context context) {
        try {
            context.getContentResolver().insert(SYS_SMS, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除系统短信数据库数据
     *
     * @param selection
     * @param selectionArgs
     * @param context
     */
    public static void deleteMessageFromSystemSMS(String selection, String[] selectionArgs,
                                                  Context context) {
        try {
            context.getContentResolver().delete(SMS_INBOXS, selection, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除系统通话记录数据
     *
     * @param selection
     * @param selectionArgs
     * @param context
     * @return
     */
    public static int deleteCallLogFromSystem(String selection, String selectionArgs,
                                              Context context) {
        int flag = 0;
        try {
            String string = formatePhoneNumber(selectionArgs);
            String[] args = new String[]{"%" + string};
            flag = context.getContentResolver().delete(CALL_LOG_URI, selection, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 查询隐私联系人表
     *
     * @param mdb
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */

    public static List<ContactBean> queryContactFromMySelfTable(ContentResolver mdb, Uri uri,
                                                                String selection, String[] selectionArgs) {
        List<ContactBean> privacyContacts = new ArrayList<ContactBean>();
        Cursor cur = null;
        try {
            cur = mdb.query(uri, null, selection, selectionArgs, null);
            if (cur != null) {
                while (cur.moveToNext()) {
                    int numberColum = cur.getColumnIndex(Constants.COLUMN_PHONE_NUMBER);
                    int idColum = cur.getColumnIndex(Constants.COLUMN_CONTACT_ID);

                    String number = cur.getString(numberColum);
                    int contactId = cur.getInt(idColum);

                    ContactBean contact = new ContactBean();
                    contact.setContactNumber(number);
                    contact.setContactId(contactId);
                    privacyContacts.add(contact);
                }
            } else {
                privacyContacts = null;
            }
        } catch (Exception e) {

        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cur);
            }
        }

        return privacyContacts;
    }


    /**
     * 查询隐私短信表
     *
     * @param mdb
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static List<MessageBean> queryPrivacyMsm(ContentResolver mdb, String selection, String[] selectionArgs) {
        List<MessageBean> privacyMessages = new ArrayList<MessageBean>();
        Cursor cur = null;
        try {
            cur = mdb.query(Constants.PRIVACY_MESSAGE_URI, null, selection, selectionArgs, null);
            if (cur != null) {
                while (cur.moveToNext()) {
                    MessageBean mb = new MessageBean();
                    String number = cur.getString(cur
                            .getColumnIndex(Constants.COLUMN_MESSAGE_PHONE_NUMBER));
                    String threadId = cur.getString(cur
                            .getColumnIndex(Constants.COLUMN_MESSAGE_THREAD_ID));
                    String name = cur.getString(cur
                            .getColumnIndex(Constants.COLUMN_MESSAGE_CONTACT_NAME));
                    String body = cur.getString(cur.getColumnIndex(Constants.COLUMN_MESSAGE_BODY));
                    int type = cur.getInt(cur.getColumnIndex(Constants.COLUMN_MESSAGE_TYPE));
                    int isRead = cur.getInt(cur.getColumnIndex(Constants.COLUMN_MESSAGE_IS_READ));
                    String time = cur.getString(cur.getColumnIndex(Constants.COLUMN_MESSAGE_DATE));
                    mb.setMessageBody(body);
                    mb.setMessageName(name);
                    mb.setPhoneNumber(number);
                    mb.setMessageType(type);
                    mb.setMessageIsRead(isRead);
                    mb.setMessageThreadId(threadId);
                    mb.setMessageTime(time);
                    privacyMessages.add(mb);
                }
            } else {
                privacyMessages = null;
            }
        } catch (Exception e) {

        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cur);
            }
        }

        return privacyMessages;
    }


    /**
     * 查询隐私通话表
     *
     * @param mdb
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static List<ContactCallLog> queryPrivacyfCall(ContentResolver mdb,
                                                         String selection, String[] selectionArgs) {
        List<ContactCallLog> privacyCallLogs = new ArrayList<ContactCallLog>();
        Cursor cursor = null;
        try {
            cursor = mdb.query(Constants.PRIVACY_CALL_LOG_URI, null, selection, selectionArgs, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int numberColum = cursor.getColumnIndex(Constants.COLUMN_CALL_LOG_PHONE_NUMBER);
                    int nameColum = cursor.getColumnIndex(Constants.COLUMN_CALL_LOG_CONTACT_NAME);
                    int dateColum = cursor.getColumnIndex(Constants.COLUMN_CALL_LOG_DATE);
                    int typeColum = cursor.getColumnIndex(Constants.COLUMN_CALL_LOG_TYPE);

                    ContactCallLog callLog = new ContactCallLog();
                    String number = cursor.getString(numberColum);
                    String name = cursor.getString(nameColum);
                    String date = cursor.getString(dateColum);
                    callLog.setClallLogDate(date);
                    cursor.getInt(typeColum);
                    callLog.setCallLogName(name);
                    callLog.setCallLogNumber(number);
                    privacyCallLogs.add(callLog);
                }
            } else {
                privacyCallLogs = null;
            }
        } catch (Exception e) {

        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cursor);
            }
        }

        return privacyCallLogs;
    }

    /**
     * 查询指定数据库表中记录数量
     *
     * @param cr
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static int queryDbLogCount(ContentResolver cr, Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        Cursor cur = null;
        try {
            cur = cr.query(uri, null, selection, selectionArgs, null);
            if (cur != null) {
                count = cur.getCount();
            }
        } catch (Exception e) {

        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cur);
            }
        }

        return count;

    }


    /**
     * 删除指定数据库表记录
     *
     * @param mdb
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static int deleteDbLog(ContentResolver mdb, Uri uri,
                                  String selection, String[] selectionArgs) {
        int number = mdb.delete(uri, selection, selectionArgs);
        return number;

    }

    /**
     * 向制定数据库表中插入数据
     *
     * @param cr
     * @param url
     * @param values
     */
    public static Uri insertDbLog(ContentResolver cr, Uri url, ContentValues values) {
        return cr.insert(url, values);
    }


    // delete contact from myself

    /**
     * 删除隐私联系人您表数据
     *
     * @param selection
     * @param selectionArgs
     * @param context
     * @return
     */
    public static int deleteContactFromMySelf(String selection, String selectionArgs,
                                              Context context) {
        int number = -1;
        try {
            String[] seleArgs = new String[]{selectionArgs};
            number = deleteDbLog(context.getContentResolver(), Constants.PRIVACY_CONTACT_URI, selection, seleArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return number;
    }


    /**
     * 删除隐私通话表记录
     *
     * @param selection
     * @param selectionArgs
     * @param context
     * @return
     */
    public static int deleteCallLogFromMySelf(String selection, String selectionArgs,
                                              Context context) {
        int number = -1;
        try {
            String[] seleArgs = new String[]{selectionArgs};
            number = deleteDbLog(context.getContentResolver(), Constants.PRIVACY_CALL_LOG_URI, selection, seleArgs);
        } catch (Exception e) {

        }

        return number;
    }

    /**
     * 向系统通话记录中插入数据
     *
     * @param cr
     * @param values
     */
    public static void insertCallLogToSystem(ContentResolver cr, ContentValues values) {
        try {
            cr.insert(CALL_LOG_URI, values);
        } catch (Exception e) {
        }

    }

    /**
     * 将图片转换成字节数组
     *
     * @param bmp
     * @return
     */

    public static byte[] formateImg(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (bmp != null) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        }
        return baos.toByteArray();
    }

    /**
     * 从数据库中读取图片转换成Bitmap类型
     *
     * @param in
     * @return
     */
    public static Bitmap getBmp(byte[] in) {
        Bitmap bmpout = BitmapFactory.decodeByteArray(in, 0, in.length);
        return bmpout;
    }

    /**
     * 通话记录时间排序
     */
    public static Comparator<ContactCallLog> mCallLogCamparator = new Comparator<ContactCallLog>() {

        public final int compare(ContactCallLog a, ContactCallLog b) {
            try {
                if (new Date(a.getClallLogDate()).before(new Date(b.getClallLogDate())))
                    return 1;
                if (new Date(a.getClallLogDate()).after(new Date(b.getClallLogDate())))
                    return -1;
                return 0;
            } catch (Exception e) {
                return 0;
            }
        }
    };

    /**
     * 短信时间排序
     */
    public static Comparator<MessageBean> mMessageCamparator = new Comparator<MessageBean>() {

        public final int compare(MessageBean a, MessageBean b) {
            try {
                if (new Date(a.getMessageTime()).before(new Date(b.getMessageTime())))
                    return 1;
                if (new Date(a.getMessageTime()).after(new Date(b.getMessageTime())))
                    return -1;
                return 0;
            } catch (Exception e) {
                return 0;
            }
        }
    };

    /**
     * 短信时间升序排序
     */
    public static Comparator<MessageBean> mMessageAscCamparator = new Comparator<MessageBean>() {

        public final int compare(MessageBean a, MessageBean b) {
            try {
                if (new Date(a.getMessageTime()).before(new Date(b.getMessageTime())))
                    return -1;
                if (new Date(a.getMessageTime()).after(new Date(b.getMessageTime())))
                    return 1;
                return 0;
            } catch (Exception e) {
                return 0;
            }
        }
    };

    /**
     * 从隐私联系人中获取联系人头像
     *
     * @param context
     * @param number
     * @return
     */
    public static Bitmap getContactIcon(Context context, String number) {
        Bitmap contactIcon = null;
        String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
        Cursor cur = null;
        try {
            cur = context.getContentResolver().query(Constants.PRIVACY_CONTACT_URI, null,
                    "contact_phone_number LIKE ? ",
                    new String[]{
                            "%" + formateNumber
                    }, null);
            if (cur != null) {
                while (cur.moveToNext()) {
                    byte[] icon = cur.getBlob(cur.getColumnIndex(Constants.COLUMN_ICON));
                    if (icon != null) {
                        contactIcon = PrivacyContactUtils.getBmp(icon);
                    } else {
                        BitmapDrawable drawable = (BitmapDrawable) context.getResources()
                                .getDrawable(
                                        R.drawable.default_user_avatar);
                        contactIcon = drawable.getBitmap();
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cur);
            }
        }
        return contactIcon;
    }

    /**
     * 从系统中获取联系人头像
     *
     * @param context
     * @param number
     * @return
     */
    public static Bitmap getContactIconFromSystem(Context context, String number) {
        Bitmap contactIcon = null;
//        String formateNumber = PrivacyContactUtils.simpleFromateNumber(number);
//        Cursor cur = null;
//        try {
//            cur = context.getContentResolver().query(CONTACT_PHONE_URL, null,
//                    Phone.NUMBER + " LIKE ? ",
//                    new String[]{
//                            "%" + formateNumber
//                    }, null);
//            if (cur != null) {
//                while (cur.moveToNext()) {
//                    Long contactid =
//                            cur.getLong(cur.getColumnIndex(Phone.CONTACT_ID));
//                    Long photoid =
//                            cur.getLong(cur.getColumnIndex("photo_id"));
//                    if (photoid > 0) {
//                        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
//                                contactid);
//                        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
//                                context.getContentResolver(),
//                                uri);
//                        contactIcon = BitmapFactory.decodeStream(input);
//                    }
//                }
//            }
//        } catch (Exception e) {
//
//        } finally {
//            if (!BuildProperties.isApiLevel14()) {
//                IoUtils.closeSilently(cur);
//            }
//        }
        long contactId = getIdFroNum(number);
        Bitmap contactPhoto = null;
        ContentResolver conRe = context.getContentResolver();
        if (contactId > 0) {
            try {
                Uri uriBuilder = ContactsContract.Contacts.CONTENT_URI;
                Uri uri = ContentUris.withAppendedId(uriBuilder, contactId);
                InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(conRe, uri);
                contactPhoto = BitmapFactory.decodeStream(input);
                int size = (int) context.getResources().getDimension(R.dimen.contact_icon_scale_size);
                contactIcon = PrivacyContactUtils.getScaledContactIcon(contactPhoto, size);
            } catch (Error e) {
            }
        }
        return contactIcon;
    }

    /**
     * 通过电话号码获取系统联系人ID
     *
     * @param number
     * @return
     */
    private static long getIdFroNum(String number) {
        if (TextUtils.isEmpty(number)) {
            return -1;
        }
        String numSelcts = null;
        String selArgs = null;
        if (number.length() >= PrivacyContactUtils.NUM_LEGH) {
            number = PrivacyContactUtils.formatePhoneNumber(number);
            numSelcts = " LIKE ? ";
            selArgs = "%" + number;
        } else {
            numSelcts = " = ? ";
            selArgs = number;
        }
        String selects = Phone.NUMBER + numSelcts;
        String[] selectArgs = new String[]{selArgs};
        Uri uri = CONTACT_PHONE_URL;
        String[] projection = new String[]{Phone.CONTACT_ID};
        String sortOrder = null;
        Context context = AppMasterApplication.getInstance();
        Cursor cur = null;

        try {
            cur = context.getContentResolver().query(uri, projection, selects, selectArgs, sortOrder);
            if (cur != null && cur.getCount() > 0) {
                while (cur.moveToFirst()) {
                    return cur.getLong(cur.getColumnIndex(Phone.CONTACT_ID));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        return -1;
    }


    /**
     * 查询短信会话列表
     *
     * @param context
     * @return
     */
    public static List<MessageBean> queryMessageList(Context context) {
        List<MessageBean> messageList = new ArrayList<MessageBean>();
        SimpleDateFormat sfd = new
                SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        // 查询canonical_address表
        Cursor cur = context.getContentResolver().query(
                Uri.parse("content://mms-sms/canonical-addresses"), null, null, null, null);
        if (cur != null && cur.getCount() > 0) {
            while (cur.moveToNext()) {
                MessageBean message = new MessageBean();
                String id = String.valueOf(cur.getInt(cur.getColumnIndex("_id")));
                // 通过_id查询thread表中对应的会话
                Cursor cr = null;
                try {
                    cr = context.getContentResolver().query(PrivacyContactUtils.SMS_INBOXS,
                            new String[]{
                                    " * from threads--"
                            }, null, null, null);
                } catch (Exception e) {

                }
                String number = cur.getString(cur.getColumnIndex("address"));
                message.setPhoneNumber(number);
                /**
                 * getContactNameFromNumber
                 */
                String name =
                        PrivacyContactUtils.getContactNameFromNumber(context.getContentResolver(),
                                number);
                message.setMessageName(name);
                if (name == null || "".equals(name)) {
                    message.setMessageName(number);
                } else {
                    message.setMessageName(name);
                }
                Bitmap icon = getContactIconFromSystem(context, number);
                if (icon != null) {
                    message.setContactIcon(icon);
                } else {
                    message.setContactIcon(((BitmapDrawable) context.getResources().getDrawable(
                            R.drawable.default_user_avatar)).getBitmap());
                }

                try {
                    if (cr != null && cr.getCount() > 0) {
                        while (cr.moveToNext()) {
                            long date = cr.getLong(DATE);
                            String snippet = cr.getString(SNIPPET);
                            String recipIDs = cr.getString(RECIPIENT_IDS);
                            if (recipIDs != null && recipIDs.equals(id)) {
                                message.setMessageBody(snippet);
                                message.setMessageTime(sfd.format(date));
                                if (date > 0) {
                                    messageList.add(message);
                                }
                                break;
                            }
                        }
                    }
                } finally {
                    if (!BuildProperties.isApiLevel14()) {
                        IoUtils.closeSilently(cr);
                    }
                }
            }
        }
        if (!BuildProperties.isApiLevel14()) {
            IoUtils.closeSilently(cur);
        }
        return messageList;
    }


    /**
     * 隐私短信标记为已读
     *
     * @param read
     * @param selection
     * @param selectionArgs
     * @param context
     */
    public static void updateMessageMyselfIsRead(int read, String selection,
                                                 String[] selectionArgs, Context context) {
        ContentValues values = new ContentValues();
        values.put("message_is_read", read);
        int count = context.getContentResolver().update(Constants.PRIVACY_MESSAGE_URI,
                values, selection,
                selectionArgs);
        if (count > 0) {
            AppMasterPreference pre = AppMasterPreference.getInstance(context);
            for (int i = 0; i < count; i++) {
                int temp = pre.getMessageNoReadCount();
                if (temp > 0) {
                    temp = temp - 1;
                    pre.setMessageNoReadCount(temp);
                    if (temp <= 0) {
                        /* 没有未读去除隐私通知*/
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(PrivacyContactUtils.MSM_NOTIFI_NUMBER);
                        String eventId = PrivacyContactUtils.PRIVACY_CONTACT_ACTIVITY_CANCEL_RED_TIP_EVENT;
                        PrivacyEditFloatEvent editEvent = new PrivacyEditFloatEvent(eventId);
                        LeoEventBus.getDefaultBus().post(editEvent);
                        /* ISwipe处理：通知没有未读 */
                        PrivacyContactManager.getInstance(context).cancelPrivacyTipFromPrivacyMsm();
                    }
                }
            }
        }
    }

    /**
     * 获取隐私联系人列表
     *
     * @param mContext
     * @return
     */
    public static List<ContactBean> loadPrivateContacts(Context mContext) {
        List<ContactBean> mContacts = new ArrayList<ContactBean>();
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
            if (!BuildProperties.isApiLevel14()) {
                IoUtils.closeSilently(cur);
            }
        }

        return mContacts;
    }

    /**
     * 查询隐私联系人id
     *
     * @param context
     * @param number
     * @return
     */
    public static int queryContactId(Context context, String number) {
        int threadId = 0;
        if (number != null && !"".equals(number)) {
            String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
            List<ContactBean> contacts = PrivacyContactUtils.queryContactFromMySelfTable(
                    context.getContentResolver(),
                    Constants.PRIVACY_CONTACT_URI,
                    Constants.COLUMN_PHONE_NUMBER + " LIKE ? ", new String[]{
                            "%" + formateNumber
                    });
            if (contacts != null && contacts.size() > 0) {
                for (ContactBean contactBean : contacts) {
                    threadId = contactBean.getContactId();
                    break;
                }
            }
        }
        return threadId;
    }

    /**
     * 分页查询系统联系人信息
     *
     * @param pageSize      每页最大的数目
     * @param currentOffset 当前的偏移量
     * @return
     */
    public static List<ContactBean> getContactsByPage(int pageSize, int currentOffset,
                                                      Context context) {

        List<ContactBean> contacts = new ArrayList<ContactBean>();
        Cursor phoneCursor = null;
        try {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            phoneCursor = context.getContentResolver().query(
                    uri,
                    null,
                    null,
                    null,
                    " sort_key COLLATE LOCALIZED asc limit  " + pageSize + " offset "
                            + currentOffset);
            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {
                    // get phonenumber
                    String phoneNumber = phoneCursor
                            .getString(phoneCursor.getColumnIndex(Phone.NUMBER));
                    // IF IS NULL CONTINUE
                    if (TextUtils.isEmpty(phoneNumber)) {
                        continue;
                    }
                    // get name
                    String contactName = phoneCursor.getString(phoneCursor
                            .getColumnIndex(Phone.DISPLAY_NAME));
                    Long contactid =
                            phoneCursor.getLong(phoneCursor.getColumnIndex(Phone.CONTACT_ID));
                    Long photoid =
                            phoneCursor.getLong(phoneCursor.getColumnIndex("photo_id"));
                    Bitmap contactPhoto = null;
                    try {
                        if (photoid > 0) {
                            Uri uriContact = ContentUris.withAppendedId(
                                    ContactsContract.Contacts.CONTENT_URI,
                                    contactid);
                            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                                    context.getContentResolver(),
                                    uriContact);
                            contactPhoto = BitmapFactory.decodeStream(input);
                        }
                    } catch (Error e) {

                    }
                    ContactBean cb = new ContactBean();
                    cb.setContactName(contactName);
                    cb.setContactNumber(phoneNumber);
                    cb.setContactIcon(contactPhoto);
                    if (phoneNumber != null) {
                        contacts.add(cb);
                    }
                }

            }
        } catch (Exception e) {
        } finally {
            if (phoneCursor != null) {
                phoneCursor.close();
            }
        }

        return contacts;
    }

    /**
     * 获取隐私短信未读通话数量
     *
     * @param context
     * @param number
     * @return
     */
    public static int getNoReadMessage(Context context, String number) {
        int count = 0;
        String fromateNumber = PrivacyContactUtils.formatePhoneNumber(number);
        Cursor cur = null;
        try {
            cur = context.getContentResolver().query(Constants.PRIVACY_MESSAGE_URI, null,
                    Constants.COLUMN_MESSAGE_PHONE_NUMBER
                            + " LIKE ? and message_is_read = 0",
                    new String[]{
                            "%" + fromateNumber
                    }, null);
            if (cur != null) {
                count = cur.getCount();
            }
        } catch (Exception e) {
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        return count;
    }

    /**
     * 获取隐私通话未读数量
     *
     * @param context
     * @param number
     * @return
     */
    public static int getNoReadCallLogCount(Context context, String number) {
        int count = 0;
        String fromateNumber = PrivacyContactUtils.formatePhoneNumber(number);
        Cursor cur = null;
        try {
            cur = context.getContentResolver().query(Constants.PRIVACY_CALL_LOG_URI, null,
                    Constants.COLUMN_CALL_LOG_PHONE_NUMBER
                            + " LIKE ? and call_log_is_read = 0",
                    new String[]{
                            "%" + fromateNumber
                    }, null);
            if (cur != null) {
                count = cur.getCount();
            }
        } catch (Exception e) {
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        return count;
    }

    /**
     * 缩放联系人头像到指定大小
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getScaledContactIcon(Bitmap bitmap, int size) {
        if (bitmap == null) {
            return null;
        }
        if (bitmap.getWidth() > size || bitmap.getHeight() > size) {
            bitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
        }
        return bitmap;
    }

    /**
     * 隐私联系人去重
     *
     * @param number
     * @return
     */
    public static boolean pryContRemovSame(String number) {
        String tempNumber = PrivacyContactUtils.formatePhoneNumber(number);
        if (TextUtils.isEmpty(tempNumber)) {
            return false;
        }
        PrivacyContactManager pcm = PrivacyContactManager.getInstance(AppMasterApplication.getInstance());
        ArrayList<ContactBean> contacts = pcm.getPrivateContacts();
        boolean contIsNoEmpty = (contacts != null && contacts.size() > 0);
        if (contIsNoEmpty) {
            for (ContactBean contactBean : contacts) {
                if (contactBean.getContactNumber() != null) {
                    boolean isSameCont = contactBean.getContactNumber().contains(tempNumber);
                    if (isSameCont) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 当前android系统版本是否<=4.4
     *
     * @return
     */
    public static boolean isLessApiLeve19() {
        return Build.VERSION.SDK_INT < 19 ? true : false;
    }

    /**
     * 来电通知
     *
     * @param context
     * @param number
     */
    public static void callLogNotification(Context context, String number) {
        boolean callLogRuningStatus = AppMasterPreference.getInstance(
                context)
                .getCallLogItemRuning();
        if (callLogRuningStatus) {
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification();
            Intent intentPending = new Intent(context,
                    PrivacyContactActivity.class);
            intentPending.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentPending.putExtra(
                    PrivacyContactUtils.TO_PRIVACY_CONTACT,
                    PrivacyContactUtils.TO_PRIVACY_CALL_FLAG);
            intentPending.putExtra(PRIVACY_MSM_CALL_NOTI, true);
            PendingIntent contentIntent = PendingIntent
                    .getActivity(
                            context,
                            0,
                            intentPending,
                            PendingIntent.FLAG_UPDATE_CURRENT);
            notification.icon = R.drawable.ic_launcher_notification;
            notification.tickerText = context
                    .getString(R.string.privacy_contact_notification_title_big);
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notification
                    .setLatestEventInfo(
                            context,
                            context.getString(R.string.privacy_contact_notification_title_big),
                            context.getString(R.string.privacy_contact_notification_title_small),
                            contentIntent);
            NotificationUtil.setBigIcon(notification,
                    R.drawable.ic_launcher_notification_big);
            notification.when = System.currentTimeMillis();
            notificationManager.notify(PrivacyContactUtils.CALL_NOTIFI_NUMBER, notification);
            /* 隐私联系人有未读 通话时发送广播 */
            PrivacyContactManager.getInstance(context).privacyContactSendReceiverToSwipe(
                    PrivacyContactManager.PRIVACY_CALL, 0, number);
        }
    }

    public static void saveCallLog(ContactBean contact) {
        SimpleDateFormat dateFormate = new SimpleDateFormat(Constants.PATTERN_DATE);
        Context context = AppMasterApplication.getInstance();
        // 判断是否为5.0系统，特别处理
        if (Build.VERSION.SDK_INT >= 21) {
            ContentValues values = new ContentValues();
            values.put(Constants.COLUMN_CALL_LOG_PHONE_NUMBER, contact.getContactNumber());
            String name = contact.getContactName();

            if (!TextUtils.isEmpty(name)) {
                values.put(Constants.COLUMN_CALL_LOG_CONTACT_NAME, contact.getContactName());
            } else {
                values.put(Constants.COLUMN_CALL_LOG_CONTACT_NAME, contact.getContactNumber());
            }
            String date = dateFormate.format(System.currentTimeMillis());
            values.put(Constants.COLUMN_CALL_LOG_DATE, date);
            values.put(Constants.COLUMN_CALL_LOG_TYPE, CallLog.Calls.INCOMING_TYPE);
            values.put(Constants.COLUMN_CALL_LOG_IS_READ, 0);
            // 保存记录
            ContentResolver cr = context.getContentResolver();
            Uri uri = insertDbLog(cr, Constants.PRIVACY_CALL_LOG_URI, values);
            AppMasterPreference pre = AppMasterPreference.getInstance(context);
            int count = pre.getCallLogNoReadCount();
            if (count > 0) {
                pre.setCallLogNoReadCount(count + 1);
            } else {
                pre.setCallLogNoReadCount(1);
            }
            if (uri != null) {
                /*通知更新通话记录*/
                String eventMsg = PrivacyContactUtils.PRIVACY_ALL_CALL_NOTIFICATION_HANG_UP;
                PrivacyEditFloatEvent event = new PrivacyEditFloatEvent(eventMsg);
                LeoEventBus.getDefaultBus().post(event);

            }
            String eventMsgNoti = PrivacyContactUtils.PRIVACY_RECEIVER_CALL_LOG_NOTIFICATION;
            PrivacyEditFloatEvent eventNoti = new PrivacyEditFloatEvent(eventMsgNoti);
            LeoEventBus.getDefaultBus().post(eventNoti);
            /*发送通知*/
            PrivacyContactUtils.callLogNotification(context, contact.getContactNumber());
        }
    }
}
