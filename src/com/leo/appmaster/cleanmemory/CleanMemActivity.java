package com.leo.appmaster.cleanmemory;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.RocketDock;
import com.leo.appmaster.utils.ProcessUtils;
import com.leo.appmaster.utils.TextFormater;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

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
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class CleanMemActivity extends Activity implements OnClickListener,
		OnTouchListener {
	public static final int MSG_UPDATE_MEM = 0;
	private CommonTitleBar mTtileBar;
	private ImageButton mRocket;
	private RocketDock mRocketDock;

	private TextView mTvMemory;

	private long mLastUsedMem;
	private long mTotalMem;

	private long mCleanMem;

	private Vibrator mVibrator;
	public boolean mVibrating;
	private boolean mTranslating;
	private boolean mUpdating;

	private ProcessCleaner mCleaner;
	private Handler mHandler;

	private Animation mRocketAnima;

	private float mTouchDownX, mTouchDownY;

	private final float mThreshold = 250;

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
		mTvMemory = (TextView) findViewById(R.id.tv_memory);

		mTotalMem = ProcessUtils.getTotalMem();
		mLastUsedMem = mTotalMem - ProcessUtils.getAvailableMem(this);

		mTvMemory.setText(TextFormater.dataSizeFormat(mLastUsedMem) + "/"
				+ TextFormater.dataSizeFormat(mTotalMem));
	}

	private void updateMem() {
		mTvMemory.setText(TextFormater.dataSizeFormat(mLastUsedMem) + "/"
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
		mRocket.setImageResource(R.drawable.rocket_stop);
	}

	private void stopTranslate() {
		if (mTranslating) {
			mTranslating = false;
			mRocket.clearAnimation();
			mRocketAnima.cancel();
			mRocket.setImageResource(R.drawable.rocket_fly);
		}
	}

	private void createRocketAnima() {
		AnimationSet as = new AnimationSet(true);
		TranslateAnimation ta = new TranslateAnimation(0, 0, 0, 50);
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

		long curUsedMem = mTotalMem - ProcessUtils.getAvailableMem(this);

		mCleanMem = Math.abs(mLastUsedMem - curUsedMem);

		startUpdataMemTip(curUsedMem);

	}

	private void startUpdataMemTip(long targetMem) {
		if (!mUpdating) {
			mUpdating = true;
			ValueAnimator update = ValueAnimator.ofInt((int) mLastUsedMem, 0);
			update.setDuration(1000);

			final ValueAnimator down = ValueAnimator.ofInt(0, (int) targetMem);
			down.setDuration(1000);

			update.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator va) {
					mLastUsedMem = (Integer) va.getAnimatedValue();
					updateMem();
				}
			});

			update.addListener(new AnimatorListener() {
				@Override
				public void onAnimationStart(Animator arg0) {
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
				}

				@Override
				public void onAnimationCancel(Animator arg0) {
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					down.start();
				}
			});

			down.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator va) {
					mLastUsedMem = (Integer) va.getAnimatedValue();
					updateMem();
				}
			});
			down.addListener(new AnimatorListener() {
				@Override
				public void onAnimationStart(Animator arg0) {
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
				}

				@Override
				public void onAnimationCancel(Animator arg0) {
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					mUpdating = false;
					Toast.makeText(
							CleanMemActivity.this,
							"为您清理" + TextFormater.dataSizeFormat(mCleanMem)
									+ "内存", Toast.LENGTH_SHORT).show();
				}
			});

			update.start();
		}
	}

	public void launchRocket() {
		Animation ta = createRocketFly();
		mRocket.setImageResource(R.drawable.rocket_fly);
		mRocket.startAnimation(ta);
	}

	private Animation createRocketFly() {
		TranslateAnimation ta = new TranslateAnimation(0, 0, 0, -2000);
		ta.setDuration(2000);
		ta.setFillEnabled(true);
		ta.setFillBefore(true);
		ta.setInterpolator(new AccelerateInterpolator());
		ta.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				startTranslate();
			}
		});
		return ta;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			mTouchDownX = event.getX();
			mTouchDownY = event.getY();
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
		} else if (action == MotionEvent.ACTION_MOVE) {
			float x = event.getX();
			float y = event.getY();

			if (Math.abs(mTouchDownX - x) > mThreshold
					|| Math.abs(mTouchDownY - y) > mThreshold) {
				cancelLaunch();
			} else {
				prepareLaunch();
			}
		}
		return false;
	}

	private void prepareLaunch() {
		if (!mVibrating) {
			mVibrating = true;
			new Thread(new VibrateTask()).start();
			// todo
			startSmoking();
		}
		stopTranslate();
	}

	private void startSmoking() {

	}

	private void cancelLaunch() {
		if (!mTranslating) {
			mTranslating = true;
			startTranslate();
		}
		stopVibrate();
	}

	private void stopVibrate() {
		mVibrating = false;
		mVibrator.cancel();
	}

	private class VibrateTask implements Runnable {

		@Override
		public void run() {
			while (mVibrating) {
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
			switch (msg.what) {
			case MSG_UPDATE_MEM:
				mActivity.updateMem();
				break;

			default:
				break;
			}
		}
	}

}
