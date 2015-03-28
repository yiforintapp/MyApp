
package com.leo.appmaster.fragment;

import com.leo.appmaster.utils.DipPixelUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.TextView;

public class TipTextView extends TextView {

    boolean hasTip;
    Paint paint;

    float x, y;
    int radius;

    public TipTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);

        radius = DipPixelUtil.dip2px(context, 4);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        x = width * 0.85f;
        y = height * 0.3f;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (hasTip) {
            canvas.drawCircle(x, y, radius, paint);
        }

    }

    public void showTip(boolean show) {
        hasTip = show;
        invalidate();
    }

    public boolean isShowingTip() {
        return hasTip;
    }
    
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
            
        }
    }

}
