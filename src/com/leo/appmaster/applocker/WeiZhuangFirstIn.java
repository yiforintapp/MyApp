
package com.leo.appmaster.applocker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.RippleView1;


public class WeiZhuangFirstIn extends BaseActivity implements View.OnClickListener {

    private RippleView1 mRvGo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weizhuang_firstin);
        init();
    }

    private void init() {
        mRvGo = (RippleView1) findViewById(R.id.rv_know_button);
        mRvGo.setOnClickListener(this);
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
