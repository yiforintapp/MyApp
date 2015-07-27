
package com.leo.appmaster.sdk.push.ui;

import com.leo.appmaster.R;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
    private MyWebviewClient mWebviewClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_layout);

        Intent intent = getIntent();
        if (null == intent) {
            finish();
        }
        mURL = intent.getStringExtra(WEB_URL);

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

        mCloseView.setOnClickListener(this);
        mFlushView.setOnClickListener(this);
        disableNextBtn();
        disableBackBtn();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void intWebView() {
        mWebView.loadUrl(mURL);
        WebSettings settings = mWebView.getSettings();
        // support javaScript
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setCacheMode(WebSettings.LOAD_NORMAL);
        settings.setPluginState(PluginState.ON);

        mWebviewClient = new MyWebviewClient();
        mWebView.setDownloadListener(new MyWebViewDownLoadListener());
        mWebView.setWebViewClient(mWebviewClient);
        mWebView.setWebChromeClient(new WebChromeClient() {
            // set progress
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
        });
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
        Log.i(TAG,"disableNextBtn");
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
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
                Log.i("######", "back show");
            } else {
                disableBackBtn();
                Log.i("######", "back hide");
            }
            if (view.canGoForward()) {
                enableNextBtn();
                Log.i("######", "forward show");
            } else {
                disableNextBtn();
                Log.i("######", "forward hide");
            }
            super.onPageFinished(view, url);
        }
    }
    
    private class MyWebViewDownLoadListener implements DownloadListener{
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition,
                String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Log.i(TAG, "downlaod url: "+uri );
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        }
    }
}
