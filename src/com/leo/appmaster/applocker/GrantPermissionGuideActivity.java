package com.leo.appmaster.applocker;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;

public class GrantPermissionGuideActivity extends BaseActivity {
    private RelativeLayout mRlRoot;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grant_permission_guide);
        initUI();
    }
    
    private void initUI() {
        mRlRoot = (RelativeLayout) findViewById(R.id.rl_root);
        mRlRoot.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                GrantPermissionGuideActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(DEFAULT_KEYS_DISABLE, DEFAULT_KEYS_DISABLE);
    }
}
