package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.leo.appmaster.R;
import com.leo.appmaster.home.HomeActivity;
import com.leo.appmaster.home.HomePrivacyFragment;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.utils.LeoLog;

/**
 * 首页上部分动画实现
 * Created by Jasper on 2015/10/10.
 */
public class HomeAnimView extends View {
    private static final String TAG = "HomeAnimView";

    private HomeAnimBgLayer mBackLayer;
    private HomeAnimShieldLayer mShieldLayer;
    private HomeAnimLoadingLayer mLoadingLayer;
    private HomeAnimStepLayer mStepLayer;

    private boolean mLayouted;

    private Runnable mRunnable;

    private boolean mClickDownOnShield;

    private int mSecurityScore;
    private PrivacyHelper mPrivacyHelper;

    private boolean mShowProcessLoading;

    private int mMaxStepOffsetY;
    private int mFinalMaxStepOffsetY;

    private boolean mMemoryLess = false;

    public HomeAnimView(Context context) {
        this(context, null);
    }

    public HomeAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBackLayer = new HomeAnimBgLayer(this);
        mShieldLayer = new HomeAnimShieldLayer(this);
        mLoadingLayer = new HomeAnimLoadingLayer(this);
        mStepLayer = new HomeAnimStepLayer(0, this);
        mPrivacyHelper = PrivacyHelper.getInstance(getContext());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBackLayer.setBounds(getLeft(), getTop(), getRight(), getBottom());

        Drawable drawable = getResources().getDrawable(R.drawable.ic_home_out_circle);
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Rect r = drawable.getBounds();
        LeoLog.i(TAG, "out circle bounds: " + r.toString() + " | w: " + width + " -- h: " + height);

        int tabH = getResources().getDimensionPixelSize(R.dimen.home_tab_height);
        int toolbarH = getResources().getDimensionPixelSize(R.dimen.toolbar_height);
        int shieldTotalH = h - tabH - toolbarH;
        int left = getLeft() + (w - width) / 2;

        int offsetY = getResources().getDimensionPixelSize(R.dimen.shield_offsetY);
        int top = getTop() + toolbarH + (shieldTotalH - height) / 2 + offsetY;
        Rect rect = new Rect(left, top, left + width, top + height);
        if (HomePrivacyFragment.sScreenHeight <= 320) {
            int cx = rect.centerX();
            int cy = rect.centerY();
            rect.scale(0.8f);
            rect.set(cx - rect.width() / 2, cy - rect.height() / 2, cx + rect.width() / 2, cy + rect.height() / 2);
        }
        mShieldLayer.setBounds(rect.left, rect.top, rect.right, rect.bottom);
        int emptyHeaderH = getResources().getDimensionPixelSize(R.dimen.pri_pro_header);
        int maxOffsetY = mShieldLayer.centerY() - emptyHeaderH / 2 - toolbarH / 2;
        int paddingX = getResources().getDimensionPixelSize(R.dimen.pp_shield_x_padding);
        int maxOffsetX = mShieldLayer.centerX() - paddingX - mShieldLayer.getWidth() / 3;
        mShieldLayer.setMaxOffsetY(maxOffsetY);
        mShieldLayer.setMaxOffsetX(maxOffsetX);

        mLoadingLayer.setBounds(getLeft(), getTop(), getRight(), getBottom());

        int stepW = getResources().getDimensionPixelSize(R.dimen.home_step_width);
        int stepH = getResources().getDimensionPixelSize(R.dimen.home_step_height);
        int sDiff = (w - stepW) / 2;
        int stepL = getLeft() + sDiff;
        int stepT = mBackLayer.centerY();
        mStepLayer.setBounds(stepL, stepT, stepL + stepW, stepT + stepH);
        int marginTop = getResources().getDimensionPixelSize(R.dimen.pri_pro_header);
        int finalMarginTop = getResources().getDimensionPixelSize(R.dimen.pri_pro_final_header);
        int marginBottom = getResources().getDimensionPixelSize(R.dimen.step_line_margin_bottom);
        mMaxStepOffsetY = stepT - marginTop + marginBottom;
        mFinalMaxStepOffsetY = stepT - finalMarginTop + marginBottom;

