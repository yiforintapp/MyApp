
package com.leo.appmaster.privacy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.leo.appmaster.R;
import com.leo.appmaster.privacy.PrivacyHelper.Level;

public class PrivacyLevelView extends View {
    private final static int SCANNING_ANIM_DURATION = 3200;
    private final static int SCANNING_ONE_DRAW = SCANNING_ANIM_DURATION / 4;

    private final static int LOCK_ANIM_DURATION = 800;

    private final static float SMALL_TEXT_POS_PERCENT = 0.27f;
    private final static float SMALL_TEXT_WIDTH_PERCENT = 0.25f;
    private final static float BIG_TEXT_WIDTH_PERCENT = 0.56f;
    private final static float SEPRATOR_WIDTH_PERCENT = 0.4f;

    private Paint mPaint;
    private FontMetrics mFontMetrics = new FontMetrics();

    private Drawable mIcon;
    private Bitmap mAnim;
    private Drawable mSeprator;

    private int mSepratorPadding;
    private int mDrawPadding;

    private Rect mIconDrawBount = new Rect();
    private Rect mLastIconDrawBount = new Rect();
    private Rect mSepratorBound = new Rect();

    private Point mSmallTextPoint = new Point();
    private Point mBigTextPoint = new Point();

    private int mSmallTextSize;
    private int mBigTextSize;

    private int mRealSmallTextSize;
    private int mRealBigTextSize;

    private String mLevelText;
    private Level mLastLevel;
    private int mColor = -1;

    private ValueAnimator mAnimator;
    private int mAlpha = 255;
    protected boolean mIsAnimating;

    private ScanningListener mScanningListener;
    private ValueAnimator mScanAnimator;
    private Drawable mScanning;
    private Drawable mScanningAppIcon;
    private Drawable mScanningPicIcon;
    private Drawable mScanningVideoIcon;
    private Drawable mScanningContactIcon;
    private Rect mScanningIconRect = new Rect();
    private Rect mScanningIconCenterRect = new Rect();
    private Rect mScanningIconStartRect = new Rect();
    private Rect mScanningIconEndRect = new Rect();
    private Drawable mScanningItem;
    private int mScanningItemAlpha;
    private int mScanningOffset;
    private float mScanDegree;
    private boolean mScannAnimating;
    private boolean mScanningCancel = false;

    public interface ScanningListener {
        public void onScanningFinish();
    }

    public PrivacyLevelView(Context context) {
        this(context, null);
    }

    public PrivacyLevelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrivacyLevelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Resources res = getResources();

        mDrawPadding = getDrawPadding(res);
        mSepratorPadding = getSepratorPadding(res);
        mSmallTextSize = getSmallTextSize(res);
        mBigTextSize = getBigTextSize(res);
        mScanningOffset = res.getDimensionPixelSize(R.dimen.privacy_scanning_offset);

        mLevelText = res.getString(R.string.privacy_level);

        mIcon = res.getDrawable(R.drawable.privacy_level_bg);
        mAnim = BitmapFactory.decodeResource(res, R.drawable.privacy_level_bg_anim);
        mSeprator = res.getDrawable(R.drawable.privacy_level_bg_line);

