package com.zlf.appmaster.zhibo;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.fragment.BaseFragment;
import com.zlf.appmaster.home.BaseFragmentActivity;
import com.zlf.appmaster.ui.PagerSlidingTabStrip;
import com.zlf.appmaster.utils.Utilities;

import java.util.List;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;


public class VideoLiveActivity extends BaseFragmentActivity implements View.OnClickListener {

    private Context mContext;

    private static final String TAG = "MainActivity";
    private String path;
    private VideoView mVideoView;
    private ImageView mFullScreenBtn;
    private RelativeLayout mContactLayout;
    private boolean mIsFullScreen;
    private ViewPager mViewPager;
    private HomeTabHolder[] mHomeHolders = new HomeTabHolder[2];
    private PagerSlidingTabStrip mPagerSlidingTab;
    private ZhiBoChatFragment chatFragment;
    private ZhiBoDataFragment dataFragment;

    private WebView mWebView;

    private int mScreenHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        if (!LibsChecker.checkVitamioLibs(this)) {
            return;
        }
        setContentView(R.layout.activity_live_video);
        initViews();

        mContext = this;
    }

    private void initViews() {

        mScreenHeight = Utilities.getScreenSize(this)[1];
        mViewPager = (ViewPager) findViewById(R.id.vedio_viewpager);
        initFragment();
        initViewPager();
        mPagerSlidingTab = (PagerSlidingTabStrip) findViewById(R.id.zhibo_tab_tabs);
        mPagerSlidingTab.setBackgroundResource(R.color.white);
        mPagerSlidingTab.setShouldExpand(true);
        mPagerSlidingTab.setIndicatorColor(getResources().getColor(R.color.indicator_select_color));
        mPagerSlidingTab.setTextColor(R.color.black);
        mPagerSlidingTab.setTextSize(30);
        mPagerSlidingTab.setIndicatorHeight(6);
        mPagerSlidingTab.setDividerColor(getResources().getColor(R.color.white));


        mPagerSlidingTab.setViewPager(mViewPager);
        mVideoView = (VideoView) findViewById(R.id.vitamio_videoView);
        mFullScreenBtn = (ImageView) findViewById(R.id.fullScreen);
        mContactLayout = (RelativeLayout) findViewById(R.id.contact_layout);
        mContactLayout.setVisibility(View.VISIBLE);
        mFullScreenBtn.setEnabled(false);
        mFullScreenBtn.setOnClickListener(this);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.setIsFullScreen(false);
        mVideoView.setVisibility(View.VISIBLE);
        path = Constants.ZHI_BO_ADDRESS;
        mVideoView.setVideoURI(Uri.parse(path));
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setPlaybackSpeed(1.0f);
                mFullScreenBtn.setEnabled(true);
            }
        });
        mVideoView.start();
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mVideoView.isPlaying()) {
                    mVideoView.start();
                }
            }
        }, 1500);

//        mWebView = (WebView) findViewById(R.id.webView);
//        WebSettings webSettings = mWebView.getSettings();
//        webSettings.setDefaultTextEncodingName("UTF-8");
//        webSettings.setBuiltInZoomControls(false);
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        webSettings.setAllowContentAccess(true);
//        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
//        webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
//        webSettings.setAllowFileAccess(true);
//        webSettings.setLoadsImagesAutomatically(true);
//        webSettings.setLoadWithOverviewMode(true);
//        mWebView.requestFocus();
//        mWebView.requestFocusFromTouch();
//        mWebView.setFocusable(true);
//        mWebView.setFocusableInTouchMode(true);
//        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
//        mWebView.setVerticalScrollBarEnabled(false);
//        mWebView.setHorizontalScrollBarEnabled(false);
//        mWebView.setDrawingCacheEnabled(true);
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setPluginsEnabled(true);
//        webSettings.setPluginState(WebSettings.PluginState.ON);
//        mWebView.setWebChromeClient(new WebChromeClient());
//        webSettings.setUseWideViewPort(true);
//        mWebView.setVisibility(View.VISIBLE);
//        mWebView.loadUrl("http://vip.zlf1688.com");

    }

    private void initViewPager() {
        mViewPager.setAdapter(new ZhiBoAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(1); //预加载2个
        mViewPager.setCurrentItem(0);
    }

    private void initFragment() {
        HomeTabHolder holder = new HomeTabHolder();
        holder.title = this.getResources().getString(R.string.zhibo_chat);
        chatFragment = new ZhiBoChatFragment();
        holder.fragment = chatFragment;
        mHomeHolders[0] = holder;

        holder = new HomeTabHolder();
        holder.title = this.getResources().getString(R.string.zhibo_data);
        dataFragment = new ZhiBoDataFragment();
        holder.fragment = dataFragment;
        mHomeHolders[1] = holder;

        FragmentManager fm = getSupportFragmentManager();
        try {
            FragmentTransaction ft = fm.beginTransaction();
            List<Fragment> list = fm.getFragments();
            if (list != null) {
                for (Fragment f : fm.getFragments()) {
                    ft.remove(f);
                }
            }
            ft.commit();
        } catch (Exception e) {

        }

    }


    class ZhiBoAdapter extends FragmentPagerAdapter {
        public ZhiBoAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mHomeHolders[position].fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mHomeHolders[position].title;
        }

        @Override
        public int getCount() {
            return mHomeHolders.length;
        }
    }

    class HomeTabHolder {
        String title;
        BaseFragment fragment;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fullScreen:
                if (!mIsFullScreen) {//设置RelativeLayout的全屏模式
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);
                    mVideoView.setLayoutParams(layoutParams);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mContactLayout.setVisibility(View.GONE);
                    mVideoView.setIsFullScreen(true);
                    mIsFullScreen = true;//改变全屏/窗口的标记
                } else {
                    RelativeLayout.LayoutParams lp = new  RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.FILL_PARENT, mScreenHeight * 1 / 3);
                    mVideoView.setLayoutParams(lp);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mContactLayout.setVisibility(View.VISIBLE);
                    mVideoView.setIsFullScreen(false);
                    mIsFullScreen = false;
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("fullscreen", mIsFullScreen);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mIsFullScreen = savedInstanceState.getBoolean("fullscreen");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }

    @Override
    public void onBackPressed() {
        if(mIsFullScreen) {
            RelativeLayout.LayoutParams lp=new  RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.FILL_PARENT, mScreenHeight * 1 / 3);
            mVideoView.setLayoutParams(lp);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mContactLayout.setVisibility(View.VISIBLE);
            mIsFullScreen = false;
            mVideoView.setIsFullScreen(false);
            return;
        }
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
