package com.leo.appmaster.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Pair;

import com.leo.appmaster.R;
import com.leo.appmaster.privacy.PrivacyHelper;
import com.leo.appmaster.utils.LeoLog;

/**
 * 首页上部分动画背景层实现、动画控制
 * Created by Jasper on 2015/10/10.
 */
public class HomeAnimBgLayer extends AnimLayer {
    private static final String TAG = "HomeAnimBgLayer";
    private static final boolean DBG = false;

    private static final int PROGRESS_BG_ALPHA = 51;

    private Shader mBgShader;
    private Shader mProgressShader;
    private Paint mPaint;
    private Paint mProgressBgPaint;
    private Paint mProgressPaint;
    private PrivacyHelper mPrivacyHelper;

    private Pair<Integer, Integer> mColorPair;

    private Rect mProgressBounds;
    private Drawable mArrowDrawable;
    private Drawable mFastArrow;
    private BitmapDrawable mRepeatFg;

    private int mProgress;
    private int mFastProgress;
    private int mProgressGap;

    private int mProgressBottom;

    private boolean mIsShowProgress = true;

    private Rect mRepeatRect;
    private int mTargetScore;
    private boolean mIncrease;
    private int mTabHeight;

    HomeAnimBgLayer(HomeAnimView view) {
        super(view);

        mProgressBounds = new Rect();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);

        mProgressBgPaint = new Paint();
        mProgressBgPaint.setAntiAlias(true);
        mProgressBgPaint.setColor(Color.BLACK);
        mProgressBgPaint.setAlpha(PROGRESS_BG_ALPHA);

        mPrivacyHelper = PrivacyHelper.getInstance(view.getContext());

        mArrowDrawable = view.getResources().getDrawable(R.drawable.ic_home_progress_arrow);
        mFastArrow = view.getResources().getDrawable(R.drawable.ic_home_fast_arrow);
        mProgressGap = view.getResources().getDimensionPixelSize(R.dimen.home_arrow_gap);

