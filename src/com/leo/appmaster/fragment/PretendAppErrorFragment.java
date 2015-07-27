
package com.leo.appmaster.fragment;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.applocker.GestureTextView;
import com.leo.appmaster.applocker.LockScreenActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.LeoLog;

public class PretendAppErrorFragment extends PretendFragment {

    private GestureTextView mGtv;
    private TextView mTitle;
    private View selector_done;

    private int mSleWidth = 0;
    private int mSleHeight = 0;

    private String mTips = "";

    @Override
    protected int layoutResourceId() {
        return R.layout.fragment_pretend_app_error;
    }
    
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
//        SDKWrapper.addEvent(mActivity, SDKWrapper.P1, 
//                "appcover ", "error");
 
    }
    
    
    
    
    

    @Override
    protected void onInitUI() {
        mTitle = (TextView) findViewById(R.id.tv_pretend_app_name);
        if (mTitle != null) {
            mTitle.setText(mTips);
        }
        mGtv = (GestureTextView) findViewById(R.id.tv_make_sure);
        mGtv.setPretendFragment(this);

        selector_done = findViewById(R.id.selector_done);

        SDKWrapper
                .addEvent(mActivity, SDKWrapper.P1, "appcover", "AppError");
    }

    public void setSelector(int left, int top, int right, int bottom) {
        LeoLog.d("setSelector", "left :" + left + "--top :" + top + "--right :" + right
                + "--bottom :" + bottom);
        mSleWidth = right - left;
        mSleHeight = bottom - top;
    }

    public void startMove() {
        FrameLayout.LayoutParams lParams = new FrameLayout.LayoutParams(mSleWidth, mSleHeight);
        selector_done.setLayoutParams(lParams);
        selector_done.setVisibility(View.VISIBLE);
        
        TranslateAnimation ta1 = new TranslateAnimation(-mSleWidth, 0, 0, 0);
        ta1.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
                
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                onUnlockPretendSuccessfully();
            }
        });
        ta1.setDuration(800);
        selector_done.startAnimation(ta1);
    }

    public void setErrorTip(String name) {
        mTips = name;
        if (mTitle != null) {
            mTitle.setText(mTips);
        }
    }
}
