
package com.leo.appmaster.appsetting;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.home.ProtocolActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.utils.AppUtil;
import com.leoers.leoanalytics.LeoStat;

public class AboutActivity extends BaseActivity implements OnClickListener {

    private CommonTitleBar mTtileBar;
    private TextView mAppVersion;
    private Button mShowProtocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_about_layout);
        mTtileBar = (CommonTitleBar) findViewById(R.id.about_title_bar);
        mTtileBar.setTitle(R.string.app_setting_about);
        mTtileBar.openBackView();
        mShowProtocol = (Button) findViewById(R.id.check_update_button);
        mShowProtocol.setOnClickListener(this);
        
        View joinBeta = findViewById(R.id.join_beta);
        joinBeta.setOnClickListener(this);
        
        View likeUs = findViewById(R.id.like_us);
        likeUs.setOnClickListener(this);

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
        Intent intent = null;
        switch (view.getId()) {
            case R.id.check_update_button:
                intent = new Intent();
                intent.setClass(AboutActivity.this, ProtocolActivity.class);
                startActivity(intent);
                SDKWrapper.addEvent(this, LeoStat.P1, "about", "join");
                break;
            case R.id.join_beta:
                if (AppUtil.appInstalled(getApplicationContext(),
                        "com.google.android.apps.plus")) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri
                            .parse("https://plus.google.com/u/0/communities/112552044334117834440");
                    intent.setData(uri);
                    ComponentName cn = new ComponentName(
                            "com.google.android.apps.plus",
                            "com.google.android.libraries.social.gateway.GatewayActivity");
                    intent.setComponent(cn);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                    }
                } else {
                    Uri uri = Uri
                            .parse("https://plus.google.com/u/0/communities/112552044334117834440");
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
                SDKWrapper.addEvent(this, LeoStat.P1, "about", "like");
                break;
            case R.id.like_us:
                if (AppUtil.appInstalled(getApplicationContext(),
                        "com.facebook.katana")) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri
                            .parse("fb://page/1709302419294051");
                    intent.setData(uri);
                    ComponentName cn = new ComponentName("com.facebook.katana",
                            "com.facebook.katana.IntentUriHandler");
                    intent.setComponent(cn);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                    }
                } else {
                    intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri
                            .parse("https://www.facebook.com/pages/App-Master/1709302419294051");
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                SDKWrapper.addEvent(this, LeoStat.P1, "about", "privacy");
                break;              
            default:
                break;
        }
    }
}
