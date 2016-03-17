package com.leo.appmaster.ui;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.DataUtils;
import com.leo.appmaster.utils.LeoLog;

/**
 * 首页上部分盾牌绘制、动画控制
 * Created by Jasper on 2015/10/10.
 */
public class HomeAnimShieldLayer extends AnimLayer {
    private static final String TAG = "HomeAnimShieldLayer";
    // 盾牌周围外光环最大比例
    public static final float MAX_OUT_CIRCLE_SCALE_RATIO = 1f;
    // 盾牌周围外光环最小比例
    public static final float MIN_OUT_CIRCLE_SCALE_RATIO = 0.7f;

    // 盾牌周围内光环最大比例
    public static final float MAX_IN_CIRCLE_SCALE_RATIO = 0.74f;
    // 盾牌周围内光环最小比例
    public static final float MIN_IN_CIRCLE_SCALE_RATIO = 0.5f;

    // 盾牌最小缩放比例
    public static final float MIN_SHIELD_SCALE_RATIO = 0.76f;
    // 盾牌最大缩放比例
    public static final float MAX_SHIELD_SCALE_RATIO = 1f;
    // 盾牌扫描时的比例
    public static final float SHIELD_SCANNING_RATIO = 0.65f;

    public static final float MAX_WAVE_RATIO = 1.6f;
    public static final float MIN_WAVE_RATIO = 0.8f;

    private int mMaxOffseteY;
    private int mMaxOffseteX;

    // 盾牌缩放比例
    private float mShieldScale = MAX_SHIELD_SCALE_RATIO;
    // 周围环旋转比例
    private float mCircleRotateRatio;
    // 外环缩放比例
    private float mOutCircleScaleRatio = MAX_OUT_CIRCLE_SCALE_RATIO;
    // 内环缩放比例
    private float mInCircleScaleRatio = MAX_IN_CIRCLE_SCALE_RATIO;

    private static final int SHADOW_COLOR = 0x19000000;
    private static final int SHADOW_Y = 10;
    private static final int SHADOW_RADIUS = 4;

    private int mCircleAlpha = 0;
    private int mShieldAlpha = 255;
    private int mInCircleAlpha = 0;
    private int mOutCircleAlpha = 0;
    private int mSecurityScore = 100;
    private float mScanningScale = 1f;

    private Matrix mOutCircleMatrix;
    private Matrix mInCircleMatrix;
    private Matrix mShieldMatrix;
    private Matrix mEmptyMatrix;
    private Matrix mWaveMatrix;
    private Matrix mScanningMatrix;

    private Matrix mScoreMatrix;

    private BitmapDrawable mOutCircleDrawable;
    private BitmapDrawable mInCircleDrawable;
    private BitmapDrawable mShieldDrawable;
    // 扫描时的虚线外框
    private BitmapDrawable mDashCircle;

    public float mCirclePx;
    private float mCirclePy;

    private float mShieldPx;
    private float mShieldPy;
    private int mShieldBgCircleMargin;
    private float mShieldBgRadius;

    private Rect mShieldBounds;

    private Paint mScorePaint;
    private Paint mTextPaint;
    private float[] mText0Pos;
    private float[] mText1Pos;
    private float[] mText2Pos;
    private String mPrivacyStatus;
    private float[] mPrivacyStatusPos;

    private Paint mPercentPaint;
    private Paint mLabelPaint;

    private int mPercentBaseY;

    private int mScoreSize;
    private int mStatusSize;

    private boolean mTouchHit;
    private int mScanningPercent = -1;

    public int mShieldOffsetY;
    public int mShieldOffsetX;

    private BitmapDrawable mWaveDrawable;
    // 盾牌周围的波浪，0 ~ 1
    private float mFirstWaveRatio;
    private float mSecondWaveRatio;
    private float mThirdWaveRatio;
    private float mFinalShieldRatio;
    private float mFinalTextRatio;
    private int mWhiteColor;
    private float mMaxFinalOffsetY;
    private int mDashAlpha;

    private boolean mMemoryLess =false;

    private ShieldFlipDecor mFlipDecor;
    private BurstDecor mBurstDecor;

