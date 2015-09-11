package com.leo.appmaster.msgcenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseBrowserActivity;
import com.leo.appmaster.ui.CommonTitleBar;

public class MsgCenterBrowserActivity extends BaseBrowserActivity implements
        View.OnClickListener {
    private static final String KEY_URL = "key_url";
    private static final String KEY_TITLE = "key_title";

    private CommonTitleBar mTitleBar;

    private String mUrl;

    public static void startMsgCenterWeb(Context context, String title, String url) {
        Intent intent = new Intent(context, MsgCenterBrowserActivity.class);
        intent.putExtra(KEY_URL, url);
        intent.putExtra(KEY_TITLE, title);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mc_browser);

        mUrl = getIntent().getStringExtra(KEY_URL);
        if (TextUtils.isEmpty(mUrl)) {
            finish();
            return;
        }

        String title = getIntent().getStringExtra(KEY_TITLE);
        mTitleBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
        mTitleBar.setTitle(title);
        mTitleBar.setBackArrowVisibility(View.VISIBLE);
        mTitleBar.setOptionImage(R.drawable.ic_msg_center_refresh);
        mTitleBar.setBackViewListener(this);
        mTitleBar.setOptionListener(this);

        getWebView().loadUrl(mUrl);
    }

    @Override
    protected WebView getWebView() {
        return (WebView) findViewById(R.id.mc_browser_web);
    }

    @Override
    protected ProgressBar getLoadingView() {
        return (ProgressBar) findViewById(R.id.mc_progress);
    }

    @Override
    protected View getErrorView() {
        return findViewById(R.id.mc_error_ll);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_title_back:
                finish();
                break;
            case R.id.tv_option_image:
                mWebView.reload();
                break;
        }
    }
}
