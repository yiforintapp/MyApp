
package com.leo.appmaster.applocker;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.TimeView;
import com.leo.appmaster.utils.BuildProperties;
import com.leo.tools.animator.ValueAnimator;
import com.leo.tools.animator.ValueAnimator.AnimatorUpdateListener;

public class WaitActivity extends BaseActivity {

    private TextView mTvTime;
    private int mWaitTime = 10;//10
    private int mInitTime = 0;
    private UpdateTask mTask;
    private TimeView mTimeView;

    private float mInitDegree;
    private boolean returned;
    private ValueAnimator va;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);
        handleIntent();
        initUI();
        mTimeView.updateDegree(mInitDegree);
        mWaitTime = 10 - mInitTime;
        mTvTime.setText("" + mWaitTime);
        mTask = new UpdateTask();
        startWaitTime();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        mInitTime = intent.getIntExtra("outcount_time", 0);
        mInitDegree = (mInitTime / 10f) * 360f;
    }

    @Override
    protected void onStop() {
//         returned = true;
        super.onStop();
    }

    private void startWaitTime() {
        va = ValueAnimator.ofFloat(mInitDegree, 360f);
        va.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator v) {
                float d = (Float) v.getAnimatedValue();
                mTimeView.updateDegree(d);
                if (returned)
                    va.cancel();
            }
        });
        String phoneModel=BuildProperties.getPoneModel();
        if(BuildProperties.I_STYLE_MODEL.equals(phoneModel)){
            va.setDuration(mWaitTime * 1000 * 2);
        }else{
            va.setDuration(mWaitTime * 1000);
        }
        va.setInterpolator(new LinearInterpolator());
        va.start();

        ThreadManager.executeOnAsyncThread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (returned) {
                        return;
                    }
                    mTvTime.post(mTask);
                    if (mWaitTime == 0) {
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mWaitTime--;
                }
                // returnBack();
                returned = true;
                finish();
            }
        });
    }
    
    // private void returnBack() {
    // if (returned) {
    // return;
    // }
    // returned = true;
    // Intent intent = new Intent(this, LockScreenActivity.class);
    // int lockType = AppMasterPreference.getInstance(WaitActivity.this)
    // .getLockType();
    // if (lockType == AppMasterPreference.LOCK_TYPE_PASSWD) {
    // intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
    // LockFragment.LOCK_TYPE_PASSWD);
    // } else {
    // intent.putExtra(LockScreenActivity.EXTRA_UKLOCK_TYPE,
    // LockFragment.LOCK_TYPE_GESTURE);
    // }
    // if (mPackage == null || mPackage.equals("")) {
    // intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
    // LockFragment.FROM_SELF);
    // } else {
    // intent.putExtra(LockHandler.EXTRA_LOCKED_APP_PKG, mPackage);
    // intent.putExtra(LockScreenActivity.EXTRA_UNLOCK_FROM,
    // LockFragment.FROM_OTHER);
    // }
    // startActivity(intent);
    // finish();
    // }

    private void initUI() {
        mTvTime = (TextView) findViewById(R.id.tv_wait_time);
        mTimeView = (TimeView) findViewById(R.id.time_view);
    }

    @Override
    public void onBackPressed() {
//        va.cancel();
//        va = null;
//        returned = true;
//        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        try {
            startActivity(intent);
        } catch (Exception e) {          
        }        
    }

    private class UpdateTask implements Runnable {
        @Override
        public void run() {
            mTvTime.setText(""+mWaitTime);
        }
    }
}
