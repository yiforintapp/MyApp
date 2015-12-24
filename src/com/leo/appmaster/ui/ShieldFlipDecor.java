package com.leo.appmaster.ui;

import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * 满分动画盾牌翻转装饰
 * Created by Jasper on 2015/12/24.
 */
public class ShieldFlipDecor extends BaseDecor {
    @Override
    public void applyDecor(Canvas canvas, Matrix matrix) {
//        setParentLayer(mParent);
        Camera camera = new Camera();
        camera.save();
        camera.rotateY(30);  
//        camera.ro
        camera.getMatrix(matrix);
        int centerX = mParent.centerX();
        int centerY = mParent.centerY();
        matrix.preTranslate(-centerX, -centerY);  
        matrix.postTranslate(centerX, centerY); 
    }
}
