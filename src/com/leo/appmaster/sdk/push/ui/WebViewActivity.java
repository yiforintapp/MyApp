
package com.leo.appmaster.sdk.push.ui;

import com.leo.appmaster.AppMasterConfig;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.Constants;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.LockSettingActivity;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.service.TaskDetectService;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WebViewActivity extends BaseActivity implements OnClickListener {

    private static final String TAG = "WebViewActivity";
    public static final String SPLASH_TO_WEBVIEW = "splash_to_webview";
    public static final String MSGCENTER_TO_WEBVIEW = "msgcenter_to_webview";
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
    private boolean mIsFromSplash;
    private boolean mIsFromMsgCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_layout);

        Intent intent = getIntent();
        mURL = intent.getStringExtra(WEB_URL);
        if (!Utilities.isEmpty(intent.getStringExtra(SPLASH_TO_WEBVIEW))) {
            if (SPLASH_TO_WEBVIEW.equals(intent.getStringExtra(SPLASH_TO_WEBVIEW))) {
                mIsFromSplash = true;
            }
        }
        mIsFromMsgCenter = intent.getBooleanExtra(MSGCENTER_TO_WEBVIEW, false);

        if (TextUtils.isEmpty(mURL)) {
            Log.i(TAG, "URL为空");
            finish();
        }
        Log.i(TAG, "URL = " + mURL);

        initUI();
        intWebView();
        SDKWrapper.addEvent(this, SDKWrapper.P1, "webview", " statusbar");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String newUrl = intent.getStringExtra(WEB_URL);
        if (TextUtils.isEmpty(newUrl)) {
            return;
        } else {
            mURL = newUrl;
            mWebView.loadUrl(mURL);
        }
        Log.i(TAG, "URL = " + mURL);
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
        mProgressBar.setProgress(5);

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
        settings.setJavaScriptEnabled(true); // support javaScript
        settings.setSupportZoom(true); // 是否可以缩放，默认true
        settings.setBuiltInZoomControls(true); // 是否显示缩放按钮，默认false
        /*
         * settings.setLoadWithOverviewMode(true);
         * settings.setUseWideViewPort(true); // 自适应屏幕
         */
        settings.setAppCacheEnabled(true); // 启动缓存
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
                startHome();
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
                startHome();
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
        try {
            // FIX: 2015/9/15 WebViewActivity has leaked window android.widget.ZoomButtonsController$Container
            ViewGroup viewGroup = (ViewGroup) getWindow().getDecorView();
            viewGroup.removeAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private class MyWebviewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getScheme().equals("market")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    // Google Play app is not installed, you may want to open
                    // the app store link
                    Uri uri = Uri.parse(url);
                    view.loadUrl("http://play.google.com/store/apps/" + uri.getHost() + "?"
                            + uri.getQuery());
                    return false;
                }
            } else {
                return false;
            }
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

    /* 进入主页 */
    private void startHome() {
        Log.i(TAG, "是否来自闪屏：" + mIsFromSplash);
        if (mIsFromSplash || !mIsFromMsgCenter) {
            AppMasterPreference amp = AppMasterPreference.getInstance(this);
            if (amp.getLockType() != AppMasterPreference.LOCK_TYPE_NONE) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (!mIsFromMsgCenter) {
                    intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                startActivity(intent);
                finish();
                TaskDetectService tds = TaskDetectService.getService();
                if(tds != null) {
                    tds.callPretendAppLaunch();
                }
            } else {
                if (AppMasterConfig.LOGGABLE) {
                    LeoLog.f(TAG, "startHome", Constants.LOCK_LOG);
                }
                Intent intent = new Intent(this, LockSettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }
    }
}
