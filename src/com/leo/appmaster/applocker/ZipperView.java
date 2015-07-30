
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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
    private Bitmap mFZipper=null;
    private Bitmap mFLeft=null;
    private Bitmap mFRight=null;
    private Bitmap mFBg=null;
    private Bitmap mFCowBoy=null;
    private Bitmap mFMask=null;
    private float mScaleW=1.0f;
    private float mScaleH=1.0f;
    private float mCenterX;
    private Paint mSpecialPaint;
    private Canvas mCanvas;
    
    private Bitmap mFBitmap;
    
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
    private OnGestureTooFastListener mTooFast=null;
    private OnGestureTooSlowListener mTooSlow=null;
    private Handler mhandler = new Handler();
    private boolean mIsZipperTouched = false;
    private Paint mPaint;
    
    private Runnable animGoBack = new Runnable()
    {
        public void run()
        {
           
                if(mYp-380<0)
                {
                    mYp=0;
                    ZipperView.this.mhandler.postDelayed(ZipperView.this.animGoBack, 15l);
                    ZipperView.this.invalidate();
                    mhandler.removeCallbacks(animGoBack);
                }
                else
                {
                    mYp -= 380;
                    ZipperView.this.mhandler.postDelayed(ZipperView.this.animGoBack, 15l);
                    ZipperView.this.invalidate();    
                }
                
            }
//           

        
    };



    // private float zipX;
    // private float zipY;

    public interface OnGestureSuccessListener {
        public void OnGestureSuccess();
    }

    public void setOnGestureSuccessListener(OnGestureSuccessListener success) {
        mSuccess = success;
    }
    
    public interface OnGestureTooFastListener
    {
        public void OnGestureTooFast();
    }
    
    public void setOnGestureTooFastListener(OnGestureTooFastListener tooFast)
    {
        mTooFast=tooFast;
    }
    
    public interface OnGestureTooSlowListener
    {
        public void OnGestureTooSlow();
    }
    
    public void setOnGestureTooSlowListener(OnGestureTooSlowListener tooSlow)
    {
        mTooSlow=tooSlow;
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
        mPaint=new Paint();
        mPaint.setAntiAlias(true);        
        mSpecialPaint=new Paint();
        mSpecialPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
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
       
        mWidth = getWidth();
        mCenterX=mWidth/2;
        mHeight = getHeight();
        initScaleBitmap();
     
    }

    private void initScaleBitmap() {
//    
        mScaleH=(float)mHeight/(float)mDrawbleCowBoy.getHeight();
        mScaleW=(float)mWidth/(float)mDrawbleCowBoy.getWidth();
        
        Log.e("zipper", "mScaleH="+mScaleH+",mScaleW="+mScaleW);
        
        if(mFLeft==null)
        {
            Log.e("zipper", "mfleft=null,to create now");
            mFLeft=Bitmap.createScaledBitmap(mLeft, (int)((float)(mLeft.getWidth()*mScaleW)), (int)((float)(mLeft.getHeight()*mScaleH)), true);
        }
        if(mFRight==null)
        {
            Log.e("zipper", "mfleft=null,to create now");
            mFRight=Bitmap.createScaledBitmap(mRight,(int)((float)(mRight.getWidth()*mScaleW)), (int)((float)(mRight.getHeight()*mScaleH)), true);
        }
        if(mFZipper==null)
        {
            Log.e("zipper", "mfleft=null,to create now");
            mFZipper= Bitmap.createScaledBitmap(mZipper, (int)((float)(mZipper.getWidth()*mScaleW*0.8f)), ((int)(float)(mZipper.getHeight()*mScaleH*0.8)), true);
        }
        if(mFCowBoy==null)
        {
            mFCowBoy=Bitmap.createScaledBitmap(mDrawbleCowBoy, mWidth, mHeight, true);
        }
        if(mFMask==null)
        {
            mFMask=Bitmap.createScaledBitmap(mMask, mWidth, mHeight, true);
        }
        mFBitmap=Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        
        
        mCanvas=new Canvas(mFBitmap);
//        this.drawBmp = Bitmap.createBitmap(this.bg.width, this.bg.height, Bitmap.Config.ARGB_8888);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw");
   
        mCanvas.drawBitmap(mFCowBoy, 0, 0, mPaint);
        mCanvas.drawBitmap(mFMask, 0, mYp-mFMask.getHeight(), mSpecialPaint);    
        mCanvas.drawBitmap(mFLeft, -(mFLeft.getWidth()-mCenterX)+(int)(mDisplayMetrics.density*5.5),mYp-mFLeft.getHeight() , mPaint);
        mCanvas.drawBitmap(mFRight, mCenterX-(int)(mDisplayMetrics.density*7.5), mYp-mFRight.getHeight(), mPaint);
        mCanvas.drawBitmap(mFZipper, mCenterX-mFZipper.getWidth()/2-(int)(mDisplayMetrics.density*1.5), (int)(mYp-20.0*mScaleH), mPaint);
        
        canvas.drawBitmap(mFBitmap, 0, 0, mPaint);
        
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
//                        
                        mYp=Math.min(y, mFMask.getHeight());//解决虚拟键盘时mask露出的bug
                        Log.i("zipper", "mFMask.height=="+mFMask.getHeight()+"and now mYp=="+mYp);
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
                        if (event.getY() >0)
                        {
                            if (event.getY()/durationTime*1000 < mHeight*1.5)
                            {
                                if(mTooSlow!=null)
                                {
                                    mTooSlow.OnGestureTooSlow();                                    
                                }
//                                Toast.makeText(getContext(), mContext.getResources().getString(R.string.zipper_too_slow), 0).show();
                            }
                            else
                            {
                                if(mTooFast!=null)
                                {
                                    mTooFast.OnGestureTooFast();                                    
                                }
//                                Toast.makeText(getContext(), mContext.getResources().getString(R.string.zipper_too_fast), 0).show();
                            }
                            this.mhandler.postDelayed(animGoBack, 1l);

                        }

                    }
                    mIsZipperTouched = false;
                    isGesture = false;
                }
                    break;
                case MotionEvent.ACTION_DOWN: {
                    // 4是让识别区域变大点
                    if (event.getX() > (mWidth - mFZipper.getWidth() - 0) / 2
                            && event.getX() < (mWidth + mFZipper.getWidth() + 0) / 2
                            && event.getY() > mYp - 0
                            && event.getY() < mYp + mFZipper.getHeight() + 0)
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

                    if ( (px01 <= px02 && ax01 > px01 + 50 && ax02 < px02 - 50)                   //01点在左，则需要抬起时01点往右移了30，02点往左移了30
                            || (px01 > px02&& ax01 < px01 - 50 && ax02 > px02 + 50)              //或者 01点在右，则需要抬起时01点往左移了30，02点往右移了30
                            ||(py01 <= py02 && ay01 > py01 + 50&&ay02<py02-50)                  //或者 01点在上，则需要抬起时01点往下移了30，02点往上移了30   
                            ||(py01>py02&&ay01<py01-50&&ay02>py02+50))                                      
                    {
                        if(mSuccess!=null)
                        {
                            mSuccess.OnGestureSuccess();                          
                        }
                        // Toast.makeText(getContext(), "缩放动作判定为成功", 0).show();
                    }

                    break;
                case MotionEvent.ACTION_POINTER_2_UP:
                    float aax01 = event.getX(0);
                    float aay01 = event.getY(0);
                    float aax02 = event.getX(1);
                    float aay02 = event.getY(1);

                    Log.e("poha", "ax01:" + aax01);
                    Log.e("poha", "ay01:" + aay01);
                    Log.e("poha", "ax02:" + aax02);
                    Log.e("poha", "ay02:" + aay02);

                    if ( (px01 <= px02 && aax01 > px01 + 50 && aax02 < px02 - 50)                   //01点在左，则需要抬起时01点往右移了30，02点往左移了30
                            || (px01 > px02&& aax01 < px01 - 50 && aax02 > px02 + 50)              //或者 01点在右，则需要抬起时01点往左移了30，02点往右移了30
                            ||(py01 <= py02 && aay01 > py01 + 50&&aay02<py02-50)                  //或者 01点在上，则需要抬起时01点往下移了30，02点往上移了30   
                            ||(py01>py02&&aay01<py01-50&&aay02>py02+50))                                       
                    {
                        // Toast.makeText(getContext(), "缩放动作判定为成功", 0).show();
                        mSuccess.OnGestureSuccess();
                    }

                    break;         

                default:
                    break;
            }
            return true;

        }

        return super.onTouchEvent(event);
    }


}
