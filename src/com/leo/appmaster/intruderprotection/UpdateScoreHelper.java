package com.leo.appmaster.intruderprotection;

import com.leo.appmaster.R;
import com.leo.appmaster.animation.AnimationListenerAdapter;
import com.leo.appmaster.ui.BaseSelfDurationToast;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class UpdateScoreHelper {
    public static void showGetScoreToast(int score ,Context context) {
        BaseSelfDurationToast toast = new BaseSelfDurationToast(context);
        toast.setDuration(2600);
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        View view = View.inflate(context, R.layout.toast_get_score, null);
        toast.setView(view);
        final TextView tvScore = (TextView) view.findViewById(R.id.tv_score);
        final RelativeLayout rlScore = (RelativeLayout) view.findViewById(R.id.rl_score);
        tvScore.setText(score+"");
        int[] scores = new int[score];
        for(int i = 0; i < scores.length; i++) {
            scores[i] = i + 1;
        }
//        PropertyValuesHolder holder = PropertyValuesHolder.ofInt("text11",scores);
//        final ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(tvScore, holder);
        
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_down_to_up_fast);
        animation.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        animation.setDuration(800);
        rlScore.startAnimation(animation);
        
//        animator.setDuration(600);
//        animator.setInterpolator(new DecelerateInterpolator());
//        animator.addUpdateListener(new AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator arg0) {
//                int animatedValue = (Integer) arg0.getAnimatedValue();
//                tvScore.setText(animatedValue+"");
//                tvScore.setTop(300);
//            }
//        });
//        toast.setGravity(Gravity.CENTER, 0, 300);
        toast.setView(view);
        toast.show();
//        animator.setStartDelay(500);
//        animator.start();
    
    }
}
