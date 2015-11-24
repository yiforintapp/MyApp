package com.leo.appmaster.intruderprotection;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.BaseSelfDurationToast;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;


public class UpdateScoreHelper {
    public static void showGetScoreToast(int score ,Context context) {
        BaseSelfDurationToast toast = new BaseSelfDurationToast(context);
        toast.setDuration(2600);
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        View view = View.inflate(context, R.layout.toast_get_score, null);
        toast.setView(view);
        final TextView tvScore = (TextView) view.findViewById(R.id.tv_score);
        tvScore.setText("1");
        int[] scores = new int[score];
        for(int i = 0; i < scores.length; i++) {
            scores[i] = i + 1;
        }
        PropertyValuesHolder holder = PropertyValuesHolder.ofInt("text11",scores);
        final ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(tvScore, holder);
        animator.setDuration(600);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                int animatedValue = (Integer) arg0.getAnimatedValue();
                tvScore.setText(animatedValue+"");
            }
        });
//        toast.setGravity(Gravity.CENTER, 0, 300);
        toast.setView(view);
        toast.show();
        animator.setStartDelay(500);
        animator.start();
    
    }
}
