package com.zlf.appmaster.hometab;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.zlf.appmaster.R;

/**
 * Created by Administrator on 2016/9/19.
 */
public class HomeTabTopWebActivity extends Activity {

    public static final String WEB_URL = "web_url";

    private WebView mWebView;
    private View mEmptyView;

    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_live);
        init();
    }

    private void init() {
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setVisibility(View.VISIBLE);
        mEmptyView = (View) findViewById(R.id.empty_view);
        mEmptyView.setVisibility(View.GONE);

        mUrl = getIntent().getStringExtra(WEB_URL);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setGeolocationEnabled(true);
        mWebView.requestFocus();
        mWebView.requestFocusFromTouch();
        mWebView.setFocusable(true);
        mWebView.setFocusableInTouchMode(true);
        mWebView.setScrollBarStyle(0);

        mWebView.loadUrl(mUrl);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.clearCache(true);
            mWebView.clearHistory();
        }
    }
}
