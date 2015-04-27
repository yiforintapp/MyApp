
package com.leo.appmaster.quickgestures;

import android.os.Bundle;
import android.widget.ListView;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;

public class QuickGestureActivity extends BaseActivity {
    private ListView mQuickGestureLV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_gesture);
        initUi();
    }

    private void initUi() {
        mQuickGestureLV=(ListView) findViewById(R.id.quick_gesture_lv);
    }

}
