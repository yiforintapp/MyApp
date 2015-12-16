
package com.leo.appmaster.privacycontact;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.CommonTitleBar;

/**
 * 未使用，作废
 */
public class PrivacyMessageSendActivity extends BaseActivity implements OnClickListener {
    private CommonTitleBar mComTitle;
    private static final String CONTACT_CALL_LOG = "contact_call_log";
    private static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    private String mName;
    private String mCallLogNumber;
    private EditText mEditText;
    private static final int SEND_MESSAGE = 2;
    private Button mSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_message_send);
        mComTitle = (CommonTitleBar) findViewById(R.id.send_message_item_layout_title_bar);
        mSendButton = (Button) findViewById(R.id.send_message_send_button);
        mEditText = (EditText) findViewById(R.id.message_send_edit_text);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        String[] bundleData = bundle.getStringArray(CONTACT_CALL_LOG);
        mName = bundleData[0];
        mCallLogNumber = bundleData[1];
        mComTitle.setTitle(mName);
        mComTitle.openBackView();
        mSendButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        SmsManager sms = SmsManager.getDefault();
        String messageContent = mEditText.getText().toString();
        ArrayList<String> divideMessageContents = sms.divideMessage(messageContent);
        try {
            for (String text : divideMessageContents) {
                Intent sentIntent = new Intent(SENT_SMS_ACTION);
                PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                        sentIntent, 0);
                sms.sendTextMessage(mCallLogNumber, null, text, sentPI, null);
            }
            mEditText.getText().clear();
            if (!messageContent.equals("") && messageContent != null) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String date = df.format(new Date());
                ContentValues values = new ContentValues();
                values.put(Constants.COLUMN_MESSAGE_PHONE_NUMBER, mCallLogNumber);
                values.put(Constants.COLUMN_MESSAGE_BODY, messageContent);
                values.put(Constants.COLUMN_MESSAGE_DATE, date);
                values.put(Constants.COLUMN_MESSAGE_IS_READ, 1);
                values.put(Constants.COLUMN_MESSAGE_TYPE, SEND_MESSAGE);
                Uri line = getContentResolver().insert(Constants.PRIVACY_MESSAGE_URI, values);
                if (line == null) {
                    Log.d("LockMessageItemActivity", "Send message insert fail!");
                }
            }
        } catch (Exception e) {

        } finally {
            this.finish();
        }
    }
}
