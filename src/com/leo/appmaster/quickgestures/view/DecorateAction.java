
package com.leo.appmaster.quickgestures.view;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public interface DecorateAction {

    public static final int ACTION_NONE = 0;
    public static final int ACTION_EVENT = 1;

    public int getActionType();

    public void draw(Canvas canvas, View view);

    public void setAlpha(int alpha);
    
    public Rect getDrawRect();
}