        mRepeatFg = (BitmapDrawable) view.getResources().getDrawable(R.drawable.ic_home_color_repeat);
        mTabHeight = view.getResources().getDimensionPixelSize(R.dimen.home_tab_height);
    }

    @Override
    protected void onSizeChanged() {
        super.onSizeChanged();

        mProgressBottom = getHeight() - mTabHeight;

        if (mBgShader == null) {
            int score = 100;
            mColorPair = mPrivacyHelper.getColorPairByScore(score);
            mBgShader = new LinearGradient(getLeft(), getTop(), getLeft(), getBottom(),
                    mColorPair.first, mColorPair.second, Shader.TileMode.REPEAT);
        }

        int progressH = mParent.getContext().getResources().getDimensionPixelSize(R.dimen.home_anim_progress);
        mProgressBounds.set(getLeft(), mProgressBottom - progressH, getRight(), mProgressBottom);
        LeoLog.d(TAG, "progress bounds: " + mProgressBounds + " | mProgressBottom: " + mProgressBottom +
                " | getHeight: " + getHeight() + " | tabH: " + mTabHeight);

        int[] colors = new int[]{
                Color.parseColor("#2797ff"),
                Color.parseColor("#00c0ff"),
                Color.parseColor("#44ec38"),
                Color.parseColor("#ffa619"),
                Color.parseColor("#ff3636")
        };
        mProgressShader = new LinearGradient(mProgressBounds.left, mProgressBounds.top, mProgressBounds.right,
                mProgressBounds.top, colors, null, Shader.TileMode.REPEAT);

        int arrowW = mArrowDrawable.getIntrinsicWidth();
        int arrowH = mArrowDrawable.getIntrinsicHeight();
        int top = mProgressBounds.top - (arrowH - mProgressBounds.height()) / 2;
        int left = getLeft() + getWidth() - arrowW;
        mArrowDrawable.setBounds(left, top, left + arrowW, top + arrowH);

        int repeatW = mRepeatFg.getIntrinsicWidth();
        int repeatH = mRepeatFg.getIntrinsicHeight();
//        mRepeatFg.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
        mRepeatFg.setBounds(getLeft(), mProgressBounds.top, getLeft() + repeatW, mProgressBounds.top + repeatH);
        mRepeatRect = mRepeatFg.getBounds();

        int fastW = mFastArrow.getIntrinsicWidth();
        int fastH = mFastArrow.getIntrinsicHeight();
        int fastL = getLeft() + getWidth() - fastW;
        int fastT = mProgressBounds.top;
        mFastArrow.setBounds(fastL, fastT, fastL + fastW, fastT + fastH);
    }

    @Override
    protected void draw(Canvas canvas) {
        if (mPaint.getShader() == null) {
            mPaint.setShader(mBgShader);
        }
        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), mPaint);

        if (mProgressPaint.getShader() == null) {
            mProgressPaint.setShader(mProgressShader);
        }

        if (mIsShowProgress) {
            canvas.save();
            int progress = mProgress;
            if (progress < getWidth()) {
                int color = ((HomeAnimView) mParent).getToolbarColor();
                mProgressBgPaint.setColor(color);
                canvas.drawRect(mProgressBounds, mProgressBgPaint);
                canvas.translate(-getWidth() + progress, 0);
            }
            canvas.drawRect(mProgressBounds, mProgressPaint);
            if (progress < getWidth()) {
                canvas.save();
                int count = getWidth() / mRepeatRect.width();
                for (int i = 0; i < count; i++) {
                    mRepeatFg.draw(canvas);
                    canvas.translate(mRepeatRect.width(), 0);
                }
                canvas.restore();

                // 滚动条箭头签名有部分阴影区域，所以需要往前平移
                canvas.translate(mProgressGap, 0);
                mArrowDrawable.draw(canvas);
            }
            if (progress < getWidth()) {
            }
            canvas.restore();

            int fastProgress = mFastProgress;
            if (fastProgress < getWidth() + mFastArrow.getIntrinsicWidth()) {
                canvas.save();
                canvas.translate(-getWidth() + fastProgress, 0);
                mFastArrow.draw(canvas);
                canvas.restore();
            }
        }
    }

    public void setIncrease(boolean increase) {
        mIncrease = increase;
    }

    /**
     * 设置盾牌上的得分
     *
     * @param securityScore
     */
    public void setSecurityScore(int securityScore) {
        Pair<Integer, Integer> pair = mPrivacyHelper.getColorPairByScore(securityScore);

        int fromUp = pair.first;
        int fromDown = pair.second;

        Pair<Integer, Integer> to = mPrivacyHelper.getNextPair(securityScore, mIncrease);
        int toUp = to.first;
        int toDown = to.second;

        float ratio = mPrivacyHelper.getGradientProgress(securityScore, mIncrease);

        int up = pair.first;
        int down = pair.second;
        if (!mPrivacyHelper.isSameLevel(securityScore, mTargetScore)) {
            up = getProgressColor(ratio, fromUp, toUp);
            down = getProgressColor(ratio, fromDown, toDown);
        }
        if (DBG) {
            LeoLog.i(TAG, "up: " + up + " | down: " + down + " | ratio: " + ratio +
                    " | securityScore: " + securityScore);
        }

        mBgShader = new LinearGradient(getLeft(), getTop(), getLeft(), getBottom(),
                up, down, Shader.TileMode.REPEAT);
        mPaint.setShader(mBgShader);
        mColorPair = new Pair<Integer, Integer>(up, down);

    }

    private int getProgressColor(float progress, int from, int to) {
        if (from == to) return from;

        int fromR = Color.red(from);
        int fromG = Color.green(from);
        int fromB = Color.blue(from);

        int toR = Color.red(to);
        int toG = Color.green(to);
        int toB = Color.blue(to);
        int tarR = (int) (fromR + (toR - fromR) * progress);
        int tarG = (int) (fromG + (toG - fromG) * progress);
        int tarB = (int) (fromB + (toB - fromB) * progress);


        // -------------------------------
//        int fromR = Color.red(from);
//        int fromG = Color.green(from);
//        int fromB = Color.blue(from);
//
//        int toR = Color.red(to);
//        int toG = Color.green(to);
//        int toB = Color.blue(to);
//
//        int tarR = (int) (toR + (fromR - toR) * progress);
//        int tarG = (int) (toG + (fromG - toG) * progress);
//        int tarB = (int) (toB + (fromB - toB) * progress);

        return Color.rgb(tarR, tarG, tarB);
    }

    public void setProgress(int progress) {
        mProgress = progress;
    }

    public void setFastProgress(int fastProgress) {
        mFastProgress = fastProgress;
    }

    public int getFastArrowWidth() {
        return mFastArrow.getIntrinsicWidth();
    }

    public int getToolbarColor() {
        return mColorPair.first;
    }

    public void setShowColorProgress(boolean showProgress) {
        mIsShowProgress = showProgress;
    }

    public void setTargetScore(int targetScore) {
        mTargetScore = targetScore;
    }
}
