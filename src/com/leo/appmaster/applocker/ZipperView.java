
package com.leo.appmaster.applocker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.leo.appmaster.R;

public class ZipperView extends View {
    private static final String TAG = "LalianView";
    private Bitmap mDrawbleBg;
    private Bitmap mDrawbleCowBoy;
    private Bitmap mZipper;
    private Bitmap mLeft;
    private Bitmap mRight;
    private Bitmap mMask;
    private int mWidth;
    private int mHeight;
    private float mYp = 0;
    private Path mPath;
    private long downtime = 0;
    private int[] bgbyte;
    private int[] cowbyte;
    private int[] newbg;
    private Context mContext;
    private DisplayMetrics mDisplayMetrics;
    private OnGestureSuccessListener mSuccess = null;
    private Handler mhandler = new Handler();
    private boolean mIsZipperTouched = false;
    
    
    private Runnable animGoBack = new Runnable()
    {
        public void run()
        {
            if (mYp > 0)
            {
                mYp -= 100;
                ZipperView.this.mhandler.postDelayed(ZipperView.this.animGoBack, 15l);
                ZipperView.this.invalidate();
            }
            else
            {
                mYp = 0;
                ZipperView.this.invalidate();
                mhandler.removeCallbacks(animGoBack);
            }

        }
    };



    // private float zipX;
    // private float zipY;

    public interface OnGestureSuccessListener {
        public void OnGestureSuccess();
    }

    public void setOnGestureSuccessListener(OnGestureSuccessListener success) {
        mSuccess = success;
    }

    private boolean zipperBeTouched;
    private boolean isGesture;
    private float px01;
    private float py01;
    private float px02;
    private float py02;

