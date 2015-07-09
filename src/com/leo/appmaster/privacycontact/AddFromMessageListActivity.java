
package com.leo.appmaster.privacycontact;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.eventbus.event.PrivacyMessageEvent;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOProgressDialog;
import com.leo.appmaster.utils.BuildProperties;

public class AddFromMessageListActivity extends BaseActivity implements OnItemClickListener {
    private ListView mListMessage;
    private MyMessageAdapter mAdapter;
    private List<MessageBean> mMessageList;
    private CommonTitleBar mTtileBar;
    private List<MessageBean> mAddPrivacyMessage;
    private LEOAlarmDialog mAddMessageDialog;
    private Handler mHandler;
    private LEOProgressDialog mProgressDialog;
    private ProgressBar mProgressBar;
    private List<MessageBean> mAddMessages;
    private List<ContactCallLog> mAddCallLogs;
    private int mAnswerType = 1;
    private boolean mLogFlag = false;
    private LinearLayout mDefaultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_privacy_message);
        mDefaultText = (LinearLayout) findViewById(R.id.add_message_default_tv);
        mTtileBar = (CommonTitleBar) findViewById(R.id.add_privacy_contact_title_bar);
        mTtileBar.openBackView();
        mTtileBar.setOptionImage(R.drawable.mode_done);
        mTtileBar.findViewById(R.id.tv_option_image).setBackgroundResource(
                R.drawable.privacy_title_bt_selecter);
        mTtileBar.setOptionListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mMessageList != null && mMessageList.size() > 0) {
                    if (mAddPrivacyMessage.size() > 0 && mAddPrivacyMessage != null) {
                        if (mHandler == null) {
                            mHandler = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    int currentValue = msg.what;
                                    if (currentValue >= mAddPrivacyMessage.size()) {
                                        if (!mLogFlag) {
                                            AddFromMessageListActivity.this.finish();
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
                        }
                        showProgressDialog(mAddPrivacyMessage.size(), 0);
                        PrivacyMessageTask task = new PrivacyMessageTask();
                        task.execute(PrivacyContactUtils.ADD_CONTACT_MODEL);
                    } else {

                        Toast.makeText(AddFromMessageListActivity.this,
                                getResources().getString(R.string.privacy_contact_toast_no_choose),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mTtileBar.setTitle(getResources()
                .getString(R.string.privacy_contact_popumenus_from_message));
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_loading);
        mMessageList = new ArrayList<MessageBean>();
        mAddPrivacyMessage = new ArrayList<MessageBean>();
        mListMessage = (ListView) findViewById(R.id.add_messageLV);
        mListMessage.setOnItemClickListener(this);
        AddMessageAsyncTask messgeTask = new AddMessageAsyncTask();
        messgeTask.execute(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListMessage.post(new Runnable() {

            @Override
            public void run() {
                if (mMessageList != null) {
                    for (MessageBean message : mMessageList) {
                        message.setCheck(false);
                    }
                }

            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = parent.getItemAtPosition(position);
        if (object instanceof MessageBean) {
            MessageBean mb = mMessageList.get(position);
            ImageView image = (ImageView) view.findViewById(R.id.message_item_check_typeIV);
            if (!mb.isCheck()) {
                mAddPrivacyMessage.add(mb);
                image.setImageDrawable(getResources().getDrawable(R.drawable.select));
                mb.setCheck(true);
            } else {
                mAddPrivacyMessage.remove(mb);
                image.setImageDrawable(getResources().getDrawable(R.drawable.unselect));
                mb.setCheck(false);
            }
        }
    }

    private class MyMessageAdapter extends BaseAdapter {
        LayoutInflater relativelayout;
        List<MessageBean> messages;

        public MyMessageAdapter(List<MessageBean> messages) {
            relativelayout = LayoutInflater.from(AddFromMessageListActivity.this);
            this.messages = messages;
        }

        @Override
        public int getCount() {

            return (messages != null) ? messages.size() : 0;
        }

        @Override
        public Object getItem(int position) {

            return messages.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        class ViewHolder {
            TextView name, content, date;
            ImageView checkView;
            CircleImageView contactIcon;
        }

        @SuppressLint("NewApi")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = relativelayout.inflate(R.layout.activity_add_privacy_message_item,
                        null);
                vh.name = (TextView) convertView.findViewById(R.id.message_item_nameTV);
                vh.content = (TextView) convertView.findViewById(R.id.message_item_contentTV);
                vh.date = (TextView) convertView.findViewById(R.id.message_list_dateTV);
                vh.checkView = (ImageView) convertView.findViewById(R.id.message_item_check_typeIV);
                vh.contactIcon = (CircleImageView) convertView.findViewById(R.id.contactIV);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            MessageBean mb = messages.get(position);
            if (mb.getMessageName() != null && !"".equals(mb.getMessageName())) {
                vh.name.setText(mb.getMessageName());
            } else {

                vh.name.setText(mb.getPhoneNumber());
            }
            vh.content.setText(mb.getMessageBody());
            SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd");
            Date tempDate = null;
            try {
                if (mb.getMessageTime() != null) {
                    tempDate = sdf.parse(mb.getMessageTime());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (tempDate != null) {
                vh.date.setText(sdf.format(tempDate));
            }
            if (mb.isCheck()) {
                vh.checkView.setImageResource(R.drawable.select);
            } else {
                vh.checkView.setImageResource(R.drawable.unselect);
            }
            Bitmap icon = mb.getContactIcon();
            vh.contactIcon.setImageBitmap(icon);
            return convertView;
        }

    }

    private void showAddMessageDialog(String title, String content) {
        if (mAddMessageDialog == null) {
            mAddMessageDialog = new LEOAlarmDialog(this);
        }
        mAddMessageDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    final int privacyTotal = mAddMessages.size() + mAddCallLogs.size();
                    if (mHandler == null) {
                        mHandler = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                int currentValue = msg.what;
                                if (currentValue >= privacyTotal) {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.cancel();
                                    }
                                    AddFromContactListActivity
                                            .notificationUpdatePrivacyContactList();
                                    AddFromMessageListActivity.this.finish();
                                } else {
                                    mProgressDialog.setProgress(currentValue);
                                }
                                super.handleMessage(msg);
                            }
                        };
                    }
                    showProgressDialog(privacyTotal, 0);
                    PrivacyMessageTask task = new PrivacyMessageTask();
                    task.execute(PrivacyContactUtils.ADD_CALL_LOG_AND_MESSAGE_MODEL);
                    SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, "contactsadd",
                            "import");
                } else if (which == 0) {
                    SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, "contactsadd",
                            "unimport");
                    if (mAddMessageDialog != null) {
                        mAddMessageDialog.cancel();
                    }
                    AddFromMessageListActivity.this.finish();
                }

            }
        });
        mAddMessageDialog.setCanceledOnTouchOutside(false);
        mAddMessageDialog.setTitle(title);
        mAddMessageDialog.setContent(content);
        mAddMessageDialog.show();
    }

    private class PrivacyMessageTask extends AsyncTask<String, Boolean, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... arg0) {
            String flag = arg0[0];
            int count = 0;
            boolean isOtherLogs = false;
            ContentResolver cr = getContentResolver();
            if (PrivacyContactUtils.ADD_CONTACT_MODEL.equals(flag)) {
                boolean added = false;
                PrivacyContactManager pcm = PrivacyContactManager
                        .getInstance(getApplicationContext());
                for (MessageBean message : mAddPrivacyMessage) {
                    String name = message.getMessageName();
                    String contactNumber = message.getPhoneNumber();
                    String numberFromMessage = PrivacyContactUtils.deleteOtherNumber(contactNumber);
                    // 隐私联系人去重
                    String tempNumber =
                            PrivacyContactUtils.formatePhoneNumber(numberFromMessage);
                    boolean flagContact = false;
                    ArrayList<ContactBean> contacts = pcm.getPrivateContacts();
                    if (contacts != null && contacts.size() != 0
                            && numberFromMessage != null && !"".equals(numberFromMessage)) {
                        for (ContactBean contactBean : contacts) {
                            if (tempNumber != null && contactBean.getContactNumber() != null) {
                                flagContact = contactBean.getContactNumber().contains(tempNumber);
                            }
                            if (flagContact) {
                                break;
                            }
                        }
                    }
                    if (!flagContact)
                    {
                        ContentValues values = new ContentValues();
                        values.put(Constants.COLUMN_PHONE_NUMBER, numberFromMessage);
                        values.put(Constants.COLUMN_CONTACT_NAME, name);
                        values.put(Constants.COLUMN_PHONE_ANSWER_TYPE, mAnswerType);
                        cr.insert(Constants.PRIVACY_CONTACT_URI, values);
                        pcm.addContact(new ContactBean(0, name, numberFromMessage, null, null,
                                null, false, mAnswerType, null));
                        added = true;
                    }
                    if (mAddMessages == null) {
                        mAddMessages = PrivacyContactUtils.getSysMessage(
                                AddFromMessageListActivity.this, cr,
                                "address LIKE ? ", new String[] {
                                    "%" + tempNumber
                                }, true);
                    } else {
                        List<MessageBean> addMessages = PrivacyContactUtils.getSysMessage(
                                AddFromMessageListActivity.this, cr,
                                "address LIKE ?", new String[] {
                                    "%" + tempNumber
                                }, true);
                        mAddMessages.addAll(addMessages);
                    }
                    if (mAddCallLogs == null) {
                        mAddCallLogs = PrivacyContactUtils.getSysCallLog(
                                AddFromMessageListActivity.this, cr,
                                "number LIKE ?", new String[] {
                                    "%" + tempNumber
                                });
                    } else {
                        List<ContactCallLog> addCalllog = PrivacyContactUtils.getSysCallLog(
                                AddFromMessageListActivity.this, cr,
                                "number LIKE ?", new String[] {
                                    "%" + tempNumber
                                });
                        mAddCallLogs.addAll(addCalllog);
                    }
                    if (!isOtherLogs) {
                        if ((mAddMessages != null && mAddMessages.size() != 0)
                                || (mAddCallLogs != null && mAddCallLogs.size() != 0)) {
                            isOtherLogs = true;
                        }
                    }
                    if (flagContact) {
                        if (mAddPrivacyMessage.size() == 1 && mAddPrivacyMessage != null) {
                            LeoEventBus
                                    .getDefaultBus()
                                    .post(
                                            new PrivacyMessageEvent(
                                                    PrivacyContactUtils.ADD_CONTACT_FROM_CONTACT_NO_REPEAT_EVENT));
                            isOtherLogs = false;
                            mLogFlag = isOtherLogs;
                        }
                    }
                    mLogFlag = true;
                    if (mHandler != null) {
                        Message messge = new Message();
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
                        PrivacyHelper.getInstance(getApplicationContext()).computePrivacyLevel(
                                PrivacyHelper.VARABLE_PRIVACY_CONTACT);
                        SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, "contactsadd",
                                "smsadd");
                    }
                }
            } else if (PrivacyContactUtils.ADD_CALL_LOG_AND_MESSAGE_MODEL.equals(flag)) {
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
                        values.put(Constants.COLUMN_MESSAGE_THREAD_ID, threadId);

                        int thread = PrivacyContactUtils.queryContactId(
                                AddFromMessageListActivity.this, message.getPhoneNumber());
                        values.put(Constants.COLUMN_MESSAGE_THREAD_ID, thread);
                        values.put(Constants.COLUMN_MESSAGE_TYPE, type);
                        Uri messageFlag = null;
                        try {
                            messageFlag = cr.insert(Constants.PRIVACY_MESSAGE_URI, values);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        PrivacyContactUtils.deleteMessageFromSystemSMS("address = ?",
                                new String[] {
                                    number
                                }, AddFromMessageListActivity.this);
                        if (messageFlag != null && mHandler != null) {
                            Message messge = new Message();
                            count = count + 1;
                            messge.what = count;
                            mHandler.sendMessage(messge);
                        }
                    }
                    // 更新联系人隐私短信
                    // for (MessageBean messageBean : mAddPrivacyMessage) {
                    // pm.removeSysMessage(messageBean);
                    // }
                }
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
                        Uri callLogFlag = null;
                        try {
                            callLogFlag = cr.insert(Constants.PRIVACY_CALL_LOG_URI, values);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        PrivacyContactUtils.deleteCallLogFromSystem("number LIKE ?", number,
                                AddFromMessageListActivity.this);
                        if (callLogFlag != null && mHandler != null) {
                            Message messge = new Message();
                            count = count + 1;
                            messge.what = count;
                            mHandler.sendMessage(messge);
                        }
                    }
                    List<String> addNumbers = new ArrayList<String>();
                    for (MessageBean messageBean : mAddPrivacyMessage) {
                        addNumbers.add(messageBean.getPhoneNumber());
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
            return isOtherLogs;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                String title = getResources().getString(
                        R.string.privacy_contact_add_log_dialog_title);
                String content = getResources().getString(
                        R.string.privacy_contact_add_log_dialog_dialog_content);
                showAddMessageDialog(title, content);
            } else {
                AddFromContactListActivity.notificationUpdatePrivacyContactList();
            }
            if (mProgressDialog != null) {
                mProgressDialog.cancel();
            }
            mHandler = null;
            super.onPostExecute(result);
        }
    }

    private void showProgressDialog(int maxValue, int currentValue) {
        if (mProgressDialog == null) {
            mProgressDialog = new LEOProgressDialog(this);
        }
        String title = getResources().getString(R.string.privacy_message_progress_dialog_title);
        String content = getResources().getString(R.string.privacy_message_progress_dialog_content);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(content);
        mProgressDialog.setMax(maxValue);
        mProgressDialog.setProgress(currentValue);
        mProgressDialog.setButtonVisiable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    private class AddMessageAsyncTask extends AsyncTask<Boolean, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Boolean... arg0) {
            boolean flag = arg0[0];
            if (flag) {
//                List<MessageBean> messageList;
                if (BuildProperties.isMIUI()) {
                    mMessageList = PrivacyContactUtils
                            .getSysMessage(AddFromMessageListActivity.this,
                                    AddFromMessageListActivity.this.getContentResolver(), null,
                                    null, false);
                } else {
                    mMessageList = PrivacyContactUtils
                            .queryMessageList(AddFromMessageListActivity.this);
                }
                // mMessageList =
                // PrivacyContactManager.getInstance(AddFromMessageListActivity.this)
                // .getSysMessage();
                // mMessageList =
                // PrivacyContactUtils.getSysMessage(AddFromMessageListActivity.this,
                // AddFromMessageListActivity.this.getContentResolver(), null,
                // null,
                // false);
                if (mMessageList != null && mMessageList.size() > 0) {
                    Collections.sort(mMessageList,
                            PrivacyContactUtils.mMessageCamparator);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (mMessageList != null && mMessageList.size() > 0) {
                mDefaultText.setVisibility(View.GONE);
            } else {
                mDefaultText.setVisibility(View.VISIBLE);
            }
            mProgressBar.setVisibility(View.GONE);
            mAdapter = new MyMessageAdapter(mMessageList);
            mListMessage.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
        }

    }
}
