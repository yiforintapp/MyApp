
package com.leo.appmaster.quickgestures.view;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;
import android.view.View.DragShadowBuilder;

public class GestureDragShadowBuilder extends DragShadowBuilder {


    public GestureDragShadowBuilder(View arg0, float scale) {
        super(arg0);
    }

    @Override
    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
//        View view = getView();
//        shadowSize.x = (int) (view.getWidth() * mShadowScale);
//        shadowSize.y = (int) (view.getHeight() * mShadowScale);
//        // shadowTouchPoint.x = (int) (shadowSize.x / 1.5f);
//        // shadowTouchPoint.y = (int) (shadowSize.y / 1.5f);
//        shadowTouchPoint.x = 0;
//        shadowTouchPoint.y = 0;
        super.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        super.onDrawShadow(canvas);
    }
}
