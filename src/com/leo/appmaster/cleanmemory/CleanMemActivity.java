package com.leo.appmaster.cleanmemory;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.RocketDock;
import com.leo.appmaster.ui.ShadeView;
import com.leo.appmaster.utils.TextFormater;

public class CleanMemActivity extends Activity implements OnClickListener,
		OnTouchListener {

	public static final int MSG_UPDATE_MEM = 0;
	private CommonTitleBar mTtileBar;
	private ImageButton mRocket;
	private ImageView mIvLoad, mIvOk;
	private View mRocketHolder;
	private RocketDock mRocketDock;
	private TextView mTvUsedMemory, mTvTotalMemory, mTvCleanResult,
			mTvAccelerate;
	private ShadeView mShadeView;

	private long mLastUsedMem;
	private long mTotalMem;
	private long mCleanMem;
	private Vibrator mVibrator;
	public boolean mVibrating;
	private boolean mTranslating;
	private boolean mUpdating;
	private ProcessCleaner mCleaner;
	private Animation mRocketAnima;
	private float mTouchDownX, mTouchDownY;
	private float mThreshold = 0;
	private Animation mShakeAnim;

	private boolean mAllowClean;
	private boolean mLading;
	private boolean mLoading;

	private Object lock = new Object();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clean_mem);
		createTranslateAnima();
		initUI();
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	private void preCleanMemory() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (lock) {
					mCleaner.cleanAllProcess(CleanMemActivity.this);
				}
			}
		}).start();
	}

	private void initUI() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.openBackView();
		mTtileBar.setTitle(R.string.clean_memory);
		mRocket = (ImageButton) findViewById(R.id.rocket_icon);
		mRocketHolder = findViewById(R.id.layout_rocket_holder);
		mRocketDock = (RocketDock) findViewById(R.id.rocket_dock);
		mTvUsedMemory = (TextView) findViewById(R.id.tv_memory);
		mTvTotalMemory = (TextView) findViewById(R.id.tv_total);
		mTvCleanResult = (TextView) findViewById(R.id.tv_clean_result);
		mTvAccelerate = (TextView) findViewById(R.id.tv_mem_tip);
		mShadeView = (ShadeView) findViewById(R.id.shade_view);
		mIvLoad = (ImageView) findViewById(R.id.iv_load);
		mIvOk = (ImageView) findViewById(R.id.clean_ok);
		mIvOk.setOnClickListener(this);
		mCleaner = ProcessCleaner.getInstance(this);

		mTotalMem = mCleaner.getTotalMem();
		mLastUsedMem = mCleaner.getUsedMem();
		updateMem();

		mAllowClean = mCleaner.allowClean();
		if (mAllowClean) {
			mRocket.setOnTouchListener(this);
			mRocket.setVisibility(View.VISIBLE);
			mIvLoad.setVisibility(View.VISIBLE);
			startLoad();
			startTranslate();

			preCleanMemory();
		} else {
			mTvCleanResult.setText(R.string.best_mem);
			mRocket.setVisibility(View.INVISIBLE);
			mIvLoad.setVisibility(View.INVISIBLE);
			mIvOk.setVisibility(View.VISIBLE);
			mTvAccelerate.setVisibility(View.INVISIBLE);
			mRocketHolder.setBackgroundDrawable(null);
			mTvAccelerate.setText(R.string.compeletely);
		}

	}

	private void startLoad() {
		rotateLoadView(2000, 360 * 3);
		final int target = (int) (mLastUsedMem / 1024);
		final ValueAnimator up = ValueAnimator.ofInt(0, target);
		up.setDuration(2000);
		up.setInterpolator(new AccelerateDecelerateInterpolator());
		up.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator va) {
				mLastUsedMem = (Integer) va.getAnimatedValue() * 1024l;
				updateMem();
			}
		});
		up.start();
		mShadeView.updateColor(0xff, 0x3b, 0x00, 2000);
	}

	private void rotateLoadView(int duration, int degrees) {
		Animation ra = new RotateAnimation(0, degrees,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		ra.setDuration(duration);
		ra.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mLoading = true;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mLoading = false;
				;
				AlphaAnimation aa = new AlphaAnimation(1f, 0.0f);
				aa.setDuration(500);
				aa.setFillEnabled(true);
				aa.setFillAfter(true);
				mIvLoad.startAnimation(aa);
			}
		});
		mIvLoad.startAnimation(ra);
	}

	private void stopLoad() {
		mIvLoad.clearAnimation();
	}

	private void updateMem() {
		mTvUsedMemory.setText(TextFormater.dataSizeFormat(mLastUsedMem));
		mTvTotalMemory.setText("/" + TextFormater.dataSizeFormat(mTotalMem));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.clean_ok:
			finish();
			break;
		default:
			break;
		}

	}

	private void startTranslate() {
		mTranslating = true;
		mRocket.startAnimation(mRocketAnima);
		mRocket.setImageResource(R.drawable.rocket_stop);
	}

	private void stopTranslate() {
		if (mTranslating) {
			mTranslating = false;
			mRocketAnima.cancel();
			mRocket.setImageResource(R.drawable.rocket_fly);
		}
	}

	private void createTranslateAnima() {
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
        synchronized (lock) {
            launchRocket();
            mCleaner.tryClean(this);
            showOK();
            long curUsedMem = mCleaner.getCurUsedMem();
            mCleanMem = Math.abs(mLastUsedMem - curUsedMem);
            startUpdataMemTip(curUsedMem);
            mShadeView.updateColor(0x28, 0x93, 0xfe, 1200);
            mAllowClean = false;
        }
	}

	private void showOK() {
		AnimationSet as = new AnimationSet(true);
		AlphaAnimation aa = new AlphaAnimation(1.0f, 0.0f);
		ScaleAnimation sa = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
				ScaleAnimation.RELATIVE_TO_SELF, 0.5f);

		aa.setDuration(800);
		sa.setDuration(1000);

		as.addAnimation(sa);
		as.addAnimation(aa);

		as.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mTvAccelerate.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mRocketHolder.setBackgroundDrawable(null);
				mTvAccelerate.setVisibility(View.INVISIBLE);
				mIvOk.setVisibility(View.VISIBLE);
				ScaleAnimation show = new ScaleAnimation(0.0f, 1.0f, 0.0f,
						1.0f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
						ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
				show.setDuration(500);
				show.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						mRocket.setVisibility(View.INVISIBLE);
						mRocket.setClickable(false);
						mRocket.setOnTouchListener(null);
					}
				});
				mRocketHolder.startAnimation(show);
			}
		});

		mRocketHolder.startAnimation(as);

	}

	private void shakeRocket() {
		mShakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake);
		mRocket.startAnimation(mShakeAnim);
	}

	private void stopShakeRocket() {
		mShakeAnim.cancel();
	}

	private void launchRocket() {
		mRocket.setOnTouchListener(null);
		mRocket.setClickable(false);
		Animation ta = createRocketFly();
		mRocket.setImageResource(R.drawable.rocket_fly);
		mRocket.startAnimation(ta);
	}

	private Animation createRocketFly() {
		TranslateAnimation ta = new TranslateAnimation(0, 0, 0, -2000);
		ta.setDuration(1000);
		ta.setFillEnabled(true);
		ta.setFillAfter(true);
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
				// mRocket.setVisibility(View.GONE);
				// ViewGroup vg = (ViewGroup) mRocket.getParent();
				// vg.removeView(mRocket);
				// mRocket.setOnTouchListener(null);
				// mRocket.setEnabled(false);
			}
		});
		return ta;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mLoading)
			return true;
		int action = event.getAction();
		if (mThreshold == 0) {
			mThreshold = Math.min(mRocketHolder.getWidth(),
					mRocketHolder.getHeight()) / 2;
		}

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
				stopShakeRocket();
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
			shakeRocket();
		}
		stopTranslate();
	}

	private void startSmoking() {

	}

	private void cancelLaunch() {
		stopVibrate();
		stopShakeRocket();
		if (!mTranslating) {
			mTranslating = true;
			startTranslate();
		}
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

	private void startUpdataMemTip(long targetMem) {
		if (!mUpdating) {
			mUpdating = true;
			ValueAnimator down = ValueAnimator.ofInt(
					(int) (mLastUsedMem / 1024), 0);
			down.setDuration(300);
			final int target = (int) (targetMem / 1024);
			final ValueAnimator up = ValueAnimator.ofInt(0, target);
			up.setDuration(800);

			down.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator va) {
					mLastUsedMem = (Integer) va.getAnimatedValue() * 1024l;
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
					up.start();
				}
			});

			up.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator va) {
					mLastUsedMem = (Integer) va.getAnimatedValue() * 1024l;
					updateMem();
				}
			});
			up.addListener(new AnimatorListener() {
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

					String s = String.format(
							getString(R.string.clean_result),
							TextFormater.dataSizeFormat(mCleaner
									.getLastCleanMem()) + "");
					Toast.makeText(CleanMemActivity.this, s, Toast.LENGTH_SHORT)
							.show();

					mTvCleanResult.setText(s);
				}
			});

			down.start();
		}
	}

}
