
package com.leo.appmaster.quickgestures.view;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public class NoneAction implements DecorateAction {

    @Override
    public int getActionType() {
        return ACTION_NONE;
    }

    @Override
    public void draw(Canvas canvas, View view) {

    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public Rect getDrawRect() {
        return null;
    }

}
