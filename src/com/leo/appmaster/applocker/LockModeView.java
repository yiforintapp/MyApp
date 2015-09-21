
package com.leo.appmaster.applocker;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.leo.appmaster.AppMasterPreference;
import com.leo.appmaster.R;
import com.leo.appmaster.applocker.manager.LockManager;
import com.leo.appmaster.applocker.model.LockMode;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.sdk.SDKWrapper;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.Utilities;

public class LockModeView extends View {

    private final static float LOCK_COUNT_TEXT_POS_PERCENT = 0.22f;
    private final static float LOCK_MODE_TEXT_POS_PERCENT = 0.82f;
    private final static float TEXT_WIDTH_PERCENT = 0.4f;

    private Paint mPaint;
    private FontMetrics mFontMetrics = new FontMetrics();

    private Drawable mBgIcon, mMaskIcon, mMoveIcon;

    private int mDrawPadding;
    private int mTextPadding;

    private Rect mBgIconDrawBount = new Rect();
    private Rect mMoveIconDrawBount = new Rect();
    private int mMoveIconY;
    private int mMoveIconTop;
    private int mMoveIconBottom;
    private int mGap;

    private int mLockCountTextSize;
    private int mTipTextSize;
    private int mLockModeTextSize;

    private String mCountText;
    private String mModeText;
    private String mTipText;

    private Handler mHandler;
    private boolean mAnimate;

    private int mDatalY;
    private int mRefreshRate = 10;
    private int mDuration = 1500;

    private int mMoveIconHeight;

    private boolean pressLockFlag;
    private boolean pressModeNameFlag;
    
    public LockModeView(Context context) {
        this(context, null);
    }

    public LockModeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockModeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources res = getResources();

        mDrawPadding = res.getDimensionPixelSize(R.dimen.privacy_level_padding);
        mTextPadding = res.getDimensionPixelSize(R.dimen.home_lock_text_padding);
        mLockCountTextSize = res.getDimensionPixelSize(R.dimen.home_lock_count_text_size);
        mTipTextSize = res.getDimensionPixelSize(R.dimen.home_lock_tip_size);
        mLockModeTextSize = res.getDimensionPixelSize(R.dimen.home_lock_mode_name_size);
        mGap = DipPixelUtil.dip2px(context, 18);

        mTipText = res.getString(R.string.aready_lock);

