package com.leo.appmaster.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.leo.appmaster.R;

/**
 * Created by Jasper on 2015/10/15.
 */
public class PrivacyProcessHeader extends View {
    private int mShieldTopMargin;

    private PrivacyAnimBgLayer mBackLayer;
    private PrivacyAnimShieldLayer mShieldLayer;
    public PrivacyProcessHeader(Context context) {
        this(context, null);
    }

    public PrivacyProcessHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrivacyProcessHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mBackLayer = new PrivacyAnimBgLayer(this);
        mShieldLayer = new PrivacyAnimShieldLayer(this);
        mShieldTopMargin = getContext().getResources().getDimensionPixelSize(R.dimen.pri_pro_top_margin);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBackLayer.setBounds(getLeft(), getTop(), getRight(), getBottom());

        Drawable shield = getResources().getDrawable(R.drawable.ic_home_shield);
        int width = (int) (shield.getIntrinsicWidth() * PrivacyAnimShieldLayer.MIN_SHIELD_SCALE_RATIO);
        int height = (int) (shield.getIntrinsicHeight() * PrivacyAnimShieldLayer.MIN_SHIELD_SCALE_RATIO);

        int diff = (w - width) / 2;
        int left = getLeft() + diff;
        int top = getTop() + mShieldTopMargin;
        mShieldLayer.setBounds(left, top, left + width, top + height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mBackLayer.draw(canvas);
        mShieldLayer.draw(canvas);
    }

    public int getToolbarColor() {
        return mBackLayer.getToolbarColor();
    }
}
