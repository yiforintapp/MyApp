package com.leo.appmaster.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.callfilter.CallFilterConstants;
import com.leo.appmaster.cloud.crypto.CryptoUtils;
import com.leo.appmaster.db.AppMasterProvider;
import com.leo.imageloader.utils.IoUtils;

/**
 * Created by Jasper on 2015/12/23.
 */
public class DebugActivity extends Activity implements View.OnClickListener {
    private TextView mDecryptTv;
    private Button mDecryptBtn;
    private TextView mDecryptMsgTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        mDecryptTv = (TextView) findViewById(R.id.decrypt_et);
        mDecryptBtn = (Button) findViewById(R.id.decrypt_btn);

        mDecryptMsgTv = (TextView) findViewById(R.id.decrypt_msg_tv);

        mDecryptBtn.setOnClickListener(this);
        findViewById(R.id.insert_data).setOnClickListener(this);
        findViewById(R.id.update_data).setOnClickListener(this);
        findViewById(R.id.query_data).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        ContentResolver cr = getContentResolver();
        ContentValues value = new ContentValues();
        Uri uri = CallFilterConstants.BLACK_LIST_URI;
        switch (v.getId()) {
            case R.id.decrypt_btn:
                String text = mDecryptTv.getEditableText().toString();
                if (TextUtils.isEmpty(text)) {
                    break;
                }
                try {
                    text = CryptoUtils.decrypt(text);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                mDecryptMsgTv.setText(text);
                break;
        }
    }
}
