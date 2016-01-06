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
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.dialog.LEOMessageDialog;
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mDecryptMsgTv.setText(text);
                break;
            case R.id.insert_data:
                /*新增操作*/
                value.put(CallFilterConstants.BLACK_LOC_HD, 1);
                value.put(CallFilterConstants.BLACK_UPLOAD_STATE, 1);
                value.put(CallFilterConstants.BLACK_REMOVE_STATE, 1);
                value.put(CallFilterConstants.BLACK_READ_STATE, 1);
                value.put(CallFilterConstants.BLACK_FIL_UP, 1);
                value.put(CallFilterConstants.BLACK_PHONE_NUMBER, "123");
                cr.insert(uri, value);
                break;
            case R.id.update_data:
                value.put(AppMasterProvider.SQL_INSERT_OR_REPLACE, true);
                value.put(CallFilterConstants.BLACK_LOC_HD, 2);
                value.put(CallFilterConstants.BLACK_UPLOAD_STATE, 2);
                value.put(CallFilterConstants.BLACK_PHONE_NUMBER, "123");
                cr.insert(uri, value);
                break;
            case R.id.query_data:
                Cursor cursor = null;
                try {
                    cursor = cr.query(uri, null, null, null, null);
                    StringBuilder stringBuilder = new StringBuilder();
                    if (cursor != null && cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            stringBuilder.append(cursor.getInt(cursor.getColumnIndex(CallFilterConstants.BLACK_LOC_HD)))
                                    .append("\n");
                            stringBuilder.append(cursor.getInt(cursor.getColumnIndex(CallFilterConstants.BLACK_UPLOAD_STATE)))
                                    .append("\n");
                            stringBuilder.append(cursor.getInt(cursor.getColumnIndex(CallFilterConstants.BLACK_REMOVE_STATE)))
                                    .append("\n");
                            stringBuilder.append(cursor.getInt(cursor.getColumnIndex(CallFilterConstants.BLACK_READ_STATE)))
                                    .append("\n");
                            stringBuilder.append(cursor.getInt(cursor.getColumnIndex(CallFilterConstants.BLACK_FIL_UP)))
                                    .append("\n");
                            stringBuilder.append(cursor.getString(cursor.getColumnIndex(CallFilterConstants.BLACK_PHONE_NUMBER)))
                                    .append("\n");
                        } while (cursor.moveToNext());
                    } else {
                        stringBuilder.append("null");
                    }
                    mDecryptMsgTv.setText(stringBuilder.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    IoUtils.closeSilently(cursor);
                }
                break;
        }
    }
}
