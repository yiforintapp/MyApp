package com.leo.appmaster.applocker;

import com.leo.appmaster.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class UnknowCallActivity extends Activity {
    private View show_text_setting;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unknowcall);
        init();
    }

    private void init() {
        show_text_setting = findViewById(R.id.show_text_setting);
        show_text_setting.setVisibility(View.VISIBLE);
    }
}
