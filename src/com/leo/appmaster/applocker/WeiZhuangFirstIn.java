
package com.leo.appmaster.applocker;

import com.leo.appmaster.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class WeiZhuangFirstIn extends Activity implements OnClickListener {

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
                this.startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }

}
