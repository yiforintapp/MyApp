
package com.leo.appmaster.privacycontact;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.leo.appmaster.ui.dialog.LEORoundProgressDialog;
import com.leo.appmaster.utils.LeoLog;

public class PrivacyContactFragment extends BaseFragment {

    private static final String TAG = "PrivacyContactFragment";
    private TextView mTextView;
    private LinearLayout mDefaultText;
    private ListView mContactCallLog;
    private ContactAdapter mAdapter;
    private List<ContactBean> mContacts;
    private Context mContext;
    private PrivacyContactAlarmDialog mPCDialog;
    private PrivacyContactDeleteAlarmDialog mAddCallLogDialog;
    private boolean mIsEditModel = false;
    private List<ContactBean> mDeleteContact;
    private int mDeleteCount = 0;
    private Handler mHandler;
    private LEORoundProgressDialog mProgressDialog;
    private boolean mIsChecked = true;
    private boolean mRestorMessagesFlag = false;
    private boolean mRestorCallLogsFlag = false;
    private PrivacyContactHandler mContactHandler = new PrivacyContactHandler();

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_privacy_contact;
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onInitUI() {
        mContext = getActivity();
        mTextView = (TextView) findViewById(R.id.content);
        mDefaultText = (LinearLayout) findViewById(R.id.contacat_default_tv);
        mContactCallLog = (ListView) findViewById(R.id.contactLV);
        mDeleteContact = new ArrayList<ContactBean>();
        mContacts = new ArrayList<ContactBean>();
        LeoEventBus.getDefaultBus().register(this);
        mAdapter = new ContactAdapter(mContacts);
        mContactCallLog.setAdapter(mAdapter);
        mContactCallLog.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                ContactBean contact = mContacts.get(arg2);
                if (!mIsEditModel) {
                    String number = contact.getContactNumber();
                    String name = contact.getContactName();
                    if (name != null) {
                        if (name.isEmpty()) {
                            name = number;
                        }
                    }
                    showContactEditDialog(arg2, contact);
                } else {
                    ImageView image = (ImageView) view.findViewById(R.id.message_item_check);
                    if (!contact.isCheck()) {
                        image.setImageDrawable(getResources().getDrawable(
                                R.drawable.select));
                        contact.setCheck(true);
                        mDeleteContact.add(contact);
                        mDeleteCount = mDeleteCount + 1;
                    } else {
                        image.setImageDrawable(getResources().getDrawable(
                                R.drawable.unselect));
                        contact.setCheck(false);
                        mDeleteContact.remove(contact);
                        mDeleteCount = mDeleteCount - 1;
                    }
                    updateTitleBarSelectStatus();
                }
            }
        });
        mContactCallLog.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                LeoEventBus.getDefaultBus().post(
                        new PrivacyMessageEvent(EventId.EVENT_PRIVACY_EDIT_MODEL,
                                PrivacyContactUtils.FROM_CONTACT_NO_SELECT_EVENT));
                mIsEditModel = true;
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });
        sendMsgHandler();
    }

    // 更新TitleBar
    private void updateTitleBarSelectStatus() {
        if (mDeleteContact != null && mDeleteContact.size() > 0) {
            LeoEventBus.getDefaultBus().post(
                    new PrivacyMessageEvent(EventId.EVENT_PRIVACY_EDIT_MODEL,
                            PrivacyContactUtils.FROM_CONTACT_EVENT));
        } else {
            LeoEventBus.getDefaultBus().post(
                    new PrivacyMessageEvent(EventId.EVENT_PRIVACY_EDIT_MODEL,
                            PrivacyContactUtils.FROM_CONTACT_NO_SELECT_EVENT));
        }
    }

    public void onEventMainThread(PrivacyEditFloatEvent event) {
        if (PrivacyContactUtils.CANCEL_EDIT_MODEL.equals(event.editModel)) {
            restoreParameter();
            if (mContacts == null || mContacts.size() == 0) {
                mDefaultText.setVisibility(View.VISIBLE);
            } else {
                mDefaultText.setVisibility(View.GONE);
            }
            mAdapter.notifyDataSetChanged();
        } else if (PrivacyContactUtils.CONTACT_EDIT_MODEL_OPERATION_DELETE
                .equals(event.editModel)) {
            if (!mDeleteContact.isEmpty()) {
                showContactDialog(null,
                        getResources().getString(R.string.privacy_contact_checkbox_dlg_title),
                        getResources().getString(R.string.privacy_contact_checkbox_dlg_content),
                        R.string.privacy_contact_checkbox_dlg_checktext,
                        PrivacyContactUtils.CONTACT_EDIT_MODEL_OPERATION_DELETE, 0);
            }
        } else if (PrivacyContactUtils.PRIVACY_ADD_CONTACT_UPDATE
                .equals(event.editModel)) {
//            PrivacyContactMyDateTask task = new PrivacyContactMyDateTask();
//            task.execute("");
            sendMsgHandler();
        }
    }

    // 恢复编辑之前的参数
    public void restoreParameter() {
        mIsEditModel = false;
        setMessageCheck(false);
        mDeleteContact.clear();
        mDeleteCount = 0;
    }

    // 设置选中状态
    public void setMessageCheck(boolean flag) {
        for (ContactBean contact : mContacts) {
            contact.setCheck(flag);
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

    public void setContent(String content) {
        if (mTextView != null) {
            mTextView.setText(content);
        }
    }

    @SuppressLint("CutPasteId")
    private class ContactAdapter extends BaseAdapter {
        LayoutInflater relativelayout;

        public ContactAdapter(List<ContactBean> contacts) {
            relativelayout = LayoutInflater.from(mContext);
            // this.contacts = contacts;
        }

        @Override
        public int getCount() {

            return (mContacts != null) ? mContacts.size() : 0;
        }

        @Override
        public Object getItem(int position) {

            return mContacts.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        class ViewHolder {
            CircleImageView contactIcon;
            ImageView editImage, bottomLine;
            TextView name, number, content, type;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = relativelayout.inflate(R.layout.activity_privacy_contact_item, null);
                vh.contactIcon = (CircleImageView) convertView.findViewById(R.id.contactIV);
                vh.name = (TextView) convertView.findViewById(R.id.message_item_nameTV);
                vh.number = (TextView) convertView.findViewById(R.id.message_item_numberTV);
                vh.content = (TextView) convertView.findViewById(R.id.message_item_contentTV);
                vh.type = (TextView) convertView.findViewById(R.id.message_item_typeTV);
                vh.editImage = (ImageView) convertView.findViewById(R.id.message_item_check);
                vh.bottomLine = (ImageView) convertView.findViewById(R.id.bottom_line);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            if (mContacts != null && mContacts.size() > position) {
                ContactBean mb = mContacts.get(position);
                if (mb.getContactName() != null && !"".equals(mb.getContactName())) {
                    vh.name.setText(mb.getContactName());
                } else {
                    vh.name.setText(mb.getContactNumber());
                }
                vh.number.setText(mb.getContactNumber());
                if (mb.getContactIcon() != null) {
                    vh.contactIcon.setImageBitmap(mb.getContactIcon());
                } else {
                    vh.contactIcon.setImageResource(R.drawable.default_user_avatar);
                }
                if (mIsEditModel) {
                    vh.editImage.setImageResource(R.drawable.unselect);
                    if (mb.isCheck()) {
                        vh.editImage.setImageResource(R.drawable.select);
                    } else {
                        vh.editImage.setImageResource(R.drawable.unselect);
                    }
                } else {
                    if (mb.getAnswerType() == 1) {
                        vh.editImage.setImageResource(R.drawable.answer_icon);
                    } else if (mb.getAnswerType() == 0) {
                        vh.editImage.setImageResource(R.drawable.decline_icon);
                    }

                }
            }

            return convertView;
        }
    }

    public void showContactEditDialog(final int position, final ContactBean contact) {
        if (mPCDialog == null) {
            mPCDialog = new PrivacyContactAlarmDialog(mContext);
        }
        if (contact.getContactIcon() != null) {
            mPCDialog.setContactIcon(contact.getContactIcon());
        } else {
            BitmapDrawable drawable = (BitmapDrawable) this.getResources().getDrawable(
                    R.drawable.default_user_avatar);
            mPCDialog.setContactIcon(drawable.getBitmap());
        }
        if (contact.getContactName() != null && !"".equals(contact.getContactName())) {
            mPCDialog.setContentOne(contact.getContactName());
        } else {
            mPCDialog.setContentOne(contact.getContactNumber());
        }
        mPCDialog.setContentTwo(contact.getContactNumber());
        mPCDialog.setTitleIcon(
                getResources().getDrawable(R.drawable.privacy_contact_edit_bt_selecter),
                getResources().getDrawable(R.drawable.privacy_contact_delete_bt_selecter));
        // 编辑按钮
        mPCDialog.setTitleEditIconListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mPCDialog.cancel();
                String[] bundleData = new String[]{
                        contact.getContactName(), contact.getContactNumber(),
                        String.valueOf(contact.getAnswerType())
                };
                Bundle bundle = new Bundle();
                bundle.putStringArray(PrivacyContactUtils.CONTACT_CALL_LOG,
                        bundleData);
                Intent intentEditPrivacyContact = new Intent(mContext,
                        EditPrivacyContactActivity.class);
                intentEditPrivacyContact.putExtras(bundle);
                try {
                    startActivity(intentEditPrivacyContact);
                } catch (Exception e) {
                }

            }
        });
        // 删除按钮
        mPCDialog.setTitleDeleteIconListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mPCDialog.cancel();
                ContactBean contact = mContacts.get(position);
                /* 查询该号码的隐私通话记录 */
                PrivacyContactUtils.queryDbLogCount(
                        mContext.getContentResolver(), Constants.PRIVACY_CALL_LOG_URI,
                        Constants.COLUMN_CALL_LOG_PHONE_NUMBER + " LIKE ?", new String[]{
                                "%" + contact.getContactNumber()
                        });
                /* 查询该号码的隐私短信 */
                PrivacyContactUtils.queryDbLogCount(
                        mContext.getContentResolver(), Constants.PRIVACY_MESSAGE_URI,
                        Constants.COLUMN_MESSAGE_PHONE_NUMBER + " LIKE ?", new String[]{
                                "%" + contact.getContactNumber()
                        });
                // if (messageNumber > 0 || callLogNumber > 0) {
                showContactDialog(
                        contact,
                        getResources().getString(
                                R.string.privacy_contact_checkbox_dlg_title),
                        getResources().getString(
                                R.string.privacy_contact_checkbox_dlg_content),
                        R.string.privacy_contact_checkbox_dlg_checktext,
                        PrivacyContactUtils.CONTACT_DETAIL_DELETE_LOG, 1);
                // }
            }
        });
        // 发短信按钮
        mPCDialog.setLeftBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPCDialog.cancel();
                String[] bundleData = new String[]{
                        contact.getContactName(), contact.getContactNumber()
                };
                Bundle bundle = new Bundle();
                bundle.putStringArray(Constants.LOCK_MESSAGE_THREAD_ID, bundleData);
                Intent intentSendMessage = new Intent(mContext, PrivacyMessageItemActivity.class);
                intentSendMessage.putExtras(bundle);
                try {
                    startActivity(intentSendMessage);
                    /* SDK */
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "sendmesg", "sendmesg  contact");
                } catch (Exception e) {
                }
            }
        });
