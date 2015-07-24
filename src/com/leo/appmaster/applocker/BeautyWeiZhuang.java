
package com.leo.appmaster.applocker;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Service;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.ZipperView.OnGestureSuccessListener;
import com.leo.appmaster.applocker.ZipperView.OnGestureTooFastListener;
import com.leo.appmaster.sdk.BaseActivity;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog;
import com.leo.appmaster.ui.dialog.LEOAlarmDialog.OnDiaogClickListener;
import com.leo.appmaster.utils.DipPixelUtil;

public class BeautyWeiZhuang extends BaseActivity
{
    private final static int BEAUTYWEIZHUANG = 4;
    private LEOAlarmDialog mAlarmDialog;
    private AppMasterPreference mAppMasterSP;
    private ZipperView mZipperView;
    private Vibrator vib;
    private ImageView mLeftArrow;
    private ImageView mRightArrow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_beauty);
        init();
    }

    private void init() {
        mAppMasterSP=AppMasterPreference.getInstance(this);
        mZipperView=(ZipperView) findViewById(R.id.zipperview_beauty_guide);
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
        
//        mLeftArrow=(ImageView) findViewById(R.id.iv_leftarrow_beauty_guide);
//        mRightArrow=(ImageView) findViewById(R.id.iv_rightarrow_beauty_guide);
//        mZipperView.setOnGestureTooFastListener(new OnGestureTooFastListener() {
            
//            @Override
//            public void OnGestureTooFast() {
//                // TODO Auto-generated method stub
//                gestureTranslationAnim(mLeftArrow,mRightArrow);
//            }
//        });
//        
        
        
        
        
        
        
    }
    
    
    
    
    
    
    
//    private AnimatorSet gestureTranslationAnim(View view1, View view2) {
//        view1.clearAnimation();
//        view2.clearAnimation();
//        AnimatorSet animatorSet = new AnimatorSet();
// 
//        float translation = DipPixelUtil.dip2px(this, 100);
//        PropertyValuesHolder arrowHolderX = PropertyValuesHolder
//                .ofFloat("translationX", 0, 0, -translation);
//        PropertyValuesHolder arrowHolderX2 = PropertyValuesHolder
//                .ofFloat("translationX", 0, 0, translation);
////        PropertyValuesHolder arrowHolderY = PropertyValuesHolder
////                .ofFloat("translationY", 0, 0, -translation);
//        ObjectAnimator translateArrow = (ObjectAnimator) ObjectAnimator.ofPropertyValuesHolder(
//                view1, arrowHolderX);
//        translateArrow.setDuration(1000);
//        translateArrow.setRepeatCount(3);
//        ObjectAnimator translateArrow2 = (ObjectAnimator) ObjectAnimator.ofPropertyValuesHolder(
//                view2, arrowHolderX2);
//        translateArrow2.setDuration(1000);
//        translateArrow2.setRepeatCount(3);
//        
//        
//        
//        animatorSet.playTogether(translateArrow2, translateArrow);
//        animatorSet.start();
//        return animatorSet;
//    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
        
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
                        Toast.makeText(BeautyWeiZhuang.this, getString(R.string.beauty_mode_ok), 0).show();
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
