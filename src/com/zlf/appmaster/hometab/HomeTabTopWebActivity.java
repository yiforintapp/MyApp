package com.zlf.appmaster.hometab;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.zlf.appmaster.R;

/**
 * Created by Administrator on 2016/9/19.
 */
public class HomeTabTopWebActivity extends Activity {

    public static final String WEB_URL = "web_url";

    private WebView mWebView;

    private String mUrl;

    private ProgressBar mBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_video);
        init();
    }

    private void init() {
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setVisibility(View.VISIBLE);

        mBar =(ProgressBar) findViewById(R.id.progressBar);

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

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mBar.setVisibility(View.GONE);
                } else {
                    if (View.GONE == mBar.getVisibility()) {
                        mBar.setVisibility(View.VISIBLE);
                    }
                    mBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }

        });



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
