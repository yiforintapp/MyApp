package com.leo.appmaster.wifiSecurity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.leo.appmaster.R;
import com.leo.appmaster.db.LeoPreference;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;

/**
 * Created by qili on 15-10-22.
 */
public class WifiSettingActivity extends BaseActivity implements View.OnClickListener {
    public final static String IS_SHOW_WIFI_SAFE = "show_wifi_safe";
    private CommonToolbar mTitleBar;
    private ImageView checkBox;
    private boolean isSelected;
    private RippleView rpBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_setting);

        initUI();
        fillData();
    }

    private void fillData() {
        isSelected = LeoPreference.getInstance().getBoolean(IS_SHOW_WIFI_SAFE, true);
        if (isSelected) {
            checkBox.setImageResource(R.drawable.switch_on);
        } else {
            checkBox.setImageResource(R.drawable.switch_off);
        }
    }

    private void initUI() {
        mTitleBar = (CommonToolbar) findViewById(R.id.wifi_setting_title_bar);
        mTitleBar.setToolbarTitle(R.string.wifi_titlebar_name);
        mTitleBar.setToolbarColorResource(R.color.cb);
        mTitleBar.setOptionMenuVisible(false);

        rpBtn = (RippleView) findViewById(R.id.content_item_all);
        rpBtn.setOnClickListener(this);
//        rpBtn.setOnRippleCompleteListener(this);

        checkBox = (ImageView) findViewById(R.id.lock_app_check);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.content_item_all:
                changeCheckBoxState();
                break;
        }
    }

    private void changeCheckBoxState() {
        boolean isShow = LeoPreference.getInstance().getBoolean(IS_SHOW_WIFI_SAFE, true);
        if (isShow) {
            LeoPreference.getInstance().putBoolean(IS_SHOW_WIFI_SAFE, false);
            checkBox.setImageResource(R.drawable.switch_off);
            SDKWrapper.addEvent(this,
                    SDKWrapper.P1, "wifi_scan", "wifi_autoscan_close");
        } else {
            LeoPreference.getInstance().putBoolean(IS_SHOW_WIFI_SAFE, true);
            checkBox.setImageResource(R.drawable.switch_on);
            SDKWrapper.addEvent(this,
                    SDKWrapper.P1, "wifi_scan", "wifi_autoscan_open");
        }
    }

}