        mBgIcon = res.getDrawable(R.drawable.mode_bar_bg);
        mMaskIcon = res.getDrawable(R.drawable.mode_bar_bg_mask);
        mMoveIcon = res.getDrawable(R.drawable.mode_bar_move);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (mAnimate) {
                    invalidate();
                    mHandler.sendEmptyMessageDelayed(0, mRefreshRate);
                }
                super.handleMessage(msg);
            }

        };
    }

    public void startAnimation() {
        if (mAnimate == false) {
            mAnimate = true;
            mHandler.sendEmptyMessage(0);
        }
    }

    public void stopAnimation() {
        if (mAnimate) {
            mAnimate = false;
            mHandler.removeMessages(0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        computeDrawBounds(right - left, bottom - top);
    }

    private void computeDrawBounds(int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2;
        int contentW = width - 2 * mDrawPadding;
        int contentH = height - 2 * mDrawPadding;

        // compute bg rect
        int iconW = mBgIcon.getIntrinsicWidth();
        int iconH = mBgIcon.getIntrinsicHeight();
        float scaleW = (float) contentW / iconW;
        float scaleH = (float) contentH / iconH;
        float scale = scaleW < scaleH ? scaleW : scaleH;
        int drawW = (int) (iconW * scale);
        int drawH = (int) (iconH * scale);
        mBgIconDrawBount.left = centerX - drawW / 2;
        mBgIconDrawBount.right = mBgIconDrawBount.left + drawW;
        mBgIconDrawBount.top = centerY - drawH / 2;
        mBgIconDrawBount.bottom = mBgIconDrawBount.top + drawH;
        
        // cpmpute move icon rect
        mMoveIconY = centerY + drawH / 2;
        iconW = mMoveIcon.getIntrinsicWidth();
        iconH = mMoveIcon.getIntrinsicHeight();
        drawW = (int) (iconW * scale);
        mMoveIconHeight = (int) (iconH * scale);

        mGap *= scale;
        mMoveIconTop = (mBgIconDrawBount.top + mGap - drawH);
        mMoveIconBottom = mBgIconDrawBount.bottom - mGap;

        mMoveIconY = mMoveIconBottom;
        mMoveIconDrawBount.left = centerX - drawW / 2;
        mMoveIconDrawBount.right = mMoveIconDrawBount.left + drawW;
        mMoveIconDrawBount.top = mMoveIconY;
        mMoveIconDrawBount.bottom = mMoveIconDrawBount.top + mMoveIconHeight;
        mDatalY = (mMoveIconBottom - mMoveIconTop) / (mDuration / mRefreshRate);

    }

    @Override
    public void draw(Canvas canvas) {
        // bg icon
        mBgIcon.setBounds(mBgIconDrawBount);
        mBgIcon.draw(canvas);

        mMoveIconY -= mDatalY;
        if (mMoveIconY < mMoveIconTop) {
            mMoveIconY = mMoveIconBottom;
        }
        mMoveIconDrawBount.top = mMoveIconY;
        mMoveIconDrawBount.bottom = mMoveIconDrawBount.top + mMoveIconHeight;
        // move icon
        mMoveIcon.setBounds(mMoveIconDrawBount);
        mMoveIcon.draw(canvas);

        // mask icon
        mMaskIcon.setBounds(mBgIconDrawBount);
        mMaskIcon.draw(canvas);

        int drawW = mBgIconDrawBount.width();
        int drawH = mBgIconDrawBount.height();
        int maxTextWidth = (int) (drawW * TEXT_WIDTH_PERCENT);
        int countBottom = 0;
        int textWidth, realSize;
        // count text
        if (!Utilities.isEmpty(mCountText)) {
            int centerY = mBgIconDrawBount.top + drawH / 2;
            textWidth = computeTextSize(mCountText, mLockCountTextSize, maxTextWidth, mPaint);
            realSize = (int) mPaint.getTextSize();
            mPaint.getFontMetrics(mFontMetrics);
            int offset = (int) Math.abs(mFontMetrics.ascent) - 2;
            countBottom = mBgIconDrawBount.top + ((int) (drawH * LOCK_COUNT_TEXT_POS_PERCENT))
                    + offset;
            if (countBottom > centerY) {
                countBottom = centerY;
            }
            mPaint.setTextSize(realSize);
            mPaint.setStyle(Style.FILL);
            mPaint.setColor(Color.WHITE);
            canvas.drawText(mCountText, mBgIconDrawBount.left + (drawW - textWidth) / 2,
                    countBottom,
                    mPaint);
            if(pressLockFlag){
                //draw the press img
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mode_press_lock_bg);
                canvas.drawBitmap(bitmap, mBgIconDrawBount.left + (drawW - bitmap.getWidth()) / 2, mBgIconDrawBount.top + ((int) (drawH * LOCK_COUNT_TEXT_POS_PERCENT))-(bitmap.getWidth()-offset)/2, null);
            }
        }

        // tip text
        textWidth = computeTextSize(mTipText, mTipTextSize, maxTextWidth, mPaint);
        realSize = (int) mPaint.getTextSize();
        mPaint.getFontMetrics(mFontMetrics);
        int offset = (int) Math.ceil(mFontMetrics.descent - mFontMetrics.ascent) + 1;
        mPaint.setTextSize(realSize);
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawText(mTipText, mBgIconDrawBount.left + (drawW - textWidth) / 2, countBottom
                + mTextPadding + offset, mPaint);

        // mode text
        if (!Utilities.isEmpty(mModeText)) {
            mPaint.setTextSize(mLockModeTextSize);
            textWidth = (int) mPaint.measureText(mModeText);
            if(textWidth > maxTextWidth){
                mModeText = subStringModetext(mModeText,mLockModeTextSize,maxTextWidth,mPaint);
                textWidth = (int) mPaint.measureText(mModeText);
            }
            realSize = (int) mPaint.getTextSize();
            mPaint.setTextSize(realSize);
            mPaint.setStyle(Style.FILL);
            mPaint.setColor(Color.WHITE);
            mPaint.getFontMetrics(mFontMetrics);
            int textHeight = (int) Math.ceil(mFontMetrics.descent - mFontMetrics.ascent);
            canvas.drawText(mModeText, mBgIconDrawBount.left + (drawW - textWidth) / 2,
                    mBgIconDrawBount.top + (int) (drawH * LOCK_MODE_TEXT_POS_PERCENT), mPaint);
            
            if(pressModeNameFlag){
              //draw the press img
                Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.mode_press_bg);
                Rect src = new Rect();
                src.left = src.top =0;
                src.right = bitmap2.getWidth();
                src.bottom = src.top + bitmap2.getHeight();
                Rect dst = new Rect();
                dst.left = mBgIconDrawBount.left + (drawW - bitmap2.getWidth()) / 2;
                dst.top = mBgIconDrawBount.top + (int) (drawH * LOCK_MODE_TEXT_POS_PERCENT)-(DipPixelUtil.dip2px(mContext, 70) - textHeight)/2-DipPixelUtil.dip2px(mContext, 10);
                dst.right = dst.left +bitmap2.getWidth();
                dst.bottom = dst.top +DipPixelUtil.dip2px(mContext, 70);
                canvas.drawBitmap(bitmap2, src, dst, null);
            }
        }
    }

    /**
     * use to cut off the over width mode name,and contact "..." 
     * @param text
     * @param maxTextSize
     * @param maxWidth
     * @param paint
     * @return
     */
    private String subStringModetext(String text, int maxTextSize, int maxWidth, Paint paint) {
        int textSize = maxTextSize,textLength;
        int region = 0;
        String omission = "...";
        paint.setTextSize(textSize);
        text.concat(omission);
        int textWidth = (int) paint.measureText(text);
        while (textWidth > maxWidth) {
            textLength = text.length();
            region = textLength - 4;
            if(region < 1) {
                region = 1;
            }
            text = text.substring(0, region);
            text = text.concat(omission);
            textWidth = (int) paint.measureText(text);
            Log.i("tag",text);
        }
       /* int textSize = maxTextSize,textLength,omissionWidth,textWidth,maxLength;
        String omission = "...";
        paint.setTextSize(textSize);
        textLength = text.length();
        
        omissionWidth = (int) paint.measureText(omission);
        maxWidth = maxWidth -omissionWidth;
        textWidth  = (int) paint.measureText(text);
        maxLength = (maxWidth*textLength)/textWidth;
        Log.i("tag","maxWidth = "+maxWidth +"  textWidth = "+textWidth+ " textLength = "+textLength);
        
        text = text.substring(0, maxLength);
        textWidth = (int) paint.measureText(text.concat(omission));
        if(textWidth > maxWidth){
            text = text.substring(0, maxLength-2);
        }
        text = text.concat(omission);
        Log.i("tag","text : ="+text +" maxLength = "+maxLength);*/
        return text;
    }
    
    private int computeTextSize(String text, int maxTextSize, int maxWidth, Paint paint) {
        int textSize = maxTextSize;
        paint.setTextSize(textSize);
        int textWidth = (int) paint.measureText(text);
        while (textWidth > maxWidth) {
            textSize = textSize - 1;
            paint.setTextSize(textSize);
            textWidth = (int) paint.measureText(text);
        }
        return textWidth;
    }

    public void updateMode(String mode, String count) {
        mModeText = mode;
        mCountText = count;
        invalidate(mBgIconDrawBount);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int upX = (int) event.getX();
        int upY = (int) event.getY();

        int dy = mBgIconDrawBount.top +  (mBgIconDrawBount.bottom - mBgIconDrawBount.top)
                * 2 / 3;
        Rect top = new Rect(mBgIconDrawBount.left, mBgIconDrawBount.top,
                mBgIconDrawBount.right, dy);
        Rect bottom = new Rect(mBgIconDrawBount.left, dy,
                mBgIconDrawBount.right, mBgIconDrawBount.bottom);
        
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                    if (top.contains(upX, upY)) {
                        this.setScaleX(0.98f);
                        this.setScaleY(0.98f);
                        pressLockFlag = true;
                        invalidate();
                    }else if(bottom.contains(upX, upY)){
                        this.setScaleX(0.98f);
                        this.setScaleY(0.98f);
                        pressModeNameFlag = true;
                        invalidate();
                    }
                    break;
            case MotionEvent.ACTION_CANCEL:
                    this.setScaleX(1.0f);
                    this.setScaleY(1.0f);
                    pressLockFlag = false;
                    pressModeNameFlag = false;
                    invalidate();
                break;
            case MotionEvent.ACTION_UP:
                this.setScaleX(1.0f);
                this.setScaleY(1.0f);
                pressLockFlag = false;
                pressModeNameFlag = false;
                invalidate();
                Context ctx = getContext();
                if (top.contains(upX, upY)) {
                    // to lock list
                    SDKWrapper.addEvent(getContext(), SDKWrapper.P1, "home", "lock_ball");
                    LockManager lm = LockManager.getInstatnce();
                    LockMode curMode = lm.getCurLockMode();
                    if (curMode != null && curMode.defaultFlag == 1 && !curMode.haveEverOpened) {
                        Intent intent = new Intent(ctx, RecommentAppLockListActivity.class);
                        intent.putExtra("target", 0);
                        ctx.startActivity(intent);
                        
                        AppMasterPreference.getInstance(ctx).setIsClockToLockList(true);
                        curMode.haveEverOpened = true;
                        lm.updateMode(curMode);
                    } else {
                        Intent intent = null;
                        intent = new Intent(ctx, AppLockListActivity.class);
                        ctx.startActivity(intent);
                        AppMasterPreference.getInstance(ctx).setIsClockToLockList(true);
                    }
                } else if (bottom.contains(upX, upY)) {
                    // to lock mode
                    SDKWrapper.addEvent(ctx, SDKWrapper.P1, "home", "changemode");
                    ((HomeActivity) ctx).showModePages(true);
                }
                break;
        }
        return true;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {
        }
    }

}
