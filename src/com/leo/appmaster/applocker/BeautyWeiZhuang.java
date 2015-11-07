
package com.leo.appmaster.applocker;


import android.app.Service;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.ZipperView.OnGestureSuccessListener;
import com.leo.appmaster.applocker.ZipperView.OnGestureTooFastListener;
import com.leo.appmaster.applocker.ZipperView.OnGestureTooSlowListener;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.tools.animator.Animator;
import com.leo.tools.animator.Animator.AnimatorListener;
import com.leo.tools.animator.AnimatorSet;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;

public class BeautyWeiZhuang extends BaseActivity
{
    private final static int BEAUTYWEIZHUANG = 4;
    private LEOAlarmDialog mAlarmDialog;
    private AppMasterPreference mAppMasterSP;
    private ZipperView mZipperView;
    private Vibrator vib;
    private ImageView mLeftArrow;
    private ImageView mRightArrow;
    private TextView mTvTitle;
    private TextView mTvContent;
    private Boolean mIsAnimationCanPlay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_beauty);
        init();
    }

    private void init() {
        mAppMasterSP = AppMasterPreference.getInstance(this);
        mZipperView = (ZipperView) findViewById(R.id.zipperview_beauty_guide);
        mZipperView.setOnGestureSuccessListener(new OnGestureSuccessListener() {
            @Override
            public void OnGestureSuccess() {
                // TODO Auto-generated method stub
                showAlarmDialog(getString(R.string.open_weizhuang_dialog_title),
                        getString(R.string.open_weizhuang_dialog_content),
                        getString(R.string.open_weizhuang_dialog_sure));
            }
        });
        vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);

        mLeftArrow = (ImageView) findViewById(R.id.iv_leftarrow_beauty_guide);
        mRightArrow = (ImageView) findViewById(R.id.iv_rightarrow_beauty_guide);
        mZipperView.setOnGestureTooFastListener(new OnGestureTooFastListener() {
        
            @Override
            public void OnGestureTooFast() {
                // TODO Auto-generated method stub

                if (mIsAnimationCanPlay)
                {
                    gestureTranslationAnim(mLeftArrow, mRightArrow);
                }
            }
        });
        mTvTitle = (TextView) findViewById(R.id.tv_title_beauty_guide);
        mTvContent=(TextView) findViewById(R.id.tv_content_beauty_guide);
        mZipperView.setOnGestureTooSlowListener(new OnGestureTooSlowListener() {

            @Override
            public void OnGestureTooSlow() {
                // TODO Auto-generated method stub

                if (mIsAnimationCanPlay)
                {
                    gestureTranslationAnim(mLeftArrow, mRightArrow);
                }
            }
        });
        

    }

    private AnimatorSet gestureTranslationAnim(View view1, View view2) {
        view1.clearAnimation();
        view2.clearAnimation();
        AnimatorSet animatorSet = new AnimatorSet();

        float translation = DipPixelUtil.dip2px(this, 10);
        PropertyValuesHolder arrowHolderX = PropertyValuesHolder
                .ofFloat("translationX", 0, translation, 0);
        PropertyValuesHolder arrowHolderX2 = PropertyValuesHolder
                .ofFloat("translationX", 0, -translation, 0);
        // PropertyValuesHolder arrowHolderY = PropertyValuesHolder
        // .ofFloat("translationY", 0, 0, -translation);
        ObjectAnimator translateArrow = (ObjectAnimator) ObjectAnimator.ofPropertyValuesHolder(
                view1, arrowHolderX);
        translateArrow.setDuration(1000);

        translateArrow.setRepeatCount(3);
        ObjectAnimator translateArrow2 = (ObjectAnimator) ObjectAnimator.ofPropertyValuesHolder(
                view2, arrowHolderX2);
        translateArrow2.setDuration(1000);
        translateArrow2.setRepeatCount(3);

        animatorSet.playTogether(translateArrow2, translateArrow);
        animatorSet.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                // TODO Auto-generated method stub
                mTvTitle.setTextColor(Color.rgb(0xfe, 0x01, 0x6c));
                mTvContent.setTextColor(Color.rgb(0xfe, 0x01, 0x6c));
                mIsAnimationCanPlay = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // TODO Auto-generated method stub
                // mTvTitle.setTextColor(0xffffff);
                mTvTitle.setTextColor(Color.rgb(0xff, 0xff, 0xff));
                mTvContent.setTextColor(Color.rgb(0xff, 0xff, 0xff));
                mIsAnimationCanPlay = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // TODO Auto-generated method stub

            }
        });
        animatorSet.start();

        return animatorSet;
    }

    private void showAlarmDialog(String title, String content, String sureText)
    {
        if (mAlarmDialog == null)
        {
            mAlarmDialog = new LEOAlarmDialog(this);
            mAlarmDialog.setOnClickListener(new OnDiaogClickListener() {
                @Override
                public void onClick(int which) {
                    // ok
                    if (which == 1)
                    {
                        mAppMasterSP.setPretendLock(BEAUTYWEIZHUANG);
                        vib.vibrate(150);
                        BeautyWeiZhuang.this.finish();
                        Toast.makeText(BeautyWeiZhuang.this, getString(R.string.beauty_mode_ok), 0)
                                .show();
                    }

                }
            });
        }
        mAlarmDialog.setSureButtonText(sureText);
        mAlarmDialog.setTitle(title);
        mAlarmDialog.setContent(content);
        mAlarmDialog.show();
    }

    protected void onDestroy() {
        if (mAlarmDialog != null) {
            mAlarmDialog.dismiss();
            mAlarmDialog = null;
        }
        super.onDestroy();
    }
}
