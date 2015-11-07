
package com.leo.appmaster.applocker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.RippleView.OnRippleCompleteListener;

public class WeiZhuangFirstIn extends BaseActivity implements
        OnRippleCompleteListener {

    private TextView mTextView;
    private RippleView mRvGo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weizhuang_firstin);
        init();
    }

    private void init() {
        mTextView = (TextView) findViewById(R.id.bt_go);
        mRvGo = (RippleView) findViewById(R.id.rv_know_button);
        // mTextView.setOnClickListener(this);
        mRvGo.setOnRippleCompleteListener(this);
    }

    // @Override
    // public void onClick(View v) {
    // switch (v.getId()) {
    // case R.id.bt_go:
    // Intent intent = new Intent(this, WeiZhuangActivity.class);
    // startActivity(intent);
    // finish();
    // overridePendingTransition(R.anim.hold_in, R.anim.lock_mode_guide_out);
    // break;
    // default:
    // break;
    // }
    // }

    @Override
    public void onRippleComplete(RippleView rippleView) {
        switch (rippleView.getId()) {
            case R.id.rv_know_button:
                Intent intent = new Intent(this, WeiZhuangActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.hold_in, R.anim.lock_mode_guide_out);
                break;
            default:
                break;
        }
    }

//    @Override
//    public void onClick(View v) {
//        // TODO Auto-generated method stub
//        
//    }
}
