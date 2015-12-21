
package com.leo.appmaster.privacycontact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.callfilter.CallFilterConstants;
import com.leo.appmaster.callfilter.CallFilterUtils;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.eventbus.event.PrivacyMessageEvent;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEORoundProgressDialog;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

public class AddFromCallLogListActivity extends BaseActivity implements OnItemClickListener {
    private static final String TAG = "AddFromCallLogListActivity";
    private List<ContactCallLog> mCallLogList;
    private CommonToolbar mComBar;
    private CallLogAdapter mCallLogAdapter;
    private ListView mListCallLog;
    private LEORoundProgressDialog mProgressDialog;
    private List<ContactCallLog> mAddPrivacyCallLog;
    private LEOAlarmDialog mAddCallLogDialog;
    private static final int ANSWER_TYPE = 1;
    private List<MessageBean> mAddMessages;
    private List<ContactCallLog> mAddCallLogs;
    private boolean mLogFlag = false;
    private ProgressBar mProgressBar;
    private LinearLayout mDefaultText;
    private Button mAutoDddBtn;
    private RippleView moto_add_btn_ripp;
    private AddFromCallHandler mAddFromCallHandler = new AddFromCallHandler();
    private String mFrom;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentValue = msg.what;
            if (currentValue >= mAddPrivacyCallLog.size()) {
                if (!mLogFlag) {
                    if (mProgressDialog != null) {
                        mProgressDialog.cancel();
                    }
                    AddFromContactListActivity
                            .notificationUpdatePrivacyContactList();
                    AddFromCallLogListActivity.this.finish();
                } else {
                    if (mProgressDialog != null) {
                        mProgressDialog.cancel();
                    }
                    mLogFlag = false;
                }
            } else {
                mProgressDialog.setProgress(currentValue);
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_privacy_call_log);
        handleIntent();
        initUI();
        sendMsgHandler();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mFrom = intent.getStringExtra(CallFilterConstants.FROMWHERE);
    }

    private void initUI() {
        mDefaultText = (LinearLayout) findViewById(R.id.add_call_log_default_tv);
        moto_add_btn_ripp = (RippleView) mDefaultText.findViewById(R.id.moto_add_btn_ripp);
        moto_add_btn_ripp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /* SDK */
                SDKWrapper.addEvent(AddFromCallLogListActivity.this, SDKWrapper.P1, "contactsadd",
                        "callsemptyadd");
                Intent intent = new Intent(AddFromCallLogListActivity.this,
                        PrivacyContactInputActivity.class);
                intent.putExtra(PrivacyContactInputActivity.TO_CONTACT_LIST, true);
                startActivity(intent);
            }
        });

        mAutoDddBtn = (Button) mDefaultText.findViewById(R.id.moto_add_btn);
        mComBar = (CommonToolbar) findViewById(R.id.add_privacy_call_log_title_bar);
        mComBar.setToolbarTitle(R.string.privacy_contact_popumenus_from_call_log);
        mComBar.setToolbarColorResource(R.color.cb);
        mListCallLog = (ListView) findViewById(R.id.add_privacy_call_logLV);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_loading);
        mComBar.setOptionMenuVisible(true);
        mComBar.setOptionImageResource(R.drawable.mode_done);

        mComBar.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mCallLogList != null && mCallLogList.size() > 0) {
                    if (mAddPrivacyCallLog.size() > 0 && mAddPrivacyCallLog != null) {
                        showProgressDialog(mAddPrivacyCallLog.size(), 0);
                        if (!Utilities.isEmpty(mFrom) &&
                                mFrom.equals(CallFilterConstants.FROM_BLACK_LIST)) {
                            sendImpLogHandler(CallFilterConstants.ADD_BLACK_LIST_MODEL);
                        } else {
                            sendImpLogHandler(PrivacyContactUtils.ADD_CONTACT_MODEL);
                        }
                    } else {
                        Toast.makeText(AddFromCallLogListActivity.this,
                                getResources().getString(R.string.privacy_contact_toast_no_choose),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mListCallLog.setOnItemClickListener(this);
        mCallLogList = new ArrayList<ContactCallLog>();
        mAddPrivacyCallLog = new ArrayList<ContactCallLog>();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        if (mProgressDialog != null) {
            mProgressDialog.cancel();
        }
        if (mAddCallLogDialog != null) {
            mAddCallLogDialog.cancel();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LeoEventBus.getDefaultBus().unregister(this);
        mListCallLog.post(new Runnable() {
            @Override
            public void run() {
                for (ContactCallLog callLog : mCallLogList) {
                    callLog.setCheck(false);
                }
            }
        });
        mHandler = null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        ContactCallLog callLog = mCallLogList.get(position);
        ImageView image = (ImageView) view.findViewById(R.id.calllog_item_check_typeIV);
        if (!callLog.isCheck()) {
            mAddPrivacyCallLog.add(callLog);
            image.setImageDrawable(getResources().getDrawable(R.drawable.select));
            callLog.setCheck(true);
        } else {
            mAddPrivacyCallLog.remove(callLog);
            image.setImageDrawable(getResources().getDrawable(R.drawable.unselect));
            callLog.setCheck(false);
        }
    }

    private class CallLogAdapter extends BaseAdapter {
        LayoutInflater relativelayout;
        List<ContactCallLog> callLog;

        public CallLogAdapter(List<ContactCallLog> callLog) {
            relativelayout = LayoutInflater.from(AddFromCallLogListActivity.this);
            this.callLog = callLog;
        }

        @Override
        public int getCount() {
            return (callLog != null) ? callLog.size() : 0;
        }

        @Override
        public Object getItem(int position) {

            return callLog.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        class ViewHolder {
            CircleImageView contactIcon;
            TextView name, date;
            ImageView checkImage, typeImage;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = relativelayout.inflate(R.layout.activity_add_privacy_call_log_item,
                        null);
                vh.name = (TextView) convertView.findViewById(R.id.add_from_call_log_item_nameTV);
                vh.date = (TextView) convertView
                        .findViewById(R.id.add_from_call_log_item_dateTV);
                vh.typeImage = (ImageView) convertView
                        .findViewById(R.id.call_log_type);
                vh.checkImage = (ImageView) convertView
                        .findViewById(R.id.calllog_item_check_typeIV);
                vh.contactIcon = (CircleImageView) convertView.findViewById(R.id.contactIV);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            ContactCallLog mb = callLog.get(position);
            if (mb.getCallLogName() != null && !mb.getCallLogName().equals("")) {
                vh.name.setText(mb.getCallLogName());
            } else {
                vh.name.setText(mb.getCallLogNumber());
            }
            vh.date.setText(mb.getClallLogDate());
            if (mb.getClallLogType() == CallLog.Calls.INCOMING_TYPE) {
                vh.typeImage.setImageResource(R.drawable.into_icon);
            } else if (mb.getClallLogType() == CallLog.Calls.OUTGOING_TYPE) {
                vh.typeImage.setImageResource(R.drawable.exhale_icon);
            } else if (mb.getClallLogType() == CallLog.Calls.MISSED_TYPE) {
                vh.typeImage.setImageResource(R.drawable.into_icon);
            }
            if (mb.isCheck()) {
                vh.checkImage.setImageResource(R.drawable.select);
            } else {
                vh.checkImage.setImageResource(R.drawable.unselect);
            }
            Bitmap icon = mb.getContactIcon();
            vh.contactIcon.setImageBitmap(icon);
            return convertView;
        }
    }

    /**
     * 向PG隐私联系人数据库中插入隐私联系人
     *
     * @param call
     */
    public void addContToSelfDb(ContactCallLog call) {
        String name = call.getCallLogName();
        String contactNumber = PrivacyContactUtils.simpleFromateNumber(call.getCallLogNumber());
        ContentResolver cr = this.getContentResolver();
        PrivacyContactManager pcm = PrivacyContactManager.getInstance(this);
        ContentValues values = new ContentValues();
        values.put(Constants.COLUMN_PHONE_NUMBER, contactNumber);
        values.put(Constants.COLUMN_CONTACT_NAME, name);
        values.put(Constants.COLUMN_PHONE_ANSWER_TYPE, ANSWER_TYPE);
        cr.insert(Constants.PRIVACY_CONTACT_URI, values);
        pcm.addContact(new ContactBean(0, name, contactNumber, null, null, null,
                false, ANSWER_TYPE, null, 0, 0, 0));
    }

    /*加载通话通话列表*/
    private void sendImpLogHandler(final String model) {
        if (mAddFromCallHandler != null) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    int isExistLog = PrivacyContactUtils.NO_EXIST_LOG;
                    try {
                        int count = 0;
                        ContentResolver cr = getContentResolver();
                        if (PrivacyContactUtils.ADD_CONTACT_MODEL.equals(model)) {
                            boolean added = false;
                            for (ContactCallLog call : mAddPrivacyCallLog) {
                                String contactNumber = call.getCallLogNumber();
                                /*隐私联系人去重,判断是否为隐私联系人*/
                                boolean isPryCont = false;
                                isPryCont = PrivacyContactUtils.pryContRemovSame(contactNumber);

                                if (!isPryCont) {
                                    addContToSelfDb(call);
                                    added = true;
                                    Context context = AddFromCallLogListActivity.this;
                                    /*4.4以上不去做短信操作*/
                                    boolean isLessLeve19 = PrivacyContactUtils.isLessApiLeve19();
                                    if (isLessLeve19) {
                                        if (mAddMessages == null) {
                                            mAddMessages = PrivacyContactManager.getInstance(context).queryMsmsForNumber(contactNumber);
                                        } else {
                                            List<MessageBean> addMessages = PrivacyContactManager.getInstance(context).queryMsmsForNumber(contactNumber);
                                            mAddMessages.addAll(addMessages);
                                        }
                                    }

                                    if (mAddCallLogs == null) {
                                        mAddCallLogs = PrivacyContactManager.getInstance(context).queryCallsForNumber(contactNumber);
                                    } else {
                                        List<ContactCallLog> addCalllog = PrivacyContactManager.getInstance(context).queryCallsForNumber(contactNumber);
                                        mAddCallLogs.addAll(addCalllog);
                                    }
                                    if (isExistLog == 0) {
                                        if ((mAddMessages != null && mAddMessages.size() != 0)
                                                || (mAddCallLogs != null && mAddCallLogs.size() != 0)) {
                                            isExistLog = PrivacyContactUtils.EXIST_LOG;
                                            mLogFlag = true;
                                        }
                                    }
                                } else {
                                    if (mAddPrivacyCallLog.size() == 1 && mAddPrivacyCallLog != null) {
                                        int evenId = EventId.EVENT_PRIVACY_EDIT_MODEL;
                                        String eventMsg = PrivacyContactUtils.ADD_CONTACT_FROM_CONTACT_NO_REPEAT_EVENT;
                                        PrivacyMessageEvent event = new PrivacyMessageEvent(evenId, eventMsg);
                                        LeoEventBus.getDefaultBus().post(event);
                                        isExistLog = PrivacyContactUtils.NO_EXIST_LOG;
                                        mLogFlag = false;
                                    }

                                }
//                                mLogFlag = true;
                                isPryCont = false;
                                if (mHandler != null) {
                                    Message messge = Message.obtain();
                                    count = count + 1;
                                    messge.what = count;
                                    mHandler.sendMessage(messge);
                                }
                                if (added) {
                                    // 通知更新隐私联系人列表
                                    String eventId = PrivacyContactUtils.PRIVACY_ADD_CONTACT_UPDATE;
                                    PrivacyEditFloatEvent edtEvent = new PrivacyEditFloatEvent(eventId);
                                    LeoEventBus.getDefaultBus().post(edtEvent);
                                    SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, "contactsadd", "callsadd");
                                }
                            }
                        } else if (CallFilterConstants.ADD_BLACK_LIST_MODEL.equals(model)) {
                            String blackNums = PreferenceTable.getInstance().getString("blackList");
                            LeoLog.d("testAddContact", "OLD blackNums:" + blackNums);
                            //TODO blacklist去重
                            //TODO 删除系统记录？
                            for (ContactCallLog contact : mAddPrivacyCallLog) {
                                String name = contact.getCallLogName();
                                String contactNumber = contact.getCallLogNumber();
                                String number = PrivacyContactUtils.simpleFromateNumber(contact.getCallLogNumber());
                                /*隐私联系人去重,判断是否为隐私联系人*/
                                boolean isPryCont = PrivacyContactUtils.pryContRemovSame(contactNumber);
                                if (!isPryCont) {
                                    boolean isHaveBlackNum =
                                            CallFilterUtils.checkIsHaveBlackNum(blackNums, number);
                                    if (!isHaveBlackNum) {
                                        String string = name + "_" + number;
                                        blackNums = blackNums + ":" + string;
                                        PreferenceTable.getInstance().
                                                putString("blackList", blackNums);
                                        LeoLog.d("testAddContact", "blackNums:" + blackNums);
                                    }
                                }
                                //cancel Process Dialog
                                Message messge = new Message();
                                count = count + 1;
                                messge.what = count;
                                if (messge != null && mHandler != null) {
                                    mHandler.sendMessage(messge);
                                }
                            }
                        } else if (PrivacyContactUtils.ADD_CALL_LOG_AND_MESSAGE_MODEL.equals(model)) {
                            // 导入通话记录
                            if (mAddCallLogs != null) {
                                for (ContactCallLog calllog : mAddCallLogs) {
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
                                    Uri callLogFlag = cr.insert(Constants.PRIVACY_CALL_LOG_URI, values);
                                    PrivacyContactUtils.deleteCallLogFromSystem("number LIKE ?", number,
                                            AddFromCallLogListActivity.this);
                                    if (callLogFlag != null && mHandler != null) {
                                        Message messge = Message.obtain();
                                        count = count + 1;
                                        messge.what = count;
                                        mHandler.sendMessage(messge);
                                    }
                                }

                            }
                            // 导入短信和通话记录
                            if (mAddMessages != null) {
                                for (MessageBean message : mAddMessages) {
                                    String number = message.getPhoneNumber();
                                    String name = message.getMessageName();
                                    String body = message.getMessageBody();
                                    String time = message.getMessageTime();
                                    String threadId = message.getMessageThreadId();
                                    int isRead = 1;// 0未读，1已读
                                    int type = message.getMessageType();// 短信类型1是接收到的，2是已发出
                                    ContentValues values = new ContentValues();
                                    values.put(Constants.COLUMN_MESSAGE_PHONE_NUMBER, number);
                                    values.put(Constants.COLUMN_MESSAGE_CONTACT_NAME, name);
                                    String bodyTrim = body.trim();
                                    values.put(Constants.COLUMN_MESSAGE_BODY, bodyTrim);
                                    values.put(Constants.COLUMN_MESSAGE_DATE, time);
                                    int thread = PrivacyContactUtils.queryContactId(
                                            AddFromCallLogListActivity.this, message.getPhoneNumber());
                                    values.put(Constants.COLUMN_MESSAGE_THREAD_ID, thread);
                                    values.put(Constants.COLUMN_MESSAGE_IS_READ, isRead);
                                    values.put(Constants.COLUMN_MESSAGE_TYPE, type);
                                    Uri messageFlag = cr.insert(Constants.PRIVACY_MESSAGE_URI, values);
                                    PrivacyContactUtils.deleteMessageFromSystemSMS("address = ?",
                                            new String[]{
                                                    number
                                            }, AddFromCallLogListActivity.this);
                                    if (messageFlag != null && mHandler != null) {
                                        Message messge = Message.obtain();
                                        count = count + 1;
                                        messge.what = count;
                                        mHandler.sendMessage(messge);
                                    }
                                }
                            }
                            if (mAddCallLogs != null && mAddCallLogs.size() != 0) {
                                LeoEventBus.getDefaultBus().post(
                                        new PrivacyEditFloatEvent(
                                                PrivacyContactUtils.UPDATE_CALL_LOG_FRAGMENT));
                            }
                            if (mAddMessages != null && mAddMessages.size() != 0) {

                                LeoEventBus.getDefaultBus().post(
                                        new PrivacyEditFloatEvent(
                                                PrivacyContactUtils.UPDATE_MESSAGE_FRAGMENT));
                            }
                            isExistLog = PrivacyContactUtils.NO_EXIST_LOG;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Message msg = new Message();
                    msg.what = PrivacyContactUtils.MSG_CALL_QU;
                    msg.arg1 = isExistLog;
                    mAddFromCallHandler.sendMessage(msg);
                }
            });
        }
    }

    private class PrivacyCallLogTask extends AsyncTask<String, Boolean, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... arg0) {
            boolean isOtherLogs = false;
            try {
                String flag = arg0[0];
                int count = 0;
                ContentResolver cr = getContentResolver();
                if (PrivacyContactUtils.ADD_CONTACT_MODEL.equals(flag)) {
                    boolean added = false;
                    PrivacyContactManager pcm = PrivacyContactManager
                            .getInstance(getApplicationContext());
                    for (ContactCallLog call : mAddPrivacyCallLog) {
                        String name = call.getCallLogName();
                        String contactNumber = call.getCallLogNumber();
                        String tempNumber = PrivacyContactUtils.formatePhoneNumber(contactNumber);

                        /*隐私联系人去重,判断是否为隐私联系人*/
                        boolean isPryCont = false;
                        isPryCont = PrivacyContactUtils.pryContRemovSame(contactNumber);

                        if (!isPryCont) {
                            addContToSelfDb(call);
                            added = true;
                        } else {
                            if (mAddPrivacyCallLog.size() == 1 && mAddPrivacyCallLog != null) {
                                int evenId = EventId.EVENT_PRIVACY_EDIT_MODEL;
                                String eventMsg = PrivacyContactUtils.ADD_CONTACT_FROM_CONTACT_NO_REPEAT_EVENT;
                                PrivacyMessageEvent event = new PrivacyMessageEvent(evenId, eventMsg);
                                LeoEventBus.getDefaultBus().post(event);
                                isOtherLogs = false;
                            }
                        }
                        Context context = AddFromCallLogListActivity.this;
                        if (mAddMessages == null) {
                            mAddMessages = PrivacyContactManager.getInstance(context).queryMsmsForNumber(contactNumber);
                        } else {
                            List<MessageBean> addMessages = PrivacyContactManager.getInstance(context).queryMsmsForNumber(contactNumber);
                            mAddMessages.addAll(addMessages);
                        }

                        if (mAddCallLogs == null) {
                            mAddCallLogs = PrivacyContactManager.getInstance(context).queryCallsForNumber(contactNumber);
                        } else {
                            List<ContactCallLog> addCalllog = PrivacyContactManager.getInstance(context).queryCallsForNumber(contactNumber);
                            mAddCallLogs.addAll(addCalllog);
                        }
                        if (!isOtherLogs) {
                            if ((mAddMessages != null && mAddMessages.size() != 0)
                                    || (mAddCallLogs != null && mAddCallLogs.size() != 0)) {
                                isOtherLogs = true;
                                mLogFlag = isOtherLogs;
                            }
                        }
                        /*子线程之前赋值*/
                        mLogFlag = true;
                        isPryCont = false;
                        if (mHandler != null) {
                            Message messge = Message.obtain();
                            count = count + 1;
                            messge.what = count;
                            mHandler.sendMessage(messge);
                        }
                        if (added) {
                            // 通知更新隐私联系人列表
                            String eventId = PrivacyContactUtils.PRIVACY_ADD_CONTACT_UPDATE;
                            PrivacyEditFloatEvent edtEvent = new PrivacyEditFloatEvent(eventId);
                            LeoEventBus.getDefaultBus().post(edtEvent);
                            SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, "contactsadd", "callsadd");
                        }
                    }
                } else if (PrivacyContactUtils.ADD_CALL_LOG_AND_MESSAGE_MODEL.equals(flag)) {
                    // 导入通话记录
                    if (mAddCallLogs != null) {
                        for (ContactCallLog calllog : mAddCallLogs) {
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
                            Uri callLogFlag = cr.insert(Constants.PRIVACY_CALL_LOG_URI, values);
                            PrivacyContactUtils.deleteCallLogFromSystem("number LIKE ?", number,
                                    AddFromCallLogListActivity.this);
                            if (callLogFlag != null && mHandler != null) {
                                Message messge = Message.obtain();
                                count = count + 1;
                                messge.what = count;
                                mHandler.sendMessage(messge);
                            }
                        }

                    }
                    // 导入短信和通话记录
                    if (mAddMessages != null) {
                        for (MessageBean message : mAddMessages) {
                            String number = message.getPhoneNumber();
                            String name = message.getMessageName();
                            String body = message.getMessageBody();
                            String time = message.getMessageTime();
                            String threadId = message.getMessageThreadId();
                            int isRead = 1;// 0未读，1已读
                            int type = message.getMessageType();// 短信类型1是接收到的，2是已发出
                            ContentValues values = new ContentValues();
                            values.put(Constants.COLUMN_MESSAGE_PHONE_NUMBER, number);
                            values.put(Constants.COLUMN_MESSAGE_CONTACT_NAME, name);
                            String bodyTrim = body.trim();
                            values.put(Constants.COLUMN_MESSAGE_BODY, bodyTrim);
                            values.put(Constants.COLUMN_MESSAGE_DATE, time);
                            int thread = PrivacyContactUtils.queryContactId(
                                    AddFromCallLogListActivity.this, message.getPhoneNumber());
                            values.put(Constants.COLUMN_MESSAGE_THREAD_ID, thread);
                            values.put(Constants.COLUMN_MESSAGE_IS_READ, isRead);
                            values.put(Constants.COLUMN_MESSAGE_TYPE, type);
                            Uri messageFlag = cr.insert(Constants.PRIVACY_MESSAGE_URI, values);
                            PrivacyContactUtils.deleteMessageFromSystemSMS("address = ?",
                                    new String[]{
                                            number
                                    }, AddFromCallLogListActivity.this);
                            if (messageFlag != null && mHandler != null) {
                                Message messge = Message.obtain();
                                count = count + 1;
                                messge.what = count;
                                mHandler.sendMessage(messge);
                            }
                        }
                    }
                    if (mAddCallLogs != null && mAddCallLogs.size() != 0) {
                        LeoEventBus.getDefaultBus().post(
                                new PrivacyEditFloatEvent(
                                        PrivacyContactUtils.UPDATE_CALL_LOG_FRAGMENT));
                    }
                    if (mAddMessages != null && mAddMessages.size() != 0) {

                        LeoEventBus.getDefaultBus().post(
                                new PrivacyEditFloatEvent(
                                        PrivacyContactUtils.UPDATE_MESSAGE_FRAGMENT));
                    }
                    isOtherLogs = false;
                }
            } catch (Exception e) {

            }

            return isOtherLogs;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            try {
                if (result) {
                    if (mProgressDialog != null) {
                        mProgressDialog.cancel();
                    }
                    String title = getResources().getString(
                            R.string.privacy_contact_add_log_dialog_title);
                    String content = getResources().getString(
                            R.string.privacy_contact_add_log_dialog_dialog_content);
                    showAddCallLogDialog(title, content);
                    mHandler = null;
                } else {
                    if (mProgressDialog != null) {
                        mProgressDialog.cancel();
                    }
                    AddFromContactListActivity.notificationUpdatePrivacyContactList();
                }
                super.onPostExecute(result);
            } catch (Exception e) {

            }
        }
    }

    private void showProgressDialog(int maxValue, int currentValue) {
        if (mProgressDialog == null) {
            mProgressDialog = new LEORoundProgressDialog(this);
        }
        String title = getResources().getString(R.string.privacy_contact_progress_dialog_title);
        String content = getResources().getString(R.string.privacy_contact_progress_dialog_content);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(content);
        mProgressDialog.setMax(maxValue);
        mProgressDialog.setProgress(currentValue);
        mProgressDialog.setButtonVisiable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        try {
            mProgressDialog.show();
        } catch (Exception e) {

        }
    }

    private void showAddCallLogDialog(String title, String content) {
        if (mAddCallLogDialog == null) {
            mAddCallLogDialog = new LEOAlarmDialog(this);
        }
        mAddCallLogDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                LeoLog.i("caocao", "laile");
                if (which == 1) {
                    mHandler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            int currentValue = msg.what;
                            int totalSize = 0;
                            if (mAddCallLogs != null) {
                                totalSize = mAddCallLogs.size();
                            }
                            if (mAddMessages != null) {
                                totalSize = totalSize + mAddMessages.size();
                            }
                            if (currentValue >= totalSize) {
                                if (mProgressDialog != null) {
                                    mProgressDialog.cancel();
                                }
                                AddFromCallLogListActivity.this.finish();
                            } else {
                                mProgressDialog.setProgress(currentValue);
                            }
                            super.handleMessage(msg);
                        }
                    };
                    int totalSize = 0;
                    if (mAddCallLogs != null) {
                        totalSize = mAddCallLogs.size();
                    }
                    if (mAddMessages != null) {
                        totalSize = totalSize + mAddMessages.size();
                    }
                    showProgressDialog(totalSize, 0);