        mLayouted = true;
        if (mRunnable != null) {
            mRunnable.run();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mBackLayer.draw(canvas);
        if (mShowProcessLoading) {
            mLoadingLayer.draw(canvas);
        }
        mShieldLayer.draw(canvas);

        canvas.setMatrix(Matrix.IDENTITY_MATRIX);
        float offsetRatio = mShieldLayer.getShieldOffsetRatio();
        if (offsetRatio > 0 && offsetRatio <= 1 && mStepLayer.getTotalStepCount() > 1) {
            float offsetY = offsetRatio * mMaxStepOffsetY;
            float finalRatio = mShieldLayer.getShieldOffsetFinalRatio();
            if (offsetRatio == 1 && finalRatio > 0) {
                offsetY = mMaxStepOffsetY + (mFinalMaxStepOffsetY - mMaxStepOffsetY) * finalRatio;
            }
            canvas.translate(0, -offsetY);
            mStepLayer.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mShieldLayer.containsPointer(x, y)) {
                    mClickDownOnShield = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mClickDownOnShield && mShieldLayer.containsPointer(x, y)) {
                    int securityScore = mPrivacyHelper.getSecurityScore();
                    if (securityScore >= mSecurityScore) {
                        // 动画跑完了，才能进入深度扫描
                        HomeActivity activity = (HomeActivity) getContext();
                        activity.onShieldClick();
                    }
                }
                break;
        }

        if (mBackLayer.processTouch(event)) {
            return true;
        }
        return mClickDownOnShield;
    }

    public void startAfterLayout(Runnable runnable) {
        mRunnable = runnable;
    }

    public boolean isLayouted() {
        return mLayouted;
    }

    /**
     * 设置盾牌缩放比率
     *
     * @param shieldScaleRatio
     */
    public void setShieldScaleRatio(float shieldScaleRatio) {
        mShieldLayer.setShieldScale(shieldScaleRatio);
        invalidate();
    }

    /**
     * 设置外光环缩放比率
     *
     * @param outCircleScaleRatio
     */
    public void setOutCircleScaleRatio(float outCircleScaleRatio) {
        mShieldLayer.setOutCircleScaleRatio(outCircleScaleRatio);
        if (mMemoryLess) {
            invalidate();
        }
    }

    /**
     * 设置内光环缩放比率
     *
     * @param inCircleScaleRatio
     */
    public void setInCircleScaleRatio(float inCircleScaleRatio) {
        mShieldLayer.setInCircleScaleRatio(inCircleScaleRatio);
        if (mMemoryLess) {
            invalidate();
        }
    }

    /**
     * 设置周围光环旋转比例
     *
     * @param circleRotateRatio
     */
    public void setCircleRotateRatio(float circleRotateRatio) {
        mShieldLayer.setCircleRotateRatio(circleRotateRatio);
        mShieldLayer.setMemoryLess(mMemoryLess);
        invalidate();
    }

    /**
     * 设置内环、外环的透明度
     *
     * @param circleAlpha
     */
    public void setCircleAlpha(int circleAlpha) {
        mShieldLayer.setCircleAlpha(circleAlpha);
        if (mMemoryLess) {
            invalidate();
        }
    }

    /**
     * 设置盾牌上的得分
     *
     * @param securityScore
     */
    public void setSecurityScore(int securityScore) {
        mSecurityScore = securityScore;
        mBackLayer.setSecurityScore(securityScore);
        mShieldLayer.setSecurityScore(securityScore);
        invalidate();
    }

    public void setProgress(int progress) {
        mBackLayer.setProgress(progress);
    }

    public void setFastProgress(int fastProgress) {
        mBackLayer.setFastProgress(fastProgress);
        if (mMemoryLess) {
            invalidate();
        }
    }

    public void setShowColorProgress(boolean showProgress) {
        mBackLayer.setShowColorProgress(showProgress);
        if (mMemoryLess) {
            invalidate();
        }
    }

    public void setScanningPercent(int scanningPercent) {
        mShieldLayer.setScanningPercent(scanningPercent);
        if (mMemoryLess) {
            invalidate();
        }
    }

    public void setShieldOffsetY(int shieldOffsetY) {
        mShieldLayer.setShieldOffsetY(shieldOffsetY);
        if (mMemoryLess) {
            invalidate();
        }
    }

    public void setShieldOffsetX(int shieldOffsetX) {
        mShieldLayer.setShieldOffsetX(shieldOffsetX);
        if (mMemoryLess) {
            invalidate();
        }
    }

    public void setShowProcessLoading(boolean show, int loadType) {
        mShowProcessLoading = show;
        mLoadingLayer.setLoadType(loadType);
    }

    public void increaseCurrentStep() {
        mStepLayer.increaseCurrentStep();
    }

    public void setTotalStepCount(int totalStepCount) {
        mStepLayer.setTotalStepCount(totalStepCount, mMemoryLess);
    }

    public HomeAnimBgLayer getBgLayer() {
        return mBackLayer;
    }

    public HomeAnimLoadingLayer getLoadingLayer() {
        return mLoadingLayer;
    }

    public HomeAnimShieldLayer getShieldLayer() {
        return mShieldLayer;
    }

    public HomeAnimStepLayer getStepLayer() {
        return mStepLayer;
    }

    public int getFastArrowWidth() {
        return mBackLayer.getFastArrowWidth();
    }

    public int getToolbarColor() {
        return mBackLayer.getToolbarColor();
    }

    public void setLessMemory() {
        mMemoryLess = true;
    }
}
