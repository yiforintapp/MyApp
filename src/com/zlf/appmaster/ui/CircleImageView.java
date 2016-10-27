
package com.zlf.appmaster.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CircleImageView extends ImageView {
    /**
     * 图片
     */
    private Bitmap mSrc;

    /**
     * 控件的宽度
     */
    private int mWidth;
    /**
     * 控件的高度
     */
    private int mHeight;


    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 1;
    private boolean isForApp = false;

    public CircleImageView(Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

//    /**
//     * 计算控件的高度和宽度
//     */
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        /**
//         * 设置宽度
//         */
//        int specMode = MeasureSpec.getMode(widthMeasureSpec);
//        int specSize = MeasureSpec.getSize(widthMeasureSpec);
//
//        // match_parent , accurate
//        if (specMode == MeasureSpec.EXACTLY) {
//            mWidth = specSize;
//        } else {
//            // 由图片决定的宽
//            int desireByImg = getPaddingLeft() + getPaddingRight() + mSrc.getWidth();
//            // wrap_content
//            if (specMode == MeasureSpec.AT_MOST) {
//                mWidth = Math.min(desireByImg, specSize);
//            }
//        }
//
//        /***
//         * 设置高度
//         */
//        specMode = MeasureSpec.getMode(heightMeasureSpec);
//        specSize = MeasureSpec.getSize(heightMeasureSpec);
//        // match_parent , accurate
//        if (specMode == MeasureSpec.EXACTLY) {
//            mHeight = specSize;
//        } else {
//            int desire = getPaddingTop() + getPaddingBottom() + mSrc.getHeight();
//            // wrap_content
//            if (specMode == MeasureSpec.AT_MOST) {
//                mHeight = Math.min(desire, specSize);
//            }
//        }
//        setMeasuredDimension(mWidth, mHeight);
//    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null || mSrc == null) {
            return;
        }

        int max = Math.max(getWidth(), getHeight());
//        /**
//         * 长度如果不一致，按小的值进行压缩
//         */
        int moreBig = max / 8;
        mSrc = Bitmap.createScaledBitmap(mSrc, max + moreBig, max + moreBig, true);
//        canvas.drawBitmap(mSrc, -mSrc.getWidth() / 2, -mSrc.getHeight() / 2, null);
        canvas.drawBitmap(createCircleImage(mSrc, max, moreBig), 0, 0, null);

    }


    /**
     * 根据原图和变长绘制圆形图片
     *
     * @param source
     * @param max
     * @param more
     */
    private Bitmap createCircleImage(Bitmap source, int max, int more) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.WHITE);
        Bitmap target = Bitmap.createBitmap(max, max, Bitmap.Config.ARGB_8888);
        /**
         * 产生一个同样大小的画布
         */
        Canvas canvas = new Canvas(target);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        /**
         * 首先绘制圆形
         */
        canvas.drawCircle(max / 2, max / 2, max / 2, paint);
        /**
         * 使用SRC_IN，参考上面的说明
         */
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        /**
         * 绘制图片
         */
        canvas.drawBitmap(source, -more / 2, -more / 2, paint);

        return target;
    }


    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mSrc = getBitmapFromDrawable(drawable);
        invalidate();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mSrc = bm;
        invalidate();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mSrc = getBitmapFromDrawable(getDrawable());
        invalidate();
    }

    public void setSelfImageDrawable(Drawable drawable, boolean isAppIcon) {
        setImageDrawable(drawable);
    }


    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION,
                        BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            super.onRestoreInstanceState(state);
        } catch (Exception e) {

        }
    }

}
