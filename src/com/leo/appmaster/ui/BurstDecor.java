package com.leo.appmaster.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * 满分动画爆裂装饰
 * Created by Jasper on 2015/12/24.
 */
public class BurstDecor extends BaseDecor {
    private float mCanvasRotateDegree = 0f;
    private float mMostFarDistance = 500f;
    
    @Override
    public void applyDecor(Canvas canvas, Matrix matrix) {
        canvas.save();
        canvas.rotate(90f, mParent.centerX(), mParent.centerY());
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);    //设置画笔的颜色为白色
        paint.setAntiAlias(true);    //消除锯齿
        paint.setStyle(Style.STROKE);    //设置画笔风格为描边
        paint.setStrokeWidth(30f);    //设置描边的宽度为3
        canvas.drawPoint(mParent.centerX(), mParent.centerX()+40, paint);
        canvas.drawPoint(mParent.centerX() + 30, mParent.centerX() + 60, paint);
        canvas.drawPoint(mParent.centerX() + 60, mParent.centerX() +420, paint);
        canvas.restore();
    }
    
    
    
}
