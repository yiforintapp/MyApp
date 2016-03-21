
package com.leo.appmaster.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.db.PreferenceTable;
import com.leo.appmaster.feedback.FeedbackActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.ui.FiveStarsLayout;
import com.leo.appmaster.utils.AppUtil;
import com.leo.appmaster.utils.PrefConst;

public class GradeTipActivity extends BaseActivity implements OnClickListener {

    private TextView mTvMakeSure;
    private TextView mFeedbackSure;
    private View mTvMakeSureClick;
    private View mFeedbackSureClick;
    private FiveStarsLayout mLayout;

    private String mContent;
    private String mUrl;
    private TextView mText;
    private PreferenceTable mPt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_show_googleplay_tip);
        FiveStarsLayout fivestars = (FiveStarsLayout) findViewById(R.id.fsl_fivestars);
        mTvMakeSure = (TextView) findViewById(R.id.tv_make);
        mFeedbackSure = (TextView) findViewById(R.id.tv_feedback);

        mTvMakeSureClick = findViewById(R.id.tv_make_click);
        mFeedbackSureClick = findViewById(R.id.tv_feedback_click);
        mLayout = (FiveStarsLayout) findViewById(R.id.fsl_fivestars);
        mText = (TextView) findViewById(R.id.textView3);

        mLayout.setOnClickListener(this);
        mTvMakeSureClick.setOnClickListener(this);
        mFeedbackSureClick.setOnClickListener(this);

        mPt = PreferenceTable.getInstance();
        mContent = getIntent().getStringExtra("content");
        mUrl = getIntent().getStringExtra("url");
        if (!TextUtils.isEmpty(mContent)) {
            mText.setText(mContent);
        }
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
                mLockManager.filterAll(1000);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri
                        .parse(Constants.RATING_ADDRESS_MARKET);
                intent.setData(uri);
                // ComponentName cn = new ComponentName("com.android.vending",
                // "com.google.android.finsky.activities.MainActivity");
                // intent.setComponent(cn);
                intent.setPackage("com.android.vending");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                mLockManager.filterSelfOneMinites();
//                Intent intent2 = new Intent(this, GooglePlayGuideActivity.class);
//                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent2);
                showGP = true;
            } catch (Exception e) {

            }
        }
        if (!showGP) {
            String url = Constants.RATING_ADDRESS_BROWSER;
            if (!TextUtils.isEmpty(mUrl)) {
                url = mUrl;
            }
            mLockManager.filterSelfOneMinites();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(url);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mTvMakeSureClick) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "home_dlg_rank_comfirm");
            if (!TextUtils.isEmpty(mContent)) {
                mPt.putBoolean(PrefConst.KEY_HAS_GRADE, true);
            }
            openShowGoogleGuide();
            finish();
        } else if (v == mFeedbackSureClick) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "home_dlg_rank_later");
            Intent intent = new Intent(GradeTipActivity.this, FeedbackActivity.class);
            startActivity(intent);
            finish();
        } else if (v == mLayout) {
            SDKWrapper.addEvent(this, SDKWrapper.P1, "home", "home_dlg_rank_comfirm");
            if (!TextUtils.isEmpty(mContent)) {
                mPt.putBoolean(PrefConst.KEY_HAS_GRADE, true);
            }
            openShowGoogleGuide();
            finish();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLayout != null){
            mLayout.stopAnim();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mLayout != null) {
            mLayout.startAnim();
        }
    }
}
