
package com.leo.appmaster.activity;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.home.ProtocolActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonToolbar;
import com.leo.appmaster.ui.RippleView;

public class AboutActivity extends BaseActivity implements OnClickListener {

    private CommonToolbar mTtileBar;
    private TextView mAppVersion;
    private RippleView mShowProtocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_about_layout);
        mTtileBar = (CommonToolbar) findViewById(R.id.about_title_bar);
        mTtileBar.setToolbarTitle(R.string.app_setting_about);
        mTtileBar.setToolbarColorResource(R.color.cb);
        mShowProtocol = (RippleView) findViewById(R.id.rv_check_update_button);
        mShowProtocol.setOnClickListener(this);

        mAppVersion = (TextView) findViewById(R.id.app_version);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            mAppVersion.setText("V" + versionName);
        } catch (NameNotFoundException e) {
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rv_check_update_button:
                Intent intent = new Intent();
                intent.setClass(AboutActivity.this, ProtocolActivity.class);
                startActivity(intent);
                SDKWrapper.addEvent(AboutActivity.this, SDKWrapper.P1, "about", "join");
                break;
            default:
                break;
        }
    }
}
