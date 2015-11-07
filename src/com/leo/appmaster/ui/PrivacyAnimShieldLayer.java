package com.leo.appmaster.ui;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.leo.appmaster.R;
import com.leo.appmaster.privacy.PrivacyHelper;

/**
 * 隐私处理盾牌显示及动画控制
 * Created by Jasper on 2015/10/15.
 */
public class PrivacyAnimShieldLayer extends AnimLayer {
    // 盾牌最小缩放比例
    public static final float MIN_SHIELD_SCALE_RATIO = 0.76f;

    private Drawable mShieldDrawable;

    private int mScoreSize;
    private int mStatusSize;

    private Paint mTextPaint;
    private float[] mText0Pos;
    private float[] mText1Pos;
    private float[] mText2Pos;
    private String mPrivacyStatus;
    private float[] mPrivacyStatusPos;

    private int mSecurityScore = 100;

    private PrivacyHelper mPrivacyHelper;

    PrivacyAnimShieldLayer(View view) {
        super(view);

        Resources res = view.getResources();
        mShieldDrawable = res.getDrawable(R.drawable.ic_home_shield);
        mScoreSize = res.getDimensionPixelSize(R.dimen.home_shield_score);
        mStatusSize = res.getDimensionPixelSize(R.dimen.home_shield_status);
        mPrivacyStatus = res.getString(R.string.home_privacy_status);
        mPrivacyHelper = PrivacyHelper.getInstance(view.getContext());

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged() {
        super.onSizeChanged();

        mShieldDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
        mSecurityScore = mPrivacyHelper.getSecurityScore();
        calculateScoreRect();
    }

    @Override
    protected void draw(Canvas canvas) {
        mShieldDrawable.draw(canvas);

        PrivacyProcessHeader parent = (PrivacyProcessHeader) mParent;
        mTextPaint.setColor(parent.getToolbarColor());
        int score = mSecurityScore;
        float[] pointer = null;
        if (score < 10) {
            pointer = mText0Pos;
        } else if (score == 100) {
            pointer = mText2Pos;
        } else {
            pointer = mText1Pos;
        }
        mTextPaint.setTextSize(mScoreSize);
        canvas.drawText(score + "", pointer[0], pointer[1], mTextPaint);

        mTextPaint.setTextSize(mStatusSize);
        canvas.drawText(mPrivacyStatus, mPrivacyStatusPos[0], mPrivacyStatusPos[1], mTextPaint);
    }

    private void calculateScoreRect() {
        String text0 = "9";
        String text1 = "99";
        String text2 = "100";

        RectF src = new RectF(getLeft(), getTop(), getRight(), getBottom());
        mText0Pos = getPointOfText(src, text0, mScoreSize);
        mText1Pos = getPointOfText(src, text1, mScoreSize);
        mText2Pos = getPointOfText(src, text2, mScoreSize);

        RectF rectF = new RectF(getLeft(), getTop(), getRight(), (getTop() + getBottom()) / 2);
        mPrivacyStatusPos = getPointOfText(rectF, mPrivacyStatus, mStatusSize);
    }

    private float[] getPointOfText(RectF src, String text, int textSize) {
        RectF areaRect = new RectF(src);
        RectF bounds = new RectF(src);
        mTextPaint.setTextSize(textSize);
        // measure text width
        bounds.right = mTextPaint.measureText(text, 0, text.length());
        // measure text height
        bounds.bottom = mTextPaint.descent() - mTextPaint.ascent();

        bounds.left += (areaRect.width() - bounds.right) / 2.0f;
        bounds.top += (areaRect.height() - bounds.bottom) / 2.0f;

        return new float[] { bounds.left, bounds.top - mTextPaint.ascent() };
    }
}
