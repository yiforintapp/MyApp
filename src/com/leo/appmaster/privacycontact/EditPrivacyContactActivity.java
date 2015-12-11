
package com.leo.appmaster.privacycontact;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyEditFloatEvent;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.ui.dialog.LEOCircleProgressDialog;
import com.leo.appmaster.utils.Utilities;

@SuppressLint("ResourceAsColor")
public class EditPrivacyContactActivity extends BaseActivity {
    private CommonToolbar mTtileBar;
    private EditText mNameEt, mNumberEt;
    private int mPhoneState = 1;
    private CheckBox mRadioNormal, mRadioHangup;
    private String mPhoneName, mPhoneNumber;
    private static final String CONTACT_CALL_LOG = "contact_call_log";
    private String mCurrentName, mCurrentNumber;
    private ImageView mCloseImage;
    private TextView mPhoneNumberShow;
    private LEOAlarmDialog mEditDialog;
    private int mAnswerType;
    private LEOCircleProgressDialog mProgressDialg;
    private EditContactHandler mEditContactHandler = new EditContactHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_contact_input);
        initUI();
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        String[] bundleData = bundle.getStringArray(CONTACT_CALL_LOG);
        mCurrentName = bundleData[0];
        if (mCurrentName == null) {
            mCurrentName = "";
        }
        mCurrentNumber = bundleData[1];
        mAnswerType = Integer.valueOf(bundleData[2]);
        mNameEt.setText(mCurrentName);
        mNumberEt.setText(mCurrentNumber);
        if (mAnswerType == 0) {
            mPhoneState = 0;
            mRadioHangup.setSelected(true);
        } else if (mAnswerType == 1) {
            mPhoneState = 1;
            mRadioNormal.setSelected(true);
        }
        mRadioNormal.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                mPhoneState = 1;
                mRadioNormal.setSelected(true);
                mRadioHangup.setSelected(false);
                SDKWrapper
                        .addEvent(getApplicationContext(), SDKWrapper.P1, "contactsadd", "answer");
            }
        });
        mRadioHangup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                mPhoneState = 0;
                mRadioNormal.setSelected(false);
                mRadioHangup.setSelected(true);
                SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, "contactsadd",
                        "decline");
            }
        });
        mTtileBar.setOptionClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!Utilities.isEmpty(mPhoneNumber)) {
                    showEditContactDialog(
                            getResources().getString(R.string.privacy_contact_edit_content),
                            getResources().getString(R.string.privacy_contact_edit_right_btn),
                            getResources().getString(R.string.privacy_contact_edit_left_btn));
                } else {
                    Toast.makeText(EditPrivacyContactActivity.this,
                            getResources().getString(R.string.input_toast_no_number_tip),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initUI() {
        mTtileBar = (CommonToolbar) findViewById(R.id.title_bar);
        mTtileBar.setOptionMenuVisible(true);
        mTtileBar.setToolbarTitle(getResources().getString(
                R.string.privacy_contact_edit_contact));
        mTtileBar.setOptionImageResource(R.drawable.mode_done);
        mNameEt = (EditText) findViewById(R.id.privacy_input_nameET);
        mNumberEt = (EditText) findViewById(R.id.privacy_input_numberEV);
        mRadioNormal = (CheckBox) findViewById(R.id.privacy_input_normalRB);
        mRadioHangup = (CheckBox) findViewById(R.id.privacy_input_hangupRB);
        mCloseImage = (ImageView) findViewById(R.id.close_image);
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
                if (mPhoneName == null || "".equals(mPhoneName)) {
                    mCloseImage.setVisibility(View.GONE);
                }

            }
        };
        mNumberEt.addTextChangedListener(watcher);
        mNameEt.addTextChangedListener(watcher);
        mCloseImage.setVisibility(View.VISIBLE);
        mCloseImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mNameEt.setText("");
                mCloseImage.setVisibility(View.GONE);
            }
        });
        mNameEt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mCloseImage.getVisibility() == View.VISIBLE) {
                    mCloseImage.setVisibility(View.GONE);
                }
            }
        });

    }

    private void checkEditTextAdd() {
        mPhoneName = mNameEt.getText().toString();
        mPhoneNumber = mNumberEt.getText().toString().trim();
    }

    // 更新修改数据
    private int updateMessageMyselfIsRead(ContentValues values, String selection,
                                          String[] selectionArgs) {
        int updateUmber = -1;
        try {
            updateUmber = this.getContentResolver().update(Constants.PRIVACY_CONTACT_URI,
                    values, selection,
                    selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updateUmber;
    }

    // 更新隐私短信联系人名称
    private void updateMessageMyselfName(ContentValues values, String selection,
                                         String formateNumber) {
        List<String> messageNumbers = new ArrayList<String>();
        Cursor cusor = null;
        try {
            cusor = this.getContentResolver().query(Constants.PRIVACY_MESSAGE_URI, null,
                    "contact_phone_number LIKE ? ", new String[]{
                            "%" + formateNumber
                    }, null);
            if (cusor != null && cusor.getCount() > 0) {
                while (cusor.moveToNext()) {
                    String number = cusor.getString(cusor
                            .getColumnIndex(Constants.COLUMN_MESSAGE_PHONE_NUMBER));
                    messageNumbers.add(number);
                }
            }
        } catch (Exception e) {
        } finally {
            if (cusor != null) {
                cusor.close();
            }
        }

        for (String string : messageNumbers) {
            try {
                this.getContentResolver().update(Constants.PRIVACY_MESSAGE_URI,
                        values, selection,
                        new String[]{
                                string
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 更新隐私通话联系人名称
    private void updateCallLogMyselfName(ContentValues values, String selection, String number) {
        List<String> callLogNumbers = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = this.getContentResolver().query(Constants.PRIVACY_CALL_LOG_URI, null,
                    "call_log_phone_number LIKE ? ", new String[]{
                            "%" + number
                    }, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String calllog = cursor.getString(cursor
                            .getColumnIndex(Constants.COLUMN_CALL_LOG_PHONE_NUMBER));
                    callLogNumbers.add(calllog);
                }
            }
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        for (String string : callLogNumbers) {
            try {
                this.getContentResolver().update(Constants.PRIVACY_CALL_LOG_URI, values, selection,
                        new String[]{
                                string
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void showEditContactDialog(String content, String leftBtn, String rightBtn) {
        if (mEditDialog == null) {
            mEditDialog = new LEOAlarmDialog(this);
        }
        mEditDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
//                    UpdateContactNameTask task = new UpdateContactNameTask();
//                    task.execute(true);
                    sendEditContactHandler();
                } else if (which == 0) {
                    mEditDialog.cancel();
                    EditPrivacyContactActivity.this.finish();
                }
            }
        });
        mEditDialog.setCanceledOnTouchOutside(false);
        mEditDialog.setLeftBtnStr(leftBtn);
        mEditDialog.setRightBtnStr(rightBtn);
        mEditDialog.setContent(content);
        mEditDialog.show();
    }

    private class UpdateContactDateTask extends AsyncTask<Boolean, Boolean, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Boolean... arg0) {
            PrivacyContactManager.getInstance(EditPrivacyContactActivity.this)
                    .updateContact();
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            LeoEventBus
                    .getDefaultBus()
                    .post(
                            new PrivacyEditFloatEvent(
                                    PrivacyContactUtils.PRIVACY_ADD_CONTACT_UPDATE));
            EditPrivacyContactActivity.this.finish();
        }
    }

    private class UpdateContactNameTask extends AsyncTask<Boolean, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Integer doInBackground(Boolean... arg0) {
            mPhoneName = mNameEt.getText().toString();
            mPhoneNumber = mNumberEt.getText().toString().trim();
            ContentValues values = new ContentValues();
            values.put(Constants.COLUMN_PHONE_NUMBER, PrivacyContactUtils.simpleFromateNumber(mPhoneNumber));
            values.put(Constants.COLUMN_CONTACT_NAME, mPhoneName);
            values.put(Constants.COLUMN_PHONE_ANSWER_TYPE, mPhoneState);
            int result = updateMessageMyselfIsRead(values, "contact_phone_number = ? ",
                    new String[]{
                            mCurrentNumber
                    });
            // 判断是否更新隐私短信和隐私通话列表
            String number = PrivacyContactUtils.formatePhoneNumber(mPhoneNumber);
            if (mCurrentNumber.contains(number) && !mCurrentName.equals(mPhoneName)) {
                ContentValues updateMessageNameValues = new ContentValues();
                updateMessageNameValues.put(Constants.COLUMN_MESSAGE_CONTACT_NAME, mPhoneName);
                ContentValues updateCallLogNameValues = new ContentValues();
                updateCallLogNameValues.put(Constants.COLUMN_CALL_LOG_CONTACT_NAME, mPhoneName);
                String formateNumber =
                        PrivacyContactUtils.formatePhoneNumber(mCurrentNumber);
                // 更新隐私短信名称
                updateMessageMyselfName(updateMessageNameValues,
                        "contact_phone_number = ?", formateNumber);
                // 更新隐私通话名称
                updateCallLogMyselfName(updateCallLogNameValues,
                        "call_log_phone_number = ? ", formateNumber);
                //通知更新列表
                LeoEventBus
                        .getDefaultBus()
                        .post(
                                new PrivacyEditFloatEvent(
                                        PrivacyContactUtils.PRIVACY_EDIT_NAME_UPDATE_CALL_LOG_EVENT));
                LeoEventBus
                        .getDefaultBus()
                        .post(
                                new PrivacyEditFloatEvent(
                                        PrivacyContactUtils.PRIVACY_EDIT_NAME_UPDATE_MESSAGE_EVENT));

            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result > 0) {
//                UpdateContactDateTask task = new UpdateContactDateTask();
//                task.execute(true);
                sendEditLogHandler();
            }
            /* sdk */
            if (mPhoneName != null && !mCurrentName.equals(mPhoneName)) {
                SDKWrapper.addEvent(EditPrivacyContactActivity.this, SDKWrapper.P1,
                        "privacyedit", "editname");
            }
            if (mCurrentNumber != null && !mCurrentNumber.equals(mPhoneNumber)) {
                SDKWrapper.addEvent(EditPrivacyContactActivity.this, SDKWrapper.P1,
                        "privacyedit", "editnumber");
            }
            if (mPhoneState != mAnswerType) {
                if (mPhoneState == 0) {
                    SDKWrapper.addEvent(EditPrivacyContactActivity.this, SDKWrapper.P1,
                            "privacyedit", "editdeline");
                } else if (mPhoneState == 1) {
                    SDKWrapper.addEvent(EditPrivacyContactActivity.this, SDKWrapper.P1,
                            "privacyedit", "editanswer");
                }
            }
            EditPrivacyContactActivity.this.finish();
            if (mProgressDialg != null) {
                mProgressDialg.dismiss();
            }
        }
    }

    private void showProgressDialog() {
        if (mProgressDialg == null) {
            mProgressDialg = new LEOCircleProgressDialog(this);
        }
        mProgressDialg.setButtonVisiable(false);
        mProgressDialg.setCanceledOnTouchOutside(false);
        mProgressDialg.setIndeterminate(false);
        mProgressDialg.show();
    }

    private class EditContactHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PrivacyContactUtils.MSG_EDIT_CONTACT:
                    Toast.makeText(EditPrivacyContactActivity.this, "MSG_EDIT_CONTACT", Toast.LENGTH_SHORT).show();
                    int result = msg.arg1;
                    if (result > 0) {
//                        UpdateContactDateTask task = new UpdateContactDateTask();
//                        task.execute(true);
                        sendEditLogHandler();
                    }
                    /* sdk */
                    if (mPhoneName != null && !mCurrentName.equals(mPhoneName)) {
                        SDKWrapper.addEvent(EditPrivacyContactActivity.this, SDKWrapper.P1,
                                "privacyedit", "editname");
                    }
                    if (mCurrentNumber != null && !mCurrentNumber.equals(mPhoneNumber)) {
                        SDKWrapper.addEvent(EditPrivacyContactActivity.this, SDKWrapper.P1,
                                "privacyedit", "editnumber");
                    }
                    if (mPhoneState != mAnswerType) {
                        if (mPhoneState == 0) {
                            SDKWrapper.addEvent(EditPrivacyContactActivity.this, SDKWrapper.P1,
                                    "privacyedit", "editdeline");
                        } else if (mPhoneState == 1) {
                            SDKWrapper.addEvent(EditPrivacyContactActivity.this, SDKWrapper.P1,
                                    "privacyedit", "editanswer");
                        }
                    }
                    if (mProgressDialg != null) {
                        mProgressDialg.dismiss();
                    }
                    EditPrivacyContactActivity.this.finish();
                    break;
                case PrivacyContactUtils.MSG_EDIT_LOG:
                    Toast.makeText(EditPrivacyContactActivity.this, "MSG_EDIT_LOG", Toast.LENGTH_SHORT).show();
                    LeoEventBus
                            .getDefaultBus()
                            .post(
                                    new PrivacyEditFloatEvent(
                                            PrivacyContactUtils.PRIVACY_ADD_CONTACT_UPDATE));
                    EditPrivacyContactActivity.this.finish();
                    break;
                default:
                    break;
            }

        }
    }

    private void sendEditContactHandler() {
        if (mEditContactHandler != null) {
            showProgressDialog();
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    mPhoneName = mNameEt.getText().toString();
                    mPhoneNumber = mNumberEt.getText().toString().trim();
                    ContentValues values = new ContentValues();
                    values.put(Constants.COLUMN_PHONE_NUMBER, PrivacyContactUtils.simpleFromateNumber(mPhoneNumber));
                    values.put(Constants.COLUMN_CONTACT_NAME, mPhoneName);
                    values.put(Constants.COLUMN_PHONE_ANSWER_TYPE, mPhoneState);
                    int result = updateMessageMyselfIsRead(values, "contact_phone_number = ? ",
                            new String[]{
                                    mCurrentNumber
                            });
                    // 判断是否更新隐私短信和隐私通话列表
                    String number = PrivacyContactUtils.formatePhoneNumber(mPhoneNumber);
                    if (mCurrentNumber.contains(number) && !mCurrentName.equals(mPhoneName)) {
                        ContentValues updateMessageNameValues = new ContentValues();
                        updateMessageNameValues.put(Constants.COLUMN_MESSAGE_CONTACT_NAME, mPhoneName);
                        ContentValues updateCallLogNameValues = new ContentValues();
                        updateCallLogNameValues.put(Constants.COLUMN_CALL_LOG_CONTACT_NAME, mPhoneName);
                        String formateNumber =
                                PrivacyContactUtils.formatePhoneNumber(mCurrentNumber);
                        // 更新隐私短信名称
                        updateMessageMyselfName(updateMessageNameValues,
                                "contact_phone_number = ?", formateNumber);
                        // 更新隐私通话名称
                        updateCallLogMyselfName(updateCallLogNameValues,
                                "call_log_phone_number = ? ", formateNumber);
                        //通知更新列表
                        LeoEventBus
                                .getDefaultBus()
                                .post(
                                        new PrivacyEditFloatEvent(
                                                PrivacyContactUtils.PRIVACY_EDIT_NAME_UPDATE_CALL_LOG_EVENT));
                        LeoEventBus
                                .getDefaultBus()
                                .post(
                                        new PrivacyEditFloatEvent(
                                                PrivacyContactUtils.PRIVACY_EDIT_NAME_UPDATE_MESSAGE_EVENT));

                    }
                    Message msg = new Message();
                    msg.what = PrivacyContactUtils.MSG_EDIT_CONTACT;
                    msg.arg1 = result;
                    mEditContactHandler.sendMessage(msg);
                }
            });
        }
    }

    private void sendEditLogHandler() {
        if (mEditContactHandler != null) {
            ThreadManager.executeOnAsyncThread(new Runnable() {
                @Override
                public void run() {
                    PrivacyContactManager.getInstance(EditPrivacyContactActivity.this)
                            .updateContact();
                    Message msg = new Message();
                    msg.what = PrivacyContactUtils.MSG_EDIT_LOG;
                    mEditContactHandler.sendMessage(msg);
                }
            });
        }
    }
}