    HomeAnimShieldLayer(HomeAnimView view) {
        super(view);
        mOutCircleMatrix = new Matrix();
        mInCircleMatrix = new Matrix();
        mShieldMatrix = new Matrix();
        mEmptyMatrix = new Matrix();
        mScoreMatrix = new Matrix();
        mScanningMatrix = new Matrix();

        Resources res = mParent.getResources();
        mPercentPaint = new Paint();
        mPercentPaint.setAntiAlias(true);
        mPercentPaint.setTypeface(Typeface.create(LightTextView.getLightFace(view.getContext()), Typeface.BOLD));
        mPercentPaint.setColor(res.getColor(R.color.white));
        mPercentPaint.setTextSize(res.getDimensionPixelSize(R.dimen.scan_percent));
        mPercentPaint.setTextAlign(Paint.Align.CENTER);

        mLabelPaint = new Paint();
        mLabelPaint.setAntiAlias(true);
        mLabelPaint.setColor(res.getColor(R.color.white));
        mLabelPaint.setTextSize(res.getDimensionPixelSize(R.dimen.scan_percent_label));

        mShieldBounds = new Rect();
        mInCircleDrawable = (BitmapDrawable) res.getDrawable(R.drawable.ic_home_in_circle);
        mOutCircleDrawable = (BitmapDrawable) res.getDrawable(R.drawable.ic_home_out_circle);
        mShieldDrawable = (BitmapDrawable) res.getDrawable(R.drawable.ic_home_shield);
        mDashCircle = (BitmapDrawable) res.getDrawable(R.drawable.ic_scan_dash_circle);
        mWaveDrawable = (BitmapDrawable) res.getDrawable(R.drawable.ic_privacy_wave);

        mShieldBgCircleMargin = res.getDimensionPixelSize(R.dimen.home_shield_bg_circle_margin);

        mScoreSize = res.getDimensionPixelSize(R.dimen.home_shield_score);
        mStatusSize = res.getDimensionPixelSize(R.dimen.home_shield_status);
        mScorePaint = new Paint();
        mScorePaint.setAntiAlias(true);
        mScorePaint.setTypeface(Typeface.create(LightTextView.getLightFace(view.getContext()), Typeface.BOLD));

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);

        mPrivacyStatus = res.getString(R.string.home_privacy_status);
        mMaxOffseteY = res.getDimensionPixelSize(R.dimen.scan_shield_offset);

        mWaveMatrix = new Matrix();

        mWhiteColor = res.getColor(R.color.white);
        mMaxFinalOffsetY = res.getDimensionPixelSize(R.dimen.toolbar_height) / 2;

