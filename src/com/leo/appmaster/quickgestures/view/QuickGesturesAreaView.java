
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.DipPixelUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * QuickGesturesAreaView
 * 
 * @author run
 */
public class QuickGesturesAreaView extends View {
    public static int viewWidth;
    public static int viewHeight;
    private boolean mIsShowReadTip;
    private Paint mPaint;
    private float x, y;
    private int radius;

    public QuickGesturesAreaView(Context context) {
        super(context);
        initUI(context);
    }

    public QuickGesturesAreaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI(context);
        initPain(context);
    }

    private void initPain(Context context) {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        radius = DipPixelUtil.px2dip(context, 4);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsShowReadTip) {
            drawReadTip(canvas, mPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        x = getMeasuredHeight() * 0.8f;
        y = getMeasuredWidth() * 0.4f;

    }

    @SuppressLint("ResourceAsColor")
    private void initUI(Context mContext) {
        this.mContext = mContext;
        setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
//         setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
        setFocusable(true);
        setClickable(true);
    }

    private void drawReadTip(Canvas canvas, Paint paint) {
        canvas.drawCircle(x, y, radius, paint);

    }

    public void setIsShowReadTip(boolean flag) {
        mIsShowReadTip = flag;
        invalidate();
    }
}
