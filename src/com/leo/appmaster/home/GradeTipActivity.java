
package com.leo.appmaster.home;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.utils.AppUtil;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

public class GradeTipActivity extends BaseActivity implements OnClickListener {

    private TextView mTvMakeSure;
    private TextView mFeedbackSure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_show_googleplay_tip);
        mTvMakeSure = (TextView) findViewById(R.id.tv_make);
        mFeedbackSure = (TextView) findViewById(R.id.tv_feedback);
        mTvMakeSure.setOnClickListener(this);
        mFeedbackSure.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        AppMasterPreference.getInstance(this).setGoogleTipShowed(true);
        super.onResume();
    }

    private void openShowGoogleGuide() {
        boolean showGP = false;
        if (AppUtil.appInstalled(getApplicationContext(), "com.android.vending")) {
            try {
                LockManager.getInstatnce().filterAllOneTime(1000);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri
                        .parse("market://details?id=com.leo.appmaster&referrer=utm_source=AppMaster");
                intent.setData(uri);
                ComponentName cn = new ComponentName("com.android.vending",
                        "com.google.android.finsky.activities.MainActivity");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                LockManager.getInstatnce().timeFilterSelf();
                Intent intent2 = new Intent(this, GooglePlayGuideActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                showGP = true;
            } catch (Exception e) {

            }
        }
        if (!showGP) {
            LockManager.getInstatnce().timeFilterSelf();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri
                    .parse("https://play.google.com/store/apps/details?id=com.leo.appmaster&referrer=utm_source=AppMaster");
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mTvMakeSure) {
            openShowGoogleGuide();
            finish();
        } else if (v == mFeedbackSure) {
            Intent intent = new Intent(GradeTipActivity.this, FeedbackActivity.class);
            startActivity(intent);
            finish();
        }

    }
}
