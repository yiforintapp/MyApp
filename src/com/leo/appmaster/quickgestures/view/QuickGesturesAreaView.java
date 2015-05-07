
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
/**
 * QuickGesturesAreaView
 * @author run
 *
 */
public class QuickGesturesAreaView extends ViewGroup {
    public static int viewWidth;
    public static int viewHeight;
    private Context mContext;
    private ImageView[] mSlidingAreaViews = new ImageView[6];
    private boolean mLeftBottomFlag = true;
    private boolean mLeftCenterFlag = false;
    private boolean mRightBottomFlag = true;
    private boolean mRightCenterFlag = false;

    public QuickGesturesAreaView(Context context) {
        super(context);
        initUI(context);
        initChildView();
    }

    public QuickGesturesAreaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initUI(context);
        initChildView();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @SuppressLint("ResourceAsColor")
    private void initUI(Context mContext) {
        this.mContext = mContext;
        setBackgroundResource(R.color.quick_gesture_switch_setting_hidden_color);
        setFocusable(true);
        setClickable(true);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
//        float leftX = 0;
//        float leftY = 0;
//        int cWidth = 100;
//        int cHeight = 200;
//        WindowManager manager = (WindowManager) mContext
//                .getSystemService(Context.WINDOW_SERVICE);
//        Display display = manager.getDefaultDisplay();
//        int height = display.getHeight();
//        int width = display.getWidth();
//        // 左边
//        // 左下
//        if (mLeftBottomFlag) {
//            getChildAt(0).layout((int) leftX, (int) height - cHeight - cHeight - cHeight,
//                    (int) cWidth,
//                    (int) height - cHeight - cHeight);
//            getChildAt(1).layout((int) leftX, (int) height - cHeight - cHeight, (int) cWidth,
//                    (int) height - cHeight);
//
//            getChildAt(2).layout((int) leftX, (int) height - cHeight,
//                    (int) cWidth,
//                    (int) height);
//        }
//        // 左中
//        if (mLeftCenterFlag) {
//            getChildAt(1).layout((int) leftX, (int) height - cHeight - cHeight - cHeight,
//                    (int) cWidth,
//                    (int) height - cHeight);
//        }
//        // 右边
//        // 右下
//        if (mRightBottomFlag) {
//            getChildAt(3).layout((int) width - cWidth, (int) height - cHeight - cHeight - cHeight,
//                    (int) width, (int) height - cHeight - cHeight);
//
//            getChildAt(4).layout((int) width - cWidth, (int) height - cHeight - cHeight,
//                    (int) width,
//                    (int) height - cHeight);
//
//            getChildAt(5).layout((int) width - cWidth, (int) height - cHeight, (int) width,
//                    (int) height);
//        }
//        // 右中
//        if (mRightCenterFlag) {
//            getChildAt(4).layout((int) width - cWidth, (int) height - cHeight - cHeight - cHeight,
//                    (int) width,
//                    (int) height - cHeight);
//
//        }

    }

    @SuppressWarnings("deprecation")
    private void initChildView() {
        int lenght = mSlidingAreaViews.length;
        int width = 100;
        int height = 500;
        for (int i = 0; i < lenght; i++) {
            mSlidingAreaViews[i] = new ImageView(mContext);
            mSlidingAreaViews[i].setScaleType(ImageView.ScaleType.CENTER);
            mSlidingAreaViews[i].setLayoutParams(new LinearLayout.LayoutParams((int) (width),
                    height));
            mSlidingAreaViews[i].setBackgroundResource(R.drawable.test);
            addView(mSlidingAreaViews[i]);
        }
    }

    // 设置显示左边底部
    public void setIsShowLeftBottom(boolean flag) {
        mLeftBottomFlag = flag;
        invalidate();
    }

    // 设置显示左边中部
    public void setIsShowLeftCenter(boolean flag) {
        mLeftCenterFlag = flag;
        invalidate();
    }

    // 设置显示右边底部
    public void setIsShowRightBottom(boolean flag) {
        mRightBottomFlag = flag;
        invalidate();
    }

    // 设置显示右边中部
    public void setIsShowRightCenter(boolean flag) {
        mRightCenterFlag = flag;
        invalidate();
    }
}
