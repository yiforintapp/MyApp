
package com.leo.appmaster.sdk.push.ui;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WebViewActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = "WebViewActivity";

    private WebView mWebView;
    private TextView mTitleView;
    private ImageView mCloseView, mBackView, mNextView, mFlushView;
    private ProgressBar mProgressBar;
    public static final String WEB_URL = "url";
    private String mURL;

    private FrameLayout mVideoFullLayout;
    private MyWebviewClient mWebviewClient;
    private MyWebChromeClient myWebChromeClient;
    private View mPlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_layout);

        Intent intent = getIntent();
        mURL = intent.getStringExtra(WEB_URL);
        if (TextUtils.isEmpty(mURL)) {
            finish();
        }
        initUI();
        intWebView();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "webview", " statusbar");
    }

    private void initUI() {
        mWebView = (WebView) findViewById(R.id.webView);
        mTitleView = (TextView) findViewById(R.id.webView_title_tv);
        mCloseView = (ImageView) findViewById(R.id.webView_close_iv);
        mBackView = (ImageView) findViewById(R.id.back_iv);
        mNextView = (ImageView) findViewById(R.id.next_iv);
        mFlushView = (ImageView) findViewById(R.id.flush_iv);
        mProgressBar = (ProgressBar) findViewById(R.id.webView_pb);
        mProgressBar.setMax(100);

        mVideoFullLayout = (FrameLayout) findViewById(R.id.video_fullView);
        mCloseView.setOnClickListener(this);
        mFlushView.setOnClickListener(this);
        disableNextBtn();
        disableBackBtn();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void intWebView() {
        mWebView.loadUrl(mURL);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);// 自适应屏幕
        settings.setLoadWithOverviewMode(true);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebviewClient = new MyWebviewClient();
        mWebView.setDownloadListener(new MyWebViewDownLoadListener());
        mWebView.setWebViewClient(mWebviewClient);
        myWebChromeClient = new MyWebChromeClient();
        mWebView.setWebChromeClient(myWebChromeClient);
    }

    @Override
    public void onClick(View arg0) {
        int id = arg0.getId();
        switch (id) {
            case R.id.back_iv:
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                }
                break;
            case R.id.next_iv:
                if (mWebView.canGoForward()) {
                    mWebView.goForward();
                }
                break;
            case R.id.flush_iv:
                mWebView.reload();
                break;
            case R.id.webView_close_iv:
                finish();
            default:
                break;
        }
    }

    private void enableBackBtn() {
        mBackView.setImageResource(R.drawable.webview_back_selecter);
        mBackView.setOnClickListener(this);
    }

    private void disableBackBtn() {
        mBackView.setImageResource(R.drawable.webview_back_icon_disable);
        mBackView.setOnClickListener(null);
    }

    private void enableNextBtn() {
        mNextView.setImageResource(R.drawable.webview_next_selecter);
        mNextView.setOnClickListener(this);
    }

    private void disableNextBtn() {
        mNextView.setImageResource(R.drawable.next_icon_disable);
        mNextView.setOnClickListener(null);
        Log.i(TAG, "disableNextBtn");
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "mPlayView  = " + mPlayView);
        if (mPlayView != null) {
            Log.i(TAG, "onBackPressed  onHideCustomView");
            hideCustomView();
        } else {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        mWebView.resumeTimers();

        Log.i(TAG, "onResume  ");

        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause  ");
        mWebView.onPause();
        mWebView.pauseTimers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy  ");

        mVideoFullLayout.removeAllViews();
        mWebView.loadUrl("about:blank");
        mWebView.stopLoading();
        mWebView.setWebChromeClient(null);
        mWebView.setWebViewClient(null);
        mWebView.destroy();
        mWebView = null;
    }

    /**
     * 全屏时按返加键执行退出全屏方法
     */
    public void hideCustomView() {
        myWebChromeClient.onHideCustomView();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        int type = this.getResources().getConfiguration().orientation;
        if (type == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i(TAG, "切换到了横屏");
        } else if (type == Configuration.ORIENTATION_PORTRAIT) {
            Log.i(TAG, "切换到了竖屏");
        }
        super.onConfigurationChanged(newConfig);
    }

    private class MyWebviewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (view.canGoBack()) {
                enableBackBtn();
                Log.i(TAG, "back show");
            } else {
                disableBackBtn();
                Log.i(TAG, "back hide");
            }
            if (view.canGoForward()) {
                enableNextBtn();
                Log.i(TAG, "forward show");
            } else {
                disableNextBtn();
                Log.i(TAG, "forward hide");
            }
            super.onPageFinished(view, url);
        }
    }

    private class MyWebChromeClient extends WebChromeClient {

        CustomViewCallback customViewCallback;

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                mProgressBar.setProgress(newProgress);
                mProgressBar.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                    }
                }, 100);
            } else {
                if (mProgressBar.getVisibility() == View.GONE) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                mProgressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            mTitleView.setText(title);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            Log.i(TAG, "onShowCustomView");
            // 设置为横屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mPlayView = view;
            customViewCallback = callback;

            mWebView.setVisibility(View.INVISIBLE);
            mVideoFullLayout.addView(view);
            mVideoFullLayout.setVisibility(View.VISIBLE);

            super.onShowCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            Log.i(TAG, "onHideCustomView");
            // 用户当前的首选方向
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            if (customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
            }
            mVideoFullLayout.removeView(mPlayView);
            mPlayView = null;
            mVideoFullLayout.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);

            super.onHideCustomView();
        }
    }

    private class MyWebViewDownLoadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition,
                String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Log.i(TAG, "downlaod url: " + uri);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
}
