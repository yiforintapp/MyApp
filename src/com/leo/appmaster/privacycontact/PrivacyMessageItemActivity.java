
package com.leo.appmaster.privacycontact;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

public class PrivacyMessageItemActivity extends BaseActivity implements OnClickListener {
    private ListView mContactCallLog;
    private CallLogAdapter mAdapter;
    private CommonToolbar mTtileBar;
    private List<MessageBean> mMessages;
    private List<String> mShowDates;
    private LinearLayout mRelativeLayout;
    private EditText mEditText;
    private Button mButton;
    private String mPhoneNumber;
    private String mThreadMySelf, mName;
    private SimpleDateFormat mDateFormatSecond = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private SimpleDateFormat mDateFormatDay = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat mTimeFormatDay = new SimpleDateFormat("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_message_item_list);
        mTtileBar = (CommonToolbar) findViewById(R.id.message_item_layout_title_bar);
        mTtileBar.setToolbarColorResource(R.color.cb);
        mMessages = new ArrayList<MessageBean>();
        mShowDates = new ArrayList<String>();
        mContactCallLog = (ListView) findViewById(R.id.contactLV);
        mRelativeLayout = (LinearLayout) findViewById(R.id.message_send_edit_parent);
        mEditText = (EditText) findViewById(R.id.message_send_edit_text);
        mButton = (Button) findViewById(R.id.message_send_button);
        mRelativeLayout.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String[] messageData = bundle.getStringArray(Constants.LOCK_MESSAGE_THREAD_ID);
            mPhoneNumber = messageData[1];
            mName = messageData[0];
            if (mPhoneNumber != null) {
                if (mName != null && !"".equals(mName)) {
                    mTtileBar.setToolbarTitle(mName);
                } else {
                    mTtileBar.setToolbarTitle(mPhoneNumber);
                }
            }
        }
        mTtileBar.setNavigationClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LeoEventBus.getDefaultBus().post(
                        new PrivacyEditFloatEvent(
                                PrivacyContactUtils.UPDATE_MESSAGE_FRAGMENT));
                PrivacyMessageItemActivity.this.finish();
            }
        });
        // 打电话
        mTtileBar.setOptionMenuVisible(true);
        mTtileBar.setOptionImageResource(R.drawable.mesage_call_icon);
        mTtileBar.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 查询该号码是否为隐私联系人
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(mPhoneNumber);
                ContactBean privacyConatact = MessagePrivacyReceiver.getPrivateMessage(
                        formateNumber, PrivacyMessageItemActivity.this);
                PrivacyContactManager.getInstance(PrivacyMessageItemActivity.this).setLastCall(
                        privacyConatact);
                Uri uri = Uri.parse("tel:" + mPhoneNumber);
                // Intent intent = new Intent(Intent.ACTION_CALL, uri);
                Intent intent = new Intent(Intent.ACTION_DIAL,
                        uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                }
                /* sdk */
                SDKWrapper.addEvent(PrivacyMessageItemActivity.this, SDKWrapper.P1, "call", "mesg");
            }
        });
        String number = PrivacyContactUtils.formatePhoneNumber(mPhoneNumber);
        getMessages(number);
        if (mMessages == null || mMessages.size() == 0) {
            mThreadMySelf = String.valueOf(System.currentTimeMillis());
        }
        mAdapter = new CallLogAdapter(mMessages);
        mContactCallLog.setAdapter(mAdapter);
        mContactCallLog.setSelection(mMessages.size() - 1);
        if (Utilities.isEmpty(mEditText.getText().toString())) {
            mButton.setEnabled(false);
            mButton.setBackgroundResource(R.drawable.unsend_icon);
        }
        TextWatcher watcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                if (mEditText.getText().toString() == null
                        || "".equals(mEditText.getText().toString())) {
                    mButton.setEnabled(false);
                    mButton.setBackgroundResource(R.drawable.unsend_icon);
                } else {
                    mButton.setEnabled(true);
                    mButton.setBackgroundResource(R.drawable.privacy_contact_send_message_bt_selecter);
                    mButton.setOnClickListener(PrivacyMessageItemActivity.this);
                }
            }
        };
        mEditText.addTextChangedListener(watcher);
        mEditText.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mContactCallLog.setSelection(mMessages.size() - 1);
                    }
                }, 200);
                return false;
            }
        });
        LeoEventBus.getDefaultBus().register(this);
        /*标识Activity创建运行,不显示通知提示*/
        AppMasterPreference.getInstance(this).setMessageItemRuning(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        LeoEventBus.getDefaultBus().post(
                new PrivacyEditFloatEvent(
                        PrivacyContactUtils.UPDATE_MESSAGE_FRAGMENT));
        PrivacyMessageItemActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        LeoEventBus.getDefaultBus().unregister(this);
        /*标识Activity结束,显示通知*/
        AppMasterPreference.getInstance(this).setMessageItemRuning(true);
        super.onDestroy();
    }

    public void onEventMainThread(PrivacyEditFloatEvent event) {
        if (PrivacyContactUtils.MESSAGE_PRIVACY_INTERCEPT_NOTIFICATION.equals(event.editModel)) {
            mMessages.clear();
            String number = PrivacyContactUtils.formatePhoneNumber(mPhoneNumber);
            getMessages(number);
            mAdapter = new CallLogAdapter(mMessages);
            mContactCallLog.setAdapter(mAdapter);
            mContactCallLog.setSelection(mMessages.size() - 1);
        }
    }

    @Override
    public void onClick(View v) {
        /*
         * 发送短信
         */
        int id = v.getId();
        if (id == R.id.message_send_button) {
              /*有发送短信，恢复短信发送失败Toast标志值*/
            PrivacyContactManager.getInstance(this).mSendMsmFail = true;
            SmsManager sms = SmsManager.getDefault();
            String messageContent = mEditText.getText().toString();
            ArrayList<String> divideMessageContents = sms.divideMessage(messageContent);
            try {
                for (String text : divideMessageContents) {
                    Intent sentIntent = new Intent(PrivacyContactUtils.SENT_SMS_ACTION);
                    PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                            sentIntent, 0);
                    sms.sendTextMessage(mPhoneNumber, null, text, sentPI, null);
                }
                mEditText.getText().clear();
                if (!messageContent.equals("") && messageContent != null) {
                    MessageBean message = new MessageBean();
                    message.setMessageBody(messageContent);
                    String date = mDateFormatSecond.format(System.currentTimeMillis());
                    message.setMessageTime(date);
                    message.setPhoneNumber(mPhoneNumber);
                    message.setMessageType(PrivacyContactUtils.SEND_MESSAGE);
                    message.setMessageName(mName);
                    String showDateTemp = mDateFormatDay.format(System.currentTimeMillis());
                    if (!mShowDates.contains(showDateTemp)) {
                        message.setShowDate(showDateTemp);
                        mShowDates.add(showDateTemp);
                    }
                    ContentValues values = new ContentValues();
                    values.put(Constants.COLUMN_MESSAGE_PHONE_NUMBER, mPhoneNumber);
                    values.put(Constants.COLUMN_MESSAGE_BODY, messageContent);
                    values.put(Constants.COLUMN_MESSAGE_CONTACT_NAME, mName);
                    values.put(Constants.COLUMN_MESSAGE_DATE, date);
                    values.put(Constants.COLUMN_MESSAGE_IS_READ, 1);
                    /*使用该隐私联系人在联系人表中的ID作为ThreadId*/
                    int threadId = PrivacyContactUtils.queryContactId(
                            PrivacyMessageItemActivity.this, message.getPhoneNumber());
                    values.put(Constants.COLUMN_MESSAGE_THREAD_ID, threadId);
                    values.put(Constants.COLUMN_MESSAGE_TYPE, PrivacyContactUtils.SEND_MESSAGE);
                    mMessages.add(message);
                    mAdapter = new CallLogAdapter(mMessages);
                    mContactCallLog.setAdapter(mAdapter);
                    mContactCallLog.setSelection(mMessages.size() - 1);
                    Uri line = getContentResolver().insert(Constants.PRIVACY_MESSAGE_URI, values);
                    if (line == null) {
                        LeoLog.i("LockMessageItemActivity", "Send message insert fail!");
                    }
                }

            } catch (Exception e) {

            }
            /* sdk */
            SDKWrapper.addEvent(PrivacyMessageItemActivity.this, SDKWrapper.P1, "sendmesg", "mesg");
        }

    }

    @SuppressLint("CutPasteId")
    private class CallLogAdapter extends BaseAdapter {
        LayoutInflater relativelayout;

        public CallLogAdapter(List<MessageBean> messages) {
            relativelayout = LayoutInflater.from(PrivacyMessageItemActivity.this);
        }

        @Override
        public int getCount() {

            return (mMessages != null) ? mMessages.size() : 0;
        }

        @Override
        public Object getItem(int position) {

            return mMessages.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        class ViewHolder {
            CircleImageView contactIcon;
            TextView receiveDate, receiveContent, sendDate, sendContent, sendContentTime,
                    receiveContentTime;
            RelativeLayout receiveMessage, sendMessage;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = relativelayout.inflate(
                        R.layout.activity_privacy_message_item_content, null);
                vh.receiveDate = (TextView) convertView
                        .findViewById(R.id.message_content_receive_item_date);
                vh.receiveContent = (TextView) convertView
                        .findViewById(R.id.message_content_receive_item);
                vh.sendDate = (TextView) convertView
                        .findViewById(R.id.message_content_send_item_date);
                vh.sendContent = (TextView) convertView
                        .findViewById(R.id.message_content_send_item);
                vh.receiveMessage = (RelativeLayout) convertView
                        .findViewById(R.id.message_content_receive);
                vh.sendMessage = (RelativeLayout) convertView
                        .findViewById(R.id.message_content_send);
                vh.sendContentTime = (TextView) convertView
                        .findViewById(R.id.message_content_send_item_time);
                vh.receiveContentTime = (TextView) convertView
                        .findViewById(R.id.message_content_receive_item_time);
                vh.contactIcon = (CircleImageView) convertView.findViewById(R.id.contactIV);
                convertView.setTag(vh);

            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            MessageBean mb = mMessages.get(position);
            if (mb != null) {
                // 获取头像
                Bitmap icon = PrivacyContactUtils.getContactIcon(
                        PrivacyMessageItemActivity.this, mb.getPhoneNumber());
                if (icon != null) {
                    vh.contactIcon.setImageBitmap(icon);
                } else {
                    vh.contactIcon.setImageResource(R.drawable.default_user_avatar);
                }
                if (mb.getMessageType() == 1) {
                    vh.receiveMessage.setVisibility(View.VISIBLE);
                    vh.sendMessage.setVisibility(View.GONE);
                    // 接收
                    vh.receiveContent.setText(mb.getMessageBody());
                    String dateReceive = mb.getMessageTime();
                    // 控制一天只显示一次日期
                    if (mb.getShowDate() != null && !mb.equals("")) {
                        vh.receiveDate.setVisibility(View.VISIBLE);
                        vh.receiveDate.setText(mb.getShowDate());
                    } else {
                        vh.receiveDate.setVisibility(View.GONE);
                    }
                    try {
                        Date formateTime = mDateFormatSecond.parse(dateReceive);
                        String time = mTimeFormatDay.format(formateTime);
                        int hours = formateTime.getHours();
                        String receiveTime = null;
                        if (hours > 12) {
                            receiveTime = time + "\u00A0PM";
                        } else {
                            receiveTime = time + "\u00A0AM";
                        }
                        vh.receiveContentTime.setText(receiveTime);
                    } catch (Exception e) {
                    }
                } else if (mb.getMessageType() == 2) {
                    vh.receiveMessage.setVisibility(View.GONE);
                    vh.sendMessage.setVisibility(View.VISIBLE);
                    // 发送
                    String dateSend = mb.getMessageTime();
                    // 控制一天只显示一次
                    if (mb.getShowDate() != null && !mb.equals("")) {
                        vh.sendDate.setVisibility(View.VISIBLE);
                        String showDate = mb.getShowDate();
                        vh.sendDate.setText(showDate);
                    } else {
                        vh.sendDate.setVisibility(View.GONE);
                    }
                    vh.sendContent.setText(mb.getMessageBody());
                    try {
                        Date formateTime = mDateFormatSecond.parse(dateSend);
                        String time = mTimeFormatDay.format(formateTime);
                        int hours = formateTime.getHours();
                        String sendTime = null;
                        if (hours > 12) {
                            sendTime = time + "\u00A0PM";
                        } else {
                            sendTime = time + "\u00A0AM";
                        }
                        vh.sendContentTime.setText(sendTime);
                    } catch (Exception e) {
                    }

                }
            }
            return convertView;
        }
    }

    /**
     * getMessages
     */
    @SuppressLint("SimpleDateFormat")
    private void getMessages(String id) {
        List<String> showDate = new ArrayList<String>();
        Cursor cur = null;
        try {
            cur = getContentResolver().query(Constants.PRIVACY_MESSAGE_URI, null,
                    Constants.COLUMN_MESSAGE_PHONE_NUMBER
                            + " LIKE ?", new String[]{
                            "%" + id
                    }, Constants.COLUMN_MESSAGE_DATE);
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
                    if (isRead == 0) {
                        String fromateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                        PrivacyContactUtils.updateMessageMyselfIsRead(1,
                                "contact_phone_number LIKE ? ",
                                new String[]{
                                        "%" + fromateNumber
                                }, this);
                    }
                    String time = cur.getString(cur.getColumnIndex(Constants.COLUMN_MESSAGE_DATE));
                    try {
                        Date date = new Date(time);
                        String showDateTemp = mDateFormatDay.format(date);
                        if (showDate == null || showDate.size() == 0) {
                            showDate.add(showDateTemp);
                            mb.setShowDate(showDateTemp);
                            mShowDates.add(showDateTemp);
                        } else {
                            if (!showDate.contains(showDateTemp)) {
                                showDate.add(showDateTemp);
                                mb.setShowDate(showDateTemp);
                                mShowDates.add(showDateTemp);
                            }
                        }
                    } catch (Exception e) {

                    }
                    mb.setMessageBody(body);
                    mb.setMessageName(name);
                    mb.setPhoneNumber(number);
                    mb.setMessageType(type);
                    mb.setMessageIsRead(isRead);
                    mb.setMessageThreadId(threadId);
                    mb.setMessageTime(time);
                    mMessages.add(mb);
                }
            }
        } catch (Exception e) {
        } finally {
            if (cur != null) {
                cur.close();
            }
        }

        Collections.sort(mMessages, PrivacyContactUtils.mMessageAscCamparator);
    }
}
