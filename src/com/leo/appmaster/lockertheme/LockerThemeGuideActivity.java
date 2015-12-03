
package com.leo.appmaster.lockertheme;

import android.os.Bundle;
import android.view.View;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.RippleView;

public class LockerThemeGuideActivity extends BaseActivity {
    private RippleView mRvSure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker_theme_guide);
        mRvSure = (RippleView) findViewById(R.id.rv_save);
        mRvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LockerThemeGuideActivity.this.finish();
            }
        });
    }
}
