
package com.leo.appmaster.privacycontact;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.Utilities;

@SuppressLint("ResourceAsColor")
public class EditPrivacyContactActivity extends BaseActivity {
    private CommonTitleBar mTtileBar;
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
        mRadioHangup.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                mPhoneState = 0;
                mRadioNormal.setSelected(false);
                mRadioHangup.setSelected(true);
                SDKWrapper.addEvent(getApplicationContext(), SDKWrapper.P1, "contactsadd",
                        "decline");
            }
        });
        mTtileBar.setOptionListener(new OnClickListener() {
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
        mTtileBar = (CommonTitleBar) findViewById(R.id.title_bar);
        mTtileBar.setTitle(getResources().getString(
                R.string.privacy_contact_edit_contact));
        mTtileBar.openBackView();
        mTtileBar.setOptionImage(R.drawable.mode_done);
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

    }

    private void checkEditTextAdd() {
        // boolean commitState = true;
        mPhoneName = mNameEt.getText().toString();
        mPhoneNumber = mNumberEt.getText().toString().trim();
        // if (mPhoneNumber.isEmpty()) {
        // commitState = false;
        // }
        // mAddBt.setEnabled(commitState);
        // mAddBt.setBackgroundResource(commitState ?
        // R.drawable.check_update_button
        // : R.drawable.update_btn_disable_bg);
    }

    // 更新修改数据
    private int updateMessageMyselfIsRead(ContentValues values, String selection,
            String[] selectionArgs) {
        int updateUmber = this.getContentResolver().update(Constants.PRIVACY_CONTACT_URI,
                values, selection,
                selectionArgs);
        return updateUmber;
    }

    private void showEditContactDialog(String content, String leftBtn, String rightBtn) {
        if (mEditDialog == null) {
            mEditDialog = new LEOAlarmDialog(this);
        }
        mEditDialog.setOnClickListener(new OnDiaogClickListener() {
            @Override
            public void onClick(int which) {
                if (which == 1) {
                    mPhoneName = mNameEt.getText().toString();
                    mPhoneNumber = mNumberEt.getText().toString().trim();
                    ContentValues values = new ContentValues();
                    values.put(Constants.COLUMN_PHONE_NUMBER, mPhoneNumber);
                    values.put(Constants.COLUMN_CONTACT_NAME, mPhoneName);
                    values.put(Constants.COLUMN_PHONE_ANSWER_TYPE, mPhoneState);
                    int result = updateMessageMyselfIsRead(values, "contact_phone_number = ? ",
                            new String[] {
                                mCurrentNumber
                            });
                    if (result > 0) {
                        UpdateContactDateTask task = new UpdateContactDateTask();
                        task.execute(true);

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
                            new PrivacyDeletEditEventBus(
                                    PrivacyContactUtils.PRIVACY_ADD_CONTACT_UPDATE));
            EditPrivacyContactActivity.this.finish();
        }
    }
}
