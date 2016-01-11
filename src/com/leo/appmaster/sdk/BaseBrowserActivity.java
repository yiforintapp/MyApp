package com.leo.appmaster.sdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.SystemClock;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.leo.appmaster.home.MenuFaqBrowserActivity;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.NetWorkUtil;

/**
 * Created by zany on 2015/4/23.
 */
public abstract class BaseBrowserActivity extends BaseActivity {
    private static final String TAG = "BaseBrowserActivity";

    public static final int FLAG_HARDWARE_ACCELERATED = 16777216;
    protected static final String MX2 = "Meizu_M040";

    // 解决部分浏览器内核连续调用onPageStarted，导致页面显示异常的问题
    private static final long INTERVAL = 1000;

    private static final int ERR_404 = -2;

    protected WebViewClientImpl mWebViewClient;
    protected WebChromClientImpl mWebChromeClient;

    protected WebView mWebView;
    protected ProgressBar mLoadingView;
    protected View mErrorView;
    protected boolean mReceivedError;
    private long mLastPageStartedTs;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mWebChromeClient = null;
        mWebViewClient = null;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        mWebViewClient = new WebViewClientImpl();
        mWebChromeClient = new WebChromClientImpl();

        mWebView = getWebView();
        mLoadingView = getLoadingView();
        mErrorView = getErrorView();

        if (mWebView == null) {
            throw new RuntimeException("webview is null.");
        }

        String mark = Build.MANUFACTURER + "_" + Build.MODEL;
        try {
            if (Build.VERSION.SDK_INT >= 11 && !MX2.equals(mark)) {
                //开启硬件加速
                getWindow().addFlags(FLAG_HARDWARE_ACCELERATED);
            }
        } catch (Exception e) {
            LeoLog.e(TAG, "FLAG_HARDWARE_ACCELERATED>>>", e);
        }

        initWebView();
    }

    private void initWebView() {
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

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                LeoLog.d(TAG, "onDownloadStart, url: " + url);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    LeoLog.e(TAG, "startActivity fail, e = " + e.getMessage());
                }
            }
        });

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        settings.setAllowFileAccess(true);
        settings.setLoadsImagesAutomatically(true);

        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);

        // 开启本地存储
        settings.setDatabaseEnabled(true);
        String dbDir = this.getApplicationContext().getDir("storage", Context.MODE_WORLD_WRITEABLE)
                .getPath();
        settings.setDatabasePath(dbDir);
        // 开启应用程序缓存
        settings.setAppCacheEnabled(true);
        String cacheDir = this.getApplicationContext().getDir("cache", Context.MODE_WORLD_WRITEABLE)
                .getPath();
        settings.setAppCachePath(cacheDir);
        settings.setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT >= 14) {
            mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
        }

        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);

    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    protected abstract WebView getWebView();

    protected abstract ProgressBar getLoadingView();

    protected abstract View getErrorView();

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        LeoLog.d(TAG, "onPageStarted, url: " + url);
        long currentTs = SystemClock.elapsedRealtime();
        if (currentTs - mLastPageStartedTs > INTERVAL) {
            // FIXME: 2015/9/21 AM-2474 浏览器内核连续回调2次，导致mReceivedError被重置为false
            mReceivedError = false;
        }
        mLastPageStartedTs = currentTs;
    }

    public void onPageFinished(WebView view, String url) {
        LeoLog.d(TAG, "onPageFinished, url: " + url);
        if (mReceivedError) {
            mErrorView.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
        } else {
            mWebView.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);
        }
        mLoadingView.setVisibility(View.GONE);
    }

    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        LeoLog.d(TAG, "--->onReceivedError, errorCode: " + errorCode + " | des: " + description);

        if (!NetWorkUtil.isNetworkAvailable(this) || errorCode == ERR_404) {
            //-2, 找不到网页
        }

        mReceivedError = true;
        mWebView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);

    }

    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        LeoLog.d(TAG, "--->onReceivedSslError");
        handler.cancel();
    }

    public void onProgressChanged(WebView view, int newProgress) {
        LeoLog.d(TAG, "--->onProgressChanged, progress: " + newProgress);
        if (newProgress < 100) {
            if (newProgress < 5) {
                newProgress = 5;
            }
            mLoadingView.setProgress(newProgress);
            mLoadingView.setVisibility(View.VISIBLE);
        } else {

            if (!NetWorkUtil.isNetworkAvailable(this) || mReceivedError) {
                //-2, 找不到网页
                mLoadingView.setVisibility(View.GONE);
                mWebView.setVisibility(View.GONE);
                mErrorView.setVisibility(View.VISIBLE);
            } else {
                mLoadingView.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onReceivedTitle(WebView view, String title) {

    }

    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//        final CustomSplitDialog dialog = DialogUtil.createCustomSplitDialog(this, message,
//                null, R.string.ok, R.string.update_no, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        }, null);
//        dialog.show();
//        result.cancel();
        return false;
    }

    protected WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return null;
    }

    protected class WebChromClientImpl extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            BaseBrowserActivity.this.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            LeoLog.d(TAG, "--->onReceivedTitle");
            super.onReceivedTitle(view, title);
            BaseBrowserActivity.this.onReceivedTitle(view, title);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            LeoLog.d(TAG, "--->onJsAlert");
            return BaseBrowserActivity.this.onJsAlert(view, url, message, result);
        }

    }

    protected class WebViewClientImpl extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return BaseBrowserActivity.this.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            WebResourceResponse result = BaseBrowserActivity.this.shouldInterceptRequest(view, url);
            if (result == null) {
                return super.shouldInterceptRequest(view, url);
            }

            return result;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            BaseBrowserActivity.this.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            BaseBrowserActivity.this.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            BaseBrowserActivity.this.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            BaseBrowserActivity.this.onReceivedSslError(view, handler, error);
        }
    }
}
