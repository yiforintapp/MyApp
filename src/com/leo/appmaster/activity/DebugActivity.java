package com.leo.appmaster.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.cloud.crypto.CryptoUtils;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.dialog.LEOMessageDialog;

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.decrypt_btn:
                String text = mDecryptTv.getEditableText().toString();
                if (TextUtils.isEmpty(text)) {
                    break;
                }
//                LEOMessageDialog dlg = new LEOMessageDialog(this);
                text = CryptoUtils.decrypt(text);
                mDecryptMsgTv.setText(text);
//                dlg.setContent(text);
//                dlg.show();
                break;
        }
    }
}
