package com.zlf.appmaster.hometab;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.ThreadManager;
import com.zlf.appmaster.home.BaseActivity;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;


public class LiveViewActivity extends BaseActivity {

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
        setContentView(R.layout.activity_view_live);
        initViews();

        mContext = this;
    }

    private void initViews() {

        mVideoView = (VideoView) findViewById(R.id.vitamio_videoView);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.setVisibility(View.VISIBLE);
        path = Constants.ZHI_BO_ADDRESS;
        mVideoView.setVideoURI(Uri.parse(path));
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setPlaybackSpeed(1.0f);
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
