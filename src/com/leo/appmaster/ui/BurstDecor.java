package com.leo.appmaster.ui;

import com.leo.appmaster.R;
import com.leo.tools.animator.ObjectAnimator;
import com.leo.tools.animator.PropertyValuesHolder;
import com.leo.tools.animator.ValueAnimator;
import com.leo.tools.animator.ValueAnimator.AnimatorUpdateListener;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;

/**
 * 满分动画爆裂装饰
 * Created by Jasper on 2015/12/24.
 */
public class BurstDecor extends BaseDecor {
    private float mCanvasRotateDegree = 0f;
    private float mMostFarDistance = 500f;
    private float[] mBurstDegrees = {36f, 72f, 108f, 144f, 180f, 216f, 252f, 288f, 324f, 360f};
    @Override
    public void applyDecor(Canvas canvas, Matrix matrix) {
        canvas.save();
        canvas.rotate(90f, mParent.centerX(), mParent.centerY());
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);    //设置画笔的颜色为白色
        paint.setAntiAlias(true);    //消除锯齿
        paint.setStyle(Style.STROKE);    //设置画笔风格为描边
        paint.setStrokeWidth(30f);    //设置描边的宽度为3
//        for ()
//        canvas.drawPoint(mParent.centerX(), mParent.centerX()+40, paint);
////        canvas.drawPoint(mParent.centerX() + 30, mParent.centerX() + 60, paint);
//        canvas.drawPoint(mParent.centerX() + 60, mParent.centerX() +420, paint);
//        canvas.restore();
//        canvas.drawb
//        
//        mParent.
    }
    
//    private calculatePosition() {
//        
//    }
    
    
    public void startBurstAnim(long duration) {
        PropertyValuesHolder v1 = PropertyValuesHolder.ofFloat("flipDegreeY", 0f, 180f);
        final ObjectAnimator animatorI = ObjectAnimator.ofPropertyValuesHolder(this, v1);
        
        
//        animatorI.addUpdateListener(new AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                float animatedValue = (Float) animation.getAnimatedValue("flipDegreeY");
//                if (animatedValue == 90f) {
//                    mCurrentStatus ++;
//                }
//            }
//        });
        animatorI.setDuration(duration);
        animatorI.start();
    }
    
}