    public ZipperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        init();
    }

    private void init() {
        mMask=BitmapFactory.decodeResource(getResources(), R.drawable.zipper_bg_mask);
        mDrawbleBg = BitmapFactory.decodeResource(getResources(), R.drawable.bg_beauty);
        mDrawbleCowBoy = BitmapFactory.decodeResource(getResources(), R.drawable.bg_cowboy);
        mZipper = BitmapFactory.decodeResource(getResources(), R.drawable.beauty_zipper);
        mLeft = BitmapFactory.decodeResource(getResources(), R.drawable.beauty_zipper_left);
        mRight = BitmapFactory.decodeResource(getResources(), R.drawable.beauty_zipper_right);
        mDisplayMetrics = mContext.getResources().getDisplayMetrics();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getWidth();
        mHeight = getHeight();

        getBgByte();
        getCowByte();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw");
   

   
        drawCowboyandBeaty(canvas);
        drawzipper(canvas);
        drawleft(canvas);
        drawright(canvas);

      
    }

    private void drawleft(Canvas canvas) {
        Bitmap left = Bitmap.createScaledBitmap(mLeft, mWidth/2, mHeight, false);
   
        
        
//        Bitmap left = Bitmap.createBitmap(mLeft);

//        canvas.drawBitmap(left, 0, mYp - mLeft.getHeight(), new Paint());
        canvas.drawBitmap(left, 0, mYp - left.getHeight(), new Paint());
    }

    private void drawright(Canvas canvas) {
         Bitmap right = Bitmap.createScaledBitmap(mRight, mWidth/2, mHeight, false);
//        Bitmap right = Bitmap.createBitmap(mRight);
        // canvas.drawBitmap(right, right.getWidth(), mYp-right.getHeight(), new
        // Paint());
//        canvas.drawBitmap(right, right.getWidth(), mYp - right.getHeight(), new Paint());
        canvas.drawBitmap(right, right.getWidth(), mYp - right.getHeight(), new Paint());
        
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int pointerCount = event.getPointerCount();

        if (pointerCount == 1)
        {
            float x = event.getX();
            float y = event.getY();
         

            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE: {
                    if (mIsZipperTouched)
                    // if(event.getX()>(mWidth-mZipper.getWidth())/2&&event.getX()<(mWidth+mZipper.getWidth())/2&&event.getY()>mYp&&event.getY()<mYp+mZipper.getHeight())
                    {
                        mYp = y;
                        invalidate();

                    }
                    // Log.i(TAG, ""+mYp);
                }
                    break;
                case MotionEvent.ACTION_UP: {
                    // if(event.getX()>(mWidth-mZipper.getWidth())/2&&event.getX()<(mWidth+mZipper.getWidth())/2&&event.getY()>mYp&&event.getY()<mYp+mZipper.getHeight())
                    if (mIsZipperTouched)
                    {
                        long durationTime = System.currentTimeMillis() - downtime;
                        Log.e("poha", durationTime + "");
                        if (event.getY() >= mHeight - mZipper.getHeight())
                        {
                            if (durationTime > 1000)
                            {
                                Toast.makeText(getContext(), "太慢啦！", 0).show();
                            }
                            else
                            {
                                Toast.makeText(getContext(), "太快啦！", 0).show();
                            }
                            this.mhandler.postDelayed(animGoBack, 1l);

                        }

                    }
                    else
                    {
                        // float aftX = event.getX();
                        // float aftY = event.getY();

                    }
                    mIsZipperTouched = false;
                    isGesture = false;
                }
                    break;
                case MotionEvent.ACTION_DOWN: {
                    // 4是让识别区域变大点
                    if (event.getX() > (mWidth - mZipper.getWidth() - 8) / 2
                            && event.getX() < (mWidth + mZipper.getWidth() + 8) / 2
                            && event.getY() > mYp - 8
                            && event.getY() < mYp + mZipper.getHeight() + 8)
                    {
                        downtime = System.currentTimeMillis();
                        mIsZipperTouched = true;
                    }
                    else
                    {
                        isGesture = true;

                        float preX = event.getX();
                        float preY = event.getY();

                    }
                    // mYp = y;
                    // postInvalidate();
                    //
                    // Log.i(TAG, ""+mYp);
                    // Log.e("poha", downtime+"");
                }

                default:
                    break;
            }
            return true;
        }
        else if (pointerCount == 2)
        {
            Log.e("poha", "两个触摸点");

            // int id1 = event.getPointerId(0);
            // int di2 = event.getPointerId(1);
            switch (event.getAction()) {
                case MotionEvent.ACTION_POINTER_2_DOWN:

                    Log.e("poha", "两个点的进入down");

                    px01 = event.getX(0);
                    py01 = event.getY(0);
                    px02 = event.getX(1);
                    py02 = event.getY(1);

                    Log.e("poha", "px01:" + px01);
                    Log.e("poha", "py01:" + py01);
                    Log.e("poha", "px02:" + px02);
                    Log.e("poha", "py02:" + py02);
                    break;
                case MotionEvent.ACTION_POINTER_1_UP:

                    float ax01 = event.getX(0);
                    float ay01 = event.getY(0);
                    float ax02 = event.getX(1);
                    float ay02 = event.getY(1);

                    Log.e("poha", "ax01:" + ax01);
                    Log.e("poha", "ay01:" + ay01);
                    Log.e("poha", "ax02:" + ax02);
                    Log.e("poha", "ay02:" + ay02);

                   
                  

                    if (((py01 <= py02 && ay01 > py01 + 70 && ay02 < py02 - 70) || (py01 > py02
                            && ay01 < py01 - 70 && ay02 > py02 + 70))
                            && ((px01 <= px02 && ax01 > px01 + 50 && ax02 < px02 - 50) || (px01 > px02
                                    && ax01 < px01 - 50 && ax02 > px02 + 50)))//
                    {
                        // Toast.makeText(getContext(), "缩放动作判定为成功", 0).show();
                        mSuccess.OnGestureSuccess();
                    }

                    break;
                case MotionEvent.ACTION_POINTER_2_UP:
                    float ax0100 = event.getX(0);
                    float ay0100 = event.getY(0);
                    float ax0200 = event.getX(1);
                    float ay0200 = event.getY(1);

                    Log.e("poha", "ax0100:" + ax0100);
                    Log.e("poha", "ay0100:" + ay0100);
                    Log.e("poha", "ax0200:" + ax0200);
                    Log.e("poha", "ay0200:" + ay0200);

                    // float theTop=(py01>=py02)?py01:py02;
                    // float thebottom=(py01<py02)?py01:py02;
                    //
                    // flo

                    if (((py01 <= py02 && ay0100 > py01 + 10 && ay0200 < py02 - 10) || (py01 > py02
                            && ay0100 < py01 - 10 && ay0200 > py02 + 10))
                            && ((px01 <= px02 && ax0100 > px01 + 10 && ax0200 < px02 - 10) || (px01 > px02
                                    && ax0100 < px01 - 10 && ax0200 > px02 + 10)))
                    {
                        // Toast.makeText(getContext(), "缩放动作判定为成功", 0).show();
                        mSuccess.OnGestureSuccess();
                    }

                default:
                    break;
            }
            return true;

        }

        return super.onTouchEvent(event);
    }

    private void refresh() {

        // postInvalidate();
        // SystemClock.sleep(100);
        // Log.e("poha", "refresh!");

        //
        //
    }

    private void drawzipper(Canvas canvas) {
        Log.e("poha", "drawing...,y is :" + mYp);
        Bitmap zipperbitmap = Bitmap.createScaledBitmap(mZipper, 80, 250, false);
        canvas.drawBitmap(mZipper, (mWidth - mZipper.getWidth()) / 2, mYp, new Paint());
    }

    private void drawCowboy(Canvas canvas) {
//        int bmwidth = mDrawbleCowBoy.getWidth();
//        int bmheight = mDrawbleCowBoy.getHeight();
//        float scalewidth = Float.intBitsToFloat(mWidth) / Float.intBitsToFloat(bmwidth);
//        float scaleheight = Float.intBitsToFloat(mHeight) / Float.intBitsToFloat(bmheight);
//        Matrix mBgMatrix;
//        mBgMatrix = new Matrix();
//        mBgMatrix.postScale(scalewidth, scaleheight);
        // canvas.drawBitmap(mDrawbleCowBoy,mBgMatrix,new Paint());
        canvas.drawBitmap(mDrawbleCowBoy, 0, 0, new Paint());
    }

    private void drawbg(Canvas canvas) {
//        int bmwidth = mDrawbleBg.getWidth();
//        int bmheight = mDrawbleBg.getHeight();
//        float scalewidth = Float.intBitsToFloat(mWidth) / Float.intBitsToFloat(bmwidth);
//        float scaleheight = Float.intBitsToFloat(mHeight) / Float.intBitsToFloat(bmheight);
//        Matrix mBgMatrix;
//        mBgMatrix = new Matrix();
//        mBgMatrix.postScale(scalewidth, scaleheight);
        // canvas.drawBitmap(mDrawbleBg,mBgMatrix,new Paint());
        canvas.drawBitmap(mDrawbleBg, 0, 0, new Paint());
    }

    // cowbyte newbg 被赋值 = new int[mWidth * mHeight]
    private void getCowByte() {
        int bmwidth = mDrawbleCowBoy.getWidth();
        int bmheight = mDrawbleCowBoy.getHeight();
        float scalewidth = Float.intBitsToFloat(mWidth) / Float.intBitsToFloat(bmwidth);
        float scaleheight = Float.intBitsToFloat(mHeight) / Float.intBitsToFloat(bmheight);
        Matrix mBgMatrix;
        mBgMatrix = new Matrix();
        mBgMatrix.setScale(scalewidth, scaleheight);

        Log.i(TAG, "mWidth = " + mWidth + ":mHeight = " + mHeight);
        Bitmap bmp = Bitmap.createBitmap(mDrawbleCowBoy, 0, 0, bmwidth, bmheight, mBgMatrix, true);
        cowbyte = new int[mWidth * mHeight];
        bmp.getPixels(cowbyte, 0, mWidth, 0, 0, mWidth, mHeight);
        newbg = new int[mWidth * mHeight];
    }

    // bgbyte被赋值 bgbyte = new int[mWidth * mHeight];
    private void getBgByte() {

        int bmwidth = mDrawbleBg.getWidth();
        int bmheight = mDrawbleBg.getHeight();

        float scalewidth = Float.intBitsToFloat(mWidth) / Float.intBitsToFloat(bmwidth);
        float scaleheight = Float.intBitsToFloat(mHeight) / Float.intBitsToFloat(bmheight);
        Matrix mBgMatrix;
        mBgMatrix = new Matrix();
        mBgMatrix.setScale(scalewidth, scaleheight);

        // Log.i(TAG, "scalewidth = "+scalewidth+":scaleheight = "+scaleheight);
        Bitmap bmp = Bitmap.createBitmap(mDrawbleBg, 0, 0, bmwidth, bmheight, mBgMatrix, true);
        bgbyte = new int[mWidth * mHeight];
        bmp.getPixels(bgbyte, 0, mWidth, 0, 0, mWidth, mHeight);
    }

    private void drawCowboyandBeaty(Canvas canvas) {
        // 这里选择相应的三角形即可
        // for (int i = 0; i < cowbyte.length; i++) {
        // newbg[i] = cowbyte[i];
        // }
        newbg = cowbyte.clone();
        if (mYp > mHeight)
        {
            mYp = mHeight;
        }
        for (int i = 0; i < mYp; i++) {// 高度
            int tempx = (int) (mWidth / 2 * (mYp - i) / mHeight);
            int lx = mWidth / 2 - tempx;
            int rx = mWidth / 2 + tempx;
            for (int j = lx; j <= rx; j++) {
                int tempi = i * mWidth + j;
                newbg[tempi] = bgbyte[tempi];
            }
        }

        Bitmap bmp = Bitmap.createBitmap(newbg, mWidth, mHeight, Config.ARGB_8888);
        canvas.drawBitmap(bmp, 0, 0, new Paint());
    }
    
    
}
