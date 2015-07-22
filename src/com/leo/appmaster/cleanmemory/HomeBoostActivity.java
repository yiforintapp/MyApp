
package com.leo.appmaster.cleanmemory;

import com.leo.appmaster.AppMasterApplication;
import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.TextFormater;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeBoostActivity extends Activity {
    private ImageView mIvRocket, mIvCloud;
    private int mRocketHeight;

    private boolean isClean = false;
    private ProcessCleaner mCleaner;
    private long mLastUsedMem;
    private long mCleanMem;
    private boolean isCleanFinish = false;
    private int mScreenH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_boost_activity);
        initUI();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        mIvRocket.post(new Runnable() {
            @Override
            public void run() {
                startClean();
            }
        });
        super.onResume();
    }

    @Override
    public void finish() {
        overridePendingTransition(0, 0);
        super.finish();
    }

    private void initUI() {
        Display mDisplay = getWindowManager().getDefaultDisplay();
        mScreenH = mDisplay.getHeight();

        mIvRocket = (ImageView) findViewById(R.id.iv_rocket);
        mIvCloud = (ImageView) findViewById(R.id.iv_cloud);
    }

    private void startClean() {
        AppMasterApplication.getInstance().getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                cleanMemory();
            }
        });
        mRocketHeight = mIvRocket.getMeasuredHeight();
        ObjectAnimator rocketAnimator1 = ObjectAnimator.ofFloat(mIvRocket, "translationY",
                mRocketHeight, mRocketHeight * 0.18f, mRocketHeight * 0.26f, mRocketHeight * 0.30f);
        rocketAnimator1.setDuration(800);
        ObjectAnimator rocketAnimator2 = ObjectAnimator.ofFloat(mIvRocket,
                "translationY",
                mRocketHeight * 0.30f, -mScreenH);
        rocketAnimator2.setDuration(800);

        rocketAnimator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIvCloud.setVisibility(View.VISIBLE);
                ObjectAnimator cloudAlphaAnimator1 = ObjectAnimator.ofFloat(mIvCloud,
                        "alpha", 0f,
                        1f);
                cloudAlphaAnimator1.setDuration(300);
                ObjectAnimator cloudScaleXAnimator = ObjectAnimator.ofFloat(mIvCloud,
                        "scaleX", 1f,
                        1.2f);
                ObjectAnimator cloudAlphaAnimator2 = ObjectAnimator.ofFloat(mIvCloud,
                        "alpha", 1f,
                        0f);
                cloudScaleXAnimator.setDuration(1200);
                cloudAlphaAnimator2.setDuration(1200);
                cloudAlphaAnimator2.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        HomeBoostActivity.this.finish();
                    }
                });
                AnimatorSet cloudAnimatorSet = new AnimatorSet();
                cloudAnimatorSet.play(cloudScaleXAnimator).with(cloudAlphaAnimator2)
                        .after(cloudAlphaAnimator1);
                cloudAnimatorSet.start();
            }

        });
        AnimatorSet as = new AnimatorSet();
        as.play(rocketAnimator2).after(300).after(rocketAnimator1);
        as.start();
        as.setInterpolator(new LinearInterpolator());
        as.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showCleanResault();
            }
        });
    }

    private void cleanMemory() {
        AppMasterPreference amp = AppMasterPreference.getInstance(this);
        long currentTime = System.currentTimeMillis();
        long lastBoostTime = amp.getLastBoostTime();
        if ((currentTime - lastBoostTime) < 10 * 1000) {
            isClean = false;
        } else {
            isClean = true;
            isCleanFinish = false;
            mCleaner = ProcessCleaner.getInstance(this);
            mLastUsedMem = mCleaner.getUsedMem();
            mCleaner.tryClean(this);
            long curUsedMem = mCleaner.getUsedMem();
            mCleanMem = Math.abs(mLastUsedMem - curUsedMem);
            isCleanFinish = true;
            amp.setLastBoostTime(currentTime);
        }
    }

    public void showCleanResault() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.toast_self_make, null);
        TextView tv_clean_rocket = (TextView) view.findViewById(R.id.tv_clean_rocket);
        String mToast;

        if (isClean) {
            if (isCleanFinish) {
                if (mCleanMem <= 0) {
                    LeoLog.d("testspeed", "CleanMem <= 0");
                    mToast = getString(R.string.home_app_manager_mem_clean_one);
                } else {
                    LeoLog.d("testspeed", "CleanMem > 0");
                    mToast = getString(R.string.home_app_manager_mem_clean,
                            TextFormater.dataSizeFormat(mCleanMem));
                }
            } else {
                mToast = getString(R.string.home_app_manager_mem_clean,
                        TextFormater.dataSizeFormat(230));
            }

        } else {
            mToast = getString(R.string.the_best_status_toast);
        }

        tv_clean_rocket.setText(mToast);
        Toast toast = new Toast(this);
        toast.setView(view);
        toast.setDuration(0);
        int marginTop = 0;
        if (mScreenH >= 1920) {
            marginTop = 150;
        } else if (mScreenH >= 1280) {
            marginTop = 120;
        } else if (mScreenH >= 800) {
            marginTop = 80;
        } else {
            marginTop = 30;
        }
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, marginTop);
        toast.show();
        isClean = true;
    }

}
