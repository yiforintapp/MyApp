
package com.leo.appmaster.quickgestures.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

public class GestureTextView extends TextView {

    private DecorateAction mDecorateAction;

    public GestureTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDecorateAction(DecorateAction action) {
        mDecorateAction = action;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDecorateAction.draw(canvas, this);
    }

}
