package com.leo.appmaster.cleanmemory;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.RocketDock;
import com.leo.appmaster.utils.ProcessUtils;
import com.leo.appmaster.utils.TextFormater;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.TextView;

public class CleanMemActivity extends Activity implements OnClickListener,
		OnTouchListener {
	private CommonTitleBar mTtileBar;
	private ImageButton mRocket;
	private RocketDock mRocketDock;

	private TextView TvMemory;

	private long mLastUsedMem;
	private long mTotalMem;
	private Vibrator mVibrator;
	public boolean mVibrate;

	private ProcessCleaner mCleaner;
	private Handler mHandler;

	private Animation mRocketAnima;

	private float mTouchDownX, mTouchDownY;

	private final float mThreshold = 200;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clean_mem);

		initUI();
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mHandler = new EventHandler(this);
		createRocketAnima();
		startTranslate();
	}

	private void initUI() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.openBackView();
		mRocket = (ImageButton) findViewById(R.id.rocket_icon);
		mRocket.setOnTouchListener(this);
		mRocketDock = (RocketDock) findViewById(R.id.rocket_dock);
		TvMemory = (TextView) findViewById(R.id.tv_memory);
		updateMem();
	}

	private void updateMem() {
		mTotalMem = ProcessUtils.getTotalMem();
		mLastUsedMem = mTotalMem - ProcessUtils.getAvailableMem(this);

		TvMemory.setText(TextFormater.dataSizeFormat(mLastUsedMem) + "/"
				+ TextFormater.dataSizeFormat(mTotalMem));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		default:
			break;
		}

	}

	private void startTranslate() {
		mRocket.startAnimation(mRocketAnima);
	}

	private void stopTranslate() {
		mRocket.clearAnimation();
		mRocketAnima.cancel();
	}

	private void createRocketAnima() {
		AnimationSet as = new AnimationSet(true);
		TranslateAnimation ta = new TranslateAnimation(0, 0, 0, 20);
		ScaleAnimation sa = new ScaleAnimation(1f, 1f, 1f, 0.9f);
		as.addAnimation(sa);
		as.addAnimation(ta);
		as.setDuration(1000);

		ta.setRepeatCount(Animation.INFINITE);
		ta.setRepeatMode(Animation.REVERSE);
		sa.setRepeatCount(Animation.INFINITE);
		sa.setRepeatMode(Animation.REVERSE);

		mRocketAnima = as;
	}

	private void cleanMemory() {
		launchRocket();
		if (mCleaner == null) {
			mCleaner = new ProcessCleaner(
					(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
		}

		mCleaner.cleanAllProcess();
		updateMem();
	}

	public void launchRocket() {
		Animation ta = createRocketFly();
		mRocket.setImageResource(R.drawable.rocket_fly);
		mRocket.startAnimation(ta);
	}

	private Animation createRocketFly() {
		TranslateAnimation ta = new TranslateAnimation(0, 0, 0, -2000);
		ta.setDuration(1000);
		ta.setFillEnabled(true);
		ta.setFillBefore(true);
		ta.setInterpolator(new AccelerateInterpolator());
		return ta;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			mTouchDownX = event.getX();
			mTouchDownY = event.getY();
			mVibrate = true;
			stopTranslate();
			prepareLaunch();
		} else if (action == MotionEvent.ACTION_UP) {

			float x = event.getX();
			float y = event.getY();

			if (Math.abs(mTouchDownX - x) > mThreshold
					|| Math.abs(mTouchDownY - y) > mThreshold) {
				cancelLaunch();
			} else {
				stopVibrate();
				cleanMemory();
			}
		}
		return false;
	}

	private void prepareLaunch() {
		new Thread(new VibrateTask()).start();
	}

	private void cancelLaunch() {
		startTranslate();
		stopVibrate();
	}

	private void stopVibrate() {
		mVibrate = false;
		mVibrator.cancel();
	}

	private class VibrateTask implements Runnable {

		@Override
		public void run() {
			while (mVibrate) {
				mVibrator.vibrate(200);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private static class EventHandler extends Handler {

		CleanMemActivity mActivity;

		public EventHandler(CleanMemActivity activity) {
			super();
			this.mActivity = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}

	}

}