//                    PrivacyCallLogTask task = new PrivacyCallLogTask();
//                    task.execute(PrivacyContactUtils.ADD_CALL_LOG_AND_MESSAGE_MODEL);
                    sendImpLogHandler(PrivacyContactUtils.ADD_CALL_LOG_AND_MESSAGE_MODEL);
                    SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, "contactsadd",
                            "import");
                } else if (which == 0) {
                    SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, "contactsadd",
                            "unimport");
                    if (mAddCallLogDialog != null) {
                        mAddCallLogDialog.cancel();
                    }
                    AddFromCallLogListActivity.this.finish();
                }

            }
        });
        mAddCallLogDialog.setCanceledOnTouchOutside(false);
        mAddCallLogDialog.setTitle(title);
        mAddCallLogDialog.setContent(content);
        try {
            mAddCallLogDialog.show();
        } catch (Exception e) {

        }
    }

    private class AddFromCallHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case PrivacyContactUtils.MSG_ADD_CALL:
                    if (msg.obj != null) {
                        LeoLog.i(TAG, "load  calls finish !");
                        List<ContactCallLog> calls = (List<ContactCallLog>) msg.obj;
                        if (mCallLogList != null) {
                            mCallLogList.clear();
                        }
                        mCallLogList = calls;
                        try {
                            if (mCallLogList != null && mCallLogList.size() > 0) {
                                mDefaultText.setVisibility(View.GONE);
                            } else {
                                mDefaultText.setVisibility(View.VISIBLE);
                            }
                            mProgressBar.setVisibility(View.GONE);
                            mCallLogAdapter = new CallLogAdapter(mCallLogList);
                            mListCallLog.setAdapter(mCallLogAdapter);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case PrivacyContactUtils.MSG_CALL_QU:
                    LeoLog.d("testCallLog", "come to MSG_CALL_QU");
                    try {
                        if (msg.arg1 == PrivacyContactUtils.EXIST_LOG) {
                            LeoLog.d("testCallLog", "EXIST_LOG");
                            if (mProgressDialog != null) {
                                mProgressDialog.cancel();
                            }
                            String title = getResources().getString(
                                    R.string.privacy_contact_add_log_dialog_title);
                            String content = getResources().getString(
                                    R.string.privacy_contact_add_log_dialog_dialog_content);
                            showAddCallLogDialog(title, content);
                            mHandler = null;
                        } else {
                            LeoLog.d("testCallLog", "EXIST_LOG else");
                            if (mProgressDialog != null) {
                                mProgressDialog.cancel();
                            }
                            AddFromContactListActivity.notificationUpdatePrivacyContactList();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /*加载通话通话列表*/
    private void sendMsgHandler() {
        if (mAddFromCallHandler != null) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    List<ContactCallLog> callLogList = PrivacyContactUtils.getSysCallLog(AddFromCallLogListActivity.this, null, null, false, false);
                    if (callLogList != null && callLogList.size() > 0) {
                        Collections.sort(callLogList, PrivacyContactUtils.mCallLogCamparator);
                    }
                    Message msg = new Message();
                    msg.what = PrivacyContactUtils.MSG_ADD_CALL;
                    msg.obj = callLogList;
                    mAddFromCallHandler.sendMessage(msg);
                }
            });
        }
    }

    private class PrivacyContactCallLogTask extends AsyncTask<String, Boolean, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... arg0) {
//             mCallLogList =PrivacyContactManager.getInstance(AddFromCallLogListActivity.this).getSysCalls();
            try {
                mCallLogList = PrivacyContactUtils.getSysCallLog(AddFromCallLogListActivity.this, null, null, false, false);
                if (mCallLogList != null && mCallLogList.size() > 0) {
                    Collections.sort(mCallLogList, PrivacyContactUtils.mCallLogCamparator);
                }
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            try {
                super.onPostExecute(result);
                if (mCallLogList != null && mCallLogList.size() > 0) {
                    mDefaultText.setVisibility(View.GONE);
                } else {
                    mDefaultText.setVisibility(View.VISIBLE);
                }
                mProgressBar.setVisibility(View.GONE);
                mCallLogAdapter = new CallLogAdapter(mCallLogList);
                mListCallLog.setAdapter(mCallLogAdapter);
            } catch (Exception e) {

            }
        }
    }
}