        mFlipDecor = new ShieldFlipDecor();
        mBurstDecor = new BurstDecor();
        mFlipDecor.setParentLayer(this);
        mBurstDecor.setParentLayer(this);
    }

    public void setMaxOffsetY(int maxOffseteY) {
        mMaxOffseteY = maxOffseteY;
    }

    public void setMaxOffsetX(int maxOffsetX) {
        mMaxOffseteX = maxOffsetX;
    }

    public int getMaxOffsetY() {
        return mMaxOffseteY;
    }

    public int getMaxOffsetX() {
        return mMaxOffseteX;
    }

    @Override
    protected void onSizeChanged() {
        super.onSizeChanged();

        mOutCircleDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
        mInCircleDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());

        mCirclePx = (getLeft() + getRight()) / 2;
        mCirclePy = (getTop() + getBottom()) / 2;

        int w = mShieldDrawable.getIntrinsicWidth();
        int h = mShieldDrawable.getIntrinsicHeight();

        float topHalfRatio = 0.54f;
        int wdiffHalf = (getWidth() - w) / 2;
        int hdiff = (getHeight() - h);

        int l = getLeft() + wdiffHalf;
        int t = getTop() + ((int) (topHalfRatio * (float) hdiff));
        int r = l + w;
        int b = t + h;
        mShieldDrawable.setBounds(l, t, r, b);
        mShieldBounds.set(l, t, r, b);

        mWaveDrawable.setBounds(l, t, r, b);

        mShieldPx = mShieldBounds.centerX();
        mShieldPy = mShieldBounds.centerY();

        mShieldBgRadius = (getWidth() - mShieldBgCircleMargin * 2) / 2;

        int dashW = mDashCircle.getIntrinsicWidth();
        int dashH = mDashCircle.getIntrinsicHeight();
        int dashL = getLeft() + (getWidth() - dashW) / 2;
        int dashT = getTop() + (getHeight() - dashH) / 2;
        mDashCircle.setBounds(dashL, dashT, dashL + dashW, dashT + dashH);

        calculateScoreRect();
        calculatePercentRect();
    }

    @Override
    protected boolean processTouch(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mShieldBounds.contains(x, y)) {
                    mTouchHit = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                return mTouchHit;
        }

        return false;
    }

    private void calculateScoreRect() {
        String text0 = "9";
        String text1 = "99";
        String text2 = "100";

        RectF src = new RectF(getLeft(), getTop(), getRight(), getBottom());
        mText0Pos = getPointOfText(src, text0, mScoreSize);
        mText1Pos = getPointOfText(src, text1, mScoreSize);
        mText2Pos = getPointOfText(src, text2, mScoreSize);

        RectF rectF = new RectF(getLeft(), mShieldBounds.top, getRight(), mShieldBounds.centerY());
        mPrivacyStatusPos = getPointOfText(rectF, mPrivacyStatus, mStatusSize);
    }

    private void calculatePercentRect() {
        Paint.FontMetricsInt fmi = mPercentPaint.getFontMetricsInt();
        int baseY = (getHeight() - fmi.bottom + fmi.top) / 2 - fmi.top;

        mPercentBaseY = baseY + getTop();
    }

    private float[] getPointOfText(RectF src, String text, int textSize) {
        RectF areaRect = new RectF(src);
        RectF bounds = new RectF(src);
        mScorePaint.setTextSize(textSize);
        // measure text width
        bounds.right = mScorePaint.measureText(text, 0, text.length());
        // measure text height
        bounds.bottom = mScorePaint.descent() - mScorePaint.ascent();

        bounds.left += (areaRect.width() - bounds.right) / 2.0f;
        bounds.top += (areaRect.height() - bounds.bottom) / 2.0f;

        return new float[]{bounds.left, bounds.top - mScorePaint.ascent()};
    }

    @Override
    protected void draw(Canvas canvas) {
        float rotate = mCircleRotateRatio;
        float inCircleScale = mInCircleScaleRatio;
        float outCircleScale = mOutCircleScaleRatio;

        int shieldOffsetX = mShieldOffsetX;
        int shieldOffsetY = mShieldOffsetY;
        int inCircleAlpha = mInCircleAlpha;
        int outCircleAlpha = mOutCircleAlpha;

        int scanningPct = mScanningPercent;
//        if (circleAlpha != 0 && shieldOffsetY != mMaxOffseteY) {
            // 绘制外环
//            circleAlpha = (int) (((float) mMaxOffseteY - (float) shieldOffsetY) / (float) mMaxOffseteY * 255f);
        if (outCircleAlpha > 0 && scanningPct < 101) {
            mOutCircleMatrix.setRotate(rotate, mCirclePx, mCirclePy);
            mOutCircleMatrix.postScale(outCircleScale, outCircleScale, mCirclePx, mCirclePy);
            if (shieldOffsetY != 0) {
                mOutCircleMatrix.postTranslate(-shieldOffsetX, -shieldOffsetY);
            }
            canvas.setMatrix(mOutCircleMatrix);
            Paint paint = mOutCircleDrawable.getPaint();
            paint.setAlpha(outCircleAlpha);
            mOutCircleDrawable.draw(canvas);
        }

        if (inCircleAlpha > 0 && scanningPct < 101) {
            // 绘制内环
            mInCircleMatrix.setRotate(-rotate, mCirclePx, mCirclePy);
            mInCircleMatrix.postScale(inCircleScale, inCircleScale, mCirclePx, mCirclePy);
            if (shieldOffsetY != 0) {
                mInCircleMatrix.postTranslate(-shieldOffsetX, -shieldOffsetY);
            }
            canvas.setMatrix(mInCircleMatrix);
            Paint paint = mInCircleDrawable.getPaint();
            paint.setAlpha(inCircleAlpha);
            mInCircleDrawable.draw(canvas);
        }

        canvas.setMatrix(mEmptyMatrix);
        if (mScanningPercent == -1 || mScanningPercent > 100) {
            drawShieldScore(canvas);
            drawShieldWave(canvas);
        } else {
            if (mShieldAlpha < 255) {
                drawPercent(canvas);
            } else {
                mScanningPercent = -1;
                drawShieldScore(canvas);
                drawShieldWave(canvas);
            }
        }

    }

    private void drawShieldWave(Canvas canvas) {
        boolean dbg = false;
        // 绘制盾牌
        if (mFirstWaveRatio > 0 && mFirstWaveRatio < 3) {
            if (dbg) {
                LeoLog.i(TAG, "drawShieldWave, mFirstWaveRatio: " + mFirstWaveRatio);
            }
            drawWave(canvas, mFirstWaveRatio);
        }

        if (mSecondWaveRatio > 0 && mSecondWaveRatio < 1) {
            if (dbg) {
                LeoLog.i(TAG, "drawShieldWave, mSecondWaveRatio: " + mSecondWaveRatio);
            }
            drawWave(canvas, mSecondWaveRatio);
        }

        if (mThirdWaveRatio > 0 && mThirdWaveRatio < 1) {
            if (dbg) {
                LeoLog.i(TAG, "drawShieldWave, mThirdWaveRatio: " + mThirdWaveRatio);
            }
            drawWave(canvas, mThirdWaveRatio);
        }
    }

    private void drawWave(Canvas canvas, float ratio) {
        int shieldOffsetY = mShieldOffsetY;
        int shieldOffsetX = mShieldOffsetX;

        float scale = MIN_WAVE_RATIO * (1f + ratio);
        int alpha = (int) (255f * (1f - ratio));
        mWaveMatrix.setScale(scale, scale, mShieldPx, mShieldPy);
        mWaveMatrix.postTranslate(-shieldOffsetX, -shieldOffsetY);
        canvas.setMatrix(mWaveMatrix);
        mWaveDrawable.getPaint().setAlpha(alpha);

        mWaveDrawable.draw(canvas);
    }

    private void drawPercent(Canvas canvas) {
        int dashAlpha = mDashAlpha;
        int shieldOffsetX = mShieldOffsetX;
        int shieldOffsetY = mShieldOffsetY;
        float scanningScale = mScanningScale;
        mScanningMatrix.setScale(scanningScale, scanningScale, mCirclePx, mCirclePy);
        mScanningMatrix.postTranslate(-shieldOffsetX, -shieldOffsetY);
        canvas.setMatrix(mScanningMatrix);
        float centerX = (getLeft() + getRight()) / 2;
        float centerY = (getTop() + getBottom()) / 2;

        mPercentPaint.setAlpha(dashAlpha);
        String percent = mScanningPercent + "";
        canvas.drawText(percent, centerX, mPercentBaseY, mPercentPaint);
        Rect rect = new Rect();
        mPercentPaint.getTextBounds(percent, 0, percent.length(), rect);

        float x = centerX + rect.width() / 2;
        canvas.drawText("%", x + 5, mPercentBaseY, mLabelPaint);

        float rotate = mCircleRotateRatio;
        canvas.rotate(-rotate, centerX, centerY);
        mDashCircle.getPaint().setAlpha(dashAlpha);
        mDashCircle.draw(canvas);
    }

    private void drawShieldScore(Canvas canvas) {
        int shieldOffsetY = mShieldOffsetY;
        int shieldOffsetX = mShieldOffsetX;

        HomeAnimView parent = (HomeAnimView) mParent;
        int score = mSecurityScore;
        // 绘制盾牌
        float shieldScale = mShieldScale;

        int textColor = parent.getToolbarColor();
        int shieldAlpha = 255;
        float finalShieldRatio = mFinalShieldRatio;
        if (finalShieldRatio > 0 && finalShieldRatio <= 1) {
            shieldScale *= 1 - finalShieldRatio;
            textColor = DataUtils.getGradientColor(finalShieldRatio, textColor, mWhiteColor);

            shieldAlpha = (int) (255f * (1f - finalShieldRatio));
            shieldOffsetY += finalShieldRatio * mMaxFinalOffsetY;
        } else /*if (mShieldAlpha != 0)*/ {
            shieldAlpha = mShieldAlpha;
        }
        mShieldMatrix.set(Matrix.IDENTITY_MATRIX);
        mFlipDecor.applyDecor(canvas, mShieldMatrix);//TODO
        mShieldMatrix.postScale(shieldScale, shieldScale, mShieldPx, mShieldPy);
        mShieldMatrix.postTranslate(-shieldOffsetX, -shieldOffsetY);
        canvas.setMatrix(mShieldMatrix);

        mScorePaint.setColor(textColor);
        mTextPaint.setColor(textColor);
        if (shieldAlpha > 0) {
            if (shieldOffsetY <= 0) {
                canvas.drawCircle(mCirclePx, mCirclePy, mShieldBgRadius, mScorePaint);
            }
            mShieldDrawable.getPaint().setAlpha(shieldAlpha);
            mShieldDrawable.draw(canvas);
        }

        float finalTextRatio = mFinalTextRatio;
        if (finalTextRatio > MIN_SHIELD_SCALE_RATIO) {
            mScoreMatrix.setScale(finalTextRatio, finalTextRatio, mShieldPx, mShieldPy);
            mScoreMatrix.postTranslate(-shieldOffsetX, -shieldOffsetY);
            canvas.setMatrix(mScoreMatrix);
        }
        float[] pointer = null;
        if (score < 10) {
            pointer = mText0Pos;
        } else if (score == 100) {
            pointer = mText2Pos;
        } else {
            pointer = mText1Pos;
        }
        mScorePaint.setTextSize(mScoreSize);
        
        canvas.drawText(score + "", pointer[0], pointer[1], mScorePaint);

        if (shieldAlpha > 0) {
            canvas.setMatrix(mShieldMatrix);
            mTextPaint.setTextSize(mStatusSize);
            canvas.drawText(mPrivacyStatus, mPrivacyStatusPos[0], mPrivacyStatusPos[1], mTextPaint);
        }
        mBurstDecor.applyDecor(canvas, null);
    }

    public void startMaxScoreAnim() {
        mFlipDecor.startFlipAnim(500, new ShieldFlipDecor.OnFlipEndListener() {
            @Override
            public void OnFlipEnd() {
                mBurstDecor.startBurstAnim(500, new BurstDecor.OnBurstEndListener() {
                    @Override
                    public void OnBurstEnd() {

                    }
                });
            }
        });
    }

    public ShieldFlipDecor getFlipDecor() {
        return mFlipDecor;
    }

    public BurstDecor getBurstDecor() {
        return mBurstDecor;
    }

    /**
     * 设置盾牌缩放比率
     *
     * @param shieldScale
     */
    public void setShieldScale(float shieldScale) {
        mShieldScale = shieldScale;
    }

    public float getShieldScale() {
        return mShieldScale;
    }

    /**
     * 设置外光环缩放比率
     *
     * @param outCircleScaleRatio
     */
    public void setOutCircleScaleRatio(float outCircleScaleRatio) {
        mOutCircleScaleRatio = outCircleScaleRatio;
    }

    public float getOutCircleScaleRatio() {
        return mOutCircleScaleRatio;
    }

    /**
     * 设置内光环缩放比率
     *
     * @param inCircleScaleRatio
     */
    public void setInCircleScaleRatio(float inCircleScaleRatio) {
        mInCircleScaleRatio = inCircleScaleRatio;
    }

    /**
     * 设置周围光环旋转比例
     *
     * @param circleRotateRatio
     */
    public void setCircleRotateRatio(float circleRotateRatio) {
        mCircleRotateRatio = circleRotateRatio;
    }

    /**
     * 设置内环、外环的透明度
     *
     * @param circleAlpha
     */
    public void setCircleAlpha(int circleAlpha) {
        mCircleAlpha = circleAlpha;
    }

    /**
     * 设置盾牌上的得分
     *
     * @param securityScore
     */
    public void setSecurityScore(int securityScore) {
        mSecurityScore = securityScore;
    }

    /**
     * 设置隐私扫描百分比
     *
     * @param scanningPercent
     */
    public void setScanningPercent(int scanningPercent) {
        mScanningPercent = scanningPercent;
    }

    public void setShieldOffsetY(int shieldOffsetY) {
        mShieldOffsetY = shieldOffsetY;
    }

    public int getShieldOffsetY() {
        return mShieldOffsetY;
    }

    public int getShieldOffsetX() {
        return mShieldOffsetX;
    }
    
    public void setShieldOffsetX(int shieldOffsetX) {
        mShieldOffsetX = shieldOffsetX;
    }

    public float getShieldOffsetRatio() {
//        float shieldOffsetY = mShieldOffsetY;
//        float maxShieldOffsetY = mMaxOffseteY;
//        return shieldOffsetY / maxShieldOffsetY;
        float shieldOffsetX = mShieldOffsetX;
        float maxShieldOffsetY = mMaxOffseteY;
        return (maxShieldOffsetY - shieldOffsetX) / maxShieldOffsetY;
    }

    public float getShieldOffsetFinalRatio() {
        return mFinalShieldRatio;
    }

    public boolean containsPointer(int x, int y) {
        return mShieldBounds.contains(x, y);
    }

    public void setFirstWaveRatio(float firstWaveRatio) {
        mFirstWaveRatio = firstWaveRatio;
        if (mMemoryLess) {
            mParent.invalidate();
        }
    }

    public void setSecondWaveRatio(float secondWaveRatio) {
        mSecondWaveRatio = secondWaveRatio;
        if (mMemoryLess) {
            mParent.invalidate();
        }
    }

    public void setThirdWaveRatio(float thirdWaveRatio) {
        mThirdWaveRatio = thirdWaveRatio;
        if (mMemoryLess) {
            mParent.invalidate();
        }
    }

    /**
     * 设置盾牌在隐私等级完成页面的比率，从0 ~ 1
     *
     * @param finalShieldRatio
     */
    public void setFinalShieldRatio(float finalShieldRatio) {
        mFinalShieldRatio = finalShieldRatio;
        if (mMemoryLess) {
            mParent.invalidate();
        }
    }

    /**
     * 设置盾牌在隐私等级完成页面的文本比率，从0.76 ~ 1.34 ~ 1.0
     *
     * @param finalTextRatio
     */
    public void setFinalTextRatio(float finalTextRatio) {
        mFinalTextRatio = finalTextRatio;
        if (mMemoryLess) {
            mParent.invalidate();
        }
    }

    /**
     * 设置盾牌透明度
     * @param shieldAlpha
     */
    public void setShieldAlpha(int shieldAlpha) {
        mShieldAlpha = shieldAlpha;
        mParent.invalidate();
    }

    /**
     * 设置内环透明度
     * @param inCircleAlpha
     */
    public void setInCircleAlpha(int inCircleAlpha) {
        mInCircleAlpha = inCircleAlpha;
        mParent.invalidate();
    }

    /**
     * 设置外环透明度
     * @param outCircleAlpha
     */
    public void setOutCircleAlpha(int outCircleAlpha) {
        mOutCircleAlpha = outCircleAlpha;
        mParent.invalidate();
    }

    public void setMemoryLess(boolean isMemoryLess) {
        mMemoryLess = isMemoryLess;
    }

    /**
     * 设置扫描百分比
     * @param scanningScale
     */
    public void setScanningScale(float scanningScale) {
        mScanningScale = scanningScale;
        mParent.invalidate();
    }

    /**
     * 设置虚线框透明度
     * @param dashAlpha
     */
    public void setDashAlpha(int dashAlpha) {
        mDashAlpha = dashAlpha;
        mParent.invalidate();
    }
}
