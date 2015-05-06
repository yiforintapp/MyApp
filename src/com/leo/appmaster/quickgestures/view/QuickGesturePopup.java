
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import com.leo.appmaster.quickgestures.view.QuickGestureLayout;
import com.leo.appmaster.quickgestures.view.QuickGestureLayout.LayoutParams;

import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;

public class QuickGesturePopup extends Activity {

    private QuickGestureContainer mContainer;
    QuickGestureLayout mDymixLayout, mMostUsedLayout, mSwitcherLayout;
    private ImageView iv0;
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;
    private ImageView iv4;
    private ImageView iv5;
    private ImageView iv6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_quick_gesture);
        mDymixLayout = (QuickGestureLayout) findViewById(R.id.qg_dymic_layout);
        mMostUsedLayout = (QuickGestureLayout) findViewById(R.id.qg_mostused_layout);
        mSwitcherLayout = (QuickGestureLayout) findViewById(R.id.qg_switcher_layout);
        fillQg1();
        fillQg2();
        fillQg3();

        mContainer = (QuickGestureContainer) findViewById(R.id.gesture_container);
        mContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                mContainer.showOpenAnimation();
            }
        }, 900);
    }

    private void fillQg1() {
        LayoutParams params = null;
        iv0 = new ImageView(this);
        iv0.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 0;
        iv0.setLayoutParams(params);
        mDymixLayout.addView(iv0);

        iv1 = new ImageView(this);
        iv1.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 1;
        iv1.setLayoutParams(params);
        mDymixLayout.addView(iv1);

        iv2 = new ImageView(this);
        iv2.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 2;
        iv2.setLayoutParams(params);
        mDymixLayout.addView(iv2);

        iv3 = new ImageView(this);
        iv3.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 3;
        iv3.setLayoutParams(params);
        mDymixLayout.addView(iv3);

        iv4 = new ImageView(this);
        iv4.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 4;
        iv4.setLayoutParams(params);
        mDymixLayout.addView(iv4);

        iv5 = new ImageView(this);
        iv5.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 5;
        iv5.setLayoutParams(params);
        mDymixLayout.addView(iv5);

        iv6 = new ImageView(this);
        iv6.setImageResource(R.drawable.ic_launcher);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 6;
        iv6.setLayoutParams(params);
//        iv6.setOnLongClickListener(new OnLongClickListener() {
//
//            @Override
//            public boolean onLongClick(View v) {
//                v.startDrag(null, new DragShadowBuilder(v), v, 0);
//                return true;
//            }
//        });
        mDymixLayout.addView(iv6);
        // mDymixLayout.setRotation(-45);
    }

    private void fillQg2() {
        LayoutParams params = null;
        iv0 = new ImageView(this);
        iv0.setImageResource(R.drawable.call_info_sms_icon);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 0;
        iv0.setLayoutParams(params);
        mMostUsedLayout.addView(iv0);

        iv1 = new ImageView(this);
        iv1.setImageResource(R.drawable.call_info_sms_icon);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 1;
        iv1.setLayoutParams(params);
        mMostUsedLayout.addView(iv1);

        iv2 = new ImageView(this);
        iv2.setImageResource(R.drawable.call_info_sms_icon);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 2;
        iv2.setLayoutParams(params);
        mMostUsedLayout.addView(iv2);

        iv3 = new ImageView(this);
        iv3.setImageResource(R.drawable.call_info_sms_icon);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 3;
        iv3.setLayoutParams(params);
        mMostUsedLayout.addView(iv3);

        iv4 = new ImageView(this);
        iv4.setImageResource(R.drawable.call_info_sms_icon);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 4;
        iv4.setLayoutParams(params);
        mMostUsedLayout.addView(iv4);

        iv5 = new ImageView(this);
        iv5.setImageResource(R.drawable.call_info_sms_icon);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 5;
        iv5.setLayoutParams(params);
        mMostUsedLayout.addView(iv5);

        iv6 = new ImageView(this);
        iv6.setImageResource(R.drawable.call_info_sms_icon);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 6;
        iv6.setLayoutParams(params);
        mMostUsedLayout.addView(iv6);
        // mMostUsedLayout.setRotation(-315);
    }

    private void fillQg3() {
        LayoutParams params = null;
        iv0 = new ImageView(this);
        iv0.setImageResource(R.drawable.app_stop_btn);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 0;
        iv0.setLayoutParams(params);
        mSwitcherLayout.addView(iv0);

        iv1 = new ImageView(this);
        iv1.setImageResource(R.drawable.app_stop_btn);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 1;
        iv1.setLayoutParams(params);
        mSwitcherLayout.addView(iv1);

        iv2 = new ImageView(this);
        iv2.setImageResource(R.drawable.app_stop_btn);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 2;
        iv2.setLayoutParams(params);
        mSwitcherLayout.addView(iv2);

        iv3 = new ImageView(this);
        iv3.setImageResource(R.drawable.app_stop_btn);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 3;
        iv3.setLayoutParams(params);
        mSwitcherLayout.addView(iv3);

        iv4 = new ImageView(this);
        iv4.setImageResource(R.drawable.app_stop_btn);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 4;
        iv4.setLayoutParams(params);
        mSwitcherLayout.addView(iv4);

        iv5 = new ImageView(this);
        iv5.setImageResource(R.drawable.app_stop_btn);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 5;
        iv5.setLayoutParams(params);
        mSwitcherLayout.addView(iv5);

        iv6 = new ImageView(this);
        iv6.setImageResource(R.drawable.app_stop_btn);
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.position = 6;
        iv6.setLayoutParams(params);
        mSwitcherLayout.addView(iv6);
        // mMostUsedLayout.setRotation(-315);
    }

    public void removeItem(View v) {
        mDymixLayout.removeView(iv2);
    }

    public void addItem(View v) {
        mDymixLayout.addView(iv2);
    }
    
    @Override
    public void onBackPressed() {
        mContainer.showCloseAnimation();
//        super.onBackPressed();
    }
}
