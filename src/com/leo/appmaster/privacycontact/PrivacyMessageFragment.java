
package com.leo.appmaster.privacycontact;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.EventId;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.eventbus.event.PrivacyMessageEvent;
import com.leo.appmaster.fragment.BaseFragment;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEORoundProgressDialog;
import com.leo.appmaster.utils.LeoLog;

public class PrivacyMessageFragment extends BaseFragment implements OnItemClickListener,
        OnItemLongClickListener {

    private static final String TAG = "PrivacyMessageFragment";
    private static final String QUERY_SQL_TABLE_MESSAGE_LIST_MODEL = "query_list_model";
    private static final String QUERY_SQL_TABLE_MESSAGE_ITEM_MODEL = "query_item_model";
    private LinearLayout mDefaultText;
    private ListView mListMessage;
    private MyMessageAdapter mAdapter;
    private List<MessageBean> mMessageList;
    private Context mContext;
    private boolean mIsEditModel = false;
    private static final String RED_TIP = "red_tip";
    private static final String spaceString = "\u00A0";
    private List<MessageBean> mRestorMessages;
    private Handler mHandler;
    private LEORoundProgressDialog mProgressDialog;
    private LEOAlarmDialog mAddCallLogDialog;
    private int mRestoreCount;
    private String mEditModelOperaction;
    private List<MessageBean> mRestoremessgeLists;
    private SimpleDateFormat mSimpleDateFormate;
    private RelativeLayout mButtomTip;
    private boolean mButtomTipIsShow = false;
    private PrivacyMsmHandler mMsmHandler = new PrivacyMsmHandler();

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_privacy_message;
    }

    @Override
    protected void onInitUI() {
        mContext = getActivity();
        mSimpleDateFormate = new SimpleDateFormat("yy/MM/dd");
        mDefaultText = (LinearLayout) findViewById(R.id.message_default_tv);
        mButtomTip = (RelativeLayout) findViewById(R.id.buttom_tip);
        mMessageList = new ArrayList<MessageBean>();
        mRestorMessages = new ArrayList<MessageBean>();
        mRestoremessgeLists = new ArrayList<MessageBean>();
        mListMessage = (ListView) findViewById(R.id.privacy_messageLV);
        mListMessage.setOnItemClickListener(this);
        mListMessage.setOnItemLongClickListener(this);
        mAdapter = new MyMessageAdapter(mMessageList);
        mListMessage.setAdapter(mAdapter);
        LeoEventBus.getDefaultBus().register(this);
//        PrivacyContactDateTask task = new PrivacyContactDateTask();
//        task.execute("");
        sendMsgHandler();
    }

    public void onEventMainThread(PrivacyEditFloatEvent event) {
        mEditModelOperaction = event.editModel;
        if (PrivacyContactUtils.CANCEL_EDIT_MODEL.equals(event.editModel)) {
            restoreParameter();
            isShowDefaultImage();
            mAdapter.notifyDataSetChanged();
        } else if (PrivacyContactUtils.EDIT_MODEL_OPERATION_RESTORE.equals(mEditModelOperaction)) {
            if (mRestorMessages != null && mRestorMessages.size() != 0) {
                showRestoreMessageDialog(
                        getResources().getString(R.string.privacy_message_resotre_message),
                        PrivacyContactUtils.EDIT_MODEL_OPERATION_RESTORE);
            }
        } else if (PrivacyContactUtils.MESSAGE_EDIT_MODEL_OPERATION_DELETE
                .equals(mEditModelOperaction)) {
            if (mRestorMessages != null && mRestorMessages.size() != 0) {
                showRestoreMessageDialog(
                        getResources().getString(R.string.privacy_message_delete_message),
                        PrivacyContactUtils.MESSAGE_EDIT_MODEL_OPERATION_DELETE);
            }
        } else if (PrivacyContactUtils.UPDATE_MESSAGE_FRAGMENT.equals(mEditModelOperaction)
                || PrivacyContactUtils.CONTACT_DETAIL_DELETE_LOG_UPDATE_MESSAGE_LIST
                .equals(mEditModelOperaction)) {
//            PrivacyContactDateTask task = new PrivacyContactDateTask();
//            task.execute("");
            sendMsgHandler();
        } else if (PrivacyContactUtils.MESSAGE_PRIVACY_INTERCEPT_NOTIFICATION
                .equals(mEditModelOperaction)) {
            mButtomTipIsShow = true;
//            PrivacyContactDateTask task = new PrivacyContactDateTask();
//            task.execute("");
            sendMsgHandler();
        } else if (PrivacyContactUtils.PRIVACY_EDIT_NAME_UPDATE_MESSAGE_EVENT
                .equals(mEditModelOperaction)) {
//            PrivacyContactDateTask task = new PrivacyContactDateTask();
//            task.execute("");
            sendMsgHandler();
        }
    }

    // 恢复编辑状态之前的参数状态
    public void restoreParameter() {
        mIsEditModel = false;
        mRestorMessages.clear();
        setMessageCheck(false);
        mRestoreCount = 0;
    }

    // 设置选中状态
    public void setMessageCheck(boolean flag) {
        for (MessageBean messageBean : mMessageList) {
            messageBean.setCheck(flag);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDestroyView();
    }

//    public void setContent(String content) {
//        if (mTextView != null) {
//            mTextView.setText(content);
//        }
//    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        LeoEventBus.getDefaultBus().post(
                new PrivacyMessageEvent(EventId.EVENT_PRIVACY_EDIT_MODEL,
                        PrivacyContactUtils.FROM_MESSAGE_NO_SELECT_EVENT));
        mIsEditModel = true;
        mAdapter.notifyDataSetChanged();
        return true;
    }

    // 更新TitleBar
    private void updateTitleBarSelectStatus() {
        if (mRestorMessages != null && mRestorMessages.size() > 0) {
            LeoEventBus.getDefaultBus().post(
                    new PrivacyMessageEvent(EventId.EVENT_PRIVACY_EDIT_MODEL,
                            PrivacyContactUtils.FROM_MESSAGE_EVENT));
        } else {
            LeoEventBus.getDefaultBus().post(
                    new PrivacyMessageEvent(EventId.EVENT_PRIVACY_EDIT_MODEL,
                            PrivacyContactUtils.FROM_MESSAGE_NO_SELECT_EVENT));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
        MessageBean mb = mMessageList.get(position);
        if (!mIsEditModel) {
            Intent intent = new Intent();
            String[] threadId = new String[2];
            threadId[0] = mb.getMessageName();
            // threadId[1] = mb.getMessageThreadId();
            threadId[1] = mb.getPhoneNumber();
            intent.setClass(mContext, PrivacyMessageItemActivity.class);
            Bundle bundle = new Bundle();
            bundle.putStringArray(Constants.LOCK_MESSAGE_THREAD_ID, threadId);
            intent.putExtras(bundle);
            try {
                startActivity(intent);
                // 标记为已读
                String number = PrivacyContactUtils.formatePhoneNumber(mb.getPhoneNumber());
                PrivacyContactUtils.updateMessageMyselfIsRead(1,
                        "contact_phone_number LIKE ? and message_is_read = 0",
                        new String[]{
                                "%" + number
                        }, mContext);
            } catch (Exception e) {
            }
        } else {
            ImageView image = (ImageView) view.findViewById(R.id.message_listCB);
            if (!mb.isCheck()) {
                image.setImageDrawable(getResources().getDrawable(R.drawable.select));
                mb.setCheck(true);
                mRestorMessages.add(mb);
                mRestoreCount = mRestoreCount + 1;
            } else {
                image.setImageDrawable(getResources().getDrawable(R.drawable.unselect));
                mb.setCheck(false);
                mRestorMessages.remove(mb);
                mRestoreCount = mRestoreCount - 1;
            }
            updateTitleBarSelectStatus();
        }
    }

    /**
     * getMessages
     */
    @SuppressLint("SimpleDateFormat")
    private List<MessageBean> getMessages(String model, String selection, String[]
            selectionArgs) {
        List<MessageBean> messages = new ArrayList<MessageBean>();
        AppMasterPreference preference = AppMasterPreference.getInstance(mContext);
        List<MessageBean> mMessages = new ArrayList<MessageBean>();
        Map<String, MessageBean> messageList = new HashMap<String, MessageBean>();
        int noReadCount = 0;
        Cursor cur = null;
        try {
            cur = mContext.getContentResolver().query(Constants.PRIVACY_MESSAGE_URI, null,
                    selection, selectionArgs, Constants.COLUMN_MESSAGE_DATE + " desc");
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
                    Bitmap icon = PrivacyContactUtils.getContactIcon(mContext, number);
                    if (icon != null) {
                        int size = (int) getActivity().getResources().getDimension(R.dimen.privacy_contact_icon_size);
                        icon = PrivacyContactUtils.getScaledContactIcon(icon, size);
                        mb.setContactIcon(icon);
                    } else {
                        mb.setContactIcon(((BitmapDrawable) mContext.getResources().getDrawable(
                                R.drawable.default_user_avatar)).getBitmap());
                    }
                    mb.setMessageBody(body);
                    if (number != null) {
                        if (name != null && !"".equals(name)) {
                            mb.setMessageName(name);
                        } else {
                            mb.setMessageName(number);
                        }
                    }
                    mb.setPhoneNumber(number);
                    mb.setMessageType(type);
                    mb.setMessageIsRead(isRead);
                    mb.setMessageThreadId(threadId);
                    mb.setMessageTime(time);
                    if (threadId != null) {
                        if (!messageList.containsKey(threadId)) {
                            int temp = noReadMessage(number);
                            mb.setCount(temp);
                            messageList.put(threadId, mb);
                        }
                    }
                    mMessages.add(mb);
                }
            }
        } catch (Exception e) {
        } finally {
            if (cur != null) {
                cur.close();
            }
        }

        if (QUERY_SQL_TABLE_MESSAGE_LIST_MODEL.equals(model)) {
            Iterable<MessageBean> it = messageList.values();
            for (MessageBean mb : it) {
                messages.add(mb);
            }
        } else if (QUERY_SQL_TABLE_MESSAGE_ITEM_MODEL.equals(model)) {
            messages = mMessages;
        }
        Collections.sort(messages, PrivacyContactUtils.mMessageCamparator);
        return messages;
    }

    // get no read message count
    private int noReadMessage(String number) {
        return PrivacyContactUtils.getNoReadMessage(mContext, number);
    }

    private class MyMessageAdapter extends BaseAdapter {
        LayoutInflater relativelayout;

        public MyMessageAdapter(List<MessageBean> messages) {
            relativelayout = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {

            return (mMessageList != null) ? mMessageList.size() : 0;
        }

        @Override
        public Object getItem(int position) {

            return mMessageList.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        class ViewHolder {
            CircleImageView contactIcon;
            ImageView checkImage, bottomLine;
            TextView name, content, date, count;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = relativelayout.inflate(R.layout.fragment_private_message_list, null);
                vh.name = (TextView) convertView.findViewById(R.id.message_list_nameTV);
                vh.content = (TextView) convertView.findViewById(R.id.message_list_contentTV);
                vh.date = (TextView) convertView.findViewById(R.id.message_list_dateTV);
                vh.checkImage = (ImageView) convertView.findViewById(R.id.message_listCB);
                vh.count = (TextView) convertView.findViewById(R.id.message_list_countTV);
                vh.contactIcon = (CircleImageView) convertView.findViewById(R.id.contactIV);
                vh.bottomLine = (ImageView) convertView.findViewById(R.id.bottom_line);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            MessageBean mb = mMessageList.get(position);
            if (mb.getPhoneNumber() != null) {
                if (mb.getMessageName() != null && !"".equals(mb.getMessageName())) {
                    vh.name.setText(mb.getMessageName());
                } else {
                    vh.name.setText(mb.getPhoneNumber());
                }
            }

            vh.content.setText(mb.getMessageBody());
            Date tempDate = null;
            try {
                tempDate = mSimpleDateFormate.parse(mb.getMessageTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String date = mSimpleDateFormate.format(tempDate);
            vh.date.setText(date);
            int messageCount = mb.getCount();
            if (messageCount > 0) {
                vh.contactIcon.setAnswerStatus(RED_TIP);
            } else {
                vh.contactIcon.setAnswerStatus("");
            }
            if (mIsEditModel) {
                vh.checkImage.setVisibility(View.VISIBLE);
                if (mb.isCheck()) {
                    vh.checkImage.setImageResource(R.drawable.select);
                } else {
                    vh.checkImage.setImageResource(R.drawable.unselect);
                }
            } else {
                vh.checkImage.setVisibility(View.INVISIBLE);
            }
            Bitmap icon = mb.getContactIcon();
            vh.contactIcon.setImageBitmap(icon);
//            if (mMessageList != null && mMessageList.size() > 0) {
//                if (position == mMessageList.size() - 1) {
//                    vh.bottomLine.setVisibility(View.GONE);
//                } else {
//                    vh.bottomLine.setVisibility(View.VISIBLE);
//                }
//            }
            return convertView;
        }
    }

    private void showProgressDialog(int maxValue, int currentValue) {
        if (mProgressDialog == null) {
            mProgressDialog = new LEORoundProgressDialog(mContext);
        }
        String title = getResources().getString(R.string.privacy_contact_progress_dialog_title);
        String content = getResources().getString(R.string.privacy_contact_progress_dialog_content);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(content);
        mProgressDialog.setMax(maxValue);
        mProgressDialog.setProgress(currentValue);
        mProgressDialog.setCustomProgressTextVisiable(true);
        mProgressDialog.setButtonVisiable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    private void showRestoreMessageDialog(String content, final String flag) {
        if (mAddCallLogDialog == null) {
            mAddCallLogDialog = new LEOAlarmDialog(mContext);
        }
        mAddCallLogDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    if (PrivacyContactUtils.EDIT_MODEL_OPERATION_RESTORE.equals(flag)) {
                        /* sdk */
                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "privacyedit", "restore_mesg");
                        mHandler = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                int currentValue = msg.what;
                                if (currentValue >= mRestoreCount) {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.cancel();
                                    }
                                } else {
                                    mProgressDialog.setProgress(currentValue);
                                }
                                super.handleMessage(msg);
                            }
                        };
                        showProgressDialog(mRestoreCount, 0);
                        PrivacyMessageTask task = new PrivacyMessageTask();
                        task.execute(PrivacyContactUtils.EDIT_MODEL_OPERATION_RESTORE);
                    } else if (PrivacyContactUtils.MESSAGE_EDIT_MODEL_OPERATION_DELETE
                            .equals(flag)) {
                        /* sdk */
                        SDKWrapper.addEvent(mContext, SDKWrapper.P1, "privacyedit", "deletemesg");
                        mHandler = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                int currentValue = msg.what;
                                if (currentValue >= mRestoreCount) {
                                    if (mProgressDialog != null) {
                                        mProgressDialog.cancel();
                                    }
                                } else {
                                    mProgressDialog.setProgress(currentValue);
                                }
                                super.handleMessage(msg);
                            }
                        };
                        showProgressDialog(mRestoreCount, 0);
                        PrivacyMessageTask task = new PrivacyMessageTask();
                        task.execute(PrivacyContactUtils.MESSAGE_EDIT_MODEL_OPERATION_DELETE);

                    }
                } else if (which == 0) {
                    if (mAddCallLogDialog != null) {
                        mAddCallLogDialog.cancel();
                    }
                    restoreParameter();
                    mAdapter.notifyDataSetChanged();
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyMessageEvent(EventId.EVENT_PRIVACY_EDIT_MODEL,
                                    PrivacyContactUtils.EDIT_MODEL_RESTOR_TO_SMS_CANCEL));
                }

            }
        });
        mAddCallLogDialog.setCanceledOnTouchOutside(false);
        mAddCallLogDialog.setContent(content);
        mAddCallLogDialog.show();
    }

    private class PrivacyMessageTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {
            int count = 0;
            String isOtherLogs = null;
            String operationModel = arg0[0];
            ContentResolver cr = mContext.getContentResolver();
            AppMasterPreference pre = AppMasterPreference.getInstance(mContext);
            if (PrivacyContactUtils.EDIT_MODEL_OPERATION_RESTORE.equals(operationModel)) {
                int temp = pre.getMessageNoReadCount();
                for (MessageBean messageBean : mRestorMessages) {
                    isOtherLogs = PrivacyContactUtils.EDIT_MODEL_OPERATION_RESTORE;
                    String fromateNumber = PrivacyContactUtils.formatePhoneNumber(messageBean
                            .getPhoneNumber());
                    mRestoremessgeLists = getMessages(
                            QUERY_SQL_TABLE_MESSAGE_ITEM_MODEL,
                            Constants.COLUMN_MESSAGE_PHONE_NUMBER + " LIKE ? ",
                            new String[]{
                                    "%" + fromateNumber
                            });
                    // delete message cancel red tip
                    if (temp > 0) {
                        int noReadMessageCount = noReadMessage(messageBean.getPhoneNumber());
                        if (noReadMessageCount > 0) {
                            for (int i = 0; i < noReadMessageCount; i++) {
                                if (temp > 0) {
                                    temp = temp - 1;
                                    pre.setMessageNoReadCount(temp);
                                }
                                if (temp <= 0) {
                                    /* ISwipe处理：通知没有未读 */
                                    PrivacyContactManager.getInstance(mContext)
                                            .cancelPrivacyTipFromPrivacyMsm();
                                    // 没有未读去除隐私通知
                                    if (pre.getCallLogNoReadCount() <= 0) {
                                        NotificationManager notificationManager = (NotificationManager) getActivity()
                                                .getSystemService(
                                                        Context.NOTIFICATION_SERVICE);
                                        notificationManager.cancel(20140901);
                                    }

                                    LeoEventBus
                                            .getDefaultBus()
                                            .post(
                                                    new PrivacyEditFloatEvent(
                                                            PrivacyContactUtils.PRIVACY_CONTACT_ACTIVITY_CANCEL_RED_TIP_EVENT));
                                }
                            }
                        }
                    }
                    for (MessageBean messages : mRestoremessgeLists) {
                        ContentValues values = new ContentValues();
                        values.put("address", messages.getPhoneNumber());
                        // 重构短信内容数据
                        String messageContent = spaceString + messages.getMessageBody()
                                + spaceString;
                        values.put("body", messageContent);
                        Long date = Date.parse(messages.getMessageTime());
                        values.put("date", date);
                        values.put("read", 1);
                        values.put("type", messages.getMessageType());
                        try {
                            PrivacyContactUtils.insertMessageToSystemSMS(values, mContext);
                        } catch (Exception e) {
                            LeoLog.i("PrivacyMessageFragment Operation",
                                    "PrivacyContactFragment restore message fail!");
                        }
                        try {
                            String formateNumber = PrivacyContactUtils.formatePhoneNumber(messages
                                    .getPhoneNumber());
                            int flag = PrivacyContactUtils.deleteMessageFromMySelf(cr,
                                    Constants.PRIVACY_MESSAGE_URI,
                                    Constants.COLUMN_MESSAGE_PHONE_NUMBER + " LIKE ? ",
                                    new String[]{
                                            "%" + formateNumber
                                    });
                        } catch (Exception e) {
                        }
                    }
                    mMessageList.remove(messageBean);
                    if (mHandler != null) {
                        Message messge = new Message();
                        count = count + 1;
                        messge.what = count;
                        mHandler.sendMessage(messge);
                    }
                }
            } else if (PrivacyContactUtils.MESSAGE_EDIT_MODEL_OPERATION_DELETE
                    .equals(operationModel)) {
                isOtherLogs = PrivacyContactUtils.MESSAGE_EDIT_MODEL_OPERATION_DELETE;
                int temp = pre.getMessageNoReadCount();
                for (MessageBean messageBean : mRestorMessages) {
                    String fromateNumber = PrivacyContactUtils.formatePhoneNumber(messageBean
                            .getPhoneNumber());
                    mRestoremessgeLists = getMessages(
                            QUERY_SQL_TABLE_MESSAGE_ITEM_MODEL,
                            Constants.COLUMN_MESSAGE_PHONE_NUMBER + " LIKE ? ",
                            new String[]{
                                    "%" + fromateNumber
                            });
                    // delete message cancel red tip
                    if (temp > 0) {
                        int noReadMessageCount = noReadMessage(messageBean.getPhoneNumber());
                        if (noReadMessageCount > 0) {
                            for (int i = 0; i < noReadMessageCount; i++) {
                                if (temp > 0) {
                                    temp = temp - 1;
                                    pre.setMessageNoReadCount(temp);
                                }
                                if (temp <= 0) {
                                    // 没有未读去除隐私通知
                                    if (pre.getCallLogNoReadCount() <= 0) {
                                        NotificationManager notificationManager = (NotificationManager) getActivity()
                                                .getSystemService(
                                                        Context.NOTIFICATION_SERVICE);
                                        notificationManager.cancel(20140901);
                                    }

                                    LeoEventBus
                                            .getDefaultBus()
                                            .post(
                                                    new PrivacyEditFloatEvent(
                                                            PrivacyContactUtils.PRIVACY_CONTACT_ACTIVITY_CANCEL_RED_TIP_EVENT));
                                }
                            }
                        }
                    }
                    for (MessageBean messages : mRestoremessgeLists) {
                        int flagNumber = PrivacyContactUtils.deleteMessageFromMySelf(cr,
                                Constants.PRIVACY_MESSAGE_URI,
                                Constants.COLUMN_MESSAGE_PHONE_NUMBER + " = ? ", new String[]{
                                        messages.getPhoneNumber()
                                });
                        if (flagNumber != -1 && mHandler != null) {
                            Message messge = new Message();
                            count = count + 1;
                            messge.what = count;
                            mHandler.sendMessage(messge);
                        }
                    }
                    // mMessageList.remove(messageBean);
                }
            }
            return isOtherLogs;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (PrivacyContactUtils.MESSAGE_EDIT_MODEL_OPERATION_DELETE.equals(result)) {
                for (MessageBean messageBean : mRestorMessages) {
                    mMessageList.remove(messageBean);
                }
            } else if (PrivacyContactUtils.EDIT_MODEL_OPERATION_RESTORE.equals(result)) {
                for (MessageBean messageBean : mRestorMessages) {
                    mMessageList.remove(messageBean);
                }
            }
            restoreParameter();
            mRestoreCount = 0;
            LeoEventBus.getDefaultBus().post(
                    new PrivacyMessageEvent(EventId.EVENT_PRIVACY_EDIT_MODEL,
                            PrivacyContactUtils.EDIT_MODEL_RESTOR_TO_SMS_CANCEL));
            isShowDefaultImage();
            mAdapter.notifyDataSetChanged();
        }
    }

    private class PrivacyMsmHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case PrivacyContactUtils.MSG_MESSAGE:
                    if (msg.obj != null) {
                        LeoLog.i(TAG, "load privacy messages ");
                        List<MessageBean> msms = (List<MessageBean>) msg.obj;
                        if (mMessageList != null) {
                            mMessageList.clear();
                        }
                        mMessageList = msms;
                        if (mMessageList == null || mMessageList.size() == 0) {
                            mDefaultText.setVisibility(View.VISIBLE);
                        } else {
                            mDefaultText.setVisibility(View.GONE);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void sendMsgHandler() {
        if (mMsmHandler != null) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    List<MessageBean> messageList = getMessages(QUERY_SQL_TABLE_MESSAGE_LIST_MODEL, null, null);
                    Message msg = new Message();
                    msg.what = PrivacyContactUtils.MSG_MESSAGE;
                    msg.obj = messageList;
                    mMsmHandler.sendMessage(msg);
                }
            });
        }
    }

    // 加载数据
    private class PrivacyContactDateTask extends AsyncTask<String, Boolean, List<MessageBean>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<MessageBean> doInBackground(String... arg0) {
            List<MessageBean> messageList = getMessages(QUERY_SQL_TABLE_MESSAGE_LIST_MODEL, null,
                    null);
            return messageList;
        }

        @Override
        protected void onPostExecute(List<MessageBean> result) {
            super.onPostExecute(result);
            if (mMessageList != null && mMessageList.size() > 0) {
                mMessageList.clear();
            }
            mMessageList = result;
            isShowDefaultImage();
            mAdapter.notifyDataSetChanged();
        }
    }

    // 判断是否显示默认图
    private void isShowDefaultImage() {
        if (mMessageList == null || mMessageList.size() == 0) {
            mDefaultText.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT > 18) {
                mButtomTip.setVisibility(View.VISIBLE);
                mButtomTipIsShow = false;
            }
        } else {
            mDefaultText.setVisibility(View.GONE);
            if (mButtomTipIsShow) {
                mButtomTip.setVisibility(View.GONE);
            }
        }
    }
}
