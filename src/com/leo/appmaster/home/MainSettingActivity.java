package com.leo.appmaster.home;

import android.os.Bundle;

import com.leo.appmaster.R;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.sdk.BaseActivity;

/**
 * Created by chenfs on 16-3-28.
 */
public class MainSettingActivity extends BaseActivity {
    private PreferenceTable mPt;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_setting);
        initUI();
        mPt = PreferenceTable.getInstance();
    }

    private void initUI() {

    }
}
