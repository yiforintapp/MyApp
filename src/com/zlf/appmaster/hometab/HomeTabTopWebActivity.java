package com.zlf.appmaster.hometab;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.zlf.appmaster.R;
import com.zlf.appmaster.home.BaseActivity;

/**
 * Created by Administrator on 2016/9/19.
 */
public class HomeTabTopWebActivity extends BaseActivity {

    public static final String WEB_URL = "web_url";

    private WebView mWebView;

    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_news_detail);
        init();
    }

    private void init() {
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setVisibility(View.VISIBLE);

        mUrl = getIntent().getStringExtra(WEB_URL);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setBuiltInZoomControls(false);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        // 回调本地代码
        webSettings.setJavaScriptEnabled(true);

        webSettings.setAllowContentAccess(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadsImagesAutomatically(true);

        webSettings.setLoadWithOverviewMode(true);



        mWebView.requestFocus();
        mWebView.requestFocusFromTouch();
        mWebView.setFocusable(true);
        mWebView.setFocusableInTouchMode(true);

        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setDrawingCacheEnabled(true);
        // 屏蔽长按事件
        mWebView.setOnCreateContextMenuListener(null);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setPluginsEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        mWebView.setWebChromeClient(new WebChromeClient());
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        mWebView.loadUrl(mUrl);
    }
}
