
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
import android.util.Log;
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
    private int mDirectionFlag = -1;// 1:左边，2：右边
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
    }

    private void initPain(Context context) {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsShowReadTip) {
            if (mDirectionFlag > 0) {
                drawReadTip(canvas, mPaint, mDirectionFlag);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        y = getMeasuredHeight();
        x = getMeasuredWidth();

    }

    @SuppressLint("ResourceAsColor")
    private void initUI(Context mContext) {
        this.mContext = mContext;
        setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
//         setBackgroundResource(R.color.quick_gesture_switch_setting_show_color);
        setFocusable(true);
        setClickable(true);
        radius = DipPixelUtil.dip2px(mContext, 5);
        initPain(mContext);
    }

    private void drawReadTip(Canvas canvas, Paint paint, int flag) {
        if (flag == 1) {
            // 左边View
            canvas.drawCircle(0 + 2 * radius, y - 2 * radius, radius, paint);
        } else if (flag == 2) {
            // 右边View
            canvas.drawCircle(x - 2 * radius, y - 2 * radius, radius, paint);
        }
        canvas.save();
    }

    public void setIsShowReadTip(boolean flag, int directionFlag) {
        mIsShowReadTip = flag;
        mDirectionFlag = directionFlag;
        invalidate();
    }
}