//        mPCDialog.setLeftBtnListener(new OnRippleCompleteListener() {
//
//            @Override
//            public void onRippleComplete(RippleView arg0) {
//                mPCDialog.cancel();
//                String[] bundleData = new String[] {
//                        contact.getContactName(), contact.getContactNumber()
//                };
//                Bundle bundle = new Bundle();
//                bundle.putStringArray(Constants.LOCK_MESSAGE_THREAD_ID, bundleData);
//                Intent intentSendMessage = new Intent(mContext, PrivacyMessageItemActivity.class);
//                intentSendMessage.putExtras(bundle);
//                try {
//                    startActivity(intentSendMessage);
//                    /* SDK */
//                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "sendmesg", "sendmesg  contact");
//                } catch (Exception e) {
//                }
//            }
//        });

        // 打电话按钮
        mPCDialog.setRightBtnListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                                /* SDK */
                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "call", "contact");
                mPCDialog.cancel();
                String number = contact.getContactNumber();
                if (TextUtils.isEmpty(number)) {
                    return;
                }
                // 查询该号码是否为隐私联系人
                String formateNumber = PrivacyContactUtils.formatePhoneNumber(number);
                ContactBean privacyConatact = PrivacyContactManager.getInstance(mContext).getPrivateMessage(
                        formateNumber, mContext);
                PrivacyContactManager.getInstance(mContext).setLastCall(
                        privacyConatact);
                Uri uri = Uri.parse("tel:" + number);
                // Intent intent = new Intent(Intent.ACTION_CALL, uri);
                Intent intent = new Intent(Intent.ACTION_DIAL,
                        uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                }
            }
        });
        mPCDialog.setCanceledOnTouchOutside(true);
        mPCDialog.show();
    }

    private void sendDeleteMsgHandler() {
        if (mContactHandler != null) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    LeoLog.i(TAG, "delete logs !");
                    int count = 0;
                    String isOtherLogs = null;
                    ContentResolver cr = mContext.getContentResolver();
                    AppMasterPreference pre = AppMasterPreference.getInstance(mContext);
                    for (ContactBean contact : mDeleteContact) {
                        List<MessageBean> mRestorMessages = null;
                        List<ContactCallLog> mRestorCallLogs = null;
                        mContacts.remove(contact);
                        PrivacyContactManager.getInstance(mActivity).removeContact(contact);
                        PrivacyContactUtils.deleteContactFromMySelf(
                                Constants.COLUMN_PHONE_NUMBER + " = ? ", contact.getContactNumber(),
                                mContext);
                        // 查询删除的联系人有无记录
                        if (mIsChecked) {
                            String tempNumber =
                                    PrivacyContactUtils.formatePhoneNumber(contact.getContactNumber());
                            if (mRestorMessages == null) {
                                mRestorMessages = PrivacyContactUtils.queryPrivacyMsm(cr,
                                        "contact_phone_number LIKE ? ", new String[]{
                                                "%" + tempNumber
                                        });
                            }
                            if (mRestorCallLogs == null) {
                                mRestorCallLogs = PrivacyContactUtils.queryPrivacyfCall(cr,
                                        "call_log_phone_number LIKE ?", new String[]{
                                                "%" + tempNumber
                                        });
                            }
                            if (!"have_log".equals(isOtherLogs) || isOtherLogs == null
                                    || "".equals(isOtherLogs)) {
                                if ((mRestorMessages != null && mRestorMessages.size() != 0)
                                        || (mRestorCallLogs != null && mRestorCallLogs.size() != 0)) {
                                    // 删除拦截短信，通话记录
                                    if (mIsChecked) {
                                        if (mRestorMessages.size() > 0 && mRestorMessages != null) {
                                            mRestorMessagesFlag = true;
                                            int noReadCount = PrivacyContactUtils.getNoReadMessage(
                                                    mContext,
                                                    contact.getContactNumber());
                                            int temp = pre.getMessageNoReadCount();
                                            if (temp > 0) {
                                                if (noReadCount > 0) {
                                                    for (int i = 0; i < noReadCount; i++) {
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
                                            for (MessageBean messageBean : mRestorMessages) {
                                                String number = messageBean.getPhoneNumber();
                                                String formateNumber = PrivacyContactUtils
                                                        .formatePhoneNumber(number);
                                                // 恢复短信
                                                ContentValues values = new ContentValues();
                                                values.put("address", messageBean.getPhoneNumber());
                                                values.put("body", messageBean.getMessageBody());
                                                Long date = null;
                                                try {
                                                    date = Date.parse(messageBean.getMessageTime());
                                                } catch (Exception e1) {
                                                    e1.printStackTrace();
                                                }
                                                values.put("date", date);
                                                values.put("read", 1);
                                                values.put("type", messageBean.getMessageType());
                                                try {
                                                    PrivacyContactUtils.insertMessageToSystemSMS(
                                                            values,
                                                            mContext);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                // 删除短信記錄
                                                try {
                                                    ContentResolver resolver = mContext.getContentResolver();
                                                    Uri url = Constants.PRIVACY_MESSAGE_URI;
                                                    String select = Constants.COLUMN_MESSAGE_PHONE_NUMBER + " LIKE ?";
                                                    String[] selectArgs = new String[]{"%" + formateNumber};
                                                    PrivacyContactUtils.deleteDbLog(resolver, url, select, selectArgs);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        if (mRestorCallLogs != null && mRestorCallLogs.size() > 0) {
                                            int noReadCallLogCount = PrivacyContactUtils
                                                    .getNoReadCallLogCount(mContext,
                                                            contact.getContactNumber());
                                            int callLogTemp = pre.getCallLogNoReadCount();
                                            if (callLogTemp > 0) {
                                                if (noReadCallLogCount > 0) {
                                                    for (int i = 0; i < noReadCallLogCount; i++) {
                                                        if (callLogTemp > 0) {
                                                            callLogTemp = callLogTemp - 1;
                                                            pre.setCallLogNoReadCount(callLogTemp);
                                                        }
                                                        if (callLogTemp <= 0) {
                                                    /* ISwipe处理：通知没有未读 */
                                                            PrivacyContactManager.getInstance(mContext)
                                                                    .cancelPrivacyTipFromPrivacyCall();
                                                            // 没有未读去除隐私通知
                                                            if (pre.getMessageNoReadCount() <= 0) {
                                                                NotificationManager notificationManager = (NotificationManager) getActivity()
                                                                        .getSystemService(
                                                                                Context.NOTIFICATION_SERVICE);
                                                                notificationManager.cancel(PrivacyContactUtils.CALL_NOTIFI_NUMBER);
                                                            }
                                                            LeoEventBus
                                                                    .getDefaultBus()
                                                                    .post(
                                                                            new PrivacyEditFloatEvent(
                                                                                    PrivacyContactUtils.PRIVACY_CONTACT_ACTIVITY_CALL_LOG_CANCEL_RED_TIP_EVENT));
                                                        }
                                                    }
                                                }
                                            }
                                            mRestorCallLogsFlag = true;
                                            for (ContactCallLog calllogBean : mRestorCallLogs) {
                                                String number =
                                                        calllogBean.getCallLogNumber();
                                                String formateNumber = PrivacyContactUtils
                                                        .formatePhoneNumber(number);
                                                /*删除通话记录*/
                                                String select = Constants.COLUMN_CALL_LOG_PHONE_NUMBER + " LIKE ?";
                                                String[] arg = new String[]{"%" + formateNumber};
                                                ContentResolver crs = mContext.getContentResolver();
                                                Uri uri = Constants.PRIVACY_CALL_LOG_URI;
                                                PrivacyContactUtils.deleteDbLog(crs, uri, select, arg);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        PrivacyContactManager.getInstance(mActivity).removeContact(contact);
                        if (mHandler != null) {
                            Message messge = new Message();
                            count = count + 1;
                            messge.what = count;
                            mHandler.sendMessage(messge);
                        }
                    }
                    Message msg = new Message();
                    msg.what = PrivacyContactUtils.MSG_CONTACT_DE;
                    mContactHandler.sendMessage(msg);
                }
            });
        }
    }


    public void showContactDialog(final ContactBean contact, String title, String content,
                                  int checkText,
                                  final String flag, final int model) {
        if (mAddCallLogDialog == null) {
            mAddCallLogDialog = new PrivacyContactDeleteAlarmDialog(mContext);
        }
        mAddCallLogDialog.setRightBtnListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mAddCallLogDialog.cancel();
                mIsChecked = mAddCallLogDialog.getChecked();
                if (mIsChecked) {
                    /* sdk */
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "privacyedit", "deletemesg");
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "privacyedit", "restore_cont");
                    SDKWrapper.addEvent(mContext, SDKWrapper.P1, "privacyedit", "deletecall");
                }
                SDKWrapper.addEvent(mContext, SDKWrapper.P1, "privacyedit", "deletecontact");
                if (PrivacyContactUtils.CONTACT_EDIT_MODEL_OPERATION_DELETE.equals(flag)) {
                    mHandler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            int currentValue = msg.what;
                            LeoLog.i(TAG, "Count=" + mDeleteCount + "——currentValue = " + currentValue);
                            if (currentValue >= mDeleteCount) {

                                if (mProgressDialog != null) {
                                    mProgressDialog.cancel();
                                    if (mContacts == null || mContacts.size() == 0) {
                                        mDefaultText.setVisibility(View.VISIBLE);
                                    } else {
                                        mDefaultText.setVisibility(View.GONE);
                                    }
                                }
                                if (mAdapter != null) {
                                    mAdapter.notifyDataSetChanged();
                                }
                            } else {
                                mProgressDialog.setProgress(currentValue);
                            }
                            super.handleMessage(msg);
                        }
                    };
                    showProgressDialog(mDeleteCount, 0);
                    sendDeleteMsgHandler();
                } else if (PrivacyContactUtils.CONTACT_DETAIL_DELETE_LOG.equals(flag)) {
                    // 执行删除操作
                    if (model == 1) {

                        mContacts.remove(contact);
                        PrivacyContactManager.getInstance(mActivity).removeContact(contact);
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }

                        if (mContacts == null || mContacts.size() == 0) {
                            mDefaultText.setVisibility(View.VISIBLE);
                        } else {
                            mDefaultText.setVisibility(View.GONE);
                        }

                        PrivacyContactEditDetailTask task = new PrivacyContactEditDetailTask();
                        task.execute(contact);
                    }

                }
            }
        });
        mAddCallLogDialog.setLeftBtnListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mAddCallLogDialog.cancel();
                mAddCallLogDialog.setChecked(true);
                restoreParameter();
                mAdapter.notifyDataSetChanged();
                LeoEventBus.getDefaultBus().post(
                        new PrivacyMessageEvent(EventId.EVENT_PRIVACY_EDIT_MODEL,
                                PrivacyContactUtils.EDIT_MODEL_RESTOR_TO_SMS_CANCEL));
            }
        });
        mAddCallLogDialog.setCanceledOnTouchOutside(false);
        mAddCallLogDialog.setContentOne(title);
        mAddCallLogDialog.setContentTwo(content);
        mAddCallLogDialog.setCheckText(checkText);
        mAddCallLogDialog.show();
    }

    private void showProgressDialog(int maxValue, int currentValue) {
        if (mProgressDialog == null) {
            mProgressDialog = new LEORoundProgressDialog(getActivity());
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

    private class PrivacyContactHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case PrivacyContactUtils.MSG_CONTACT:
                    if (msg.obj != null) {
                        LeoLog.i(TAG, "load privacy contacts finish !");
                        List<ContactBean> contacts = (List<ContactBean>) msg.obj;
                        if (mContacts != null) {
                            mContacts.clear();
                        }
                        mContacts = contacts;
                        if (mContacts == null || mContacts.size() == 0) {
                            mDefaultText.setVisibility(View.VISIBLE);
                        } else {
                            mDefaultText.setVisibility(View.GONE);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case PrivacyContactUtils.MSG_CONTACT_DE:
                    mAddCallLogDialog.setChecked(true);
                    mIsEditModel = false;
                    mDeleteCount = 0;
                    mDeleteContact.clear();
                    LeoEventBus.getDefaultBus().post(
                            new PrivacyMessageEvent(EventId.EVENT_PRIVACY_EDIT_MODEL,
                                    PrivacyContactUtils.EDIT_MODEL_RESTOR_TO_SMS_CANCEL));
                    if (mRestorCallLogsFlag) {
                        mRestorCallLogsFlag = false;
                        LeoEventBus
                                .getDefaultBus()
                                .post(new PrivacyEditFloatEvent(
                                        PrivacyContactUtils.CONTACT_DETAIL_DELETE_LOG_UPDATE_CALL_LOG_LIST));
                    }
                    if (mRestorMessagesFlag) {
                        mRestorMessagesFlag = false;
                        LeoEventBus
                                .getDefaultBus()
                                .post(new PrivacyEditFloatEvent(
                                        PrivacyContactUtils.CONTACT_DETAIL_DELETE_LOG_UPDATE_MESSAGE_LIST));
                    }
                    if (mContacts == null || mContacts.size() == 0) {
                        mDefaultText.setVisibility(View.VISIBLE);
                    } else {
                        mDefaultText.setVisibility(View.GONE);
                    }
                    mAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    }

    private void sendMsgHandler() {
        if (mContactHandler != null) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    List<ContactBean> contacts = PrivacyContactManager.getInstance(mActivity).getPrivateContacts();
                    Message msg = new Message();
                    msg.what = PrivacyContactUtils.MSG_CONTACT;
                    msg.obj = contacts;
                    mContactHandler.sendMessage(msg);
                }
            });
        }
    }

    // 详情删除联系人
    private class PrivacyContactEditDetailTask extends AsyncTask<ContactBean, Boolean, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(ContactBean... arg0) {
            ContactBean contact = arg0[0];
            AppMasterPreference pre = AppMasterPreference.getInstance(mContext);
            // no read call log count
            int noReadCount = PrivacyContactUtils.getNoReadCallLogCount(mContext,
                    contact.getContactNumber());
            int temp = pre.getCallLogNoReadCount();
            if (temp > 0) {
                if (noReadCount > 0) {
                    for (int i = 0; i < noReadCount; i++) {
                        if (temp > 0) {
                            temp = temp - 1;
                            pre.setCallLogNoReadCount(temp);
                        }
                        if (temp <= 0) {
                            String msg = PrivacyContactUtils.PRIVACY_CONTACT_ACTIVITY_CALL_LOG_CANCEL_RED_TIP_EVENT;
                            PrivacyEditFloatEvent event = new PrivacyEditFloatEvent(msg);
                            LeoEventBus.getDefaultBus().post(event);
                        }
                    }
                }
            }
            // no read message count
            int noReadMessageCount = PrivacyContactUtils.getNoReadMessage(mContext,
                    contact.getContactNumber());
            int messageTemp = pre.getMessageNoReadCount();
            if (noReadMessageCount > 0) {
                if (noReadCount > 0) {
                    for (int i = 0; i < noReadMessageCount; i++) {
                        if (messageTemp > 0) {
                            messageTemp = messageTemp - 1;
                            pre.setMessageNoReadCount(messageTemp);
                        }
                        if (temp <= 0) {
                            String msg = PrivacyContactUtils.PRIVACY_CONTACT_ACTIVITY_CANCEL_RED_TIP_EVENT;
                            PrivacyEditFloatEvent event = new PrivacyEditFloatEvent(msg);
                            LeoEventBus.getDefaultBus().post(event);
                        }
                    }
                }
            }

            int flagNumber = PrivacyContactUtils.deleteContactFromMySelf(
                    Constants.COLUMN_PHONE_NUMBER + " = ? ",
                    contact.getContactNumber(),
                    mContext);
            if (mContacts != null && mContacts.size() > 0) {
                mContacts.remove(contact);
                PrivacyContactManager.getInstance(mActivity).removeContact(contact);
            }
            if (flagNumber > 0) {
                PrivacyContactManager.getInstance(mActivity).removeContact(contact);
//                PrivacyHelper.getInstance(mActivity).computePrivacyLevel(
//                        PrivacyHelper.VARABLE_PRIVACY_CONTACT);
                // 查询该号码是否有隐私短信，通话记录
                String number = PrivacyContactUtils.formatePhoneNumber(contact
                        .getContactNumber());
                try {
                    if (mIsChecked) {
                        // 恢复短信
                        List<MessageBean> messages = null;
                        String formateNumber = PrivacyContactUtils
                                .formatePhoneNumber(contact.getContactNumber());
                        if (contact != null) {
                            messages = PrivacyContactUtils.queryPrivacyMsm(
                                    mContext.getContentResolver(),
                                    "contact_phone_number LIKE ? ", new String[]{
                                            "%" + formateNumber
                                    });
                        }
                        if (messages != null) {
                            for (MessageBean messageBean : messages) {
                                ContentValues values = new ContentValues();
                                values.put("address", messageBean.getPhoneNumber());
                                values.put("body", messageBean.getMessageBody());
                                Long date = Date.parse(messageBean.getMessageTime());
                                values.put("date", date);
                                values.put("read", 1);
                                values.put("type", messageBean.getMessageType());
                                try {
                                    PrivacyContactUtils.insertMessageToSystemSMS(
                                            values,
                                            mContext);
                                } catch (Exception e) {
                                    LeoLog.i("PrivacyContactFragment Operation",
                                            "PrivacyContactFragment restore message fail!");
                                }
                            }
                        }
                        // 删除短信
                        int deleteMessage = PrivacyContactUtils
                                .deleteDbLog(
                                        mContext.getContentResolver(),
                                        Constants.PRIVACY_MESSAGE_URI,
                                        Constants.COLUMN_MESSAGE_PHONE_NUMBER
                                                + " LIKE ?",
                                        new String[]{
                                                "%" + number
                                        });
                        int deleteCallLog = PrivacyContactUtils
                                .deleteDbLog(
                                        mContext.getContentResolver(),
                                        Constants.PRIVACY_CALL_LOG_URI,
                                        Constants.COLUMN_CALL_LOG_PHONE_NUMBER
                                                + " LIKE ?",
                                        new String[]{
                                                "%" + number
                                        });
                        if (deleteCallLog > 0) {
                            LeoEventBus
                                    .getDefaultBus()
                                    .post(new PrivacyEditFloatEvent(
                                            PrivacyContactUtils.CONTACT_DETAIL_DELETE_LOG_UPDATE_CALL_LOG_LIST));
                        }
                        if (deleteMessage > 0) {
                            LeoEventBus
                                    .getDefaultBus()
                                    .post(new PrivacyEditFloatEvent(
                                            PrivacyContactUtils.CONTACT_DETAIL_DELETE_LOG_UPDATE_MESSAGE_LIST));
                        }
                    }
                } catch (Exception e) {

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mAddCallLogDialog.setChecked(true);
            if (mContacts == null || mContacts.size() == 0) {
                mDefaultText.setVisibility(View.VISIBLE);
            } else {
                mDefaultText.setVisibility(View.GONE);
            }
            mAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }

    }
}
