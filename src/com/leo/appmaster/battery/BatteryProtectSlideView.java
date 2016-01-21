package com.leo.appmaster.battery;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

/**
 * @author Taolin
 * @date Dec 03, 2013
 * @since v1.0
 */

public class BatteryProtectSlideView extends View {


    private static final int MSG_REDRAW = 1;
    private static final int DRAW_INTERVAL = 50;
    private static final int STEP_LENGTH = 5;

    private Paint mPaint;
    private LinearGradient mGradient;
    private int[] mGradientColors;
    private int mGradientIndex;
    private float mDensity;
    private Matrix mMatrix;

    private String mText;
    private int mTextSize;
    private int mTextLeft;
    private int mTextTop;

    private Bitmap mBitmap;
    private int mBitmapWidth;
    private int mBitmapHeight;

    private int mSlidableLength;
    private int mScreenHeight;
    private int mScreenWidth;

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_REDRAW:
                    mMatrix.setTranslate(mGradientIndex, 0);
                    mGradient.setLocalMatrix(mMatrix);
                    invalidate();
                    mGradientIndex += STEP_LENGTH * mDensity;
                    if (mGradientIndex > mSlidableLength) {
                        mGradientIndex = 0;
                    }
                    mHandler.sendEmptyMessageDelayed(MSG_REDRAW, DRAW_INTERVAL);
                    break;
            }
        }
    };

    public BatteryProtectSlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mDensity = getResources().getDisplayMetrics().density;

        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.SlideView);
        mText = typeArray.getString(R.styleable.SlideView_maskText);
        mTextSize = typeArray.getDimensionPixelSize(R.styleable.SlideView_maskTextSize, R.dimen.mask_text_size);

//        mTextLeft = typeArray.getDimensionPixelSize(R.styleable.SlideView_maskTextMarginLeft, R.dimen.mask_text_margin_left);
//        mTextTop = typeArray.getDimensionPixelSize(R.styleable.SlideView_maskTextMarginTop, R.dimen.mask_text_margin_top);
        mSlidableLength = typeArray.getDimensionPixelSize(R.styleable.SlideView_slidableLength, R.dimen.slidable_length);

        typeArray.recycle();

        mGradientColors = new int[]{Color.argb(255, 120, 120, 120),
                Color.argb(255, 120, 120, 120), Color.argb(255, 255, 255, 255)};

        mGradient = new LinearGradient(0, 0, 100 * mDensity, 0, mGradientColors,
                new float[]{0, 0.7f, 1}, TileMode.MIRROR);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mScreenHeight = wm.getDefaultDisplay().getHeight();
        mScreenWidth = wm.getDefaultDisplay().getWidth();

//        mTextLeft = mScreenWidth / 2 - 200;
//        mTextTop = mScreenHeight - mScreenHeight / 8;

        mGradientIndex = 0;
        mPaint = new Paint();
        mMatrix = new Matrix();
        mPaint.setTextSize(mTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);


        //draw Bitmap
        mBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.bay_arrow_slide);
        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
        LeoLog.d("testMesure", "mBitmapWidth : " + mBitmapWidth);
        LeoLog.d("testMesure", "mBitmapHeight : " + mBitmapHeight);


        mHandler.sendEmptyMessageDelayed(MSG_REDRAW, DRAW_INTERVAL);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LeoLog.d("testMesure", "f height : " + getHeight());
        LeoLog.d("testMesure", "f width : " + getWidth());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setShader(mGradient);
        int centerX = getWidth() / 2;
        int baseline = getHeight() / 2 + mTextSize / 2;
        canvas.drawText(mText, centerX, baseline, mPaint);

//        canvas.drawBitmap(mBitmap, centerX - 150, getHeight() / 2, mPaint);

    }

}
