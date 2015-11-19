
package com.leo.appmaster.privacycontact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
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
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.eventbus.event.PrivacyMessageEvent;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView1;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEORoundProgressDialog;

public class AddFromCallLogListActivity extends BaseActivity {
    private List<ContactCallLog> mCallLogList;
    private CommonToolbar mComBar;
    private CallLogAdapter mCallLogAdapter;
    private ListView mListCallLog;
    private Handler mHandler;
    private LEORoundProgressDialog mProgressDialog;
    private List<ContactCallLog> mAddPrivacyCallLog;
    private LEOAlarmDialog mAddCallLogDialog;
    private int mAnswerType = 1;
    private List<MessageBean> mAddMessages;
    private List<ContactCallLog> mAddCallLogs;
    private boolean mLogFlag = false;
    private ProgressBar mProgressBar;
    private LinearLayout mDefaultText;
    private Button mAutoDddBtn;
    private RippleView1 moto_add_btn_ripp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_privacy_call_log);
        mDefaultText = (LinearLayout) findViewById(R.id.add_call_log_default_tv);
        moto_add_btn_ripp = (RippleView1) mDefaultText.findViewById(R.id.moto_add_btn_ripp);
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
//        mAutoDddBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                /* SDK */
//                SDKWrapper.addEvent(AddFromCallLogListActivity.this, SDKWrapper.P1, "contactsadd",
//                        "callsemptyadd");
//                Intent intent = new Intent(AddFromCallLogListActivity.this,
//                        PrivacyContactInputActivity.class);
//                intent.putExtra(PrivacyContactInputActivity.TO_CONTACT_LIST, true);
//                startActivity(intent);
//            }
//        });
//        MaterialRippleLayout.on(mAutoDddBtn)
//                .rippleColor(getResources().getColor(R.color.blue_button_click))
//                .rippleAlpha(0.8f)
//                .rippleHover(true)
//                .create();
        mComBar = (CommonToolbar) findViewById(R.id.add_privacy_call_log_title_bar);
        mComBar.setToolbarTitle(R.string.privacy_contact_popumenus_from_call_log);
        mComBar.setToolbarColorResource(R.color.cb);
        mListCallLog = (ListView) findViewById(R.id.add_privacy_call_logLV);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_loading);
        mComBar.setOptionMenuVisible(true);
        mComBar.setOptionImageResource(R.drawable.mode_done);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int currentValue = msg.what;
                if (currentValue >= mAddPrivacyCallLog.size()) {
                    if (!mLogFlag) {
                        mProgressDialog.cancel();
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
        // if (mCallLogList != null && mCallLogList.size() > 0) {
        mComBar.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mCallLogList != null && mCallLogList.size() > 0) {
                    if (mAddPrivacyCallLog.size() > 0 && mAddPrivacyCallLog != null) {
                        showProgressDialog(mAddPrivacyCallLog.size(), 0);
                        PrivacyCallLogTask task = new PrivacyCallLogTask();
                        task.execute(PrivacyContactUtils.ADD_CONTACT_MODEL);
                    } else {
                        Toast.makeText(AddFromCallLogListActivity.this,
                                getResources().getString(R.string.privacy_contact_toast_no_choose),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // }
        mListCallLog.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
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
        });
        mCallLogList = new ArrayList<ContactCallLog>();
        mAddPrivacyCallLog = new ArrayList<ContactCallLog>();
        // 加载数据
        PrivacyContactCallLogTask callLogTask = new PrivacyContactCallLogTask();
        callLogTask.execute(PrivacyContactUtils.ADD_CONTACT_MODEL);
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
                    for (ContactCallLog message : mAddPrivacyCallLog) {
                        String name = message.getCallLogName();
                        String contactNumber = message.getCallLogNumber();
                        // 隐私联系人去重
                        String tempNumber =
                                PrivacyContactUtils.formatePhoneNumber(contactNumber);
                        boolean flagContact = false;
                        ArrayList<ContactBean> contacts = pcm.getPrivateContacts();
                        if (contacts != null && contacts.size() != 0
                                && contactNumber != null && !"".equals(contactNumber)) {
                            for (ContactBean contactBean : contacts) {
                                if (contactBean.getContactNumber() != null && tempNumber != null) {
                                    flagContact =
                                            contactBean.getContactNumber().contains(tempNumber);
                                }
                                if (flagContact) {
                                    break;
                                }
                            }
                        }

                        if (!flagContact) {
                            ContentValues values = new ContentValues();
                            values.put(Constants.COLUMN_PHONE_NUMBER, contactNumber);
                            values.put(Constants.COLUMN_CONTACT_NAME, name);
                            values.put(Constants.COLUMN_PHONE_ANSWER_TYPE, mAnswerType);
                            Uri uri = cr.insert(Constants.PRIVACY_CONTACT_URI, values);
                            pcm.addContact(new ContactBean(0, name, contactNumber, null, null, null,
                                    false, mAnswerType, null, 0, 0, 0));
                            added = true;
                        }
                        if (mAddMessages == null) {
                            mAddMessages = PrivacyContactUtils.getSysMessage(
                                    AddFromCallLogListActivity.this,
                                    "address LIKE ? ", new String[]{
                                            "%" + tempNumber
                                    }, true, false);
                        } else {
                            List<MessageBean> addMessages = PrivacyContactUtils.getSysMessage(
                                    AddFromCallLogListActivity.this,
                                    "address LIKE ?", new String[]{
                                            "%" + tempNumber
                                    }, true, false);
                            mAddMessages.addAll(addMessages);
                        }
                        if (mAddCallLogs == null) {
                            mAddCallLogs = PrivacyContactUtils.getSysCallLog(
                                    AddFromCallLogListActivity.this,
                                    "number LIKE ?", new String[]{
                                            "%" + tempNumber
                                    }, true, false);
                        } else {
                            List<ContactCallLog> addCalllog = PrivacyContactUtils.getSysCallLog(
                                    AddFromCallLogListActivity.this,
                                    "number LIKE ?", new String[]{
                                            "%" + tempNumber
                                    }, true, false);
                            mAddCallLogs.addAll(addCalllog);
                        }
                        if (!isOtherLogs) {
                            if ((mAddMessages != null && mAddMessages.size() != 0)
                                    || (mAddCallLogs != null && mAddCallLogs.size() != 0)) {
                                isOtherLogs = true;
                                mLogFlag = isOtherLogs;
                            }
                        }
                        if (flagContact) {
                            if (mAddPrivacyCallLog.size() == 1 && mAddPrivacyCallLog != null) {
                                LeoEventBus
                                        .getDefaultBus()
                                        .post(new PrivacyMessageEvent(
                                                EventId.EVENT_PRIVACY_EDIT_MODEL,
                                                PrivacyContactUtils.ADD_CONTACT_FROM_CONTACT_NO_REPEAT_EVENT));
                                isOtherLogs = false;
                            }
                        }
                        mLogFlag = true;// 子线程之前赋值
                        if (mHandler != null) {
                            Message messge = Message.obtain();
                            count = count + 1;
                            messge.what = count;
                            mHandler.sendMessage(messge);
                        }
                        flagContact = false;
                        if (added) {
                            // 通知更新隐私联系人列表
                            LeoEventBus
                                    .getDefaultBus()
                                    .post(new PrivacyEditFloatEvent(
                                            PrivacyContactUtils.PRIVACY_ADD_CONTACT_UPDATE));
                            SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, "contactsadd",
                                    "callsadd");
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
                if (which == 1) {
                    mHandler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            int currentValue = msg.what;
                            if (currentValue >= mAddPrivacyCallLog.size()) {
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
                    showProgressDialog(mAddMessages.size() + mAddCallLogs.size(), 0);
                    PrivacyCallLogTask task = new PrivacyCallLogTask();
                    task.execute(PrivacyContactUtils.ADD_CALL_LOG_AND_MESSAGE_MODEL);
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
