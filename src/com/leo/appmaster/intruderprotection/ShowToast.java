package com.leo.appmaster.intruderprotection;

import com.leo.appmaster.R;
import com.leo.appmaster.ThreadManager;
import com.leo.appmaster.animation.AnimationListenerAdapter;
import com.leo.appmaster.ui.BaseSelfDurationToast;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.LeoLog;
import com.leo.appmaster.utils.Utilities;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class ShowToast {
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

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_down_to_up_fast);
        animation.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        animation.setDuration(800);
        rlScore.startAnimation(animation);
        toast.setView(view);
        toast.show();
    }

    public static void showPermissionGuideToast(final Context context) {
        ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final BaseSelfDurationToast toast = new BaseSelfDurationToast(context);
                toast.setDuration(1000 * 5);
                if (Utilities.hasNavigationBar(context)) {
                    toast.setWindowAnimations(R.style.toast_guide_permission_navigationbar);
                } else {
                    toast.setWindowAnimations(R.style.toast_guide_permission);
                }
                toast.setMatchParent();
                toast.setGravity(Gravity.BOTTOM, 0, DipPixelUtil.dip2px(context, 14));
                View v = LayoutInflater.from(context).inflate(R.layout.toast_permission_guide, null);
                ImageView ivClose = (ImageView) v.findViewById(R.id.iv_permission_guide_close);
                ivClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (toast != null) {
                            toast.hide();
                        }
                    }
                });
                toast.setView(v);
                toast.show();
            }
        }, 200);
    }


//    ThreadManager.getUiThreadHandler().postDelayed(new Runnable() {
//        @Override
//        public void run() {
//            if (mPermissionGuideToast == null) {
//                mPermissionGuideToast = new BaseSelfDurationToast(mContext);
//            }
//            mPermissionGuideToast.setDuration(1000 * 5);
//            if (Utilities.hasNavigationBar(mContext)) {
//                mPermissionGuideToast.setWindowAnimations(R.style.toast_guide_permission_navigationbar);
//            } else {
//                mPermissionGuideToast.setWindowAnimations(R.style.toast_guide_permission);
//            }
//            mPermissionGuideToast.setMatchParent();
//            mPermissionGuideToast.setGravity(Gravity.BOTTOM, 0, DipPixelUtil.dip2px(mContext, 14));
//            View v = LayoutInflater.from(mContext).inflate(R.layout.toast_permission_guide, null);
//            ImageView ivClose = (ImageView) v.findViewById(R.id.iv_permission_guide_close);
//            ivClose.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (mPermissionGuideToast != null) {
//                        mPermissionGuideToast.hide();
//                    }
//                }
//            });
//            mPermissionGuideToast.setView(v);
//            mPermissionGuideToast.show();
//        }
//    }, 200);

}
