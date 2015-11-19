
package com.leo.appmaster.lockertheme;

import android.os.Bundle;
import android.view.View;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.RippleView1;

public class LockerThemeGuideActivity extends BaseActivity {
    private RippleView1 mRvSure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker_theme_guide);
        mRvSure = (RippleView1) findViewById(R.id.rv_save);
        mRvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockerThemeGuideActivity.this.finish();
            }
        });
    }
}
