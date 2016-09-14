package com.zlf.appmaster.stocknews;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.home.BaseActivity;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.widget.VideoView;


public class testWebViewActivity extends BaseActivity {

    private Context mContext;

    private static final String TAG = "MainActivity";
    private String path;
    private VideoView mVideoView;

    private WebView mWebView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if (!LibsChecker.checkVitamioLibs(this)) {
            return;
        }
        setContentView(R.layout.test_activity_news_detail);
        initViews();

        mContext = this;
    }

    private void initViews() {

        mVideoView = (VideoView) findViewById(R.id.vitamio_videoView);
//        mVideoView.setMediaController(new MediaController(this));
        path = Constants.ZHI_BO_ADDRESS;
//        mVideoView.setVideoURI(Uri.parse(path));
//        mVideoView.requestFocus();
//        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                mediaPlayer.setPlaybackSpeed(1.0f);
//            }
//        });
//        mVideoView.start();
//        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (!mVideoView.isPlaying()) {
//                    mVideoView.start();
//                }
//            }
//        }, 1500);

        mWebView = (WebView) findViewById(R.id.webView);
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
        mWebView.loadUrl("http://vip.zlf1688.com");

    }

    @Override
    public void onBackPressed() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mVideoView != null) {
                    ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mVideoView.start();
                        }
            }, 1500);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    /**
     * 主要是把webview所持用的资源销毁，
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
