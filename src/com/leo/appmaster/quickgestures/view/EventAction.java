
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.DipPixelUtil;
import com.leo.appmaster.utils.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * this action shows unread sms and call
 * 
 * @author zhangwenyang
 */
public class EventAction implements DecorateAction {
    private int mAlpha = 255;
    private Paint mNumberPaint;
    private int mNumber;
    final Drawable mNumBg;
    private Rect mDrawRect;
    private Context mContext;
    @Override
    public int getActionType() {
        return ACTION_EVENT;
    }

    @Override
    public void draw(Canvas canvas, View view) {
            draw(canvas, mNumber, view.getWidth(), 0);
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
    }

    public void setNumber(int num) {
        mNumber = num;
    }

    public EventAction(Context context, int number) {
        mNumBg = context.getResources().getDrawable(R.drawable.gesture_sms_red);
        mNumber = number;
        mContext=context;
    }

    /**
     * scrollX is mScrollX + getWidth()
     */
    public void draw(Canvas canvas, int number, int scrollX, int scrollY) {
        scrollX = scrollX - mNumBg.getIntrinsicWidth();

        mDrawRect = new Rect(scrollX, scrollY, scrollX + mNumBg.getIntrinsicWidth(), scrollY
                + mNumBg.getIntrinsicHeight());
        
        float offsetx = mContext.getResources().getDimension(R.dimen.gesture_item_readtips_offsetx);
        //canvas.translate(scrollX-33, scrollY);
        
        canvas.translate(scrollX-offsetx, scrollY);
        mNumBg.setBounds(0, 0, mNumBg.getIntrinsicWidth(), mNumBg.getIntrinsicHeight());
        mNumBg.draw(canvas);

        if (mNumberPaint == null) {
            initPaint();
        } else {
            mNumberPaint.setTextSize(22);
        }
        if (number > 0) {
            final int width = mNumBg.getIntrinsicWidth();
            final int height = mNumBg.getIntrinsicHeight();
            final String str = String.valueOf(number);
            int realW = (int) mNumberPaint.measureText(str);
            int left = (width - realW) / 2;
            int top = height / 2 + 3;

            if (number > 99) {
                int fontSize = 22;
                for (; fontSize >= 2; fontSize -= 2) {
                    mNumberPaint.setTextSize(fontSize);
                    realW = (int) mNumberPaint.measureText(str);
                    if (realW < width - 20) {
                        break;
                    }
                }
                left = (width - realW) / 2;
                top = height / 2 + 3;
            }
            canvas.drawText(String.valueOf(number), left, top, mNumberPaint);
            canvas.translate(-scrollX, -scrollY);
        }
    }

    private void initPaint() {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setTextSize(22);
        p.setShadowLayer(2.0f, 1.0f, 1.0f, Color.DKGRAY);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setColor(Color.WHITE);
        p.setAlpha(mAlpha);
        mNumberPaint = p;
    }

    @Override
    public Rect getDrawRect() {
        return mDrawRect;
    }

}
