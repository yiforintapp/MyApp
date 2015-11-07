
package com.leo.appmaster.lockertheme;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.RippleView.OnRippleCompleteListener;

public class LockerThemeGuideActivity extends BaseActivity {
    private TextView mMakeSure;
    private RippleView mRvSure;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locker_theme_guide);
        mRvSure = (RippleView) findViewById(R.id.rv_save);
        mRvSure.setOnRippleCompleteListener(new OnRippleCompleteListener() {

            @Override
            public void onRippleComplete(RippleView arg0) {
                LockerThemeGuideActivity.this.finish();
            }
        });
    }
}
