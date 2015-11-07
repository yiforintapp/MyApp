package com.leo.appmaster.ui;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Pair;

import com.leo.appmaster.privacy.PrivacyHelper;

/**
 * 隐私等级一级页面背景
 * Created by Jasper on 2015/10/15.
 */
public class PrivacyAnimBgLayer extends AnimLayer {
    private Shader mBgShader;
    private Paint mPaint;

    private Pair<Integer, Integer> mColorPair;
    private int mSecurityScore;

    private PrivacyHelper mPrivacyHelper;

    PrivacyAnimBgLayer(PrivacyProcessHeader view) {
        super(view);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPrivacyHelper = PrivacyHelper.getInstance(view.getContext());
    }

    @Override
    protected void onSizeChanged() {
        super.onSizeChanged();

        mSecurityScore = mPrivacyHelper.getSecurityScore();
        int score = mSecurityScore;
        mColorPair = mPrivacyHelper.getColorPairByScore(score);
        mBgShader = new LinearGradient(getLeft(), getTop(), getLeft(), getBottom(),
                mColorPair.first, mColorPair.second, Shader.TileMode.REPEAT);
    }

    @Override
    protected void draw(Canvas canvas) {
        if (mPaint.getShader() == null) {
            mPaint.setShader(mBgShader);
        }

        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), mPaint);
    }

    public int getToolbarColor() {
        return mColorPair.first;
    }
}
