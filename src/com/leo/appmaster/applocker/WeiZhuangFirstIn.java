
package com.leo.appmaster.applocker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;

public class WeiZhuangFirstIn extends BaseActivity implements OnClickListener {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weizhuang_firstin);
        init();
    }

    private void init() {
        mTextView = (TextView) findViewById(R.id.bt_go);
        mTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_go:
                Intent intent = new Intent(this, WeiZhuangActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.hold_in, R.anim.lock_mode_guide_out);
                break;
            default:
                break;
        }
    }

}
