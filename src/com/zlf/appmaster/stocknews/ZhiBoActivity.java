package com.zlf.appmaster.stocknews;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.zlf.appmaster.Constants;
import com.zlf.appmaster.R;
import com.zlf.appmaster.home.BaseActivity;

import java.io.IOException;
import java.lang.ref.WeakReference;


/**
 * Created by Administrator on 2016/9/7.
 */
public class ZhiBoActivity extends BaseActivity implements SurfaceHolder.Callback, View.OnClickListener {

    public static final String TAG = "ZhiBoActivity";

    public static final int DISAPPEAR_PAUSE_BUTTON = 0;

    private MediaPlayer mMediaPlayer;

    private SurfaceView mSurfaceView;

    private SurfaceHolder mSurfaceHolder;

    private int mVideoWidth;

    private int mVideoHeight;

    private ImageButton mPauseButton;

    private DataHandler mHandler;

    private boolean mPlaying;
    private boolean mOnCreat;

    private RelativeLayout mProgress;


    //用于处理消息的Handler
    private static class DataHandler extends Handler {
        WeakReference<ZhiBoActivity> mActivityReference;

        public DataHandler(ZhiBoActivity activity) {
            super();
            mActivityReference = new WeakReference<ZhiBoActivity>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ZhiBoActivity activity = mActivityReference.get();
            if (activity == null) {
                return;
            }

            switch (msg.what) {
                case DISAPPEAR_PAUSE_BUTTON:
                    if (activity.mMediaPlayer != null && activity.mMediaPlayer.isPlaying()) {
                        activity.mPauseButton.setVisibility(View.GONE);
                    }
                    break;
                default:
                    break;
            }


        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zhibo_layout);
        Log.e(TAG, "a");

        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mProgress = (RelativeLayout) findViewById(R.id.loading);
        mHandler = new DataHandler(this);
        mPauseButton = (ImageButton) findViewById(R.id.mediacontroller_play_pause);
        mPauseButton.setOnClickListener(this);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mOnCreat = true;
        Log.e(TAG, "b");
    }





    private void playVideo() throws IllegalArgumentException,
            IllegalStateException, IOException {

        if (mMediaPlayer == null) {
            mProgress.setVisibility(View.GONE);
            mMediaPlayer = new MediaPlayer();
            String path3 = Constants.ZHI_BO_ADDRESS;
            mMediaPlayer.setDataSource(path3);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mPlaying = true;
            mHandler.sendMessageDelayed(mHandler.obtainMessage(DISAPPEAR_PAUSE_BUTTON), 2000);
        } else {
            mMediaPlayer.setDisplay(mSurfaceHolder);
        }

//this.mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Log.e(TAG, "c");
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    public void surfaceCreated(SurfaceHolder arg0) {

//        FrameLayout.LayoutParams params  =
//                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//
//        mSurfaceView.setLayoutParams(params);
        mSurfaceHolder = arg0;
        try {
            playVideo();
        } catch (Exception e) {
            Log.e(TAG, ">>>error", e);
        }
        Log.e(TAG, "surface created");
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
        if (mSurfaceHolder != null) {
            mSurfaceHolder = null;
        }
        release();
    }

    public void onPrepared(MediaPlayer arg0) {
        // TODO Auto-generated method stub

        mVideoWidth = mMediaPlayer.getVideoWidth();
        mVideoHeight = mMediaPlayer.getVideoHeight();

        if (mVideoHeight != 0 && mVideoWidth != 0) {
            mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
            mMediaPlayer.start();
        }

    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mPauseButton.setImageResource(R.drawable.mediacontroller_play);
            mMediaPlayer.pause();
        }
        mOnCreat = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        if (mPlaying) {
            mPauseButton.setImageResource(R.drawable.mediacontroller_pause);
            mMediaPlayer.start();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(DISAPPEAR_PAUSE_BUTTON), 4000);
        } else {
            if (!mOnCreat) {
                mPauseButton.setImageResource(R.drawable.mediacontroller_play);
                mMediaPlayer.pause();
            }

        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop");
    }

    public void release() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart");
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        release();
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mPauseButton.getVisibility() == View.GONE) {
            mPauseButton.setVisibility(View.VISIBLE);
        }
        mHandler.sendMessageDelayed(mHandler.obtainMessage(DISAPPEAR_PAUSE_BUTTON), 2000);
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mediacontroller_play_pause:
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mPlaying = false;
                    mPauseButton.setImageResource(R.drawable.mediacontroller_play);
                } else if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
                    mPauseButton.setImageResource(R.drawable.mediacontroller_pause);
                    mMediaPlayer.start();
                    mPlaying = true;
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(DISAPPEAR_PAUSE_BUTTON), 2000);
                }
                break;
            default:
                break;
        }
    }

}

