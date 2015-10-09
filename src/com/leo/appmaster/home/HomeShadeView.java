
package com.leo.appmaster.home;

import com.leo.appmaster.R;
import com.leo.appmaster.eventbus.LeoEventBus;
import com.leo.appmaster.eventbus.event.PrivacyLevelChangeEvent;
import com.leo.appmaster.home.HomeColorUtil.ColorHolder;
import com.leo.appmaster.privacy.PrivacyHelper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

public class HomeShadeView extends View {

    public interface OnShaderColorChangedLisetner {
        public void onShaderColorChanged(int color);
    }

    private Drawable mLock, mPrivacy, mAppManager;
    private Drawable mShowing;
    private int mColor;
    private float mPosition;
    ColorHolder targetHolder;
    private int bgIconWidth, bgIconHeight;
    private int viewWidth = 0, viewHeight = 0;

    private OnShaderColorChangedLisetner mColorChangedListener;

    public HomeShadeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Resources res = getContext().getResources();
        mLock = res.getDrawable(R.drawable.top_bar_bg_lock_icon);
        mPrivacy = res.getDrawable(R.drawable.top_bar_bg_protction_icon);
        mAppManager = res.getDrawable(R.drawable.top_bar_bg_my_apps_icon);
        targetHolder = new ColorHolder();
    }

    @Override
    protected void onAttachedToWindow() {
        LeoEventBus.getDefaultBus().register(this);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        LeoEventBus.getDefaultBus().unregister(this);
        super.onDetachedFromWindow();
    }

    public void onEventMainThread(PrivacyLevelChangeEvent event) {
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw bg color
        ColorHolder colorHolder = PrivacyHelper.getInstance(getContext()).getCurLevelColor();
        if (mPosition < 1) {
            targetHolder.red = (int) (HomeColorUtil.sColorBlue.red + (colorHolder.red - HomeColorUtil.sColorBlue.red)
                    * mPosition);
            targetHolder.green = (int) (HomeColorUtil.sColorBlue.green + (colorHolder.green - HomeColorUtil.sColorBlue.green)
                    * mPosition);
            targetHolder.blue = (int) (HomeColorUtil.sColorBlue.blue + (colorHolder.blue - HomeColorUtil.sColorBlue.blue)
                    * mPosition);
        } else {
            targetHolder.red = (int) (colorHolder.red + (HomeColorUtil.sColorBlue.red - colorHolder.red)
                    * (mPosition - 1f));
            targetHolder.green = (int) (colorHolder.green + (HomeColorUtil.sColorBlue.green - colorHolder.green)
                    * (mPosition - 1f));
            targetHolder.blue = (int) (colorHolder.blue + (HomeColorUtil.sColorBlue.blue - colorHolder.blue)
                    * (mPosition - 1f));
        }
        mColor = targetHolder.toIntColor();
        canvas.drawColor(mColor);

        if (mColorChangedListener != null) {
            mColorChangedListener.onShaderColorChanged(mColor);
        }

        // draw bg icon
        if (mPosition < 0.5f) {
            mShowing = mLock;
        } else if (mPosition < 1.5f) {
            mShowing = mPrivacy;
        } else {
            mShowing = mAppManager;
        }

        bgIconWidth = mShowing.getIntrinsicWidth();
        bgIconHeight = mShowing.getIntrinsicHeight();

        if (viewWidth == 0) {
            viewWidth = this.getWidth();
            viewHeight = this.getHeight();
        }
        canvas.translate((viewWidth - bgIconWidth) / 2, 0);
        mShowing.setAlpha(caculateAlpha());
        mShowing.setBounds(0, 0, bgIconWidth, bgIconHeight);
        mShowing.draw(canvas);
        super.onDraw(canvas);
    }

    private int caculateAlpha() {
        if (mPosition < 0.5f) {
            return (int) (-255 / 0.5f * mPosition + 255);
        } else if (mPosition < 1f) {
            return (int) (255 / 0.5f * mPosition - 255);
        } else if (mPosition < 1.5f) {
            return (int) (-255 / 0.5f * (mPosition - 1) + 255);
        } else {
            return (int) (255 / 0.5f * (mPosition - 1) - 255);
        }
    }

    public void setColorChangedListener(OnShaderColorChangedLisetner listener) {
        mColorChangedListener = listener;
    }

    public void setPosition(float position) {
        mPosition = position;
        invalidate();
    }

    public int getCurColor() {
        return mColor;
    }
    
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {          
        }
    }
}