        mScanning = res.getDrawable(R.drawable.scanning);
        mScanningAppIcon = res.getDrawable(R.drawable.privacy_app_scanning_icon);
        mScanningPicIcon = res.getDrawable(R.drawable.privacy_pictures_scanning_icon);
        mScanningVideoIcon = res.getDrawable(R.drawable.privacy_video_scanning_icon);
        mScanningContactIcon = res.getDrawable(R.drawable.privacy_contact_scanning_icon);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
    }

    protected int getDrawPadding(Resources res) {
        return res.getDimensionPixelSize(R.dimen.privacy_level_padding);
    }

    protected int getSepratorPadding(Resources res) {
        return res.getDimensionPixelSize(R.dimen.privacy_level_seprator_padding);
    }

    protected int getSmallTextSize(Resources res) {
        return res.getDimensionPixelSize(R.dimen.privacy_level_small_text_size);
    }

    protected int getBigTextSize(Resources res) {
        return res.getDimensionPixelSize(R.dimen.privacy_level_big_text_size);
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
        int iconW = mIcon.getIntrinsicWidth();
        int iconH = mIcon.getIntrinsicHeight();
        float scaleW = (float) contentW / iconW;
        float scaleH = (float) contentH / iconH;
        float scale = scaleW < scaleH ? scaleW : scaleH;

        int drawW = (int) (iconW * scale);
        int drawH = (int) (iconH * scale);

        mIconDrawBount.left = centerX - drawW / 2;
        mIconDrawBount.right = mIconDrawBount.left + drawW;
        mIconDrawBount.top = centerY - drawH / 2;
        mIconDrawBount.bottom = mIconDrawBount.top + drawH;

        int scanningIconSize = drawH / 4;
        mScanningIconCenterRect.left = centerX - scanningIconSize / 2;
        mScanningIconCenterRect.right = mScanningIconCenterRect.left + scanningIconSize;
        mScanningIconCenterRect.top = centerY - scanningIconSize / 2;
        mScanningIconCenterRect.bottom = mScanningIconCenterRect.top + scanningIconSize;

        mScanningIconStartRect.right = mIconDrawBount.right;
        mScanningIconStartRect.left = mScanningIconStartRect.right - scanningIconSize;
        mScanningIconStartRect.top = mScanningIconCenterRect.top;
        mScanningIconStartRect.bottom = mScanningIconCenterRect.top + scanningIconSize;

        mScanningIconEndRect.left = mIconDrawBount.left;
        mScanningIconEndRect.right = mScanningIconEndRect.left + scanningIconSize;
        mScanningIconEndRect.top = mScanningIconCenterRect.top;
        mScanningIconEndRect.bottom =  mScanningIconCenterRect.top + scanningIconSize;
        
        if(!mLastIconDrawBount.equals(mIconDrawBount) && isShown()) {
            int maxTextWidth = (int)(drawW * SMALL_TEXT_WIDTH_PERCENT);
            int textSize = computeTextSize(mLevelText, mSmallTextSize, maxTextWidth, mPaint);
            mRealSmallTextSize = (int) mPaint.getTextSize();
            mPaint.getFontMetrics(mFontMetrics);
            int offset = (int) Math.ceil(mFontMetrics.descent - mFontMetrics.ascent) + 1;
            mSmallTextPoint.set(mIconDrawBount.left + (drawW - textSize) / 2, mIconDrawBount.top
                    + ((int) (drawH * SMALL_TEXT_POS_PERCENT)) + offset);

            int sepratorW = (int) (drawW * SEPRATOR_WIDTH_PERCENT);
            mSepratorBound.left = centerX - sepratorW / 2;
            mSepratorBound.right = mSepratorBound.left + sepratorW;
            mSepratorPadding = (int) (mSepratorPadding * scale);
            if (mSepratorPadding < 5) {
                mSepratorPadding = 5;
            }
            mSepratorBound.top = mSmallTextPoint.y + mSepratorPadding;
            mSepratorBound.bottom = mSepratorBound.top + mSeprator.getIntrinsicHeight();

            mLastIconDrawBount.set(mIconDrawBount);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        // background
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Style.FILL);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);

        // icon background
        PrivacyHelper ph = PrivacyHelper.getInstance(getContext());
        mPaint.setColor(mColor == -1 ? ph.getCurLevelColor().toIntColor() : mColor);
        mPaint.setStyle(Style.FILL);
        canvas.drawRect(mIconDrawBount, mPaint);

        // icon
        mIcon.setBounds(mIconDrawBount);
        mIcon.draw(canvas);

        if (mScannAnimating) {
            canvas.save();
            canvas.rotate(mScanDegree, mIconDrawBount.centerX(), mIconDrawBount.centerY());
            mScanning.setBounds(mIconDrawBount.left - mScanningOffset, mIconDrawBount.top
                    - mScanningOffset, mIconDrawBount.right + mScanningOffset,
                    mIconDrawBount.bottom + mScanningOffset);
            mScanning.draw(canvas);
            canvas.restore();
            if (mScanningItem != null) {
                mScanningItem.setBounds(mScanningIconRect);
                mScanningItem.setAlpha(mScanningItemAlpha);
                mScanningItem.draw(canvas);
            }

        } else {
            // small text
            mPaint.setTextSize(mRealSmallTextSize);
            mPaint.setStyle(Style.FILL);
            mPaint.setColor(Color.WHITE);
            canvas.drawText(mLevelText, mSmallTextPoint.x, mSmallTextPoint.y, mPaint);

            // separator
            mSeprator.setBounds(mSepratorBound);
            mSeprator.draw(canvas);

            // big text
            int drawW = mIconDrawBount.width();
            int maxTextWidth = (int)(drawW * BIG_TEXT_WIDTH_PERCENT);
            Level level = ph.getPrivacyLevel();
            String text = ph.getLevelDescription(level);
            if(mLastLevel != level) {
                mLastLevel = level;
                int textSize = computeTextSize(text, mBigTextSize, maxTextWidth, mPaint);
                mRealBigTextSize = (int)mPaint.getTextSize();
                mPaint.getFontMetrics(mFontMetrics);
                int offset =  (int) Math.abs(mFontMetrics.ascent) - 2;
                mBigTextPoint.set(mIconDrawBount.left + (drawW - textSize) / 2, mSepratorBound.top + mSepratorPadding + offset);
            }

            mPaint.setTextSize(mRealBigTextSize);
            mPaint.setStyle(Style.FILL);
            mPaint.setColor(Color.WHITE);
            canvas.drawText(text, mBigTextPoint.x, mBigTextPoint.y, mPaint);

            // animation
            mPaint.setAlpha(mAlpha);
            canvas.drawBitmap(mAnim, null, mIconDrawBount, mPaint);
            mPaint.setAlpha(255);
        }
    }

    private int computeTextSize(String text, int maxTextSize, int maxWidth, Paint paint) {
        int textSize = maxTextSize;
        paint.setTextSize(textSize);
        int textWidth = (int) paint.measureText(text);
        while (textWidth > maxWidth) {
            textSize = textSize - 3;
            paint.setTextSize(textSize);
            textWidth = (int) paint.measureText(text);
        }
        return textWidth;
    }

    public void getLevelRectOnScreen(Rect rect) {
        int[] pos = new int[2];
        getLocationOnScreen(pos);
        rect.set(mIconDrawBount);
        rect.offset(pos[0], pos[1]);
    }

    public void invalidate(int color) {
        mColor = color;
        invalidate();
    }

    public void palyAnim() {
        if (hasNoAnmation() || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        cancelAnim(true);
        mAnimator = new ValueAnimator();
        mAnimator.setDuration(LOCK_ANIM_DURATION);
        mAnimator.setFloatValues(1.0f, 0.0f);
        mAnimator.removeAllUpdateListeners();
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                final float percent = (Float) animation.getAnimatedValue();
                mAlpha = (int) (255 * percent);
                invalidate();
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsAnimating = true;
            }

            public void onAnimationEnd(Animator animation) {
                mAlpha = 255;
                mIsAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mAlpha = 255;
                mIsAnimating = false;
            }
        });
        mAnimator.start();
    }

    protected boolean hasNoAnmation() {
        return false;
    }

    public void cancelAnim(boolean both) {
        if (mAnimator != null) {
            mAnimator.end();
            mAnimator = null;
        }
        if (both && mScanAnimator != null) {
            mScanAnimator.cancel();
            mScanAnimator = null;
        }
        invalidate();
    }

    public void startScanning() {
        if (mScannAnimating) {
            return;
        }
        if (mScanAnimator != null) {
            mScanAnimator.cancel();
        }
        mScanAnimator = new ValueAnimator();
        mScanAnimator.setDuration(SCANNING_ANIM_DURATION);
        mScanAnimator.setIntValues(0, SCANNING_ANIM_DURATION);
        mScanAnimator.setInterpolator(new LinearInterpolator());
        mScanAnimator.removeAllUpdateListeners();
        mScanAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                mScanDegree = (float) value / SCANNING_ANIM_DURATION * 1080;
                if (mScanDegree > 720) {
                    mScanDegree = mScanDegree - 720;
                } else if (mScanDegree > 360) {
                    mScanDegree = mScanDegree - 360;
                }
                int size = mScanningIconCenterRect.width();
                int num = value / SCANNING_ONE_DRAW;
                mScanningItem = getScanningIcon(num);
                int delta = value - num * SCANNING_ONE_DRAW;
                int percent = delta * 100 / SCANNING_ONE_DRAW;
                if (percent < 40) {
                    float realPercent = ((float) percent) / 100 * 2.5f;
                    mScanningItemAlpha = (int) (255 * realPercent);
                    int left = mScanningIconStartRect.left
                            + (int) Math
                                    .round(((mScanningIconCenterRect.left - mScanningIconStartRect.left) * realPercent));
                    mScanningIconRect.set(left, mScanningIconCenterRect.top, left + size,
                            mScanningIconCenterRect.top + size);
                } else if (percent > 60) {
                    float realPercent = ((float) percent - 60) / 100 * 2.5f;
                    mScanningItemAlpha = (int) (255 * (1 - realPercent));
                    int left = mScanningIconCenterRect.left
                            + (int) Math
                                    .round(((mScanningIconEndRect.left - mScanningIconCenterRect.left) * realPercent));
                    mScanningIconRect.set(left, mScanningIconCenterRect.top, left + size,
                            mScanningIconCenterRect.top + size);
                } else {
                    mScanningItemAlpha = 255;
                    mScanningIconRect.set(mScanningIconCenterRect);
                }
                invalidate();
            }
        });
        mScanAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mScanningCancel = false;
                mScannAnimating = true;
            }

            public void onAnimationEnd(Animator animation) {
                mScannAnimating = false;
                if (!mScanningCancel && mScanningListener != null) {
                    mScanningListener.onScanningFinish();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mScanningCancel = true;
                mScannAnimating = false;
            }
        });
        mScanAnimator.start();
    }

    private Drawable getScanningIcon(int num) {
        switch (num) {
            case 1:
                return mScanningPicIcon;
            case 2:
                return mScanningVideoIcon;
            case 3:
                return mScanningContactIcon;
        }
        return mScanningAppIcon;
    }

    public void stopScanning() {
        if (mScanAnimator != null) {
            mScanAnimator.cancel();
        }
    }

    public void setScanningListener(ScanningListener listener) {
        mScanningListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                int upX = (int) event.getX();
                int upY = (int) event.getY();
                if (mIconDrawBount.contains(upX, upY)) {
                    performClick();
                }
                break;
        }
        cancelAnim(false);
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
