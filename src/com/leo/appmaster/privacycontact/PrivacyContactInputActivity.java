
package com.leo.appmaster.privacycontact;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.e;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.mgr.MgrContext;
import com.leo.appmaster.mgr.impl.PrivacyContactManagerImpl;
import com.leo.appmaster.phoneSecurity.PhoneSecurityConstants;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOProgressDialog;
import com.leo.appmaster.ui.dialog.LEORoundProgressDialog;

public class PrivacyContactInputActivity extends BaseActivity {
    private CommonToolbar mTtileBar;
    private EditText mNameEt, mNumberEt;
    private int mPhoneState = 1;
    private CheckBox mRadioNormal, mRadioHangup;
    private String mPhoneName, mPhoneNumber;
    private List<MessageBean> mAddMessages;
    private List<ContactCallLog> mAddCallLogs;
    private boolean mIsOtherLogs = false;
    private LEORoundProgressDialog mProgressDialog;
    private LEOAlarmDialog mAddCallLogDialog;
    private TextView mPhoneNumberShow;
    private Handler mHandler;
    private int mMsmCount, mCallCount;

    public static final String TO_CONTACT_LIST = "to_contact_list";
    private boolean mToContactList = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_contact_input);
        initUI();
        Intent intent = getIntent();
        mToContactList = intent.getBooleanExtra(TO_CONTACT_LIST, false);
        mRadioNormal.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                /* sdk */
                SDKWrapper.addEvent(PrivacyContactInputActivity.this, SDKWrapper.P1,
                        "contactsadd", "answer");
                mPhoneState = 1;
                mRadioNormal.setSelected(true);
                mRadioHangup.setSelected(false);
            }
        });
        mRadioHangup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                mPhoneState = 0;
                mRadioNormal.setSelected(false);
                mRadioHangup.setSelected(true);
            }
        });
        mTtileBar.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!"".equals(mPhoneNumber) && mPhoneNumber != null) {
                    PrivacyContactManager pcm = PrivacyContactManager
                            .getInstance(PrivacyContactInputActivity.this);
                    ArrayList<ContactBean> contacts = pcm.getPrivateContacts();

                    /*隐私联系人去重*/
                    String tempNumber =
                            PrivacyContactUtils.formatePhoneNumber(mPhoneNumber);

                    boolean flagContact = PrivacyContactUtils.pryContRemovSame(mPhoneNumber);
                    if (!flagContact) {
                        ContactBean contact = new ContactBean();
                        contact.setContactName(mPhoneName);
                        contact.setContactNumber(mPhoneNumber);
                        contact.setAnswerType(mPhoneState);
                        ContentValues values = new ContentValues();
                        values.put(Constants.COLUMN_PHONE_NUMBER, mPhoneNumber);
                        if (mPhoneName != null && !"".equals(mPhoneName)) {
                            values.put(Constants.COLUMN_CONTACT_NAME, mPhoneName);
                        } else {
                            values.put(Constants.COLUMN_CONTACT_NAME, mPhoneNumber);
                        }
                        values.put(Constants.COLUMN_PHONE_ANSWER_TYPE, mPhoneState);
                        Uri result = null;
                        result = PrivacyContactInputActivity.this.getContentResolver().insert(Constants.PRIVACY_CONTACT_URI, values);
                        long id = ContentUris.parseId(result);
                        if (id != -1) {
                            if (contacts != null) {
                                pcm.addContact(contact);
                            }

                            ContentResolver cr = PrivacyContactInputActivity.this.getContentResolver();
                            PrivacyContactManagerImpl pm = (PrivacyContactManagerImpl) MgrContext.getManager(MgrContext.MGR_PRIVACY_CONTACT);
                            /*查看是否存在短信*/
                            Cursor curMsm = pm.getSystemMessages("address LIKE ? ", new String[]{"%" + tempNumber});
                            if (curMsm != null) {
                                mMsmCount = curMsm.getCount();
                            }
                            /*查询是否存在通话*/
                            Cursor callCur = pm.getSystemCalls("number LIKE ?", new String[]{"%" + tempNumber});
                            if (callCur != null) {
                                mCallCount = callCur.getCount();
                            }
                            if (!mIsOtherLogs) {
                                if (mMsmCount > 0 || mCallCount > 0) {
                                    mIsOtherLogs = true;
                                }
                            }
                            if (mIsOtherLogs) {
                                String title = getResources().getString(
                                        R.string.privacy_contact_add_log_dialog_title);
                                String content = getResources().getString(
                                        R.string.privacy_contact_add_log_dialog_dialog_content);
                                showAddContactLogDialog(title, content);
                            } else {
                                toContactList();
                            }
                        }
                        /* "添加成功！", 通知更新隐私联系人列表*/
                        String msg = PrivacyContactUtils.PRIVACY_ADD_CONTACT_UPDATE;
                        PrivacyEditFloatEvent event = new PrivacyEditFloatEvent(msg);
                        LeoEventBus.getDefaultBus().post(event);
                    } else {
                        Context context = PrivacyContactInputActivity.this;
                        String str = getResources().getString(R.string.privacy_add_contact_toast);
                        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Context context = PrivacyContactInputActivity.this;
                    String str = getResources().getString(R.string.input_toast_no_number_tip);
                    Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*查询系统是否有指定号码短信和通话记录*/
    private void queryCallsMsms(String number) {
        /*4.4以上不去做短信操作*/
        boolean isLessLeve19 = PrivacyContactUtils.isLessApiLeve19();
        if (isLessLeve19) {
            if (mAddMessages == null) {
                mAddMessages = PrivacyContactManager.getInstance(this).queryMsmsForNumber(number);
            } else {
                List<MessageBean> addMessages = PrivacyContactManager.getInstance(this).queryMsmsForNumber(number);
                mAddMessages.addAll(addMessages);
            }
        }
        /*查询通话记录*/
        if (mAddCallLogs == null) {
            mAddCallLogs = PrivacyContactManager.getInstance(this).queryCallsForNumber(number);
        } else {
            List<ContactCallLog> addCalllog = PrivacyContactManager.getInstance(this).queryCallsForNumber(number);
            mAddCallLogs.addAll(addCalllog);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initUI() {
        mTtileBar = (CommonToolbar) findViewById(R.id.title_bar);
        mTtileBar.setOptionMenuVisible(true);
        mTtileBar.setToolbarColorResource(R.color.cb);
        mTtileBar.setToolbarTitle(R.string.privacy_contact_popumenus_add_new_privacy_contact);
        mTtileBar.setOptionImageResource(R.drawable.mode_done);
        mNameEt = (EditText) findViewById(R.id.privacy_input_nameET);
        mNumberEt = (EditText) findViewById(R.id.privacy_input_numberEV);
        mRadioNormal = (CheckBox) findViewById(R.id.privacy_input_normalRB);
        mRadioNormal.setSelected(true);
        mRadioHangup = (CheckBox) findViewById(R.id.privacy_input_hangupRB);
        mPhoneNumberShow = (TextView) findViewById(R.id.privacy_input_numberTV);
        String numberTip = getResources().getString(
                R.string.privacy_contact_activity_input_edit_number);
        Spanned spannedText = Html.fromHtml(numberTip);
        mPhoneNumberShow.setText(spannedText);
        TextWatcher watcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                checkEditTextAdd();
            }
        };
        mNumberEt.addTextChangedListener(watcher);
        mNameEt.addTextChangedListener(watcher);
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
        mProgressDialog.show();
    }

    private void showAddContactLogDialog(String title, String content) {
        if (mAddCallLogDialog == null) {
            mAddCallLogDialog = new LEOAlarmDialog(this);
        }
        mAddCallLogDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    final int privacyTotal = mMsmCount + mCallCount;
                    if (mHandler == null) {
                        mHandler = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                int currentValue = msg.what;
                                if (currentValue >= privacyTotal) {
                                    mProgressDialog.cancel();
                                    toContactList();
                                } else {
                                    mProgressDialog.setProgress(currentValue);
                                }
                                super.handleMessage(msg);
                            }
                        };
                    }
                    showProgressDialog(privacyTotal, 0);
                    QueryLogAsyncTask task = new QueryLogAsyncTask();
                    task.execute(true);
                } else if (which == 0) {
                    /* SDK */
                    SDKWrapper.addEvent(PrivacyContactInputActivity.this, SDKWrapper.P1,
                            "contactsadd", "unimport");
                    if (mAddCallLogDialog != null) {
                        mAddCallLogDialog.cancel();
                    }
                    toContactList();
                }
            }
        });
        mAddCallLogDialog.setCanceledOnTouchOutside(false);
        mAddCallLogDialog.setTitle(title);
        mAddCallLogDialog.setContent(content);
        mAddCallLogDialog.show();
    }

    private class QueryLogAsyncTask extends AsyncTask<Boolean, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Boolean... arg0) {
            boolean flag = arg0[0];
            int count = 0;
            ContentResolver cr = getContentResolver();
            if (flag) {
                queryCallsMsms(mPhoneNumber);
                /*导入短信和通话记录*/
                if (mAddMessages != null && mAddMessages.size() != 0) {
                    for (MessageBean message : mAddMessages) {
                        String contactNumber = message.getPhoneNumber();
                        String number = PrivacyContactUtils.deleteOtherNumber(contactNumber);
                        // String name = message.getMessageName();
                        String name = null;
                        if (mPhoneName != null && !"".equals(mPhoneName)) {
                            name = mPhoneName;
                        } else {
                            name = mPhoneNumber;
                        }
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
                                PrivacyContactInputActivity.this, message.getPhoneNumber());
                        values.put(Constants.COLUMN_MESSAGE_THREAD_ID, thread);
                        values.put(Constants.COLUMN_MESSAGE_IS_READ, isRead);
                        values.put(Constants.COLUMN_MESSAGE_TYPE, type);
                        Uri messageFlag = cr.insert(Constants.PRIVACY_MESSAGE_URI, values);
                        PrivacyContactUtils.deleteMessageFromSystemSMS("address = ?",
                                new String[]{
                                        number
                                }, PrivacyContactInputActivity.this);
                        if (messageFlag != null) {
                            Message messge = new Message();
                            count = count + 1;
                            messge.what = count;
                            mHandler.sendMessage(messge);
                        }
                    }
                }

                /*导入通话记录*/
                if (mAddCallLogs != null && mAddCallLogs.size() != 0) {
                    for (ContactCallLog calllog : mAddCallLogs) {
                        String number = calllog.getCallLogNumber();
                        // String name = calllog.getCallLogName();
                        String name = null;
                        if (mPhoneName != null && !"".equals(mPhoneName)) {
                            name = mPhoneName;
                        } else {
                            name = mPhoneNumber;
                        }
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
                                PrivacyContactInputActivity.this);
                        if (callLogFlag != null) {
                            Message messge = new Message();
                            count = count + 1;
                            messge.what = count;
                            mHandler.sendMessage(messge);
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (mAddCallLogs != null && mAddCallLogs.size() != 0) {
                String msg = PrivacyContactUtils.UPDATE_CALL_LOG_FRAGMENT;
                PrivacyEditFloatEvent editEvent = new PrivacyEditFloatEvent(msg);
                LeoEventBus.getDefaultBus().post(editEvent);
            }

            if (mAddMessages != null && mAddMessages.size() != 0) {
                String msg = PrivacyContactUtils.UPDATE_MESSAGE_FRAGMENT;
                PrivacyEditFloatEvent event = new PrivacyEditFloatEvent(msg);
                LeoEventBus.getDefaultBus().post(event);
            }
        }
    }

    private void checkEditTextAdd() {
        mPhoneName = mNameEt.getText().toString();
        mPhoneNumber = mNumberEt.getText().toString().trim();
    }

    /**
     * 跳转联系人列表
     */
    private void toContactList() {
        if (mToContactList) {
            Intent intent = new Intent(this, PrivacyContactActivity.class);
            intent.putExtra(PrivacyContactUtils.TO_PRIVACY_CONTACT, PrivacyContactUtils.TO_PRIVACY_CONTACT_FLAG);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            PrivacyContactInputActivity.this.finish();
        }
        mMsmCount = 0;
        mCallCount = 0;
    }
}
