package com.leo.appmaster.wifiSecurity.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Pair;
import android.view.View;

import com.leo.appmaster.home.HomeColor;
import com.leo.appmaster.ui.AnimLayer;

/**
 * Created by qili on 15-10-27.
 */
public class WifiBgLayer extends AnimLayer {
    private int bgColor = 101;
    private Paint mPaint;
    private Pair<Integer, Integer> mColorPair;
    private Shader mBgShader;

    WifiBgLayer(View view) {
        super(view);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

    }

    @Override
    protected void onSizeChanged() {
        super.onSizeChanged();
        int score = bgColor;
        mColorPair = getColorPairByScore(score);
        mBgShader = new LinearGradient(getLeft(), getTop(), getLeft(), getBottom(),
                mColorPair.first, mColorPair.second, Shader.TileMode.REPEAT);
    }

    @Override
    protected void draw(Canvas canvas) {
        mPaint.setShader(mBgShader);
        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), mPaint);
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;

        Pair<Integer, Integer> pair = getColorPairByScore(bgColor);

        int fromLow = pair.first;
        int fromHigh = pair.second;

        int score = bgColor - 20;
        Pair<Integer, Integer> to = getColorPairByScore(score < 0 ? 0 : score);
        int toLow = to.first;
        int toHight = to.second;

        float ratio = getGradientProgress(score);

        int low = getProgressColor(ratio, fromLow, toLow);
        int high = getProgressColor(ratio, fromHigh, toHight);

        mBgShader = new LinearGradient(getLeft(), getTop(), getLeft(), getBottom(),
                low, high, Shader.TileMode.REPEAT);
        mPaint.setShader(mBgShader);
        mColorPair = new Pair<Integer, Integer>(low, high);
    }

    public float getGradientProgress(int score) {
        int scoreSimple = score % 20;
        if (score == 100 || score == 80 || score == 60 || score == 40 || score == 20) {
            scoreSimple = 20;
        }
        float ratio = 1f - (float) scoreSimple / 20f;
        return ratio;
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

        return Color.rgb(tarR, tarG, tarB);
    }

    public Pair<Integer, Integer> getColorPairByScore(int score) {
        Pair<Integer, Integer> pair;
        if (score > 100) {
            pair = new Pair<Integer, Integer>(HomeColor.WIFI_NORMAL, HomeColor.WIFI_NORMAL);
        } else if (score > 80 && score <= 100) {
            pair = new Pair<Integer, Integer>(HomeColor.SAFE_PAGE_START, HomeColor.SAFE_PAGE_END);
        } else if (score > 60 && score <= 80) {
            pair = new Pair<Integer, Integer>(HomeColor.sLevel1ColorUp, HomeColor.sLevel1ColorDown);
        } else if (score > 40 && score <= 60) {
            pair = new Pair<Integer, Integer>(HomeColor.sLevel4ColorUp, HomeColor.sLevel4ColorDown);
        } else if (score > 20 && score <= 40) {
            pair = new Pair<Integer, Integer>(HomeColor.sLevel5ColorUp, HomeColor.sLevel5ColorDown);
        } else {
            pair = new Pair<Integer, Integer>(HomeColor.UNSAFE_PAGE_START, HomeColor.UNSAFE_PAGE_END);
        }
        return pair;
    }
}
